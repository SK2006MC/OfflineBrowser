package com.sk.revisit.activities;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.sk.revisit.R;
import com.sk.revisit.databinding.ActivityDownloadBinding;

public class DownloadActivity extends AppCompatActivity {

    ActivityDownloadBinding binding;
    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        binding=ActivityDownloadBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.buttonDownload.setOnClickListener(v -> {
            //TODO
        });
    }
}