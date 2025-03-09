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