/*
 * Copyright (c) 2020 LG Electronics Inc.
 * SPDX-License-Identifier: LicenseRef-LGE-Proprietary
 */
package com.connectsdk.service.webos.lgcast.screenmirroring.uibc;

import android.app.Activity;
import android.app.Application;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import com.connectsdk.service.webos.lgcast.common.utils.AppUtil;
import com.connectsdk.service.webos.lgcast.common.utils.HandlerThreadEx;
import com.connectsdk.service.webos.lgcast.common.utils.Logger;
import com.connectsdk.service.webos.lgcast.screenmirroring.ScreenMirroringConfig;
import org.json.JSONObject;

public class UibcAppEventDispatcher {
    private Application mApplication;
    private Activity mCurrentActivity;
    private PointConverter mPointConverter;
    private static HandlerThreadEx mServiceHandler;

    private int mLeftMargin = 0;
    private int mTopMargin = 0;

    public UibcAppEventDispatcher(Application application) {
        if (application == null) throw new IllegalStateException("Invalid application");
        mApplication = application;
    }

    public void create() {
        Logger.print("create");
        mApplication.registerActivityLifecycleCallbacks(mActivityLifecycleCallbacks);

        mCurrentActivity = AppUtil.getCurrentActivity();
        mPointConverter = new PointConverter(mApplication, ScreenMirroringConfig.Video.DEFAULT_WIDTH, ScreenMirroringConfig.Video.DEFAULT_HEIGHT);

        int orientation = AppUtil.getActivityOrientation(mCurrentActivity);
        updateScreenMargin(orientation);

        mServiceHandler = new HandlerThreadEx("UibcActivityDispatcher Handler");
        mServiceHandler.start(msg -> handleUibcInfo((JSONObject) msg.obj));
    }

    public void destroy() {
        Logger.print("destroy");
        mApplication.unregisterActivityLifecycleCallbacks(mActivityLifecycleCallbacks);

        if (mServiceHandler != null) mServiceHandler.quit();
        mServiceHandler = null;
    }

    public void updateScreenMargin(int orientation) {
        mPointConverter.update(ScreenMirroringConfig.Video.DEFAULT_WIDTH, ScreenMirroringConfig.Video.DEFAULT_HEIGHT);
        mLeftMargin = 0;
        mTopMargin = 0;

        Rect rect = AppUtil.getWindowVisibleDisplayFrame(mCurrentActivity);
        if (rect != null && orientation == Configuration.ORIENTATION_LANDSCAPE) mLeftMargin = rect.left;
        else if (rect != null && orientation == Configuration.ORIENTATION_PORTRAIT) mTopMargin = rect.top;
    }

    public static void sendUibcInfo(JSONObject uibcInfo) {
        if (mServiceHandler != null) mServiceHandler.sendMessage(uibcInfo);
        else Logger.error("Invalid handler");
    }

    private void handleUibcInfo(JSONObject uibcInfo) {
        /*Logger.debug("uibcInfo: " + uibcInfo);*/
        String type = (uibcInfo != null) ? uibcInfo.optString("type", "none") : "none";

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
        int systemKeyCode = UibcKeyCode.getSystemKeyCode(keyCode);
        int metaState = shiftKey ? KeyEvent.META_SHIFT_ON : 0;
        KeyEvent downKey = new KeyEvent(0L, 0L, KeyEvent.ACTION_DOWN, systemKeyCode, 0, metaState);
        KeyEvent upKey = new KeyEvent(0L, 0L, KeyEvent.ACTION_UP, systemKeyCode, 0, metaState);
        if (type.equals("keydown") == true) dispatchKeyEvent(downKey);
        else if (type.equals("keyup") == true) dispatchKeyEvent(upKey);
    }

    private void handleWheelEvent(String type, int deltaY, int screenX, int screenY) {
        MotionEvent.PointerCoords coord = new MotionEvent.PointerCoords();
        coord.x = screenX;
        coord.y = screenY;
        coord.setAxisValue(MotionEvent.AXIS_VSCROLL, -1 * deltaY);
        MotionEvent.PointerCoords[] coords = {coord};

        MotionEvent.PointerProperties properties = new MotionEvent.PointerProperties();
        properties.id = 0;
        MotionEvent.PointerProperties[] prop = {properties};

        MotionEvent scrollEvent = MotionEvent.obtain(0L, 0L, MotionEvent.ACTION_SCROLL, 1, prop, coords, 0, 0, 1f, 1f, 0, 0, InputDevice.SOURCE_CLASS_POINTER, 0);
        dispatchGenericMotionEvent(scrollEvent);
    }

    private void onRemoteMouseDown(float x, float y) {
        PointConverter.POINT p = mPointConverter.convert(x, y); /*p.debug();*/
        if (p.screenX == -1 || p.screenY == -1) return;

        MotionEvent event = MotionEvent.obtain(0L, 0L, MotionEvent.ACTION_DOWN, p.screenX - mLeftMargin, p.screenY - mTopMargin, 0);
        dispatchTouchEvent(event);
    }

    private void onRemoteMouseMove(float x, float y) {
        PointConverter.POINT p = mPointConverter.convert(x, y); /*p.debug();*/
        if (p.screenX == -1 || p.screenY == -1) return;

        MotionEvent event = MotionEvent.obtain(0L, 0L, MotionEvent.ACTION_MOVE, p.screenX - mLeftMargin, p.screenY - mTopMargin, 0);
        dispatchTouchEvent(event);
    }

    private void onRemoteMouseUp(float x, float y) {
        PointConverter.POINT p = mPointConverter.convert(x, y); /*p.debug();*/
        if (p.screenX == -1 || p.screenY == -1) return;

        MotionEvent event = MotionEvent.obtain(0L, 0L, MotionEvent.ACTION_UP, p.screenX - mLeftMargin, p.screenY - mTopMargin, 0);
        dispatchTouchEvent(event);
    }

    private void dispatchTouchEvent(MotionEvent motionEvent) {
        if (mCurrentActivity != null) mCurrentActivity.runOnUiThread(() -> {
            if (mCurrentActivity != null) mCurrentActivity.dispatchTouchEvent(motionEvent);
        });
    }

    private void dispatchKeyEvent(KeyEvent keyEvent) {
        if (mCurrentActivity != null) mCurrentActivity.runOnUiThread(() -> {
            if (mCurrentActivity != null) mCurrentActivity.dispatchKeyEvent(keyEvent);
        });
    }

    private void dispatchGenericMotionEvent(MotionEvent motionEvent) {
        if (mCurrentActivity != null) mCurrentActivity.runOnUiThread(() -> {
            if (mCurrentActivity != null) mCurrentActivity.dispatchGenericMotionEvent(motionEvent);
        });
    }

    private Application.ActivityLifecycleCallbacks mActivityLifecycleCallbacks = new Application.ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(Activity activity, Bundle bundle) {
            Logger.debug("Activity created: " + activity.getClass().getSimpleName());
        }

        @Override
        public void onActivityStarted(Activity activity) {
            Logger.debug("Activity started: " + activity.getClass().getSimpleName());
        }

        @Override
        public void onActivityResumed(Activity activity) {
            Logger.debug("Activity resumed: " + activity.getClass().getSimpleName());
            mCurrentActivity = activity;
        }

        @Override
        public void onActivityPaused(Activity activity) {
            Logger.debug("Activity paused: " + activity.getClass().getSimpleName());
            mCurrentActivity = null;
        }

        @Override
        public void onActivityStopped(Activity activity) {
            Logger.debug("Activity stopped: " + activity.getClass().getSimpleName());
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            Logger.debug("Activity destroyed: " + activity.getClass().getSimpleName());
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
        }
    };
}
