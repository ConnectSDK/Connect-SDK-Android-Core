/*
 * Copyright (c) 2020 LG Electronics Inc.
 * SPDX-License-Identifier: LicenseRef-LGE-Proprietary
 */
package com.connectsdk.service.webos.lgcast.common.utils;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

public class ThreadUtil {
    public static Thread runInBackground(Runnable r) {
        if (r == null) return null;
        Thread thread = new Thread(r, "ThreadUtil");
        thread.start();
        return thread;
    }

    public static void runOnMainLooper(Runnable r) {
        if (r == null) return;
        new Handler(Looper.getMainLooper()).post(r);
    }

    public static void runOnMainLooper(Runnable r, long delayMillis) {
        if (r == null) return;
        new Handler(Looper.getMainLooper()).postDelayed(r, delayMillis);
    }

    public static Handler createHandler(String name) {
        HandlerThread handlerThread = new HandlerThread(name);
        handlerThread.start();
        return new Handler(handlerThread.getLooper());
    }

    public static void destroyHandler(Handler handler) {
        if (handler != null) handler.getLooper().quit();
    }

    public static void sleep(long millis) {
        try {
            if (millis > 0) Thread.sleep(millis);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getName() {
        return Thread.currentThread().getName();
    }

    public static long getId() {
        return Thread.currentThread().getId();
    }

    public static boolean isMainThread() {
        return ThreadUtil.getName().equals("main");
    }
}
