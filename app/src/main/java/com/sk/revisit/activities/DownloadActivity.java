package com.sk.revisit.activities;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.sk.revisit.MyUtils;
import com.sk.revisit.adapter.HostAdapter;
import com.sk.revisit.data.Host;
import com.sk.revisit.data.Url;
import com.sk.revisit.databinding.ActivityDownloadBinding;
import com.sk.revisit.managers.MySettingsManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public class DownloadActivity extends AppCompatActivity {

    private static final String TAG = "DownloadActivity";
    private ActivityDownloadBinding binding;
    private MyUtils myUtils;
    private Map<String, List<String>> hosts = new HashMap<>(); // Initialize the map
    private MySettingsManager settingsManager;
    private TextView statusTextView;

    private final AtomicLong totalSize = new AtomicLong(0);
    private int urlCount = 0;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    public RecyclerView hostRecycler;
    private HostAdapter hostAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDownloadBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        settingsManager = new MySettingsManager(this);
        myUtils = new MyUtils(this, settingsManager.getRootStoragePath());

        initUI();
        loadUrlsFromFile();
        initRecyclerView();
    }

    void initRecyclerView() {
        List<Host> hosts2 = new ArrayList<>();
        hostAdapter = new HostAdapter(hosts2);
        hostRecycler = binding.hosts;

        Set<String> hostsStr = hosts.keySet();
        for (String host : hostsStr) {
            Host host2 = new Host(host);
            hosts2.add(host2);

            List<Url> urls = new ArrayList<>();
            List<String> urls1 = hosts.get(host);

            if (urls1 != null) {
                for (String url : urls1) {
                    urls.add(new Url(url));
                }
                host2.setUrls(urls);
            }
        }

        hostRecycler.setAdapter(hostAdapter);
        hostAdapter.notifyDataSetChanged();
    }

    void initUI() {
        statusTextView = binding.downloadStatus;
        hostRecycler = binding.hosts;
        binding.total.setText("0 B");
        binding.buttonDownload.setOnClickListener(v -> {
            // Implement download functionality here
        });
    }

    private void loadUrlsFromFile() {
        String filePath = settingsManager.getRootStoragePath() + File.separator + "req.txt";
        File file = new File(filePath);
        if (!file.exists()) {
            Log.e(TAG, "req.txt not found at: " + filePath);
            Toast.makeText(this, "req.txt not found at " + filePath, Toast.LENGTH_SHORT).show();
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String url = line.trim();
                Uri uri = Uri.parse(url);
                String host = uri.getHost();
                if (!url.isEmpty() && host != null) {
                    add(hosts, host, url);
                    urlCount++;
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error reading req.txt", e);
            Toast.makeText(this, "Error reading req.txt: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        mainHandler.post(() -> statusTextView.setText("Urls to download: " + urlCount));
    }

    private String getSize(long size) {
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int index = 0;
        double s = size;
        while (s >= 1024 && index < units.length - 1) {
            s /= 1024.0;
            index++;
        }
        return String.format("%.2f %s", s, units[index]);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myUtils.shutdown();
    }

    public void add(Map<String, List<String>> listMap, String host, String url) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            listMap.computeIfAbsent(host, k -> new ArrayList<>()).add(url);
        } else {
            if (!listMap.containsKey(host)) {
                listMap.put(host, new ArrayList<>());
            }
            listMap.get(host).add(url);
        }
    }
}