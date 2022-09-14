/*
 * Copyright (c) 2020 LG Electronics Inc.
 * SPDX-License-Identifier: LicenseRef-LGE-Proprietary
 */
package com.connectsdk.service.webos.lgcast.remotecamera.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.connectsdk.R;
import com.connectsdk.service.webos.lgcast.common.transfer.RTPStreamerConfig;
import com.connectsdk.service.webos.lgcast.common.transfer.RTPStreamerData;
import com.connectsdk.service.webos.lgcast.remotecamera.RemoteCameraConfig;
import com.lge.lib.lgcast.iface.MasterKey;
import java.util.ArrayList;

public class CameraServiceFunc {
    private static final String NOTI_CHANNEL_ID = "LG_CAST_REMOTE_CAMERA";
    private static final String NOTI_CHANNEL_NAME = "LG Cast Remote Camera";

    public static Notification createNotification(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notiChannel = new NotificationChannel(NOTI_CHANNEL_ID, NOTI_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
            ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(notiChannel);
        }

        Intent stopIntent = new Intent(context, CameraService.class).setAction(CameraServiceIF.ACTION_STOP_BY_NOTIFICATION);
        PendingIntent stopPendingIntent = PendingIntent.getService(context, 0, stopIntent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTI_CHANNEL_ID);
        builder.setSmallIcon(R.drawable.lgcast_noti_icon);
        builder.setContentTitle(context.getString(R.string.notification_remote_camera_title)); // TODO - string translation
        builder.setContentText(context.getString(R.string.notification_remote_camera_desc)); // TODO - string translation
        builder.addAction(R.drawable.lgcast_noti_icon, context.getString(R.string.notification_disconnect_action), stopPendingIntent);
        return builder.build();
    }

    public static String getDeviceIpAddress(Intent intent) {
        return (intent != null) ? intent.getStringExtra(CameraServiceIF.EXTRA_DEVICE_IP_ADDRESS) : null;
    }

    public static RTPStreamerConfig.VideoConfig createRtpVideoConfig(int width, int height) {
        RTPStreamerConfig.VideoConfig videoConfig = new RTPStreamerConfig.VideoConfig();
        videoConfig.setType(RTPStreamerData.VideoType.MJPEG);
        videoConfig.setEnableMP(false);
        videoConfig.setMpUnitSize((int) (width * height * 1.5));
        videoConfig.setWidth(width);
        videoConfig.setHeight(height);
        videoConfig.setFramerate(RemoteCameraConfig.Camera.FRAMERATE);
        videoConfig.setBitrate(RemoteCameraConfig.Camera.BITRATE);
        return videoConfig;
    }

    public static RTPStreamerConfig.AudioConfig createRtpAudioConfig() {
        RTPStreamerConfig.AudioConfig audioConfig = new RTPStreamerConfig.AudioConfig();
        audioConfig.setType(RTPStreamerData.AudioType.PCM);
        audioConfig.setSamplingRate(RemoteCameraConfig.Mic.SAMPLING_RATE);
        audioConfig.setChannelCnt(RemoteCameraConfig.Mic.CHANNEL_COUNT);
        audioConfig.setEnableMP(false);
        return audioConfig;
    }

    public static RTPStreamerConfig.SecurityConfig createRtpSecurityConfig(ArrayList<MasterKey> masterKeys) {
        ArrayList<RTPStreamerConfig.SecurityKey> keys = new ArrayList<>();

        for (MasterKey masterKey : masterKeys) {
            RTPStreamerConfig.SecurityKey key = new RTPStreamerConfig.SecurityKey();
            key.masterKey = masterKey.key;
            key.mki = masterKey.mki;
            keys.add(key);
        }

        RTPStreamerConfig.SecurityConfig securityConfig = new RTPStreamerConfig.SecurityConfig();
        securityConfig.setEnableSecurity(true);
        securityConfig.setCipherType(RTPStreamerData.SRTPCipherType.AES_128_ICM);
        securityConfig.setAuthType(RTPStreamerData.SRTPAuthType.HMAC_SHA1_80);
        securityConfig.setKeys(keys);
        return securityConfig;
    }
}
