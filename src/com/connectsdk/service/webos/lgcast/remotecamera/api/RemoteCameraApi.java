/*
 * RemoteCameraApi
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
package com.connectsdk.service.webos.lgcast.remotecamera.api;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.view.Surface;
import androidx.core.app.ActivityCompat;
import com.connectsdk.service.capability.RemoteCameraControl;
import com.connectsdk.service.capability.RemoteCameraControl.RemoteCameraError;
import com.connectsdk.service.capability.RemoteCameraControl.RemoteCameraErrorListener;
import com.connectsdk.service.capability.RemoteCameraControl.RemoteCameraPlayingListener;
import com.connectsdk.service.capability.RemoteCameraControl.RemoteCameraProperty;
import com.connectsdk.service.capability.RemoteCameraControl.RemoteCameraPropertyChangeListener;
import com.connectsdk.service.capability.RemoteCameraControl.RemoteCameraStartListener;
import com.connectsdk.service.capability.RemoteCameraControl.RemoteCameraStopListener;
import com.connectsdk.service.webos.lgcast.common.utils.LocalBroadcastEx;
import com.connectsdk.service.webos.lgcast.common.utils.Logger;
import com.connectsdk.service.webos.lgcast.remotecamera.service.CameraServiceError;
import com.connectsdk.service.webos.lgcast.remotecamera.service.CameraServiceIF;

public class RemoteCameraApi {
    private LocalBroadcastEx mLocalBroadcastEx = new LocalBroadcastEx();

    private RemoteCameraApi() {
    }

    private static class LazyHolder {
        private static final RemoteCameraApi INSTANCE = new RemoteCameraApi();
    }

    public static RemoteCameraApi getInstance() {
        return LazyHolder.INSTANCE;
    }

    public void startRemoteCamera(Context context, Surface previewSurface, String deviceIpAddress, boolean micMute, int lensFacing, RemoteCameraStartListener startListener) {
        Logger.print("startRemoteCamera");

        try {
            if (context == null || deviceIpAddress == null) throw new Exception("Invalid arguments");
            if (RemoteCameraControl.isCompatibleOsVersion() == false) throw new Exception("Incompatible OS version");
            if (RemoteCameraControl.isRunning(context) == true) throw new Exception("Remote Camera is ALREADY running");

            boolean hasCameraPermission = ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
            if (hasCameraPermission == false) throw new Exception("Invalid camera permission");

            boolean hasAudioPermission = ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
            if (hasAudioPermission == false) throw new Exception("Invalid audio permission");

            mLocalBroadcastEx.registerOnce(context, CameraServiceIF.ACTION_NOTIFY_PAIRING, intent -> {
                if (startListener != null) startListener.onPairing();
            });

            mLocalBroadcastEx.registerOnce(context, CameraServiceIF.ACTION_START_RESPONSE, intent -> {
                boolean result = intent.getBooleanExtra(CameraServiceIF.EXTRA_RESULT, false);
                if (startListener != null) startListener.onStart(result);
            });

            Logger.debug("Request start");
            CameraServiceIF.requestStart(context, previewSurface, deviceIpAddress, micMute, lensFacing);
        } catch (Exception e) {
            if (startListener != null) startListener.onStart(false);
        }
    }

    public void stopRemoteCamera(Context context, RemoteCameraStopListener stopListener) {
        Logger.print("stopRemoteCamera");

        try {
            if (context == null) throw new Exception("Invalid arguments");
            if (RemoteCameraControl.isRunning(context) == false) throw new Exception("Remote Camera is NOT running");

            mLocalBroadcastEx.registerOnce(context, CameraServiceIF.ACTION_STOP_RESPONSE, intent -> {
                boolean result = intent.getBooleanExtra(CameraServiceIF.EXTRA_RESULT, false);
                if (stopListener != null) stopListener.onStop(result);
                mLocalBroadcastEx.unregisterAll(context);
            });

            Logger.debug("Request stop");
            CameraServiceIF.requestStop(context);
        } catch (Exception e) {
            if (stopListener != null) stopListener.onStop(false);
        }
    }

    public void setMicMute(Context context, boolean micMute) {
        Logger.print("setMicMute (micMute=%s)", micMute);
        if (RemoteCameraControl.isRunning(context) == true) CameraServiceIF.setMicMute(context, micMute);
        else Logger.error("Remote camera is NOT running");
    }

    public void setLensFacing(Context context, int lensFacing) {
        Logger.print("setLensFacing (lensFacing=%d)", lensFacing);
        if (RemoteCameraControl.isRunning(context) == true) CameraServiceIF.setLensFacing(context, lensFacing);
        else Logger.error("Remote camera is NOT running");
    }

    public void setCameraPlayingListener(Context context, RemoteCameraPlayingListener playingListener) {
        mLocalBroadcastEx.registerOnce(context, CameraServiceIF.ACTION_NOTIFY_PLAYING, intent -> {
            if (playingListener != null) playingListener.onPlaying();
        });
    }

    public void setPropertyChangeListener(Context context, RemoteCameraPropertyChangeListener propertyChangeListener) {
        mLocalBroadcastEx.register(context, CameraServiceIF.ACTION_NOTIFY_PROPERTY_CHANGE, intent -> {
            RemoteCameraProperty property = (RemoteCameraProperty) intent.getSerializableExtra(CameraServiceIF.EXTRA_PROPERTY);
            if (propertyChangeListener != null) propertyChangeListener.onChange(property);
        });
    }

    public void setErrorListener(Context context, RemoteCameraErrorListener errorListener) {
        mLocalBroadcastEx.register(context, CameraServiceIF.ACTION_NOTIFY_ERROR, intent -> {
            CameraServiceError serviceError = (CameraServiceError) intent.getSerializableExtra(CameraServiceIF.EXTRA_ERROR);
            if (errorListener != null) errorListener.onError(toRemoteCameraError(serviceError));
        });
    }

    private RemoteCameraError toRemoteCameraError(CameraServiceError serviceError) {
        if (serviceError == CameraServiceError.ERROR_CONNECTION_CLOSED) return RemoteCameraError.ERROR_CONNECTION_CLOSED;
        if (serviceError == CameraServiceError.ERROR_DEVICE_SHUTDOWN) return RemoteCameraError.ERROR_DEVICE_SHUTDOWN;
        if (serviceError == CameraServiceError.ERROR_RENDERER_TERMINATED) return RemoteCameraError.ERROR_RENDERER_TERMINATED;
        if (serviceError == CameraServiceError.ERROR_STOPPED_BY_NOTIFICATION) return RemoteCameraError.ERROR_STOPPED_BY_NOTIFICATION;
        return RemoteCameraError.ERROR_GENERIC;
    }
}