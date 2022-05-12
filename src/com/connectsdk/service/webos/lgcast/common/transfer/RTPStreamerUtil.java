/*
 * Copyright (c) 2020 LG Electronics Inc.
 * SPDX-License-Identifier: LicenseRef-LGE-Proprietary
 */
package com.connectsdk.service.webos.lgcast.common.transfer;

import com.connectsdk.service.webos.lgcast.common.utils.Logger;

public class RTPStreamerUtil {
    static void parseH264FrameInfo(byte[] data, long pts) {
        // DEBUG H264 Slice Type
        int nalType = (data[4] & 0x1F);
        if (nalType == 1) {
            int pType = (data[5] & 0x40);
            if (pType != 0) {
                Logger.debug("P Slice : pts : " + pts + ", length : " + data.length);
            } else {
                Logger.debug("B Slice : pts : " + pts + ", length : " + data.length);
            }
        } else if (nalType == 5) {
            Logger.debug("I Slice : pts : " + pts + ", length : " + data.length);
        } else if (nalType == 7) {
            Logger.debug("SPS/PPS Slice : pts : " + pts + ", length : " + data.length);
        } else {
            Logger.debug("Unknown Slice : pts : " + pts + ", length : " + data.length);
        }
    }

    static long[] MP4A_SAMPLING_TABLE = {96000, 88200, 64000, 48000, 44100, 32000, 24000, 22050,
            16000, 12000, 11025, 8000, 7350, 0, 0, 0};

    static void parseMP4ACodecData(byte[] data) {
        int objectType = (data[0] & 0xF8) >> 3;
        int samplingIdx = (data[0] & 0x07) << 1 | (data[1] & 0x80) >> 7;
        long rate;
        int channel;

        if (samplingIdx == 15) {
            rate = (data[1] & 0x7F) << 17 | (data[2]) << 9 | (data[3]) << 1 | (data[4] & 0x80) >> 7;
            channel = (data[4] & 0x78) >> 3;
        } else {
            rate = MP4A_SAMPLING_TABLE[samplingIdx];
            channel = (data[1] & 0x78) >> 3;
        }

        Logger.debug("MP4 Audio Codec Data : Object Type : %d, Sampling Rate : %d, Channel : %d", objectType, rate, channel);
    }
}
