package com.sk.revisit.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sk.revisit.R;
import com.sk.revisit.data.Host;
import com.sk.revisit.databinding.ItemHostBinding;

import java.util.List;

public class HostUrlAdapter extends RecyclerView.Adapter<HostUrlAdapter.HostUrlViewHolder> {

	private List<Host> hosts;

	public HostUrlAdapter(List<Host> hosts) {
		this.hosts = hosts;
	}

	public void setHostUrls(List<Host> hosts) {
		this.hosts = hosts;
		notifyDataSetChanged();
	}

	@NonNull
	@Override
	public HostUrlViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_host, parent, false);
		return new HostUrlViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull HostUrlViewHolder holder, int position) {
	}

	@Override
	public int getItemCount() {
		return hosts.size();
	}

	public static class HostUrlViewHolder extends RecyclerView.ViewHolder {

		public HostUrlViewHolder(@NonNull View itemView) {
			super(itemView);
		}
	}
}
