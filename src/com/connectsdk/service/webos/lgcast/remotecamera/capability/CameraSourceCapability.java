package com.connectsdk.service.webos.lgcast.remotecamera.capability;

import android.content.Context;
import android.util.Size;
import com.connectsdk.service.webos.lgcast.common.utils.Logger;
import com.connectsdk.service.webos.lgcast.remotecamera.service.CameraUtility;
import com.lge.lib.security.iface.MasterKey;
import com.lge.lib.security.iface.MasterKeyFactoryIF;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CameraSourceCapability {
    public ArrayList<MasterKey> masterKeys;
    public ArrayList<Size> mSupportedPreviewSizes;

    public static CameraSourceCapability create(Context context, String encodedPublicKey) {
        CameraSourceCapability cameraSourceCapability = new CameraSourceCapability();
        cameraSourceCapability.masterKeys = new MasterKeyFactoryIF().createKeys(encodedPublicKey);
        cameraSourceCapability.mSupportedPreviewSizes = new ArrayList<>();

        for (Size s : CameraUtility.getSupportedPreviewSizes(context)) {
            boolean c1 = s.getWidth() <= 1920 && s.getHeight() <= 1080;
            boolean c2 = s.getWidth() >= 320 && s.getHeight() >= 240;
            if (c1 == true && c2 == true) cameraSourceCapability.mSupportedPreviewSizes.add(s);
        }

        return cameraSourceCapability;
    }

    public JSONObject toJSONObject() {
        try {
            JSONArray mkiList = new JSONArray();
            JSONArray previewSizeList = new JSONArray();

            for (MasterKey masterKey : masterKeys) {
                JSONObject mkiObj = new JSONObject();
                mkiObj.put("mki", masterKey.mkiSecureText);
                mkiObj.put("key", masterKey.keySecureText);
                mkiList.put(mkiObj);
            }

            for (Size s : mSupportedPreviewSizes) {
                JSONObject previewSize = new JSONObject();
                previewSize.put("w", s.getWidth());
                previewSize.put("h", s.getHeight());
                previewSizeList.put(previewSize);
            }

            JSONObject jsonObj = new JSONObject();
            jsonObj.put("crypto", mkiList);
            jsonObj.put("previewSize", previewSizeList);
            return jsonObj;
        } catch (JSONException e) {
            return new JSONObject();
        }
    }

    public void debug() {
        for (Size s : mSupportedPreviewSizes)
            Logger.debug("Preview size: %d x %d", s.getWidth(), s.getHeight());

        Logger.debug("");
    }
}
