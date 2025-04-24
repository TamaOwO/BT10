package com.example.bt10.adapter;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bt10.R;
import com.example.bt10.model.VideoModel;
import com.example.bt10.model.VideoModelThay;

import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.MyViewHolder>{
    private Context context;
    private List<VideoModelThay> videoList;
    private boolean isFav;
    public VideoAdapter(Context context, List<VideoModelThay> videoList) {
        this.context = context;
        this.videoList = videoList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_video_row, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        VideoModelThay video = videoList.get(position);
        holder.textVideoTitle.setText(video.getTitle());
        holder.textVideoDescription.setText(video.getDescription());
        holder.videoView.setVideoURI(Uri.parse(video.getUrl()));

        holder.videoView.setOnPreparedListener(mediaPlayer -> {
            holder.videoProgressBar.setVisibility(View.GONE);
            mediaPlayer.start();
            float videoRatio = mediaPlayer.getVideoWidth() / (float) mediaPlayer.getVideoHeight();
            float screenRatio = holder.videoView.getWidth() / (float) holder.videoView.getHeight();
            float scale = videoRatio / screenRatio;
            if (scale >= 1f) {
                holder.videoView.setScaleX(scale);
            } else {
                holder.videoView.setScaleY(1f/scale);
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
                if(!isFav){
                    holder.imFav.setImageResource(R.drawable.ic_favorite);
                    isFav = true;
                }else{
                    holder.imFav.setImageResource(R.drawable.ic_fill_favorite);
                    isFav = false;
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        private VideoView videoView;
        private ProgressBar videoProgressBar;
        private TextView textVideoTitle;
        private TextView textVideoDescription;
        private ImageView imPerson;
        private ImageView imFav;
        private ImageView imShare;
        private ImageView imMore;
        public MyViewHolder(View itemView) {
            super(itemView);
            videoView = itemView.findViewById(R.id.videoView);
            videoProgressBar = itemView.findViewById(R.id.videoProgressBar);
            textVideoTitle = itemView.findViewById(R.id.textVideoTitle);
            textVideoDescription = itemView.findViewById(R.id.textVideoDescription);
            imPerson = itemView.findViewById(R.id.imPerson);
            imShare = itemView.findViewById(R.id.imShare);
            imMore = itemView.findViewById(R.id.imMore);
            imFav = itemView.findViewById(R.id.ivLike);

        }
    }
}
