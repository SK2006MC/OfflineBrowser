```java
package com.sk.revisit.webview;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.sk.revisit.jsact.JSBridge;

public class MyWebView extends WebView {

    private JSBridge jsBridge;

    public MyWebView(Context context) {
        super(context);
        init(context);
    }

    public MyWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MyWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        WebSettings webSettings = getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);

        // Enable remote debugging
        WebView.setWebContentsDebuggingEnabled(true);
    }


    public void setJSBridge(JSBridge jsBridge) {
        this.jsBridge = jsBridge;
        if (jsBridge != null) {
            addJavascriptInterface(jsBridge, "Revisit");
        }
    }

    public JSBridge getJSBridge() {
        return jsBridge;
    }

    public void destroyWebView() {
        clearHistory();
        clearCache(true);
        loadUrl("about:blank");
        pauseTimers();
        removeJavascriptInterface("Revisit");
        removeAllViews();
        destroy();
    }
}
```
