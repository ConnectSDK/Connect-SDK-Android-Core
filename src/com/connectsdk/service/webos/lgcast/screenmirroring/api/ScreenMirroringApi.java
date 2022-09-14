/*
 * ScreenMirroringApi
 * Connect SDK
 *
 * Copyright (c) 2020 LG Electronics.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.connectsdk.service.webos.lgcast.screenmirroring.api;

import android.app.Presentation;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.view.Display;
import com.connectsdk.service.capability.ScreenMirroringControl;
import com.connectsdk.service.capability.ScreenMirroringControl.ScreenMirroringError;
import com.connectsdk.service.capability.ScreenMirroringControl.ScreenMirroringErrorListener;
import com.connectsdk.service.capability.ScreenMirroringControl.ScreenMirroringStartListener;
import com.connectsdk.service.capability.ScreenMirroringControl.ScreenMirroringStopListener;
import com.connectsdk.service.webos.lgcast.common.utils.DeviceUtil;
import com.connectsdk.service.webos.lgcast.common.utils.LocalBroadcastEx;
import com.connectsdk.service.webos.lgcast.common.utils.Logger;
import com.connectsdk.service.webos.lgcast.screenmirroring.ScreenMirroringConfig;
import com.connectsdk.service.webos.lgcast.screenmirroring.service.MirroringServiceError;
import com.connectsdk.service.webos.lgcast.screenmirroring.service.MirroringServiceIF;
import java.lang.reflect.Constructor;

public class ScreenMirroringApi {
    private LocalBroadcastEx mLocalBroadcastEx = new LocalBroadcastEx();
    private DisplayManager.DisplayListener mDisplayListener;
    private Presentation mSecondScreen;

    private ScreenMirroringApi() {
    }

    private static class LazyHolder {
        private static final ScreenMirroringApi INSTANCE = new ScreenMirroringApi();
    }

    public static ScreenMirroringApi getInstance() {
        return LazyHolder.INSTANCE;
    }

    public void startMirroring(Context context, Intent projectionData, String deviceIpAddress, Class secondScreenClass, ScreenMirroringStartListener startListener) {
        Logger.print("startMirroring");

        try {
            if (context == null || projectionData == null || deviceIpAddress == null) throw new Exception("Invalid arguments");
            if (ScreenMirroringControl.isCompatibleOsVersion() == false) throw new Exception("Incompatible OS version");
            if (ScreenMirroringControl.isRunning(context) == true) throw new Exception("Screen Mirroring is ALREADY running");

            mLocalBroadcastEx.registerOnce(context, MirroringServiceIF.ACTION_NOTIFY_PAIRING, intent -> {
                if (startListener != null) startListener.onPairing();
            });

            mLocalBroadcastEx.registerOnce(context, MirroringServiceIF.ACTION_START_RESPONSE, intent -> {
                boolean result = intent.getBooleanExtra(MirroringServiceIF.EXTRA_RESULT, false);
                if (startListener != null) startListener.onStart(result, mSecondScreen);
            });

            if (secondScreenClass != null) {
                mDisplayListener = createDisplayListener(context, secondScreenClass);
                getDisplayManager(context).registerDisplayListener(mDisplayListener, null);
            }

            Logger.debug("Request start");
            MirroringServiceIF.requestStart(context, projectionData, deviceIpAddress, secondScreenClass != null);
        } catch (Exception e) {
            Logger.error(e);
            if (startListener != null) startListener.onStart(false, null);
        }
    }

    public void stopMirroring(Context context, ScreenMirroringStopListener stopListener) {
        Logger.print("stopMirroring");

        try {
            if (context == null) throw new Exception("Invalid arguments");
            if (ScreenMirroringControl.isRunning(context) == false) throw new Exception("Screen Mirroring is NOT running");

            mLocalBroadcastEx.registerOnce(context, MirroringServiceIF.ACTION_STOP_RESPONSE, intent -> {
                boolean result = intent.getBooleanExtra(MirroringServiceIF.EXTRA_RESULT, false);
                if (stopListener != null) stopListener.onStop(result);
                mLocalBroadcastEx.unregisterAll(context);
            });

            if (mDisplayListener != null) getDisplayManager(context).unregisterDisplayListener(mDisplayListener);
            mDisplayListener = null;

            if (mSecondScreen != null) mSecondScreen.dismiss();
            mSecondScreen = null;

            Logger.debug("Request stop");
            MirroringServiceIF.requestStop(context);
        } catch (Exception e) {
            Logger.error(e);
            if (stopListener != null) stopListener.onStop(false);
        }
    }

    public void setErrorListener(Context context, ScreenMirroringErrorListener errorListener) {
        mLocalBroadcastEx.registerOnce(context, MirroringServiceIF.ACTION_NOTIFY_ERROR, intent -> {
            MirroringServiceError serviceError = (MirroringServiceError) intent.getSerializableExtra(MirroringServiceIF.EXTRA_ERROR);
            if (errorListener != null) errorListener.onError(toScreenMirroringError(serviceError));
        });
    }

    private DisplayManager getDisplayManager(Context context) {
        return (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
    }

    private DisplayManager.DisplayListener createDisplayListener(Context context, Class secondScreenClass) {
        return new DisplayManager.DisplayListener() {
            @Override
            public void onDisplayAdded(int id) {
                Logger.debug("onDisplayAdded (id=%d)", id);
                Display display = getDisplayManager(context).getDisplay(id);

                if (display == null || display.getName().equals(ScreenMirroringConfig.Video.DISPLAY_NAME) == false) {
                    Logger.error("Unknown display");
                    return;
                }

                if (secondScreenClass != null) {
                    mSecondScreen = createSecondScreenInstance(context, secondScreenClass, getDisplayManager(context).getDisplay(id));
                    if (mSecondScreen != null) mSecondScreen.show();
                }

                if (mDisplayListener != null) getDisplayManager(context).unregisterDisplayListener(mDisplayListener);
                mDisplayListener = null;
            }

            @Override
            public void onDisplayRemoved(int id) {
                Logger.debug("onDisplayRemoved (id=%d)", id);
            }

            @Override
            public void onDisplayChanged(int id) {
                Logger.debug("onDisplayChanged (id=%d)", id);
            }
        };
    }

    private Presentation createSecondScreenInstance(Context context, Class secondScreenClass, Display display) {
        try {
            if (secondScreenClass == null) throw new Exception("Invalid class");
            if (display == null) throw new Exception("Invalid display");

            Constructor constructor = secondScreenClass.getConstructor(new Class[]{Context.class, Display.class});
            return (Presentation) constructor.newInstance(context, display);
        } catch (Exception e) {
            Logger.error(e);
            return null;
        }
    }

    private ScreenMirroringError toScreenMirroringError(MirroringServiceError serviceError) {
        if (serviceError == MirroringServiceError.ERROR_CONNECTION_CLOSED) return ScreenMirroringError.ERROR_CONNECTION_CLOSED;
        if (serviceError == MirroringServiceError.ERROR_DEVICE_SHUTDOWN) return ScreenMirroringError.ERROR_DEVICE_SHUTDOWN;
        if (serviceError == MirroringServiceError.ERROR_RENDERER_TERMINATED) return ScreenMirroringError.ERROR_RENDERER_TERMINATED;
        if (serviceError == MirroringServiceError.ERROR_STOPPED_BY_NOTIFICATION) return ScreenMirroringError.ERROR_STOPPED_BY_NOTIFICATION;
        return ScreenMirroringError.ERROR_GENERIC;
    }
}
