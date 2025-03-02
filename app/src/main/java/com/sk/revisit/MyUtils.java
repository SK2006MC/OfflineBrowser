package com.sk.revisit;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;

import com.sk.revisit.managers.SQLiteDBM;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MyUtils {
	private static final String TAG = "MyUtils";
	private static final int MAX_THREADS = 8;
	private static final String INDEX_HTML = "index.html";
	private static final long INVALID_SIZE = -1;
	private static final long REMOTE_SIZE_CACHE_EXPIRATION_MILLIS = 5 * 60 * 1000; // 5 minutes
	// Use atomic counters for thread safety
	public static AtomicLong requests = new AtomicLong(0);
	public static AtomicLong resolved = new AtomicLong(0);
	public static AtomicLong failed = new AtomicLong(0);
	public static boolean isNetworkAvailable = false, shouldUpdate = false;
	public final SQLiteDBM dbm;
	public final String rootPath;
	public final Context context;
	private final ExecutorService executorService;
	private final ExecutorService loggingExecutor;
	private final OkHttpClient client;
	private final LoggerHelper logger;
	// Cache for remote file sizes (to avoid repeating HEAD requests)
	private final ConcurrentHashMap<String, CachedSize> remoteSizeCache = new ConcurrentHashMap<>();

	public MyUtils(Context context, String rootPath) {
		this.rootPath = rootPath;
		this.context = context;
		this.executorService = Executors.newFixedThreadPool(MAX_THREADS, new CustomThreadFactory());
		this.loggingExecutor = Executors.newSingleThreadExecutor(new LoggingThreadFactory());
		this.client = new OkHttpClient.Builder()
				.connectTimeout(10, TimeUnit.SECONDS)
				.readTimeout(30, TimeUnit.SECONDS)
				.build();
		this.dbm = new SQLiteDBM(context, rootPath + "/revisit.db");
		this.logger = new LoggerHelper(context, rootPath, loggingExecutor);
	}

	// Logging methods use the separate logging executor.
	public void log(String tag, String msg, Exception e) {
		loggingExecutor.execute(() -> logger.log(tag + "\t" + msg + "\t" + e.toString()));
	}

	public void log(String tag, String msg) {
		loggingExecutor.execute(() -> logger.log(tag + "\t" + msg));
	}

	public void saveReq(String m) {
		loggingExecutor.execute(() -> logger.saveReq(m));
	}

	public void saveResp(String m) {
		loggingExecutor.execute(() -> logger.saveResp(m));
	}

	/**
	 * Builds a local file path based on the given URI.
	 */
	public String buildLocalPath(@NonNull Uri uri) {
		String lastPathSegment = uri.getLastPathSegment();
		String host = uri.getHost();
		String encodedPath = uri.getEncodedPath();

		if (TextUtils.isEmpty(host) || TextUtils.isEmpty(encodedPath)) {
			log(TAG, "Invalid URI: Host or path is empty.");
			return null;
		}

		String localPath = rootPath + File.separator + host + encodedPath;

		if (lastPathSegment == null || !lastPathSegment.contains(".")) {
			return localPath.endsWith("/") ? localPath + INDEX_HTML : localPath + File.separator + INDEX_HTML;
		}
		return localPath;
	}

	@NonNull
	public String getMimeType(String url) {
		String extension = MimeTypeMap.getFileExtensionFromUrl(url);
		return extension != null ? MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) : "application/octet-stream";
	}

	/**
	 * Gets the size of a local file.
	 */
	public long getSizeFromLocal(@NonNull String filePath) {
		File file = new File(filePath);
		return file.exists() ? file.length() : INVALID_SIZE;
	}

	/**
	 * Gets the remote file size via a HEAD request. Uses a cache to avoid repeated network calls.
	 */
	public long getSizeFromUrl(@NonNull Uri uri) {
		String key = uri.toString();
		CachedSize cached = remoteSizeCache.get(key);
		long now = System.currentTimeMillis();
		if (cached != null && now - cached.timestamp < REMOTE_SIZE_CACHE_EXPIRATION_MILLIS) {
			return cached.size;
		}

		Request request = new Request.Builder()
				.url(key)
				.head()
				.build();

		try (Response response = client.newCall(request).execute()) {
			if (response.isSuccessful() && response.body() != null) {
				long size = response.body().contentLength();
				remoteSizeCache.put(key, new CachedSize(size, now));
				return size;
			} else {
				log(TAG, "Error getting size from URL: " + uri + ". Response code: " + response.code());
			}
		} catch (IOException e) {
			log(TAG, "Error getting size from URL: " + uri, e);
		}
		return INVALID_SIZE;
	}

	/**
	 * Downloads a resource from a URI to a local file using buffered streams.
	 */
	public void download(@NonNull final Uri uri, @NonNull final DownloadListener listener) {
		executorService.execute(() -> {
			String localFilePath = buildLocalPath(uri);
			if (localFilePath == null) {
				listener.onFailure(new IOException("Failed to build local path for URI: " + uri));
				return;
			}

			// Check if file already exists and is up to date
			if (!shouldUpdate && new File(localFilePath).exists()) {
				log(TAG, "File already exists, skipping download: " + localFilePath);
				listener.onSuccess(new File(localFilePath), null);
				return;
			}

			Request request = new Request.Builder().url(uri.toString()).build();

			client.newCall(request).enqueue(new Callback() {
				@Override
				public void onFailure(@NonNull Call call, @NonNull IOException e) {
					log(TAG, "Download failed for URI: " + uri, e);
					listener.onFailure(e);
				}

				@Override
				public void onResponse(@NonNull Call call, @NonNull Response response) {
					if (!response.isSuccessful() || response.body() == null) {
						log(TAG, "Download failed. Response code: " + response.code() + " for URI: " + uri);
						listener.onFailure(new IOException("Download failed. Response code: " + response.code()));
						return;
					}

					try (InputStream in = new BufferedInputStream(response.body().byteStream());
						 BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(localFilePath))) {
						byte[] buffer = new byte[8192];
						int bytesRead;
						while ((bytesRead = in.read(buffer)) != -1) {
							out.write(buffer, 0, bytesRead);
						}
						out.flush();
						log(TAG, "Downloaded: " + uri + " to " + localFilePath);
						listener.onSuccess(new File(localFilePath), response.headers());

						// Store metadata asynchronously
						loggingExecutor.execute(() ->
								dbm.insertIntoUrlsIfNotExists(uri, localFilePath, new File(localFilePath).length(), response.headers())
						);

					} catch (IOException e) {
						log(TAG, "Error writing to file: " + localFilePath, e);
						listener.onFailure(e);
					}
				}
			});
		});
	}

	private File prepareFile(String path) {
		File file = new File(path);
		File parentFile = file.getParentFile();
		if (parentFile != null && !parentFile.exists() && !parentFile.mkdirs()) {
			log(TAG, "Failed to create directory: " + parentFile.getAbsolutePath());
			return null;
		}
		return file;
	}

	/**
	 * Shuts down the executors.
	 */
	public void shutdown() {
		executorService.shutdown();
		loggingExecutor.shutdown();
	}

	/**
	 * Listener interface for download events.
	 */
	public interface DownloadListener {
		void onSuccess(File file, Headers headers);

		void onFailure(Exception e);
	}

	// Thread factory for download tasks
	private static class CustomThreadFactory implements ThreadFactory {
		private int count = 0;

		@Override
		public Thread newThread(Runnable r) {
			return new Thread(r, "MyUtils-Thread-" + count++);
		}
	}

	// Thread factory for logging tasks (lower priority)
	private static class LoggingThreadFactory implements ThreadFactory {
		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r, "MyUtils-Logging-Thread");
			t.setPriority(Thread.MIN_PRIORITY);
			return t;
		}
	}

	// Simple class to cache remote file sizes
	private static class CachedSize {
		final long size;
		final long timestamp;

		CachedSize(long size, long timestamp) {
			this.size = size;
			this.timestamp = timestamp;
		}
	}
}
