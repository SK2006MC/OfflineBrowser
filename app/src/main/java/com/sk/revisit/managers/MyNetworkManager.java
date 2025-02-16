package com.sk.revisit.managers;

import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class MyNetworkManager {
    final String TAG = "MyNetworkManager";

    public Map<String, String> getHeadRequestHeaders(String urlString) {
        HttpURLConnection connection = null;
        Map<String, String> headers = new HashMap<>();
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.connect();

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                headers = getConnectionHeaders(connection);
            }

        } catch (IOException e) {
            Log.d(TAG, e.toString());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return headers;
    }

    private Map<String, String> getConnectionHeaders(HttpURLConnection connection) {
        Map<String, String> headers = new HashMap<>();
        Map<String, java.util.List<String>> headerFields = connection.getHeaderFields();
        if (headerFields != null) {
            for (Map.Entry<String, java.util.List<String>> entry : headerFields.entrySet()) {
                String key = entry.getKey();
                if (key != null) {
                    headers.put(key, entry.getValue().get(0)); // Assuming single value per header for simplicity
                }
            }
        }
        return headers;
    }


}
