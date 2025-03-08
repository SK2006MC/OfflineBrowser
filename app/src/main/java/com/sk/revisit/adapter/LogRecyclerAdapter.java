package com.sk.revisit.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sk.revisit.R;
import com.sk.revisit.databinding.ItemLogBinding;

import java.util.ArrayList;
import java.util.List;

public class LogRecyclerAdapter extends RecyclerView.Adapter<LogRecyclerAdapter.LogViewHolder> {

	List<String[]> logs;
	public LogRecyclerAdapter(List<String[]> logs) {
		this.logs = logs;
	}

	@NonNull
	@Override
	public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		ItemLogBinding binding = ItemLogBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
		return new LogViewHolder(binding);
	}

	@Override
	public int getItemCount() {
		return logs.size();
	}

	@Override
	public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
		holder.bind(logs.get(position));
	}

	public static class LogViewHolder extends RecyclerView.ViewHolder {
		private final ItemLogBinding binding;

		public LogViewHolder(@NonNull ItemLogBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}

		public void bind(String[] log) {
			binding.tag.setText(log[0]);
			binding.msg.setText(log[1]);
			try {
				binding.exception.setText(log[2]);
			} catch (Exception e) {
				binding.exception.setText(R.string.no_exception_given);
			}
		}
	}
}