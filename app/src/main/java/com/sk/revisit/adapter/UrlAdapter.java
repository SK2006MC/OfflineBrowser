package com.sk.revisit.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sk.revisit.data.Url;
import com.sk.revisit.databinding.ItemUrlBinding;

import java.util.List;

public class UrlAdapter extends RecyclerView.Adapter<UrlAdapter.UrlHolderView> {

	List<Url> urls;

	public UrlAdapter(@NonNull List<Url> urls) {
		this.urls = urls;
	}

	public void setUrls(List<Url> urls) {
		this.urls = urls;
	}

	@NonNull
	@Override
	public UrlHolderView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		ItemUrlBinding binding = ItemUrlBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
		return new UrlHolderView(binding);
	}

	@Override
	public void onBindViewHolder(@NonNull UrlHolderView holder, int position) {
		Url url = urls.get(position);
		holder.bind(url);
	}

	@Override
	public int getItemCount() {
		return urls.size();
	}


	public static class UrlHolderView extends RecyclerView.ViewHolder {
		public ItemUrlBinding binding;

		public UrlHolderView(@NonNull ItemUrlBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}

		public void bind(@NonNull Url url) {
			binding.urlText.setText(url.getUrl());
		}
	}
}
