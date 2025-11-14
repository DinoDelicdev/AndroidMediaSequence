package com.example.mediapresentation;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.VideoView;
import com.bumptech.glide.Glide;
import java.util.List;

public class MediaSequencer {

    private final List<MediaItem> mediaQueue;
    private final ImageView imageView;
    private final VideoView videoView;
    private final Context context;
    private final Handler imageTimerHandler;

    private int currentItemIndex = 0;

    public MediaSequencer(Context context, ImageView imageView, VideoView videoView, List<MediaItem> mediaQueue) {
        this.context = context;
        this.imageView = imageView;
        this.videoView = videoView;
        this.mediaQueue = mediaQueue;
        this.imageTimerHandler = new Handler(Looper.getMainLooper());

        setupVideoListeners();
    }

    private void setupVideoListeners() {
        // Ovo runna kad video prestane
        videoView.setOnCompletionListener(mediaPlayer -> playNextItem());

        // Ovo Ako Video ima neki error
        videoView.setOnErrorListener((mediaPlayer, what, extra) -> {
            // Samo preskoci video i pusti sljedeci item
            playNextItem();
            return true;
        });

        // Ovo Ide kad je vido buffered i spreman da se pusti
        videoView.setOnPreparedListener(mediaPlayer -> {
            // Video spreman. Startaj ga
            mediaPlayer.start();
        });
    }

    private void playItem(int index) {
        // Zaustavi sve potencijalne timere za slike
        imageTimerHandler.removeCallbacksAndMessages(null);

        // Za svaki slucaj, da se uvjerimo da imamo iteme za pustat
        if (mediaQueue == null || mediaQueue.isEmpty()) {
            return;
        }

        MediaItem item = mediaQueue.get(index);

        if (item.type.equals("image")) {
            //Ako je image
            //Zaustavi potencijalni video
            videoView.stopPlayback();
            // Sakrij Video View
            videoView.setVisibility(View.GONE);
            // Prikazi Image View
            imageView.setVisibility(View.VISIBLE);

            Glide.with(context)
                    .load(item.url)
                    .into(imageView);

            // Starta timer
            //imageTimerHandler.postDelayed(this::playNextItem, item.durationInSeconds * 1000L);

            imageTimerHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    playNextItem();
                }
            }, item.durationInSeconds * 1000L);

        } else if (item.type.equals("video")) {
            // Ako Je video
            imageView.setVisibility(View.GONE);
            videoView.setVisibility(View.VISIBLE);

            Uri videoUri = Uri.parse(item.url);
            videoView.setVideoURI(videoUri);
            // .start() ne treba jer smo gore stavili mediaPlayer.start(); u setOnPreparedListener
        }
    }

    private void playNextItem() {
        currentItemIndex++;
        if (currentItemIndex >= mediaQueue.size()) {
            currentItemIndex = 0; // Loop back na pocetak
        }
        playItem(currentItemIndex);
    }

    public void start() {
        // Startaj sekvencu
        playItem(currentItemIndex);
    }

    public void stop() {
        // mediaPlayer.start();
        videoView.stopPlayback();
        imageTimerHandler.removeCallbacksAndMessages(null);
    }
}