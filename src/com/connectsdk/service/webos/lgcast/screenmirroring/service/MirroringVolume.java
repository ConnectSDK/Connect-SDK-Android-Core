/*
 * Copyright (c) 2020 LG Electronics Inc.
 * SPDX-License-Identifier: LicenseRef-LGE-Proprietary
 */
package com.connectsdk.service.webos.lgcast.screenmirroring.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import com.connectsdk.service.webos.lgcast.common.utils.Logger;
import java.util.concurrent.atomic.AtomicBoolean;
import java.lang.IllegalArgumentException;

public class MirroringVolume {
    private final String VOLUME_CHANGED_ACTION = "android.media.VOLUME_CHANGED_ACTION";

    private Context mContext;
    private AudioManager mAudioManager;

    private AtomicBoolean mStarted;
    private int mPrevVolume;
    private BroadcastReceiver mBroadcastReceiver;

    public MirroringVolume(Context context) {
        mContext = context;
        if (context == null) throw new IllegalArgumentException("Invalid context");

        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        if (mAudioManager == null) throw new IllegalArgumentException("Invalid AudioManager");

        mStarted = new AtomicBoolean();
        mStarted.set(false);
    }

    public void startMute() {
        Logger.print("startMute");
        mPrevVolume = getStreamVolume();
        if (getStreamVolume() > 0) setStreamVolume(0);

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(mStarted.get() == false) return;

                if (getStreamVolume() > 0) {
                    Logger.debug("set mute");
                    setStreamVolume(0);
                }
            }
        };

        mContext.registerReceiver(mBroadcastReceiver, new IntentFilter(VOLUME_CHANGED_ACTION));
        mStarted.set(true);
    }

    public void stopMute() {
        Logger.print("stopMute");
        mStarted.set(false);
        setStreamVolume(mPrevVolume);
        try {
            if (mBroadcastReceiver != null) mContext.unregisterReceiver(mBroadcastReceiver);
        }
        catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private int getStreamVolume() {
        return mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    private void setStreamVolume(int volume) {
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, AudioManager.FLAG_SHOW_UI);
    }
}
