package com.sk.web1;

import android.app.Activity;
import android.Manifest;
import android.content.pm.PackageManager;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.annotation.NonNull;

public class PermissionManagerV1 {

    private static final int REQUEST_STORAGE_PERMISSION = 100; // Constant defined in FirstActivity

    private static final String TAG = "PermissionManager";
    private Activity activity;

    public interface OnPermissionGrantedListener {
        void onPermissionGranted();
    }

    public PermissionManagerV1(Activity activity) {
        this.activity = activity;
    }

    public boolean hasStoragePermission() {
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestStoragePermission(OnPermissionGrantedListener listener) {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
        permissionListener = listener;
    }

    private OnPermissionGrantedListener permissionListener;

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (permissionListener != null) {
                    permissionListener.onPermissionGranted();
                }
            } else {
                // Permission denied, handle it
                Toast.makeText(activity, "Storage permission is necessary for this feature. Please grant access in Settings.", Toast.LENGTH_SHORT).show();
            }
            permissionListener = null;
        }
    }
}
