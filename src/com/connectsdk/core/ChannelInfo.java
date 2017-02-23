/*
 * ChannelInfo
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
 * Normalized reference object for information about a TVs channels. This object is required to set the channel on a TV.
 */
public class ChannelInfo implements JSONSerializable {
    // @cond INTERNAL
    String channelName;
    String channelId;
    String channelNumber;
    int minorNumber;
    int majorNumber;

    JSONObject rawData;
    // @endcond

    /**
     * Default constructor method.
     */
    public ChannelInfo() {
    }

    /** @return the raw data from the first screen device about the channel. In most cases, this is an NSDictionary. */
    public JSONObject getRawData() {
        return rawData;
    }

    /**
     * @param rawData the raw data from the first screen device about the channel. In most cases, this is an
     *            NSDictionary.
     */
    public void setRawData(JSONObject rawData) {
        this.rawData = rawData;
    }

    /** @return the user-friendly name of the channel */
    public String getName() {
        return channelName;
    }

    /** @param channelName the user-friendly name of the channel */
    public void setName(String channelName) {
        this.channelName = channelName;
    }

    /** @return the TV's unique ID for the channel */
    public String getId() {
        return channelId;
    }

    /** @param channelId the TV's unique ID for the channel */
    public void setId(String channelId) {
        this.channelId = channelId;
    }

    /** @return the TV channel's number (likely to be a combination of the major &amp; minor numbers) */
    public String getNumber() {
        return channelNumber;
    }

    /** @param channelNumber the TV channel's number (likely to be a combination of the major &amp; minor numbers) */
    public void setNumber(String channelNumber) {
        this.channelNumber = channelNumber;
    }

    /** @return the TV channel's minor number */
    public int getMinorNumber() {
        return minorNumber;
    }

    /** @param minorNumber the TV channel's minor number */
    public void setMinorNumber(int minorNumber) {
        this.minorNumber = minorNumber;
    }

    /** @return the TV channel's major number */
    public int getMajorNumber() {
        return majorNumber;
    }

    /** @param majorNumber the TV channel's major number */
    public void setMajorNumber(int majorNumber) {
        this.majorNumber = majorNumber;
    }

    // @cond INTERNAL
    @Override
    public JSONObject toJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();

        obj.put("name", channelName);
        obj.put("id", channelId);
        obj.put("number", channelNumber);
        obj.put("majorNumber", majorNumber);
        obj.put("minorNumber", minorNumber);
        obj.put("rawData", rawData);

        return obj;
    }
    // @endcond

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        if (channelId != null) {
            result = prime * result + channelId.hashCode();
        } else {
            result = prime * result + ((channelName == null) ? 0 : channelName.hashCode());
            result = prime * result + ((channelNumber == null) ? 0 : channelNumber.hashCode());
            result = prime * result + majorNumber;
            result = prime * result + minorNumber;
        }
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
        ChannelInfo other = (ChannelInfo) obj;

        if (channelId != null) {
            if (channelId.equals(other.channelId))
                return true;
        }

        if (channelName == null) {
            if (other.channelName != null)
                return false;
        } else if (!channelName.equals(other.channelName))
            return false;
        if (channelNumber == null) {
            if (other.channelNumber != null)
                return false;
        } else if (!channelNumber.equals(other.channelNumber))
            return false;
        if (majorNumber != other.majorNumber)
            return false;
        if (minorNumber != other.minorNumber)
            return false;
        return true;
    }
}
