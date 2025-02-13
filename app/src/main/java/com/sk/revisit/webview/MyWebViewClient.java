package com.sk.revisit.webview;

import android.webkit.WebView;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebViewClient;
import com.sk.revisit.managers.WebStorageManager;

public class MyWebViewClient extends WebViewClient {
	
	WebStorageManager webStorageManager;
	
	public MyWebViewClient(){
		webStorageManager = new WebStorageManager();
	}
	
	@Override
	public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
		return webStorageManager.getStoredResponse(request);
	}
}