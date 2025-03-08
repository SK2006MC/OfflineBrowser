package com.sk.revisit.data;

public class Url {
	private final String url;
	public long size;
	public int progress = 0;
	private boolean isDownloaded;
	private boolean isSelected;
	private boolean isUpdateAvailable;

	public Url(String url) {
		this.url = url;
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

	public void setSize(int size) {
		this.size = size;
	}

	public void setProgressListener(progressListener listener) {

	}

	public interface progressListener {
		void onProgressChanged(int p);
	}
}
