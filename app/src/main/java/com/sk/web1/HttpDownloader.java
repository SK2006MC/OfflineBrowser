package com.sk.web1;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.concurrent.ExecutorService;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class HttpDownloader implements Downloader {
    private final Context context;
    private final ExecutorService executorService;
    private final OkHttpClient client;

    public HttpDownloader(Context context, ExecutorService executorService, OkHttpClient client) {
        this.context = context;
        this.executorService = executorService;
        this.client = client;
    }

    @Override
    public void download(String url, File localFile, DownloadCallback callback) {
        executorService.submit(() -> {
            callback.onStart();
            Request request = new Request.Builder().url(url).build();
            Call call = client.newCall(request);
            try (Response response = call.execute()) {
                if (!response.isSuccessful()) {
                    callback.onFailure("Download failed: " + response.code());
                    return;
                }

                ResponseBody body = response.body();
                if (body == null) {
                    callback.onFailure("Download downloaded failed: Response body is null");
                    return;
                }

                long contentLength = body.contentLength();

                try (InputStream in = body.byteStream();
                     FileOutputStream fileOutputStream = new FileOutputStream(localFile);
                     FileChannel outputChannel = fileOutputStream.getChannel();
                     ReadableByteChannel inputChannel = Channels.newChannel(in)) {

                    ByteBuffer buffer = ByteBuffer.allocateDirect(1024); // Allocate a buffer for reading

                    long totalBytesRead = 0;
                    long bytesRead;
                    while ((bytesRead = inputChannel.read(buffer)) != -1) {
                        buffer.flip(); // Flip the buffer to prepare for writing
                        outputChannel.write(buffer); // Write the data from the buffer to the file
                        buffer.clear(); // Clear the buffer for the next read

                        totalBytesRead += bytesRead;
                        if (contentLength > 0) { // Avoid division by zero
                            int progress = (int) ((totalBytesRead * 100) / contentLength);
                            callback.onProgress(progress, contentLength);
                        }
                    }
                    callback.onSuccess();

                } catch (IOException innerException) {
                    callback.onFailure("Error writing file: " + innerException.getMessage());
                }
            } catch (IOException e) {
                callback.onFailure("Download failed: " + e.getMessage());
            }
        });
    }
}
