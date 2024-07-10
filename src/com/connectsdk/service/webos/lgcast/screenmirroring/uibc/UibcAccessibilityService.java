/*
 * Copyright (c) 2020 LG Electronics Inc.
 * SPDX-License-Identifier: LicenseRef-LGE-Proprietary
 */
package com.connectsdk.service.webos.lgcast.screenmirroring.uibc;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.content.res.Configuration;
import android.graphics.Path;
import android.view.ViewConfiguration;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import com.connectsdk.service.webos.lgcast.common.utils.HandlerThreadEx;
import com.connectsdk.service.webos.lgcast.common.utils.Logger;
import com.connectsdk.service.webos.lgcast.screenmirroring.ScreenMirroringConfig;
import com.connectsdk.service.webos.lgcast.screenmirroring.service.MirroringServiceFunc;
import org.json.JSONObject;

public class UibcAccessibilityService extends AccessibilityService {
    public static final String START_SERVICE = "AccessibilityService:START_SERVICE";
    public static final String STOP_SERVICE = "AccessibilityService:STOP_SERVICE";

    private static HandlerThreadEx mServiceHandler;
    private static HandlerThreadEx mRotationHandler;

    private PointConverter mPointConverter;
    private Path mMousePointPath;
    private boolean mIsMouseClicked;
    private long mMouseDownTime;

    private int mCurrentOrientation;
    private int mCurrentScreenWidth;

    private String mCurrentTVOrientation;

    @Override
    public void onCreate() {
        super.onCreate();

        Configuration config = getResources().getConfiguration();
        mCurrentOrientation = config.orientation;
        mCurrentScreenWidth = config.smallestScreenWidthDp;

        mCurrentTVOrientation = "landscape";
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
    }

    @Override
    public void onInterrupt() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = (intent != null) ? intent.getAction() : null;
        if (START_SERVICE.equals(action) == true) startService();
        if (STOP_SERVICE.equals(action) == true) stopService();
        return START_STICKY;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mCurrentOrientation == newConfig.orientation && mCurrentScreenWidth == newConfig.smallestScreenWidthDp) return;
        mCurrentOrientation = newConfig.orientation;
        mCurrentScreenWidth = newConfig.smallestScreenWidthDp;

        if (mPointConverter != null) {
            if ("portrait".equals(mCurrentTVOrientation)) mPointConverter.update(ScreenMirroringConfig.Video.DEFAULT_HEIGHT, ScreenMirroringConfig.Video.DEFAULT_WIDTH);
            else mPointConverter.update(ScreenMirroringConfig.Video.DEFAULT_WIDTH, ScreenMirroringConfig.Video.DEFAULT_HEIGHT);
        }
    }

    public static void sendUibcInfo(JSONObject uibcInfo) {
        if (uibcInfo == null) return;
        if (mServiceHandler != null) mServiceHandler.sendMessage(uibcInfo);
        else Logger.error("Invalid handler");
    }

    public static void onDisplayRotated(String displayOrientation) {
        if (mRotationHandler != null) mRotationHandler.sendMessage(displayOrientation);
        else Logger.error("Invalid handler");
    }

    private void startService() {
        Logger.print("start TvInputService");
        startForeground(ScreenMirroringConfig.Notification.ID, MirroringServiceFunc.createNotification(this), ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE);

        mServiceHandler = new HandlerThreadEx("UibcAccessibilityService Handler");
        mServiceHandler.start(msg -> handleUibcInfo((JSONObject) msg.obj));

        mRotationHandler = new HandlerThreadEx("UibcAccessibilityService Screen Rotation Handler");
        mRotationHandler.start(msg -> handleDisplayRotation((String) msg.obj));

        mPointConverter = new PointConverter(this, ScreenMirroringConfig.Video.DEFAULT_WIDTH, ScreenMirroringConfig.Video.DEFAULT_HEIGHT);
        mMousePointPath = new Path();
        mIsMouseClicked = false;
        mMouseDownTime = 0;
    }

    private void stopService() {
        Logger.print("stop TvInputService");
        if (mServiceHandler != null) mServiceHandler.quit();
        mServiceHandler = null;
        if (mRotationHandler != null) mRotationHandler.quit();
        mRotationHandler = null;

        stopForeground(true);
        stopSelf();
    }

    private void handleUibcInfo(JSONObject uibcInfo) {
        String type = (uibcInfo != null) ? uibcInfo.optString("type", "none") : "none";
        /*Logger.error("### screenX=%d, screenY=%d ###", uibcInfo.optInt("screenX", 0), uibcInfo.optInt("screenY", 0));//*/

        switch (type) {
            case "mousedown":
            case "mousemove":
            case "mouseup": {
                int button = uibcInfo.optInt("button", 0);
                int screenX = uibcInfo.optInt("screenX", 0);
                int screenY = uibcInfo.optInt("screenY", 0);
                handleMouseEvent(type, button, screenX, screenY);
                break;
            }

            case "keydown":
            case "keyup": {
                int keyCode = uibcInfo.optInt("keyCode", 0);
                boolean shiftKey = uibcInfo.optBoolean("shiftKey", false);
                handleKeyEvent(type, keyCode, shiftKey);
                break;
            }

            case "wheel": {
                int deltaY = uibcInfo.optInt("deltaY", 0);
                int screenX = uibcInfo.optInt("screenX", 0);
                int screenY = uibcInfo.optInt("screenY", 0);
                handleWheelEvent(type, deltaY, screenX, screenY);
                break;
            }
        }
    }

    private void handleMouseEvent(String type, int button, int screenX, int screenY) {
        if (button == 0 /* LEFT BUTTON */) {
            if (type.equals("mousedown") == true) onRemoteMouseDown(screenX, screenY);
            else if (type.equals("mousemove") == true) onRemoteMouseMove(screenX, screenY);
            else if (type.equals("mouseup") == true) onRemoteMouseUp(screenX, screenY);
        }
    }

    private void handleKeyEvent(String type, int keyCode, boolean shiftKey) {
        if (keyCode == UibcKeyCode.TV_KEYCODE_BACK && type.equals("keyup") == true) onRemoteBackButton();
    }

    private void handleWheelEvent(String type, int deltaY, int screenX, int screenY) {
        boolean result = scrollView(getRootInActiveWindow(), deltaY);
        Logger.debug("Dispatch result = " + result);
    }

    private void onRemoteMouseDown(float x, float y) {
        PointConverter.POINT p = mPointConverter.convert(x, y); /*p.debug();*/
        if (p.screenX == -1 || p.screenY == -1) return;

        mMousePointPath.reset();
        mMousePointPath.moveTo(p.screenX, p.screenY);

        mMouseDownTime = System.currentTimeMillis();
        mIsMouseClicked = true;
    }

    private void onRemoteMouseMove(float x, float y) {
        if (mIsMouseClicked == false) {
            /*Logger.error("Not mouse clicked");*/
            return;
        }

        PointConverter.POINT p = mPointConverter.convert(x, y); /*p.debug();*/
        if (p.screenX == -1 || p.screenY == -1) return;
        mMousePointPath.lineTo(p.screenX, p.screenY);
    }

    private void onRemoteMouseUp(float x, float y) {
        if (mIsMouseClicked == false) {
            Logger.error("Not mouse clicked");
            return;
        }

        if (mMousePointPath.isEmpty() == true) {
            Logger.error("Empty mouse path");
            return;
        }

        long mouseUpTime = System.currentTimeMillis();
        long mousePressDuration = mouseUpTime - mMouseDownTime;
        if (mousePressDuration < ViewConfiguration.getLongPressTimeout() /* 500ms */) mousePressDuration = mousePressDuration / 2;
        long duration = Math.min(mousePressDuration, ViewConfiguration.getLongPressTimeout());

        PointConverter.POINT p = mPointConverter.convert(x, y); /*p.debug();*/
        if (p.screenX != -1 && p.screenY != -1) mMousePointPath.lineTo(p.screenX, p.screenY);

        boolean result = dispatchSwipeEvent(mMousePointPath, duration);
        Logger.debug("Dispatch result = " + result);

        mMousePointPath.reset();
        mIsMouseClicked = false;
    }

    private void onRemoteBackButton() {
        boolean result = performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
        Logger.debug("Dispatch result = " + result);
    }

    private boolean dispatchSwipeEvent(Path mPath, long duration) {
        if (duration <= 0) return false;
        GestureDescription.Builder builder = new GestureDescription.Builder();
        builder.addStroke(new GestureDescription.StrokeDescription(mPath, 10L, duration));
        GestureDescription gestureDescription = builder.build();

        return dispatchGesture(gestureDescription, new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                /*Logger.print("Dispatch completed!");*/
                super.onCompleted(gestureDescription);
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                Logger.error("Dispatch cancelled!!");
                super.onCancelled(gestureDescription);
            }
        }, null);
    }

    private boolean scrollView(AccessibilityNodeInfo nodeInfo, int deltaY) {
        if (nodeInfo == null || deltaY == 0) return false;

        if (nodeInfo.isScrollable()) {
            int action = (deltaY > 0) ? AccessibilityNodeInfo.ACTION_SCROLL_FORWARD : AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD;
            return nodeInfo.performAction(action);
        }

        for (int i = 0; i < nodeInfo.getChildCount(); i++)
            if (scrollView(nodeInfo.getChild(i), deltaY)) return true;

        return false;
    }

    private void handleDisplayRotation(String displayOrientation) {
        mCurrentTVOrientation = displayOrientation;
        Logger.debug("handleDisplayRotation (displayOrientation=%s)", displayOrientation);

        if (mPointConverter != null) {
            if ("portrait".equals(mCurrentTVOrientation)) mPointConverter.update(ScreenMirroringConfig.Video.DEFAULT_HEIGHT, ScreenMirroringConfig.Video.DEFAULT_WIDTH);
            else mPointConverter.update(ScreenMirroringConfig.Video.DEFAULT_WIDTH, ScreenMirroringConfig.Video.DEFAULT_HEIGHT);
        }
    }
}
