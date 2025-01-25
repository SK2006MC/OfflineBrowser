package com.sk.web1;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;

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
    
    binding.rootPath.setText(sm.getRootPath());
    //binding.keepUptodate
    
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
        	sm.setRootPath(folderPath);
        	sm.done();
          binding.rootPath.setText(sm.getRootPath());
          Toast.makeText(this, sm.getRootPath(), Toast.LENGTH_LONG).show();
        });
  }
  
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    storageManager.onActivityResult(requestCode, resultCode, data);
  }
  
  @Override
  protected void onDestroy() {
  	super.onDestroy();
      sm.done();
  }
}
