package com.sk.revisit.managers;

import android.content.Context;
import android.content.SharedPreferences;

public class MySettingsManager {
	private static final String PREF_NAME = "RevisitSettings";
	private static final String KEY_ROOT_PATH = "root_path";
	private static final String KEY_IS_FIRST = "isfirst";

	private final SharedPreferences prefs;
	private String reqFileName = "req.txt";

	public MySettingsManager(Context context) {
		prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
	}

	public String getRootStoragePath() {
		return prefs.getString(KEY_ROOT_PATH, null);
	}

	public void setRootStoragePath(String path) {
		prefs.edit().putString(KEY_ROOT_PATH, path).apply();
	}

	public boolean getIsFirst() {
		return prefs.getBoolean(KEY_IS_FIRST, true);
	}

	public void setIsFirst(boolean o) {
		prefs.edit().putBoolean(KEY_IS_FIRST, o).apply();
	}

	public String getReqFileName() {
		return reqFileName;
	}

	public void setReqFileName(String reqFileName) {
		this.reqFileName = reqFileName;
	}
}
