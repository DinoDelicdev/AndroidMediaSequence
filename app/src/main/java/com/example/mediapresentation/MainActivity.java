package com.example.mediapresentation;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.VideoView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private MediaSequencer mediaSequencer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ImageView imageView = findViewById(R.id.imageView);
        VideoView videoView = findViewById(R.id.videoView);


        List<MediaItem> mediaQueue = new ArrayList<>();
        mediaQueue.add(new MediaItem("image", "https://picsum.photos/id/10/1920/1080", 5));
        mediaQueue.add(new MediaItem("video", "https://www.shutterstock.com/shutterstock/videos/3838457605/preview/stock-footage-global-connection-lines-network-data-transfer-over-earth-futuristic-ai-technology-g-satellite.mp4"));
        mediaQueue.add(new MediaItem("image", "https://picsum.photos/id/10/1920/1080", 2));
        mediaQueue.add(new MediaItem("image", "https://picsum.photos/id/237/200/300", 3));
        mediaQueue.add(new MediaItem("video", "https://www.shutterstock.com/shutterstock/videos/3831072487/preview/stock-footage-caucasian-guy-sleeping-tired-businessman-sleep-nap-at-dark-night-office-lost-health-problem.mp4"));
        mediaQueue.add(new MediaItem("image", "https://picsum.photos/seed/picsum/200/300", 4));
        mediaQueue.add(new MediaItem("video", "https://www.shutterstock.com/shutterstock/videos/3827203677/preview/stock-footage-translucent-human-head-with-glowing-intricate-neon-pink-brain.mp4"));
        mediaQueue.add(new MediaItem("image", "https://picsum.photos/seed/picsum/200/300", 4));
        mediaQueue.add(new MediaItem("video", "https://www.shutterstock.com/shutterstock/videos/3827203677/preview/stock-footage-translucent-human-head-with-glowing-intricate-neon-pink-brain.mp4"));
        mediaQueue.add(new MediaItem("image", "https://picsum.photos/id/10/1920/1080", 2));


        mediaSequencer = new MediaSequencer(this, imageView, videoView, mediaQueue);

    }



    @Override
    protected void onStart() {
        super.onStart();
        if (mediaSequencer != null) {
            mediaSequencer.start();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mediaSequencer != null) {
            mediaSequencer.stop();
        }
    }
}