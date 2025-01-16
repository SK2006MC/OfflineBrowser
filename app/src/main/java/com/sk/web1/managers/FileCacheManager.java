package com.sk.web1;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import okhttp3.CacheControl;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FileCacheManager implements CacheManager {

  private final String rootPath;
  private final Downloader downloader;
  private final MimeTypeResolver mimeTypeResolver;
  private final SQLiteDatabaseManager databaseManager;
  private final OkHttpClient client;
  SettingsManager sm;
  NetworkUtils network;
  Context main;

  public FileCacheManager(
      Context main,
      SettingsManager sm,
      String rootPath,
      Downloader downloader,
      MimeTypeResolver mimeTypeResolver,
      SQLiteDatabaseManager databaseManager,
      OkHttpClient client,
      NetworkUtils network) {
    this.main = main;
    this.rootPath = rootPath;
    this.downloader = downloader;
    this.mimeTypeResolver = mimeTypeResolver;
    this.databaseManager = databaseManager;
    this.client = client;
    this.sm = sm;
    this.network = network;
  }

  @Override
  public WebResourceResponse getCachedResponse(WebResourceRequest request) {
    Uri uri = request.getUrl();
    String localPath = buildLocalPath(uri);
    File localFile = new File(localPath);

    if (localFile.exists()) {
      if (sm.getKeepUptodate() && network.isInternetAvailable(main)) {
        try {¹
          Headers headers = getHeadersFromDb(uri);
          Request okHttpRequest =
              new Request.Builder()
                  .url(uri.toString())
                  .cacheControl(CacheControl.FORCE_NETWORK)
                  .headers(headers)
                  .build();
          Response response = client.newCall(okHttpRequest).execute();
          if (response.code() == 304) {
            return loadFromLocal(localFile);
          } else if (response.isSuccessful()) {
            downloadFile(uri, localFile, response.headers());
            return loadFromLocal(localFile);
          }
        } catch (IOException e) {
          Log.d("fileCacheManager", e.toString());
          // Handle exception, proceed with regular download
        }
      }
      return loadFromLocal(localFile);
    } else if(network.isInternetAvailable(main)) {
      downloadFile(uri, localFile, null);
      return loadFromLocal(localFile);
    }else{
      databaseManager.saveUrlToDownload(uri);
      return null;
    }
    return null;
  }

  private Headers getHeadersFromDb(Uri uri) {
    // get from db or return empty headers
    return new Headers.Builder().build();
  }

  private String buildLocalPath(Uri uri) {
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

  private void downloadFile(Uri uri, File localFile, Headers headers) {
    downloader.download(
        uri.toString(),
        localFile,
        new Downloader.DownloadCallback() {
          @Override
          public void onStart() {
            // Handle download start (e.g., show progress bar)
          }

          @Override
          public void onProgress(int progress, long total) {
            // Update progress bar
          }

          @Override
          public void onSuccess() {
            if (headers != null) {
              databaseManager.recordDownload(
                  uri.getHost(),
                  uri.toString(),
                  localFile.getAbsolutePath(),
                  localFile.length(),
                  headers.get("ETag"),
                  headers.get("Last-Modified"));
            } else {
              databaseManager.recordDownload(
                  uri.getHost(),
                  uri.toString(),
                  localFile.getAbsolutePath(),
                  localFile.length(),
                  null,
                  null);
            }
          }

          @Override
          public void onFailure(String error) {
          databaseManager.saveUrlToDownload(uri);
            // Handle download failure (e.g., show error message)
          }
        });
  }

  private WebResourceResponse loadFromLocal(File localFile) {
    if (!localFile.exists()) {
      return null;
    }
    String mimeType = mimeTypeResolver.getMimeType(localFile.getPath());
    try {
      FileInputStream fis = new FileInputStream(localFile);
      return new WebResourceResponse(mimeType, "UTF-8", fis);
    } catch (IOException e) {
      // Handle exception (e.g., log error)
      return null;
    }
  }

  @Override
  public void clearCache() {
    databaseManager.deleteCacheData();
    File cacheDir = new File(rootPath);
    if (cacheDir.exists() && cacheDir.isDirectory()) {
      File[] files = cacheDir.listFiles();
      if (files != null) {
        for (File file : files) {
          deleteRecursive(file);
        }
      }
    }
  }

  private void deleteRecursive(File fileOrDirectory) {
    if (fileOrDirectory.isDirectory()) {
      for (File child : fileOrDirectory.listFiles()) {
        deleteRecursive(child);
      }
    }
    fileOrDirectory.delete();
  }
}
