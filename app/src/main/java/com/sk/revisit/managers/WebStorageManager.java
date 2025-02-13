package com.sk.revisit.managers;

import android.net.Uri;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import java.io.File;

public class WebStorageManager {

	MySettingsManager settingsManager;
	String rootPath;
	MyNetworkManager networkManager;

	public WebStorageManager() {

	}

	public WebResourceResponse getStoredResponse(WebResourceRequest request) {
		if (request.getMethod().equals("GET")) {
			
			Uri uri = request.getUrl();
			String localPath = buildLocalPath(uri, rootPath);
			File file = new File(localPath);
			
			if(file.exists()){
				
			}
		}
		return null;
	}

	public static String buildLocalPath(Uri uri, String rootPath) {
		String last = uri.getLastPathSegment();
		String localPathT = rootPath + '/' + uri.getHost() + uri.getEncodedPath();

		if (last == null) {
			return localPathT + "index.html";
		}

		if (last.contains(".")) {
			return localPathT;
		} else {
			return uri.toString().endsWith("/") ? localPathT + "index.html" : localPathT + "/index.html";
		}
	}
}