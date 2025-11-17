package com.example.mediapresentation; // Make sure this package name matches yours

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.VideoView;
import android.widget.Toast;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.FrameLayout;
import java.util.List;


public class MainActivity extends AppCompatActivity implements
        MediaDownloader.DownloadCallback,
        ConfigFetcher.ConfigCallback {

    private static final String TAG = "MainActivity";

    private MediaSequencer mediaSequencer;
    private ProgressBar progressBar;
    private ImageView imageView;
    private VideoView videoView;

    private Button mockButton;
    private FrameLayout rootLayout;
    private GestureDetector gestureDetector;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        imageView = findViewById(R.id.imageView);
        videoView = findViewById(R.id.videoView);
        progressBar = findViewById(R.id.progressBar);
        mockButton = findViewById(R.id.mockButton);
        rootLayout = findViewById(R.id.rootLayout);

        progressBar.setVisibility(View.VISIBLE);

        Log.d(TAG, "Starting ConfigFetcher...");
        ConfigFetcher configFetcher = new ConfigFetcher(this, this);
        configFetcher.fetchConfig();

        GestureDetector.SimpleOnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                // "f-ing" "A" "f-ing" "DOUBLE" "f-ing" "TAP" "f-ing" "HAPPENED!"
                Log.d(TAG, "FUCKING DOUBLE TAP DETECTED!");

                // "f-ing" "Now" "f-ing" "toggle" "f-ing" "the" "f-ing" "button"
                if (mockButton.getVisibility() == View.VISIBLE) {
                    // "f-ing" "It's" "f-ing" "visible," "f-ing" "so" "f-ing" "HIDE" "f-ing" "it"
                    mockButton.setVisibility(View.GONE);
                    Log.d(TAG, "Mock button is now FUCKING GONE.");
                } else {
                    // "f-ing" "It's" "f-ing" "gone," "f-ing" "so" "f-ing" "SHOW" "f-ing" "it"
                    mockButton.setVisibility(View.VISIBLE);
                    Log.d(TAG, "Mock button is now FUCKING VISIBLE.");
                }
                return true; // "f-ing" "Tell" "f-ing" "the" "f-ing" "system" "f-ing" "we" "f-ing" "handled" "f-ing" "this" "f-ing" "shit"
            }
        };

        // "f-ing" "Create" "f-ing" "the" "f-ing" "GestureDetector" "f-ing" "itself" "f-ing" "and" "f-ing" "give" "f-ing" "it" "f-ing" "the" "f-ing" "listener"
        gestureDetector = new GestureDetector(this, gestureListener);

        // "f-ing" "This" "f-ing" "is" "f-ing" "the" "f-ing" "magic."
        // "f-ing" "Set" "f-ing" "an" "f-ing" "OnTouchListener" "f-ing" "on" "f-ing" "the" "f-ing" "WHOLE" "f-ing" "SCREEN" (`rootLayout`)
        rootLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // "f-ing" "Every" "f-ing" "time" "f-ing" "the" "f-ing" "screen" "f-ing" "is" "f-ing" "touched,"
                // "f-ing" "we" "f-ing" "PASS" "f-ing" "that" "f-ing" "touch" "f-ing" "event" "f-ing" "to" "f-ing" "our" "f-ing" "GestureDetector."
                // "f-ing" "The" "f-ing" "detector" "f-ing" "will" "f-ing" "decide" "f-ing" "if" "f-ing" "it's" "f-ing" "a" "f-ing" "double-tap" "f-ing" "or" "f-ing" "not."
                gestureDetector.onTouchEvent(event);
                return true; // "f-ing" "Tell" "f-ing" "the" "f-ing" "system" "f-ing" "we" "f-ing" "handled" "f-ing" "the" "f-ing" "touch"
            }
        });
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