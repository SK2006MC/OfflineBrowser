package com.sk.revisit.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sk.revisit.data.Host;
import com.sk.revisit.data.Url;

import java.util.List;

public class HostUrlAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private static final String TAG = "HostUrlAdapter";
	private static final int VIEW_TYPE_HOST = 0;
	private static final int VIEW_TYPE_URL = 1;

	private List<Host> hostList;

	public HostUrlAdapter(List<Host> hostList) {
		this.hostList = hostList;
	}

	public void setHostList(List<Host> hostList) {
		this.hostList = hostList;
		notifyDataSetChanged();
	}

	@NonNull
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(parent.getContext());
		if (viewType == VIEW_TYPE_HOST) {
			ItemHostBinding binding = ItemHostBinding.inflate(inflater, parent, false);
			return new HostViewHolder(binding, this, presenter);
		} else if (viewType == VIEW_TYPE_URL) {
			ItemUrlBinding binding = ItemUrlBinding.inflate(inflater, parent, false);
			return new UrlViewHolder(binding, this, presenter);
		} else {
			throw new IllegalArgumentException("Invalid view type: " + viewType);
		}
	}

	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
		Object item = getItem(position);
		if (holder instanceof HostViewHolder && item instanceof Host) {
			((HostViewHolder) holder).bind((Host) item, position);
		} else if (holder instanceof UrlViewHolder && item instanceof Url) {
			int hostPosition = getHostPositionForUrl(position);
			int urlPositionInHost = getUrlPositionInHost(position, hostPosition);
			((UrlViewHolder) holder).bind((Url) item, hostPosition, urlPositionInHost);
		} else {
			Log.e(TAG, "onBindViewHolder: Invalid view holder or item type at position " + position);
		}
	}

	private int getHostPositionForUrl(int urlItemPosition) {
		int hostItemCount = 0;
		int hostIndex = -1;
		for (int i = 0; i < hostList.size(); i++) {
			Host host = hostList.get(i);
			hostIndex++; // Increment for the host item itself
			if (host.isExpanded()) {
				if (urlItemPosition > hostIndex && urlItemPosition <= hostIndex + host.getUrls().size()) {
					return i; // Found the host index for the URL item
				}
				hostIndex += host.getUrls().size();
			}
		}
		return -1; // Should not happen in normal cases, but return -1 to indicate error if needed
	}

	private int getUrlPositionInHost(int urlItemPosition, int hostPosition) {
		if (hostPosition == -1)
			return -1;
		int hostItemCount = 0;
		int urlIndexInHost;
		for (int i = 0; i < hostList.size(); i++) {
			Host host = hostList.get(i);
			hostItemCount++; // For the host item
			if (host.isExpanded()) {
				if (i == hostPosition) {
					urlIndexInHost = urlItemPosition - hostItemCount; // Calculate URL index within the host's URL list
					return urlIndexInHost;
				}
				hostItemCount += host.getUrls().size();
			}
		}
		return -1; // Should not happen in normal cases, but return -1 to indicate error if needed
	}

	@Override
	public int getItemViewType(int position) {
		Object item = getItem(position);
		if (item instanceof Host) {
			return VIEW_TYPE_HOST;
		} else if (item instanceof Url) {
			return VIEW_TYPE_URL;
		} else {
			return -1;
		}
	}

	@Override
	public int getItemCount() {
		int count = 0;
		for (Host host : hostList) {
			count++;
			if (host.isExpanded()) {
				count += host.getUrls().size();
			}
		}
		return count;
	}

	private Object getItem(int position) {
		int hostItemCount = 0;
		for (Host host : hostList) {
			if (hostItemCount == position) {
				return host;
			}
			hostItemCount++;
			if (host.isExpanded()) {
				if (position < hostItemCount + host.getUrls().size()) {
					return host.getUrls().get(position - hostItemCount);
				}
				hostItemCount += host.getUrls().size();
			}
		}
		return null;
	}

	// ViewHolder for Host items
	public static class HostViewHolder extends RecyclerView.ViewHolder {
		private final ItemHostBinding binding;
		private final HostUrlAdapter adapter;


		public HostViewHolder(ItemHostBinding binding, HostUrlAdapter adapter) {
			super(binding.getRoot());
			this.binding = binding;
			this.adapter = adapter;

		}

		public void bind(Host host, int position) {
			binding.hostNameTextView.setText(host.getName());
			binding.hostCheckBox.setChecked(host.isSelected());

			updateExpandCollapseIcon(host);

			binding.getRoot().setOnClickListener(v -> presenter.onHostExpanded(position));

			binding.hostCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> presenter.onHostSelectionChanged(position, isChecked));
		}

		private void updateExpandCollapseIcon(Host host) {
			if (host.isExpanded()) {
				binding.expandCollapseIcon.setImageResource(R.drawable.ic_expand_less);
			} else {
				binding.expandCollapseIcon.setImageResource(R.drawable.ic_expand_more);
			}
		}
	}

	// ViewHolder for URL items
	public static class UrlViewHolder extends RecyclerView.ViewHolder {
		private final ItemUrlBinding binding;
		private final HostUrlAdapter adapter;
		Presenter presenter;
		
		public UrlViewHolder(ItemUrlBinding binding, HostUrlAdapter adapter,Presenter presenter) {
			super(binding.getRoot());
			this.binding = binding;
			this.adapter = adapter;
		}

		public void bind(Url url, int hostPosition, int urlPositionInHost) {
			binding.urlTextView.setText(url.getUrl());
			binding.urlCheckBox.setChecked(url.isSelected());

			if (url.isUpdateAvailable()) {
				binding.updateIndicatorImageView.setVisibility(View.VISIBLE);
			} else {
				binding.updateIndicatorImageView.setVisibility(View.GONE);
			}

			binding.urlCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> presenter.onUrlSelectionChanged(hostPosition, urlPositionInHost, isChecked));
		}
	}
}