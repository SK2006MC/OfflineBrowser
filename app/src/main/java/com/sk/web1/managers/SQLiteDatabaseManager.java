package com.sk.web1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

import okhttp3.Headers;

public class SQLiteDatabaseManager implements DatabaseManager {

    private static final String DATABASE_NAME = "webview_cache.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_URLS = "urls";
    private static final String KEY_URL = "url";
    private static final String KEY_LOCAL_PATH = "localPath";
    private static final String KEY_SIZE = "size";
    private static final String KEY_HOST = "host";
    private static final String KEY_ETAG = "etag";
    private static final String KEY_LAST_MODIFIED = "last_modified";

    private final DatabaseHelper dbHelper;
    private SQLiteDatabase db;
    private final String customDbPath;

    public SQLiteDatabaseManager(Context context, String customDbPath) {
        this.customDbPath = customDbPath;
        dbHelper = new DatabaseHelper(context, customDbPath);
        db = dbHelper.getWritableDatabase();
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        private final String customDbPath;

        DatabaseHelper(Context context, String customDbPath) {
            super(context, new File(customDbPath,DATABASE_NAME).getAbsolutePath(), null, DATABASE_VERSION);
            this.customDbPath = customDbPath;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(
                    String.format(
                            "CREATE TABLE IF NOT EXISTS %s( %s TEXT PRIMARY KEY, %s TEXT, %s TEXT, %s LONG,  %s TEXT,  %s TEXT);",
                            TABLE_URLS, KEY_URL, KEY_LOCAL_PATH, KEY_HOST, KEY_SIZE, KEY_ETAG, KEY_LAST_MODIFIED));
            db.execSQL("CREATE INDEX url_index ON " + TABLE_URLS + "(" + KEY_URL + ")");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // Handle database schema upgrades here
            // Example:
            // if (oldVersion < 2) {
            //     db.execSQL("ALTER TABLE " + TABLE_URLS + " ADD COLUMN new_column TEXT");
            // }
        }
    }

    @Override
    public void recordDownload(
            String host, String url, String localPath, long size, String eTag, String lastModified) {
        ContentValues values = new ContentValues();
        values.put(KEY_URL, url);
        values.put(KEY_LOCAL_PATH, localPath);
        values.put(KEY_SIZE, size);
        values.put(KEY_HOST, host);
        values.put(KEY_ETAG, eTag);
        values.put(KEY_LAST_MODIFIED, lastModified);
        try {
            db.insertWithOnConflict(TABLE_URLS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        } catch (Exception e) {
            Log.e("DatabaseError", e.toString());
        }
    }

    
    public Headers getHeadersForUrl(String url) {
        Headers.Builder builder = new Headers.Builder();
        try (Cursor cursor = db.query(
                TABLE_URLS,
                new String[]{KEY_ETAG, KEY_LAST_MODIFIED},
                KEY_URL + "=?",
                new String[]{url},
                null,
                null,
                null)) {
            if (cursor != null && cursor.moveToFirst()) {
                String etag = cursor.getString(cursor.getColumnIndexOrThrow(KEY_ETAG));
                String lastModified = cursor.getString(cursor.getColumnIndexOrThrow(KEY_LAST_MODIFIED));
                if (etag != null) {
                    builder.add("If-None-Match", etag);
                }
                if (lastModified != null) {
                    builder.add("If-Modified-Since", lastModified);
                }
            }
        }
        return builder.build();
    }


    @Override
    public void deleteCacheData() {
        try {
            db.delete(TABLE_URLS, null, null);
        } catch (Exception e) {
            Log.e("DatabaseError", e.toString());
        }
    }
    
    @Override
    public ArrayList<String> getDownloadedUrls() {
        ArrayList<String> urls = new ArrayList<>();
        try (Cursor cursor = db.query(TABLE_URLS, new String[]{KEY_URL}, null, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    urls.add(cursor.getString(cursor.getColumnIndexOrThrow(KEY_URL)));
                } while (cursor.moveToNext());
            }
        }
        return urls;
    }

    @Override
    public void setNonDownloadedUrl(Uri uri) {
        ContentValues values = new ContentValues();
        values.put(KEY_HOST,uri.getHost());
        values.put(KEY_URL,uri.toString());
        try {
            db.insertWithOnConflict("download_queue", null, values, SQLiteDatabase.CONFLICT_IGNORE);
        } catch (Exception e) {
            Log.e("DatabaseError", e.toString());
        }
    }

    @Override
    public void removeUrl(String url) {
        try{
            db.delete("download_queue",KEY_URL+"=?",new String[]{url});
        }catch (Exception e){
            Log.e("DatabaseError",e.toString());
        }
    }
}
