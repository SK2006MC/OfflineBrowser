package com.sk.revisit.activities;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.sk.revisit.databinding.ActivityWebpagesBinding;
import com.sk.revisit.managers.MySettingsManager;
import com.sk.revisit.managers.SQLiteDBM;

public class WebpagesActivity extends AppCompatActivity {

	public SQLiteDBM dbm;
	ActivityWebpagesBinding binding;
	RecyclerView recyclerView;
	MySettingsManager settingsManager;
	Button webpagesRefreshButton;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		binding = ActivityWebpagesBinding.inflate(getLayoutInflater());

		setContentView(binding.getRoot());

		settingsManager = new MySettingsManager(this);
		//dbm = new SQLiteDBM(this, settingsManager.getRootStoragePath());

		webpagesRefreshButton = binding.webpagesRefreshButton;
		recyclerView = binding.webpagesHosts;
	}

	void loadLocalPages(){

	}
}