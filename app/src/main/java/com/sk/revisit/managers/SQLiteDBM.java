package com.sk.revisit.managers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.os.ExecutorCompat;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Headers;

public class SQLiteDBM {

    private static final String TAG = SQLiteDBM.class.getSimpleName();

    private static final String DATABASE_NAME = "revisit_web_db";
    private static final int DATABASE_VERSION = 2;
    private final String customDatabasePath;

    // Table: Stored URLs
    private static final String TABLE_STORED_URLS = "stored_urls";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_URL = "url";
    private static final String COLUMN_HOST = "host";
    private static final String COLUMN_FILE_PATH = "file_path";
    private static final String COLUMN_FILE_SIZE = "file_size";
    private static final String COLUMN_LAST_MODIFIED = "last_modified";
    private static final String COLUMN_HEADERS = "headers";
    private static final String COLUMN_ETAG = "etag";

    // Table: Download Requests
    private static final String TABLE_DOWNLOAD_REQUESTS = "download_requests";
    private static final String COLUMN_REQUEST_ID = "id";
    private static final String COLUMN_REQUEST_URL = "url";
    private static final String COLUMN_REQUEST_HOST = "host";

    private final DatabaseHelper dbHelper;
    private SQLiteDatabase db;
    ExecutorService executor;

    public SQLiteDBM(Context context, String customDatabasePath) {
        this.customDatabasePath = customDatabasePath;
        dbHelper = new DatabaseHelper(context, customDatabasePath);
        executor = Executors.newFixedThreadPool(2);
    }

    /**
     * Opens the database for writing.
     * It's recommended to open the database only when needed and close it as soon as possible.
     */
    public void open() {
        if (db == null || !db.isOpen()) {
            db = dbHelper.getWritableDatabase();
            Log.d(TAG, "Database opened.");
        }
    }

    /**
     * Closes the database if it's open.
     */
    public void close() {
        if (db != null && db.isOpen()) {
            db.close();
            Log.d(TAG, "Database closed.");
        }
    }


    /**
     * Stores URL details in the database.
     *
     * @param url          The URL.
     * @param filePath     The local file path.
     * @param fileSize     The file size.
     * @param lastModified The last modified timestamp.
     * @param etag         The ETag.
     * @return The row ID of the newly inserted row, or -1 if an error occurred.
     */
    public void insertIntoUrlsIfNotExists(@NonNull Uri url, String filePath, long fileSize, Headers headers) {
        executor.execute(()->{
            long id = -1;
            try {
                open();
                ContentValues values = new ContentValues();
                values.put(COLUMN_URL, url.toString());
                values.put(COLUMN_HOST, url.getHost());
                values.put(COLUMN_FILE_PATH, filePath);
                values.put(COLUMN_FILE_SIZE, fileSize);
                values.put(COLUMN_HEADERS, headers.toString());
//                values.put(COLUMN_LAST_MODIFIED, lastModified);
//                values.put(COLUMN_ETAG, etag);

                id = db.insertWithOnConflict(TABLE_STORED_URLS, null, values,SQLiteDatabase.CONFLICT_IGNORE);

                if (id == -1) {
                    Log.e(TAG, "Failed to insert URL: " + url.toString());
                } else {
                    Log.d(TAG, "Inserted URL: " + url.toString() + " with ID: " + id);
                }
            } finally {
                close();
            }
        });
    }

    /**
     * Retrieves stored URL details from the database.
     *
     * @param url The URL to look up.
     * @return A map containing the file path, last modified timestamp, and ETag, or null if not found.
     */
    public Map<String, String> selectAllFromUrlsWhereUrl(String url) {
        Map<String, String> details = null;
        Cursor cursor = null;
        try {
            open();
            cursor = db.query(TABLE_STORED_URLS,
                    new String[]{COLUMN_FILE_PATH, COLUMN_LAST_MODIFIED, COLUMN_ETAG},
                    COLUMN_URL + "=?",
                    new String[]{url}, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                details = new HashMap<>();
                details.put("filePath", cursor.getString(0));
                details.put("lastModified", cursor.getString(1));
                details.put("etag", cursor.getString(2));
                Log.d(TAG, "Retrieved details for URL: " + url);
            } else {
                Log.d(TAG, "No details found for URL: " + url);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            close();
        }
        return details;
    }

    /**
     * Adds a download request to the database.
     *
     * @param url The URL to download.
     * @return The row ID of the newly inserted row, or -1 if an error occurred.
     */
    public void insertIntoQueIfNotExists(@NonNull Uri url) {
        executor.execute(()->{
            long id = -1;
            try {
                open();
                ContentValues values = new ContentValues();
                values.put(COLUMN_REQUEST_URL, url.toString());
                values.put(COLUMN_REQUEST_HOST, url.getHost());
                id = db.insertWithOnConflict(TABLE_DOWNLOAD_REQUESTS, null, values,SQLiteDatabase.CONFLICT_IGNORE);
                if (id == -1) {
                    Log.e(TAG, "Failed to insert download request for URL: " + url.toString());
                } else {
                    Log.d(TAG, "Inserted download request for URL: " + url.toString() + " with ID: " + id);
                }
            } finally {
                close();
            }
        });
    }

    /**
     * Retrieves a set of downloaded hosts from the database.
     *
     * @return A set of hostnames.
     */
    public Set<String> selectUniqueHostFromUrls() {
        Set<String> hosts = new HashSet<>();
        Cursor cursor = null;
        try {
            open();
            cursor = db.query(true, TABLE_STORED_URLS, new String[]{COLUMN_HOST}, null, null, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    hosts.add(cursor.getString(0));
                } while (cursor.moveToNext());
                Log.d(TAG, "Retrieved " + hosts.size() + " downloaded hosts.");
            } else {
                Log.d(TAG, "No downloaded hosts found.");
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            close();
        }
        return hosts;
    }

    /**
     * Retrieves a list of downloaded URLs from the database.
     *
     * @return A list of URLs.
     */
    public ArrayList<String> selectUrlFromUrls() {
        ArrayList<String> urls = new ArrayList<>();
        Cursor cursor = null;
        try {
            open();
            cursor = db.query(TABLE_STORED_URLS, new String[]{COLUMN_URL}, null, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    urls.add(cursor.getString(0));
                } while (cursor.moveToNext());
                Log.d(TAG, "Retrieved " + urls.size() + " downloaded URLs.");
            } else {
                Log.d(TAG, "No downloaded URLs found.");
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            close();
        }
        return urls;
    }



    /**
     * Database helper class for creating and managing the database.
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {
        private final String customDatabasePath;

        DatabaseHelper(Context context, String customDatabasePath) {
            super(context, customDatabasePath, null, DATABASE_VERSION);
            this.customDatabasePath = customDatabasePath;
        }

        @Override
        public void onCreate(@NonNull SQLiteDatabase db) {
            // Create tables
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_STORED_URLS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_URL + " TEXT UNIQUE, " +
                    COLUMN_HOST + " TEXT, " +
                    COLUMN_FILE_PATH + " TEXT, " +
                    COLUMN_FILE_SIZE + " INTEGER, " +
                    COLUMN_HEADERS + "TEXT" +
//                    COLUMN_LAST_MODIFIED + " TEXT, " +
//                    COLUMN_ETAG + " TEXT" +
                    ")");

            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_DOWNLOAD_REQUESTS + " (" +
                    COLUMN_REQUEST_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_REQUEST_URL + " TEXT UNIQUE, " +
                    COLUMN_REQUEST_HOST + " TEXT" +
                    ")");

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}