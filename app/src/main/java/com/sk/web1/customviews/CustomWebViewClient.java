package com.sk.web1;

import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.util.ArrayList;

public class CustomWebViewClient extends WebViewClient {
  private final CacheManager cacheManager;
  ArrayList<String> sugg;

  public CustomWebViewClient(CacheManager cacheManager, ArrayList<String> sugg) {
    this.cacheManager = cacheManager;
    this.sugg = sugg;
  }

  @Override
  public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
    return cacheManager.getCachedResponse(request);
  }

  @Override
  public void onPageFinished(WebView arg0, String arg1) {
    super.onPageFinished(arg0, arg1);
    sugg.add(arg1);
  }
}
