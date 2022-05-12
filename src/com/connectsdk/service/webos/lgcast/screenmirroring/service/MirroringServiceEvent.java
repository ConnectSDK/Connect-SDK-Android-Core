/*
 * Copyright (c) 2020 LG Electronics Inc.
 * SPDX-License-Identifier: LicenseRef-LGE-Proprietary
 */
package com.connectsdk.service.webos.lgcast.screenmirroring.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;

public class MirroringServiceEvent {
    public interface ScreenOnOffListener {
        void onScreenOnOffChanged(boolean turnOn);
    }

    public interface AccessibilitySettingListener {
        void onAccessibilitySettingChanged(boolean uibcEnabled);
    }

    private Context mContext;
    private BroadcastReceiver mScreenOnOffReceiver;
    private ContentObserver mAccessibilitySettingObserver;

    public MirroringServiceEvent(Context context) {
        if (context == null) throw new IllegalStateException("Invalid context");
        mContext = context;
    }

    public void startScreenOnOffReceiver(ScreenOnOffListener listener) {
        mScreenOnOffReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context contex, Intent intent) {
                final String ScreenOn = "android.intent.action.SCREEN_ON";
                final String ScreenOff = "android.intent.action.SCREEN_OFF";

                boolean turnOn = false;
                if (intent.getAction().equals(ScreenOn) == true) turnOn = true;
                else if (intent.getAction().equals(ScreenOff) == true) turnOn = false;
                if (listener != null) listener.onScreenOnOffChanged(turnOn);
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        mContext.registerReceiver(mScreenOnOffReceiver, intentFilter);
    }

    public void startAccessibilitySettingObserver(AccessibilitySettingListener listener) {
        mAccessibilitySettingObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
            @Override
            public void onChange(boolean selfChange) {
                boolean uibcEnabled = MirroringServiceFunc.isUibcEnabled(mContext);
                if (listener != null) listener.onAccessibilitySettingChanged(uibcEnabled);
            }
        };

        Uri uri = Settings.Secure.getUriFor(Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        mContext.getContentResolver().registerContentObserver(uri, false, mAccessibilitySettingObserver);
    }

    public void quit() {
        if (mScreenOnOffReceiver != null) mContext.unregisterReceiver(mScreenOnOffReceiver);
        mScreenOnOffReceiver = null;

        if (mAccessibilitySettingObserver != null) mContext.getContentResolver().unregisterContentObserver(mAccessibilitySettingObserver);
        mAccessibilitySettingObserver = null;
    }
}
