/*
 * Copyright (c) 2020 LG Electronics Inc.
 * SPDX-License-Identifier: LicenseRef-LGE-Proprietary
 */
package com.connectsdk.service.webos.lgcast.screenmirroring.capability;

import com.connectsdk.service.webos.lgcast.common.utils.Logger;
import com.connectsdk.service.webos.lgcast.screenmirroring.ScreenMirroringConfig;
import org.json.JSONObject;

/*
http://collab.lge.com/main/pages/viewpage.action?pageId=1279300558

{
  "keepAliveTimeout": 60,
  "ipAddress": "172.16.0.18",
  "mirroring": {
    "video": {
      "codec": "H264",
      "udpPort": 53274,
      "portrait": {
        "maxHeight": 1920,
        "maxWidth": 1080
      },
      "landscape": {
        "maxHeight": 1080,
        "maxWidth": 1920
      }
    },
    "supportedFeatures": {
      "screenOrientation": "landscape|portrait"
    },
    "displayOrientation": "landscape",
    "audio": {
      "codec": "AAC",
      "udpPort": 53272
    }
  },
  "publicKey": "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AM...",
  "deviceInfo": {
    "version": "00.00.00",
    "SoC": "O22",
    "type": "tv",
    "platform": "WEBOS22"
  },
  "returnValue": true
}
*/
public class MirroringSinkCapability {
    public String ipAddress;
    public int keepAliveTimeout;
    public String publicKey;

    // V2 properties
    public String deviceType;
    public String deviceVersion;
    public String devicePlatform;
    public String deviceSoC;

    public String videoCodec;
    public int videoUdpPort;

    // V2 properties
    public int videoPortraitMaxWidth;
    public int videoPortraitMaxHeight;
    public int videoLandscapeMaxWidth;
    public int videoLandscapeMaxHeight;

    public String audioCodec;
    public int audioUdpPort;

    // V2 properties
    public String supportedOrientation;
    public String displayOrientation;

    public MirroringSinkCapability() {
    }

    public MirroringSinkCapability(JSONObject jsonObj) {
        if (jsonObj == null) throw new IllegalArgumentException();
        /*Logger.error(jsonObj.toString());*/

        ipAddress = jsonObj.optString("ipAddress", "127.0.0.1");
        keepAliveTimeout = jsonObj.optInt("keepAliveTimeout", 60) * 1000;
        publicKey = jsonObj.optString("publicKey");

        // V2 properties
        JSONObject deviceInfoObj = jsonObj.optJSONObject("deviceInfo");
        if (deviceInfoObj != null) {
            deviceType = deviceInfoObj.optString("type");
            deviceVersion = deviceInfoObj.optString("version");
            devicePlatform = deviceInfoObj.optString("platform");
            deviceSoC = deviceInfoObj.optString("SoC");
        }

        JSONObject mirroringObj = jsonObj.optJSONObject("mirroring");
        if (mirroringObj != null) {

            JSONObject videoObj = mirroringObj.optJSONObject("video");
            if (videoObj != null) {
                videoCodec = videoObj.optString("codec");
                videoUdpPort = videoObj.optInt("udpPort");

                // V2 properties
                JSONObject videoPortraitSize = videoObj.optJSONObject("portrait");
                if (videoPortraitSize != null) {
                    videoPortraitMaxWidth = videoPortraitSize.optInt("maxWidth", ScreenMirroringConfig.Video.DEFAULT_HEIGHT);
                    videoPortraitMaxHeight = videoPortraitSize.optInt("maxHeight", ScreenMirroringConfig.Video.DEFAULT_WIDTH);
                }

                // V2 properties
                JSONObject videoLandscapeSize = videoObj.optJSONObject("landscape");
                if (videoLandscapeSize != null) {
                    videoLandscapeMaxWidth = videoLandscapeSize.optInt("maxWidth", ScreenMirroringConfig.Video.DEFAULT_WIDTH);
                    videoLandscapeMaxHeight = videoLandscapeSize.optInt("maxHeight", ScreenMirroringConfig.Video.DEFAULT_HEIGHT);
                } else { // V1 properties
                    videoLandscapeMaxWidth = ScreenMirroringConfig.Video.DEFAULT_WIDTH;
                    videoLandscapeMaxHeight = ScreenMirroringConfig.Video.DEFAULT_HEIGHT;
                }
            }

            JSONObject audioObj = mirroringObj.optJSONObject("audio");
            if (audioObj != null) {
                audioCodec = audioObj.optString("codec", "none");
                audioUdpPort = audioObj.optInt("udpPort");
            }

            // V2 properties
            JSONObject supportedFeatures = mirroringObj.optJSONObject("supportedFeatures");
            if (supportedFeatures != null) {
                supportedOrientation = supportedFeatures.optString("screenOrientation", "landscape");
            }

            // V2 properties
            displayOrientation = mirroringObj.optString("displayOrientation", "landscape");
        }
    }

    public boolean isSupportLandscapeMode() {
        return supportedOrientation != null && supportedOrientation.contains("landscape");
    }

    public boolean isSupportPortraitMode() {
        return supportedOrientation != null && supportedOrientation.contains("portrait");
    }

    public boolean isDisplayLandscape() {
        return "landscape".equals(displayOrientation);
    }

    public boolean isDisplayPortrait() {
        return "portrait".equals(displayOrientation);
    }

    public void debug() {
        /*Logger.debug("ipAddress=" + ipAddress);
        Logger.debug("keepAliveTimeout=" + keepAliveTimeout);
        Logger.debug("deviceType=" + deviceType);
        Logger.debug("deviceVersion=" + deviceVersion);
        Logger.debug("devicePlatform=" + devicePlatform);
        Logger.debug("deviceSoC=" + deviceSoC);
        Logger.debug("videoCodec=" + videoCodec);
        Logger.debug("videoUdpPort=" + videoUdpPort);
        Logger.debug("videoPortraitMaxWidth=" + videoPortraitMaxWidth);
        Logger.debug("videoPortraitMaxHeight=" + videoPortraitMaxHeight);
        Logger.debug("videoLandscapeMaxWidth=" + videoLandscapeMaxWidth);
        Logger.debug("videoLandscapeMaxHeight=" + videoLandscapeMaxHeight);
        Logger.debug("audioCodec=" + audioCodec);
        Logger.debug("audioUdpPort=" + audioUdpPort);
        Logger.debug("supportedOrientation=" + supportedOrientation);
        Logger.debug("displayOrientation=" + displayOrientation);
        Logger.debug("");//*/

        Logger.error("##### MIRRORING SINK CAPABILITY #####");
        Logger.error("ipAddress=" + ipAddress);
        Logger.error("videoPortraitMaxWidth=" + videoPortraitMaxWidth);
        Logger.error("videoPortraitMaxHeight=" + videoPortraitMaxHeight);
        Logger.error("videoLandscapeMaxWidth=" + videoLandscapeMaxWidth);
        Logger.error("videoLandscapeMaxHeight=" + videoLandscapeMaxHeight);
        Logger.error("supportedOrientation=" + supportedOrientation);
        Logger.error("displayOrientation=" + displayOrientation);
        Logger.error("");
    }
}