package com.sk.web1;

import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;

public interface CacheManager {
    WebResourceResponse getCachedResponse(WebResourceRequest request);
    void clearCache(); // Add a method to clear the cache
}

