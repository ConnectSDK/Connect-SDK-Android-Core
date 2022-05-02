/*
 * Copyright (c) 2020 LG Electronics Inc.
 * SPDX-License-Identifier: LicenseRef-LGE-Proprietary
 */
package com.connectsdk.service.webos.lgcast.common.utils;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;

public class HandlerThreadEx {
    public interface HandlerCallback {
        void handleMessage(Message msg);
    }

    private String mThreadName;
    private int mThreadPriority;

    private HandlerThread mHandlerThread;
    private Handler mHandler;

    public HandlerThreadEx(String name) {
        mThreadName = name;
        mThreadPriority = Process.THREAD_PRIORITY_DEFAULT;
    }

    public void start() {
        mHandlerThread = new HandlerThread(mThreadName, mThreadPriority);
        mHandlerThread.start();

        Looper looper = mHandlerThread.getLooper();
        mHandler = new Handler(looper);
    }

    public void start(HandlerCallback callback) {
        if (callback == null) throw new IllegalArgumentException();
        mHandlerThread = new HandlerThread(mThreadName, mThreadPriority);
        mHandlerThread.start();

        mHandler = new Handler(mHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                callback.handleMessage(msg);
            }
        };
    }

    public void quit() {
        if (mHandler != null) mHandler.getLooper().quit();
        mHandler = null;

        if (mHandlerThread != null) mHandlerThread.quit();
        /*if (mHandlerThread != null) mHandlerThread.interrupt();*/
        mHandlerThread = null;
    }

    public void post(Runnable r) {
        if (mHandler != null) mHandler.post(r);
    }

    public void post(Runnable r, long delayMillis) {
        if (mHandler != null) mHandler.postDelayed(r, delayMillis);
    }

    public boolean sendMessage(Object obj) {
        return sendMessage(0, obj);
    }

    public boolean sendMessage(int what, Object obj) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.obj = obj;
        return (mHandler != null) ? mHandler.sendMessage(msg) : false;
    }

    public Handler getHandler() {
        return mHandler;
    }
}
