package com.sk.revisit.data;

public class UrlLog {
	String urlText;
	long size;
	double p;
	boolean isComplete;
	
	UrlLog(String urlText,long size){
		this.urlText = urlText;
		this.size = size;
		this.isComplete = false;
	}
	
	public void setIsComplete(boolean o){
		this.isComplete = o;
	}
	
	public void setProgress(double p){
		this.p = p;
	}
}