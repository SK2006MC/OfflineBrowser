package com.sk.revisit;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Base64;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;

import com.sk.revisit.helper.LoggerHelper;
import com.sk.revisit.helper.MimeTypeHelper;
import com.sk.revisit.log.Log;
import com.sk.revisit.managers.SQLiteDBM;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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
	public static AtomicLong requests = new AtomicLong(0);
	public static AtomicLong resolved = new AtomicLong(0);
	public static AtomicLong failed = new AtomicLong(0);
	public static boolean isNetworkAvailable = false, shouldUpdate = false;
	public final SQLiteDBM dbm;
	public final String rootPath;
	public final Context context;
	public final OkHttpClient client;
	private final ExecutorService executorService;
	private final LoggerHelper logger;
	public MimeTypeHelper mimeTypeHelper;

	public MyUtils(Context context, String rootPath) {
		this.rootPath = rootPath;
		this.context = context;
		this.executorService = Executors.newFixedThreadPool(MAX_THREADS, new CustomThreadFactory());
		this.client = new OkHttpClient.Builder()
				.connectTimeout(10, TimeUnit.SECONDS)
				.readTimeout(30, TimeUnit.SECONDS)
				.build();
		this.dbm = new SQLiteDBM(context, rootPath + "/revisit.db");
		this.logger = new LoggerHelper(context, rootPath);
		this.mimeTypeHelper = new MimeTypeHelper(this);
	}

	public void log(String tag, String msg, Exception e) {
		logger.log(tag + "\t" + msg + "\t" + e.toString());
	}

	public void log(String tag, String msg) {
		logger.log(tag + "\t" + msg);
	}

	public void saveReq(String m) {
		logger.saveReq(m);
	}

	public void saveUrl(String uriStr) {
		logger.saveUrl(uriStr);
	}

	public void saveResp(String m) {
		logger.saveResp(m);
	}

	/**
	 * Builds a local file path based on the given URI.
	 */
	public String buildLocalPath(@NonNull Uri uri) {
		String lastPathSegment = uri.getLastPathSegment();
		String host = uri.getAuthority();
		String path = uri.getPath();
		String query = uri.getQuery();

		if (query != null) {
			query = Base64.encodeToString(query.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);
		}

		if (TextUtils.isEmpty(host) || TextUtils.isEmpty(path)) {
			log(TAG, "Invalid URI: Host or path is empty.");
			return null;
		}

		String localPath = rootPath + File.separator + host + path;

		if (lastPathSegment == null || !lastPathSegment.contains(".")) {
			return localPath.endsWith("/") ? localPath + INDEX_HTML : localPath + File.separator + INDEX_HTML;
		}

//		log(uri.toString(), localPath + ",query=" + query);
		if(query!=null){
			localPath = localPath +':'+query;
		}
		log(TAG+" the uri="+uri, " localpath="+localPath);

		return localPath;
	}

	@NonNull
	public String getMimeType(String url) {
		String extension = MimeTypeMap.getFileExtensionFromUrl(url);
		return extension != null ? MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) : "application/octet-stream";
	}

	public void createMimeTypeMeta(Uri uri) {
		mimeTypeHelper.createMimeTypeMeta(uri);
	}

	public String getMimeTypeFromMeta(String filepath) {
		return mimeTypeHelper.getMimeTypeFromMeta(filepath);
	}

	public void createMimeTypeMetaFile(String filepath, String type) {
		mimeTypeHelper.createMimeTypeMetaFile(filepath, type);
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

			File file = new File(localFilePath);
			if(file.exists()&&!MyUtils.shouldUpdate){
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
					File outfile = prepareFile(localFilePath);
					long contentLength = response.body().contentLength();
					if (contentLength==0){
						contentLength=1;
					}
					try (InputStream in = new BufferedInputStream(response.body().byteStream());
						 BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outfile))) {
						byte[] buffer = new byte[8192];
						long bytesRead;
						while ((bytesRead = in.read(buffer)) != -1) {
							out.write(buffer, 0, (int) bytesRead);
							listener.onProgress((double) bytesRead /contentLength);
						}
						out.flush();

						log(TAG, "Downloaded: " + uri + " to " + localFilePath);
						listener.onSuccess(new File(localFilePath), response.headers());

						createMimeTypeMetaFile(localFilePath, response.body().contentType().toString());

						// Store metadata asynchronously
						executorService.execute(() ->
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

	public File prepareFile(String filepath) {
		try {
			File file = new File(filepath);
			File parentDir = file.getParentFile();
			if (parentDir != null && !parentDir.exists()) {
				parentDir.mkdirs();
			}
			if (!file.exists()) {
				file.createNewFile();
			}
			return file;
		} catch (Exception e) {
			Log.e(TAG, e.toString());
			return null;
		}
	}

	/**
	 * Shuts down the executors.
	 */
	public void shutdown() {
		executorService.shutdown();
		logger.shutdown();
	}

	/**
	 * Listener interface for download events.
	 */
	public interface DownloadListener {
		void onSuccess(File file, Headers headers);
		void onProgress(double p);
		void onFailure(Exception e);
	}

	private static class CustomThreadFactory implements ThreadFactory {
		private int count = 0;

		@Override
		public Thread newThread(Runnable r) {
			return new Thread(r, "MyUtils-Thread-" + count++);
		}
	}
}