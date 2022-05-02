/*
 * Copyright (c) 2020 LG Electronics Inc.
 * SPDX-License-Identifier: LicenseRef-LGE-Proprietary
 */
package com.connectsdk.service.webos.lgcast.common.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;

public class DeviceUtil {
    public static int getProcessorBits() {
        if (Build.SUPPORTED_64_BIT_ABIS != null && Build.SUPPORTED_64_BIT_ABIS.length > 0) return 64;
        if (Build.SUPPORTED_32_BIT_ABIS != null && Build.SUPPORTED_32_BIT_ABIS.length > 0) return 32;
        return 0;
    }

    public static long getTotalMemorySpace(Context context) {
        if (context == null) return 0;
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        return mi.totalMem;
    }

    public static long getFreeMemorySpace(Context context) {
        if (context == null) return 0;
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        return mi.availMem;
    }
}
