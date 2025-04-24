package com.example.bt10.adapter;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.bt10.R;
import com.example.bt10.activity.ProfileActivity;
import com.example.bt10.model.UserModel;
import com.example.bt10.model.VideoModel;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

public class VideoFirebaseAdapter extends FirebaseRecyclerAdapter<VideoModel, VideoFirebaseAdapter.MyHolder> {
    private static final String TAG = "VideosFireBaseAdapter";
    private boolean isFav = false;


    public VideoFirebaseAdapter(@NonNull FirebaseRecyclerOptions<VideoModel> options) {
        super(options);
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_video_row, parent, false);
        MyHolder holder = new MyHolder(view);

        // Initialize VideoView and its listener only once
        holder.videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                holder.videoProgressBar.setVisibility(View.GONE);
                mediaPlayer.start();

                float videoRatio = mediaPlayer.getVideoWidth() / (float) mediaPlayer.getVideoHeight();
                float screenRatio = holder.videoView.getWidth() / (float) holder.videoView.getHeight();
                float scale = videoRatio / screenRatio;
                if (scale >= 1f) {
                    holder.videoView.setScaleX(scale);
                } else {
                    holder.videoView.setScaleY(1f / scale);
                }
            }
        });

        holder.videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mediaPlayer.start();
            }
        });

        holder.videoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isFav) {
                    holder.favorites.setImageResource(R.drawable.ic_favorite);
                    isFav = true;
                } else {
                    holder.favorites.setImageResource(R.drawable.ic_fill_favorite);
                    isFav = false;
                }
            }
        });

        // Xử lý click ảnh profile (moved here as it's a one-time setup for the ViewHolder)
        holder.imPerson.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ProfileActivity.class);
            v.getContext().startActivity(intent);
        });

        return holder;
    }

    @Override
    protected void onBindViewHolder(@NonNull MyHolder holder, int position, @NonNull VideoModel model) {
        holder.textVideoTitle.setText(model.getTitle());
        holder.textVideoDescription.setText(model.getDesc());
        String currentUrl = model.getUrl();
        Object videoTag = holder.videoView.getTag();

        if (!holder.isVideoLoaded || !String.valueOf(videoTag).equals(currentUrl)) {
            holder.videoView.setVideoURI(Uri.parse(currentUrl));
            holder.videoView.setTag(currentUrl);
            holder.isVideoLoaded = true;
        }


        holder.tvUserEmail.setVisibility(View.VISIBLE);



        // Lấy thông tin user từ bảng users
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(model.getUserId());
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserModel user = snapshot.getValue(UserModel.class);
                if (user != null) {
                    // Hiển thị thông tin user
                    holder.tvUserEmail.setText(user.getEmail());

                    // Hiển thị ảnh profile từ Cloudinary
                    if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
                        String urlWithTimestamp = user.getProfileImage() + "?t=" + System.currentTimeMillis();
                        Log.d(TAG, "Loading profile image in adapter: " + urlWithTimestamp);

                        Glide.with(holder.itemView.getContext())
                                .load(urlWithTimestamp)
                                .placeholder(R.drawable.ic_person_pin)
                                .error(R.drawable.ic_person_pin)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .skipMemoryCache(true)
                                .circleCrop()
                                .into(holder.imPerson);
                    } else {
                        holder.imPerson.setImageResource(R.drawable.ic_person_pin);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load user data", error.toException());
                Toast.makeText(holder.itemView.getContext(), "Failed to load user data", Toast.LENGTH_SHORT).show();
            }
        });

        // Hiển thị số lượt thích/không thích
        holder.tvLikeCount.setText(String.valueOf(model.getLikes()));
        holder.tvDislikeCount.setText(String.valueOf(model.getDislikes()));

        // Xử lý like/dislike clicks
        holder.ivLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLike(getRef(position).getKey(), model.getUserId());
            }
        });

        holder.ivDislike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleDislike(getRef(position).getKey(), model.getUserId());
            }
        });

        holder.imShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Implement share functionality
            }
        });
    }

    public static class MyHolder extends RecyclerView.ViewHolder {
        private final VideoView videoView;
        private final ProgressBar videoProgressBar;
        private final TextView textVideoTitle;
        private final TextView textVideoDescription;
        private final ImageView imPerson;
        private ImageView favorites;
        private final ImageView imShare;
        private final TextView tvUserEmail;
        private final ImageView ivLike, ivDislike;
        private final TextView tvLikeCount, tvDislikeCount;
        public boolean isVideoLoaded = false; // ✨ THÊM DÒNG NÀY


        public MyHolder(@NonNull View itemview) {
            super(itemview);
            videoView = itemview.findViewById(R.id.videoView);
            videoProgressBar = itemview.findViewById(R.id.videoProgressBar);
            textVideoTitle = itemview.findViewById(R.id.textVideoTitle);
            textVideoDescription = itemview.findViewById(R.id.textVideoDescription);
            imPerson = itemview.findViewById(R.id.imPerson);
            imShare = itemview.findViewById(R.id.imShare);
            tvUserEmail = itemview.findViewById(R.id.tvUserEmail);
            ivLike = itemview.findViewById(R.id.ivLike);
            ivDislike = itemview.findViewById(R.id.ivDislike);
            tvLikeCount = itemview.findViewById(R.id.tvLikeCount);
            tvDislikeCount = itemview.findViewById(R.id.tvDislikeCount);

        }
    }

    private void handleLike(String videoId, String userId) {
        DatabaseReference videoRef = FirebaseDatabase.getInstance().getReference("videos").child(videoId);
        videoRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                VideoModel video = mutableData.getValue(VideoModel.class);
                if (video == null) {
                    return Transaction.success(mutableData);
                }

                video.setLikes(video.getLikes() + 1);
                mutableData.setValue(video);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                // Transaction completed
            }
        });
    }

    private void handleDislike(String videoId, String userId) {
        DatabaseReference videoRef = FirebaseDatabase.getInstance().getReference("videos").child(videoId);
        videoRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                VideoModel video = mutableData.getValue(VideoModel.class);
                if (video == null) {
                    return Transaction.success(mutableData);
                }

                video.setDislikes(video.getDislikes() + 1); // Corrected to increment dislikes
                mutableData.setValue(video);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                // Transaction completed
            }
        });
    }
}
