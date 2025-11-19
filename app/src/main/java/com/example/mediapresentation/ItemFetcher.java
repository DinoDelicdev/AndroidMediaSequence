package com.example.mediapresentation;
import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONObject;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;


public class ItemFetcher {
    private static final String TAG = "ItemFetcher";


    public interface ItemCallback {
        void onItemReceived(ArticleItem articleItem);
        void onError(String error);
    }

    private final Context context;
    private final ItemCallback callback;
    private final RequestQueue requestQueue;

    private final Gson gson;


    public ItemFetcher(Context context, ItemCallback callback) {
        this.context = context;
        this.callback = callback;
        this.requestQueue = Volley.newRequestQueue(context);
        this.gson = new Gson();
    }


    public void fetch() {
        String url = "https://test-backend-for-media-production.up.railway.app/item";

        Log.d(TAG, "Fetching Item Info...");

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    Log.d(TAG, "Got status: " + response);
                    ArticleItem articleItem = parseArticleItem(response);
                    if (articleItem != null) {
                        Log.d(TAG, "Parsed Item: " + articleItem.toString());
                        callback.onItemReceived(articleItem);
                    } else {
                        callback.onError("Failed to parse item data");
                    }
                    callback.onItemReceived(articleItem);
                },
                error -> {
                    Log.e(TAG, "Status check failed");
                    callback.onError(error.getMessage());
                }
        ){

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() {
                try {
                    JSONObject jsonBody = new JSONObject();
                    jsonBody.put("PJ", 10);
                    jsonBody.put("SifraArtikla", "XYZ-999");
                    String requestBody = jsonBody.toString();
                    return requestBody.getBytes(StandardCharsets.UTF_8);
                } catch (Exception uee) {
                    Log.e(TAG, "Failed to build JSON body");
                    return null;
                }
            }

        };

        requestQueue.add(request);
    }

    private ArticleItem parseArticleItem(String jsonString) {
        try {
            // "F-ing" CLEANER WAY:
            return gson.fromJson(jsonString, ArticleItem.class);
        } catch (Exception e) {
            Log.e(TAG, "Gson parsing failed", e);
            return null; // Return null on error so we know it failed
        }
    }

}
