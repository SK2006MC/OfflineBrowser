package com.sk.web1;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import java.io.File;

public class MyStorageManager {

    private static final int REQUEST_PICK_FOLDER = 101;
    private Activity activity;

    public interface OnFolderSelectedListener {
        void onFolderSelected(String folderPath);
    }

    public MyStorageManager(Activity activity) {
        this.activity = activity;
    }

    public void pickRootPath(OnFolderSelectedListener listener) {
        if (new PermissionManagerV1(activity).hasStoragePermission()) {
            // Permission already granted, open folder picker
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            activity.startActivityForResult(intent, REQUEST_PICK_FOLDER);
            folderSelectedListener = listener;
        } else {
            // Request permission if not granted yet
            new PermissionManagerV1(activity).requestStoragePermission(() -> pickRootPath(listener));
        }
    }

    private OnFolderSelectedListener folderSelectedListener;

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PICK_FOLDER && resultCode == Activity.RESULT_OK) {
            // Get the selected folder path
            Uri uri = data.getData();
            String path = uri.getPath();
            String root = path.split(":")[1];
            String folderPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + root;

            if (folderSelectedListener != null) {
                folderSelectedListener.onFolderSelected(folderPath);
            }
        }
    }
}
