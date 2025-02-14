package com.sk.revisit;

import android.net.Uri;
import android.os.Build;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyUtils {
    final String rootPath;
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    public MyUtils(String rootPath) {
        this.rootPath = rootPath;
    }

    public String buildLocalPath(Uri uri) {
        String last = uri.getLastPathSegment();
        String localPathT = rootPath + '/' + uri.getHost() + uri.getEncodedPath();

        if (last == null) {
            return localPathT + "index.html";
        }

        if (last.contains(".")) {
            return localPathT;
        } else {
            return uri.toString().endsWith("/") ? localPathT + "index.html" : localPathT + "/index.html";
        }
    }

    public String getMimeType(String p) {
        try {
            return Files.probeContentType(Paths.get(p));
        } catch (Exception e) {
            Log.d("WEbView", e.toString());
            return null;
        }
    }

    public long getSizeFromLocal(String path) {
        File file = new File(path);
        return file.exists() ? file.length() : -1;
    }
    public long getSizeFromUrl(Uri uri) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(uri.toString()).openConnection();
            connection.setRequestMethod("HEAD");
            return connection.getContentLength();
        } catch (Exception e) {
            Log.d("WEbView", e.toString());
            return -1;
        }
    }
    public void download(final Uri uri) {
        executorService.submit(() -> {
            try {
                File localFile = new File(buildLocalPath(uri));
                URL url = new URL(uri.toString());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    File parentFile = localFile.getParentFile();
                    if (parentFile != null && !parentFile.exists()) {
                        boolean mkdirs = parentFile.mkdirs();
                        if (mkdirs) Log.d("Web view", "mkdir is true");
                    }
                    try (InputStream in = new BufferedInputStream(connection.getInputStream());
                         FileOutputStream fileOutputStream = new FileOutputStream(localFile);
                         FileChannel outputChannel = fileOutputStream.getChannel();
                         ReadableByteChannel inputChannel = Channels.newChannel(in)) {
                        outputChannel.transferFrom(inputChannel, 0, Long.MAX_VALUE);
                    }
                }
            } catch (Exception e) {
                Log.d("WEbView", e.toString());
            }
        });
    }
}
