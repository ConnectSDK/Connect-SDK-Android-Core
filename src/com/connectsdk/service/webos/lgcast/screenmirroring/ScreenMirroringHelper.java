/*
 * ScreenMirroringHelper
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

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import com.lge.lgcast.common.utils.AppUtil;
import com.lge.lgcast.screenmirroring.service.MirroringService;

public class ScreenMirroringHelper {
    public static boolean isOsCompatible() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
    }

    public static boolean isRunning(Context context) {
        ActivityManager.RunningServiceInfo serviceInfo = AppUtil.getServiceInfo(context, MirroringService.class.getName());
        return (serviceInfo != null) ? serviceInfo.foreground : false;
    }
}
