package com.sk.web1;

public interface DatabaseManager {
    void recordDownload(String host, String url, String localPath, long size, String eTag, String lastModified);
    void deleteCacheData();
}

