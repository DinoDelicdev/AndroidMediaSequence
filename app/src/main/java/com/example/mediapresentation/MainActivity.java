package com.example.mediapresentation; // Make sure this package name matches yours

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.VideoView;
import android.widget.Toast; // Import Toast

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements
        MediaDownloader.DownloadCallback,
        ConfigFetcher.ConfigCallback {

    private static final String TAG = "MainActivity";

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

        Log.d(TAG, "Starting ConfigFetcher...");
        ConfigFetcher configFetcher = new ConfigFetcher(this, this);
        configFetcher.fetchConfig();
    }


    @Override
    public void onConfigFetched(List<MediaItem> webMediaQueue) {
        Log.d(TAG, "Config fetched! " + webMediaQueue.size() + " items found.");

        if (webMediaQueue.isEmpty()) {
            Log.e(TAG, "Config was empty. Cannot proceed.");
            onConfigFetchFailed("Loaded config, but it was empty.");
            return;
        }


        Log.d(TAG, "Starting MediaDownloader...");
        MediaDownloader downloader = new MediaDownloader(this, webMediaQueue, this);
        downloader.startDownloads();
    }


    @Override
    public void onConfigFetchFailed(String error) {
        Log.e(TAG, "Failed to get config: " + error);

        progressBar.setVisibility(View.GONE);

        Toast.makeText(this, "FATAL: Could not load media list. " + error, Toast.LENGTH_LONG).show();
    }



    @Override
    public void onDownloadsComplete(List<MediaItem> localMediaQueue) {
        Log.d(TAG, "Media downloads complete!");


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