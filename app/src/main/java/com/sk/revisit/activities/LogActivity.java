package com.sk.revisit.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.sk.revisit.R;
import com.sk.revisit.databinding.ActivityLogBinding;

class LogActivity extends AppCompatActivity {

    ActivityLogBinding binding;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);

        binding = ActivityLogBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


    }
}