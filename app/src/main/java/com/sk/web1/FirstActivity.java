package com.sk.web1;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;

public class FirstActivity extends Activity {

  private TextView rootPath;
  public SettingsManager sm;
  private MyStorageManager storageManager;
  private PermissionManagerV1 permissionManager;

  //private static final int REQUEST_STORAGE_PERMISSION = 100;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.first);

    rootPath = findViewById(R.id.rootPath);
    sm = new SettingsManager(this);

    storageManager = new MyStorageManager(this);
    permissionManager = new PermissionManagerV1(this);

    if (!permissionManager.hasStoragePermission()) {
      permissionManager.requestStoragePermission(
          () -> {
            Toast.makeText(this, "Storage permission granted!", Toast.LENGTH_SHORT).show();
          });
    }
  }

  @Override
  public void onRequestPermissionsResult(
      int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    permissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
  }

  public void start(View v) {
    try {
      sm.setRootPath(rootPath.getText().toString());
      sm.setIsFirst(false);
      sm.done();
      finish();
    } catch (Exception err) {
      Toast.makeText(this, err.toString(), Toast.LENGTH_LONG).show();
    }
  }

  public void pickRootPath(View v) {
    storageManager.pickRootPath(
        folderPath -> {
          rootPath.setText(folderPath);
          sm.setRootPath(folderPath);
          Toast.makeText(this, folderPath, Toast.LENGTH_LONG).show();
        });
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    storageManager.onActivityResult(requestCode, resultCode, data);
  }
}
