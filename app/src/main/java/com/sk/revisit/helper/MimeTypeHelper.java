package com.sk.revisit.helper;

import android.net.Uri;

import com.sk.revisit.MyUtils;
import com.sk.revisit.log.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MimeTypeHelper {

	private static final String TAG = "MimeTypeHelper";
	private final OkHttpClient client;
	MyUtils utils;

	public MimeTypeHelper(MyUtils utils) { // Add rootPath to constructor
		this.utils = utils;
		this.client = utils.client;
	}


	/**
	 * Creates MIME type metadata for a given URI.
	 *
	 * @param uri The URI of the resource.
	 */
	public void createMimeTypeMeta(Uri uri) {

		if (!MyUtils.isNetworkAvailable) {
			Log.w(TAG, "Network not available. Skipping MIME type metadata creation.");
			return;
		}

		try {
			String localPath = utils.buildLocalPath(uri);
			if (localPath == null) {
				Log.e(TAG, "Failed to build local path for URI: " + uri);
				return;
			}
			Request request = new Request.Builder()
					.head()
					.url(uri.toString())
					.build();

			Response response = client.newCall(request).execute();

			if (!response.isSuccessful()) {
				Log.e(TAG, "Failed to get MIME type. HTTP error code: " + response.code() + " for URI: " + uri);
				return;
			}

			ResponseBody body = response.body();
			if (body == null) {
				Log.e(TAG, "Response body is null for URI: " + uri);
				return;
			}

			MediaType mediaType = body.contentType();
			if (mediaType == null) {
				Log.e(TAG, "Content type is null for URI: " + uri);
				return;
			}

			createMimeTypeMetaFile(localPath, mediaType.toString());
			response.close();
		} catch (IOException e) {
			Log.e(TAG, "Error creating MIME type metadata for URI: " + uri, e);
		} catch (Exception e) {
			Log.e(TAG, "An unexpected error occurred while processing URI: " + uri, e);
		}
	}


	/**
	 * Creates MIME type metadata file.
	 *
	 * @param localPath The local path.
	 * @param mimeType  The MIME type.
	 */
	public void createMimeTypeMetaFile(String localPath, String mimeType) {
		String filepath = localPath + ".mime";

		try {
			File file = new File(filepath);
			File parentDir = file.getParentFile();
			if (parentDir != null && !parentDir.exists()) {
				parentDir.mkdirs();
			}
			if (!file.exists()) {
				file.createNewFile();
			}
			BufferedWriter writer = new BufferedWriter(new FileWriter(filepath));
			if (mimeType.contains(";")) {
				mimeType = mimeType.split(";")[0];
			}
			writer.write(mimeType);
			writer.close();
		} catch (Exception e) {
			Log.e(TAG, "Error creating MIME type metadata file for path: " + filepath, e);
		}
	}

	/**
	 * Gets the MIME type from the metadata file.
	 *
	 * @param filepath The file path.
	 * @return The MIME type, or null if not found.
	 */
	public String getMimeTypeFromMeta(String filepath) {
		filepath = filepath + ".mime";
		File file = new File(filepath);
		if (!file.exists()) {
			return null;
		}
		try {
			BufferedReader bufferedReader = new BufferedReader(new FileReader(filepath));
			String type = bufferedReader.readLine();
			bufferedReader.close();
			return type;
		} catch (Exception e) {
			Log.e(TAG, "Error reading MIME type from file: " + filepath, e);
		}
		return null;
	}
}