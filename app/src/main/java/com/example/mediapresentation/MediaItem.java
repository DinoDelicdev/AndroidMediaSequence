package com.example.mediapresentation;

public class MediaItem {
    String type;
    public String url;
    int durationInSeconds;

    public MediaItem() { }

    // Konstruktor za slike
    public MediaItem(String type, String url, int durationInSeconds) {
        this.type = type;
        this.url = url;
        this.durationInSeconds = durationInSeconds;
    }

    // Konstruktor za videe
    public MediaItem(String type, String url) {
        this.type = type;
        this.url = url;
        //this.durationInSeconds = 0;
    }
}
