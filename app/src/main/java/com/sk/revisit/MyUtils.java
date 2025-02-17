package com.sk.revisit;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Base64;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;

import com.sk.revisit.managers.MyLogManager;
import com.sk.revisit.managers.SQLiteDBM;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MyUtils {
	private static final String TAG = "MyUtils";
	private static final int MAX_THREADS = 8;
	private static final String INDEX_HTML = "index.html";
	private static final long INVALID_SIZE = -1;
	public static long requests =0,resolved=0,failed=0;
	public static boolean isNetworkAvailable=false,shouldUpdate=false;
	public final SQLiteDBM dbm;
	public final String rootPath;
	private final ExecutorService executorService;
	private final OkHttpClient client;
	public final Context context;

	MyLogManager myLogManager,req,resp;

	public MyUtils(Context context, String rootPath) {
		this.rootPath = rootPath;
		this.context = context;
		this.executorService = Executors.newFixedThreadPool(MAX_THREADS);
		//this.executorService = Executors.newThreadPool();
		this.client = new OkHttpClient();
		this.dbm = new SQLiteDBM(context, rootPath + "/revisit.db");
		this.myLogManager=new MyLogManager(context,rootPath+"/log.txt");
		this.req=new MyLogManager(context,rootPath+"/req.txt");
		this.resp=new MyLogManager(context,rootPath+"/saved.base64");
	}

	public void log(String tag,String msg,Exception e){
		executorService.execute(()->myLogManager.log(tag+"\t"+msg+"\t"+e.toString()+"\n"));
	}

	public void log(String tag,String msg){
		executorService.execute(()->myLogManager.log(tag+"\t"+msg+"\n"));
	}

	public void saveReq(String m){
		executorService.execute(()->req.log(m+"\n"));
	}

	public void saveResp(String m){
		executorService.execute(()->resp.log(Base64.encodeToString(m.getBytes(),1)+"\n----\n"));
	}

	/**
	 * Builds a local file path based on the given URI.
	 *
	 * @param uri The URI to build the local path from.
	 * @return The local file path as a String.
	 */
	public String buildLocalPath(@NonNull Uri uri) {
		String lastPathSegment = uri.getLastPathSegment();
		String host = uri.getHost();
		String encodedPath = uri.getEncodedPath();

		String q=uri.getQuery();
		if(q!=null){
			//uriStr = uri.toString().split("\\?")[0]+Base64.encodeToString(q.getBytes("UTF-8"),Base64.NO_WRAP);
		}else{
			//uriStr = uri.toString();
		}

		// Validate inputs
		if (TextUtils.isEmpty(host) || TextUtils.isEmpty(encodedPath)) {
			log(TAG, "Invalid URI: Host or path is empty.");
			return null; // Or throw an exception if appropriate
		}

		String localPath = rootPath + File.separator + host + encodedPath;

		if (lastPathSegment == null) {
			return localPath + File.separator + INDEX_HTML;
		}

		if (lastPathSegment.contains(".")) {
			return localPath;
		} else {
			return localPath.endsWith("/") ? localPath + INDEX_HTML : localPath + File.separator + INDEX_HTML;
		}
	}


	@NonNull
	public String getMimeType(String url) {
		String type = null;
		String extension = MimeTypeMap.getFileExtensionFromUrl(url);
		if (extension != null) {
			type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
		}
		return type != null ? type : "application/octet-stream";
	}

	/**
	 * Gets the size of a local file.
	 *
	 * @param filePath The path to the local file.
	 * @return The size of the file in bytes, or -1 if the file does not exist.
	 */
	public long getSizeFromLocal(@NonNull String filePath) {
		File file = new File(filePath);
		return file.exists() ? file.length() : INVALID_SIZE;
	}

	/**
	 * Gets the size of a resource from a URL using OkHttp.
	 *
	 * @param uri The URI of the resource.
	 * @return The size of the resource in bytes, or -1 if an error occurs.
	 */
	public long getSizeFromUrl(@NonNull Uri uri) {
		Request request = new Request.Builder()
				.url(uri.toString())
				.head() // Use HEAD request to get only headers
				.build();

		try (Response response = client.newCall(request).execute()) {
			if (response.isSuccessful()) {
				ResponseBody body = response.body();
				if (body != null) {
					return body.contentLength();
				}
			} else {
				log(TAG, "Error getting size from URL: " + uri + ". Response code: " + response.code());
			}
		} catch (IOException e) {
			log(TAG, "Error getting size from URL: " + uri, e);
		}
		return INVALID_SIZE;
	}

	/**
	 * Downloads a resource from a URI to a local file using OkHttp.
	 *
	 * @param uri      The URI of the resource to download.
	 * @param listener The listener to receive download events.
	 */
	public void download(@NonNull final Uri uri, @NonNull final DownloadListener listener) {
		executorService.execute(() -> {
			String localFilePath = buildLocalPath(uri);
			if (localFilePath == null) {
				listener.onFailure(new IOException("Failed to build local path for URI: " + uri));
				return;
			}
			File localFile = new File(localFilePath);
			File parentFile = localFile.getParentFile();
			if (parentFile != null && !parentFile.exists()) {
				boolean mkdirs = parentFile.mkdirs();
				if (!mkdirs) {
					listener.onFailure(new IOException("Failed to create directory: " + parentFile.getAbsolutePath()));
					return;
				}
				log(TAG, "Created directory: " + parentFile.getAbsolutePath());
			}

			Request request = new Request.Builder()
					.url(uri.toString())
					.build();

			client.newCall(request).enqueue(new Callback() {
				@Override
				public void onFailure(@NonNull Call call, @NonNull IOException e) {
					log(TAG, "Download failed for URI: " + uri, e);
					listener.onFailure(e);
				}

				@Override
				public void onResponse(@NonNull Call call, @NonNull Response response) {
					if (response.isSuccessful()) {
						ResponseBody body = response.body();
						if (body == null) {
							listener.onFailure(new IOException("Empty response body for URI: " + uri));
							return;
						}
						try (InputStream in = body.byteStream();
							 FileOutputStream out = new FileOutputStream(localFile)) {
							byte[] buffer = new byte[8192];
							int bytesRead;
							while ((bytesRead = in.read(buffer)) != -1) {
								out.write(buffer, 0, bytesRead);
							}
							log(TAG, "Downloaded: " + uri + " to " + localFilePath);
							listener.onSuccess(localFile, response.headers());
						} catch (IOException e) {
							log(TAG, "Error writing to file: " + localFilePath, e);
							listener.onFailure(e);
						}
					} else {
						log(TAG, "Download failed. Response code: " + response.code() + " for URI: " + uri);
						listener.onFailure(new IOException("Download failed. Response code: " + response.code()));
					}
				}
			});
		});
	}

	/**
	 * Shuts down the executor service.
	 */
	public void shutdown() {
		executorService.shutdown();
	}

	/**
	 * Interface for listening to download events.
	 */
	public interface DownloadListener {
		/**
		 * Called when the download is successful.
		 *
		 * @param file The downloaded file.
		 */
		void onSuccess(File file, Headers headers);

		/**
		 * Called when the download fails.
		 *
		 * @param e The exception that caused the failure.
		 */
		void onFailure(Exception e);
	}
}
