/*
 * Copyright (c) 2020 LG Electronics Inc.
 * SPDX-License-Identifier: LicenseRef-LGE-Proprietary
 */
package com.connectsdk.service.webos.lgcast.screenmirroring.capability;

import com.connectsdk.service.webos.lgcast.common.utils.Logger;
import com.lge.lib.lgcast.iface.MasterKey;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MirroringSourceCapability {
    public String videoCodec;
    public int videoClockRate;
    public int videoFramerate;
    public int videoBitrate;
    public int videoWidth;
    public int videoHeight;
    public int videoActiveWidth;
    public int videoActiveHeight;
    public String videoOrientation;

    public String audioCodec;
    public int audioClockRate;
    public int audioFrequency;
    public String audioStreamMuxConfig;
    public int audioChannels;

    public ArrayList<MasterKey> masterKeys;

    public boolean uibcEnabled;
    public String screenOrientation;

    public JSONObject toJSONObject() {
        try {
            JSONObject videoObj = new JSONObject();
            videoObj.put("codec", videoCodec);
            videoObj.put("clockRate", videoClockRate);
            videoObj.put("framerate", videoFramerate);
            videoObj.put("bitrate", videoBitrate);
            videoObj.put("width", videoWidth);
            videoObj.put("height", videoHeight);
            videoObj.put("activeWidth", videoActiveWidth);
            videoObj.put("activeHeight", videoActiveHeight);
            videoObj.put("orientation", videoOrientation);

            JSONObject audioObj = new JSONObject();
            audioObj.put("codec", audioCodec);
            audioObj.put("clockRate", audioClockRate);
            audioObj.put("frequency", audioFrequency);
            audioObj.put("streamMuxConfig", audioStreamMuxConfig);
            audioObj.put("channels", audioChannels);

            JSONArray cryptoObj = new JSONArray();

            for (MasterKey masterKey : masterKeys) {
                JSONObject mkiObj = new JSONObject();
                mkiObj.put("mki", masterKey.mkiSecureText);
                mkiObj.put("key", masterKey.keySecureText);
                cryptoObj.put(mkiObj);
            }

            JSONObject supportedFeatures = new JSONObject();
            supportedFeatures.put("screenOrientation", screenOrientation);

            JSONObject mirroringCapability = new JSONObject();
            mirroringCapability.put("video", videoObj);
            mirroringCapability.put("audio", audioObj);
            mirroringCapability.put("crypto", cryptoObj);
            mirroringCapability.put("uibcEnabled", uibcEnabled);
            mirroringCapability.put("supportedFeatures", supportedFeatures);
            return mirroringCapability;
        } catch (JSONException e) {
            return new JSONObject();
        }
    }

    public void debug() {
        /*Logger.debug("videoCodec=" + videoCodec);
        Logger.debug("videoClockRate=" + videoClockRate);
        Logger.debug("videoFramerate=" + videoFramerate);
        Logger.debug("videoBitrate=" + videoBitrate);
        Logger.debug("videoWidth=" + videoWidth);
        Logger.debug("videoHeight=" + videoHeight);
        Logger.debug("videoActiveWidth=" + videoActiveWidth);
        Logger.debug("videoActiveHeight=" + videoActiveHeight);
        Logger.debug("videoOrientation=" + videoOrientation);
        Logger.debug("audioCodec=" + audioCodec);
        Logger.debug("audioClockRate=" + audioClockRate);
        Logger.debug("audioFrequency=" + audioFrequency);
        Logger.debug("audioStreamMuxConfig=" + audioStreamMuxConfig);
        Logger.debug("audioChannels=" + audioChannels);
        Logger.debug("uibcEnabled=" + uibcEnabled);
        Logger.debug("screenOrientation=" + screenOrientation);
        Logger.debug("");//*/

        Logger.error("##### MIRRORING SOURCE CAPABILITY #####");
        Logger.error("videoBitrate=" + videoBitrate);
        Logger.error("videoWidth=" + videoWidth);
        Logger.error("videoHeight=" + videoHeight);
        Logger.error("videoActiveWidth=" + videoActiveWidth);
        Logger.error("videoActiveHeight=" + videoActiveHeight);
        Logger.error("videoOrientation=" + videoOrientation);
        Logger.error("uibcEnabled=" + uibcEnabled);
        Logger.error("screenOrientation=" + screenOrientation);
        Logger.error("");
    }
}
