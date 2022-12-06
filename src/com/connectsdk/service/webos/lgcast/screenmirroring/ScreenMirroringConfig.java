/*
 * Copyright (c) 2020 LG Electronics Inc.
 * SPDX-License-Identifier: LicenseRef-LGE-Proprietary
 */
package com.connectsdk.service.webos.lgcast.screenmirroring;

import android.graphics.Point;

public class ScreenMirroringConfig {
    public static class Video {
        public static final String CODEC = "H264";
        public static final int CLOCK_RATE = 90000;
        public static final int FRAMERATE = 60;
        public static final int BITRATE_1_5MB = (int) (1.5 * 1024 * 1024);
        public static final int BITRATE_3_0MB = (3 * 1024 * 1024);
        public static final int BITRATE_6_0MB = (6 * 1024 * 1024);

        public static final int DEFAULT_WIDTH = 1920;
        public static final int DEFAULT_HEIGHT = 1080;

        public static final Point CAPTURE_SIZE_720P = new Point(1280, 720);
        public static final Point CAPTURE_SIZE_1080P = new Point(1920, 1080);

        public static final String DISPLAY_NAME = "LGCastVirtualDisplay";
    }

    public static class Audio {
        public static final String CODEC = "AAC";
        public static final int SAMPLING_RATE = 48000;
        public static final int CHANNEL_COUNT = 2; // Stereo
        public static final String STREAM_MUX_CONFIG = "40002320"; // 48000, Stereo
    }

    public static class RTP {
        public static final long SSRC = 1356955624;

        public static final byte[] FIXED_KEY = new byte[]{
                (byte) 0x01, (byte) 0x23, (byte) 0x45, (byte) 0x67, (byte) 0x89, (byte) 0x01, (byte) 0x23, (byte) 0x45,
                (byte) 0x67, (byte) 0x89, (byte) 0x01, (byte) 0x23, (byte) 0x45, (byte) 0x67, (byte) 0x89, (byte) 0x01,
                (byte) 0x23, (byte) 0x45, (byte) 0x67, (byte) 0x89, (byte) 0x01, (byte) 0x23, (byte) 0x45, (byte) 0x67,
                (byte) 0x89, (byte) 0x01, (byte) 0x23, (byte) 0x45, (byte) 0x67, (byte) 0x89
        };
    }

    public static class Notification {
        public static final int ID = 0x1000;
        public static final String CHANNEL_ID = "LG_CAST_SCREEN_MIRRORING";
        public static final String CHANNEL_NAME = "LG Cast Screen Mirroring";
    }

    public static class Test {
        public static final boolean usePcPlayer = false; // false in release version
        public static final String pcIpAddress = "10.0.0.5";
        public static final int pcVideoUdpPort = 5000;
        public static final int pcAudioUdpPort = 5002;

        public static final boolean testMkiUpdate = false; // false in release version
        public static final boolean testOrientationChange = false; // false in release version
        public static final String displayOrientation = null; // null in release version. (null | portrait | landscape)
    }
}
