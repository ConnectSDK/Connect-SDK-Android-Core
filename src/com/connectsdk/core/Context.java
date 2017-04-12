package com.connectsdk.core;

import java.net.InetAddress;

/** Interface that must be implemented for the library to interface with surrounding environment. */
public interface  Context {
    public String getPackageName();
    public String getDataDir();
    public String getApplicationName();
    public InetAddress getIpAddress();
}
