package com.connectsdk.service;

import android.util.Log;

import com.connectsdk.discovery.DiscoveryFilter;
import com.connectsdk.service.config.ServiceConfig;
import com.connectsdk.service.config.ServiceDescription;

public class NewAndroidService extends DeviceService {

    public static final String ID = "AndroidTV2";

    public NewAndroidService(ServiceDescription serviceDescription, ServiceConfig serviceConfig) {
        super(serviceDescription, serviceConfig);
    }

    public static DiscoveryFilter discoveryFilter() {
        Log.d("NEW_ANDROID_SERVICE", "discoveryFilter()");
        return new DiscoveryFilter(ID, "_androidtvremote2._tcp.local.");
    }

    @Override
    public void connect() {
        connected = true;

        reportConnected(true);
    }

    @Override
    public void disconnect() {
        connected = false;
    }

    @Override
    public boolean isConnectable() {
        return true;
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

}
