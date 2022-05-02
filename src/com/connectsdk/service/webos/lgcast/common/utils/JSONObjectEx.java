/*
 * Copyright (c) 2020 LG Electronics Inc.
 * SPDX-License-Identifier: LicenseRef-LGE-Proprietary
 */
package com.connectsdk.service.webos.lgcast.common.utils;

import org.json.JSONException;
import org.json.JSONObject;

public class JSONObjectEx {
    private JSONObject mJSONObject;

    public JSONObjectEx() {
        mJSONObject = new JSONObject();
    }

    public JSONObjectEx(JSONObject jsonObject) {
        mJSONObject = jsonObject;
    }

    public JSONObjectEx put(final String name, final JSONObject value) {
        try {
            if (name == null || value == null) throw new Exception("Invalid arguments");
            mJSONObject.put(name, value);
        } catch (Exception e) {
            Logger.error(e);
        }

        return this;
    }

    public JSONObjectEx put(final String name, final JSONObjectEx value) {
        try {
            if (name == null || value == null) throw new Exception("Invalid arguments");
            mJSONObject.put(name, value.toJSONObject());
        } catch (Exception e) {
            Logger.error(e);
        }

        return this;
    }

    public JSONObjectEx put(final String name, final String value) {
        try {
            if (name == null) throw new Exception("Invalid arguments");
            mJSONObject.put(name, value);
        } catch (Exception e) {
            Logger.error(e);
        }

        return this;
    }

    public JSONObjectEx put(final String name, final int value) {
        try {
            if (name == null) throw new Exception("Invalid arguments");
            mJSONObject.put(name, value);
        } catch (Exception e) {
            Logger.error(e);
        }

        return this;
    }

    public JSONObjectEx put(final String name, final boolean value) {
        try {
            if (name == null) throw new Exception("Invalid arguments");
            mJSONObject.put(name, value);
        } catch (Exception e) {
            Logger.error(e);
        }

        return this;
    }

    public JSONObject toJSONObject() {
        return mJSONObject;
    }

    public String toString() {
        return mJSONObject.toString();
    }

    public String toString(int indentSpaces) {
        try {
            return mJSONObject.toString(indentSpaces);
        } catch (JSONException e) {
            return new String();
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static JSONObject put(JSONObject jsonObj, String name, String value) {
        try {
            if (jsonObj != null && name != null) jsonObj.put(name, value);
        } catch (Exception e) {
            Logger.error(e);
        }

        return jsonObj;
    }

    public static JSONObject put(JSONObject jsonObj, String name, int value) {
        try {
            if (jsonObj != null && name != null) jsonObj.put(name, value);
        } catch (Exception e) {
            Logger.error(e);
        }

        return jsonObj;
    }

    public static JSONObject put(JSONObject jsonObj, String name, boolean value) {
        try {
            if (jsonObj != null && name != null) jsonObj.put(name, value);
        } catch (Exception e) {
            Logger.error(e);
        }

        return jsonObj;
    }

    public static String toString(JSONObject jsonObj) {
        try {
            return (jsonObj != null) ? jsonObj.toString(4) : new String();
        } catch (JSONException e) {
            return new String();
        }
    }
}
