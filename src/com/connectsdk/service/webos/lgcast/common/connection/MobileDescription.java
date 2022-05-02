/*
 * Copyright (c) 2020 LG Electronics Inc.
 * SPDX-License-Identifier: LicenseRef-LGE-Proprietary
 */
package com.connectsdk.service.webos.lgcast.common.connection;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import com.connectsdk.R;
import com.connectsdk.service.webos.lgcast.common.utils.IOUtil;
import com.connectsdk.service.webos.lgcast.common.utils.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class MobileDescription {
    private static final String KEY_TYPE = "type";
    private static final String KEY_PLATFORM = "platform";
    private static final String KEY_VERSION = "version";
    private static final String KEY_MANUFACTURER = "manufacturer";
    private static final String KEY_MODEL_NAME = "modelName";
    private static final String KEY_DEVICE_NAME = "deviceName";

    private static final String VAL_PHONE = "phone";
    private static final String VAL_ANDROID = "android";

    public String type;
    public String platform;
    public String version;
    public String manufacturer;
    public String modelName;
    public String deviceName;

    public MobileDescription(Context context) {
        type = VAL_PHONE;
        platform = VAL_ANDROID;
        version = IOUtil.readRawResourceText(context, R.raw.lgcast_version);
        manufacturer = Build.MANUFACTURER;
        modelName = Build.MODEL;
        deviceName = Settings.Global.getString(context.getContentResolver(), "device_name");
        if (deviceName == null || deviceName.length() == 0) deviceName = Settings.Secure.getString(context.getContentResolver(), "bluetooth_name");
        if (deviceName == null || deviceName.length() == 0) deviceName = Build.MODEL;
    }

    public JSONObject toJSONObject() {
        try {
            JSONObject deviceObj = new JSONObject();
            deviceObj.put(KEY_TYPE, type);
            deviceObj.put(KEY_PLATFORM, platform);
            deviceObj.put(KEY_VERSION, version);
            deviceObj.put(KEY_MANUFACTURER, manufacturer);
            deviceObj.put(KEY_MODEL_NAME, modelName);
            deviceObj.put(KEY_DEVICE_NAME, deviceName);
            return deviceObj;
        } catch (JSONException e) {
            return new JSONObject();
        }
    }

    public void debug() {
        Logger.debug("type=" + type);
        Logger.debug("platform=" + platform);
        Logger.debug("version=" + version);
        Logger.debug("manufacturer=" + manufacturer);
        Logger.debug("modelName=" + modelName);
        Logger.debug("deviceName=" + deviceName);
    }
}