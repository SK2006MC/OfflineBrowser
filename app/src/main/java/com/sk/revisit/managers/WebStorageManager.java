package com.sk.revisit.managers;

import android.net.Uri;
import android.webkit.URLUtil;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sk.revisit.MyUtils;

import org.jetbrains.annotations.Contract;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import okhttp3.Headers;

public class WebStorageManager {
	private static final String TAG = "WebStorageManager";
	private static final String GET_METHOD = "GET";
	private static final String UTF_8 = "UTF-8";
	private final MyUtils utils;

	public WebStorageManager(MyUtils utils) {
		this.utils = utils;
	}

	@Nullable
	public WebResourceResponse getResponse(@NonNull WebResourceRequest request) {
		MyUtils.requests.incrementAndGet();
		if (!GET_METHOD.equals(request.getMethod())) {
			utils.log(TAG, "Request method is not GET: " + request.getMethod());
			return null;
		}

		Uri uri = request.getUrl();
		String uriStr = uri.toString();
		if (!URLUtil.isNetworkUrl(uriStr)) {
			utils.log(TAG, "Not a network URL: " + uriStr);
			return null;
		}

		String localPath = utils.buildLocalPath(uri);
		if (localPath == null) return null;

		File localFile = new File(localPath);
		if (localFile.exists()) {
			if (MyUtils.shouldUpdate&&MyUtils.isNetworkAvailable) {
				utils.download(uri, createDownloadListener(uriStr, localPath));
			}
			return loadFromLocal(localFile);
		} else {
			if (MyUtils.isNetworkAvailable) {
				utils.download(uri, createDownloadListener(uriStr, localPath));
				return loadFromLocal(localFile);
			}
			utils.saveReq(uriStr);
			return new WebResourceResponse("text/html", UTF_8, new ByteArrayInputStream("No offline file available.".getBytes()));
		}
	}

	@NonNull
	@Contract("_, _ -> new")
	private MyUtils.DownloadListener createDownloadListener(String uriStr, String localPath) {
		return new MyUtils.DownloadListener() {
			@Override
			public void onSuccess(File file, Headers headers) {
				utils.saveResp(uriStr + "|" + localPath + "|" + file.length() + "|" + headers.toString());
			}

			@Override
			public void onFailure(Exception e) {
				MyUtils.failed.incrementAndGet();
				utils.saveReq(uriStr);
			}
		};
	}

	@Nullable
	private WebResourceResponse loadFromLocal(@NonNull File localFile) {
		try {
			return new WebResourceResponse(utils.getMimeType(localFile.getPath()), UTF_8, new FileInputStream(localFile));
		} catch (FileNotFoundException e) {
			utils.log(TAG, "File not found: " + localFile.getAbsolutePath(), e);
			return null;
		}
	}
}