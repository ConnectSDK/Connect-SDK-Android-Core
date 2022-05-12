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
import com.connectsdk.service.webos.lgcast.common.utils.Logger;
import com.connectsdk.service.webos.lgcast.screenmirroring.ScreenMirroringConfig;
import com.connectsdk.service.webos.lgcast.screenmirroring.capability.MirroringSinkCapability;
import com.connectsdk.service.webos.lgcast.screenmirroring.capability.MirroringSourceCapability;
import com.connectsdk.service.webos.lgcast.screenmirroring.uibc.UibcAccessibilityService;
import com.lge.lib.security.iface.MasterKey;
import com.lge.lib.security.iface.MasterKeyFactoryIF;
import java.util.ArrayList;
import org.json.JSONObject;

public class MirroringServiceFunc {
    private static final String WIDTH = "width";
    private static final String HEIGHT = "height";
    private static final String ACTIVE_WIDTH = "activeWidth";
    private static final String ACTIVE_HEIGHT = "activeHeight";
    private static final String ORIENTATION = "orientation";
    private static final String VIDEO = "video";

    private static final String LANDSCAPE = "landscape";
    private static final String PORTRAIT = "portrait";
    private static final String LANDSCAPE_OR_PORTRAIT = "landscape|portrait";
    private static final String UIBC_ENABLED = "uibcEnabled";

    private static final String NOTI_CHANNEL_ID = "LG_CAST_SCREEN_MIRRORING";
    private static final String NOTI_CHANNEL_NAME = "LG Cast Screen Mirroring";

    @SuppressLint("NewApi")
    public static Notification createNotification(Context context) {
        NotificationChannel notiChannel = new NotificationChannel(NOTI_CHANNEL_ID, NOTI_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(notiChannel);

        Intent stopIntent = new Intent(context, MirroringService.class).setAction(MirroringServiceIF.ACTION_STOP_BY_NOTIFICATION);
        PendingIntent stopPendingIntent = PendingIntent.getService(context, 0, stopIntent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder notiBuilder = new NotificationCompat.Builder(context, NOTI_CHANNEL_ID);
        notiBuilder.setSmallIcon(R.drawable.lgcast_noti_icon);
        notiBuilder.setContentTitle(context.getString(R.string.notification_screen_sharing_title));
        notiBuilder.setContentText(context.getString(R.string.notification_screen_sharing_desc));
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
    public static boolean isCaptureByDisplaySize(Context context) {
        if (ScreenMirroringConfig.Test.captureByDisplaySize == true) return true;
        return DeviceUtil.getTotalMemorySpace(context) <= 3L * 1024L * 1024L * 1024L; // We are assuming that a device which has 3GB RAM is low spec.
    }

    public static Point getCaptureSizeInLandscape(Context context) {
        return (MirroringServiceFunc.isCaptureByDisplaySize(context) == true) ?
                AppUtil.getDisplaySizeInLandscape(context) :
                new Point(ScreenMirroringConfig.Video.DEFAULT_WIDTH, ScreenMirroringConfig.Video.DEFAULT_HEIGHT);
    }

    public static MirroringSinkCapability createPcMirroringSinkCapa() {
        MirroringSinkCapability mirroringSinkCapability = new MirroringSinkCapability();
        mirroringSinkCapability.ipAddress = ScreenMirroringConfig.Test.pcIpAddress;
        mirroringSinkCapability.videoUdpPort = ScreenMirroringConfig.Test.pcVideoUdpPort;
        mirroringSinkCapability.audioUdpPort = ScreenMirroringConfig.Test.pcAudioUdpPort;
        mirroringSinkCapability.videoLandscapeMaxWidth = ScreenMirroringConfig.Video.DEFAULT_WIDTH;
        mirroringSinkCapability.videoLandscapeMaxHeight = ScreenMirroringConfig.Video.DEFAULT_HEIGHT;
        mirroringSinkCapability.videoPortraitMaxWidth = ScreenMirroringConfig.Video.DEFAULT_HEIGHT;
        mirroringSinkCapability.videoPortraitMaxHeight = ScreenMirroringConfig.Video.DEFAULT_WIDTH;
        return mirroringSinkCapability;
    }

    public static MirroringSourceCapability createMirroringSourceCapa(Context context, Intent intent, MirroringSinkCapability mirroringSinkCapability) {
        boolean captureByDisplaySize = isCaptureByDisplaySize(context);
        boolean isDisplayLandscape = mirroringSinkCapability.isDisplayLandscape();
        Logger.debug("captureByDisplaySize=%s, isDisplayLandscape=%s", captureByDisplaySize, isDisplayLandscape);

        Point captureSize = calculateVideoCaptureSize(context, mirroringSinkCapability);
        Point activeSize = calculateVideoActiveSize(context, captureSize);

        Logger.error("##### MIRRORING SOURCE CAPABILITY (onConnectionPrepared) #####");
        Logger.error("display orientation=" + mirroringSinkCapability.displayOrientation);
        Logger.error("phone orientation=" + ((AppUtil.isLandscape(context) == true) ? LANDSCAPE : PORTRAIT));
        Logger.error("capture width=" + captureSize.x);
        Logger.error("capture height=" + captureSize.y);
        Logger.error("active width=" + activeSize.x);
        Logger.error("active height=" + activeSize.y);
        Logger.error("--------------------------------------------------------------");

        // Video spec
        MirroringSourceCapability mirroringSourceCapability = new MirroringSourceCapability();
        mirroringSourceCapability.videoCodec = ScreenMirroringConfig.Video.CODEC;
        mirroringSourceCapability.videoClockRate = ScreenMirroringConfig.Video.CLOCK_RATE;
        mirroringSourceCapability.videoFramerate = ScreenMirroringConfig.Video.FRAMERATE;
        mirroringSourceCapability.videoWidth = captureSize.x;
        mirroringSourceCapability.videoHeight = captureSize.y;
        mirroringSourceCapability.videoActiveWidth = activeSize.x;
        mirroringSourceCapability.videoActiveHeight = activeSize.y;
        mirroringSourceCapability.videoOrientation = (AppUtil.isLandscape(context) == true) ? LANDSCAPE : PORTRAIT;

        // Audio spec
        mirroringSourceCapability.audioCodec = ScreenMirroringConfig.Audio.CODEC;
        mirroringSourceCapability.audioClockRate = ScreenMirroringConfig.Audio.SAMPLING_RATE;
        mirroringSourceCapability.audioFrequency = ScreenMirroringConfig.Audio.SAMPLING_RATE;
        mirroringSourceCapability.audioStreamMuxConfig = ScreenMirroringConfig.Audio.STREAM_MUX_CONFIG;
        mirroringSourceCapability.audioChannels = ScreenMirroringConfig.Audio.CHANNEL_COUNT;

        mirroringSourceCapability.masterKeys = new MasterKeyFactoryIF().createKeys(mirroringSinkCapability.publicKey);
        mirroringSourceCapability.uibcEnabled = isUibcEnabled(context); // TODO -------------------------------
        mirroringSourceCapability.screenOrientation = LANDSCAPE_OR_PORTRAIT;

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

    public static JSONObject createVideoSizeInfo(Context context, MirroringSinkCapability mirroringSinkCapability) {
        boolean captureByDisplaySize = isCaptureByDisplaySize(context);
        boolean isDisplayLandscape = mirroringSinkCapability.isDisplayLandscape();
        Logger.debug("captureByDisplaySize=%s, isDisplayLandscape=%s", captureByDisplaySize, isDisplayLandscape);

        Point captureSize = calculateVideoCaptureSize(context, mirroringSinkCapability);
        Point activeSize = calculateVideoActiveSize(context, captureSize);

        Logger.error("##### MIRRORING SOURCE CAPABILITY (onDisplayRotated) #####");
        Logger.error("display orientation=" + mirroringSinkCapability.displayOrientation);
        Logger.error("phone orientation=" + ((AppUtil.isLandscape(context) == true) ? LANDSCAPE : PORTRAIT));
        Logger.error("capture width=" + captureSize.x);
        Logger.error("capture height=" + captureSize.y);
        Logger.error("active width=" + activeSize.x);
        Logger.error("active height=" + activeSize.y);
        Logger.error("--------------------------------------------------------------");

        JSONObjectEx videoObj = new JSONObjectEx();
        videoObj.put(WIDTH, captureSize.x);
        videoObj.put(HEIGHT, captureSize.y);
        videoObj.put(ACTIVE_WIDTH, activeSize.x);
        videoObj.put(ACTIVE_HEIGHT, activeSize.y);
        videoObj.put(ORIENTATION, (AppUtil.isLandscape(context) == true) ? LANDSCAPE : PORTRAIT);

        JSONObjectEx mirroringObj = new JSONObjectEx();
        mirroringObj.put(VIDEO, videoObj);
        return mirroringObj.toJSONObject();
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
        JSONObjectEx uibcInfo = new JSONObjectEx().put(UIBC_ENABLED, uibcEnabled);
        return uibcInfo.toJSONObject();
    }

    public static boolean isUibcEnabled(Context context) {
        String prefString = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        return prefString != null && prefString.contains(context.getPackageName() + "/" + UibcAccessibilityService.class.getName());
    }

    // Privates -----------------------------------------------------------------------------------
    private static Point calculateVideoCaptureSize(Context context, MirroringSinkCapability mirroringSinkCapability) {
        boolean captureByDisplaySize = isCaptureByDisplaySize(context);
        boolean isDisplayLandscape = mirroringSinkCapability.isDisplayLandscape();
        Point displaySizeInLandscape = AppUtil.getDisplaySizeInLandscape(context);
        Logger.debug("captureByDisplaySize=%s, isDisplayLandscape=%s", captureByDisplaySize, isDisplayLandscape);

        // In case of low spec device, it will capture screen by display size, otherwise by FHD
        int w = (captureByDisplaySize == true) ? displaySizeInLandscape.x : ScreenMirroringConfig.Video.DEFAULT_WIDTH;
        int h = (captureByDisplaySize == true) ? displaySizeInLandscape.y : ScreenMirroringConfig.Video.DEFAULT_HEIGHT;
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
