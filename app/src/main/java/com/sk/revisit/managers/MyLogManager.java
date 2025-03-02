package com.sk.revisit.managers;

import android.content.Context;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MyLogManager {
	private final File file;
	private BufferedWriter writer;

	public MyLogManager(Context context, String filePath) {
		this.file = new File(filePath);
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			this.writer = new BufferedWriter(new FileWriter(file, true));
		} catch (IOException e) {
			System.err.println("Error initializing MyLogManager: " + e.getMessage());
		}
	}

	public synchronized void log(String msg) {
		try {
			writer.write(msg);
			writer.newLine();
			writer.flush();
		} catch (IOException e) {
			System.err.println("Error writing log: " + e.getMessage());
		}
	}

	public synchronized void log(byte[] b) {
		try {
			writer.write(new String(b));
			writer.newLine();
			writer.flush();
		} catch (IOException e) {
			System.err.println("Error writing log bytes: " + e.getMessage());
		}
	}

	// Call this when shutting down the application
	public synchronized void close() {
		try {
			writer.close();
		} catch (IOException e) {
			System.err.println("Error closing log writer: " + e.getMessage());
		}
	}
}