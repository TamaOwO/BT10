package com.example.bt10.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.bt10.R;
import com.example.bt10.adapter.VideoAdapter;
import com.example.bt10.model.MessageVideoModel;
import com.example.bt10.model.VideoModelThay;
import com.example.bt10.retrofit.APIService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VideoShortFirebaseAPIActivity extends AppCompatActivity {

    private ViewPager2 viewPager2;
    private VideoAdapter videosAdapter;
    private List<VideoModelThay> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_video_short_firebase_apiactivity);
        viewPager2 = findViewById(R.id.vpager2);
        list = new ArrayList<>();
        getsVideo();
    }

    private void getsVideo(){
        APIService.serviceApi.getVideos().enqueue(new Callback<MessageVideoModel>() {
            @Override
            public void onResponse(Call<MessageVideoModel> call, Response<MessageVideoModel> response) {
                list = response.body().getResult();
                videosAdapter = new VideoAdapter(VideoShortFirebaseAPIActivity.this, list);
                viewPager2.setOrientation(ViewPager2.ORIENTATION_VERTICAL);
                viewPager2.setAdapter(videosAdapter);
            }

            @Override
            public void onFailure(Call<MessageVideoModel> call, Throwable t) {
                Log.e("TAG", "onFailure: " + t.getMessage());
            }
        });
    }

}