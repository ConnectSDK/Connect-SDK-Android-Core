package com.connectsdk.service.webos.lgcast.remotecamera.capability;

import com.connectsdk.service.webos.lgcast.common.utils.Logger;
import org.json.JSONObject;

public class CameraSinkCapability {
    public String ipAddress;
    public int keepAliveTimeout;
    public String publicKey;

    public String deviceType;
    public String deviceVersion;
    public String devicePlatform;
    public String deviceSoC;

    public CameraSinkCapability(JSONObject jsonObj) {
        if (jsonObj == null) throw new IllegalArgumentException();
        /*Logger.debug(object.toString());*/

        ipAddress = jsonObj.optString("ipAddress", "0.0.0.0");
        keepAliveTimeout = jsonObj.optInt("keepAliveTimeout", 60) * 1000;
        publicKey = jsonObj.optString("publicKey");

        if (jsonObj.has("deviceInfo") == true) {
            JSONObject deviceInfo = jsonObj.optJSONObject("deviceInfo");
            deviceType = deviceInfo.optString("type");
            deviceVersion = deviceInfo.optString("version");
            devicePlatform = deviceInfo.optString("platform");
            deviceSoC = deviceInfo.optString("SoC");
        }
    }

    public void debug() {
        Logger.debug("ipAddress=" + ipAddress);
        Logger.debug("keepAliveTimeout=" + keepAliveTimeout);
        Logger.debug("deviceType=" + deviceType);
        Logger.debug("deviceVersion=" + deviceVersion);
        Logger.debug("devicePlatform=" + devicePlatform);
        Logger.debug("deviceSoC=" + deviceSoC);
        Logger.debug("");
    }
}
