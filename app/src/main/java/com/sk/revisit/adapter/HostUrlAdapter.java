package com.sk.revisit.adapter; // Replace with your package name

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import com.sk.revisit.databinding.ItemHostBinding;
import com.sk.revisit.databinding.ItemUrlBinding;
import com.sk.revisit.data.Host;
import com.sk.revisit.data.Url;
import java.util.List;

public class HostUrlAdapter extends ExpandableRecyclerViewAdapter<HostUrlAdapter.HostViewHolder, HostUrlAdapter.UrlViewHolder> {

    private Context context;
    private List<Host> hosts;

    public HostUrlAdapter(Context context, List<Host> hosts) {
        super(hosts);
        this.context = context;
        this.hosts = hosts;
    }

    @Override
    public HostViewHolder onCreateHostViewHolder(ViewGroup parent, int viewType) {
        ItemHostBinding binding = ItemHostBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new HostViewHolder(binding);
    }

    @Override
    public UrlViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        ItemUrlBinding binding = ItemUrlBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new UrlViewHolder(binding);
    }

    @Override
    public void onBindHostViewHolder(HostViewHolder holder, int flatPosition, ExpandableGroup group) {
        Host host = (Host) group;
        holder.binding.hostNameTextview.setText(host.name);

        holder.itemView.setOnClickListener(v -> {
            host.isExpanded = !host.isExpanded;
            notifyItemChanged(flatPosition); // Important!
            if (host.isExpanded) {
                holder.binding.urlRecyclerview.setVisibility(View.VISIBLE);
                holder.binding.urlRecyclerview.setLayoutManager(new LinearLayoutManager(context));
                UrlAdapter urlAdapter = new UrlAdapter(host.getUrls()); // Use the nested adapter
                holder.binding.urlRecyclerview.setAdapter(urlAdapter);
            } else {
                holder.binding.urlRecyclerview.setVisibility(View.GONE);
            }
        });

        holder.binding.hostCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Handle host checkbox changes here
        });
    }

    @Override
    public void onBindChildViewHolder(UrlViewHolder holder, int flatPosition, ExpandableGroup group, int childIndex) {
        Url child = group.getUrls().get(childIndex);
        holder.binding.urlNameTextview.setText(child.childText);

        holder.binding.urlCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Handle URL checkbox changes here
        });
    }

    public class HostViewHolder extends RecyclerView.ViewHolder {
        ItemHostBinding binding;

        public HostViewHolder(ItemHostBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public class UrlViewHolder extends RecyclerView.ViewHolder {
        ItemUrlBinding binding;

        public UrlViewHolder(ItemUrlBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }


    private class UrlAdapter extends RecyclerView.Adapter<UrlViewHolder> {
        private List<Url> urls;

        public UrlAdapter(List<Url> urls) {
            this.urls = urls;
        }

        @NonNull
        @Override
        public UrlViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemUrlBinding binding = ItemUrlBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new UrlViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull UrlViewHolder holder, int position) {
            Url url = urls.get(position);
            holder.binding.urlNameTextview.setText(url.childText);
        }

        @Override
        public int getItemCount() {
            return urls.size();
        }
    }
}
