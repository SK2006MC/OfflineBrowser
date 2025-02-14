package com.sk.revisit;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyUtils {
    private static final String TAG = "MyUtils";
    private static final int CONNECT_TIMEOUT = 5000; // 5 seconds
    private static final int READ_TIMEOUT = 5000; // 5 seconds
    private static final int MAX_THREADS = 5;
    private static final String INDEX_HTML = "index.html";
    private static final long INVALID_SIZE = -1;

    private final String rootPath;
    private final ExecutorService executorService;

    public MyUtils(String rootPath) {
        this.rootPath = rootPath;
        this.executorService = Executors.newFixedThreadPool(MAX_THREADS);
    }

    /**
     * Builds a local file path based on the given URI.
     *
     * @param uri The URI to build the local path from.
     * @return The local file path as a String.
     */
    public String buildLocalPath(@NonNull Uri uri) {
        String lastPathSegment = uri.getLastPathSegment();
        String host = uri.getHost();
        String encodedPath = uri.getEncodedPath();

        // Validate inputs
        if (TextUtils.isEmpty(host) || TextUtils.isEmpty(encodedPath)) {
            Log.e(TAG, "Invalid URI: Host or path is empty.");
            return null; // Or throw an exception if appropriate
        }

        String localPath = rootPath + File.separator + host + encodedPath;

        if (lastPathSegment == null) {
            return localPath + File.separator + INDEX_HTML;
        }

        if (lastPathSegment.contains(".")) {
            return localPath;
        } else {
            return uri.toString().endsWith("/") ? localPath + File.separator + INDEX_HTML : localPath + File.separator + INDEX_HTML;
        }
    }

    /**
     * Gets the MIME type of a file.
     *
     * @param filePath The path to the file.
     * @return The MIME type as a String, or null if an error occurs.
     */
    @Nullable
    public String getMimeType(@NonNull String filePath) {
        try {
            return Files.probeContentType(Paths.get(filePath));
        } catch (IOException e) {
            Log.e(TAG, "Error getting MIME type for: " + filePath, e);
            return null;
        }
    }

    /**
     * Gets the size of a local file.
     *
     * @param filePath The path to the local file.
     * @return The size of the file in bytes, or -1 if the file does not exist.
     */
    public long getSizeFromLocal(@NonNull String filePath) {
        File file = new File(filePath);
        return file.exists() ? file.length() : INVALID_SIZE;
    }

    /**
     * Gets the size of a resource from a URL.
     *
     * @param uri The URI of the resource.
     * @return The size of the resource in bytes, or -1 if an error occurs.
     */
    public long getSizeFromUrl(@NonNull Uri uri) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(uri.toString()).openConnection();
            connection.setRequestMethod("HEAD");
            return connection.getContentLength();
        } catch (IOException e) {
            Log.e(TAG, "Error getting size from URL: " + uri, e);
            return INVALID_SIZE;
        }
    }

    /**
     * Downloads a resource from a URI to a local file.
     *
     * @param uri The URI of the resource to download.
     */
    public void download(@NonNull final Uri uri) {
        executorService.submit(() -> {
            try {
                String localFilePath = buildLocalPath(uri);
                if (localFilePath == null) {
                    Log.e(TAG, "Failed to build local path for URI: " + uri);
                    return;
                }
                File localFile = new File(localFilePath);
                URL url = new URL(uri.toString());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(CONNECT_TIMEOUT);
                connection.setReadTimeout(READ_TIMEOUT);
                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    File parentFile = localFile.getParentFile();
                    if (parentFile != null && !parentFile.exists()) {
                        boolean mkdirs = parentFile.mkdirs();
                        if (mkdirs) Log.d(TAG, "Created directory: " + parentFile.getAbsolutePath());
                        else Log.e(TAG, "Failed to create directory: " + parentFile.getAbsolutePath());
                    }
                    try (InputStream in = new BufferedInputStream(connection.getInputStream());
                         FileOutputStream fileOutputStream = new FileOutputStream(localFile);
                         FileChannel outputChannel = fileOutputStream.getChannel();
                         ReadableByteChannel inputChannel = Channels.newChannel(in)) {
                        outputChannel.transferFrom(inputChannel, 0, Long.MAX_VALUE);
                        Log.d(TAG, "Downloaded: " + uri + " to " + localFilePath);
                    }
                } else {
                    Log.e(TAG, "Download failed. Response code: " + responseCode + " for URI: " + uri);
                }
            } catch (MalformedURLException e) {
                Log.e(TAG, "Invalid URL: " + uri, e);
            } catch (IOException e) {
                Log.e(TAG, "Error downloading from URI: " + uri, e);
            }
        });
    }

    /**
     * Shuts down the executor service.
     */
    public void shutdown() {
        executorService.shutdown();
    }
}