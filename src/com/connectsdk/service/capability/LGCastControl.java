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

import android.content.Context;
import android.content.Intent;
import com.connectsdk.service.capability.listeners.ResponseListener;
import com.connectsdk.service.lgcast.screenmirroring.SecondScreen;

public interface LGCastControl extends CapabilityMethods {
    String Any = "LGCast.Any";
    String ScreenMirroring = "LGCast.ScreenMirroring";
    String RemoteCamera = "LGCast.RemoteCamera";

    String[] Capabilities = {
            ScreenMirroring,
            RemoteCamera,
    };

    int SCREEN_MIRRORING_ERROR_GENERIC = 0;
    int SCREEN_MIRRORING_ERROR_CONNECTION_CLOSED = 1;
    int SCREEN_MIRRORING_ERROR_DEVICE_SHUTDOWN = 2;
    int SCREEN_MIRRORING_ERROR_RENDERER_TERMINATED = 3;
    int SCREEN_MIRRORING_ERROR_STOPPED_BY_NOTIFICATION = 4;

    void startScreenMirroring(Context context, Intent projectionData, ScreenMirroringStartListener listener);
    void startScreenMirroring(Context context, Intent projectionData, Class secondScreenClass, ScreenMirroringStartListener listener);
    void stopScreenMirroring(Context context, ScreenMirroringStopListener listener);

    void startRemoteCamera();
    void stopRemoteCamera();

    interface ScreenMirroringStartListener extends ResponseListener<SecondScreen> {
        void onPairing();
    }

    interface ScreenMirroringStopListener extends ResponseListener<String> {
    }
}
