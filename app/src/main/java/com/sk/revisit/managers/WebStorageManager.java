package com.sk.revisit.managers;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.webkit.URLUtil;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;

import androidx.annotation.NonNull;

import com.sk.revisit.MyUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class WebStorageManager {

    final MySettingsManager settingsManager;

    final MyUtils utils;

    final String TAG = "WebStorageManager";

    public WebStorageManager(Context context,MyUtils utils) {
        this.settingsManager = new MySettingsManager(context);
        this.utils = utils;
    }


    public WebResourceResponse getResponse(WebResourceRequest request) {
        Uri uri = request.getUrl();
        String uriStr=uri.toString();
        if (!URLUtil.isNetworkUrl(uriStr)) {
            return null;
        }

        String localPath = utils.buildLocalPath(uri);
        File localFile=new File(localPath);

        if ("GET".equals(request.getMethod())) {
            if (localFile.exists()) {
                if (true) {
                    long remoteSize = utils.getSizeFromUrl(uri);
                    long localSize = utils.getSizeFromLocal(localPath);
                    if (remoteSize != -1 && localSize != -1 && remoteSize != localSize) {
                        utils.download(uri);
                    }
                }
            } else {
                utils.download(uri);
            }
            return loadFromLocal(localFile);
        }
        return null;
    }

    private WebResourceResponse loadFromLocal(@NonNull File localFile) {
        String mimeType = utils.getMimeType(localFile.getPath());
        try {
            InputStream fis = null;
            fis = Files.newInputStream(localFile.toPath());
            return new WebResourceResponse(mimeType, "UTF-8", fis);
        } catch (Exception e) {
            Log.d("WEbView", e.toString());
            return null;
        }
    }
}