/*
 * Copyright (c) 2020 LG Electronics Inc.
 * SPDX-License-Identifier: LicenseRef-LGE-Proprietary
 */
package com.connectsdk.service.webos.lgcast.screenmirroring.capability;

import android.content.Context;
import com.connectsdk.service.webos.lgcast.common.utils.AppUtil;
import com.connectsdk.service.webos.lgcast.common.utils.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class VideoSizeInfo {
    public int videoWidth;
    public int videoHeight;
    public int videoActiveWidth;
    public int videoActiveHeight;
    public String videoOrientation;

    public JSONObject toJSONObject(Context context) {
        try {
            JSONObject videoObj = new JSONObject();
            videoObj.put("width", videoWidth);
            videoObj.put("height", videoHeight);
            videoObj.put("activeWidth", videoActiveWidth);
            videoObj.put("activeHeight", videoActiveHeight);
            videoObj.put("orientation", (AppUtil.isLandscape(context) == true) ? "landscape" : "portrait");

            JSONObject mirroringObj = new JSONObject();
            mirroringObj.put("video", videoObj);
            return mirroringObj;
        } catch (JSONException e) {
            return new JSONObject();
        }
    }

    public void debug() {
        Logger.error("##### VIDEO SIZE INFO #####");
        Logger.error("videoWidth=" + videoWidth);
        Logger.error("videoHeight=" + videoHeight);
        Logger.error("videoActiveWidth=" + videoActiveWidth);
        Logger.error("videoActiveHeight=" + videoActiveHeight);
        Logger.error("videoOrientation=" + videoOrientation);
        Logger.error("");
    }
}
