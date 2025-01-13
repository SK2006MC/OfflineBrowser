package com.sk.web1;

import java.io.File;

public interface Downloader {
    void download(String url, File localFile, DownloadCallback callback);

    interface DownloadCallback {
        void onStart();
        void onProgress(int progress, long total);
        void onSuccess();
        void onFailure(String error);
    }
}

