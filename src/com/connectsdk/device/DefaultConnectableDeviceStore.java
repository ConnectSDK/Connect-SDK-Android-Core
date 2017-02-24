/*
 * DefaultConnectableDeviceStore
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

package com.connectsdk.device;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.connectsdk.core.Context;
import com.connectsdk.core.Log;
import com.connectsdk.core.Util;
import com.connectsdk.service.DeviceService;
import com.connectsdk.service.config.ServiceConfig;
import com.connectsdk.service.config.ServiceDescription;

/**
 * Default implementation of ConnectableDeviceStore. It stores data in a file in application
 * data directory.
 */
public class DefaultConnectableDeviceStore implements ConnectableDeviceStore {
    // @cond INTERNAL

    public static final String KEY_VERSION = "version";
    public static final String KEY_CREATED = "created";
    public static final String KEY_UPDATED = "updated";
    public static final String KEY_DEVICES = "devices";

    static final int CURRENT_VERSION = 0;

    static final String DIRPATH = "/android/data/connect_sdk/";
    static final String FILENAME = "StoredDevices";

    static final String IP_ADDRESS = "ipAddress";
    static final String FRIENDLY_NAME = "friendlyName";
    static final String MODEL_NAME = "modelName";
    static final String MODEL_NUMBER = "modelNumber";
    static final String SERVICES = "services";
    static final String DESCRIPTION = "description";
    static final String CONFIG = "config";

    static final String FILTER = "filter";
    static final String UUID = "uuid";
    static final String PORT = "port";

    static final String SERVICE_UUID = "serviceUUID";
    static final String CLIENT_KEY = "clientKey";
    static final String SERVER_CERTIFICATE = "serverCertificate";
    static final String PAIRING_KEY = "pairingKey";

    static final String DEFAULT_SERVICE_WEBOSTV = "WebOSTVService";
    static final String DEFAULT_SERVICE_NETCASTTV = "NetcastTVService";

    // @endcond

    /** Date (in seconds from 1970) that the ConnectableDeviceStore was created. */
    public long created;
    /** Date (in seconds from 1970) that the ConnectableDeviceStore was last updated. */
    public long updated;
    /** Current version of the ConnectableDeviceStore, may be necessary for migrations */
    public int version;


    // @cond INTERNAL
    private String fileFullPath;

    private Map<String, JSONObject> storedDevices = new ConcurrentHashMap<String, JSONObject>();
    private Map<String, ConnectableDevice> activeDevices = new ConcurrentHashMap<String, ConnectableDevice>();

    public DefaultConnectableDeviceStore(Context context) { 
        fileFullPath = context.getDataDir() + "/" + FILENAME;
        
        load();
    }
    // @endcond

    @Override
    public void addDevice(ConnectableDevice device) {
        if (device == null || device.getServices().size() == 0)
            return;

        if (!activeDevices.containsKey(device.getId()))
            activeDevices.put(device.getId(), device);

        JSONObject storedDevice = getStoredDevice(device.getId());

        if (storedDevice != null) {
            updateDevice(device);
        } else {
            storedDevices.put(device.getId(), device.toJSONObject());

            store();
        }
    }

    @Override
    public void removeDevice(ConnectableDevice device) {
        if (device == null)
            return;

        activeDevices.remove(device.getId());
        storedDevices.remove(device.getId());

        store();
    }

    @Override
    public void updateDevice(ConnectableDevice device) {
        if (device == null || device.getServices().size() == 0)
            return;

        JSONObject storedDevice = getStoredDevice(device.getId());

        if (storedDevice == null)
            return;

        try {
            storedDevice.put(ConnectableDevice.KEY_LAST_IP, device.getLastKnownIPAddress());
            storedDevice.put(ConnectableDevice.KEY_LAST_SEEN, device.getLastSeenOnWifi());
            storedDevice.put(ConnectableDevice.KEY_LAST_CONNECTED, device.getLastConnected());
            storedDevice.put(ConnectableDevice.KEY_LAST_DETECTED, device.getLastDetection());

            JSONObject services = storedDevice.optJSONObject(ConnectableDevice.KEY_SERVICES);

            if (services == null)
                services = new JSONObject();

            for (DeviceService service : device.getServices()) {
                JSONObject serviceInfo = service.toJSONObject();

                if (serviceInfo != null)
                    services.put(service.getServiceDescription().getUUID(), serviceInfo);
            }

            storedDevice.put(ConnectableDevice.KEY_SERVICES, services);

            storedDevices.put(device.getId(), storedDevice);
            activeDevices.put(device.getId(), device);

            store();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeAll() {
        activeDevices.clear();
        storedDevices.clear();

        store();
    }

    @Override
    public JSONObject getStoredDevices() {
        JSONObject ret = new JSONObject();

        for (java.util.Map.Entry<String, JSONObject> entry: storedDevices.entrySet()) {
            try {
                ret.put(entry.getKey(), entry.getValue());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    @Override
    public ConnectableDevice getDevice(String uuid) {
        if (uuid == null || uuid.length() == 0)
            return null;

        ConnectableDevice foundDevice = getActiveDevice(uuid);

        if (foundDevice == null) {
            JSONObject foundDeviceInfo = getStoredDevice(uuid);

            if (foundDeviceInfo != null)
                foundDevice = new ConnectableDevice(foundDeviceInfo);
        }

        return foundDevice;
    }

    private ConnectableDevice getActiveDevice(String uuid) {
        ConnectableDevice foundDevice = activeDevices.get(uuid);

        if (foundDevice == null) {
            for (ConnectableDevice device : activeDevices.values()) {
                for (DeviceService service : device.getServices()) {
                    if (uuid.equals(service.getServiceDescription().getUUID())) {
                        return device;
                    }
                }
            }
        }
        return foundDevice;
    }

    private JSONObject getStoredDevice(String uuid) {
        JSONObject foundDevice = storedDevices.get(uuid);

        if (foundDevice == null) {
            for (JSONObject device: storedDevices.values()) {
                JSONObject services = device.optJSONObject(ConnectableDevice.KEY_SERVICES);

                if (services != null && services.has(uuid))
                    return device;
            }
        }
        return foundDevice;
    }

    @Override
    public ServiceConfig getServiceConfig(ServiceDescription serviceDescription) {
        if (serviceDescription == null) {
            return null;            
        }
        String uuid = serviceDescription.getUUID();
        if (uuid == null || uuid.length() == 0) {
            return null;
        }

        JSONObject device = getStoredDevice(uuid);
        if (device != null) {
            JSONObject services = device.optJSONObject(ConnectableDevice.KEY_SERVICES);
            if (services != null) {
                JSONObject service = services.optJSONObject(uuid);
                if (service != null) {
                    JSONObject serviceConfigInfo = service.optJSONObject(DeviceService.KEY_CONFIG);
                    if (serviceConfigInfo != null) {
                        return ServiceConfig.getConfig(serviceConfigInfo);
                    }
                }
            }
        }

        return null;
    }

    // @cond INTERNAL
    private void load() {
        String line;

        File file = new File(fileFullPath);

        if (!file.exists()) {
            version = CURRENT_VERSION;
            created = Util.getTime();
            updated = Util.getTime();
        } else {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))){
                
                StringBuilder sb = new StringBuilder();

                while ((line = in.readLine()) != null) {
                    sb.append(line);
                }

                JSONObject data = new JSONObject(sb.toString());
                JSONArray deviceArray = data.optJSONArray(KEY_DEVICES);
                if (deviceArray != null) {
                    for (int i = 0; i < deviceArray.length(); i++) {
                        JSONObject device = deviceArray.getJSONObject(i);
                        storedDevices.put(device.getString(ConnectableDevice.KEY_ID), device);
                    }
                }

                version = data.optInt(KEY_VERSION, CURRENT_VERSION);
                created = data.optLong(KEY_CREATED, 0);
                updated = data.optLong(KEY_UPDATED, 0);
            } catch (IOException|JSONException e) {
                e.printStackTrace();
            }             
        }
    }

    private synchronized void store() {
        updated = Util.getTime();

        File output = new File(fileFullPath);

        if (!output.exists() && !output.getParentFile().mkdirs()) {
            Log.e(Util.T, "Failed to create folders structure to device store "+output.getParentFile().toString());
            return;
        } 

        JSONObject deviceStore = new JSONObject();
        try {
            deviceStore.put(KEY_VERSION, version);
            deviceStore.put(KEY_CREATED, created);
            deviceStore.put(KEY_UPDATED, updated);
            JSONArray deviceArray = new JSONArray(storedDevices.values());
            deviceStore.put(KEY_DEVICES, deviceArray);
            
                        
            try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output), StandardCharsets.UTF_8))) {    
                out.write(deviceStore.toString());
            } 
        } catch (JSONException|IOException e) {
            e.printStackTrace();
        }        
    }


    // @endcond
}
