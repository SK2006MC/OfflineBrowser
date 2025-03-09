```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:gravity="center_vertical"
    android:orientation="horizontal"
    android:padding="8dp">

    <CheckBox
        android:id="@+id/url_checkbox"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginEnd="8dp" />

    <TextView
        android:id="@+id/url_text"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_weight="1"
        android:ellipsize="middle"
        android:singleLine="true"
        android:textAppearance="?android:attr/textAppearanceListItemSmall" />

    <TextView
        android:id="@+id/url_size"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginStart="8dp"
        android:textAppearance="?android:attr/textAppearanceSmall" />

    <ProgressBar
        android:id="@+id/url_progressbar"
        style="?android:attr/progressBarStyleHorizontal"
        android:layout_width="80dp"
        android:layout_height="wrap_content"
        android:layout_marginStart="8dp"
        android:max="100" />

</LinearLayout>
```

```java
package com.sk.revisit.data;

public class Url {
    private final String url;
    private long size;
    private int progress;
    private boolean isDownloaded;
    private boolean isSelected;
    private boolean isUpdateAvailable;
    private progressListener listener;

    public Url(String url) {
        this.url = url;
        this.progress = 0; // Initialize progress to 0
    }

    public String getUrl() {
        return this.url;
    }

    public boolean isDownloaded() {
        return this.isDownloaded;
    }

    public void setDownloaded(boolean downloaded) {
        this.isDownloaded = downloaded;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public boolean isUpdateAvailable() {
        return isUpdateAvailable;
    }

    public void setUpdateAvailable(boolean updateAvailable) {
        isUpdateAvailable = updateAvailable;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }


    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        if (progress >= 0 && progress <= 100) {
            this.progress = progress;
            if (listener != null) {
                listener.onProgressChanged(progress);
            }
        }
    }

    public void setProgressListener(progressListener listener) {
        this.listener = listener;
    }

    public interface progressListener {
        void onProgressChanged(int progress);
    }
}
```

```java
package com.sk.revisit.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sk.revisit.data.Url;
import com.sk.revisit.databinding.ItemUrlBinding;

import java.util.List;
import java.util.Locale;

public class UrlAdapter extends RecyclerView.Adapter<UrlAdapter.UrlHolderView> {

    private List<Url> urls;
    private OnItemClickListener listener;

    public UrlAdapter(@NonNull List<Url> urls) {
        this.urls = urls;
    }

    public void setUrls(List<Url> urls) {
        this.urls = urls;
        notifyDataSetChanged(); // Refresh the RecyclerView when data changes
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
        holder.bind(url, listener);
    }

    @Override
    public int getItemCount() {
        return urls.size();
    }

    // Interface for item click
    public interface OnItemClickListener {
        void onItemClick(Url url);
    }

    // Method to set the item click listener
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public static class UrlHolderView extends RecyclerView.ViewHolder {
        public ItemUrlBinding binding;

        public UrlHolderView(@NonNull ItemUrlBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(@NonNull Url url, OnItemClickListener listener) {
            binding.urlText.setText(url.getUrl());

            // Format size to human-readable format (KB, MB, etc.)
            String sizeText = formatFileSize(url.getSize());
            binding.urlSize.setText(sizeText);

            binding.urlCheckbox.setChecked(url.isSelected());
            binding.urlProgressbar.setProgress(url.getProgress());

            // Set up progress listener to update progress bar
            url.setProgressListener(progress -> {
                binding.urlProgressbar.setProgress(progress);
            });

            // Handle item click
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(url);
                }
            });

            // Handle checkbox click
            binding.urlCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                url.setSelected(isChecked);
            });
        }

        // Helper method to format file size
        private String formatFileSize(long size) {
            String[] units = {"B", "KB", "MB", "GB", "TB"};
            int unitIndex = 0;
            double sizeInUnit = size;

            while (sizeInUnit > 1024 && unitIndex < units.length - 1) {
                sizeInUnit /= 1024;
                unitIndex++;
            }

            return String.format(Locale.getDefault(), "%.2f %s", sizeInUnit, units[unitIndex]);
        }
    }
}
```
