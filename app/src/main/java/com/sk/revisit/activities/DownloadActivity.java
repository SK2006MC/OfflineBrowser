package com.sk.revisit.activities;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.sk.revisit.MyUtils;
import com.sk.revisit.R;
import com.sk.revisit.adapter.UrlAdapter;
import com.sk.revisit.data.Url;
import com.sk.revisit.databinding.ActivityDownloadBinding;
import com.sk.revisit.log.Log;
import com.sk.revisit.managers.MySettingsManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Headers;

public class DownloadActivity extends AppCompatActivity {

	private static final String TAG = "DownloadActivity";
	private final Set<String> urlsStr = new HashSet<>();
	private ActivityDownloadBinding binding;
	private MyUtils myUtils;
	private MySettingsManager settingsManager;
	private UrlAdapter urlAdapter;
	private final List<Url> urlList = new ArrayList<>();
	private final Handler mainHandler = new Handler(Looper.getMainLooper());

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityDownloadBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		settingsManager = new MySettingsManager(this);
		myUtils = new MyUtils(this, settingsManager.getRootStoragePath());

		loadUrisFromFile();
		initRecyclerView();
		initUI();
	}

	private void initRecyclerView() {
		urlList.clear();
		for (String urlStr : urlsStr) {
			urlList.add(new Url(urlStr));
		}

		urlAdapter = new UrlAdapter(urlList);
		binding.urlsRecyclerview.setAdapter(urlAdapter);
		binding.urlsRecyclerview.setLayoutManager(new LinearLayoutManager(this));

		DividerItemDecoration decoration = new DividerItemDecoration(
				binding.urlsRecyclerview.getContext(),
				LinearLayoutManager.VERTICAL
		);
		decoration.setDrawable(Objects.requireNonNull(ContextCompat.getDrawable(this, R.drawable.divider)));
		binding.urlsRecyclerview.addItemDecoration(decoration);
	}

	@SuppressLint("NotifyDataSetChanged")
	public void refreshUrls() {
		// Ideally, only update the changed items
		urlAdapter.notifyDataSetChanged();
	}

	private void loadUrisFromFile() {
		String filePath = settingsManager.getRootStoragePath() + File.separator + "req.txt";

		File file = new File(filePath);

		if (!file.exists()) {
			Log.e(TAG, "req.txt not found at: " + filePath);
			showAlert("req.txt not found at: " + filePath);
			return;
		}

		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = reader.readLine()) != null) {
				String url = line.trim();
				urlsStr.add(url);
			}
		} catch (IOException e) {
			showAlert("Error reading req.txt");
		}
	}

	private void downloadSelectedUrls() {
		List<Url> selectedUrls = new ArrayList<>();
		for (Url url : urlList) {
			if (url.isSelected) {
				selectedUrls.add(url);
			}
		}

		if (selectedUrls.isEmpty()) {
			showAlert("No URLs selected for download.");
			return;
		}

		for (Url url : selectedUrls) {
			myUtils.download(Uri.parse(url.url), new MyUtils.DownloadListener() {
				@Override
				public void onSuccess(File file, Headers headers) {
					url.isDownloaded=true;
				}
				@Override
				public void onProgress(double p) {
					url.setProgress(p);
				}
				@Override
				public void onFailure(Exception e) {
					url.isDownloaded = false;
				}
			});
		}
	}

	private void calculateTotalSize() {
		long totalSize = 0;
		for (Url url : urlList) {
//			url.size =
			mainHandler.post(() -> {
				urlAdapter.notifyItemChanged(urlList.indexOf(url)); // Update size in RecyclerView
			});
		}

		long finalTotalSize = totalSize;
		mainHandler.post(() -> {
			binding.totalSizeTextview.setText("Total Size: " + finalTotalSize + " bytes");
		});
	}

	private void initUI() {
		binding.totalSizeTextview.setText(getString(R.string.total));

		binding.refreshButton.setOnClickListener(v -> {
			loadUrisFromFile();
			initRecyclerView(); // Reload data from file and refresh RecyclerView
		});

		binding.calcButton.setOnClickListener(v -> {
			calculateTotalSize();
		});

		binding.downloadButton.setOnClickListener(v -> {
			downloadSelectedUrls();
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		myUtils.shutdown();
	}

	private void showAlert(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show(); // Use LENGTH_SHORT for less intrusive messages
	}
}