package com.connectsdk.service.webos.lgcast.common.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;

public class ProgressUtil {
    public interface DismissListener {
        void onDismiss();
    }

    private ProgressDialog mDialog;

    private ProgressUtil(Context context) {
        mDialog = new ProgressDialog(context);
        mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mDialog.setCancelable(false);
        mDialog.setCanceledOnTouchOutside(false);
        if (context instanceof Activity) mDialog.setOwnerActivity((Activity) context);
    }

    public static ProgressUtil create(Context context, String message, boolean cancelable) {
        ProgressUtil progress = new ProgressUtil(context);
        progress.setMessage(message);
        progress.setCancelable(cancelable);
        return progress;
    }

    public void setMessage(String message) {
        mDialog.setMessage(message);
    }

    public void setCancelable(boolean cancelable) {
        mDialog.setCancelable(cancelable);
    }

    public boolean isShowing() {
        return mDialog.isShowing();
    }

    public void show() {
        showProgress();
    }

    public void show(long timeout) {
        showProgress();
        TimerUtil.schedule(() -> dismissProgress(), timeout);
    }

    public void show(String message) {
        setMessage(message);
        showProgress();
    }

    public void show(String message, long timeout) {
        setMessage(message);
        showProgress();
        TimerUtil.schedule(() -> dismissProgress(), timeout);
    }

    public void show(String message, long timeout, DismissListener listener) {
        setMessage(message);
        showProgress();

        TimerUtil.schedule(() -> {
            dismissProgress();
            if (listener != null) listener.onDismiss();
        }, timeout);
    }

    public void dismiss() {
        dismissProgress();
    }

    private void showProgress() {
        if (mDialog == null) return;
        if (ThreadUtil.isMainThread() == true) mDialog.show();
        else ThreadUtil.runOnMainLooper(() -> mDialog.show());
    }

    private void dismissProgress() {
        if (mDialog == null) return;
        if (ThreadUtil.isMainThread() == true) mDialog.dismiss();
        else ThreadUtil.runOnMainLooper(() -> mDialog.dismiss());
    }
}
