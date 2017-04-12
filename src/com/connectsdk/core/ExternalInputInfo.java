/*
 * ExternalInputInfo
 * Connect SDK
 * 
 * Copyright (c) 2014 LG Electronics.
 * Created by Hyun Kook Khang on 19 Jan 2014
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.connectsdk.core;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Normalized reference object for information about a DeviceService's external inputs. This object is required to set a DeviceService's external input.
 */
public class ExternalInputInfo implements JSONSerializable {
    String id;
    String name;
    boolean connected;
    String iconURL;

    JSONObject rawData;

    /**
     * Default constructor method.
     */
    public ExternalInputInfo() {
    }

    /** @return the ID of the external input on the first screen device. */
    public String getId() {
        return id;
    }

    /** @param inputId the ID of the external input on the first screen device. */
    public void setId(String inputId) {
        this.id = inputId;
    }

    /** @return the user-friendly name of the external input (ex. AV, HDMI1, etc). */
    public String getName() {
        return name;
    }

    /** @param inputName the user-friendly name of the external input (ex. AV, HDMI1, etc). */
    public void setName(String inputName) {
        this.name = inputName;
    }

    /** @param rawData the raw data from the first screen device about the external input. */
    public void setRawData(JSONObject rawData) {
        this.rawData = rawData;
    }

    /** @return the raw data from the first screen device about the external input. */
    public JSONObject getRawData() {
        return rawData;
    }

    /** @return true if the DeviceService is currently connected to this external input. */
    public boolean isConnected() {
        return connected;
    }

    /** @param connected whether the DeviceService is currently connected to this external input. */
    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    /** @return the URL to an icon representing this external input. */
    public String getIconURL() {
        return iconURL;
    }

    /** @param iconURL the URL to an icon representing this external input. */
    public void setIconURL(String iconURL) {
        this.iconURL = iconURL;
    }

    // @cond INTERNAL
    @Override
    public JSONObject toJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();

        obj.put("id", id);
        obj.put("name", name);
        obj.put("connected", connected);
        obj.put("icon", iconURL);
        obj.put("rawData", rawData);

        return obj;
    }
    // @endcond

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ExternalInputInfo other = (ExternalInputInfo) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

    
}
