```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:gravity="center_vertical"
    android:orientation="vertical"
    android:padding="8dp">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:gravity="center_vertical"
        android:orientation="horizontal">

        <ImageView
            android:id="@+id/expandhost"
            android:layout_width="24dp"
            android:layout_height="24dp"
            android:contentDescription="@string/expand_collapse"
            android:src="@drawable/baseline_expand_more_24" />

        <CheckBox
            android:id="@+id/host_checkbox"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:minWidth="0dp"
            android:minHeight="0dp" />

        <TextView
            android:id="@+id/host_text"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:ellipsize="end"
            android:maxLines="1"
            android:textAppearance="?android:attr/textAppearanceMedium" />

        <TextView
            android:id="@+id/host_size"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:paddingStart="8dp"
            android:paddingEnd="8dp"
            android:textAppearance="?android:attr/textAppearanceSmall" />

        <ProgressBar
            android:id="@+id/host_progressbar"
            style="@style/Widget.AppCompat.ProgressBar.Horizontal"
            android:layout_width="75dp"
            android:layout_height="wrap_content"
            android:max="100" />
    </LinearLayout>

    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/urls"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:visibility="gone"
        app:layoutManager="androidx.recyclerview.widget.LinearLayoutManager" />

</LinearLayout>
```

```java
package com.sk.revisit.data;

import java.util.ArrayList;
import java.util.List;

public class Host {
    private String name;
    private boolean isSelected;
    private boolean isExpanded;
    private List<Url> urls;
    private long totalSize;
    private int progress;

    public Host(String name) {
        this.name = name;
        this.isSelected = false;
        this.isExpanded = false;
        this.urls = new ArrayList<>();
        this.totalSize = 0;
        this.progress = 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }

    public List<Url> getUrls() {
        return urls;
    }

    public void setUrls(List<Url> urls) {
        this.urls = urls;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }

    public void addUrl(Url url) {
        this.urls.add(url);
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }
}
```

```java
package com.sk.revisit.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sk.revisit.data.Host;
import com.sk.revisit.data.Url;
import com.sk.revisit.databinding.ItemHostBinding;

import java.util.List;

public class HostAdapter extends RecyclerView.Adapter<HostAdapter.HostViewHolder> {

    private List<Host> hosts;
    private OnHostClickListener onHostClickListener;

    public HostAdapter(List<Host> hosts) {
        this.hosts = hosts;
    }

    public void setHosts(List<Host> hosts) {
        this.hosts = hosts;
        notifyDataSetChanged();
    }

    public interface OnHostClickListener {
        void onHostClicked(int position);

        void onHostCheckedChanged(int position, boolean isChecked);
    }

    public void setOnHostClickListener(OnHostClickListener listener) {
        this.onHostClickListener = listener;
    }

    @NonNull
    @Override
    public HostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHostBinding binding = ItemHostBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new HostViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull HostViewHolder holder, int position) {
        Host host = hosts.get(position);
        holder.bind(host, position);
    }

    @Override
    public int getItemCount() {
        return hosts.size();
    }

    public class HostViewHolder extends RecyclerView.ViewHolder {
        ItemHostBinding binding;
        UrlAdapter urlAdapter;

        public HostViewHolder(@NonNull ItemHostBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Host host, int position) {
            binding.hostText.setText(host.getName());
            binding.hostSize.setText(String.valueOf(host.getTotalSize()));
            binding.hostProgressbar.setProgress(host.getProgress());
            binding.hostCheckbox.setChecked(host.isSelected());

            binding.urls.setLayoutManager(new LinearLayoutManager(binding.urls.getContext()));
            urlAdapter = new UrlAdapter(host.getUrls());
            binding.urls.setAdapter(urlAdapter);

            binding.urls.setVisibility(host.isExpanded() ? View.VISIBLE : View.GONE);


            binding.expandhost.setImageResource(host.isExpanded() ? android.R.drawable.arrow_up_float : android.R.drawable.arrow_down_float);


            binding.expandhost.setOnClickListener(v -> {
                host.setExpanded(!host.isExpanded());
                binding.urls.setVisibility(host.isExpanded() ? View.VISIBLE : View.GONE);

                binding.expandhost.setImageResource(host.isExpanded() ? android.R.drawable.arrow_up_float : android.R.drawable.arrow_down_float);
                if (onHostClickListener != null) {
                    onHostClickListener.onHostClicked(getAdapterPosition());
                    notifyItemChanged(getAdapterPosition());
                }
            });

            binding.hostCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                host.setSelected(isChecked);
                if (onHostClickListener != null) {
                    onHostClickListener.onHostCheckedChanged(getAdapterPosition(), isChecked);
                }
            });


        }
    }
}
```
