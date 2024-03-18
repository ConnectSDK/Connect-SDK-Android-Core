/*
 * Copyright (c) 2020 LG Electronics Inc.
 * SPDX-License-Identifier: LicenseRef-LGE-Proprietary
 */
package com.connectsdk.service.webos.lgcast.common.connection;

import androidx.annotation.NonNull;
import com.connectsdk.device.ConnectableDevice;
import com.connectsdk.device.ConnectableDeviceListener;
import com.connectsdk.service.DeviceService;
import com.connectsdk.service.command.ServiceCommandError;
import com.connectsdk.service.webos.lgcast.common.utils.HandlerThreadEx;
import com.connectsdk.service.webos.lgcast.common.utils.JSONObjectEx;
import com.connectsdk.service.webos.lgcast.common.utils.Logger;
import com.connectsdk.service.webos.lgcast.common.utils.TimerUtil;
import com.connectsdk.service.webos.lgcast.screenmirroring.uibc.UibcAccessibilityService;
import java.util.List;
import java.util.Timer;
import org.json.JSONObject;

public class ConnectionManager implements ConnectableDeviceListener {
    public enum ConnectionState {
        NONE, CONNECTING, CONNECTED, DISCONNECTING, DISCONNECTED
    }

    private static final long SEND_KEEP_ALIVE_DELAY = 30000;
    private static final long SEND_KEEP_ALIVE_PERIOD = 60000;

    private static final String COMMAND_PLAY = "PLAY";
    private static final String COMMAND_STOP = "STOP";
    private static final String COMMAND_PAUSE = "PAUSE";
    private static final String COMMAND_TEARDOWN = "TEARDOWN";
    private static final String COMMAND_SET_PARAMETER = "SET_PARAMETER";
    private static final String COMMAND_GET_PARAMETER = "GET_PARAMETER";

    private static final String KEY_SUBSCRIBED = "subscribed";
    private static final String KEY_CMD = "cmd";
    private static final String KEY_CLIENTKEY = "clientKey";
    private static final String KEY_UIBCINFO = "uibcInfo";
    private static final String KEY_PROCESSING = "processing";
    private static final String VAL_REQUEST_POWER_OFF = "Request Power Off";

    private String mServiceName;
    private ConnectionState mCurrentState;

    private HandlerThreadEx mConnectionHandler;
    private HandlerThreadEx mUibcRecvHandler;

    private ConnectableDevice mConnectableDevice;
    private ConnectionManagerListener mConnectionManagerListener;
    private LGCastCommand mLGCastCommand;

    private boolean mKeepConnection;
    private Timer mKeepAliveTimer;

    public ConnectionManager(String serviceName) {
        mServiceName = serviceName;
    }

    public void openConnection(ConnectableDevice connectableDevice, ConnectionManagerListener connectionListener) {
        Logger.print("openConnection");
        mCurrentState = ConnectionState.CONNECTING;

        try {
            mConnectionHandler = new HandlerThreadEx("Connection Handler");
            mConnectionHandler.start();

            mUibcRecvHandler = new HandlerThreadEx("UIBC Recv Handler");
            mUibcRecvHandler.start();

            mConnectableDevice = connectableDevice;
            mConnectionManagerListener = connectionListener;
            mLGCastCommand = LGCastCommand.newInstance(mConnectableDevice);
            if (mConnectableDevice == null || mConnectionManagerListener == null || mLGCastCommand == null) throw new Exception("Invalid arguments");

            mConnectableDevice.addListener(this);
            mKeepConnection = mConnectableDevice.isConnected();

            if (mKeepConnection == true) {
                Logger.error("Device already connected");
                onDeviceReady(mConnectableDevice);
            } else {
                Logger.error("Create new connection");
                mConnectableDevice.connect(); //-> onDeviceReady
            }
        } catch (Exception e) {
            Logger.trace(e);
            callOnConnectionFailed("Error: " + e.getMessage());
        }
    }

    public void closeConnection() {
        Logger.print("closeConnection");
        mCurrentState = ConnectionState.DISCONNECTING;

        if (mKeepAliveTimer != null) mKeepAliveTimer.cancel();
        mKeepAliveTimer = null;

        if (mLGCastCommand != null) mLGCastCommand.sendTeardown(mServiceName);
        mLGCastCommand = null;

        if (mConnectableDevice != null) mConnectableDevice.removeListener(this);
        if (mConnectableDevice != null && mKeepConnection == false) mConnectableDevice.disconnect();
        mConnectableDevice = null;

        if (mUibcRecvHandler != null) mUibcRecvHandler.quit();
        mUibcRecvHandler = null;

        if (mConnectionHandler != null) mConnectionHandler.quit();
        mConnectionHandler = null;

        mCurrentState = ConnectionState.DISCONNECTED;
    }

    public void notifyScreenOnOff(boolean isOn) {
        Logger.print("notifyScreenOnOff (isOn=%s)", isOn);
        if (mCurrentState == ConnectionState.CONNECTED) {
            mConnectionHandler.post(() -> {
                if (mLGCastCommand == null) return;
                if (isOn == true) mLGCastCommand.sendPlay();
                else mLGCastCommand.sendStop();
            });
        } else {
            Logger.error("Device NOT connected");
        }
    }

    public void setSourceDeviceCapability(JSONObject capability, JSONObject mobileDescription) {
        Logger.print("setSourceDeviceCapability");
        if (mLGCastCommand == null) return;
        if (mCurrentState == ConnectionState.CONNECTED) mConnectionHandler.post(() -> mLGCastCommand.sendSetParameter(mServiceName, capability, mobileDescription));
        else callOnConnectionFailed("Device NOT connected");

        if (mKeepAliveTimer != null) mKeepAliveTimer.cancel();
        mKeepAliveTimer = TimerUtil.schedule(() -> {
            Logger.debug("Send keep alive");
            if (mLGCastCommand == null) return;
            mLGCastCommand.sendKeepAlive();
        }, SEND_KEEP_ALIVE_DELAY, SEND_KEEP_ALIVE_PERIOD);
    }

    public void updateSourceDeviceCapability(JSONObject capability) {
        Logger.print("updateSourceDeviceCapability: %s", (capability != null) ? capability.toString() : "");
        if (mLGCastCommand == null) return;
        if (mCurrentState == ConnectionState.CONNECTED) mConnectionHandler.post(() -> mLGCastCommand.sendSetParameter(mServiceName, capability, null));
        else Logger.error("Device NOT connected");
    }

    // Remote Camera
    public void sendGetParameterResponse(JSONObject parameter) {
        Logger.print("sendGetParameterResponse");
        if (mLGCastCommand == null) return;
        if (mCurrentState == ConnectionState.CONNECTED && parameter != null) mConnectionHandler.post(() -> mLGCastCommand.sendGetParameterResponse(mServiceName, parameter));
        else callOnConnectionFailed("Device NOT connected");
    }

    // Remote Camera
    public void sendSetParameterResponse(JSONObjectEx parameter) {
        Logger.print("sendSetParameterResponse");
        if (mLGCastCommand == null) return;
        if (mCurrentState == ConnectionState.CONNECTED && parameter != null) mConnectionHandler.post(() -> mLGCastCommand.sendSetParameterResponse(mServiceName, parameter.toJSONObject()));
        else callOnConnectionFailed("Device NOT connected");
    }

    @Override
    public void onPairingRequired(ConnectableDevice device, DeviceService service, DeviceService.PairingType pairingType) {
        Logger.print("onPairingRequired");
        callOnPairingRequested();
    }

    @Override
    public void onDeviceReady(ConnectableDevice device) {
        Logger.print("onDeviceReady");
        if (mConnectionHandler != null) {
            mConnectionHandler.post(this::subscribe);
        }
    }

    @Override
    public void onCapabilityUpdated(ConnectableDevice device, List<String> added, List<String> removed) {
        Logger.print("onCapabilityUpdated");
    }

    @Override
    public void onDeviceDisconnected(ConnectableDevice device) {
        Logger.error("onDeviceDisconnected");
        if (mCurrentState == ConnectionState.CONNECTING) callOnPairingRejected();
        else if (mCurrentState == ConnectionState.CONNECTED) callOnError(ConnectionManagerError.DEVICE_SHUTDOWN, "device disconnected");
        else Logger.debug("Ignore event (state=%s)", mCurrentState);
    }

    @Override
    public void onConnectionFailed(ConnectableDevice device, ServiceCommandError error) {
        Logger.error("onConnectionFailed (error=%s)", error.getMessage());
        if (mCurrentState == ConnectionState.CONNECTING) callOnConnectionFailed("connection failed");
        else if (mCurrentState == ConnectionState.CONNECTED) callOnError(ConnectionManagerError.CONNECTION_CLOSED, "connection closed");
        else Logger.debug("Ignore event (state=%s)", mCurrentState);
    }

    private void subscribe() {
        Logger.print("subscribe");
        if (mLGCastCommand != null) mLGCastCommand.subscribeForServiceCommand(response -> {
            if (response == null) return;
            Logger.debug("Service command: " + response);
            if (response.has(KEY_SUBSCRIBED) == true) handleSubscribed(response);
            else if (response.has(KEY_CMD) == true) handleCommand(response);
        }, mConnectionHandler.getHandler());

        if (mLGCastCommand != null) mLGCastCommand.subscribeForUserInput(response -> {
            if (response == null) return;
            Logger.debug("User input: " + response);
            JSONObject uibcInfo = response.optJSONObject(KEY_UIBCINFO);
            UibcAccessibilityService.sendUibcInfo(uibcInfo);
        }, mUibcRecvHandler.getHandler());

        if (mLGCastCommand != null) mLGCastCommand.subscribeForPowerState(response -> {
            if (response == null) return;
            Logger.debug("Power state: " + response);
            String processing = response.optString(KEY_PROCESSING);
            if (VAL_REQUEST_POWER_OFF.equalsIgnoreCase(processing) == true) callOnError(ConnectionManagerError.DEVICE_SHUTDOWN, "device shut down");
        }, mConnectionHandler.getHandler());
    }

    private void handleSubscribed(@NonNull JSONObject response) {
        Logger.print("handleSubscribed");
        if (response.optBoolean(KEY_SUBSCRIBED, false) == true) sendConnect();
        else callOnConnectionFailed("subscribe failure");
    }

    private void sendConnect() {
        Logger.print("sendConnect");
        if (mLGCastCommand == null) return;
        if (mLGCastCommand.sendConnect(mServiceName) == true) getParameter();
        else callOnConnectionFailed("sendConnect failure");
    }

    private void getParameter() {
        Logger.print("getParameter");
        if (mLGCastCommand == null) return;
        JSONObject result = mLGCastCommand.sendGetParameter(mServiceName);
        if (result != null) callOnConnectionCompleted(result);
        else callOnConnectionFailed("getParameter error");
    }

    // Send capability of source device (Mobile).
    //private void setParameter(JSONObject sourceDeviceSpec, JSONObject mobileDeviceSpec) {
    //    Logger.print("setParameter");
    //
    //    try {
    //        if (sourceDeviceSpec == null || mobileDeviceSpec == null) throw new Exception("Invalid arguments");
    //        if (mLGCastCommand.sendSetParameter(mServiceName, sourceDeviceSpec, mobileDeviceSpec) == false) throw new Exception("sendSetParameter error");
    //    } catch (Exception e) {
    //        Logger.error(e);
    //        callOnConnectionFailed(e.getMessage());
    //    }
    //}

    private void handleCommand(@NonNull JSONObject jsonObj) {
        String command = jsonObj.optString(KEY_CMD);
        String targetClientKey = jsonObj.optString(KEY_CLIENTKEY);
        Logger.print("handleCommand (%s)", command);
        if (mLGCastCommand == null) return;

        if (command == null || targetClientKey == null) {
            Logger.error("Invalid command (%s, %s)", command, targetClientKey);
            return;
        }

        if (targetClientKey.equals(mLGCastCommand.getClientKey()) == false) {
            Logger.error("Client key not matched (%s, %s)", targetClientKey, mLGCastCommand.getClientKey());
            return;
        }

        switch (command) {
            case COMMAND_PLAY:
                callOnReceivePlayCommand(jsonObj);
                break;

            case COMMAND_STOP:
            case COMMAND_PAUSE:
                callOnReceiveStopCommand(jsonObj);
                break;

            case COMMAND_TEARDOWN:
                callOnError(ConnectionManagerError.RENDERER_TERMINATED, "renderer terminated");
                break;

            case COMMAND_GET_PARAMETER:
                callOnReceiveGetParameter(jsonObj);
                break;

            case COMMAND_SET_PARAMETER:
                callOnReceiveSetParameter(jsonObj);
                break;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    private void callOnPairingRequested() {
        //null check
        if (mConnectionHandler != null)
        {
            mConnectionHandler.post(() -> mConnectionManagerListener.onPairingRequested());
        }
    }

    private void callOnPairingRejected() {
        if (mConnectionHandler != null) {
            mConnectionHandler.post(() -> mConnectionManagerListener.onPairingRejected());
        }
        mCurrentState = ConnectionState.NONE;
    }

    private void callOnConnectionFailed(@NonNull String message) {
        if (mConnectionHandler != null) {
            mConnectionHandler.post(() -> mConnectionManagerListener.onConnectionFailed(message));
        }
        mCurrentState = ConnectionState.NONE;
    }

    private void callOnConnectionCompleted(@NonNull JSONObject jsonResult) {
        if (mConnectionHandler != null) {
            mConnectionHandler.post(() -> mConnectionManagerListener.onConnectionCompleted(jsonResult));
        }
        mCurrentState = ConnectionState.CONNECTED;
    }

    private void callOnReceivePlayCommand(@NonNull JSONObject jsonObj) {
        if (mConnectionHandler != null) {
            mConnectionHandler.post(() -> mConnectionManagerListener.onReceivePlayCommand(jsonObj));
        }
    }

    private void callOnReceiveStopCommand(@NonNull JSONObject jsonObj) {
        if (mConnectionHandler != null) {
            mConnectionHandler.post(() -> mConnectionManagerListener.onReceiveStopCommand(jsonObj));
        }
    }

    private void callOnReceiveGetParameter(@NonNull JSONObject jsonObj) {
        if (mConnectionHandler != null) {
            mConnectionHandler.post(() -> mConnectionManagerListener.onReceiveGetParameter(jsonObj));
        }
    }

    private void callOnReceiveSetParameter(@NonNull JSONObject jsonObj) {
        if (mConnectionHandler != null) {
            mConnectionHandler.post(() -> mConnectionManagerListener.onReceiveSetParameter(jsonObj));
        }
    }

    private void callOnError(@NonNull ConnectionManagerError connectionError, @NonNull String message) {
        if (mConnectionHandler != null) {
            mConnectionHandler.post(() -> mConnectionManagerListener.onError(connectionError, message));
        }
        mCurrentState = ConnectionState.NONE;
    }
}
