/*
 * Copyright (c) 2020 LG Electronics Inc.
 * SPDX-License-Identifier: LicenseRef-LGE-Proprietary
 */
package com.connectsdk.service.webos.lgcast.common.transfer;

import java.util.ArrayList;

public class RTPStreamerConfig {
    public static class SecurityKey {
        // Master key (minimum of 30 and maximum of 46 bytes)
        // 128 bit key size: 14 (salt) + 16
        // 256 bit key size: 14 (salt) + 16 + 16
        public byte[] masterKey;

        // 0 < MKI Byte Length <= 128
        // if mki is not supported, set to 0
        public byte[] mki;
    }

    public static class SecurityConfig {
        // TODO ONLY TEST
        boolean enableSecurity = true;
        public boolean enableMki = true;

        ArrayList<SecurityKey> keys;

        // Default Value : HMAC_SHA1_80
        RTPStreamerData.SRTPAuthType authType = RTPStreamerData.SRTPAuthType.HMAC_SHA1_80;
        // Default Value : AES_128_ICM
        RTPStreamerData.SRTPCipherType cipherType = RTPStreamerData.SRTPCipherType.AES_128_ICM;


        public boolean isEnableSecurity() {
            return enableSecurity;
        }

        public void setEnableSecurity(boolean enableSecurity) {
            this.enableSecurity = enableSecurity;
        }

        public RTPStreamerData.SRTPAuthType getAuthType() {
            return authType;
        }

        public void setAuthType(RTPStreamerData.SRTPAuthType authType) {
            this.authType = authType;
        }

        public RTPStreamerData.SRTPCipherType getCipherType() {
            return cipherType;
        }

        public void setCipherType(RTPStreamerData.SRTPCipherType cipherType) {
            this.cipherType = cipherType;
        }

        public ArrayList<SecurityKey> getKeys() {
            return keys;
        }

        public void setKeys(ArrayList<SecurityKey> keys) {
            this.keys = keys;
        }
    }

    public static class VideoConfig {
        RTPStreamerData.VideoType type = RTPStreamerData.VideoType.H264;
        RTPStreamerData.ResourceType resourceType = RTPStreamerData.ResourceType.APP;

        int width;
        int height;
        int framerate;
        int bitrate;

        // TODO:: Codec?
        byte[] codecData = null;

        // Memory Pool
        boolean enableMP = false;
        int mpUnitSize;

        public RTPStreamerData.VideoType getType() {
            return type;
        }

        public void setType(RTPStreamerData.VideoType type) {
            this.type = type;
        }

        public RTPStreamerData.ResourceType getResourceType() {
            return resourceType;
        }

        public void setResourceType(RTPStreamerData.ResourceType resourceType) {
            this.resourceType = resourceType;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public int getFramerate() {
            return framerate;
        }

        public void setFramerate(int framerate) {
            this.framerate = framerate;
        }

        public int getBitrate() {
            return bitrate;
        }

        public void setBitrate(int bitrate) {
            this.bitrate = bitrate;
        }

        public byte[] getCodecData() {
            return codecData;
        }

        public void setCodecData(byte[] codecData) {
            this.codecData = codecData;
        }

        public boolean isEnableMP() {
            return enableMP;
        }

        public void setEnableMP(boolean enableMP) {
            this.enableMP = enableMP;
        }

        public int getMpUnitSize() {
            return mpUnitSize;
        }

        public void setMpUnitSize(int mpUnitSize) {
            this.mpUnitSize = mpUnitSize;
        }
    }

    public static class AudioConfig {
        RTPStreamerData.AudioType type = RTPStreamerData.AudioType.AAC;
        RTPStreamerData.ResourceType resourceType = RTPStreamerData.ResourceType.APP;
        int samplingRate;
        int channelCnt;

        // TODO:: Codec?
        byte[] codecData = null;

        // Memory Pool
        boolean enableMP = false;
        int mpUnitSize;

        public RTPStreamerData.AudioType getType() {
            return type;
        }

        public void setType(RTPStreamerData.AudioType type) {
            this.type = type;
        }

        public RTPStreamerData.ResourceType getResourceType() {
            return resourceType;
        }

        public void setResourceType(RTPStreamerData.ResourceType resourceType) {
            this.resourceType = resourceType;
        }

        public byte[] getCodecData() {
            return codecData;
        }

        public void setCodecData(byte[] codecData) {
            this.codecData = codecData;
        }

        public boolean isEnableMP() {
            return enableMP;
        }

        public void setEnableMP(boolean enableMP) {
            this.enableMP = enableMP;
        }

        public int getMpUnitSize() {
            return mpUnitSize;
        }

        public void setMpUnitSize(int mpUnitSize) {
            this.mpUnitSize = mpUnitSize;
        }

        public int getSamplingRate() {
            return samplingRate;
        }

        public void setSamplingRate(int samplingRate) {
            this.samplingRate = samplingRate;
        }

        public int getChannelCnt() {
            return channelCnt;
        }

        public void setChannelCnt(int channelCnt) {
            this.channelCnt = channelCnt;
        }
    }
}
