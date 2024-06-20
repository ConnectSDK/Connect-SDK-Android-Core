/*
 * Copyright (c) 2020 LG Electronics Inc.
 * SPDX-License-Identifier: LicenseRef-LGE-Proprietary
 */
package com.connectsdk.service.webos.lgcast.screenmirroring.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.content.res.Configuration;
import android.media.projection.MediaProjection;
import android.os.IBinder;
import com.connectsdk.R;
import com.connectsdk.device.ConnectableDevice;
import com.connectsdk.discovery.DiscoveryManager;
import com.connectsdk.service.webos.lgcast.common.connection.ConnectionManager;
import com.connectsdk.service.webos.lgcast.common.connection.ConnectionManagerError;
import com.connectsdk.service.webos.lgcast.common.connection.ConnectionManagerListener;
import com.connectsdk.service.webos.lgcast.common.connection.MobileDescription;
import com.connectsdk.service.webos.lgcast.common.streaming.RTPStreaming;
import com.connectsdk.service.webos.lgcast.common.utils.AppUtil;
import com.connectsdk.service.webos.lgcast.common.utils.HandlerThreadEx;
import com.connectsdk.service.webos.lgcast.common.utils.IOUtil;
import com.connectsdk.service.webos.lgcast.common.utils.Logger;
import com.connectsdk.service.webos.lgcast.common.utils.StringUtil;
import com.connectsdk.service.webos.lgcast.common.utils.ThreadUtil;
import com.connectsdk.service.webos.lgcast.common.utils.TimerUtil;
import com.connectsdk.service.webos.lgcast.screenmirroring.ScreenMirroringConfig;
import com.connectsdk.service.webos.lgcast.screenmirroring.ScreenMirroringConst;
import com.connectsdk.service.webos.lgcast.screenmirroring.capability.MirroringSinkCapability;
import com.connectsdk.service.webos.lgcast.screenmirroring.capability.MirroringSourceCapability;
import com.connectsdk.service.webos.lgcast.screenmirroring.capability.VideoSizeInfo;
import com.connectsdk.service.webos.lgcast.screenmirroring.uibc.UibcAccessibilityService;
import com.lge.lib.lgcast.iface.AudioCaptureIF;
import com.lge.lib.lgcast.iface.VideoCaptureIF;
import org.json.JSONObject;

public class MirroringService extends Service {
    private HandlerThreadEx mServiceHandler;

    private MirroringServiceEvent mMirroringServiceEvent;
    private MirroringVolume mMirroringVolume;
    private MediaProjection mMediaProjection;

    private ConnectionManager mConnectionManager;
    private MirroringSinkCapability mMirroringSinkCapability;
    private MirroringSourceCapability mMirroringSourceCapability;

    private RTPStreaming mRTPStreaming;
    private AudioCaptureIF mAudioCapture;
    private VideoCaptureIF mVideoCapture;

    private int mCurrentOrientation;
    private int mCurrentScreenWidth;

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.showDebug(com.connectsdk.BuildConfig.DEBUG);
        mServiceHandler = new HandlerThreadEx("MirroringService Handler");
        mServiceHandler.start();

        mCurrentOrientation = AppUtil.getOrientation(this);
        mCurrentScreenWidth = getResources().getConfiguration().smallestScreenWidthDp;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mServiceHandler.quit();
        mServiceHandler = null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger.print("onStartCommand: " + StringUtil.toString(intent));
        String action = (intent != null) ? intent.getAction() : null;

        mServiceHandler.post(() -> {
            if (MirroringServiceIF.ACTION_START_REQUEST.equals(action) == true) executeStart(intent);
            else if (MirroringServiceIF.ACTION_STOP_REQUEST.equals(action) == true) executeStop();
            else if (MirroringServiceIF.ACTION_STOP_BY_NOTIFICATION.equals(action) == true) executeStopByNotification();
        });

        return START_STICKY;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mConnectionManager == null || mMirroringSinkCapability == null || mMirroringSourceCapability == null) return; // Device is not connected

        if (mCurrentOrientation != newConfig.orientation) {
            Logger.debug("Orientation changed: old=%d, new=%d", mCurrentOrientation, newConfig.orientation);
            mCurrentOrientation = newConfig.orientation;
            updateVideoSizeInfo((mCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE) ? ScreenMirroringConst.LANDSCAPE : ScreenMirroringConst.PORTRAIT);
        }

        if (mCurrentScreenWidth != newConfig.smallestScreenWidthDp) {
            Logger.debug("Screen width changed: old=%d, new=%d", mCurrentScreenWidth, newConfig.smallestScreenWidthDp);
            mCurrentScreenWidth = newConfig.smallestScreenWidthDp;
            updateVideoSizeInfo((mCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE) ? ScreenMirroringConst.LANDSCAPE : ScreenMirroringConst.PORTRAIT);
        }
    }

    private void executeStart(Intent intent) {
        ConnectionManagerListener connectionListener = new ConnectionManagerListener() {
            @Override
            public void onPairingRequested() {
                Logger.debug("onPairingRequested");
                MirroringServiceIF.notifyPairing(getBaseContext());
            }

            @Override
            public void onPairingRejected() {
                Logger.error("onPairingRejected");
                MirroringServiceIF.respondStart(getBaseContext(), false, false);
                stop();
            }

            @Override
            public void onConnectionFailed(String message) {
                Logger.error("onConnectionFailed (%s)", message);
                MirroringServiceIF.respondStart(getBaseContext(), false, false);
                stop();
            }

            @Override
            public void onConnectionCompleted(JSONObject jsonObj) {
                Logger.debug("onConnectionCompleted");
                mMirroringSinkCapability = new MirroringSinkCapability(jsonObj);
                mMirroringSinkCapability.displayOrientation = (ScreenMirroringConfig.Test.displayOrientation != null) ? ScreenMirroringConfig.Test.displayOrientation : mMirroringSinkCapability.displayOrientation;
                mMirroringSinkCapability.debug();

                mMirroringSourceCapability = MirroringServiceFunc.createMirroringSourceCapa(getBaseContext(), mMirroringSinkCapability);
                mMirroringSourceCapability.debug();

                MobileDescription mobileDescription = new MobileDescription(getBaseContext());
                mobileDescription.debug();

                mConnectionManager.setSourceDeviceCapability(mMirroringSourceCapability.toJSONObject(), mobileDescription.toJSONObject());
                UibcAccessibilityService.onDisplayRotated(mMirroringSinkCapability.displayOrientation); // TODO: Check it later.
            }

            @Override
            public void onReceivePlayCommand(JSONObject jsonObj) {
                Logger.debug("onReceivePlayCommand");
                boolean result = startCaptureAndStreaming(intent);
                MirroringServiceIF.respondStart(getBaseContext(), result, MirroringServiceFunc.isDualScreen(intent));
                if (result == false) stop();
            }

            @Override
            public void onReceiveStopCommand(JSONObject jsonObj) {
                Logger.error("onReceiveStopCommand (noop)");
            }

            @Override
            public void onReceiveGetParameter(JSONObject jsonObj) {
                Logger.error("onReceiveGetParameter (noop)");
            }

            @Override
            public void onReceiveSetParameter(JSONObject jsonObj) {
                Logger.debug("onReceiveSetParameter");
                JSONObject mirroringObj = (jsonObj != null) ? jsonObj.optJSONObject("mirroring") : null;
                String displayOrientation = (mirroringObj != null) ? mirroringObj.optString("displayOrientation") : null;
                if (displayOrientation == null) return;

                Logger.debug("onDisplayRotated (displayOrientation=%s, phoneOrientation=%s)", displayOrientation, AppUtil.getOrientation(getBaseContext()));
                UibcAccessibilityService.onDisplayRotated(displayOrientation); // TODO: Check it!!

                if (mMirroringSinkCapability != null && mMirroringSinkCapability.isSupportPortraitMode() == true) {
                    updateVideoSizeInfo(displayOrientation);
                    mVideoCapture.resizeCapture(mMirroringSourceCapability.videoWidth, mMirroringSourceCapability.videoHeight, mMirroringSourceCapability.videoBitrate);
                } else {
                    Logger.error("TV does not support PORTRAIT mode");
                }
            }

            @Override
            public void onError(ConnectionManagerError connectionError, String errorMessage) {
                Logger.error("onError: connectionError=%s, errorMessage=%s", connectionError, errorMessage);
                TimerUtil.schedule(() -> MirroringServiceIF.notifyError(getBaseContext(), MirroringServiceIF.toMirroringError(connectionError)), 150);
                stop();
            }
        };

        Logger.print("executeStart");
        start(intent, connectionListener);
        if (com.connectsdk.BuildConfig.DEBUG == true) AppUtil.showToastLong(this, "########## DEBUG version ##########");
    }

    private void executeStop() {
        Logger.print("executeStop");
        stop();
        MirroringServiceIF.respondStop(this, true);
    }

    private void executeStopByNotification() {
        Logger.print("executeStopByNotification");
        stop();
        MirroringServiceIF.notifyError(this, MirroringServiceError.ERROR_STOPPED_BY_NOTIFICATION);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    private void start(Intent intent, ConnectionManagerListener connectionListener) {
        Logger.print("stop");
        initializeService();
        openTvConnection(intent, connectionListener);
    }

    private void stop() {
        Logger.print("stop");
        stopCaptureAndStreaming();
        closeTvConnection();
        terminateService();
    }

    @SuppressLint("NewApi")
    private void initializeService() {
        Logger.print("initializeService (SDK version=%s)", IOUtil.readRawResourceText(this, R.raw.lgcast_version));
        startForeground(ScreenMirroringConfig.Notification.ID, MirroringServiceFunc.createNotification(this), ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION);

        Intent intent = new Intent(this, UibcAccessibilityService.class).setAction(UibcAccessibilityService.START_SERVICE);
        startService(intent);

        //???mUibcAppEventDispatcher = new UibcAppEventDispatcher(getApplication());
        //???mUibcAppEventDispatcher.create();

        mMirroringServiceEvent = new MirroringServiceEvent(this);
        mMirroringServiceEvent.startScreenOnOffReceiver(turnOn -> {
            if (mConnectionManager != null) mConnectionManager.notifyScreenOnOff(turnOn);
        });

        mMirroringServiceEvent.startAccessibilitySettingObserver(uibcEnabled -> {
            JSONObject uibcInfo = MirroringServiceFunc.createUibcInfo(getBaseContext());
            if (mConnectionManager != null) mConnectionManager.updateSourceDeviceCapability(uibcInfo);
        });

        mMirroringVolume = new MirroringVolume(this);
        mMirroringVolume.startMute();
    }

    private void terminateService() {
        Logger.print("terminateService");
        if (mMirroringVolume != null) mMirroringVolume.stopMute();
        mMirroringVolume = null;

        if (mMirroringServiceEvent != null) mMirroringServiceEvent.quit();
        mMirroringServiceEvent = null;

        //???if (mUibcAppEventDispatcher != null) mUibcAppEventDispatcher.destroy();
        //???mUibcAppEventDispatcher = null;

        Intent intent = new Intent(this, UibcAccessibilityService.class).setAction(UibcAccessibilityService.STOP_SERVICE);
        startService(intent);

        stopForeground(true);
        ThreadUtil.runOnMainLooper(this::stopSelf, 150);
    }

    private void openTvConnection(Intent intent, ConnectionManagerListener connectionListener) {
        Logger.print("openTvConnection");

        if (ScreenMirroringConfig.Test.usePcPlayer == true) {
            mMirroringSinkCapability = MirroringServiceFunc.createPcMirroringSinkCapa();
            mMirroringSourceCapability = MirroringServiceFunc.createPcMirroringSourceCapa();
            if (connectionListener != null) connectionListener.onReceivePlayCommand(null);
        } else {
            String deviceIpAddress = MirroringServiceFunc.getDeviceIpAddress(intent);
            ConnectableDevice connectableDevice = DiscoveryManager.getInstance().getDeviceByIpAddress(deviceIpAddress);
            mConnectionManager = new ConnectionManager("mirroring");
            mConnectionManager.openConnection(connectableDevice, connectionListener);
        }
    }

    private void closeTvConnection() {
        Logger.print("closeTvConnection");
        if (mConnectionManager != null) mConnectionManager.closeConnection();
        mConnectionManager = null;
    }

    private boolean startCaptureAndStreaming(Intent intent) {
        Logger.print("startCaptureAndStreaming");

        try {
            mMediaProjection = MirroringServiceFunc.getMediaProjection(this, intent);
            if (mMediaProjection == null) throw new Exception("Invalid projection");
            mMediaProjection.registerCallback(new MediaProjection.Callback() {
                @Override
                public void onStop() {
                    super.onStop();
                    stopCaptureAndStreaming();
                }
            }, null);

            mRTPStreaming = new RTPStreaming();
            mRTPStreaming.setStreamingConfig(MirroringServiceFunc.createRtpVideoConfig(mMirroringSourceCapability.videoBitrate), MirroringServiceFunc.createRtpAudioConfig(), MirroringServiceFunc.createRtpSecurityConfig(mMirroringSourceCapability.masterKeys));
            mRTPStreaming.open(this, ScreenMirroringConfig.RTP.SSRC, mMirroringSinkCapability.ipAddress, mMirroringSinkCapability.videoUdpPort, mMirroringSinkCapability.audioUdpPort);

            if (ScreenMirroringConfig.Test.testMkiUpdate == true) {
                TimerUtil.schedule(() -> {
                    Logger.error("### TEST MASTER KEY UPDATE ###");
                    if (mRTPStreaming != null) mRTPStreaming.updateMasterKey();
                }, 30 * 1000);
            }

            if (ScreenMirroringConfig.Test.testOrientationChange == true) {
                TimerUtil.schedule(() -> {
                    Logger.error("### TEST ORIENTATION CHANGE ###");

                    if (mMirroringSourceCapability.videoWidth == 1920 && mMirroringSourceCapability.videoHeight == 1080) {
                        Logger.error("Change to PORTRAIT mode");
                        mMirroringSourceCapability.videoWidth = 1080;
                        mMirroringSourceCapability.videoHeight = 1920;
                    } else {
                        Logger.error("Change to LANDSCAPE mode");
                        mMirroringSourceCapability.videoWidth = 1920;
                        mMirroringSourceCapability.videoHeight = 1080;
                    }

                    mVideoCapture.stopCapture();
                    mVideoCapture.startCapture(mMirroringSourceCapability.videoWidth, mMirroringSourceCapability.videoHeight, mMirroringSourceCapability.videoBitrate, mMediaProjection, mRTPStreaming.getVideoStreamHandler());
                }, 10000, 5000);
            }

            mAudioCapture = new AudioCaptureIF();
            mAudioCapture.setErrorListener(this::stop);
            mAudioCapture.startCapture(ScreenMirroringConfig.Audio.SAMPLING_RATE, ScreenMirroringConfig.Audio.CHANNEL_COUNT, mMediaProjection, mRTPStreaming.getAudioStreamHandler());

            mVideoCapture = new VideoCaptureIF();
            mVideoCapture.setErrorListener(this::stop);
            mVideoCapture.startCapture(mMirroringSourceCapability.videoWidth, mMirroringSourceCapability.videoHeight, mMirroringSourceCapability.videoBitrate, mMediaProjection, mRTPStreaming.getVideoStreamHandler());
            return true;
        } catch (Exception e) {
            Logger.error(e);
            return false;
        }
    }

    private void stopCaptureAndStreaming() {
        Logger.print("stopCaptureAndStreaming");
        if (mVideoCapture != null) mVideoCapture.stopCapture();
        mVideoCapture = null;

        if (mAudioCapture != null) mAudioCapture.stopCapture();
        mAudioCapture = null;

        if (mRTPStreaming != null) mRTPStreaming.close();
        mRTPStreaming = null;

        if (mMediaProjection != null) mMediaProjection.stop();
        mMediaProjection = null;
    }

    private void updateVideoSizeInfo(String displayOrientation) {
        VideoSizeInfo videoSizeInfo = MirroringServiceFunc.createVideoSizeInfo(this, ScreenMirroringConst.LANDSCAPE.equals(displayOrientation));
        videoSizeInfo.debug();

        mMirroringSourceCapability.videoWidth = videoSizeInfo.videoWidth;
        mMirroringSourceCapability.videoHeight = videoSizeInfo.videoHeight;
        mMirroringSourceCapability.videoActiveWidth = videoSizeInfo.videoActiveWidth;
        mMirroringSourceCapability.videoActiveHeight = videoSizeInfo.videoActiveHeight;
        mMirroringSourceCapability.videoOrientation = videoSizeInfo.videoOrientation;
        mMirroringSinkCapability.displayOrientation = displayOrientation;

        JSONObject capabilityJson = videoSizeInfo.toJSONObject(this);
        if (mConnectionManager != null) mConnectionManager.updateSourceDeviceCapability(capabilityJson);
    }
}
