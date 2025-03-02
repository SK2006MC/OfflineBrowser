package com.sk.revisit.activities;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.sk.revisit.MyUtils;
import com.sk.revisit.adapter.HostUrlAdapter;
import com.sk.revisit.databinding.ActivityDownloadBinding;
import com.sk.revisit.managers.MySettingsManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public class DownloadActivity extends AppCompatActivity {

	private static final String TAG = "DownloadActivity";
	ActivityDownloadBinding binding;
	private MyUtils myUtils;
	private final Set<String> urlsToDownload = new HashSet<>();
	private MySettingsManager settingsManager;
	private TextView statusTextView;

	private final AtomicLong totalSize = new AtomicLong(0);
	private int urlCount = 0;
	private final Handler mainHandler = new Handler(Looper.getMainLooper());
	public RecyclerView recyclerView;
	HostUrlAdapter hostUrlAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityDownloadBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		settingsManager = new MySettingsManager(this);
		myUtils = new MyUtils(this, settingsManager.getRootStoragePath());
		hostUrlAdapter = new HostUrlAdapter(new ArrayList<>());

		initUI();
		loadUrls();
	}

	void initUI(){
		statusTextView = binding.downloadStatus;
		recyclerView = binding.hosts;
		binding.total.setText("0 B");
		binding.buttonDownload.setOnClickListener(v -> {
			downloadUrls();
		});
	}

	private void loadUrls() {
		String filePath = settingsManager.getRootStoragePath() + File.separator + "req.txt";
		File file = new File(filePath);
		if (!file.exists()) {
			Log.e(TAG, "req.txt not found at: " + filePath);
			Toast.makeText(this, "req.txt not found!", Toast.LENGTH_SHORT).show();
			return;
		}

		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line;
			//a line contains urlhost,url (seperated by ',')
			while ((line = reader.readLine()) != null) {
//				String host = line.trim().split(",")[0];
				String url = line.trim().split(",")[1];
				if (!url.isEmpty()) {
					urlsToDownload.add(url);
					urlCount++;
				}
			}
		} catch (IOException e) {
			Log.e(TAG, "Error reading req.txt", e);
			Toast.makeText(this, "Error reading req.txt", Toast.LENGTH_SHORT).show();
		}
		mainHandler.post(() -> statusTextView.setText("Urls to download: " + urlCount));

	}

	private void downloadUrls() {
		if (urlsToDownload.isEmpty()) {
			Toast.makeText(this, "No URLs to download!", Toast.LENGTH_SHORT).show();
			return;
		}
		binding.buttonDownload.setEnabled(false);

		for (String url : urlsToDownload) {
			Uri uri = Uri.parse(url);
			myUtils.download(uri, new MyUtils.DownloadListener() {
				@Override
				public void onSuccess(File file, okhttp3.Headers headers) {
					totalSize.addAndGet(file.length());
				}

				@Override
				public void onFailure(Exception e) {
					myUtils.saveReq(uri.getHost() + "," + url);
				}
			});
		}
		mainHandler.post(() -> Toast.makeText(this, "Downloading...", Toast.LENGTH_SHORT).show());
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
}
