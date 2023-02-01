/*
 * Copyright (c) 2020 LG Electronics Inc.
 * SPDX-License-Identifier: LicenseRef-LGE-Proprietary
 */
package com.connectsdk.service.webos.lgcast.screenmirroring.service;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.provider.Settings;
import androidx.core.app.NotificationCompat;
import com.connectsdk.R;
import com.connectsdk.service.webos.lgcast.common.transfer.RTPStreamerConfig;
import com.connectsdk.service.webos.lgcast.common.transfer.RTPStreamerData;
import com.connectsdk.service.webos.lgcast.common.utils.AppUtil;
import com.connectsdk.service.webos.lgcast.common.utils.DeviceUtil;
import com.connectsdk.service.webos.lgcast.common.utils.JSONObjectEx;
import com.connectsdk.service.webos.lgcast.screenmirroring.ScreenMirroringConfig;
import com.connectsdk.service.webos.lgcast.screenmirroring.ScreenMirroringConst;
import com.connectsdk.service.webos.lgcast.screenmirroring.capability.MirroringSinkCapability;
import com.connectsdk.service.webos.lgcast.screenmirroring.capability.MirroringSourceCapability;
import com.connectsdk.service.webos.lgcast.screenmirroring.capability.VideoSizeInfo;
import com.connectsdk.service.webos.lgcast.screenmirroring.uibc.UibcAccessibilityService;
import com.lge.lib.lgcast.iface.MasterKey;
import com.lge.lib.lgcast.iface.MasterKeyFactoryIF;
import java.util.ArrayList;
import org.json.JSONObject;

public class MirroringServiceFunc {
    @SuppressLint("NewApi")
    public static Notification createNotification(Context context) {
        NotificationChannel notiChannel = new NotificationChannel(ScreenMirroringConfig.Notification.CHANNEL_ID, ScreenMirroringConfig.Notification.CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(notiChannel);

        Intent stopIntent = new Intent(context, MirroringService.class).setAction(MirroringServiceIF.ACTION_STOP_BY_NOTIFICATION);
        PendingIntent stopPendingIntent = PendingIntent.getService(context, 0, stopIntent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder notiBuilder = new NotificationCompat.Builder(context, ScreenMirroringConfig.Notification.CHANNEL_ID);
        notiBuilder.setSmallIcon(R.drawable.lgcast_noti_icon);
        notiBuilder.setContentTitle(context.getString(R.string.notification_screen_sharing_title));
        notiBuilder.setContentText(context.getString(R.string.notification_screen_sharing_desc));
        notiBuilder.setOngoing(true);//Android 13 changes for notification
        notiBuilder.addAction(R.drawable.lgcast_noti_icon_thinq, context.getString(R.string.notification_disconnect_action), stopPendingIntent);
        return notiBuilder.build();
    }

    // Getters ------------------------------------------------------------------------------------
    public static MediaProjection getMediaProjection(Context context, Intent intent) {
        MediaProjectionManager projectionManager = (MediaProjectionManager) context.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        return (intent != null) ? projectionManager.getMediaProjection(Activity.RESULT_OK, intent.getParcelableExtra(MirroringServiceIF.EXTRA_PROJECTION_DATA)) : null;
    }

    public static String getDeviceIpAddress(Intent intent) {
        return (intent != null) ? intent.getStringExtra(MirroringServiceIF.EXTRA_DEVICE_IP_ADDRESS) : null;
    }

    public static String getPackageName(Intent intent) {
        return (intent != null) ? intent.getStringExtra(MirroringServiceIF.EXTRA_PACKAGE_NAME) : null;
    }

    public static boolean isDualScreen(Intent intent) {
        return (intent != null) ? intent.getBooleanExtra(MirroringServiceIF.EXTRA_IS_DUAL_SCREEN, false) : false;
    }

    // Mirroring Capability -----------------------------------------------------------------------
    public static MirroringSinkCapability createPcMirroringSinkCapa() {
        MirroringSinkCapability mirroringSinkCapability = new MirroringSinkCapability();
        mirroringSinkCapability.ipAddress = ScreenMirroringConfig.Test.pcIpAddress;
        mirroringSinkCapability.videoUdpPort = ScreenMirroringConfig.Test.pcVideoUdpPort;
        mirroringSinkCapability.audioUdpPort = ScreenMirroringConfig.Test.pcAudioUdpPort;
        mirroringSinkCapability.videoLandscapeMaxWidth = 1920;
        mirroringSinkCapability.videoLandscapeMaxHeight = 1080;
        mirroringSinkCapability.videoPortraitMaxWidth = 1080;
        mirroringSinkCapability.videoPortraitMaxHeight = 1920;
        return mirroringSinkCapability;
    }

    public static MirroringSourceCapability createPcMirroringSourceCapa() {
        MirroringSourceCapability mirroringSourceCapability = new MirroringSourceCapability();
        mirroringSourceCapability.videoBitrate = 6 * 1024 * 1024;
        mirroringSourceCapability.videoWidth = 1920;
        mirroringSourceCapability.videoHeight = 1080;
        mirroringSourceCapability.masterKeys = new MasterKeyFactoryIF().createFixedKeys(ScreenMirroringConfig.RTP.FIXED_KEY);
        return mirroringSourceCapability;
    }

    public static MirroringSourceCapability createMirroringSourceCapa(Context context, MirroringSinkCapability mirroringSinkCapability) {
        int bitrate = calculateVideoBitrate(context);
        Point captureSize = calculateVideoCaptureSize(context, mirroringSinkCapability.isDisplayLandscape());
        Point activeSize = calculateVideoActiveSize(context, captureSize);

        // Video spec
        MirroringSourceCapability mirroringSourceCapability = new MirroringSourceCapability();
        mirroringSourceCapability.videoCodec = ScreenMirroringConfig.Video.CODEC;
        mirroringSourceCapability.videoClockRate = ScreenMirroringConfig.Video.CLOCK_RATE;
        mirroringSourceCapability.videoFramerate = ScreenMirroringConfig.Video.FRAMERATE;
        mirroringSourceCapability.videoBitrate = bitrate;
        mirroringSourceCapability.videoWidth = captureSize.x;
        mirroringSourceCapability.videoHeight = captureSize.y;
        mirroringSourceCapability.videoActiveWidth = activeSize.x;
        mirroringSourceCapability.videoActiveHeight = activeSize.y;
        mirroringSourceCapability.videoOrientation = (AppUtil.isLandscape(context) == true) ? ScreenMirroringConst.LANDSCAPE : ScreenMirroringConst.PORTRAIT;

        // Audio spec
        mirroringSourceCapability.audioCodec = ScreenMirroringConfig.Audio.CODEC;
        mirroringSourceCapability.audioClockRate = ScreenMirroringConfig.Audio.SAMPLING_RATE;
        mirroringSourceCapability.audioFrequency = ScreenMirroringConfig.Audio.SAMPLING_RATE;
        mirroringSourceCapability.audioStreamMuxConfig = ScreenMirroringConfig.Audio.STREAM_MUX_CONFIG;
        mirroringSourceCapability.audioChannels = ScreenMirroringConfig.Audio.CHANNEL_COUNT;

        // etc
        mirroringSourceCapability.masterKeys = new MasterKeyFactoryIF().createKeys(mirroringSinkCapability.publicKey);
        mirroringSourceCapability.uibcEnabled = isUibcEnabled(context); // TODO -------------------------------
        mirroringSourceCapability.screenOrientation = ScreenMirroringConst.LANDSCAPE_OR_PORTRAIT;

        // TODO --------------------------------------------------------------------------
        //if (isDualScreen(intent) == true) {
        //    mirroringSourceCapability.videoActiveWidth = captureSize.x;
        //    mirroringSourceCapability.videoActiveHeight = captureSize.y;
        //    mirroringSourceCapability.videoOrientation = MirroringConst.LANDSCAPE;
        //    mirroringSourceCapability.screenOrientation = MirroringConst.LANDSCAPE;
        //}
        // TODO --------------------------------------------------------------------------

        return mirroringSourceCapability;
    }

    public static VideoSizeInfo createVideoSizeInfo(Context context, boolean isDisplayLandscape) {
        Point captureSize = calculateVideoCaptureSize(context, isDisplayLandscape);
        Point activeSize = calculateVideoActiveSize(context, captureSize);

        VideoSizeInfo videoSizeInfo = new VideoSizeInfo();
        videoSizeInfo.videoWidth = captureSize.x;
        videoSizeInfo.videoHeight = captureSize.y;
        videoSizeInfo.videoActiveWidth = activeSize.x;
        videoSizeInfo.videoActiveHeight = activeSize.y;
        videoSizeInfo.videoOrientation = (AppUtil.isLandscape(context) == true) ? ScreenMirroringConst.LANDSCAPE : ScreenMirroringConst.PORTRAIT;
        return videoSizeInfo;
    }

    // RTP ----------------------------------------------------------------------------------------
    public static RTPStreamerConfig.VideoConfig createRtpVideoConfig(int videoBitRate) {
        RTPStreamerConfig.VideoConfig videoConfig = new RTPStreamerConfig.VideoConfig();
        videoConfig.setType(RTPStreamerData.VideoType.H264);
        videoConfig.setEnableMP(false);
        videoConfig.setFramerate(ScreenMirroringConfig.Video.FRAMERATE);
        videoConfig.setBitrate(videoBitRate);
        return videoConfig;
    }

    public static RTPStreamerConfig.AudioConfig createRtpAudioConfig() {
        RTPStreamerConfig.AudioConfig audioConfig = new RTPStreamerConfig.AudioConfig();
        audioConfig.setType(RTPStreamerData.AudioType.AAC);
        audioConfig.setCodecData(new byte[]{(byte) 0x11, (byte) 0x90}); // AAC LC, 48000, Stereo
        audioConfig.setSamplingRate(ScreenMirroringConfig.Audio.SAMPLING_RATE);
        audioConfig.setChannelCnt(ScreenMirroringConfig.Audio.CHANNEL_COUNT);
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

    // UIBC ---------------------------------------------------------------------------------------
    public static JSONObject createUibcInfo(Context context) {
        boolean uibcEnabled = MirroringServiceFunc.isUibcEnabled(context);
        JSONObjectEx uibcInfo = new JSONObjectEx().put(ScreenMirroringConst.UIBC_ENABLED, uibcEnabled);
        return uibcInfo.toJSONObject();
    }

    public static boolean isUibcEnabled(Context context) {
        String prefString = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        return prefString != null && prefString.contains(context.getPackageName() + "/" + UibcAccessibilityService.class.getName());
    }

    // Privates -----------------------------------------------------------------------------------
    private static int calculateVideoBitrate(Context context) {
        double ramSizeByGB = DeviceUtil.getTotalMemorySpace(context) / 1024.0 / 1024.0 / 1024.0;

        if (ramSizeByGB <= 3.0) return ScreenMirroringConfig.Video.BITRATE_1_5MB;
        else if (ramSizeByGB <= 4.0) return ScreenMirroringConfig.Video.BITRATE_3_0MB;
        else return ScreenMirroringConfig.Video.BITRATE_6_0MB;
    }

    private static Point calculateVideoCaptureSize(Context context, boolean isDisplayLandscape) {
        double ramSizeInGB = DeviceUtil.getTotalMemorySpace(context) / 1024.0 / 1024.0 / 1024.0;
        int w, h;

        if (ramSizeInGB <= 3.0) {
            w = ScreenMirroringConfig.Video.CAPTURE_SIZE_720P.x;
            h = ScreenMirroringConfig.Video.CAPTURE_SIZE_720P.y;
        } else if (ramSizeInGB <= 4.0) {
            w = ScreenMirroringConfig.Video.CAPTURE_SIZE_1080P.x;
            h = ScreenMirroringConfig.Video.CAPTURE_SIZE_1080P.y;
        } else {
            w = ScreenMirroringConfig.Video.CAPTURE_SIZE_1080P.x;
            h = ScreenMirroringConfig.Video.CAPTURE_SIZE_1080P.y;
        }

        return (isDisplayLandscape == true) ? new Point(w, h) : new Point(h, w);
    }

    private static Point calculateVideoActiveSize(Context context, Point captureSize) {
        Point activeSize = new Point();
        Point displaySize = AppUtil.getDisplaySize(context);

        if (AppUtil.isLandscape(context) == true) {
            activeSize.x = captureSize.x;
            double activeHeight = ((double) displaySize.y * (double) activeSize.x) / (double) displaySize.x;
            activeSize.y = (int) Math.round(activeHeight);
        } else {
            activeSize.y = captureSize.y;
            double activeWidth = ((double) displaySize.x * (double) activeSize.y) / (double) displaySize.y;
            activeSize.x = (int) Math.round(activeWidth);
        }

        return activeSize;
    }
}
