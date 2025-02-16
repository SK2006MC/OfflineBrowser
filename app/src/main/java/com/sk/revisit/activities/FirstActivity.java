package com.sk.revisit.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.sk.revisit.databinding.ActivityFirstBinding;
import com.sk.revisit.managers.MySettingsManager;

public class FirstActivity extends AppCompatActivity {

    ActivityFirstBinding binding;
    MySettingsManager settingsManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFirstBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        settingsManager = new MySettingsManager(this);

        binding.pickPath.setOnClickListener((v) -> {

        });
    }
}
