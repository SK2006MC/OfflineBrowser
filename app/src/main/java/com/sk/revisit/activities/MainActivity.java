package com.sk.revisit.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.sk.revisit.MyUtils;
import com.sk.revisit.R;
import com.sk.revisit.databinding.ActivityMainBinding;
import com.sk.revisit.jsact.JSConsoleLogger;
import com.sk.revisit.jsact.JSWebViewManager;
import com.sk.revisit.jsv2.JSAutoCompleteTextView;
import com.sk.revisit.webview.MyWebViewClient;

public class MainActivity extends AppCompatActivity {

    private EditText urlEditText;
    private WebView mainWebView;
    private NavigationView mainNavigationView;
    private ScrollView jsConsoleScrollView;
    private LinearLayout jsConsoleLayout,bg;
    private DrawerLayout mainDrawerLayout;
    private JSAutoCompleteTextView jsInputTextView;
    private ImageButton executeJsButton;

    // Managers and Utilities
    private JSConsoleLogger jsConsoleLogger;
    private JSWebViewManager jsWebViewManager;
    private MyUtils myUtils;

    // Binding
    private ActivityMainBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize UI elements using binding
        initializeUI();

        // Initialize Managers and Utilities
        myUtils = new MyUtils(this, getObbDir().getAbsolutePath());
        jsConsoleLogger = new JSConsoleLogger(this, jsConsoleLayout, jsConsoleScrollView);
        jsWebViewManager = new JSWebViewManager(this, mainWebView, jsConsoleLogger);

        // Initialize components
        initNetworkChangeListener();
        initJSConsole();
        initNavView(mainNavigationView);
        initWebView(mainWebView);
        initUrlEditText(urlEditText, mainWebView);

        jsInputTextView.setWebView(mainWebView);
    }

    private void initializeUI() {
        mainDrawerLayout = binding.drawerLayout;
        mainNavigationView = binding.myNav;
        View headerView = mainNavigationView.getHeaderView(0);
        urlEditText = headerView.findViewById(R.id.urlEditText);
        mainWebView = binding.myWebView;
        jsInputTextView = binding.jsInput;
        jsConsoleLayout = binding.consoleLayout;
        jsConsoleScrollView = binding.consoleScrollView;
        executeJsButton = binding.executeJsBtn;

        bg=binding.myNav.getHeaderView(0).findViewById(R.id.bg);
    }

    private void initNetworkChangeListener() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest request = new NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR).addTransportType(NetworkCapabilities.TRANSPORT_WIFI).build();
        connectivityManager.registerNetworkCallback(request, new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                super.onAvailable(network);
                MyUtils.isNetworkAvailable = true;
                chbg(MyUtils.isNetworkAvailable);
            }

            @Override
            public void onLost(@NonNull Network network) {
                super.onLost(network);
                MyUtils.isNetworkAvailable = false;
                chbg(MyUtils.isNetworkAvailable);
            }
        });
    }

    @SuppressLint("ResourceAsColor")
    public void chbg(boolean o){
        runOnUiThread(()->{
            if(o){
                bg.setBackgroundColor(R.color.black);
            }else {
                bg.setBackgroundColor(R.color.teal_700);
            }

        });
    }
    private void initJSConsole() {
        executeJsButton.setOnClickListener(v -> {
            String code = jsInputTextView.getText().toString();
            jsWebViewManager.executeJS(code, r -> jsConsoleLogger.logConsoleMessage(">" + code + "\n" + r + "\n"));
        });

        executeJsButton.setOnLongClickListener(arg0 -> {
            jsConsoleLayout.removeAllViewsInLayout();
            return true;
        });
    }

    private void initNavView(@NonNull NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_dn) {
                startMyActivity(DownloadActivity.class);
            } else if (id == R.id.nav_ud) {
                startMyActivity(UpdateActivity.class);
            } else if (id == R.id.nav_settings) {
                startMyActivity(SettingsActivity.class);
            } else if (id == R.id.nav_about) {
                startMyActivity(AboutActivity.class);
            } else if (id == R.id.nav_web) {
                startMyActivity(WebpagesActivity.class);
            } else if (id == R.id.nav_log) {
                startMyActivity(MyLogActivity.class);
            }
            // Return true to indicate that the item selection is handled
            return true;
        });
    }

    private void initUrlEditText(@NonNull EditText urlEditText, WebView webView) {
        urlEditText.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) return; // Only load URL when focus is lost
            try {
                webView.loadUrl(urlEditText.getText().toString());
            } catch (Exception e) {
                showAlert(e.toString());
            }
        });
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView(@NonNull WebView webView) {
        webView.setWebViewClient(new MyWebViewClient(myUtils));
        WebSettings webSettings = webView.getSettings();
        webSettings.setAllowContentAccess(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        webSettings.setUseWideViewPort(true);
        // webSettings.setUserAgentString(); // Consider setting a custom User-Agent if needed
    }

    @Override
    public void onBackPressed() {
        try {
            if (mainDrawerLayout.isDrawerOpen(mainNavigationView)) {
                mainDrawerLayout.closeDrawer(mainNavigationView);
            } else if (mainDrawerLayout.isDrawerOpen(R.id.jsnav)) {
                mainDrawerLayout.closeDrawer(R.id.jsnav);
            } else if (mainWebView.canGoBack()) {
                mainWebView.goBack();
            } else {
                super.onBackPressed();
            }
        } catch (Exception e) {
            showAlert(e.toString());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myUtils.shutdown();

    }

    private void startMyActivity(Class<?> activityClass) {
        startActivity(new Intent(this, activityClass));
    }

    private void showAlert(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}