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
package com.connectsdk.service.capability;

import android.app.ActivityManager;
import android.content.Context;
import android.hardware.camera2.CameraCharacteristics;
import android.os.Build;
import android.view.Surface;
import com.connectsdk.device.ConnectableDevice;
import com.connectsdk.discovery.DiscoveryManager;
import com.connectsdk.service.webos.lgcast.common.utils.AppUtil;
import com.connectsdk.service.webos.lgcast.common.utils.IOUtil;
import com.connectsdk.service.webos.lgcast.common.utils.StringUtil;
import com.connectsdk.service.webos.lgcast.remotecamera.service.CameraService;
import java.util.ArrayList;
import java.util.List;

public interface RemoteCameraControl extends CapabilityMethods {
    String Any = "RemoteCameraControl.Any";
    String RemoteCamera = "RemoteCameraControl.RemoteCamera";
    String[] Capabilities = {RemoteCamera};

    int LENS_FACING_FRONT = CameraCharacteristics.LENS_FACING_FRONT;
    int LENS_FACING_BACK = CameraCharacteristics.LENS_FACING_BACK;

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Static functions
    ///////////////////////////////////////////////////////////////////////////////////////////////
    static int getSdkVersion(Context context) {
        String version = IOUtil.readRawResourceText(context, com.connectsdk.R.raw.lgcast_version);
        return StringUtil.toInteger(version, -1);
    }

    static boolean isCompatibleOsVersion() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
    }

    static boolean isRunning(Context context) {
        ActivityManager.RunningServiceInfo serviceInfo = AppUtil.getServiceInfo(context, CameraService.class.getName());
        return (serviceInfo != null) ? serviceInfo.foreground : false;
    }

    static boolean isSupportRemoteCamera(String deviceId) {
        ConnectableDevice dvc = DiscoveryManager.getInstance().getDeviceById(deviceId);
        List<String> capabilities = (dvc != null) ? dvc.getCapabilities() : new ArrayList<>();
        return capabilities.contains(RemoteCameraControl.RemoteCamera);
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Interfaces
    ///////////////////////////////////////////////////////////////////////////////////////////////
    RemoteCameraControl getRemoteCameraControl();
    void startRemoteCamera(Context context, Surface previewSurface, boolean micMute, int lensFacing, RemoteCameraStartListener startListener);
    void stopRemoteCamera(Context context, RemoteCameraStopListener stopListener);
    void setMicMute(Context context, boolean micMute);
    void setLensFacing(Context context, int lensFacing);
    void setCameraPlayingListener(Context context, RemoteCameraPlayingListener playingListener);
    void setPropertyChangeListener(Context context, RemoteCameraPropertyChangeListener propertyChangeListener);
    void setErrorListener(Context context, RemoteCameraErrorListener errorListener);


    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Listeners
    ///////////////////////////////////////////////////////////////////////////////////////////////
    enum RemoteCameraError {
        ERROR_GENERIC,
        ERROR_CONNECTION_CLOSED,
        ERROR_DEVICE_SHUTDOWN,
        ERROR_RENDERER_TERMINATED,
        ERROR_STOPPED_BY_NOTIFICATION;
    }

    enum RemoteCameraProperty {
        UNKNOWN,
        RESOLUTION,
        LENS_FACING,
        BRIGHTNESS,
        WHITE_BALANCE,
        AUTO_WHITE_BALANCE,
        AUDIO,
    }

    interface RemoteCameraStartListener {
        void onPairing();
        void onStart(boolean result);
    }

    interface RemoteCameraStopListener {
        void onStop(boolean result);
    }

    interface RemoteCameraPlayingListener {
        void onPlaying();
    }

    interface RemoteCameraPropertyChangeListener {
        void onChange(RemoteCameraProperty property);
    }

    interface RemoteCameraErrorListener {
        void onError(RemoteCameraError error);
    }
}
