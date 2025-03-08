package com.sk.revisit.activities;

import android.content.Context;
import android.os.Bundle;
import android.widget.ListAdapter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.sk.revisit.Log;
import com.sk.revisit.R;
import com.sk.revisit.adapter.LogRecyclerAdapter;
import com.sk.revisit.databinding.ActivityLogBinding;

public class LogActivity extends AppCompatActivity {

	ActivityLogBinding binding;
	LogRecyclerAdapter adapter;


	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		binding = ActivityLogBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		adapter = new LogRecyclerAdapter(Log.getLogs());
		binding.logs.setLayoutManager(new LinearLayoutManager(this));

		DividerItemDecoration decoration = new DividerItemDecoration(
				binding.logs.getContext(),
				LinearLayoutManager.VERTICAL
		);

		decoration.setDrawable(ContextCompat.getDrawable(this, R.drawable.divider));

		binding.logs.addItemDecoration(decoration);

		binding.logs.setAdapter(adapter);

		binding.refreshButton.setOnClickListener(v -> loadLogs());
	}

	private void loadLogs() {
		adapter.notifyDataSetChanged();
	}
}