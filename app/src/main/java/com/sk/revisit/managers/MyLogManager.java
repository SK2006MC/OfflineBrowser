package com.sk.revisit.managers;

import android.content.Context;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MyLogManager {

    final File file;
    final String filePath;
    final Context c;
    FileOutputStream fos;

    public MyLogManager(Context c, String filePath) {
        this.filePath = filePath;
        this.c = c;
        this.file = new File(this.filePath);
        try {
            if(!file.exists()){
                file.createNewFile();
            }
            this.fos=new FileOutputStream(this.file,true);
        } catch (Exception e) {
            alert(e.toString());
        }
    }

    public void log(String msg) {
        try {
            fos.write(msg.getBytes());
        } catch (IOException e) {
            alert(e.toString());
        }
    }

    public void log(byte[] b) {
        try {
            fos.write(b);
        } catch (IOException e) {
            alert(e.toString());
        }
    }

    public void alert(String msg) {
        Toast.makeText(c, msg, Toast.LENGTH_LONG).show();
    }
}