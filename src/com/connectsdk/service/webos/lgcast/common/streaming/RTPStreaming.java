/*
 * Copyright (c) 2020 LG Electronics Inc.
 * SPDX-License-Identifier: LicenseRef-LGE-Proprietary
 */
package com.connectsdk.service.webos.lgcast.common.streaming;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.connectsdk.service.webos.lgcast.common.transfer.RTPStreamer;
import com.connectsdk.service.webos.lgcast.common.transfer.RTPStreamerConfig;
import com.connectsdk.service.webos.lgcast.common.transfer.RTPStreamerData;
import com.connectsdk.service.webos.lgcast.common.utils.Logger;
import com.connectsdk.service.webos.lgcast.common.utils.ThreadUtil;
import com.lge.lib.mediacapture.iface.MediaData;
import java.util.concurrent.CountDownLatch;

public class RTPStreaming {
    private RTPStreamerConfig.VideoConfig mVideoConfig;
    private RTPStreamerConfig.AudioConfig mAudioConfig;
    private RTPStreamerConfig.SecurityConfig mSecurityConfig;
    private RTPStreamer mRTPStreamer;

    private Handler mVideoHandler;
    private Handler mAudioHandler;

    public void setStreamingConfig(RTPStreamerConfig.VideoConfig videoConfig, RTPStreamerConfig.AudioConfig audioConfig, RTPStreamerConfig.SecurityConfig securityConfig) {
        mVideoConfig = videoConfig;
        mAudioConfig = audioConfig;
        mSecurityConfig = securityConfig;
    }

    public void open(Context context, long ssrc, String ip, int videoPort, int audioPort) throws Exception {
        Logger.print("open (ssrc=%d, ip=%s, videoPort=%d, audioPort=%d)", ssrc, ip, videoPort, audioPort);
        if (mVideoConfig == null || mAudioConfig == null || mSecurityConfig == null) throw new Exception("Invalid configuration");
        if (context == null) throw new Exception("Invalid arguments");

        mRTPStreamer = RTPStreamer.getInstance();
        mRTPStreamer.initialize(context);
        mRTPStreamer.setStreamMode(RTPStreamerData.ProtocolType.RTP, RTPStreamerData.MediaType.AV, ssrc);
        mRTPStreamer.setVideoConfig(mVideoConfig);
        mRTPStreamer.setVideoDstToUri(ip, videoPort);
        mRTPStreamer.setAudioConfig(mAudioConfig);
        mRTPStreamer.setAudioDstToUri(ip, audioPort);
        mRTPStreamer.setSecurityConfig(mSecurityConfig);
        mRTPStreamer.start();

        CountDownLatch counter = new CountDownLatch(2);
        ThreadUtil.runInBackground(() -> executeVideoCast(counter));
        ThreadUtil.runInBackground(() -> executeAudioCast(counter));
        counter.await();
    }

    public void close() {
        Logger.print("close");
        if (mRTPStreamer != null) mRTPStreamer.stop();
        if (mRTPStreamer != null) mRTPStreamer.terminate();
        mRTPStreamer = null;

        if (mVideoHandler != null) mVideoHandler.getLooper().quit();
        mVideoHandler = null;

        if (mAudioHandler != null) mAudioHandler.getLooper().quit();
        mAudioHandler = null;
    }

    public Handler getVideoStreamHandler() {
        return mVideoHandler;
    }

    public Handler getAudioStreamHandler() {
        return mAudioHandler;
    }

    public void updateMasterKey() {
        if (mRTPStreamer != null) mRTPStreamer.updateMasterKey();
    }

    private void executeVideoCast(CountDownLatch counter) {
        Logger.print("executeVideoCast");
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_DISPLAY);

        Looper.prepare();
        mVideoHandler = new Handler(Looper.myLooper()) {
            @Override
            public void handleMessage(Message msg) {
                MediaData mediaData = (MediaData) msg.obj;
                if (mediaData != null) mRTPStreamer.sendData(RTPStreamerData.MediaType.VIDEO, mediaData.pts * 11111, mediaData.data);
            }
        };

        counter.countDown();
        Logger.debug("countDown done");

        Looper.loop();
        Logger.print("executeVideoCast done");
    }

    private void executeAudioCast(CountDownLatch counter) {
        Logger.print("executeAudioCast");
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);

        Looper.prepare();
        mAudioHandler = new Handler(Looper.myLooper()) {
            @Override
            public void handleMessage(Message msg) {
                MediaData mediaData = (MediaData) msg.obj;
                if (mediaData != null) mRTPStreamer.sendData(RTPStreamerData.MediaType.AUDIO, 0, mediaData.data);
            }
        };

        counter.countDown();
        Logger.debug("countDown done");

        Looper.loop();
        Logger.print("executeAudioCast done");
    }
}
