package com.sk.revisit.managers;

import android.net.Uri;
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

    public WebStorageManager(MyUtils utils) {
        this.utils = utils;
    }

    @Nullable
    public WebResourceResponse getResponse(WebResourceRequest request) {
        MyUtils.requests.incrementAndGet();
        if (!GET_METHOD.equals(request.getMethod())) {
            utils.log(TAG, "Request method is not GET: " + request.getMethod());
            return null;
        }

        Uri uri = request.getUrl();
        String uriStr = uri.toString();
        if (!URLUtil.isNetworkUrl(uriStr)) {
            utils.log(TAG, "Not a network URL: " + uriStr);
            return null;
        }

        String localPath = utils.buildLocalPath(uri);
        if (localPath == null) {
            return null;
        }
        File localFile = new File(localPath);
        MyUtils.DownloadListener listener = createDownloadListener(uri, uriStr, localPath);

        if (localFile.exists()) {
            if (MyUtils.isNetworkAvailable && MyUtils.shouldUpdate && shouldUpdateLocalFile(uri, localPath)) {
                utils.log(TAG, "Updating local file: " + localPath);
                utils.download(uri, listener);
            }
            return loadFromLocal(localFile);
        } else {
            if (MyUtils.isNetworkAvailable) {
                utils.log(TAG, "Downloading missing file: " + localPath);
                utils.download(uri, listener);
            }
            utils.saveReq(uri.getHost() + "," + uriStr);
            return new WebResourceResponse("text/html", UTF_8,
                    new ByteArrayInputStream("No offline file available, please refresh.".getBytes()));
        }
    }

    private boolean shouldUpdateLocalFile(Uri uri, String localPath) {
        long remoteSize = utils.getSizeFromUrl(uri);
        long localSize = utils.getSizeFromLocal(localPath);
        return remoteSize > 0 && localSize > 0 && remoteSize != localSize;
    }

    private MyUtils.DownloadListener createDownloadListener(Uri uri, String uriStr, String localPath) {
        return new MyUtils.DownloadListener() {
            @Override
            public void onSuccess(File file, Headers headers) {
                utils.saveResp(uriStr + "|" + localPath + "|" + file.length() + "|" + headers.toString());
            }

            @Override
            public void onFailure(Exception e) {
                MyUtils.failed.incrementAndGet();
                utils.saveReq(uri.getHost() + "," + uriStr);
            }
        };
    }

    @Nullable
    private WebResourceResponse loadFromLocal(@NonNull File localFile) {
        if (!localFile.exists() || !localFile.isFile()) {
            utils.log(TAG, "Local file does not exist or is not a file: " + localFile.getAbsolutePath());
            return null;
        }
        String mimeType = utils.getMimeType(localFile.getPath());
        try {
            InputStream fis = new FileInputStream(localFile);
            MyUtils.resolved.incrementAndGet();
            return new WebResourceResponse(mimeType, UTF_8, fis);
        } catch (FileNotFoundException e) {
            utils.log(TAG, "Error loading from local file: " + localFile.getAbsolutePath(), e);
            return null;
        }
    }
}