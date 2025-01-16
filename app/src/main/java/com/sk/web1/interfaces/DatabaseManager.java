package com.sk.web1;

import java.util.ArrayList;
import android.net.Uri;

public interface DatabaseManager {
    void recordDownload(String host, String url, String localPath, long size, String eTag, String lastModified);
    void deleteCacheData();
    void removeUrl(String url);
    void setNonDownloadedUrl(Uri url);
    ArrayList<String> getDownloadedUrls();
    /*
    ArrayList<String> getUrls();
    ArrayList<String,String> getNonDownloadedUrls();
    */
}