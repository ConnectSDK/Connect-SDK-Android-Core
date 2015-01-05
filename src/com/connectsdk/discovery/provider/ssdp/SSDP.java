/*
 * SSDP
 * Connect SDK
 * 
 * Copyright (c) 2014 LG Electronics.
 * Copyright (c) 2011 stonker.lee@gmail.com https://code.google.com/p/android-dlna/
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

package com.connectsdk.discovery.provider.ssdp;


public class SSDP {
    /* New line definition */
    public static final String NEWLINE = "\r\n";
    
    public static final String MULTICAST_ADDRESS = "239.255.255.250";
    public static final int PORT = 1900;
    
    /* Definitions of start line */
    public static final String NOTIFY = "NOTIFY * HTTP/1.1";
    public static final String MSEARCH = "M-SEARCH * HTTP/1.1";
    public static final String OK = "HTTP/1.1 200 OK";

    /* Definitions of search targets */
//    public static final String DEVICE_MEDIA_SERVER_1 = "urn:schemas-upnp-org:device:MediaServer:1"; 
    
//    public static final String SERVICE_CONTENT_DIRECTORY_1 = "urn:schemas-upnp-org:service:ContentDirectory:1";
//    public static final String SERVICE_CONNECTION_MANAGER_1 = "urn:schemas-upnp-org:service:ConnectionManager:1";
//    public static final String SERVICE_AV_TRANSPORT_1 = "urn:schemas-upnp-org:service:AVTransport:1";
//    
//    public static final String ST_ContentDirectory = ST + ":" + UPNP.SERVICE_CONTENT_DIRECTORY_1;
    
    /* Definitions of notification sub type */
    public static final String ALIVE = "ssdp:alive";
    public static final String BYEBYE = "ssdp:byebye";
    public static final String UPDATE = "ssdp:update";
}