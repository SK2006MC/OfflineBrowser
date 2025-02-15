package com.sk.revisit.managers;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

public class UrlDatabaseHelper {

    private static final String TAG = "UrlDatabaseHelper";
    private static final String TABLE_STORED_URLS = "stored_urls";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_URL = "url";
    private static final String COLUMN_HOST = "host";
    private static final String COLUMN_FILE_PATH = "file_path";
    private static final String COLUMN_FILE_SIZE = "file_size";
    private static final String COLUMN_LAST_MODIFIED = "last_modified";
    private static final String COLUMN_ETAG = "etag";

    private SQLiteDatabase db;

    // Assume you have methods to open and close the database
    public void open() {
        // Implementation to open the database
        // Example: db = dbHelper.getWritableDatabase();
        Log.d(TAG, "Database opened");
    }

    public void close() {
        // Implementation to close the database
        // Example: dbHelper.close();
        Log.d(TAG, "Database closed");
    }

    /**
     * Inserts a URL into the database if it doesn't already exist.
     *
     * @param url          The URL to insert.
     * @param filePath     The file path associated with the URL.
     * @param fileSize     The file size.
     * @param lastModified The last modified date.
     * @param etag         The ETag.
     * @return The ID of the inserted or existing URL, or -1 if an error occurred.
     */
    public long insertOrGetUrlId(@NonNull Uri url, String filePath, long fileSize, String lastModified, String etag) {
        long id = -1;
        try {
            open();
            // Check if the URL already exists
            id = getUrlIdIfExists(url);
            if (id != -1) {
                Log.d(TAG, "URL already exists: " + url.toString() + " with ID: " + id);
                return id; // Return the existing ID
            }

            // URL doesn't exist, so insert it
            ContentValues values = new ContentValues();
            values.put(COLUMN_URL, url.toString());
            values.put(COLUMN_HOST, url.getHost());
            values.put(COLUMN_FILE_PATH, filePath);
            values.put(COLUMN_FILE_SIZE, fileSize);
            values.put(COLUMN_LAST_MODIFIED, lastModified);
            values.put(COLUMN_ETAG, etag);

            id = db.insert(TABLE_STORED_URLS, null, values);

            if (id == -1) {
                Log.e(TAG, "Failed to insert URL: " + url.toString());
            } else {
                Log.d(TAG, "Inserted URL: " + url.toString() + " with ID: " + id);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error during database operation", e);
            id = -1;
        } finally {
            close();
        }
        return id;
    }

    /**
     * Checks if a URL already exists in the database and returns its ID.
     *
     * @param url The URL to check.
     * @return The ID of the existing URL, or -1 if it doesn't exist.
     */
    private long getUrlIdIfExists(@NonNull Uri url) {
        long id = -1;
        Cursor cursor = null;
        try {
            String selection = COLUMN_URL + " = ?";
            String[] selectionArgs = {url.toString()};
            cursor = db.query(TABLE_STORED_URLS, new String[]{COLUMN_ID}, selection, selectionArgs, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return id;
    }
}