package com.example.mediapresentation;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ConfigFetcher {

    private static final String TAG = "ConfigFetcher";
    private static final String CONFIG_FILE_NAME = "media_config.json";


    public interface ConfigCallback {
        void onConfigFetched(List<MediaItem> mediaList);
        void onConfigFetchFailed(String error);
    }

    private final Context context;
    private final ConfigCallback callback;
    private final RequestQueue requestQueue;
    private final Gson gson;

    public ConfigFetcher(Context context, ConfigCallback callback) {
        this.context = context;
        this.callback = callback;
        this.requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        this.gson = new Gson();
    }

    public void fetchConfig() {

        String url = "https://test-backend-for-media-production.up.railway.app/";




        Log.d(TAG, "Fetching config from: " + url);


        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,

                response -> {
                    Log.d(TAG, "Config fetched successfully!");
                    saveConfigToLocal(response);
                    List<MediaItem> mediaList = parseConfig(response);
                    callback.onConfigFetched(mediaList);
                },

                error -> {
                    Log.e(TAG, "Volley request failed: " + error.getMessage());
                    Log.d(TAG, "Attempting to load config from local storage...");
                    String localJson = loadConfigFromLocal();
                    if (localJson != null) {
                        Log.d(TAG, "Successfully loaded config from local storage.");
                        List<MediaItem> mediaList = parseConfig(localJson);
                        callback.onConfigFetched(mediaList);
                    } else {
                        Log.e(TAG, "No local config found. Failed to load.");
                        callback.onConfigFetchFailed("Network failed and no local config available.");
                    }
                }
        );


        requestQueue.add(stringRequest);
    }


    private List<MediaItem> parseConfig(String jsonString) {
        try {
            Type listType = new TypeToken<ArrayList<MediaItem>>(){}.getType();
            return gson.fromJson(jsonString, listType);
        } catch (Exception e) {
            Log.e(TAG, "Gson parsing failed", e);
            return new ArrayList<>();
        }
    }

    private void saveConfigToLocal(String jsonString) {
        try {
            File file = new File(context.getFilesDir(), CONFIG_FILE_NAME);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(jsonString.getBytes());
            fos.close();
            Log.d(TAG, "Config saved to local storage: " + file.getAbsolutePath());
        } catch (Exception e) {
            Log.e(TAG, "Failed to save config to local storage", e);
        }
    }

    private String loadConfigFromLocal() {
        try {
            File file = new File(context.getFilesDir(), CONFIG_FILE_NAME);
            if (!file.exists()) {
                return null;
            }
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            fis.close();
            return sb.toString();
        } catch (Exception e) {
            Log.e(TAG, "Failed to load config from local storage", e);
            return null;
        }
    }
}