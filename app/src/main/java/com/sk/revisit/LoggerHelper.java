package com.sk.revisit;

import android.util.Base64;

import com.sk.revisit.managers.MyLogManager;

class LoggerHelper {
    private final MyLogManager myLogManager;
    private final MyLogManager req;
    private final MyLogManager resp;

    public LoggerHelper(android.content.Context context, String rootPath, java.util.concurrent.ExecutorService loggingExecutor) {
        this.myLogManager = new MyLogManager(context, rootPath + "/log.txt");
        this.req = new MyLogManager(context, rootPath + "/req.txt");
        this.resp = new MyLogManager(context, rootPath + "/saved.base64");
    }

    public void log(String msg) {
        myLogManager.log(msg);
    }

    public void saveReq(String msg) {
        req.log(msg);
    }

    public void saveResp(String msg) {
        resp.log(Base64.encodeToString(msg.getBytes(), Base64.NO_WRAP) + "\n----\n");
    }
}