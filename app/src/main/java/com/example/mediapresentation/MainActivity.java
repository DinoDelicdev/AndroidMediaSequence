package com.example.mediapresentation;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.VideoView;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements MediaDownloader.DownloadCallback {

    private MediaSequencer mediaSequencer;
    private ProgressBar progressBar;
    private ImageView imageView;
    private VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        imageView = findViewById(R.id.imageView);
        videoView = findViewById(R.id.videoView);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        List<MediaItem> webMediaQueue = new ArrayList<>();
        webMediaQueue.add(new MediaItem("image", "https://i.pinimg.com/736x/ac/27/1d/ac271de883faa03617b212beeda73db3.jpg", 5));
        //webMediaQueue.add(new MediaItem("video", "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"));
        webMediaQueue.add(new MediaItem("image", "https://picsum.photos/id/20/1920/1080", 3));
        //webMediaQueue.add(new MediaItem("image", "https://picsum.photos/id/30/1920/1080", 4));
        webMediaQueue.add(new MediaItem("video", "https://www.shutterstock.com/shutterstock/videos/3831653471/preview/stock-footage-australia-sydney-warm-sunlight-reflect-on-water-city-skyline-modern-buildings-in-background.mp4"));
        webMediaQueue.add(new MediaItem("video", "https://www.shutterstock.com/shutterstock/videos/3771633729/preview/stock-footage-business-people-tablet-or-manager-shaking-hands-for-corporate-funding-agreement-or-b-b-deal.webm"));

        MediaDownloader downloader = new MediaDownloader(this, webMediaQueue, this);
        downloader.startDownloads();
    }


    @Override
    public void onDownloadsComplete(List<MediaItem> localMediaQueue) {
        progressBar.setVisibility(View.GONE);

        mediaSequencer = new MediaSequencer(this, imageView, videoView, localMediaQueue);
        if (!isFinishing() && !isDestroyed()) {
            mediaSequencer.start();
        }
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