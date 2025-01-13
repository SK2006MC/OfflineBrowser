package com.sk.web1;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class NetworkUtils {

    private static final String TAG = "NetworkUtils";

    public static boolean isInternetAvailable(Context context) {
        if (context == null) {
            Log.w(TAG, "Context is null. Cannot check internet connectivity.");
            return false;
        }

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager == null) {
            Log.w(TAG, "ConnectivityManager is null. Cannot check internet connectivity.");
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Network network = connectivityManager.getActiveNetwork();
            if (network == null) {
                return false;
            }

            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
            if (networkCapabilities == null) {
                return false;
            }

            if (!(networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED))) {
                return false;
            }
        } else {
            // For API levels below 23, consider using a different approach like a ping to a known host
            // This approach might be less reliable than using NetworkCapabilities
            // but it can be an alternative if migrating to getNetworkCapabilities is not feasible.
            
            try {
                InetAddress ipAddr = InetAddress.getByName("8.8.8.8"); // Google Public DNS
                return !ipAddr.equals("");
            } catch (UnknownHostException e) {
                Log.e(TAG, "UnknownHostException: " + e.getMessage());
                return false;
            }
        }

        // Perform actual internet check in a background thread
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Boolean> future = executor.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                try {
                    InetAddress ipAddr = InetAddress.getByName("8.8.8.8"); // Google Public DNS
                    return !ipAddr.equals("");
                } catch (UnknownHostException e) {
                    Log.e(TAG, "UnknownHostException: " + e.getMessage());
                    return false;
                }
            }
        });

        try {
            return future.get(3, TimeUnit.SECONDS); // Timeout of 3 seconds
        } catch (InterruptedException e) {
            Log.e(TAG, "InterruptedException: " + e.getMessage());
            Thread.currentThread().interrupt(); // Restore interrupt status
            return false;
        } catch (ExecutionException e) {
            Log.e(TAG, "ExecutionException: " + e.getMessage());
            return false;
        } catch (TimeoutException e) {
            Log.e(TAG, "TimeoutException: " + e.getMessage());
            return false;
        } finally {
            executor.shutdownNow();
        }
    }
}
