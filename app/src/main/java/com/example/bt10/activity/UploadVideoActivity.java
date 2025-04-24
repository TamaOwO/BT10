package com.example.bt10.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.bt10.R;
import com.example.bt10.cloudinary.CloudinaryHelper;
import com.example.bt10.model.VideoModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Map;

public class UploadVideoActivity extends AppCompatActivity {
    private static final int PICK_VIDEO_REQUEST = 101;
    private static final int PERMISSION_REQUEST_CODE = 102;

    private Button btnSelectVideo, btnUpload;
    private VideoView videoPreview;
    private EditText etTitle, etDescription;
    private ProgressBar progressBar;
    private TextView tvStatus;

    private Uri videoUri;
    private String videoUrl;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_video);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        btnSelectVideo = findViewById(R.id.btnSelectVideo);
        btnUpload = findViewById(R.id.btnUpload);
        videoPreview = findViewById(R.id.videoPreview);
        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        progressBar = findViewById(R.id.progressBar);
        tvStatus = findViewById(R.id.tvStatus);

        checkPermissions();

        btnSelectVideo.setOnClickListener(v -> selectVideo());
        btnUpload.setOnClickListener(v -> uploadVideo());
    }
    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void selectVideo() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("video/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Select Video"), PICK_VIDEO_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_VIDEO_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            videoUri = data.getData();
            try {
                // Kiểm tra xem có thể đọc file không
                getContentResolver().openFileDescriptor(videoUri, "r");

                videoPreview.setVideoURI(videoUri);
                videoPreview.setVisibility(View.VISIBLE);
                videoPreview.start();
                btnUpload.setEnabled(true);
            } catch (Exception e) {
                tvStatus.setText("Cannot read video file: " + e.getMessage());
            }
        }
    }

    private void uploadVideo() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (title.isEmpty()) {
            etTitle.setError("Title is required");
            return;
        }

        if (videoUri == null) {
            tvStatus.setText("Please select a video first");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnUpload.setEnabled(false);
        tvStatus.setText("Uploading video...");

        // Tạo file tạm từ Uri trước khi upload
        try {
            // Lấy InputStream từ Uri
            InputStream inputStream = getContentResolver().openInputStream(videoUri);

            // Tạo file tạm
            File tempFile = File.createTempFile("video_upload", ".mp4", getCacheDir());
            FileOutputStream outputStream = new FileOutputStream(tempFile);

            // Copy dữ liệu
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            // Đóng stream
            outputStream.close();
            inputStream.close();

            // Upload file tạm
            CloudinaryHelper.uploadVideo(this, tempFile.getAbsolutePath(), currentUser.getUid(), new UploadCallback() {
                @Override
                public void onStart(String requestId) {
                    // Upload started
                }

                @Override
                public void onProgress(String requestId, long bytes, long totalBytes) {
                    runOnUiThread(() -> {
                        int progress = (int) ((bytes * 100) / totalBytes);
                        tvStatus.setText("Uploading: " + progress + "%");
                    });
                }

                @Override
                public void onSuccess(String requestId, Map resultData) {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        videoUrl = (String) resultData.get("url");
                        saveVideoToDatabase(title, description, videoUrl);

                        // Xóa file tạm sau khi upload xong
                        tempFile.delete();
                    });
                }

                @Override
                public void onError(String requestId, ErrorInfo error) {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        tvStatus.setText("Upload failed: " + error.getDescription());
                        btnUpload.setEnabled(true);

                        // Xóa file tạm nếu có lỗi
                        tempFile.delete();
                    });
                }

                @Override
                public void onReschedule(String requestId, ErrorInfo error) {
                    // Upload rescheduled
                }
            });

        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            tvStatus.setText("Error: " + e.getMessage());
            btnUpload.setEnabled(true);
        }
    }

    private void saveVideoToDatabase(String title, String description, String videoUrl) {
        String videoId = FirebaseDatabase.getInstance().getReference("videos").push().getKey();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        VideoModel video = new VideoModel();
        video.setTitle(title);
        video.setDesc(description);
        video.setUrl(videoUrl);
        video.setUserId(userId);
        video.setLikes(0);
        video.setDislikes(0);

        // Lưu video
        FirebaseDatabase.getInstance().getReference("videos")
                .child(videoId)
                .setValue(video)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Tăng videoCount cho user
                        increaseUserVideoCount(userId);
                        Toast.makeText(this, "Upload successful", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to save video", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void increaseUserVideoCount(String userId) {
        FirebaseDatabase.getInstance().getReference("users")
                .child(userId)
                .child("videoCount")
                .runTransaction(new Transaction.Handler() {
                    @NonNull
                    @Override
                    public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                        Integer currentCount = mutableData.getValue(Integer.class);
                        if (currentCount == null) {
                            mutableData.setValue(1);
                        } else {
                            mutableData.setValue(currentCount + 1);
                        }
                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                        // Xử lý sau khi hoàn thành
                    }
                });
    }
}