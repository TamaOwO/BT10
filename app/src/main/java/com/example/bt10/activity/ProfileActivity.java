package com.example.bt10.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.bt10.R;
import com.example.bt10.cloudinary.CloudinaryHelper;
import com.example.bt10.model.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    private static final int PICK_IMAGE_REQUEST = 100;

    private ImageView ivProfile;
    private TextView tvEmail, tvVideoCount, tvStatus;
    private ProgressBar progressBar;

    private FirebaseUser currentUser;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize views
        ivProfile = findViewById(R.id.ivProfile);
        tvEmail = findViewById(R.id.tvEmail);
        tvVideoCount = findViewById(R.id.tvVideoCount);
        tvStatus = findViewById(R.id.textView);
        progressBar = findViewById(R.id.progressBar2);
        progressBar.setVisibility(View.GONE);

        // Initialize Firebase
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize Cloudinary
        try {
            CloudinaryHelper.initCloudinary(getApplicationContext());
        } catch (Exception e) {
            Log.e(TAG, "Cloudinary init error", e);
            Toast.makeText(this, "Error initializing image service: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());

        // Set up listeners
        findViewById(R.id.btnChangeProfile).setOnClickListener(v -> openImageChooser());

        // Load user data
        loadUserData();
    }

    private void loadUserData() {
        progressBar.setVisibility(View.VISIBLE);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressBar.setVisibility(View.GONE);

                UserModel user = snapshot.getValue(UserModel.class);
                if (user != null) {
                    tvEmail.setText(user.getEmail());
                    tvVideoCount.setText("Video count: " + user.getVideoCount());

                    // Load profile image (default or custom)
                    loadProfileImage(user.getProfileImage());
                } else {
                    Log.w(TAG, "User data not found in database");
                    Toast.makeText(ProfileActivity.this,
                            "User data not found",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Failed to load user data", error.toException());
                Toast.makeText(ProfileActivity.this,
                        "Failed to load user data",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadProfileImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            // Use default image
            ivProfile.setImageResource(R.drawable.ic_person_pin);
        } else {
            // Add cache busting parameter to avoid caching issues
            String urlWithTimestamp = imageUrl + "?t=" + System.currentTimeMillis();
            Log.d(TAG, "Loading profile image: " + urlWithTimestamp);

            Glide.with(this)
                    .load(urlWithTimestamp)
                    .placeholder(R.drawable.ic_person_pin)
                    .error(R.drawable.ic_person_pin)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)  // Skip disk cache
                    .skipMemoryCache(true)  // Skip memory cache
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                    Target<Drawable> target, boolean isFirstResource) {
                            Log.e(TAG, "Glide load failed: " + (e != null ? e.getMessage() : ""));
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model,
                                                       Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            Log.d(TAG, "Image loaded successfully");
                            return false;
                        }
                    })
                    .circleCrop()
                    .into(ivProfile);
        }
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // Add permission flag
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST &&
                resultCode == RESULT_OK &&
                data != null &&
                data.getData() != null) {

            Uri imageUri = data.getData();
            Log.d(TAG, "Selected image URI: " + imageUri);
            uploadProfileImage(imageUri);
        }
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private Uri compressImage(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);

            // Compress to 80% quality
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);

            // Create a temporary file
            File outputFile = new File(getCacheDir(), "compressed_image.jpg");
            FileOutputStream fos = new FileOutputStream(outputFile);
            fos.write(baos.toByteArray());
            fos.close();

            Log.d(TAG, "Image compressed successfully");
            return Uri.fromFile(outputFile);
        } catch (Exception e) {
            Log.e(TAG, "Error compressing image", e);
            return imageUri; // Return original if compression fails
        }
    }

    private void uploadProfileImage(Uri imageUri) {
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        tvStatus.setText("Uploading image...");

        // Use user's UID as the public ID to overwrite previous image
        String publicId = "profile_" + currentUser.getUid();
        Log.d(TAG, "Starting upload with publicId: " + publicId + ", URI: " + imageUri);

        // Try to get a compressed version of the image
        Uri compressedUri = compressImage(imageUri);
        Log.d(TAG, "Compressed URI: " + compressedUri);

        CloudinaryHelper.uploadProfileImage(this, compressedUri, publicId, new UploadCallback() {
            @Override
            public void onStart(String requestId) {
                Log.d(TAG, "Upload started with requestId: " + requestId);
                runOnUiThread(() -> tvStatus.setText("Preparing upload..."));
            }

            @Override
            public void onProgress(String requestId, long bytes, long totalBytes) {
                int progress = (int) ((bytes * 100) / totalBytes);
                Log.d(TAG, "Upload progress: " + progress + "%");
                runOnUiThread(() -> tvStatus.setText("Uploading: " + progress + "%"));
            }

            @Override
            public void onSuccess(String requestId, Map resultData) {
                Log.d(TAG, "Upload success. Response: " + resultData);

                String imageUrl = (String) resultData.get("url");
                if (imageUrl == null) {
                    imageUrl = (String) resultData.get("secure_url");
                }

                final String finalUrl = imageUrl;
                if (finalUrl != null && !finalUrl.isEmpty()) {
                    saveProfileImageUrl(finalUrl);
                } else {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        tvStatus.setText("Upload failed: Invalid URL");
                        Toast.makeText(ProfileActivity.this,
                                "Invalid image URL received", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Invalid URL in response: " + resultData);
                    });
                }
            }

            @Override
            public void onError(String requestId, ErrorInfo error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    tvStatus.setText("Upload failed");
                    String errorMsg = "Error: " + error.getDescription() + " - Code: " + error.getCode();
                    Toast.makeText(ProfileActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Upload error: " + errorMsg + ", Details: " + error.toString());
                });
            }

            @Override
            public void onReschedule(String requestId, ErrorInfo error) {
                runOnUiThread(() -> {
                    tvStatus.setText("Retrying upload...");
                    Log.w(TAG, "Upload rescheduled: " + error.getDescription());
                });
            }
        });
    }

    private void saveProfileImageUrl(String imageUrl) {
        Log.d(TAG, "Saving image URL to Firebase: " + imageUrl);
        userRef.child("profileImage").setValue(imageUrl)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        tvStatus.setText("Upload complete");
                        // Load image with cache busting
                        String urlWithTimestamp = imageUrl + "?t=" + System.currentTimeMillis();
                        Glide.with(ProfileActivity.this)
                                .load(urlWithTimestamp)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .skipMemoryCache(true)
                                .circleCrop()
                                .into(ivProfile);
                        Toast.makeText(ProfileActivity.this,
                                "Profile image updated",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        tvStatus.setText("Save failed");
                        Toast.makeText(ProfileActivity.this,
                                "Failed to save image URL: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Failed to save image URL", task.getException());
                    }
                });
    }
}