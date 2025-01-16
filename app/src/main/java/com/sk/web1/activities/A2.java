package com.sk.web1;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class A2 extends AppCompatActivity {

  private static final String TAG = "MainActivity";
  private static final Pattern QUOTE_REMOVAL_PATTERN = Pattern.compile("^\"|\"$");
  private static final String JS_INTERFACE_NAME = "Android";

  private WebView webView;
  private DrawerLayout drawerLayout;
  private LinearLayout consoleLayout;
  private ScrollView consoleScrollView;
  private JSAutoCompleteTextView jsInput;
  private Button executeJsButton;
  private final List<String> jsCodeHistory = new ArrayList<>();
  private WebAppInterface webAppInterface;

  @SuppressLint("SetJavaScriptEnabled")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.a2);

    initializeViews();
    setupWebView();
    // setupDrawer();
    setupAutocomplete();
    setupExecuteButton();

    getLifecycle()
        .addObserver(
            (LifecycleEventObserver)
                (source, event) -> {
                  if (event == Lifecycle.Event.ON_DESTROY && webView != null) {
                    webView.removeAllViews();
                    webView.destroy();
                    webView = null;
                  }
                });
  }

  private void initializeViews() {
    webView = findViewById(R.id.webView);
    drawerLayout = findViewById(R.id.drawer_layout);
    consoleLayout = findViewById(R.id.console_layout);
    consoleScrollView = findViewById(R.id.console_scroll_view);
    jsInput = findViewById(R.id.js_input);
    executeJsButton = findViewById(R.id.execute_js);
  }

  @SuppressLint("SetJavaScriptEnabled")
  private void setupWebView() {
    WebSettings webSettings = webView.getSettings();
    webSettings.setJavaScriptEnabled(true);
    webSettings.setUseWideViewPort(true);
    webSettings.setLoadWithOverviewMode(true);
    webSettings.setDomStorageEnabled(true);
    webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
    webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
    webSettings.setSupportMultipleWindows(true);
    webSettings.setAllowContentAccess(true);
    webSettings.setAllowFileAccess(true);
    webSettings.setAllowFileAccessFromFileURLs(true);
    webSettings.setAllowUniversalAccessFromFileURLs(true);
    webSettings.setSafeBrowsingEnabled(false);

    MyWebChromeClient chromeClient = new MyWebChromeClient(consoleLayout, this);
    MyWebViewClient viewClient = new MyWebViewClient();

    webView.setWebChromeClient(chromeClient);
    webView.setWebViewClient(viewClient);

    webAppInterface = new WebAppInterface(this, webView);

    webView.addJavascriptInterface(webAppInterface, JS_INTERFACE_NAME);
    webView.loadUrl("file:///android_asset/index.html");
  }

  private static class MyWebViewClient extends WebViewClient {
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
      return false;
    }
  }

  private void setupDrawer() {
    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    drawerLayout.openDrawer(Gravity.RIGHT);
    drawerLayout.closeDrawer(Gravity.RIGHT);
  }

  private void setupAutocomplete() {
    jsInput.setWebView(webView);
  }

  private void setupExecuteButton() {
    executeJsButton.setOnClickListener(
        v -> {
          String jsCode = jsInput.getText().toString().trim();
          if (!jsCode.isEmpty()) {
            jsCodeHistory.add(jsCode);
            jsInput.adapter.notifyDataSetChanged();
            executeJS(jsCode);
            jsInput.setText("");
          }
        });

    executeJsButton.setOnLongClickListener(
        new Button.OnLongClickListener() {

          @Override
          public boolean onLongClick(View arg0) {
          consoleLayout.removeAllViewsInLayout();
            return false;
          }
        });
  }

  private void executeJS(String jsCode) {
    webView.evaluateJavascript(
        jsCode,
        result -> {
          if (result != null) {
            Matcher matcher = QUOTE_REMOVAL_PATTERN.matcher(result);
            String formattedResult = matcher.replaceAll("");
            addConsoleMessage(
                ">" + jsCode + '\n' + "Result: " + formattedResult,
                ConsoleMessage.MessageLevel.LOG);
          } else {
            Log.e(TAG, "JavaScript evaluation returned null.");
            addConsoleMessage(
                "Error: JavaScript evaluation returned null.", ConsoleMessage.MessageLevel.ERROR);
          }
        });
  }

  private void addConsoleMessage(String message, ConsoleMessage.MessageLevel level) {
    runOnUiThread(
        () -> {
          TextView textView = new TextView(this);
          textView.setText(message);
          LinearLayout.LayoutParams params =
              new LinearLayout.LayoutParams(
                  LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
          textView.setLayoutParams(params);

          int color = getColorForLogLevel(level);
          textView.setTextColor(color);
          textView.setTextIsSelectable(true);
          consoleLayout.addView(textView);
          consoleScrollView.fullScroll(ScrollView.FOCUS_DOWN);
        });
  }

  private int getColorForLogLevel(ConsoleMessage.MessageLevel level) {
    switch (level) {
      case DEBUG:
        return ContextCompat.getColor(this, android.R.color.darker_gray);
      case ERROR:
        return ContextCompat.getColor(this, android.R.color.holo_red_dark);
      case WARNING:
        return ContextCompat.getColor(this, android.R.color.holo_orange_dark);
      case LOG:
      default:
        return ContextCompat.getColor(this, android.R.color.black);
    }
  }

  private static class MyWebChromeClient extends WebChromeClient {
    LinearLayout ll;
    Context main;

    MyWebChromeClient(LinearLayout ll, Context main) {
      this.ll = ll;
      this.main = main;
    }

    @Override
    public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
      String formattedMessage =
          consoleMessage.message()
              + ":at "
              + consoleMessage.lineNumber()
              + " in "
              + consoleMessage.sourceId();

      TextView msg = new TextView(main);
      msg.setText(formattedMessage);
      msg.setTextColor(getColorForLogLevel(consoleMessage.messageLevel()));
      ll.addView(msg);
      return true;
    }

    private int getColorForLogLevel(ConsoleMessage.MessageLevel level) {
      switch (level) {
        case DEBUG:
          return ContextCompat.getColor(main, android.R.color.darker_gray);
        case ERROR:
          return ContextCompat.getColor(main, android.R.color.holo_red_dark);
        case WARNING:
          return ContextCompat.getColor(main, android.R.color.holo_orange_dark);
        case LOG:
        default:
          return ContextCompat.getColor(main, android.R.color.black);
      }
    }
  }

  public static class WebAppInterface {
    private final Context mContext;
    WebView webview;

    WebAppInterface(Context c, WebView webview) {
      mContext = c;
      this.webview = webview;
    }

    @JavascriptInterface
    public void showToast(String toast) {
      Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
    }

    @JavascriptInterface
    public void loadUrl(String url) {
      webview.loadUrl(url);
    }
  }
}
