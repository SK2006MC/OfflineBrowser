package com.sk.web1;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import androidx.appcompat.app.AppCompatActivity;
import com.sk.web1.databinding.MainBinding;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import okhttp3.OkHttpClient;

public class MainActivity extends AppCompatActivity {
  MainBinding binding;
  SettingsManager sm;
  WebView myWebView;
  ArrayList<String> suggestions;
  OptimizedAutoCompleteTextView autoCompleteTextView;
  Context main;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    suggestions = new ArrayList<String>();
    binding = MainBinding.inflate(getLayoutInflater());
    main = this;
    
    sm = new SettingsManager(this);
    isFirstLaunched();
    setContentView(binding.getRoot());
    setupAutoCompleteTextView();
    setupWebView();
    
  }

  public void setupWebView() {

    FileMimeTypeResolver mimeTypeResolver = new FileMimeTypeResolver();
    ExecutorService exeService = Executors.newFixedThreadPool(3);
    OkHttpClient client = new OkHttpClient();
    NetworkUtils netUtils = new NetworkUtils();

    SQLiteDatabaseManager database = new SQLiteDatabaseManager(this, sm.getRootPath());

    HttpDownloader downloader = new HttpDownloader(this, exeService, client);
    FileCacheManager cacheManager =
        new FileCacheManager(
            this, sm, sm.getRootPath(), downloader, mimeTypeResolver, database, client, netUtils);

    myWebView = binding.myWebView;
    CustomWebViewClient webViewClient = new CustomWebViewClient(cacheManager,suggestions);
    myWebView.setWebViewClient(webViewClient);
    WebChromeClient webChromeClient =new WebChromeClient();
    myWebView.setWebChromeClient(webChromeClient);
    WebSettings settings = myWebView.getSettings();
    settings.setAllowContentAccess(true);
    settings.setAllowFileAccess(true);
    settings.setDomStorageEnabled(true);
    settings.setJavaScriptEnabled(true);
    settings.setMixedContentMode(1);
    settings.setSafeBrowsingEnabled(false);
    settings.setUseWideViewPort(true);
    settings.setJavaScriptCanOpenWindowsAutomatically(true);
    settings.setOffscreenPreRaster(true);
  }

  public void loadUrl(View v) {
    String url = autoCompleteTextView.getText().toString();
    myWebView.loadUrl(url);
    binding.info1.setText(myWebView.getContentDescription());
    if (!suggestions.contains(url)) suggestions.add(url);
  }

  void setupAutoCompleteTextView() {
    autoCompleteTextView = binding.autoCompleteTextView;
    suggestions.add("google.com");
    suggestions.add("html5.com");
    autoCompleteTextView.setSuggestions(suggestions);
    autoCompleteTextView.setDebounceDelay(500);
  }

  public void gotoSettings(View v) {
    startActivity(new Intent(this, SettingsActivity.class));
  }

  private void isFirstLaunched() {
    if (sm.getIsFirst()) {
      startActivity(new Intent(this, FirstActivity.class));
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    sm.done();
  }

  @Override
  public void onBackPressed() {
    if(binding.drawerLayout.isOpen()){
      binding.drawerLayout.close();
    }else if(myWebView.canGoBack()){
      myWebView.goBack();
    }else{
      super.onBackPressed();
    }
  }
}
