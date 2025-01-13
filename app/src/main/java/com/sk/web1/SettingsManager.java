package com.sk.web1;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class SettingsManager {
  public SharedPreferences pref;
  public SharedPreferences.Editor editor;
  public Context main;
  public static final String KEY_KEEPUPTODATE="KEY_KEEPUPTODATE",KEY_PREF="web1",KEY_ROOT_PATH = "KEY_ROOT_PATH",KEY_IS_FIRST="KEY_IS_FIRST";
  SettingsManager(Context c) {
    main = c;
    pref = main.getSharedPreferences(KEY_PREF, Activity.MODE_PRIVATE);
    editor = pref.edit();
  }

  public SharedPreferences getPref() {
    return pref;
  }
  
  public SharedPreferences.Editor getEditor(){
    return editor;
  }
  
  public void setRootPath(String path){
    editor.putString(KEY_ROOT_PATH,path);
  }
  
  public String getRootPath(){
    return pref.getString(KEY_ROOT_PATH,"not set");
  }
  
  public void setIsFirst(Boolean o){
    editor.putBoolean(KEY_IS_FIRST,o);
  }
  public boolean getIsFirst(){
    return pref.getBoolean(KEY_IS_FIRST,true);
  }
  
  public void setKeepUptodate(Boolean o) {
  	editor.putBoolean(KEY_KEEPUPTODATE,o);
  }
  
  public boolean getKeepUptodate(){
    return pref.getBoolean(KEY_KEEPUPTODATE,false);
  }
  
  public void done(){
    editor.apply();
  }
}
