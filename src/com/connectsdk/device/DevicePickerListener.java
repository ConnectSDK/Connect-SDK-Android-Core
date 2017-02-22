package com.connectsdk.device;

public interface DevicePickerListener {
    /**
     * Called when the user selects a device.
     * @param device the selected device
     */
    public void onPickDevice(ConnectableDevice device);

    /**
     * Called when the picker fails or was cancelled by the user.
     * @param canceled true if picker was canceled by user, false if due to error
     */
    public void onPickDeviceFailed(boolean canceled);
}
