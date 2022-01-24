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

package com.connectsdk.service.lgcast.remotecamera;

import android.app.ActivityManager;
import android.content.Context;
import android.view.Surface;
import com.lge.lgcast.remotecamera.service.RemoteCameraService;
import com.lge.lgcast.remotecamera.service.RemoteCameraServiceIF;
import com.lge.lgcast.common.utils.AppUtil;

public class RemoteCameraApi {
    private Context mContext;

    public RemoteCameraApi(Context context) {
        mContext = context;
    }

    public void startRemoteCamera(Surface previewSurface) {
        RemoteCameraServiceIF.requestStart(mContext, previewSurface);
    }

    public void stopRemoteCamera() {
        RemoteCameraServiceIF.requestStop(mContext);
    }

    public boolean isRunning() {
        ActivityManager.RunningServiceInfo serviceInfo = AppUtil.getServiceInfo(mContext, RemoteCameraService.class.getName());
        return (serviceInfo != null) ? serviceInfo.foreground : false;
    }
}
