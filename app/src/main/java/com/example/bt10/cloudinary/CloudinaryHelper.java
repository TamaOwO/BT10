package com.example.bt10.cloudinary;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.cloudinary.Transformation;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.UploadCallback;
import com.example.bt10.R;

import java.util.HashMap;
import java.util.Map;

public class CloudinaryHelper {
    private static final String TAG = "CloudinaryHelper";
    private static boolean isInitialized = false;

    public static void initCloudinary(Context context) {
        if (!isInitialized) {
            try {
                Map<String, String> config = new HashMap<>();
                config.put("cloud_name", context.getString(R.string.cloudinary_cloud_name));
                config.put("api_key", context.getString(R.string.cloudinary_api_key));
                config.put("api_secret", context.getString(R.string.cloudinary_api_secret));
                MediaManager.init(context, config);
                isInitialized = true;
                Log.d(TAG, "Cloudinary initialized successfully");
            } catch (Exception e) {
                Log.e(TAG, "Error initializing Cloudinary", e);
                throw e;
            }
        }
    }

    public static void uploadVideo(Context context, String filePath, String userId, UploadCallback callback) {
        initCloudinary(context);

        String publicId = "video_shorts_" + userId + "_" + System.currentTimeMillis();

        MediaManager.get().upload(filePath)
                .option("resource_type", "video")
                .option("public_id", publicId)
                .callback(callback)
                .dispatch();
    }

    public static void uploadProfileImage(Context context, Uri imageUri, String publicId, UploadCallback callback) {
        initCloudinary(context);

        // Log for debugging
        Log.d(TAG, "Uploading profile image: " + imageUri + " with publicId: " + publicId);

        MediaManager.get().upload(imageUri)
                .option("public_id", publicId)
                .option("overwrite", true)
                .option("invalidate", true)
                .option("transformation", new Transformation()
                        .width(200)
                        .height(200)
                        .crop("fill")
                        .gravity("face"))
                .callback(callback)
                .dispatch();
    }

    public static String getProfileImageUrl(String userId) {
        return MediaManager.get().url()
                .transformation(new Transformation()
                        .width(200)
                        .height(200)
                        .crop("fill")
                        .gravity("face"))
                .generate("profile_" + userId);
    }
}
