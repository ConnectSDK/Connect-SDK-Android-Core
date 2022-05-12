/*
 * Copyright (c) 2020 LG Electronics Inc.
 * SPDX-License-Identifier: LicenseRef-LGE-Proprietary
 */
package com.connectsdk.service.webos.lgcast.screenmirroring;

public class ScreenMirroringConfig {
    public static class Video {
        public static final String CODEC = "H264";
        public static final int CLOCK_RATE = 90000;
        public static final int FRAMERATE = 60;
        public static final int BITRATE = 6 * 1024 * 1024;

        public static final int DEFAULT_WIDTH = 1920;
        public static final int DEFAULT_HEIGHT = 1080;

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
    }

    public static class Test {
        public static final boolean usePcPlayer = false; // false in release version
        public static final String pcIpAddress = "172.16.0.9";
        public static final int pcVideoUdpPort = 5000;
        public static final int pcAudioUdpPort = 5002;

        public static final boolean testMkiUpdate = false; // false in release version
        public static final boolean showDebugLog = true; // false in release version
        public static final String displayOrientation = null; // null in release version. values are null, portrait or landscape
        public static final boolean captureByDisplaySize = false; // false in release version
    }
}
