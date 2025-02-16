package com.sk.revisit.managers;

import android.net.Uri;
import android.util.Log;
import android.webkit.URLUtil;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sk.revisit.MyUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import okhttp3.Headers;

public class WebStorageManager {

    private static final String TAG = "WebStorageManager";
    private static final String GET_METHOD = "GET";
    private static final String UTF_8 = "UTF-8";
    private final MyUtils utils;
    final SQLiteDBM dbm;

    public WebStorageManager(MyUtils utils) {
        this.utils = utils;
        this.dbm = utils.dbm;
    }

    /**
     * Intercepts and handles web resource requests.
     *
     * @param request The web resource request.
     * @return A WebResourceResponse if the request can be handled locally, null otherwise.
     */
    @Nullable
    public WebResourceResponse getResponse(WebResourceRequest request) {

        if (!GET_METHOD.equals(request.getMethod())) {
            Log.d(TAG, "Request method is not GET, ignoring: " + request.getMethod());
            return null;
        }

        Uri uri = request.getUrl();
        String uriStr = uri.toString();

        if (!URLUtil.isNetworkUrl(uriStr)) {
            Log.d(TAG, "Not a network URL, ignoring: " + uriStr);
            return null;
        }

        String localPath = utils.buildLocalPath(uri);
        File localFile = new File(localPath);

        if (localFile.exists()) {
            if (MyUtils.isNetworkAvailable) {
                if (shouldUpdateLocalFile(uri, localPath)) {
                    Log.d(TAG, "Updating local file: " + localPath);
                    utils.download(uri, new MyUtils.DownloadListener() {

                        @Override
                        public void onSuccess(File file, Headers headers) {
                           // dbm.insertIntoUrlsIfNotExists(uri, localPath, file.length(), headers);
                        }

                        @Override
                        public void onFailure(Exception e) {
                            //dbm.insertIntoQueIfNotExists(uri);
                        }
                    });
                }
            }
            Log.d(TAG, "Loading from local file: " + localPath);
            return loadFromLocal(localFile);
        } else {
            if (MyUtils.isNetworkAvailable) {
                Log.d(TAG, "Local file does not exist, downloading: " + localPath);
                utils.download(uri, new MyUtils.DownloadListener() {

                    @Override
                    public void onSuccess(File file, Headers headers) {
                        //dbm.insertIntoUrlsIfNotExists(uri, localPath, file.length(), headers);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        //dbm.insertIntoQueIfNotExists(uri);
                    }
                });
            }
            //dbm.insertIntoQueIfNotExists(uri);
            return new WebResourceResponse("text/html", UTF_8, new ByteArrayInputStream("err refresh".getBytes()));
        }
    }

    /**
     * Checks if the local file should be updated based on the remote file size.
     *
     * @param uri       The URI of the remote resource.
     * @param localPath The local path of the file.
     * @return True if the local file should be updated, false otherwise.
     */
    private boolean shouldUpdateLocalFile(Uri uri, String localPath) {
        long remoteSize = utils.getSizeFromUrl(uri);
        long localSize = utils.getSizeFromLocal(localPath);
        return remoteSize != -1 && localSize != -1 && remoteSize != localSize;
    }

    /**
     * Loads a resource from a local file.
     *
     * @param localFile The local file to load from.
     * @return A WebResourceResponse containing the local file's data, or null if an error occurs.
     */
    @Nullable
    private WebResourceResponse loadFromLocal(@NonNull File localFile) {
        if (!localFile.exists() || !localFile.isFile()) {
            Log.e(TAG, "Local file does not exist or is not a file: " + localFile.getAbsolutePath());
            return null;
        }
        String mimeType = utils.getMimeType(localFile.getPath());
        try {
            InputStream fis = new FileInputStream(localFile);
            return new WebResourceResponse(mimeType, UTF_8, fis);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Error loading from local file: " + localFile.getAbsolutePath(), e);
            return null;
        }
    }
}