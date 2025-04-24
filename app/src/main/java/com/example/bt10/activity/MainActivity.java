package com.example.bt10.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bt10.R;
import com.example.bt10.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @SuppressLint({"SetJavaScriptEnabled", "WebViewApiAvailability"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        binding.webView.getSettings().setLoadWithOverviewMode(true);
        binding.webView.getSettings().setUseWideViewPort(true);
        binding.webView.getSettings().setJavaScriptEnabled(true);
        binding.webView.setWebViewClient(new WebViewClient());
        binding.webView.getSettings().setBuiltInZoomControls(true);
        binding.webView.getSettings().setDomStorageEnabled(true);
        binding.webView.getSettings().setDatabaseEnabled(true);
        binding.webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        binding.webView.setWebChromeClient(new WebChromeClient());
        binding.webView.loadUrl("http://iotstar.vn:8081");

        // Xử lý sự kiện click cho button Next
        binding.btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Thêm chức năng khi click vào button Next ở đây
                // Ví dụ: chuyển sang trang tiếp theo hoặc tải URL mới
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }
}