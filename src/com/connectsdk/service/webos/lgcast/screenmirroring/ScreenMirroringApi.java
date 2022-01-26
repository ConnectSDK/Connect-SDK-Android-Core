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
package com.connectsdk.service.webos.lgcast.screenmirroring;

import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.view.Display;
import com.connectsdk.service.capability.LGCastControl;
import com.connectsdk.service.command.ServiceCommandError;
import com.lge.lgcast.common.utils.LocalBroadcastUtil;
import com.lge.lgcast.common.utils.Logger;
import com.lge.lgcast.screenmirroring.service.MirroringServiceError;
import com.lge.lgcast.screenmirroring.service.MirroringServiceIF;
import java.lang.reflect.Constructor;

public class ScreenMirroringApi {
    private DisplayManager.DisplayListener mDisplayListener;
    private SecondScreen mSecondScreen;

    private ScreenMirroringApi() {
    }

    private static class LazyHolder {
        private static final ScreenMirroringApi INSTANCE = new ScreenMirroringApi();
    }

    public static ScreenMirroringApi getInstance() {
        return LazyHolder.INSTANCE;
    }

    public void startMirroring(Context context, Intent projectionData, String address, Class secondScreenClass, LGCastControl.ScreenMirroringStartListener listener) {
        LocalBroadcastUtil.registerOneTimeReceiver(context, MirroringServiceIF.ACTION_NOTIFY_PAIRING, intent -> {
            if (listener != null) listener.onPairing();
        });

        LocalBroadcastUtil.registerOneTimeReceiver(context, MirroringServiceIF.ACTION_START_RESPONSE, intent -> {
            if (listener == null) return;
            boolean result = intent.getBooleanExtra(MirroringServiceIF.EXTRA_RESULT, false);
            if (result) listener.onSuccess(mSecondScreen);
            else listener.onError(new ServiceCommandError(LGCastControl.SCREEN_MIRRORING_ERROR_GENERIC, "Failed to start mirroring"));
        });

        LocalBroadcastUtil.registerOneTimeReceiver(context, MirroringServiceIF.ACTION_NOTIFY_ERROR, intent -> {
            if (listener == null) return;
            MirroringServiceError serviceError = (MirroringServiceError) intent.getSerializableExtra(MirroringServiceIF.EXTRA_ERROR);
            if (serviceError == MirroringServiceError.ERROR_CONNECTION_CLOSED) listener.onError(new ServiceCommandError(LGCastControl.SCREEN_MIRRORING_ERROR_CONNECTION_CLOSED, "Connection closed"));
            else if (serviceError == MirroringServiceError.ERROR_DEVICE_SHUTDOWN) listener.onError(new ServiceCommandError(LGCastControl.SCREEN_MIRRORING_ERROR_DEVICE_SHUTDOWN, "Device shutdown"));
            else if (serviceError == MirroringServiceError.ERROR_RENDERER_TERMINATED) listener.onError(new ServiceCommandError(LGCastControl.SCREEN_MIRRORING_ERROR_RENDERER_TERMINATED, "Renderer terminated"));
            else if (serviceError == MirroringServiceError.ERROR_STOPPED_BY_NOTIFICATION) listener.onError(new ServiceCommandError(LGCastControl.SCREEN_MIRRORING_ERROR_STOPPED_BY_NOTIFICATION, "User stopped mirroring by clicking notification"));
            else listener.onError(new ServiceCommandError(LGCastControl.SCREEN_MIRRORING_ERROR_GENERIC, "Unknown error"));
        });

        if (projectionData == null || address == null) {
            Logger.error("Invalid arguments (projectionData=%s, address=%s)", projectionData, address);
            MirroringServiceIF.respondStart(context, false, false);
            return;
        }

        if (ScreenMirroringHelper.isRunning(context) == true) {
            Logger.error("Mirroring is ALREADY running.");
            MirroringServiceIF.respondStart(context, true, secondScreenClass != null);
            return;
        }

        if (secondScreenClass != null) {
            mDisplayListener = createDisplayListener(context, secondScreenClass);
            getDisplayManager(context).registerDisplayListener(mDisplayListener, null);
        }

        Logger.debug("Request start");
        MirroringServiceIF.requestStart(context, projectionData, address, secondScreenClass != null);
    }

    public void stopMirroring(Context context, LGCastControl.ScreenMirroringStopListener listener) {
        Logger.print("stopMirroring");

        LocalBroadcastUtil.registerOneTimeReceiver(context, MirroringServiceIF.ACTION_STOP_RESPONSE, intent -> {
            if (listener != null) listener.onSuccess("stopMirroring Success");
        });

        if (ScreenMirroringHelper.isRunning(context) == false) {
            Logger.error("Mirroring is NOT running.");
            MirroringServiceIF.respondStop(context);
            return;
        }

        if (mDisplayListener != null) getDisplayManager(context).unregisterDisplayListener(mDisplayListener);
        mDisplayListener = null;

        if (mSecondScreen != null) mSecondScreen.dismiss();
        mSecondScreen = null;

        Logger.debug("Request stop");
        MirroringServiceIF.requestStop(context);
    }

    private DisplayManager getDisplayManager(Context context) {
        return (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
    }

    private DisplayManager.DisplayListener createDisplayListener(Context context, Class secondScreenClass) {
        return new DisplayManager.DisplayListener() {
            @Override
            public void onDisplayAdded(int id) {
                Logger.debug("onDisplayAdded (id=%d)", id);
                mSecondScreen = createSecondScreenInstance(context, secondScreenClass, getDisplayManager(context).getDisplay(id));
                if (mSecondScreen != null) mSecondScreen.show();
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

    private SecondScreen createSecondScreenInstance(Context context, Class secondScreenClass, Display display) {
        try {
            if (secondScreenClass == null) throw new Exception("Invalid class");
            if (display == null) throw new Exception("Invalid display");

            Constructor constructor = secondScreenClass.getConstructor(new Class[]{Context.class, Display.class});
            return (SecondScreen) constructor.newInstance(context, display);
        } catch (Exception e) {
            Logger.error(e);
            return null;
        }
    }
}
