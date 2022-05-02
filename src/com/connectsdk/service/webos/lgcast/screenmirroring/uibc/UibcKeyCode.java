/*
 * Copyright (c) 2020 LG Electronics Inc.
 * SPDX-License-Identifier: LicenseRef-LGE-Proprietary
 */
package com.connectsdk.service.webos.lgcast.screenmirroring.uibc;

import android.view.KeyEvent;
import java.util.HashMap;

public class UibcKeyCode {
    public static final int TV_KEYCODE_0 = 0x30;
    public static final int TV_KEYCODE_1 = 0x31;
    public static final int TV_KEYCODE_2 = 0x32;
    public static final int TV_KEYCODE_3 = 0x33;
    public static final int TV_KEYCODE_4 = 0x34;
    public static final int TV_KEYCODE_5 = 0x35;
    public static final int TV_KEYCODE_6 = 0x36;
    public static final int TV_KEYCODE_7 = 0x37;
    public static final int TV_KEYCODE_8 = 0x38;
    public static final int TV_KEYCODE_9 = 0x39;
    //public static final int TV_KEYCODE_CHANNEL_UP = 0x21;
    //public static final int TV_KEYCODE_CHANNEL_DOWN = 0x22;
    public static final int TV_KEYCODE_LEFT = 0x25;
    public static final int TV_KEYCODE_UP = 0x26;
    public static final int TV_KEYCODE_RIGHT = 0x27;
    public static final int TV_KEYCODE_DOWN = 0x28;
    //public static final int TV_KEYCODE_OK = 0x0D;
    public static final int TV_KEYCODE_STOP = 0x19D;
    public static final int TV_KEYCODE_REWIND = 0x19C;
    public static final int TV_KEYCODE_PLAY = 0x19F;
    public static final int TV_KEYCODE_PAUSE = 0x13;
    public static final int TV_KEYCODE_FORWARD = 0x1A1;
    //public static final int TV_KEYCODE_RED = 0x193;
    //public static final int TV_KEYCODE_GREEN = 0x194;
    //public static final int TV_KEYCODE_YELLOW = 0x195;
    //public static final int TV_KEYCODE_BLUE = 0x196;
    public static final int TV_KEYCODE_BACK = 0x1CD;

    private static final HashMap<Integer, Integer> mKeycodeMap = new HashMap<>();

    static {
        mKeycodeMap.put(TV_KEYCODE_0, KeyEvent.KEYCODE_0);
        mKeycodeMap.put(TV_KEYCODE_1, KeyEvent.KEYCODE_1);
        mKeycodeMap.put(TV_KEYCODE_2, KeyEvent.KEYCODE_2);
        mKeycodeMap.put(TV_KEYCODE_3, KeyEvent.KEYCODE_3);
        mKeycodeMap.put(TV_KEYCODE_4, KeyEvent.KEYCODE_4);
        mKeycodeMap.put(TV_KEYCODE_5, KeyEvent.KEYCODE_5);
        mKeycodeMap.put(TV_KEYCODE_6, KeyEvent.KEYCODE_6);
        mKeycodeMap.put(TV_KEYCODE_7, KeyEvent.KEYCODE_7);
        mKeycodeMap.put(TV_KEYCODE_8, KeyEvent.KEYCODE_8);
        mKeycodeMap.put(TV_KEYCODE_9, KeyEvent.KEYCODE_9);
        //mKeycodeMap.put(TV_KEYCODE_CHANNEL_UP, KeyEvent.KEYCODE_);
        //mKeycodeMap.put(TV_KEYCODE_CHANNEL_DOWN, KeyEvent.KEYCODE_);
        mKeycodeMap.put(TV_KEYCODE_LEFT, KeyEvent.KEYCODE_DPAD_LEFT);
        mKeycodeMap.put(TV_KEYCODE_UP, KeyEvent.KEYCODE_DPAD_UP);
        mKeycodeMap.put(TV_KEYCODE_RIGHT, KeyEvent.KEYCODE_DPAD_RIGHT);
        mKeycodeMap.put(TV_KEYCODE_DOWN, KeyEvent.KEYCODE_DPAD_DOWN);
        //mKeycodeMap.put(TV_KEYCODE_OK, KeyEvent.KEYCODE_);
        mKeycodeMap.put(TV_KEYCODE_STOP, KeyEvent.KEYCODE_MEDIA_STOP);
        mKeycodeMap.put(TV_KEYCODE_REWIND, KeyEvent.KEYCODE_MEDIA_REWIND);
        mKeycodeMap.put(TV_KEYCODE_PLAY, KeyEvent.KEYCODE_MEDIA_PLAY);
        mKeycodeMap.put(TV_KEYCODE_PAUSE, KeyEvent.KEYCODE_MEDIA_PAUSE);
        mKeycodeMap.put(TV_KEYCODE_FORWARD, KeyEvent.KEYCODE_MEDIA_FAST_FORWARD);
        //mKeycodeMap.put(TV_KEYCODE_RED, KeyEvent.KEYCODE_);
        //mKeycodeMap.put(TV_KEYCODE_GREEN, KeyEvent.KEYCODE_);
        //mKeycodeMap.put(TV_KEYCODE_YELLOW, KeyEvent.KEYCODE_);
        //mKeycodeMap.put(TV_KEYCODE_BLUE, KeyEvent.KEYCODE_);
        mKeycodeMap.put(TV_KEYCODE_BACK, KeyEvent.KEYCODE_BACK);
    }

    public static int getSystemKeyCode(int tvKeyCode) {
        return mKeycodeMap.getOrDefault(tvKeyCode, -1);
    }
}
