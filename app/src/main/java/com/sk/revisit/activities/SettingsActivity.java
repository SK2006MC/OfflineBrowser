package com.sk.revisit.activities;

import android.os.Bundle;
import android.os.PersistableBundle;

import androidx.appcompat.app.AppCompatActivity;

import com.sk.revisit.databinding.ActivitySettingsBinding;
import com.sk.revisit.managers.MySettingsManager;

public class SettingsActivity extends AppCompatActivity {

    ActivitySettingsBinding binding;
    MySettingsManager settingsManager;

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        settingsManager=new MySettingsManager(this);
        binding.rootPathTextView.setText(settingsManager.getRootStoragePath());
    }

}