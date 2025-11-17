package com.example.mediapresentation;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.MimeTypeMap;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MediaDownloader {

    private static final String TAG = "MediaDownloader";

    private static final int MAX_PARALLEL_DOWNLOADS = 4;
    private final ExecutorService downloadExecutor;


    private final ExecutorService managerExecutor;
    private final Handler mainThreadHandler;

    public interface DownloadCallback {
        void onDownloadsComplete(List<MediaItem> localMediaQueue);
    }

    private final Context context;
    private final List<MediaItem> mediaQueue;
    private final DownloadCallback callback;

    public MediaDownloader(Context context, List<MediaItem> mediaQueue, DownloadCallback callback) {
        this.context = context;
        this.mediaQueue = mediaQueue;
        this.callback = callback;
        this.mainThreadHandler = new Handler(Looper.getMainLooper());
        this.downloadExecutor = Executors.newFixedThreadPool(MAX_PARALLEL_DOWNLOADS);
        this.managerExecutor = Executors.newSingleThreadExecutor();

    }

    public void startDownloads() {
        Log.d(TAG, "--- Download process STARTING (Parallel) ---");


        managerExecutor.execute(() -> {
            Set<String> requiredFileNames = new HashSet<>();
            int filesToDownloadCount = 0;
            for (MediaItem item : mediaQueue) {
                if (item.url.startsWith("http")) {
                    File localFile = getLocalFile(item.url);
                    if (localFile == null) continue;
                    requiredFileNames.add(localFile.getName());

                    if (!localFile.exists()) {
                        filesToDownloadCount++;
                    }
                }
            }
            Log.d(TAG, filesToDownloadCount + " new files to download.");


            final CountDownLatch downloadLatch = new CountDownLatch(filesToDownloadCount);


            for (MediaItem item : mediaQueue) {
                if (item.url.startsWith("http")) {
                    File localFile = getLocalFile(item.url);
                    if (localFile == null) continue;

                    if (localFile.exists()) {
                        Log.d(TAG, "File exists, skipping: " + localFile.getName());
                        item.url = localFile.getAbsolutePath();
                    } else {

                        Log.d(TAG, "Submitting download job for: " + localFile.getName());


                        downloadExecutor.execute(() -> {
                            Log.d(TAG, "Worker thread STARTING download: " + localFile.getName());

                            downloadFile(item.url, localFile);

                            if (localFile.exists() && localFile.length() > 0) {
                                item.url = localFile.getAbsolutePath();
                            } else {
                                Log.e(TAG, "File is missing or empty, will not play: " + localFile.getName());
                            }
                            Log.d(TAG, "Worker thread FINISHED download: " + localFile.getName());
                            downloadLatch.countDown();
                        });
                    }
                }
            }


            try {

                Log.d(TAG, "Manager thread waiting for all " + filesToDownloadCount + " downloads to complete...");
                downloadLatch.await();
            } catch (InterruptedException e) {
                Log.e(TAG, "Download latch was interrupted", e);

                return;
            }

            Log.d(TAG, "All downloads complete. Proceeding to cleanup...");

            cleanupOldFiles(requiredFileNames);

            Log.d(TAG, "--- Download process COMPLETE ---");
            mainThreadHandler.post(() -> callback.onDownloadsComplete(mediaQueue));
        });
    }

    private void downloadFile(String downloadUrl, File targetFile) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(downloadUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36");
            connection.setInstanceFollowRedirects(true);
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(15000);
            connection.connect();

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                Log.e(TAG, "Server returned HTTP " + responseCode + " for URL: " + downloadUrl);
                return;
            }

            InputStream input = connection.getInputStream();
            FileOutputStream output = new FileOutputStream(targetFile);

            byte[] data = new byte[4096];
            int count;
            while ((count = input.read(data)) != -1) {
                output.write(data, 0, count);
            }

            output.flush();
            output.close();
            input.close();

            Log.d(TAG, "SUCCESS downloading: " + targetFile.getName());

        } catch (Exception e) {
            Log.e(TAG, "ERROR downloading: " + downloadUrl, e);
            if (targetFile.exists()) {
                if (!targetFile.delete()) {
                    Log.e(TAG, "Failed to delete partial download file: " + targetFile.getName());
                }
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private void cleanupOldFiles(Set<String> requiredFileNames) {
        Log.d(TAG, "--- Starting cleanup... ---");
        File mediaDir = new File(context.getFilesDir(), "media");

        if (!mediaDir.exists() || !mediaDir.isDirectory()) {
            Log.d(TAG, "Media directory not found, skipping cleanup.");
            return;
        }

        File[] existingFiles = mediaDir.listFiles();
        if (existingFiles == null) {
            Log.d(TAG, "No files found in media directory, skipping cleanup.");
            return;
        }

        for (File file : existingFiles) {
            if (file == null) {
                continue;
            }

            String fileName = file.getName();
            if (!requiredFileNames.contains(fileName)) {
                Log.d(TAG, "Deleting unused file: " + fileName);

                if (file.delete()) {
                    Log.d(TAG, "Successfully deleted: " + fileName);
                } else {
                    Log.e(TAG, "Failed to delete: " + fileName);
                }
            }
        }
        Log.d(TAG, "--- Cleanup complete. ---");
    }


    private File getLocalFile(String httpUrl) {
        File filesDir = context.getFilesDir();
        File mediaDir = new File(filesDir, "media");

        if (!mediaDir.exists()) {
            if (!mediaDir.mkdirs()) {
                Log.e(TAG, "CRITICAL: Failed to create media directory. Cannot save files.");
                return null;
            }
        }

        String fileName = getHashedFileName(httpUrl);

        if (fileName == null) {
            fileName = httpUrl.replaceAll("[^a-zA-Z0-9.-]", "_");

            if (fileName.length() > 100) {
                fileName = fileName.substring(fileName.length() - 100);
            }
        }


        return new File(mediaDir, fileName);
    }

    private String getHashedFileName(String httpUrl) {
        String extension = getFileExtension(httpUrl);
        String hash = md5(httpUrl);
        if (hash == null) {
            return null;
        }
        return hash + extension;
    }

    private String getFileExtension(String url) {
        String urlWithoutQuery = url;
        int queryIndex = url.lastIndexOf('?');
        if (queryIndex > 0) {
            urlWithoutQuery = url.substring(0, queryIndex);
        }

        String extension = MimeTypeMap.getFileExtensionFromUrl(urlWithoutQuery);
        if (extension != null && !extension.isEmpty()) {
            return "." + extension;
        }


        int lastDot = urlWithoutQuery.lastIndexOf('.');
        if (lastDot > 0 && urlWithoutQuery.length() - lastDot <= 5) {
            return urlWithoutQuery.substring(lastDot);
        }

        return ".media";
    }

    private String md5(String s) {
        try {
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte[] messageDigest = digest.digest();
            BigInteger no = new BigInteger(1, messageDigest);
            String hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "MD5 algorithm not found", e);
            return null;
        }
    }
}