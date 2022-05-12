/*
 * Copyright (c) 2020 LG Electronics Inc.
 * SPDX-License-Identifier: LicenseRef-LGE-Proprietary
 */
package com.connectsdk.service.webos.lgcast.common.streaming;

import android.content.Context;
import com.connectsdk.service.webos.lgcast.common.transfer.RTPStreamerConfig;
import com.connectsdk.service.webos.lgcast.common.utils.Logger;
import com.connectsdk.service.webos.lgcast.common.utils.ThreadUtil;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;

public class UDPStreaming {
    private DatagramSocket mVideoSocket;
    private DatagramSocket mAudioSocket;
    private InetSocketAddress mVideoAdderss;
    private InetSocketAddress mAudioAdderss;
    private AtomicBoolean mStopThread;

    public UDPStreaming() {
        mStopThread = new AtomicBoolean(false);
    }

    public void setStreamingConfig(RTPStreamerConfig.VideoConfig videoConfig, RTPStreamerConfig.AudioConfig audioConfig, RTPStreamerConfig.SecurityConfig securityConfig) {
    }

    public void open(Context context, long ssrc, String ip, int videoPort, int audioPort) throws Exception {
        Logger.print("open (ssrc=%d, ip=%s, videoPort=%d, audioPort=%d)", ssrc, ip, videoPort, audioPort);
        if (context == null) throw new Exception("Invalid arguments");

        mVideoSocket = new DatagramSocket();
        mAudioSocket = new DatagramSocket();
        mVideoAdderss = new InetSocketAddress(ip, videoPort);
        mAudioAdderss = new InetSocketAddress(ip, audioPort);

        mStopThread.set(false);
        ThreadUtil.runInBackground(() -> executeVideoCast());
        ThreadUtil.runInBackground(() -> executeAudioCast());
    }

    public void close() {
        Logger.print("close");
        mStopThread.set(true);
    }

    private void executeVideoCast() {
        Logger.print("executeVideoCast");

        try {
            while (mStopThread.get() == false) {
                /*send(mVideoSocket, mVideoAdderss, media);*/
            }
        } catch (Exception e) {
            Logger.error(e);
        } finally {
            Logger.debug("executeVideoCast completed");
        }
    }

    private void executeAudioCast() {
        Logger.print("executeAudioCast");

        try {
            while (mStopThread.get() == false) {
                /*send(mAudioSocket, mAudioAdderss, media);*/
            }
        } catch (Exception e) {
            Logger.error(e);
        } finally {
            Logger.debug("executeAudioCast completed");
        }
    }

    //private void send(DatagramSocket socket, InetSocketAddress address, MediaBuffer.Media media) throws Exception {
    //    int packetSize = (media != null) ? media.data.length : 0;
    //    if (packetSize == 0 || packetSize > 65527) return;
    //    socket.send(new DatagramPacket(media.data, media.data.length, address));
    //}
}
