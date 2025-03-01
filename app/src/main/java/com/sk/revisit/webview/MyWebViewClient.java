package com.sk.revisit.webview;

import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.sk.revisit.MyUtils;
import com.sk.revisit.managers.WebStorageManager;

public class MyWebViewClient extends WebViewClient {
    private final WebStorageManager webStorageManager;

    public MyWebViewClient(WebStorageManager webStorageManager) {
        this.webStorageManager = webStorageManager;
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        return webStorageManager.getResponse(request);
    }
}