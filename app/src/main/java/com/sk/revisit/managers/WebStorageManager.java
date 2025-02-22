package com.sk.revisit.managers;

import android.net.Uri;
import android.webkit.URLUtil;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sk.revisit.MyUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import okhttp3.Headers;

public class WebStorageManager {

	private static final String TAG = "WebStorageManager";
	private static final String GET_METHOD = "GET";
	private static final String UTF_8 = "UTF-8";
	private final MyUtils utils;
	final SQLiteDBM dbm;

	public WebStorageManager(MyUtils utils) {
		this.utils = utils;
		this.dbm = utils.dbm;
	}

	/**
	 * Intercepts and handles web resource requests.
	 *
	 * @param request The web resource request.
	 * @return A WebResourceResponse if the request can be handled locally, null otherwise.
	 */
	@Nullable
	public WebResourceResponse getResponse(WebResourceRequest request) {
		MyUtils.requests++;
		if (!GET_METHOD.equals(request.getMethod())) {
			utils.log(TAG," Request method is not GET, ignoring: " + request.getMethod());
			return null;
		}


		Uri uri = request.getUrl();
		String uriStr= uri.toString();
		String q=uri.getQuery();
		if(q!=null){
			utils.log(TAG,uriStr+" "+q);
			//return null;
		}



		if (!URLUtil.isNetworkUrl(uriStr)) {
			utils.log(TAG," Not a network URL, ignoring: " + uriStr);
			return null;
		}

		String localPath = utils.buildLocalPath(uri);
		File localFile = new File(localPath);

		MyUtils.DownloadListener listener =  new MyUtils.DownloadListener(){
			@Override
			public void onSuccess(File file, Headers headers) {
				utils.saveResp(uriStr+"|"+localPath+"|"+file.length()+"|"+headers.toString());
				// dbm.insertIntoUrlsIfNotExists(uri, localPath, file.length(), headers);
			}

			@Override
			public void onFailure(Exception e) {
				MyUtils.requests++;
				utils.saveReq(uri.getHost()+","+uriStr);
				//dbm.insertIntoQueIfNotExists(uri);
			}
		};

		if (localFile.exists()) {
			if (MyUtils.isNetworkAvailable&&MyUtils.shouldUpdate) {
				if (shouldUpdateLocalFile(uri, localPath)) {
					utils.log(TAG, "Updating local file: " + localPath);
					utils.download(uri,listener);
				}
			}
			//utils.log(TAG, "Loading from local file: " + localPath);
			return loadFromLocal(localFile);
		} else {
			if (MyUtils.isNetworkAvailable) {
				utils.log(TAG, "Local file does not exist, downloading: " + localPath);
				utils.download(uri,listener);
				return loadFromLocal(localFile);
			}
			utils.saveReq(uri.getHost()+","+uriStr);
			//dbm.insertIntoQueIfNotExists(uri);
			return new WebResourceResponse("text/html", UTF_8, new ByteArrayInputStream("no offline file  or err refresh".getBytes()));
		}
	}

	/**
	 * Checks if the local file should be updated based on the remote file size.
	 *
	 * @param uri       The URI of the remote resource.
	 * @param localPath The local path of the file.
	 * @return True if the local file should be updated, false otherwise.
	 */
	private boolean shouldUpdateLocalFile(Uri uri, String localPath) {
		utils.log(TAG,"Checking for update...");
		long remoteSize = utils.getSizeFromUrl(uri);
		long localSize = utils.getSizeFromLocal(localPath);
		return remoteSize != -1 && localSize != -1 && remoteSize != localSize;
	}

	/**
	 * Loads a resource from a local file.
	 *
	 * @param localFile The local file to load from.
	 * @return A WebResourceResponse containing the local file's data, or null if an error occurs.
	 */
	@Nullable
	private WebResourceResponse loadFromLocal(@NonNull File localFile) {
		if (!localFile.exists() || !localFile.isFile()) {
			utils.log(TAG, "Local file does not exist or is not a file: " + localFile.getAbsolutePath());
			return null;
		}
		String mimeType = utils.getMimeType(localFile.getPath());
		try {
			InputStream fis = new FileInputStream(localFile);
			MyUtils.resolved++;
			return new WebResourceResponse(mimeType, UTF_8, fis);
		} catch (FileNotFoundException e) {
			utils.log(TAG, "Error loading from local file: " + localFile.getAbsolutePath(), e);
			return null;
		}
	}
}