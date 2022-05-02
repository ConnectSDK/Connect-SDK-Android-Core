/*
 * Copyright (c) 2020 LG Electronics Inc.
 * SPDX-License-Identifier: LicenseRef-LGE-Proprietary
 */
package com.connectsdk.service.webos.lgcast.common.utils;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ThreadWait<E> {
    private class WaitResult {
        public E result;

        public WaitResult(E result) {
            this.result = result;
        }
    }

    private BlockingQueue<WaitResult> mBlockingQueue;
    private AtomicBoolean mIsWaiting;

    public ThreadWait() {
        mBlockingQueue = new LinkedBlockingQueue<>();
        mIsWaiting = new AtomicBoolean(false);
    }

    public boolean isWaiting() {
        return mIsWaiting.get();
    }

    public E waitFor(E fallback) {
        return waitFor(Long.MAX_VALUE, fallback);
    }

    public E waitFor(long timeout, E fallback) {
        try {
            mIsWaiting.set(true);
            mBlockingQueue.clear();

            WaitResult waitResult = mBlockingQueue.poll(timeout, TimeUnit.MILLISECONDS);
            return (waitResult != null) ? waitResult.result : fallback;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return fallback;
        } finally {
            mIsWaiting.set(false);
        }
    }

    public void wakeUp(E result) {
        try {
            mBlockingQueue.put(new WaitResult(result));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
