package com.sk.web1;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.sk.web1.databinding.ActivitySettingsBinding;

public class SettingsActivity extends Activity {
  ActivitySettingsBinding binding;
  MyStorageManager storageManager;
  SettingsManager sm;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivitySettingsBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    storageManager = new MyStorageManager(this);
    sm = new SettingsManager(this);
    
    binding.keepUptodate.setOnCheckedChangeListener((p,q)->{
      sm.setKeepUptodate(q);
    });
    
    binding.save.setOnClickListener((p)->{
      finish();
    });
  }

  public void pickRootPath(View v) {
    storageManager.pickRootPath(
        folderPath -> {
          binding.rootPath.setText(folderPath);
          sm.setRootPath(folderPath);
          Toast.makeText(this, folderPath, Toast.LENGTH_LONG).show();
        });
  }
  
  @Override
  protected void onDestroy() {
  	super.onDestroy();
      sm.done();
  }
}
