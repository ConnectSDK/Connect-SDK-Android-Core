package com.connectsdk.service.webos.lgcast.remotecamera.service;

import com.connectsdk.service.webos.lgcast.common.utils.JSONObjectEx;
import com.connectsdk.service.webos.lgcast.common.utils.Logger;
import com.connectsdk.service.webos.lgcast.remotecamera.RemoteCameraConfig;
import org.json.JSONObject;

public class CameraProperty {
    public static final String WIDTH = "width";
    public static final String HEIGHT = "height";
    public static final String FACING = "facing";
    public static final String BRIGHTNESS = "brightness";
    public static final String WHITE_BALANCE = "whiteBalance";
    public static final String AUTO_WHITE_BALANCE = "autoWhiteBalance";
    public static final String AUDIO = "audio";
    public static final String ROTATION = "rotation";

    // Camera Properties
    public int width = RemoteCameraConfig.Properties.DEFAULT_WIDTH;
    public int height = RemoteCameraConfig.Properties.DEFAULT_HEIGHT;
    public int facing = RemoteCameraConfig.Properties.DEFAULT_LENS_FACING;
    public int brightness = RemoteCameraConfig.Properties.DEFAULT_BRIGHTNESS;
    public int whiteBalance = RemoteCameraConfig.Properties.DEFAULT_WHITE_BALANCE;
    public boolean autoWhiteBalance = RemoteCameraConfig.Properties.DEFAULT_AUTO_WHITE_BALANCE;
    public boolean audio = RemoteCameraConfig.Properties.DEFAULT_AUDIO;
    public int rotation = -1;

    public CameraProperty() {
    }

    public JSONObject toJSONObject() {
        JSONObjectEx property = new JSONObjectEx();
        property.put(WIDTH, width);
        property.put(HEIGHT, height);
        property.put(FACING, facing);
        property.put(BRIGHTNESS, brightness);
        property.put(WHITE_BALANCE, whiteBalance);
        property.put(AUTO_WHITE_BALANCE, autoWhiteBalance);
        property.put(AUDIO, audio);
        property.put(ROTATION, rotation);
        return property.toJSONObject();
    }

    public void debug() {
        Logger.debug(WIDTH + "=" + width);
        Logger.debug(HEIGHT + "=" + height);
        Logger.debug(FACING + "=" + facing);
        Logger.debug(BRIGHTNESS + "=" + +brightness);
        Logger.debug(WHITE_BALANCE + "=" + whiteBalance);
        Logger.debug(AUTO_WHITE_BALANCE + "=" + autoWhiteBalance);
        Logger.debug(AUDIO + "=" + audio);
        Logger.debug(ROTATION + "=" + rotation);
        Logger.debug("");
    }
}
