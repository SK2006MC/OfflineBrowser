package com.sk.revisit.webview;

import android.content.Context;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.sk.revisit.MyUtils;
import com.sk.revisit.managers.WebStorageManager;

public class MyWebViewClient extends WebViewClient {

    final WebStorageManager webStorageManager;

    public MyWebViewClient(Context context, MyUtils utils) {
        webStorageManager = new WebStorageManager(context, utils);
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        return webStorageManager.getResponse(request);
    }
}