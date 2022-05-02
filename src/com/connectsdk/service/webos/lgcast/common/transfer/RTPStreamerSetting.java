/*
 * Copyright (c) 2020 LG Electronics Inc.
 * SPDX-License-Identifier: LicenseRef-LGE-Proprietary
 */
package com.connectsdk.service.webos.lgcast.common.transfer;

import com.connectsdk.service.webos.lgcast.common.utils.Logger;
import java.util.ArrayList;

public class RTPStreamerSetting {
    public static class StreamRSCInfo {
        int rscType;
        String host;
        int port;

        public StreamRSCInfo(RTPStreamerData.ResourceType rscType, String host, int port) {
            this.rscType = rscType.getType();
            this.host = host;
            this.port = port;
        }
    }

    public static class StreamInfo {
        // Mode
        int type;
        int pt;

        // https://wiki.multimedia.cx/index.php/MPEG-4_Audio
        // http://thompsonng.blogspot.com/2010/03/aac-configuration.html
        int codecDataLen = 0;
        byte[] codecData;

        // Pool
        boolean supportPool = false;
        int poolSize;
        int maxpools;

        StreamRSCInfo src;
        StreamRSCInfo dst;
    }

    public static class VideoStreamInfo extends StreamInfo {
        // Default settings
        int width;
        int height;
        int frameRate;
        int bitRate;
        private boolean isCompleted = false;

        public VideoStreamInfo() {
            super();
            type = RTPStreamerData.VideoType.H264.getType();
            pt = RTPStreamerData.VideoType.H264.getPt();
        }

        public boolean isCompleted() {
            return isCompleted;
        }

        public void setVideoConfig(RTPStreamerConfig.VideoConfig config) {
            type = config.type.getType();
            pt = config.type.getPt();
            width = config.width;
            height = config.height;
            bitRate = config.bitrate;
            frameRate = config.framerate;

            if (config.codecData != null) {
                codecDataLen = config.codecData.length;
                codecData = config.codecData;
            }

            if (config.enableMP && config.mpUnitSize > 0) {
                supportPool = true;
                poolSize = config.mpUnitSize;
                maxpools = 100;
            } else {
                supportPool = false;
            }

            src = new StreamRSCInfo(config.resourceType, "", 0);
            isCompleted = true;
        }
    }

    public static class AudioStreamInfo extends StreamInfo {
        int samplingRate;
        int channelCnt;
        private boolean isCompleted = false;

        public AudioStreamInfo() {
            super();
            type = RTPStreamerData.AudioType.PCM.getType();
            pt = RTPStreamerData.AudioType.PCM.getPt();
        }

        public boolean isCompleted() {
            return isCompleted;
        }

        public void setAudioConfig(RTPStreamerConfig.AudioConfig config) {
            type = config.type.getType();
            pt = config.type.getPt();
            samplingRate = config.samplingRate;
            channelCnt = config.channelCnt;

            if (config.codecData != null) {
                codecDataLen = config.codecData.length;
                codecData = config.codecData;

                //RTPStreamerUtil.parseMP4ACodecData(config.codecData);
            }

            if (config.enableMP && config.mpUnitSize > 0) {
                supportPool = true;
                poolSize = config.mpUnitSize;
                maxpools = 100;
            } else {
                supportPool = false;
            }

            src = new StreamRSCInfo(config.resourceType, "", 0);
            isCompleted = true;
        }
    }

    public static class KeyInfo {
        // Master key (minimum of 30 and maximum of 46 bytes)
        int masterKeyLen;
        byte[] masterKey;
        int mkiByteLen;
        byte[] mki;
    }

    public static class SecurityInfo {
        boolean enableSecurity = false;
        boolean enableMki = false;
        private boolean isCompleted = false;

        int keyCount;
        ArrayList<KeyInfo> keyInfos;

        int authType;
        int cipherType;

        public boolean isCompleted() {
            if (enableSecurity == false) {
                return true;
            } else {
                return isCompleted;
            }
        }

        public void setSecurityConfig(RTPStreamerConfig.SecurityConfig config) {
            this.enableSecurity = config.enableSecurity;

            if (enableSecurity) {
                this.enableMki = config.enableMki;
                this.authType = config.authType.type;
                if (setSecurityKey(config)) {
                    this.isCompleted = true;
                    this.keyCount = this.keyInfos.size();
                }
            }
        }

        private boolean setSecurityKey(RTPStreamerConfig.SecurityConfig config) {
            switch (config.cipherType) {
                case NONE:
                    cipherType = RTPStreamerData.SRTPCipherType.NONE.type;
                    keyCount = 0;
                    return true;
                case AES_128_GCM:
                case AES_256_GCM:
                case AES_128_ICM:
                case AES_256_ICM:
                    keyInfos = new ArrayList<KeyInfo>();
                    for (RTPStreamerConfig.SecurityKey key : config.keys) {
                        KeyInfo keyInfo = addMasterkey(key, config.cipherType.getLength());
                        if (keyInfo != null) {
                            keyInfos.add(keyInfo);
                        } else {
                            keyInfos = null;
                            return false;
                        }
                    }
                    return true;
                default:
                    return false;
            }
        }

        private void addMkiInfo(RTPStreamerConfig.SecurityKey key, KeyInfo keyInfo) {
            if (key.mki != null && key.mki.length > 0) {
                keyInfo.mkiByteLen = Math.min(key.mki.length, 128);
                keyInfo.mki = new byte[keyInfo.mkiByteLen];
                System.arraycopy(key.mki, 0, keyInfo.mki, 0, keyInfo.mkiByteLen);
            } else {
                keyInfo.mkiByteLen = 0;
                keyInfo.mki = null;
            }
        }

        private KeyInfo addMasterkey(RTPStreamerConfig.SecurityKey key, int keyLen) {
            if (key.masterKey != null && key.masterKey.length == keyLen) {
                KeyInfo keyInfo = new KeyInfo();
                keyInfo.masterKeyLen = keyLen;
                keyInfo.masterKey = new byte[keyInfo.masterKeyLen];
                System.arraycopy(key.masterKey, 0, keyInfo.masterKey, 0, keyInfo.masterKeyLen);

                if (enableMki) {
                    addMkiInfo(key, keyInfo);
                }

                return keyInfo;
            } else {
                Logger.error("please check master key info !!");
                return null;
            }
        }
    }

    public static class StreamSetting {
        int protocolType;
        int mediaType;
        long ssrc; // random 32-bit number
        boolean isLibDebug = true;
        boolean isGLibDebug = false;

        VideoStreamInfo videoStreamInfo;
        AudioStreamInfo audioStreamInfo;
        SecurityInfo securityInfo;

        public StreamSetting(RTPStreamerData.ProtocolType protocolType, RTPStreamerData.MediaType mediaType, long ssrc) {
            this.protocolType = protocolType.getType();
            this.mediaType = mediaType.getType();
            this.ssrc = ssrc;

            if (mediaType == RTPStreamerData.MediaType.VIDEO || mediaType == RTPStreamerData.MediaType.AV)
                videoStreamInfo = new VideoStreamInfo();

            if (mediaType == RTPStreamerData.MediaType.AUDIO || mediaType == RTPStreamerData.MediaType.AV)
                audioStreamInfo = new AudioStreamInfo();

            this.securityInfo = new SecurityInfo();
        }

        public void setSourceStream(RTPStreamerData.MediaType mediaType, RTPStreamerData.ResourceType srcType, String host, int port) {
            if (mediaType != RTPStreamerData.MediaType.VIDEO && mediaType != RTPStreamerData.MediaType.AUDIO) {
                Logger.error("can't set SRC source(media type: %d) resource info ", mediaType.getType());
                return;
            }

            StreamRSCInfo rscInfo = new StreamRSCInfo(srcType, host, port);
            if (mediaType == RTPStreamerData.MediaType.VIDEO) {
                videoStreamInfo.src = rscInfo;
            } else if (mediaType == RTPStreamerData.MediaType.AUDIO) {
                audioStreamInfo.src = rscInfo;
            }
        }

        public void setDestinationStream(RTPStreamerData.MediaType mediaType, String host, int port) {
            if (mediaType != RTPStreamerData.MediaType.VIDEO && mediaType != RTPStreamerData.MediaType.AUDIO) {
                Logger.error("can't set DST source(media type: %d) resource info ", mediaType.getType());
                return;
            }

            StreamRSCInfo rscInfo = new StreamRSCInfo(RTPStreamerData.ResourceType.SOCKET, host, port);
            if (mediaType == RTPStreamerData.MediaType.VIDEO) {
                videoStreamInfo.dst = rscInfo;
            } else if (mediaType == RTPStreamerData.MediaType.AUDIO) {
                audioStreamInfo.dst = rscInfo;
            }
        }
    }
}
