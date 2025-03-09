package com.sk.revisit.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sk.revisit.R;
import com.sk.revisit.data.Url;

import java.util.List;

public class UrlAdapter extends RecyclerView.Adapter<UrlAdapter.UrlViewHolder> {

	private final List<Url> urlList;

	public UrlAdapter(List<Url> urlList) {
		this.urlList = urlList;
	}

	@NonNull
	@Override
	public UrlViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_url, parent, false);
		return new UrlViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull UrlViewHolder holder, int position) {
		Url url = urlList.get(position);
		holder.urlTextView.setText(url.url);
		holder.sizeTextView.setText(url.size > 0 ? url.size + " bytes" : "Calculating...");
		holder.urlCheckbox.setChecked(url.isSelected);
		holder.progressBar.setProgress((int)url.progress);
		url.setOnProgressChangeListener((p)->{
			holder.progressBar.setProgress((int)p);
		});
		holder.urlCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> url.isSelected=isChecked);
	}

	@Override
	public int getItemCount() {
		return urlList.size();
	}

	public static class UrlViewHolder extends RecyclerView.ViewHolder {
		CheckBox urlCheckbox;
		TextView urlTextView;
		TextView sizeTextView;
		ProgressBar progressBar;

		public UrlViewHolder(@NonNull View itemView) {
			super(itemView);
			urlCheckbox = itemView.findViewById(R.id.url_checkbox);
			urlTextView = itemView.findViewById(R.id.url_textview);
			sizeTextView = itemView.findViewById(R.id.size_textview);
			progressBar = itemView.findViewById(R.id.progress_bar);
		}
	}
}