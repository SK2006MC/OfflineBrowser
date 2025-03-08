package com.sk.revisit.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.sk.revisit.Log;
import com.sk.revisit.MyUtils;
import com.sk.revisit.R;
import com.sk.revisit.adapter.UrlAdapter;
import com.sk.revisit.data.Url;
import com.sk.revisit.databinding.ActivityDownloadBinding;
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

public class DownloadActivity extends AppCompatActivity {

	private static final String TAG = "DownloadActivity";
	private final Set<String> urlsStr = new HashSet<>();
	private ActivityDownloadBinding binding;
	private MyUtils myUtils;
	private MySettingsManager settingsManager;
	UrlAdapter urlAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityDownloadBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		settingsManager = new MySettingsManager(this);
		myUtils = new MyUtils(this, settingsManager.getRootStoragePath());

		loadUrisFromFile();
		initUI();
		initRecyclerView();
	}

	void initRecyclerView() {
		List<Url> urlList = new ArrayList<>();

		for (String urlStr : urlsStr) {
			urlList.add(new Url(urlStr));
		}

		urlAdapter = new UrlAdapter(urlList);
		binding.urls.setAdapter(urlAdapter);

		binding.urls.setLayoutManager(new LinearLayoutManager(this));
		DividerItemDecoration decoration=new DividerItemDecoration(
				binding.urls.getContext(),
				LinearLayoutManager.VERTICAL
		);
		decoration.setDrawable(Objects.requireNonNull(ContextCompat.getDrawable(this,R.drawable.divider)));
		binding.urls.addItemDecoration(decoration);
	}

	public void refreshUrls(){
		urlAdapter.notifyDataSetChanged();
	}

	private void loadUrisFromFile() {
		String filePath = settingsManager.getRootStoragePath() + File.separator + "req.txt";

		File file = new File(filePath);

		if (!file.exists()) {
			Log.e(TAG, "req.txt not found at: " + filePath);
			alert("req.txt not found at: " + filePath);
			return;
		}

		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = reader.readLine()) != null) {
				String url = line.trim();
				urlsStr.add(url);
			}
		} catch (IOException e) {
			alert("Error reading req.txt");
		}
	}

	void download(){

	}
	void initUI() {
		binding.total.setText("0 B");
		binding.refreshButton.setOnClickListener(v->{
			refreshUrls();
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		myUtils.shutdown();
	}

	void alert(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
	}
}
