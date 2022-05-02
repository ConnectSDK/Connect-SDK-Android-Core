/*
 * Copyright (c) 2020 LG Electronics Inc.
 * SPDX-License-Identifier: LicenseRef-LGE-Proprietary
 */
package com.connectsdk.service.webos.lgcast.common.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import java.util.HashMap;

public class LocalBroadcastEx {
    public interface BroadcastListener {
        void onReceive(Intent intent);
    }

    private HashMap<String, BroadcastReceiver> mReceiverMap = new HashMap<>();

    public void register(Context context, String action, BroadcastListener listener) {
        if (context == null || action == null) return;

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (listener != null) listener.onReceive(intent);
            }
        };

        LocalBroadcastManager.getInstance(context).registerReceiver(receiver, new IntentFilter(action));
        unregister(context, action);
        mReceiverMap.put(action, receiver);
    }

    public void registerOnce(Context context, String action, BroadcastListener listener) {
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (listener != null) listener.onReceive(intent);
                unregister(context, action);
            }
        };

        if (context == null || action == null) return;
        if (registered(action) == true) unregister(context, action);
        LocalBroadcastManager.getInstance(context).registerReceiver(receiver, new IntentFilter(action));
        mReceiverMap.put(action, receiver);
    }

    public boolean registered(String action) {
        return mReceiverMap.get(action) != null;
    }

    public void unregister(Context context, String action) {
        if (context == null || action == null) return;
        BroadcastReceiver receiver = mReceiverMap.remove(action);
        if (receiver != null) LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver);
    }

    public void unregisterAll(Context context) {
        if (context == null) return;

        for (BroadcastReceiver receiver : mReceiverMap.values())
            LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver);
        mReceiverMap.clear();
    }
}
