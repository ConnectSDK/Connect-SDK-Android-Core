/*
 * Copyright (c) 2020 LG Electronics Inc.
 * SPDX-License-Identifier: LicenseRef-LGE-Proprietary
 */
package com.connectsdk.service.webos.lgcast.common.transfer;

public class RTPStreamerData {
    public enum VideoType {
        RAW(0, 96), H264(1, 96), MP2TS(2, 33), MJPEG(3, 26);

        int type;
        int pt;

        VideoType(int type, int pt) {
            this.type = type;
            this.pt = pt;
        }

        public int getType() {
            return this.type;
        }

        public int getPt() {
            return this.pt;
        }
    }

    public enum AudioType {
        PCM(0, 11), AAC(1, 97), OPUS(2, 97);
        int type;
        int pt;

        AudioType(int type, int pt) {
            this.type = type;
            this.pt = pt;
        }

        public int getType() {
            return this.type;
        }

        public int getPt() {
            return this.pt;
        }
    }

    public enum MediaType {
        VIDEO(0), AUDIO(1), AV(2);
        int type;

        MediaType(int type) {
            this.type = type;
        }

        public int getType() {
            return this.type;
        }
    }

    public enum ProtocolType {
        RTP(0), RTSP(1), FILE(2);
        int type;

        ProtocolType(int type) {
            this.type = type;
        }

        public int getType() {
            return this.type;
        }
    }

    public enum ResourceType {
        APP(0), FILE(1), SOCKET(2), CAMERA(3);
        int type;

        ResourceType(int type) {
            this.type = type;
        }

        public int getType() {
            return this.type;
        }
    }

    public enum SRTPAuthType {
        NONE(0), HMAC_SHA1_32(1), HMAC_SHA1_80(2);
        int type;

        SRTPAuthType(int type) {
            this.type = type;
        }

        public int getType() {
            return this.type;
        }
    }

    public enum SRTPCipherType {
        NONE(0, 0), AES_128_ICM(1, 30), AES_256_ICM(2, 46), AES_128_GCM(3, 30), AES_256_GCM(4, 46);
        int type;
        int length;

        SRTPCipherType(int type, int length) {
            this.length = length;
            this.type = type;
        }

        public int getType() {
            return this.type;
        }

        public int getLength() {
            return this.length;
        }
    }
}
