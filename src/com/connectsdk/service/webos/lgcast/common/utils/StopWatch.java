/*
 * Copyright (c) 2020 LG Electronics Inc.
 * SPDX-License-Identifier: LicenseRef-LGE-Proprietary
 */
package com.connectsdk.service.webos.lgcast.common.utils;

public class StopWatch {
    private long started = 0;

    private StopWatch() {
    }

    public static StopWatch start() {
        StopWatch watch = new StopWatch();
        watch.started = System.currentTimeMillis();
        return watch;
    }

    public long reset() {
        long elapsed = System.currentTimeMillis() - started;
        started = System.currentTimeMillis();
        return elapsed;
    }

    public long stop() {
        return System.currentTimeMillis() - started;
    }
}
