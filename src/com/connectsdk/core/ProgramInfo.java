/*
 * ProgramInfo
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

/** Normalized reference object for information about a TVs program. */
public class ProgramInfo {
    // @cond INTERNAL
    private String id;
    private String name;

    private ChannelInfo channelInfo;

    private Object rawData;
    // @endcond 

    /** Returns the ID of the program on the first screen device. Format is different depending on the platform. 
     * @return the ID */
    public String getId() {
        return id;
    }

    /** Sets the ID of the program on the first screen device. Format is different depending on the platform.
     * @param id the ID */
    public void setId(String id) {
        this.id = id;
    }

    /** @return the user-friendly name of the program (ex. Sesame Street, Cosmos, Game of Thrones, etc). */
    public String getName() {
        return name;
    }

    /** Sets the user-friendly name of the program (ex. Sesame Street, Cosmos, Game of Thrones, etc).
     * @param name the name */
    public void setName(String name) {
        this.name = name;
    }

    public ChannelInfo getChannelInfo() {
        return channelInfo;
    }

    public void setChannelInfo(ChannelInfo channelInfo) {
        this.channelInfo = channelInfo;
    }

    /** @return the raw data from the first screen device about the program. In most cases, this is an NSDictionary. */
    public Object getRawData() {
        return rawData;
    }

    public void setRawData(Object rawData) {
        this.rawData = rawData;
    }

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
        ProgramInfo other = (ProgramInfo) obj;
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
