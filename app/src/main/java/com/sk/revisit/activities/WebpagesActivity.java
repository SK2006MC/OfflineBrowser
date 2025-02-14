package com.sk.revisit.activities;

import android.os.Bundle;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.sk.revisit.R;
import com.sk.revisit.data.Host;
import com.sk.revisit.managers.MySettingsManager;
import com.sk.revisit.managers.SQLiteDBM;

import java.util.Set;

public class WebpagesActivity extends AppCompatActivity {

    public SQLiteDBM dbm;
    RecyclerView recyclerView;
    MySettingsManager settingsManager;

    Set<String> hosts;
    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_webpages);
        settingsManager = new MySettingsManager(this);
        dbm = new SQLiteDBM(this, settingsManager.getRootStoragePath());
        recyclerView=findViewById(R.id.recyclerView);
    }

    void loadDownloadedHost(){
        hosts=dbm.getDownloadedHosts();
        recyclerView.setAdapter(new RecyclerView.Adapter() {

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
        });
    }
}