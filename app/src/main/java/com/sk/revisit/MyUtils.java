package com.sk.revisit;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import com.sk.revisit.managers.SQLiteDBM;

public class MyUtils {
    private static final String TAG = "MyUtils";
    private static final int MAX_THREADS = 5;
    private static final String INDEX_HTML = "index.html";
    private static final long INVALID_SIZE = -1;

    final String rootPath;
    private final ExecutorService executorService;
    private final OkHttpClient client;
	Context context;
	public final SQLiteDBM dbm;

    public MyUtils(Context context,String rootPath) {
        this.rootPath = rootPath;
		this.context = context;
        this.executorService = Executors.newFixedThreadPool(MAX_THREADS);
        this.client = new OkHttpClient();
		this.dbm=new SQLiteDBM(context,rootPath+"/revisit.db");
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
     * Gets the size of a resource from a URL using OkHttp.
     *
     * @param uri The URI of the resource.
     * @return The size of the resource in bytes, or -1 if an error occurs.
     */
    public long getSizeFromUrl(@NonNull Uri uri) {
        Request request = new Request.Builder()
                .url(uri.toString())
                .head() // Use HEAD request to get only headers
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                ResponseBody body = response.body();
                if (body != null) {
                    return body.contentLength();
                }
            } else {
                Log.e(TAG, "Error getting size from URL: " + uri + ". Response code: " + response.code());
            }
        } catch (IOException e) {
            Log.e(TAG, "Error getting size from URL: " + uri, e);
        }
        return INVALID_SIZE;
    }

    /**
     * Downloads a resource from a URI to a local file using OkHttp.
     *
     * @param uri      The URI of the resource to download.
     * @param listener The listener to receive download events.
     */
    public void download(@NonNull final Uri uri, @NonNull final DownloadListener listener) {
        executorService.submit(() -> {
            String localFilePath = buildLocalPath(uri);
            if (localFilePath == null) {
                listener.onFailure(new IOException("Failed to build local path for URI: " + uri));
                return;
            }
            File localFile = new File(localFilePath);
            File parentFile = localFile.getParentFile();
            if (parentFile != null && !parentFile.exists()) {
                boolean mkdirs = parentFile.mkdirs();
                if (!mkdirs) {
                    listener.onFailure(new IOException("Failed to create directory: " + parentFile.getAbsolutePath()));
                    return;
                }
                Log.d(TAG, "Created directory: " + parentFile.getAbsolutePath());
            }

            Request request = new Request.Builder()
                    .url(uri.toString())
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e(TAG, "Download failed for URI: " + uri, e);
                    listener.onFailure(e);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    if (response.isSuccessful()) {
                        ResponseBody body = response.body();
                        if (body == null) {
                            listener.onFailure(new IOException("Empty response body for URI: " + uri));
                            return;
                        }
                        try (InputStream in = body.byteStream();
                             FileOutputStream out = new FileOutputStream(localFile)) {
                            byte[] buffer = new byte[8192];
                            int bytesRead;
                            while ((bytesRead = in.read(buffer)) != -1) {
                                out.write(buffer, 0, bytesRead);
                            }
                            Log.d(TAG, "Downloaded: " + uri + " to " + localFilePath);
                            listener.onSuccess(localFile);
                        } catch (IOException e) {
                            Log.e(TAG, "Error writing to file: " + localFilePath, e);
                            listener.onFailure(e);
                        }
                    } else {
                        Log.e(TAG, "Download failed. Response code: " + response.code() + " for URI: " + uri);
                        listener.onFailure(new IOException("Download failed. Response code: " + response.code()));
                    }
                }
            });
        });
    }

    /**
     * Shuts down the executor service.
     */
    public void shutdown() {
        executorService.shutdown();
    }

    /**
     * Interface for listening to download events.
     */
    public interface DownloadListener {
        /**
         * Called when the download is successful.
         *
         * @param file The downloaded file.
         */
        void onSuccess(File file);

        /**
         * Called when the download fails.
         *
         * @param e The exception that caused the failure.
         */
        void onFailure(Exception e);
    }
}