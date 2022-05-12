/*
 * Copyright (c) 2020 LG Electronics Inc.
 * SPDX-License-Identifier: LicenseRef-LGE-Proprietary
 */
package com.connectsdk.service.webos.lgcast.common.utils;

import java.util.Timer;
import java.util.TimerTask;

public class TimerUtil {
    public interface TimerListener {
        void onTime();
    }

    public static Timer schedule(TimerListener listener, long delay) {
        Timer timer = new Timer("TimerUtil");
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (listener != null) listener.onTime();
            }
        }, delay);

        return timer;
    }

    public static Timer schedule(TimerListener listener, long delay, long period) {
        Timer timer = new Timer("TimerUtil");
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (listener != null) listener.onTime();
            }
        }, delay, period);

        return timer;
    }
}
