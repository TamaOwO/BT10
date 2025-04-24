package com.example.bt10.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.bt10.R;
import com.example.bt10.adapter.VideoFirebaseAdapter;
import com.example.bt10.model.UserModel;
import com.example.bt10.model.VideoModel;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class VideoShortFirebaseActivity extends AppCompatActivity {

    private ViewPager2 viewPager2;
    private VideoFirebaseAdapter videosAdapter;
    private Button btnNext;
    private Button btnLogout;
    private FirebaseAuth mAuth;
    private Button btnUpload;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_video_short_firebase);
        viewPager2 = findViewById(R.id.vpager);

        getsVideo();

        btnNext = findViewById(R.id.btnNext);
        btnNext.setOnClickListener(v -> {
            Intent intent = new Intent(VideoShortFirebaseActivity.this, VideoShortFirebaseAPIActivity.class);
            startActivity(intent);
        });

        btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(VideoShortFirebaseActivity.this, LoginActivity.class));
            finish();
        });

        btnUpload = findViewById(R.id.btnUpload);
        btnUpload.setOnClickListener(v -> {
            startActivity(new Intent(VideoShortFirebaseActivity.this, UploadVideoActivity.class));
        });

        // Trong onCreate()
        ImageView ivProfile = findViewById(R.id.ivProfile);
        ivProfile.setOnClickListener(v -> {
            startActivity(new Intent(VideoShortFirebaseActivity.this, ProfileActivity.class));
        });

// Load ảnh profile từ Cloudinary
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    UserModel userModel = snapshot.getValue(UserModel.class);
                    if (userModel != null && userModel.getProfileImage() != null) {
                        Glide.with(VideoShortFirebaseActivity.this)
                                .load(userModel.getProfileImage())
                                .circleCrop()
                                .into(ivProfile);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Xử lý lỗi
                }
            });
        }
    }

    private void getsVideo() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("videos");
        FirebaseRecyclerOptions<VideoModel> options = new FirebaseRecyclerOptions.Builder<VideoModel>().setQuery(databaseReference, VideoModel.class).build();
        videosAdapter = new VideoFirebaseAdapter(options);
        viewPager2.setOrientation(ViewPager2.ORIENTATION_VERTICAL);
        viewPager2.setAdapter(videosAdapter);

    }

    @Override
    protected void onStart() {

        super.onStart();
        videosAdapter.startListening();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String email = user.getEmail();
            Toast.makeText(this, "Welcome, " + email, Toast.LENGTH_SHORT).show();
            // Hiển thị email hoặc các thông tin khác của người dùng
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        videosAdapter.stopListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
        videosAdapter.notifyDataSetChanged();
    }

}