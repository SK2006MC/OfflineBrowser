package com.sk.revisit.activities;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.sk.revisit.databinding.ActivityWebpagesBinding;
import com.sk.revisit.managers.MySettingsManager;
import com.sk.revisit.managers.SQLiteDBM;

import java.util.Set;

public class WebpagesActivity extends AppCompatActivity {

    ActivityWebpagesBinding binding;
    public SQLiteDBM dbm;
    RecyclerView recyclerView;
    MySettingsManager settingsManager;
    Set<String> hosts;
    Button webpagesRefresh ;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        binding = ActivityWebpagesBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        settingsManager = new MySettingsManager(this);
        dbm = new SQLiteDBM(this, settingsManager.getRootStoragePath());

        webpagesRefresh = binding.webpagesRefresh;
        recyclerView = binding.webpagesHosts;
    }

    void loadDownloadedHost() {
        hosts = dbm.selectUniqueHostFromUrls();
        recyclerView.setAdapter(new MyRecyclerAdapter());
    }

    private static class MyRecyclerAdapter extends RecyclerView.Adapter {

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return null;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return 0;
        }
    }
}