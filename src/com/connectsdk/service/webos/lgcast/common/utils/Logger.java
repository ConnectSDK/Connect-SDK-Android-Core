/*
 * Copyright (c) 2020 LG Electronics Inc.
 * SPDX-License-Identifier: LicenseRef-LGE-Proprietary
 */
package com.connectsdk.service.webos.lgcast.common.utils;

import android.util.Log;
import android.widget.ScrollView;
import android.widget.TextView;

public class Logger {
    private static final String TAG = "LGCAST";
    private static ScrollView mScrollView = null;
    private static TextView mTextView = null;
    private static boolean mShowDebug = true;

    public static void setView(ScrollView scrollView, TextView textView) {
        mScrollView = scrollView;
        mTextView = textView;
    }

    public static void showDebug(boolean showDebug) {
        mShowDebug = showDebug;
    }

    public static void print(String format, Object... args) {
        out(Log.VERBOSE, StringUtil.format(format, args));
    }

    public static void debug(String format, Object... args) {
        if (mShowDebug == true) out(Log.DEBUG, StringUtil.format(format, args));
    }

    public static void error(String format, Object... args) {
        out(Log.ERROR, StringUtil.format(format, args));
    }

    public static void error(Throwable throwable) {
        StackTraceElement[] stackTrace = new Exception().getStackTrace();
        out(Log.ERROR, "Exception: " + throwable.getMessage() + " (" + stackTrace[1].getFileName() + ":" + stackTrace[1].getLineNumber() + ")");
    }

    public static void trace(Throwable throwable) {
        out(Log.ERROR, Log.getStackTraceString(throwable));
    }

    public static void clear() {
        if (mTextView != null) mTextView.post(() -> mTextView.setText(""));
    }

    private static void out(final int type, final String message) {
        StackTraceElement element = new Exception().getStackTrace()[2];
        String fileName = element.getClassName();
        String caller = String.format("[%s:%d] ", fileName.substring(fileName.lastIndexOf(".") + 1), element.getLineNumber());

        if (type == Log.VERBOSE) Log.v(TAG, caller + message);
        else if (type == Log.DEBUG) Log.d(TAG, caller + message);
        else if (type == Log.ERROR) Log.e(TAG, caller + message);

        if (mScrollView != null && mTextView != null) {
            mTextView.post(() -> {
                mTextView.append(message + "\n");
                mScrollView.post(() -> mScrollView.scrollTo(0, mTextView.getHeight()));
            });
        }
    }
}
