/*
 * WebOSTVService
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

package com.connectsdk.service;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Base64;
import android.util.Log;

import com.connectsdk.core.AppInfo;
import com.connectsdk.core.ChannelInfo;
import com.connectsdk.core.ExternalInputInfo;
import com.connectsdk.core.ImageInfo;
import com.connectsdk.core.MediaInfo;
import com.connectsdk.core.ProgramInfo;
import com.connectsdk.core.ProgramList;
import com.connectsdk.core.Util;
import com.connectsdk.device.ConnectableDevice;
import com.connectsdk.discovery.DiscoveryFilter;
import com.connectsdk.discovery.DiscoveryManager;
import com.connectsdk.discovery.DiscoveryManager.PairingLevel;
import com.connectsdk.service.capability.ExternalInputControl;
import com.connectsdk.service.capability.KeyControl;
import com.connectsdk.service.capability.Launcher;
import com.connectsdk.service.capability.MediaControl;
import com.connectsdk.service.capability.MediaPlayer;
import com.connectsdk.service.capability.MouseControl;
import com.connectsdk.service.capability.PowerControl;
import com.connectsdk.service.capability.TVControl;
import com.connectsdk.service.capability.TextInputControl;
import com.connectsdk.service.capability.ToastControl;
import com.connectsdk.service.capability.VolumeControl;
import com.connectsdk.service.capability.WebAppLauncher;
import com.connectsdk.service.capability.listeners.ResponseListener;
import com.connectsdk.service.command.NotSupportedServiceSubscription;
import com.connectsdk.service.command.ServiceCommand;
import com.connectsdk.service.command.ServiceCommandError;
import com.connectsdk.service.command.ServiceSubscription;
import com.connectsdk.service.command.URLServiceSubscription;
import com.connectsdk.service.config.ServiceConfig;
import com.connectsdk.service.config.ServiceDescription;
import com.connectsdk.service.config.WebOSTVServiceConfig;
import com.connectsdk.service.sessions.LaunchSession;
import com.connectsdk.service.sessions.LaunchSession.LaunchSessionType;
import com.connectsdk.service.sessions.WebAppSession;
import com.connectsdk.service.sessions.WebAppSession.WebAppPinStatusListener;
import com.connectsdk.service.sessions.WebOSWebAppSession;
import com.connectsdk.service.webos.WebOSTVKeyboardInput;
import com.connectsdk.service.webos.WebOSTVMouseSocketConnection;
import com.connectsdk.service.webos.WebOSTVServiceSocketClient;
import com.connectsdk.service.webos.WebOSTVServiceSocketClient.WebOSTVServiceSocketClientListener;
import com.lge.tms.loader.config.TmsConfig;
import com.lge.tms.loader.utils.LLog;

import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@SuppressLint("DefaultLocale")
public class WebOSTVService extends DeviceService implements Launcher, MediaControl, MediaPlayer, VolumeControl, TVControl, ToastControl, ExternalInputControl, MouseControl, TextInputControl, PowerControl, KeyControl, WebAppLauncher {

    public static final String ID = "webOS TV";

    public interface WebOSTVServicePermission {
        public enum Open implements WebOSTVServicePermission {
            LAUNCH,
            LAUNCH_WEB,
            APP_TO_APP,
            CONTROL_AUDIO,
            CONTROL_INPUT_MEDIA_PLAYBACK
        }

        public static final WebOSTVServicePermission[] OPEN = {
            Open.LAUNCH,
            Open.LAUNCH_WEB,
            Open.APP_TO_APP,
            Open.CONTROL_AUDIO,
            Open.CONTROL_INPUT_MEDIA_PLAYBACK
        };

        public enum Protected implements WebOSTVServicePermission {
            CONTROL_POWER,
            READ_INSTALLED_APPS,
            CONTROL_DISPLAY,
            CONTROL_INPUT_JOYSTICK,
            CONTROL_INPUT_MEDIA_RECORDING,
            CONTROL_INPUT_TV,
            READ_INPUT_DEVICE_LIST,
            READ_NETWORK_STATE,
            READ_TV_CHANNEL_LIST,
            WRITE_NOTIFICATION_TOAST,
            CONTROL_BLUETOOTH,
            CHECK_BLUETOOTH_DEVICE,
            CONTROL_TV_SCREEN
        }

        public static final WebOSTVServicePermission[] PROTECTED = {
            Protected.CONTROL_POWER,
            Protected.READ_INSTALLED_APPS,
            Protected.CONTROL_DISPLAY,
            Protected.CONTROL_INPUT_JOYSTICK,
            Protected.CONTROL_INPUT_MEDIA_RECORDING,
            Protected.CONTROL_INPUT_TV,
            Protected.READ_INPUT_DEVICE_LIST,
            Protected.READ_NETWORK_STATE,
            Protected.READ_TV_CHANNEL_LIST,
            Protected.WRITE_NOTIFICATION_TOAST,
            Protected.CONTROL_BLUETOOTH,
            Protected.CHECK_BLUETOOTH_DEVICE,
            Protected.CONTROL_TV_SCREEN
        };

        public enum PersonalActivity implements WebOSTVServicePermission {
            CONTROL_INPUT_TEXT,
            CONTROL_MOUSE_AND_KEYBOARD,
            READ_CURRENT_CHANNEL,
            READ_RUNNING_APPS
        }

        public static final WebOSTVServicePermission[] PERSONAL_ACTIVITY = {
            PersonalActivity.CONTROL_INPUT_TEXT,
            PersonalActivity.CONTROL_MOUSE_AND_KEYBOARD,
            PersonalActivity.READ_CURRENT_CHANNEL,
            PersonalActivity.READ_RUNNING_APPS
        };
    }

    public final static String[] kWebOSTVServiceOpenPermissions = {
        "LAUNCH",
        "LAUNCH_WEBAPP",
        "APP_TO_APP",
        "CONTROL_AUDIO",
        "CONTROL_INPUT_MEDIA_PLAYBACK",
        "UPDATE_FROM_REMOTE_APP"
    };

    public final static String[] kWebOSTVServiceProtectedPermissions = {
        "CONTROL_POWER",
        "READ_INSTALLED_APPS",
        "CONTROL_DISPLAY",
        "CONTROL_INPUT_JOYSTICK",
        "CONTROL_INPUT_MEDIA_RECORDING",
        "CONTROL_INPUT_TV",
        "READ_INPUT_DEVICE_LIST",
        "READ_NETWORK_STATE",
        "READ_TV_CHANNEL_LIST",
        "WRITE_NOTIFICATION_TOAST",
        "CONTROL_BLUETOOTH",
        "CHECK_BLUETOOTH_DEVICE",
        "CONTROL_USER_INFO",
        "CONTROL_TIMER_INFO",
        "READ_SETTINGS",
        "CONTROL_TV_SCREEN"
    };

    public final static String[] kWebOSTVServicePersonalActivityPermissions = {
        "CONTROL_INPUT_TEXT",
        "CONTROL_MOUSE_AND_KEYBOARD",
        "READ_CURRENT_CHANNEL",
        "READ_RUNNING_APPS"
    };


    public interface SecureAccessTestListener extends ResponseListener<Boolean> { }

    public interface ACRAuthTokenListener extends ResponseListener<String> { }

    public interface LaunchPointsListener extends ResponseListener<JSONArray> { }

    static String FOREGROUND_APP = "ssap://com.webos.applicationManager/getForegroundAppInfo";
    static String APP_STATUS = "ssap://com.webos.service.appstatus/getAppStatus";
    static String APP_STATE = "ssap://system.launcher/getAppState";
    static String VOLUME = "ssap://audio/getVolume";
    static String MUTE = "ssap://audio/getMute";
    static String VOLUME_STATUS = "ssap://audio/getStatus";
    static String CHANNEL_LIST = "ssap://tv/getChannelList";
    static String CHANNEL = "ssap://tv/getCurrentChannel";
    static String PROGRAM = "ssap://tv/getChannelProgramInfo";

    static final String CLOSE_APP_URI = "ssap://system.launcher/close";
    static final String CLOSE_MEDIA_URI = "ssap://media.viewer/close";
    static final String CLOSE_WEBAPP_URI = "ssap://webapp/closeWebApp";

    WebOSTVMouseSocketConnection mouseSocket;

    WebOSTVKeyboardInput keyboardInput;

    ConcurrentHashMap<String, String> mAppToAppIdMappings;
    ConcurrentHashMap<String, WebOSWebAppSession> mWebAppSessions;

    WebOSTVServiceSocketClient socket;

    List<String> permissions;
    boolean isMouseConnecting = false;

    public WebOSTVService(ServiceDescription serviceDescription, ServiceConfig serviceConfig) {
        super(serviceDescription, serviceConfig);
        setServiceDescription(serviceDescription);

        pairingType = PairingType.FIRST_SCREEN;

        mAppToAppIdMappings = new ConcurrentHashMap<String, String>();
        mWebAppSessions = new ConcurrentHashMap<String, WebOSWebAppSession>();
    }

    @Override
    public void setPairingType(PairingType pairingType) {
        this.pairingType = pairingType;
    }

    @Override
    public void setServiceDescription(ServiceDescription serviceDescription) {
        super.setServiceDescription(serviceDescription);

        if (this.serviceDescription.getVersion() == null && this.serviceDescription.getResponseHeaders() != null)
        {
            String serverInfo = serviceDescription.getResponseHeaders().get("Server").get(0);
            String systemOS = serverInfo.split(" ")[0];
            String[] versionComponents = systemOS.split("/");
            String systemVersion = versionComponents[versionComponents.length - 1];

            this.serviceDescription.setVersion(systemVersion);

            this.updateCapabilities();
        }
    }

    private DeviceService getDLNAService() {
        Map<String, ConnectableDevice> allDevices = DiscoveryManager.getInstance().getAllDevices();
        ConnectableDevice device = null;
        DeviceService service = null;

        if (allDevices != null && allDevices.size() > 0)
            device = allDevices.get(this.serviceDescription.getIpAddress());

        if (device != null)
            service = device.getServiceByName("DLNA");

        return service;
    }

    public static DiscoveryFilter discoveryFilter() {
        return new DiscoveryFilter(ID, "urn:lge-com:service:webos-second-screen:1");
    }

    @Override
    public boolean isConnected() {
        if (DiscoveryManager.getInstance().getPairingLevel() == PairingLevel.ON) {
            return this.socket != null && this.socket.isConnected() && (((WebOSTVServiceConfig)serviceConfig).getClientKey() != null);
        } else {
            return this.socket != null && this.socket.isConnected();
        }
    }

    @Override
    public void connect() {
        if (this.socket == null) {
            this.socket = new WebOSTVServiceSocketClient(this, WebOSTVServiceSocketClient.getURI(this));
            this.socket.setListener(mSocketListener);
        }

        if (!this.isConnected())
            this.socket.connect();
    }

    @Override
    public void disconnect() {
        Log.d("Connect SDK", "attempting to disconnect to " + serviceDescription.getIpAddress());

        Util.runOnUI(new Runnable() {

            @Override
            public void run() {
                if (listener != null)
                    listener.onDisconnect(WebOSTVService.this, null);
            }
        });

        if (socket != null) {
            socket.setListener(null);
            socket.disconnect();
            socket = null;
        }

        if (mAppToAppIdMappings != null)
            mAppToAppIdMappings.clear();

        if (mWebAppSessions != null) {
            Enumeration<WebOSWebAppSession> iterator = mWebAppSessions.elements();

            while (iterator.hasMoreElements()) {
                WebOSWebAppSession session = iterator.nextElement();
                session.disconnectFromWebApp();
            }

            mWebAppSessions.clear();
        }
    }

    @Override
    public void cancelPairing() {
        if (this.socket != null) {
            this.socket.disconnect();
        }
    }

    private WebOSTVServiceSocketClientListener mSocketListener = new WebOSTVServiceSocketClientListener() {

        @Override
        public void onRegistrationFailed(final ServiceCommandError error) {
            disconnect();

            Util.runOnUI(new Runnable() {

                @Override
                public void run() {
                    if (listener != null)
                        listener.onConnectionFailure(WebOSTVService.this, error);
                }
            });
        }

        @Override
        public Boolean onReceiveMessage(JSONObject message) { return true; }

        @Override
        public void onFailWithError(final ServiceCommandError error) {
            socket.setListener(null);
            socket.disconnect();
            socket = null;

            Util.runOnUI(new Runnable() {

                @Override
                public void run() {
                    if (listener != null)
                        listener.onConnectionFailure(WebOSTVService.this, error);
                }
            });
        }

        @Override
        public void onConnect() {
            reportConnected(true);
        }

        @Override
        public void onCloseWithError(final ServiceCommandError error) {
            socket.setListener(null);
            socket.disconnect();
            socket = null;

            Util.runOnUI(new Runnable() {

                @Override
                public void run() {
                    if (listener != null)
                        listener.onDisconnect(WebOSTVService.this, error);
                }
            });
        }

        @Override
        public void onBeforeRegister(final PairingType pairingType) {
            if (DiscoveryManager.getInstance().getPairingLevel() == PairingLevel.ON) {
                Util.runOnUI(new Runnable() {

                    @Override
                    public void run() {
                        if (listener != null)
                            listener.onPairingRequired(WebOSTVService.this, pairingType, null);
                    }
                });
            }
        }
    };

    // @cond INTERNAL

    public ConcurrentHashMap<String, String> getWebAppIdMappings() {
        return mAppToAppIdMappings;
    }

    // @endcond

    @Override
    public Launcher getLauncher() {
        return this;
    }

    @Override
    public CapabilityPriorityLevel getLauncherCapabilityLevel() {
        return CapabilityPriorityLevel.HIGH;
    }

    @Override
    public void launchApp(String appId, AppLaunchListener listener) {
        AppInfo appInfo = new AppInfo();
        appInfo.setId(appId);

        launchAppWithInfo(appInfo, listener);
    }

    @Override
    public void launchAppWithInfo(AppInfo appInfo, Launcher.AppLaunchListener listener) {
        launchAppWithInfo(appInfo, null, listener);
    }

    @Override
    public void launchAppWithInfo(final AppInfo appInfo, Object params, final Launcher.AppLaunchListener listener) {
        String uri = "ssap://system.launcher/launch";
        JSONObject payload = new JSONObject();

        final String appId = appInfo.getId();

        String contentId = null;

        if (params != null) {
            try {
                contentId = (String) ((JSONObject) params).get("contentId");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        try {
            payload.put("id", appId);

            if (contentId != null)
                payload.put("contentId", contentId);

            if (params != null)
                payload.put("params", params);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ResponseListener<Object> responseListener = new ResponseListener<Object>() {

            @Override
            public void onSuccess(Object response) {
                JSONObject obj = (JSONObject) response;
                LaunchSession launchSession = new LaunchSession();

                launchSession.setService(WebOSTVService.this);
                launchSession.setAppId(appId); // note that response uses id to mean appId
                launchSession.setSessionId(obj.optString("sessionId"));
                launchSession.setSessionType(LaunchSessionType.App);

                Util.postSuccess(listener, launchSession);
            }

            @Override
            public void onError(ServiceCommandError error) {
                Util.postError(listener, error);
            }
        };

        ServiceCommand<ResponseListener<Object>> request = new ServiceCommand<ResponseListener<Object>>(this, uri, payload, true, responseListener);
        request.send();
    }


    @Override
    public void launchBrowser(String url, final Launcher.AppLaunchListener listener) {
        String uri = "ssap://system.launcher/open";
        JSONObject payload = new JSONObject();

        ResponseListener<Object> responseListener = new ResponseListener<Object>() {

            @Override
            public void onSuccess(Object response) {
                JSONObject obj = (JSONObject) response;
                LaunchSession launchSession = new LaunchSession();

                launchSession.setService(WebOSTVService.this);
                launchSession.setAppId(obj.optString("id")); // note that response uses id to mean appId
                launchSession.setSessionId(obj.optString("sessionId"));
                launchSession.setSessionType(LaunchSessionType.App);
                launchSession.setRawData(obj);

                Util.postSuccess(listener, launchSession);
            }

            @Override
            public void onError(ServiceCommandError error) {
                Util.postError(listener, error);
            }
        };

        try {
            payload.put("target", url);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ServiceCommand<ResponseListener<Object>> request = new ServiceCommand<ResponseListener<Object>>(this, uri, payload, true, responseListener);
        request.send();
    }

    @Override
    public void launchYouTube(String contentId, Launcher.AppLaunchListener listener) {
        launchYouTube(contentId, (float) 0.0, listener);
    }

    @Override
    public void launchYouTube(final String contentId, float startTime, final AppLaunchListener listener) {
        JSONObject params = new JSONObject();

        if (contentId != null && contentId.length() > 0) {
            if (startTime < 0.0) {
                Util.postError(listener, new ServiceCommandError(0, "Start time may not be negative", null));
                return;
            }

            try {
                //2015.08.03 hj - 유투브 재생안되는 이슈 수정
//                params.put("contentId", String.format("%s&pairingCode=%s&t=%.1f", contentId, UUID.randomUUID().toString(), startTime));
                params.put("contentId", contentId);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        AppInfo appInfo = new AppInfo() {{
            setId("youtube.leanback.v4");
            setName("YouTube");
        }};

        launchAppWithInfo(appInfo, params, listener);
    }

    @Override
    public void launchHulu(String contentId, Launcher.AppLaunchListener listener) {
        JSONObject params = new JSONObject();

        try {
            params.put("contentId", contentId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        AppInfo appInfo = new AppInfo() {{
            setId("hulu");
            setName("Hulu");
        }};

        launchAppWithInfo(appInfo, params, listener);
    }

    @Override
    public void launchNetflix(String contentId, Launcher.AppLaunchListener listener) {
        JSONObject params = new JSONObject();
        String netflixContentId = "m=http%3A%2F%2Fapi.netflix.com%2Fcatalog%2Ftitles%2Fmovies%2F" + contentId + "&source_type=4";

        try {
            params.put("contentId", netflixContentId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        AppInfo appInfo = new AppInfo() {{
            setId("netflix");
            setName("Netflix");
        }};

        launchAppWithInfo(appInfo, params, listener);
    }

    @Override
    public void launchAppStore(String appId, AppLaunchListener listener) {
        AppInfo appInfo = new AppInfo("com.webos.app.discovery");
        appInfo.setName("LG Store");

        JSONObject params = new JSONObject();

        if (appId != null && appId.length() > 0) {
            String query = String.format("category/GAME_APPS/%s", appId);
            try {
                params.put("query", query);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        launchAppWithInfo(appInfo, params, listener);
    }

    @Override
    public void closeApp(LaunchSession launchSession, ResponseListener<Object> listener) {
        String uri = "ssap://system.launcher/close";
        String appId = launchSession.getAppId();
        String sessionId = launchSession.getSessionId();

        JSONObject payload = new JSONObject();

        try {
            payload.put("id", appId);
            payload.put("sessionId", sessionId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ServiceCommand<ResponseListener<Object>> request = new ServiceCommand<ResponseListener<Object>>(launchSession.getService(), uri, payload, true, listener);
        request.send();
    }

    @Override
    public void getAppList(final AppListListener listener) {
        String uri = "ssap://com.webos.applicationManager/listApps";

        ResponseListener<Object> responseListener = new ResponseListener<Object>() {

            @Override
            public void onSuccess(Object response) {

                try {
                    JSONObject jsonObj = (JSONObject) response;

                    JSONArray apps = (JSONArray) jsonObj.get("apps");
                    List<AppInfo> appList = new ArrayList<AppInfo>();

                    for (int i = 0; i < apps.length(); i++)
                    {
                        final JSONObject appObj = apps.getJSONObject(i);

                        AppInfo appInfo = new AppInfo() {{
                            setId(appObj.getString("id"));
                            setName(appObj.getString("title"));
                            setRawData(appObj);
                        }};

                        appList.add(appInfo);
                    }

                    Util.postSuccess(listener, appList);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(ServiceCommandError error) {
                Util.postError(listener, error);
            }
        };

        ServiceCommand<ResponseListener<Object>> request = new ServiceCommand<ResponseListener<Object>>(this, uri, null, true, responseListener);
        request.send();
    }

    private ServiceCommand<AppInfoListener> getRunningApp(boolean isSubscription, final AppInfoListener listener) {
        ServiceCommand<AppInfoListener> request;

        ResponseListener<Object> responseListener = new ResponseListener<Object>() {

            @Override
            public void onSuccess(Object response) {
                final JSONObject jsonObj = (JSONObject)response;
                AppInfo app = new AppInfo() {{
                    setId(jsonObj.optString("appId"));
                    setName(jsonObj.optString("appName"));
                    setRawData(jsonObj);
                }};

                Util.postSuccess(listener, app);
            }

            @Override
            public void onError(ServiceCommandError error) {
                Util.postError(listener, error);
            }
        };


//        JSONObject payload = new JSONObject();
//        try {
//            payload.put("type", "subscribe");
//            if(isExtraApp)
//                payload.put("extraInfo", true);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }


        if (isSubscription)
            request = new URLServiceSubscription<AppInfoListener>(this, FOREGROUND_APP, null, true, responseListener);
        else
            request = new ServiceCommand<AppInfoListener>(this, FOREGROUND_APP, null, true, responseListener);

        request.send();

        return request;
    }

    @Override
    public void getRunningApp(AppInfoListener listener) {
        getRunningApp(false, listener);
    }

    @Override
    public ServiceSubscription<AppInfoListener> subscribeRunningApp(AppInfoListener listener) {
        return (URLServiceSubscription<AppInfoListener>) getRunningApp(true, listener);
    }

    private ServiceCommand<AppInfoListener> getExtraApp(boolean isSubscription, boolean isExtraApp, final AppInfoListener listener) {
        ServiceCommand<AppInfoListener> request;

        ResponseListener<Object> responseListener = new ResponseListener<Object>() {

            @Override
            public void onSuccess(Object response) {
                final JSONObject jsonObj = (JSONObject)response;
                AppInfo app = new AppInfo() {{
                    setId(jsonObj.optString("appId"));
                    setName(jsonObj.optString("appName"));
                    setRawData(jsonObj);
                }};

                Util.postSuccess(listener, app);
            }

            @Override
            public void onError(ServiceCommandError error) {
                Util.postError(listener, error);
            }
        };


        JSONObject payload = new JSONObject();
        try {
            if(isSubscription)
                payload.put("type", "subscribe");
            if(isExtraApp)
                payload.put("extraInfo", true);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        if (isSubscription)
            request = new URLServiceSubscription<AppInfoListener>(this, FOREGROUND_APP, payload, true, responseListener);
        else
            request = new ServiceCommand<AppInfoListener>(this, FOREGROUND_APP, null, true, responseListener);

        request.send();

        return request;
    }


//    @Override
//    public void getRunningExtraApp(boolean isSubscription, boolean isExtraApp, AppInfoListener listener) {
//        getExtraApp(isSubscription, isExtraApp, listener);
//    }

    @Override
    public ServiceSubscription<AppInfoListener> subscribeRunningExtraApp(boolean isSubscription, boolean isExtraApp, AppInfoListener listener) {
        return (URLServiceSubscription<AppInfoListener>) getExtraApp(isSubscription, isExtraApp, listener);
    }

    private ServiceCommand<AppStateListener> getAppState(boolean subscription, LaunchSession launchSession, final AppStateListener listener) {
        ServiceCommand<AppStateListener> request;
        JSONObject params = new JSONObject();

        try {
//            params.put("appId", launchSession.getAppId());
            params.put("id", launchSession.getAppId());
            params.put("sessionId", launchSession.getSessionId());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ResponseListener<Object> responseListener = new ResponseListener<Object>() {

            @Override
            public void onError(ServiceCommandError error) {
                Util.postError(listener, error);
            }

            @Override
            public void onSuccess(Object object) {
                JSONObject json = (JSONObject) object;
                try {
                    Util.postSuccess(listener, new AppState(json.getBoolean("running"), json.getBoolean("visible")));
                } catch (JSONException e) {
                    Util.postError(listener, new ServiceCommandError(0, "Malformed JSONObject", null));
                    e.printStackTrace();
                }
            }
        };

        if (subscription) {
            request = new URLServiceSubscription<Launcher.AppStateListener>(this, APP_STATE, params, true, responseListener);
        } else {
            request = new ServiceCommand<Launcher.AppStateListener>(this, APP_STATE, params, true, responseListener);
        }

        request.send();

        return request;
    }

    @Override
    public void getAppState(LaunchSession launchSession, AppStateListener listener) {
        getAppState(false, launchSession, listener);
    }

    @Override
    public ServiceSubscription<AppStateListener> subscribeAppState(LaunchSession launchSession, AppStateListener listener) {
        return (URLServiceSubscription<AppStateListener>) getAppState(true, launchSession, listener);
    }


    /**
     * ***************
     * TOAST CONTROL
     * ***************
     */
    @Override
    public ToastControl getToastControl() {
        return this;
    }

    ;

    @Override
    public CapabilityPriorityLevel getToastControlCapabilityLevel() {
        return CapabilityPriorityLevel.HIGH;
    }

    @Override
    public void showToast(String message, ResponseListener<Object> listener) {
        showToast(message, null, null, listener);
    }

    @Override
    public void showToast(String message, String iconData, String iconExtension, ResponseListener<Object> listener) {
        JSONObject payload = new JSONObject();

        try {
            payload.put("message", message);

            if (iconData != null)
            {
                payload.put("iconData", iconData);
                payload.put("iconExtension", iconExtension);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        sendToast(payload, listener);
    }

    @Override
    public void showClickableToastForApp(String message, AppInfo appInfo, JSONObject params, ResponseListener<Object> listener) {
        showClickableToastForApp(message, appInfo, params, null, null, listener);
    }

    @Override
    public void showClickableToastForApp(String message, AppInfo appInfo, JSONObject params, String iconData, String iconExtension, ResponseListener<Object> listener) {
        JSONObject payload = new JSONObject();

        try {
            payload.put("message", message);

            if (iconData != null) {
                payload.put("iconData", iconData);
                payload.put("iconExtension", iconExtension);
            }

            if (appInfo != null) {
                JSONObject onClick = new JSONObject();
                onClick.put("appId", appInfo.getId());
                if (params != null) {
                    onClick.put("params", params);
                }
                payload.put("onClick", onClick);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        sendToast(payload, listener);
    }

    @Override
    public void showClickableToastForURL(String message, String url, ResponseListener<Object> listener) {
        showClickableToastForURL(message, url, null, null, listener);
    }

    @Override
    public void showClickableToastForURL(String message, String url, String iconData, String iconExtension, ResponseListener<Object> listener) {
        JSONObject payload = new JSONObject();

        try {
            payload.put("message", message);

            if (iconData != null) {
                payload.put("iconData", iconData);
                payload.put("iconExtension", iconExtension);
            }

            if (url != null) {
                JSONObject onClick = new JSONObject();
                onClick.put("target", url);
                payload.put("onClick", onClick);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        sendToast(payload, listener);
    }

    private void sendToast(JSONObject payload, ResponseListener<Object> listener) {
        if (!payload.has("iconData"))
        {
            Context context = DiscoveryManager.getInstance().getContext();

            try {
                Drawable drawable = context.getPackageManager().getApplicationIcon(context.getPackageName());

                if(drawable != null) {
                    BitmapDrawable bitDw = ((BitmapDrawable) drawable);
                    Bitmap bitmap = bitDw.getBitmap();

                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);

                    byte[] bitmapByte = stream.toByteArray();
                    bitmapByte = Base64.encode(bitmapByte,Base64.NO_WRAP);
                    String bitmapData = new String(bitmapByte);

                    payload.put("iconData", bitmapData);
                    payload.put("iconExtension", "png");
                }
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        String uri = "palm://system.notifications/createToast";
        ServiceCommand<ResponseListener<Object>> request = new ServiceCommand<ResponseListener<Object>>(this, uri, payload, true, listener);
        request.send();
    }


    /**
     * ***************
     * VOLUME CONTROL
     * ***************
     */
    @Override
    public VolumeControl getVolumeControl() {
        return this;
    }

    @Override
    public CapabilityPriorityLevel getVolumeControlCapabilityLevel() {
        return CapabilityPriorityLevel.HIGH;
    }

    public void volumeUp() {
        volumeUp(null);
    }

    @Override
    public void volumeUp(ResponseListener<Object> listener) {
        String uri = "ssap://audio/volumeUp";
        ServiceCommand<ResponseListener<Object>> request = new ServiceCommand<ResponseListener<Object>>(this, uri, null, true, listener);

        request.send();
    }

    public void volumeDown() {
        volumeDown(null);
    }

    @Override
    public void volumeDown(ResponseListener<Object> listener) {
        String uri = "ssap://audio/volumeDown";
        ServiceCommand<ResponseListener<Object>> request = new ServiceCommand<ResponseListener<Object>>(this, uri, null, true, listener);

        request.send();
    }

    public void setVolume(int volume) { 
        setVolume(volume, null);
    }

    @Override
    public void setVolume(float volume, ResponseListener<Object> listener) {
        String uri = "ssap://audio/setVolume";
        JSONObject payload = new JSONObject();
        int intVolume = (int) Math.round(volume*100.0f);

        try {
            payload.put("volume", intVolume);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ServiceCommand<ResponseListener<Object>> request = new ServiceCommand<ResponseListener<Object>>(this, uri, payload, true, listener);
        request.send();
    }

    private ServiceCommand<VolumeListener> getVolume(boolean isSubscription, final VolumeListener listener) {
        ServiceCommand<VolumeListener> request;

        ResponseListener<Object> responseListener = new ResponseListener<Object>() {

            @Override
            public void onSuccess(Object response) {

                try {
                    JSONObject jsonObj = (JSONObject)response;
                    int iVolume = (Integer) jsonObj.get("volume");
                    float fVolume = (float) (iVolume / 100.0);

                    Util.postSuccess(listener, fVolume);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(ServiceCommandError error) {
                Util.postError(listener, error);
            }
        };

        if (isSubscription)
            request = new URLServiceSubscription<VolumeListener>(this, VOLUME, null, true, responseListener);
        else
            request = new ServiceCommand<VolumeListener>(this, VOLUME, null, true, responseListener);

        request.send();

        return request;
    }

    @Override
    public void getVolume(VolumeListener listener) {
        getVolume(false, listener);
    }

    @SuppressWarnings("unchecked")
    @Override
    public ServiceSubscription<VolumeListener> subscribeVolume(VolumeListener listener) {
        return (ServiceSubscription<VolumeListener>) getVolume(true, listener);
    }

    public void mute() {
        sendSpecialKey("MUTE", null);
    }
    //audio guidance 이슈로 사용하지 않음(WOSLQEVENT-119595), keycode + getmute로 변경
    @Override
    public void setMute(boolean isMute, ResponseListener<Object> listener) {
        String uri = "ssap://audio/setMute";
        JSONObject payload = new JSONObject();

        try {
            payload.put("mute", isMute);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ServiceCommand<ResponseListener<Object>> request = new ServiceCommand<ResponseListener<Object>>(this, uri, payload, true, listener);
        request.send();
    }

    private ServiceCommand<ResponseListener<Object>> getMuteStatus(boolean isSubscription, final MuteListener listener) {
        ServiceCommand<ResponseListener<Object>> request;

        ResponseListener<Object> responseListener = new ResponseListener<Object>() {

            @Override
            public void onSuccess(Object response) {
                try {
                    JSONObject jsonObj = (JSONObject)response;
                    boolean isMute = (Boolean) jsonObj.get("mute");
                    Util.postSuccess(listener, isMute);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(ServiceCommandError error) {
                Util.postError(listener, error);
            }
        };

        if (isSubscription)
            request = new URLServiceSubscription<ResponseListener<Object>>(this, MUTE, null, true, responseListener);
        else
            request = new ServiceCommand<ResponseListener<Object>>(this, MUTE, null, true, responseListener);

        request.send();

        return request;
    }

    @Override
    public void getMute(MuteListener listener) {
        getMuteStatus(false, listener);
    }

    @SuppressWarnings("unchecked")
    @Override
    public ServiceSubscription<MuteListener> subscribeMute(MuteListener listener) {
        return (ServiceSubscription<MuteListener>) getMuteStatus(true, listener);
    }

    private ServiceCommand<ResponseListener<Object>> getVolumeStatus(boolean isSubscription, final VolumeStatusListener listener) {
        ServiceCommand<ResponseListener<Object>> request;

        ResponseListener<Object> responseListener = new ResponseListener<Object>() {

            @Override
            public void onSuccess(Object response) {
                try {
                    JSONObject jsonObj = (JSONObject) response;
                    boolean isMute = (Boolean) jsonObj.get("mute");
                    int iVolume = jsonObj.getInt("volume");
                    float fVolume = (float) (iVolume / 100.0);

                    Util.postSuccess(listener, new VolumeStatus(isMute, fVolume));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(ServiceCommandError error) {
                Util.postError(listener, error);
            }
        };

        if (isSubscription)
            request = new URLServiceSubscription<ResponseListener<Object>>(this, VOLUME_STATUS, null, true, responseListener);
        else
            request = new ServiceCommand<ResponseListener<Object>>(this, VOLUME_STATUS, null, true, responseListener);

        request.send();

        return request;
    }

    public void getVolumeStatus(VolumeStatusListener listener) {
        getVolumeStatus(false, listener);
    }

    @SuppressWarnings("unchecked")
    public ServiceSubscription<VolumeStatusListener> subscribeVolumeStatus(VolumeStatusListener listener) {
        return (ServiceSubscription<VolumeStatusListener>) getVolumeStatus(true, listener);
    }


    /**
     * ***************
     * MEDIA PLAYER
     * ***************
     */
    @Override
    public MediaPlayer getMediaPlayer() {
        return this;
    }

    @Override
    public CapabilityPriorityLevel getMediaPlayerCapabilityLevel() {
        return CapabilityPriorityLevel.HIGH;
    }

    @Override
    public void getMediaInfo(MediaInfoListener listener) {
        Util.postError(listener, ServiceCommandError.notSupported());
    }

    @Override
    public ServiceSubscription<MediaInfoListener> subscribeMediaInfo(
            MediaInfoListener listener) {
        listener.onError(ServiceCommandError.notSupported());
        return null;
    }

    private void displayMedia(JSONObject params, final MediaPlayer.LaunchListener listener) {
        String uri = "ssap://media.viewer/open";

        ResponseListener<Object> responseListener = new ResponseListener<Object>() {
            @Override
            public void onSuccess(Object response) {
                JSONObject obj = (JSONObject) response;

                LaunchSession launchSession = LaunchSession.launchSessionForAppId(obj.optString("id"));
                launchSession.setService(WebOSTVService.this);
                launchSession.setSessionId(obj.optString("sessionId"));
                launchSession.setSessionType(LaunchSessionType.Media);

                Util.postSuccess(listener, new MediaLaunchObject(launchSession, WebOSTVService.this));
            }

            @Override
            public void onError(ServiceCommandError error) {
                Util.postError(listener, error);
            }
        };

        ServiceCommand<ResponseListener<Object>> request = new ServiceCommand<ResponseListener<Object>>(this, uri, params, true, responseListener);
        request.send();
    }

    @Override
    public void displayImage(final String url, final String mimeType, final String title, final String description, final String iconSrc, final MediaPlayer.LaunchListener listener) {
        if ("4.0.0".equalsIgnoreCase(this.serviceDescription.getVersion())) {
            DeviceService dlnaService = this.getDLNAService();

            if (dlnaService != null) {
                MediaPlayer mediaPlayer = dlnaService.getAPI(MediaPlayer.class);

                if (mediaPlayer != null) {
                    mediaPlayer.displayImage(url, mimeType, title, description, iconSrc, listener);
                    return;
                }
            }

            JSONObject params = null;

            try {
                params = new JSONObject() {{
                    put("target", url);
                    put("title", title == null ? NULL : title);
                    put("description", description == null ? NULL : description);
                    put("mimeType", mimeType == null ? NULL : mimeType);
                    put("iconSrc", iconSrc == null ? NULL : iconSrc);
                }};
            } catch (JSONException ex) {
                ex.printStackTrace();
                Util.postError(listener, new ServiceCommandError(-1, ex.getLocalizedMessage(), ex));
            }

            if (params != null)
                this.displayMedia(params, listener);
        } else {
            final String webAppId = "MediaPlayerTest";

            final WebAppSession.LaunchListener webAppLaunchListener = new WebAppSession.LaunchListener() {

                @Override
                public void onError(ServiceCommandError error) {
                    listener.onError(error);
                }

                @Override
                public void onSuccess(WebAppSession webAppSession) {
                    webAppSession.displayImage(url, mimeType, title, description, iconSrc, listener);
                }
            };

            getRunningApp(false, new AppInfoListener() {
                @Override
                public void onSuccess(AppInfo object) {
                    if (object.getId().equals("com.lgsmartplatform.redirect.MediaPlayer") || object.getId().equals("com.webos.app.webapphost")) {
                        System.out.println("[DEBUG] getRunningApp is media player");

                        getWebAppLauncher().joinWebApp(webAppId, new WebAppSession.LaunchListener() {

                            @Override
                            public void onError(ServiceCommandError error) {
                                getWebAppLauncher().launchWebApp(webAppId, true, webAppLaunchListener);
                            }

                            @Override
                            public void onSuccess(WebAppSession webAppSession) {
                                webAppSession.displayImage(url, mimeType, title, description, iconSrc, listener);
                            }
                        });
                    }
                    else {
                        System.out.println("[DEBUG] getRunningApp is not media Player");

                        getWebAppLauncher().launchWebApp(webAppId, true, webAppLaunchListener);
                    }
                }

                @Override
                public void onError(ServiceCommandError error) {
                    System.out.println("[DEBUG] test getRunningApp Error");

                    getWebAppLauncher().launchWebApp(webAppId, true, webAppLaunchListener);
                }
            });

//            this.getWebAppLauncher().joinWebApp(webAppId, new WebAppSession.LaunchListener() {
//
//                @Override
//                public void onError(ServiceCommandError error) {
//                    getWebAppLauncher().launchWebApp(webAppId, true, webAppLaunchListener);
//                }
//
//                @Override
//                public void onSuccess(WebAppSession webAppSession) {
//                    webAppSession.displayImage(url, mimeType, title, description, iconSrc, listener);
//                }
//            });
        }
    }

    @Override
    public void displayImage(MediaInfo mediaInfo, MediaPlayer.LaunchListener listener) {
        String mediaUrl = null;
        String mimeType = null;
        String title = null;
        String desc = null;
        String iconSrc = null;

        if (mediaInfo != null) {
            mediaUrl = mediaInfo.getUrl();
            mimeType = mediaInfo.getMimeType();
            title = mediaInfo.getTitle();
            desc = mediaInfo.getDescription();

            if (mediaInfo.getImages() != null && mediaInfo.getImages().size() > 0) {
                ImageInfo imageInfo = mediaInfo.getImages().get(0);
                iconSrc = imageInfo.getUrl();
            }
        }

        displayImage(mediaUrl, mimeType, title, desc, iconSrc, listener);
    }

    @Override
    public void playMedia(final String url, final String mimeType, final String title, final String description, final String iconSrc, final boolean shouldLoop, final MediaPlayer.LaunchListener listener) {
        if ("4.0.0".equalsIgnoreCase(this.serviceDescription.getVersion())) {
            DeviceService dlnaService = this.getDLNAService();

            if (dlnaService != null) {
                MediaPlayer mediaPlayer = dlnaService.getAPI(MediaPlayer.class);

                if (mediaPlayer != null) {
                    mediaPlayer.playMedia(url, mimeType, title, description, iconSrc, shouldLoop, listener);
                    return;
                }
            }

            JSONObject params = null;

            try {
                params = new JSONObject() {{
                    put("target", url);
                    put("title", title == null ? NULL : title);
                    put("description", description == null ? NULL : description);
                    put("mimeType", mimeType == null ? NULL : mimeType);
                    put("iconSrc", iconSrc == null ? NULL : iconSrc);
                    put("loop", shouldLoop);
                }};
            } catch (JSONException ex) {
                ex.printStackTrace();
                Util.postError(listener, new ServiceCommandError(-1, ex.getLocalizedMessage(), ex));
            }

            if (params != null)
                this.displayMedia(params, listener);
        } else {
            final String webAppId = "MediaPlayerTest";

            final WebAppSession.LaunchListener webAppLaunchListener = new WebAppSession.LaunchListener() {

                @Override
                public void onError(ServiceCommandError error) {
                    listener.onError(error);
                }

                @Override
                public void onSuccess(WebAppSession webAppSession) {
                    webAppSession.playMedia(url, mimeType, title, description, iconSrc, shouldLoop, listener);
                }
            };

            getRunningApp(false, new AppInfoListener() {
                @Override
                public void onSuccess(AppInfo object) {
                    if (object.getId().equals("com.lgsmartplatform.redirect.MediaPlayer")|| object.getId().equals("com.webos.app.webapphost")) {
                        System.out.println("[DEBUG] 2 getRunningApp is media player");

                        getWebAppLauncher().joinWebApp(webAppId, new WebAppSession.LaunchListener() {

                            @Override
                            public void onError(ServiceCommandError error) {
                                getWebAppLauncher().launchWebApp(webAppId, true, webAppLaunchListener);
                            }

                            @Override
                            public void onSuccess(WebAppSession webAppSession) {
                                webAppSession.playMedia(url, mimeType, title, description, iconSrc, shouldLoop, listener);
                            }
                        });
                    }
                    else {
                        System.out.println("[DEBUG] 2 getRunningApp is not media Player");

                        getWebAppLauncher().launchWebApp(webAppId, true, webAppLaunchListener);
                    }
                }

                @Override
                public void onError(ServiceCommandError error) {
                    System.out.println("[DEBUG] 2 test getRunningApp Error");

                    getWebAppLauncher().launchWebApp(webAppId, true, webAppLaunchListener);
                }
            });


//            this.getWebAppLauncher().joinWebApp(webAppId, new WebAppSession.LaunchListener() {
//
//                @Override
//                public void onError(ServiceCommandError error) {
//                    getWebAppLauncher().launchWebApp(webAppId, true, webAppLaunchListener);
//                }
//
//                @Override
//                public void onSuccess(WebAppSession webAppSession) {
//                    webAppSession.playMedia(url, mimeType, title, description, iconSrc, shouldLoop, listener);
//                }
//            });
        }
    }

    @Override
    public void playMedia(MediaInfo mediaInfo, boolean shouldLoop, MediaPlayer.LaunchListener listener) {
        String mediaUrl = null;
        String mimeType = null;
        String title = null;
        String desc = null;
        String iconSrc = null;

        if (mediaInfo != null) {
            mediaUrl = mediaInfo.getUrl();
            mimeType = mediaInfo.getMimeType();
            title = mediaInfo.getTitle();
            desc = mediaInfo.getDescription();

            if (mediaInfo.getImages() != null && mediaInfo.getImages().size() > 0) {
                ImageInfo imageInfo = mediaInfo.getImages().get(0);
                iconSrc = imageInfo.getUrl();
            }
        }

        playMedia(mediaUrl, mimeType, title, desc, iconSrc, shouldLoop, listener);
    }

    @Override
    public void closeMedia(LaunchSession launchSession, ResponseListener<Object> listener) {
        JSONObject payload = new JSONObject();

        try {
            if (launchSession.getAppId() != null && launchSession.getAppId().length() > 0)
                payload.put("id", launchSession.getAppId());

            if (launchSession.getSessionId() != null && launchSession.getSessionId().length() > 0)
                payload.put("sessionId", launchSession.getSessionId());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ServiceCommand<ResponseListener<Object>> request = new ServiceCommand<ResponseListener<Object>>(launchSession.getService(), CLOSE_MEDIA_URI, payload, true, listener);
        request.send();
    }

    /**
     * ***************
     * MEDIA CONTROL
     * ***************
     */
    @Override
    public MediaControl getMediaControl() {
        return this;
    }

    ;

    @Override
    public CapabilityPriorityLevel getMediaControlCapabilityLevel() {
        return CapabilityPriorityLevel.HIGH;
    }

    @Override
    public void play(ResponseListener<Object> listener) {
        String uri = "ssap://media.controls/play";
        ServiceCommand<ResponseListener<Object>> request = new ServiceCommand<ResponseListener<Object>>(this, uri, null, true, listener);

        request.send();
    }

    @Override
    public void pause(ResponseListener<Object> listener) {
        String uri = "ssap://media.controls/pause";
        ServiceCommand<ResponseListener<Object>> request = new ServiceCommand<ResponseListener<Object>>(this, uri, null, true, listener);

        request.send();
    }

    @Override
    public void stop(ResponseListener<Object> listener) {
        String uri = "ssap://media.controls/stop";
        ServiceCommand<ResponseListener<Object>> request = new ServiceCommand<ResponseListener<Object>>(this, uri, null, true, listener);

        request.send();
    }

    @Override
    public void rewind(ResponseListener<Object> listener) {
        String uri = "ssap://media.controls/rewind";
        ServiceCommand<ResponseListener<Object>> request = new ServiceCommand<ResponseListener<Object>>(this, uri, null, true, listener);

        request.send();
    }

    @Override
    public void fastForward(ResponseListener<Object> listener) {
        String uri = "ssap://media.controls/fastForward";
        ServiceCommand<ResponseListener<Object>> request = new ServiceCommand<ResponseListener<Object>>(this, uri, null, true, listener);

        request.send();
    }

    @Override
    public void previous(ResponseListener<Object> listener) {
        Util.postError(listener, ServiceCommandError.notSupported());
    }

    @Override
    public void next(ResponseListener<Object> listener) {
        Util.postError(listener, ServiceCommandError.notSupported());
    }

    @Override
    public void seek(long position, ResponseListener<Object> listener) {
        Util.postError(listener, ServiceCommandError.notSupported());
    }

    @Override
    public void getDuration(DurationListener listener) {
        Util.postError(listener, ServiceCommandError.notSupported());
    }

    @Override
    public void getPosition(PositionListener listener) {
        Util.postError(listener, ServiceCommandError.notSupported());
    }


    /**
     * ***************
     * TV CONTROL
     * ***************
     */
    @Override
    public TVControl getTVControl() {
        return this;
    }

    @Override
    public CapabilityPriorityLevel getTVControlCapabilityLevel() {
        return CapabilityPriorityLevel.HIGH;
    }

    public void channelUp() {
        channelUp(null);
    }

    @Override
    public void channelUp(ResponseListener<Object> listener) {
        String uri = "ssap://tv/channelUp";

        ServiceCommand<ResponseListener<Object>> request = new ServiceCommand<ResponseListener<Object>>(this, uri, null, true, listener);
        request.send();
    }

    public void channelDown() {
        channelDown(null);
    }

    @Override
    public void channelDown(ResponseListener<Object> listener) {
        String uri = "ssap://tv/channelDown";

        ServiceCommand<ResponseListener<Object>> request = new ServiceCommand<ResponseListener<Object>>(this, uri, null, true, listener);
        request.send();
    }

    @Override
    public void setChannel(ChannelInfo channelInfo, ResponseListener<Object> listener) {
        String uri = "ssap://tv/openChannel";
        JSONObject payload = new JSONObject();

        try {
            payload.put("channelNumber", channelInfo.getNumber());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ServiceCommand<ResponseListener<Object>> request = new ServiceCommand<ResponseListener<Object>>(this, uri, payload, true, listener);
        request.send();
    }

    public void setChannelById(String channelId) {
        setChannelById(channelId, null);
    }

    public void setChannelById(String channelId, ResponseListener<Object> listener) {
        String uri = "ssap://tv/openChannel";
        JSONObject payload = new JSONObject();

        try {
            payload.put("channelId", channelId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ServiceCommand<ResponseListener<Object>> request = new ServiceCommand<ResponseListener<Object>>(this, uri, payload, true, listener);
        request.send();
    }

    private ServiceCommand<ResponseListener<Object>> getCurrentChannel(boolean isSubscription, final ChannelListener listener) {
        ServiceCommand<ResponseListener<Object>> request;

        ResponseListener<Object> responseListener = new ResponseListener<Object>() {

            @Override
            public void onSuccess(Object response) {
                JSONObject jsonObj = (JSONObject) response;
                ChannelInfo channel = parseRawChannelData(jsonObj);

                Util.postSuccess(listener, channel);
            }

            @Override
            public void onError(ServiceCommandError error) {
                Util.postError(listener, error);
            }
        };

        if (isSubscription) {
            request = new URLServiceSubscription<ResponseListener<Object>>(this, CHANNEL, null, true, responseListener);
        }
        else
            request = new ServiceCommand<ResponseListener<Object>>(this, CHANNEL, null, true, responseListener);

        request.send();

        return request;
    }

    @Override
    public void getCurrentChannel(ChannelListener listener) {
        getCurrentChannel(false, listener);
    }

    @SuppressWarnings("unchecked")
    @Override
    public ServiceSubscription<ChannelListener> subscribeCurrentChannel(ChannelListener listener) {
        return (ServiceSubscription<ChannelListener>) getCurrentChannel(true, listener);
    }

    private ServiceCommand<ResponseListener<Object>> getChannelList(boolean isSubscription, final ChannelListListener listener) {
        ServiceCommand<ResponseListener<Object>> request;

        ResponseListener<Object> responseListener = new ResponseListener<Object>() {

            @Override
            public void onSuccess(Object response) {
                try {
                    JSONObject jsonObj = (JSONObject)response;
                    ArrayList<ChannelInfo> list = new ArrayList<ChannelInfo>();

                    JSONArray array = (JSONArray) jsonObj.get("channelList");
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject object = (JSONObject) array.get(i);

                        ChannelInfo channel = parseRawChannelData(object);
                        list.add(channel);
                    }

                    Util.postSuccess(listener, list);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(ServiceCommandError error) {
                Util.postError(listener, error);
            }
        };

        if (isSubscription)
            request = new URLServiceSubscription<ResponseListener<Object>>(this, CHANNEL_LIST, null, true, responseListener);
        else
            request = new ServiceCommand<ResponseListener<Object>>(this, CHANNEL_LIST, null, true, responseListener);

        request.send();

        return request;
    }

    @Override
    public void getChannelList(ChannelListListener listener) {
        getChannelList(false, listener);
    }

    @SuppressWarnings("unchecked")
    public ServiceSubscription<ChannelListListener> subscribeChannelList(final ChannelListListener listener) {
        return (ServiceSubscription<ChannelListListener>) getChannelList(true, listener);
    }

    private ServiceCommand<ResponseListener<Object>> getChannelCurrentProgramInfo(boolean isSubscription, final ProgramInfoListener listener) {
        String uri ="ssap://tv/getChannelCurrentProgramInfo";

        ServiceCommand<ResponseListener<Object>> request;

        ResponseListener<Object> responseListener = new ResponseListener<Object>() {

            @Override
            public void onSuccess(Object response) {
                JSONObject jsonObj = (JSONObject)response;
                ProgramInfo programInfo = parseRawProgramInfo(jsonObj);

                Util.postSuccess(listener, programInfo);
            }

            @Override
            public void onError(ServiceCommandError error) {
                Util.postError(listener, error);
            }
        };

        if (isSubscription)
            request = new URLServiceSubscription<ResponseListener<Object>>(this, uri, null, true, responseListener);
        else
            request = new ServiceCommand<ResponseListener<Object>>(this, uri, null, true, responseListener);

        request.send();

        return request;
    }
    
    public void getChannelCurrentProgramInfo(ProgramInfoListener listener) {
        getChannelCurrentProgramInfo(false, listener);
    }

    @SuppressWarnings("unchecked")
    public ServiceSubscription<ProgramInfoListener> subscribeChannelCurrentProgramInfo(ProgramInfoListener listener) {
        return (ServiceSubscription<ProgramInfoListener>) getChannelCurrentProgramInfo(true, listener);
    }

    private ServiceCommand<ResponseListener<Object>> getProgramList(boolean isSubscription, final ProgramListListener listener) {
        ServiceCommand<ResponseListener<Object>> request;

        ResponseListener<Object> responseListener = new ResponseListener<Object>() {

            @Override
            public void onSuccess(Object response) {
                try {
                    JSONObject jsonObj = (JSONObject) response;
                    JSONObject jsonChannel = (JSONObject) jsonObj.get("channel");
                    ChannelInfo channel = parseRawChannelData(jsonChannel);
                    JSONArray programList = (JSONArray) jsonObj.get("programList");

                    Util.postSuccess(listener, new ProgramList(channel, programList));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(ServiceCommandError error) {
                Util.postError(listener, error);
            }
        };

        if (isSubscription)
            request = new URLServiceSubscription<ResponseListener<Object>>(this, PROGRAM, null, true, responseListener);
        else
            request = new ServiceCommand<ResponseListener<Object>>(this, PROGRAM, null, true, responseListener);

        request.send();

        return request;
    }

    @Override
    public void getProgramInfo(ProgramInfoListener listener) {
        // TODO need to parse current program when program id is correct
        Util.postError(listener, ServiceCommandError.notSupported());
    }

    @Override
    public ServiceSubscription<ProgramInfoListener> subscribeProgramInfo(ProgramInfoListener listener) {
        Util.postError(listener, ServiceCommandError.notSupported());

        return new NotSupportedServiceSubscription<ProgramInfoListener>();
    }

    @Override
    public void getProgramList(ProgramListListener listener) {
        getProgramList(false, listener);
    }

    @SuppressWarnings("unchecked")
    @Override
    public ServiceSubscription<ProgramListListener> subscribeProgramList(ProgramListListener listener) {
        return (ServiceSubscription<ProgramListListener>) getProgramList(true, listener);
    }

    @Override
    public void set3DEnabled(final boolean enabled, final ResponseListener<Object> listener) {
        String uri; 
        if (enabled)
            uri = "ssap://com.webos.service.tv.display/set3DOn";
        else
            uri = "ssap://com.webos.service.tv.display/set3DOff";

        ServiceCommand<ResponseListener<Object>> request = new ServiceCommand<ResponseListener<Object>>(this, uri, null, true, listener);

        request.send();
    }

    private ServiceCommand<State3DModeListener> get3DEnabled(boolean isSubscription, final State3DModeListener listener) {
        String uri = "ssap://com.webos.service.tv.display/get3DStatus";

        ResponseListener<Object> responseListener = new ResponseListener<Object>() {

            @Override
            public void onSuccess(Object response) {
                JSONObject jsonObj = (JSONObject)response;

                JSONObject status;
                try {
                    status = jsonObj.getJSONObject("status3D");
                    boolean isEnabled = status.getBoolean("status");

                    Util.postSuccess(listener, isEnabled);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(ServiceCommandError error) {
                Util.postError(listener, error);
            }
        };

        ServiceCommand<State3DModeListener> request;
        if (isSubscription)
            request = new URLServiceSubscription<State3DModeListener>(this, uri, null, true, responseListener);
        else 
            request = new ServiceCommand<State3DModeListener>(this, uri, null, true, responseListener);

        request.send();

        return request;
    }

    @Override
    public void get3DEnabled(final State3DModeListener listener) {
        get3DEnabled(false, listener);
    }

    @SuppressWarnings("unchecked")
    @Override
    public ServiceSubscription<State3DModeListener> subscribe3DEnabled(final State3DModeListener listener) {
        return (ServiceSubscription<State3DModeListener>) get3DEnabled(true, listener);
    }


    /**
     * ***********
     * EXTERNAL INPUT
     * ************
     */
    @Override
    public ExternalInputControl getExternalInput() {
        return this;
    }

    @Override
    public CapabilityPriorityLevel getExternalInputControlPriorityLevel() {
        return CapabilityPriorityLevel.HIGH;
    }

    @Override
    public void launchInputPicker(final AppLaunchListener listener) {
        AppInfo appInfo = new AppInfo() {{
            setId("com.webos.app.inputpicker");
            setName("InputPicker");
        }};

        launchAppWithInfo(appInfo, null, listener);
    }

    @Override
    public void closeInputPicker(LaunchSession launchSession, ResponseListener<Object> listener) {
        closeApp(launchSession, listener);
    }

    @Override
    public void getExternalInputList(final ExternalInputListListener listener) {
        String uri = "ssap://tv/getExternalInputList";

        ResponseListener<Object> responseListener = new ResponseListener<Object>() {

            @Override
            public void onSuccess(Object response) {
                try {
                    JSONObject jsonObj = (JSONObject)response;
                    JSONArray devices = (JSONArray) jsonObj.get("devices");
                    Util.postSuccess(listener, externalnputInfoFromJSONArray(devices));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(ServiceCommandError error) {
                Util.postError(listener, error);
            }
        };

        ServiceCommand<ResponseListener<Object>> request = new ServiceCommand<ResponseListener<Object>>(this, uri, null, true, responseListener);
        request.send();
    }

    @Override
    public void setExternalInput(ExternalInputInfo externalInputInfo, final ResponseListener<Object> listener) {
        String uri = "ssap://tv/switchInput";

        JSONObject payload = new JSONObject();

        try {
            if (externalInputInfo != null && externalInputInfo.getId() != null) {
                payload.put("inputId", externalInputInfo.getId());
            }
            else {
                Log.w("Connect SDK", "ExternalInputInfo has no id");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ServiceCommand<ResponseListener<Object>> request = new ServiceCommand<ResponseListener<Object>>(this, uri, payload, true, listener);
        request.send();
    }


    /**
     * ***********
     * MOUSE CONTROL
     * ************
     */
    @Override
    public MouseControl getMouseControl() {
        return this;
    }

    @Override
    public CapabilityPriorityLevel getMouseControlCapabilityLevel() {
        return CapabilityPriorityLevel.HIGH;
    }

    @Override
    public void connectMouse() {
//        if (isMouseConnecting)
//            return;
//
//        isMouseConnecting = true;
        connectMouse(new WebOSTVMouseSocketConnection.WebOSTVMouseSocketListener() {
            @Override
            public void onConnected() {
                // intentionally left empty
//                isMouseConnecting = false;
            }

            @Override
            public void onDisconnected() {
                disconnectMouse();
            }
        });
    }

    @Override
    public void disconnectMouse() {
        if (mouseSocket == null)
            return;

        mouseSocket.disconnect();
        mouseSocket = null;
    }

    private void connectMouse(final WebOSTVMouseSocketConnection.WebOSTVMouseSocketListener successHandler) {
        LLog.d("hj", "isMouseConnecting : " + isMouseConnecting);
//        if (mouseSocket != null)
//            return;
        if (mouseSocket != null || isMouseConnecting)//2015.10.16 hj 여러번 connectMouse시 버그 발생함.
            return;

//        try {
//            throw new Exception();
//        } catch (Exception e) {
//            Log.e("hj", "connectMouse StackTrace : ", e);
//        }
        isMouseConnecting = true;//2015.10.18 hj 여러번 connectMouse시 버그 발생함.

        String uri = "ssap://com.webos.service.networkinput/getPointerInputSocket";

        ResponseListener<Object> listener = new ResponseListener<Object>() {

            @Override
            public void onSuccess(Object response) {
                try {
                    JSONObject jsonObj = (JSONObject)response;
                    String socketPath = (String) jsonObj.get("socketPath");
                    mouseSocket = new WebOSTVMouseSocketConnection(socketPath, successHandler);
                    isMouseConnecting = false;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(ServiceCommandError error) {
                Log.w(Util.T, "Connect mouse error: " + error.getMessage());
                isMouseConnecting = false;
            }
        };

        ServiceCommand<ResponseListener<Object>> request = new ServiceCommand<ResponseListener<Object>>(this, uri, null, true, listener);
        request.send();
    }

    @Override
    public void click() {
        if (mouseSocket != null) {
            mouseSocket.click();
        }
        else {
            connectMouse(new WebOSTVMouseSocketConnection.WebOSTVMouseSocketListener() {
                @Override
                public void onConnected() {
                    mouseSocket.click();
                }

                @Override
                public void onDisconnected() {
                    mouseSocket = null;
                }
            });
        }
    }

    @Override
    public void move(final double dx, final double dy) {
        if (mouseSocket != null) {
            mouseSocket.move(dx, dy);
        }
        else {
            connectMouse(new WebOSTVMouseSocketConnection.WebOSTVMouseSocketListener() {
                @Override
                public void onConnected() {
                    mouseSocket.move(dx, dy);
                }
                @Override
                public void onDisconnected() {
                    mouseSocket = null;
                }
            });
        }
    }

    @Override
    public void move(PointF diff) {
        move(diff.x, diff.y);
    }

    @Override
    public void scroll(final double dx, final double dy) {
        if (mouseSocket != null) {
            mouseSocket.scroll(dx, dy);
        }
        else {
            connectMouse(new WebOSTVMouseSocketConnection.WebOSTVMouseSocketListener() {
                @Override
                public void onConnected() {
                    mouseSocket.scroll(dx, dy);
                }

                @Override
                public void onDisconnected() {
                    mouseSocket = null;
                }
            });
        }
    }

    @Override
    public void scroll(PointF diff) {
        scroll(diff.x, diff.y);
    }


    /**
     * ***********
     * KEYBOARD CONTROL
     * ************
     */
    @Override
    public TextInputControl getTextInputControl() {
        return this;
    }

    @Override
    public CapabilityPriorityLevel getTextInputControlCapabilityLevel() {
        return CapabilityPriorityLevel.HIGH;
    }

    @Override
    public ServiceSubscription<TextInputStatusListener> subscribeTextInputStatus(TextInputStatusListener listener) {
        keyboardInput = new WebOSTVKeyboardInput(this);
        return keyboardInput.connect(listener);
    }

    @Override
    public void sendText(String input) {
        if (keyboardInput != null) {
            keyboardInput.addToQueue(input);
        }
    }

    @Override
    public void sendKeyCode(KeyCode keycode, ResponseListener<Object> listener) {
        switch (keycode) {
            case NUM_0:
            case NUM_1:
            case NUM_2:
            case NUM_3:
            case NUM_4:
            case NUM_5:
            case NUM_6:
            case NUM_7:
            case NUM_8:
            case NUM_9:
                sendSpecialKey(String.valueOf(keycode.getCode()), listener);
                break;
            case DASH:
                sendSpecialKey("DASH", listener);
                break;
            case ENTER:
                sendSpecialKey("ENTER", listener);
                break;
            default:
                Util.postError(listener, new ServiceCommandError(0, "The keycode is not available", null));
        }
    }

    @Override
    public void sendEnter() {
        if (keyboardInput != null) {
            keyboardInput.sendEnter();
        }
    }

    @Override
    public void sendDelete() {
        if (keyboardInput != null) {
            keyboardInput.sendDel();
        }
    }


    /**
     * ***********
     * POWER CONTROL
     * ************
     */
    @Override
    public PowerControl getPowerControl() {
        return this;
    }

    ;

    @Override
    public CapabilityPriorityLevel getPowerControlCapabilityLevel() {
        return CapabilityPriorityLevel.HIGH;
    }

    @Override
    public void powerOff(ResponseListener<Object> listener) {
        ResponseListener<Object> responseListener = new ResponseListener<Object>() {

            @Override
            public void onSuccess(Object response) {

            }

            @Override
            public void onError(ServiceCommandError error) {

            }
        };

        String uri = "ssap://system/turnOff";
        ServiceCommand<ResponseListener<Object>> request = new ServiceCommand<ResponseListener<Object>>(this, uri, null, true, responseListener);

        request.send();
    }

    @Override
    public void powerOn(ResponseListener<Object> listener) {
        Util.postError(listener, ServiceCommandError.notSupported());
    }


    public void turnOnScreen(ResponseListener<Object> listener) {
        ResponseListener<Object> responseListener = new ResponseListener<Object>() {
            @Override
            public void onSuccess(Object object) {

            }

            @Override
            public void onError(ServiceCommandError error) {

            }
        };

        String uri = "ssap://com.webos.service.tvpower/power/turnOnScreen";
        ServiceCommand<ResponseListener<Object>> request = new ServiceCommand<ResponseListener<Object>>(this, uri, null, true, responseListener);

        request.send();
    }


    /**
     * ***********
     * KEY CONTROL
     * ************
     */
    @Override
    public KeyControl getKeyControl() {
        return this;
    }

    @Override
    public CapabilityPriorityLevel getKeyControlCapabilityLevel() {
        return CapabilityPriorityLevel.HIGH;
    }

    private void sendSpecialKey(final String key, final ResponseListener<Object> listener) {
        if (mouseSocket != null) {
            mouseSocket.button(key);
            Util.postSuccess(listener, null);
        }
        else {
            connectMouse(new WebOSTVMouseSocketConnection.WebOSTVMouseSocketListener() {
                @Override
                public void onConnected() {
                    mouseSocket.button(key);
                    Util.postSuccess(listener, null);
                }

                @Override
                public void onDisconnected() {
                    mouseSocket = null;
                }
            });
        }
    }

    @Override
    public void up(ResponseListener<Object> listener) {
        sendSpecialKey("UP", listener);
    }

    @Override
    public void down(ResponseListener<Object> listener) {
        sendSpecialKey("DOWN", listener);
    }

    @Override
    public void left(ResponseListener<Object> listener) {
        sendSpecialKey("LEFT", listener);
    }

    @Override
    public void right(ResponseListener<Object> listener) {
        sendSpecialKey("RIGHT", listener);
    }

    @Override
    public void ok(final ResponseListener<Object> listener) {
        if (mouseSocket != null) {
            mouseSocket.click();
            Util.postSuccess(listener, null);
        }
        else {
            connectMouse(new WebOSTVMouseSocketConnection.WebOSTVMouseSocketListener() {
                @Override
                public void onConnected() {
                    mouseSocket.click();
                    Util.postSuccess(listener, null);
                }
                @Override
                public void onDisconnected() {
                    mouseSocket = null;
                }
            });
        }
    }

    @Override
    public void back(ResponseListener<Object> listener) {
        sendSpecialKey("BACK", listener);
    }

    @Override
    public void home(ResponseListener<Object> listener) {
        sendSpecialKey("HOME", listener);
    }

    // LJS_20170518_Add Quick Settings launching
    public void qmenu(ResponseListener<Object> listener) {
        sendSpecialKey("QMENU", listener);
    }

    // LJS_20170824_Add Info Button
    public void info(ResponseListener<Object> listener) {
        sendSpecialKey("INFO", listener);
    }

    /**
     * ***********
     * Web App Launcher
     * ************
     */

    @Override
    public WebAppLauncher getWebAppLauncher() {
        return this;
    }

    @Override
    public CapabilityPriorityLevel getWebAppLauncherCapabilityLevel() {
        return CapabilityPriorityLevel.HIGH;
    }

    @Override
    public void launchWebApp(final String webAppId, final WebAppSession.LaunchListener listener) {
        this.launchWebApp(webAppId, null, true, listener);
    }

    @Override
    public void launchWebApp(String webAppId, boolean relaunchIfRunning, WebAppSession.LaunchListener listener) {
        launchWebApp(webAppId, null, relaunchIfRunning, listener);
    }

    //2015.01.14 hj - dynamic url 추가
    public void launchWebApp(final String webAppId, final String webAppUrl, final JSONObject params, final WebAppSession.LaunchListener listener){
        if (webAppId == null || webAppId.length() == 0) {
            Util.postError(listener, new ServiceCommandError(-1, "You need to provide a valid webAppId.", null));

            return;
        }

        final WebOSWebAppSession _webAppSession = mWebAppSessions.get(webAppId);

        String uri = "ssap://webapp/launchWebApp";
        JSONObject payload = new JSONObject();

        try {
            payload.put("webAppId", webAppId);
            payload.put("webAppUrl", webAppUrl);
            if (params != null) {
                payload.put("urlParams", params);
                JSONObject appInfo = new JSONObject();
                appInfo.put("title", "LG TV Plus");
//                appInfo.put("defaultWindowType", "overlay");//overlay//floating
//                appInfo.put("transparent", true);
                payload.put("appInfo", appInfo);

//            JSONObject param = new JSONObject();
//            param.put("noLoadingSplash", true);
//            payload.put("params", param);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ResponseListener<Object> responseListener = new ResponseListener<Object>() {

            @Override
            public void onSuccess(final Object response) {
                JSONObject obj = (JSONObject) response;

                LaunchSession launchSession = null;
                WebOSWebAppSession webAppSession = _webAppSession;

                if (webAppSession != null)
                    launchSession = webAppSession.launchSession;
                else {
                    launchSession = LaunchSession.launchSessionForAppId(webAppId);
                    webAppSession = new WebOSWebAppSession(launchSession, WebOSTVService.this);
                    mWebAppSessions.put(webAppId, webAppSession);
                }

                launchSession.setService(WebOSTVService.this);
                launchSession.setSessionId(obj.optString("sessionId"));
                launchSession.setSessionType(LaunchSessionType.WebApp);
                launchSession.setRawData(obj);

                Util.postSuccess(listener, webAppSession);
            }

            @Override
            public void onError(ServiceCommandError error) {
                Util.postError(listener, error);
            }
        };

        ServiceCommand<ResponseListener<Object>> request = new ServiceCommand<ResponseListener<Object>>(this, uri, payload, true, responseListener);
        request.send();
    }
    //2015.11.18 cic hj - title 추가함.
    public void launchWebApp(final String webAppId, final JSONObject params, final WebAppSession.LaunchListener listener) {
        if (webAppId == null || webAppId.length() == 0) {
            Util.postError(listener, new ServiceCommandError(-1, "You need to provide a valid webAppId.", null));

            return;
        }

        final WebOSWebAppSession _webAppSession = mWebAppSessions.get(webAppId);

        String uri = "ssap://webapp/launchWebApp";
        JSONObject payload = new JSONObject();

        try {
            payload.put("webAppId", webAppId);
            if ("MediaPlayerTest".equals(webAppId)){
                JSONObject param = new JSONObject();
                param.put("lang", Locale.getDefault());
                payload.put("urlParams", param);



                JSONObject appInfo = new JSONObject();
                appInfo.put("title", "LG TV Plus");
                JSONObject noRecent = new JSONObject();
                noRecent.put("hidden", true);
                appInfo.put("class", noRecent);
                payload.put("appInfo", appInfo);

            }
            else if (params != null) {
                payload.put("urlParams", params);
                JSONObject appInfo = new JSONObject();
                appInfo.put("title", "LG TV Plus");
//                appInfo.put("defaultWindowType", "overlay");//overlay//floating
//                appInfo.put("transparent", true);
                payload.put("appInfo", appInfo);

//            JSONObject param = new JSONObject();
//            param.put("noLoadingSplash", true);
//            payload.put("params", param);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ResponseListener<Object> responseListener = new ResponseListener<Object>() {

            @Override
            public void onSuccess(final Object response) {
                JSONObject obj = (JSONObject) response;

                LaunchSession launchSession = null;
                WebOSWebAppSession webAppSession = _webAppSession;

                if (webAppSession != null)
                    launchSession = webAppSession.launchSession;
                else {
                    launchSession = LaunchSession.launchSessionForAppId(webAppId);
                    webAppSession = new WebOSWebAppSession(launchSession, WebOSTVService.this);
                    mWebAppSessions.put(webAppId, webAppSession);
                }

                launchSession.setService(WebOSTVService.this);
                launchSession.setSessionId(obj.optString("sessionId"));
                launchSession.setSessionType(LaunchSessionType.WebApp);
                launchSession.setRawData(obj);

                Util.postSuccess(listener, webAppSession);
            }

            @Override
            public void onError(ServiceCommandError error) {
                Util.postError(listener, error);
            }
        };

        ServiceCommand<ResponseListener<Object>> request = new ServiceCommand<ResponseListener<Object>>(this, uri, payload, true, responseListener);
        request.send();
    }

    @Override
    public void launchWebApp(final String webAppId, final JSONObject params, boolean relaunchIfRunning, final WebAppSession.LaunchListener listener) {
        if (webAppId == null) {
            Util.postError(listener, new ServiceCommandError(0, "Must pass a web App id", null));
            return;
        }

        if (relaunchIfRunning) {
            launchWebApp(webAppId, params, listener);
        } else {
            getLauncher().getRunningApp(new AppInfoListener() {

                @Override
                public void onError(ServiceCommandError error) {
                    Util.postError(listener, error);
                }

                @Override
                public void onSuccess(AppInfo appInfo) {
                    //  TODO: this will only work on pinned apps, currently
                    if (appInfo.getId().indexOf(webAppId) != -1) {
                        LaunchSession launchSession = LaunchSession.launchSessionForAppId(webAppId);
                        launchSession.setSessionType(LaunchSessionType.WebApp);
                        launchSession.setService(WebOSTVService.this);
                        launchSession.setRawData(appInfo.getRawData());

                        WebOSWebAppSession webAppSession = webAppSessionForLaunchSession(launchSession);

                        Util.postSuccess(listener, webAppSession);
                    } else {
                        launchWebApp(webAppId, params, listener);
                    }
                }
            });
        }
    }

    @Override
    public void closeWebApp(LaunchSession launchSession, final ResponseListener<Object> listener) {
        if (launchSession == null || launchSession.getAppId() == null || launchSession.getAppId().length() == 0) {
            Util.postError(listener, new ServiceCommandError(0, "Must provide a valid launch session", null));
            return;
        }

        final WebOSWebAppSession webAppSession = mWebAppSessions.get(launchSession.getAppId());

        if (webAppSession != null && webAppSession.isConnected()) {
            JSONObject serviceCommand = new JSONObject();
            JSONObject closeCommand = new JSONObject();

            try {
                serviceCommand.put("type", "close");

                closeCommand.put("contentType", "connectsdk.serviceCommand");
                closeCommand.put("serviceCommand", serviceCommand);
            } catch (JSONException ex) {
                ex.printStackTrace();
            }

            webAppSession.sendMessage(closeCommand, new ResponseListener<Object>() {

                @Override
                public void onError(ServiceCommandError error) {
                    webAppSession.disconnectFromWebApp();

                    Util.postError(listener, error);
                }

                @Override
                public void onSuccess(Object object) {
                    webAppSession.disconnectFromWebApp();

                    Util.postSuccess(listener, object);
                }
            });
        } else {
            if (webAppSession != null)
                webAppSession.disconnectFromWebApp();

            String uri = "ssap://webapp/closeWebApp";
            JSONObject payload = new JSONObject();

            try {
                if (launchSession.getAppId() != null) payload.put("webAppId", launchSession.getAppId());
                if (launchSession.getSessionId() != null) payload.put("sessionId", launchSession.getSessionId());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            ServiceCommand<ResponseListener<Object>> request = new ServiceCommand<ResponseListener<Object>>(this, uri, payload, true, listener);
            request.send();
        }
    }

    public void connectToWebApp(final WebOSWebAppSession webAppSession, final boolean joinOnly, final ResponseListener<Object> connectionListener) {
        if (mWebAppSessions == null)
            mWebAppSessions = new ConcurrentHashMap<String, WebOSWebAppSession>();

        if (mAppToAppIdMappings == null)
            mAppToAppIdMappings = new ConcurrentHashMap<String, String>();

        if (webAppSession == null || webAppSession.launchSession == null) {
            Util.postError(connectionListener, new ServiceCommandError(0, "You must provide a valid LaunchSession object", null));
            return;
        }

        String _appId = webAppSession.launchSession.getAppId();
        String _idKey = null;

        if (webAppSession.launchSession.getSessionType() == LaunchSession.LaunchSessionType.WebApp)
            _idKey = "webAppId";
        else
            _idKey = "appId";

        if (_appId == null || _appId.length() == 0) {
            Util.postError(connectionListener, new ServiceCommandError(-1, "You must provide a valid web app session", null));

            return;
        }

        final String appId = _appId;
        final String idKey = _idKey;

        String uri = "ssap://webapp/connectToApp";
        JSONObject payload = new JSONObject();

        try {
            payload.put(idKey, appId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ResponseListener<Object> responseListener = new ResponseListener<Object>() {

            @Override
            public void onSuccess(final Object response) {
                JSONObject jsonObj = (JSONObject)response;

                String state = jsonObj.optString("state");

                LLog.d("hj", "connectToWebApp state : " + state);
//                if (!state.equalsIgnoreCase("CONNECTED")) {
//                    if (joinOnly && state.equalsIgnoreCase("WAITING_FOR_APP")) {
//                        Util.postError(connectionListener, new ServiceCommandError(0, "Web app is not currently running", null));
//                    }
//
//                    return;
//                }

                String fullAppId = jsonObj.optString("appId");

                if (fullAppId != null && fullAppId.length() != 0) {
                    if (webAppSession.launchSession.getSessionType() == LaunchSessionType.WebApp)
                        mAppToAppIdMappings.put(fullAppId, appId);

                    webAppSession.setFullAppId(fullAppId);
                }

                if (connectionListener != null) {
                    Util.runOnUI(new Runnable() {

                        @Override
                        public void run() {
                            connectionListener.onSuccess(response);
                        }
                    });
                }
            }

            @Override
            public void onError(ServiceCommandError error) {
                webAppSession.disconnectFromWebApp();

                boolean appChannelDidClose = false;

                if (error != null && error.getPayload() != null)
                    appChannelDidClose = error.getPayload().toString().contains("app channel closed");

                if (appChannelDidClose) {
                    if (webAppSession.getWebAppSessionListener() != null) {
                        Util.runOnUI(new Runnable() {

                            @Override
                            public void run() {
                                if(webAppSession != null && webAppSession.getWebAppSessionListener() != null)
                                    webAppSession.getWebAppSessionListener().onWebAppSessionDisconnect(webAppSession);
                            }
                        });
                    }
                } else {
                    Util.postError(connectionListener, error);
                }
            }
        };

        webAppSession.appToAppSubscription = new URLServiceSubscription<ResponseListener<Object>>(webAppSession.socket, uri, payload, true, responseListener);
        webAppSession.appToAppSubscription.subscribe();
    }

    private void notifyPairingRequired() {
        if (listener != null) {
            listener.onPairingRequired(this, pairingType, null);
        }
    }

    @Override
    public void pinWebApp(String webAppId, final ResponseListener<Object> listener) {
        if (webAppId == null || webAppId.length() == 0) {
            if (listener != null) {
                listener.onError(new ServiceCommandError(-1, "You must provide a valid web app id", null));
            }
            return;
        }
        
        String uri = "ssap://webapp/pinWebApp";
        JSONObject payload = new JSONObject();

        try {
            payload.put("webAppId", webAppId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ResponseListener<Object> responseListener = new ResponseListener<Object>() {

            @Override
            public void onSuccess(final Object response) {
                JSONObject obj = (JSONObject) response;
                if (obj.has("pairingType")) {
                    notifyPairingRequired();
                }
                else if (listener != null) {
                    listener.onSuccess(response);
                }
            }

            @Override
            public void onError(ServiceCommandError error) {
                Util.postError(listener, error);
            }
        };

        ServiceCommand<ResponseListener<Object>> request = 
                new URLServiceSubscription<ResponseListener<Object>>(this, uri, payload, true, responseListener);
        request.send();
    }

    @Override
    public void unPinWebApp(String webAppId, final ResponseListener<Object> listener) {
        if (webAppId == null || webAppId.length() == 0) {
            if (listener != null) {
                listener.onError(new ServiceCommandError(-1, "You must provide a valid web app id", null));
            }
            return;
        }

        String uri = "ssap://webapp/removePinnedWebApp";
        JSONObject payload = new JSONObject();

        try {
            payload.put("webAppId", webAppId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ResponseListener<Object> responseListener = new ResponseListener<Object>() {

            @Override
            public void onSuccess(final Object response) {
                JSONObject obj = (JSONObject) response;
                if (obj.has("pairingType")) {
                    notifyPairingRequired();
                }
                else if (listener != null) {
                    listener.onSuccess(response);
                }
            }

            @Override
            public void onError(ServiceCommandError error) {
                Util.postError(listener, error);
            }
        };

        ServiceCommand<ResponseListener<Object>> request =
                new URLServiceSubscription<ResponseListener<Object>>(this, uri, payload, true, responseListener);
        request.send();
    }

    private ServiceCommand<WebAppPinStatusListener> isWebAppPinned(boolean isSubscription, String webAppId, final WebAppPinStatusListener listener) {
        if (webAppId == null || webAppId.length() == 0) {
            if (listener != null) {
                listener.onError(new ServiceCommandError(-1, "You must provide a valid web app id", null));
            }
            return null;
        }

        String uri = "ssap://webapp/isWebAppPinned";
        JSONObject payload = new JSONObject();

        try {
            payload.put("webAppId", webAppId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ResponseListener<Object> responseListener = new ResponseListener<Object>() {

            @Override
            public void onSuccess(final Object response) {
                JSONObject obj = (JSONObject) response;

                boolean status = obj.optBoolean("pinned");

                if (listener != null) {
                    listener.onSuccess(status);
                }
            }

            @Override
            public void onError(ServiceCommandError error) {
                Util.postError(listener, error);
            }
        };

        ServiceCommand<WebAppPinStatusListener> request;
        if (isSubscription)
            request = new URLServiceSubscription<WebAppPinStatusListener>(this, uri, payload, true, responseListener);
        else 
            request = new ServiceCommand<WebAppPinStatusListener>(this, uri, payload, true, responseListener);

        request.send();

        return request;
    }

    @Override
    public void isWebAppPinned(String webAppId, WebAppPinStatusListener listener) {
        isWebAppPinned(false, webAppId, listener);
    }

    @Override
    public ServiceSubscription<WebAppPinStatusListener> subscribeIsWebAppPinned(
            String webAppId, WebAppPinStatusListener listener) {
        return (URLServiceSubscription<WebAppPinStatusListener>)isWebAppPinned(true, webAppId, listener);
    }

    /* Join a native/installed webOS app */
    public void joinApp(String appId, WebAppSession.LaunchListener listener) {
        LaunchSession launchSession = LaunchSession.launchSessionForAppId(appId);
        launchSession.setSessionType(LaunchSessionType.App);
        launchSession.setService(this);

        joinWebApp(launchSession, listener);
    }

    /* Connect to a native/installed webOS app */
    public void connectToApp(String appId, final WebAppSession.LaunchListener listener) {
        LaunchSession launchSession = LaunchSession.launchSessionForAppId(appId);
        launchSession.setSessionType(LaunchSessionType.App);
        launchSession.setService(this);

        final WebOSWebAppSession webAppSession = webAppSessionForLaunchSession(launchSession);

        connectToWebApp(webAppSession, false, new ResponseListener<Object>() {
            @Override
            public void onError(ServiceCommandError error) {
                Util.postError(listener, error);
            }

            @Override
            public void onSuccess(Object object) {
                Util.postSuccess(listener, webAppSession);
            }
        });
    }

    @Override
    public void joinWebApp(final LaunchSession webAppLaunchSession, final WebAppSession.LaunchListener listener) {
        final WebOSWebAppSession webAppSession = this.webAppSessionForLaunchSession(webAppLaunchSession);
        webAppSession.launchSession.setAppName(null);//2015.10.29 hj - add null
        webAppSession.join(new ResponseListener<Object>() {

            @Override
            public void onError(ServiceCommandError error) {
                Util.postError(listener, error);
            }

            @Override
            public void onSuccess(Object object) {
                //2015.10.28 cic hj - Connect 상태 알기 위해 추가함..
                JSONObject jsonObj = (JSONObject) object;
                LLog.d("hj", "jsonObj : " + jsonObj);
                if (jsonObj != null) {
                    String state = jsonObj.optString("state");
                    LLog.d("hj", "join state : " + state);
                    if (state.equalsIgnoreCase("CONNECTED")) {
                        webAppSession.launchSession.setAppName("CONNECTED");
                    } else {
                        webAppSession.launchSession.setAppName(null);
                    }
                }
                ///////////////////////////////////////////
                Util.postSuccess(listener, webAppSession);
            }
        });
    }

    @Override
    public void joinWebApp(String webAppId, WebAppSession.LaunchListener listener) {
        LaunchSession launchSession = LaunchSession.launchSessionForAppId(webAppId);
        launchSession.setSessionType(LaunchSessionType.WebApp);
        launchSession.setService(this);

        joinWebApp(launchSession, listener);
    }

    private WebOSWebAppSession webAppSessionForLaunchSession(LaunchSession launchSession) {
        if (mWebAppSessions == null)
            mWebAppSessions = new ConcurrentHashMap<String, WebOSWebAppSession>();

        if (launchSession.getService() == null)
            launchSession.setService(this);

        WebOSWebAppSession webAppSession = mWebAppSessions.get(launchSession.getAppId());

        if (webAppSession == null) {
            webAppSession = new WebOSWebAppSession(launchSession, this);
            mWebAppSessions.put(launchSession.getAppId(), webAppSession);
        }

        return webAppSession;
    }

    @SuppressWarnings("unused")
    private void sendMessage(Object message, LaunchSession launchSession, ResponseListener<Object> listener) {
        if (launchSession == null || launchSession.getAppId() == null) {
            Util.postError(listener, new ServiceCommandError(0, "Must provide a valid LaunchSession object", null));
            return;
        }

        if (message == null) {
            Util.postError(listener, new ServiceCommandError(0, "Cannot send a null message", null));
            return;
        }

        if (socket == null) {
            connect();
        }

        String appId = launchSession.getAppId();
        String fullAppId = appId;

        if (launchSession.getSessionType() == LaunchSessionType.WebApp)
            fullAppId = mAppToAppIdMappings.get(appId);

        if (fullAppId == null || fullAppId.length() == 0) {
            Util.postError(listener, new ServiceCommandError(-1, "You must provide a valid LaunchSession to send messages to", null));

            return;
        }

        JSONObject payload = new JSONObject();

        try {
            payload.put("type", "p2p");
            payload.put("to", fullAppId);
            payload.put("payload", message);

            Object payTest = payload.get("payload");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ServiceCommand<ResponseListener<Object>> request = new ServiceCommand<ResponseListener<Object>>(this, null, payload, true, listener);
        sendCommand(request);
    }

    public void sendMessage(String message, LaunchSession launchSession, ResponseListener<Object> listener) {
        if (message != null && message.length() > 0) {
            sendMessage((Object) message, launchSession, listener);
        } 
        else {
            Util.postError(listener, new ServiceCommandError(0, "Cannot send a null message", null));
        }
    }

    public void sendMessage(JSONObject message, LaunchSession launchSession, ResponseListener<Object> listener) {
        if (message != null && message.length() > 0)
            sendMessage((Object) message, launchSession, listener);
        else
            Util.postError(listener, new ServiceCommandError(0, "Cannot send a null message", null));
    }


    /**
     * ***********
     * SYSTEM CONTROL
     * ************
     */
    public void getServiceInfo(final ServiceInfoListener listener) {
        String uri = "ssap://api/getServiceList";

        ServiceCommand<ResponseListener<Object>> request = new ServiceCommand<ResponseListener<Object>>(this, uri, null, true, new ResponseListener<Object>() {

            @Override
            public void onSuccess(Object response) {
                try {
                    JSONObject jsonObj = (JSONObject)response;
                    JSONArray services = (JSONArray) jsonObj.get("services");
                    Util.postSuccess(listener, services);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(ServiceCommandError error) {
                Util.postError(listener, error);
            }
        });

        request.send();
    }

    public void getCurrentTime(final CurrentTimeListener listener) {
        String uri = "ssap://com.webos.service.tv.time/getCurrentTime";

        ServiceCommand<ResponseListener<Object>> request = new ServiceCommand<ResponseListener<Object>>(this, uri, null, true, new ResponseListener<Object>() {

            @Override
            public void onSuccess(Object response) {
                JSONObject jsonObj = (JSONObject)response;
                Util.postSuccess(listener, jsonObj);
            }

            @Override
            public void onError(ServiceCommandError error) {
                Util.postError(listener, error);
            }
        });

        request.send();
    }

    public void getSystemInfo(final SystemInfoListener listener) {
        String uri = "ssap://system/getSystemInfo";

        ServiceCommand<ResponseListener<Object>> request = new ServiceCommand<ResponseListener<Object>>(this, uri, null, true, new ResponseListener<Object>() {

            @Override
            public void onSuccess(Object response) {
//                try {
                    JSONObject jsonObj = (JSONObject)response;
//                    JSONObject features = (JSONObject) jsonObj.get("features");
                    Util.postSuccess(listener, jsonObj);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
            }

            @Override
            public void onError(ServiceCommandError error) {
                Util.postError(listener, error);
            }
        });

        request.send();
    }

    public void secureAccessTest(final SecureAccessTestListener listener) {
        String uri = "ssap://com.webos.service.secondscreen.gateway/test/secure";

        ResponseListener<Object> responseListener = new ResponseListener<Object>() {

            @Override
            public void onSuccess(Object response) {

                try {
                    JSONObject jsonObj = (JSONObject) response;
                    boolean isSecure = (Boolean) jsonObj.get("returnValue");
                    Util.postSuccess(listener, isSecure);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(ServiceCommandError error) {
                Util.postError(listener, error);
            }
        };

        ServiceCommand<ResponseListener<Object>> request = new ServiceCommand<ResponseListener<Object>>(this, uri, null, true, responseListener);
        request.send();
    }

    public void getACRAuthToken(final ACRAuthTokenListener listener) {
        String uri = "ssap://tv/getACRAuthToken";

        ResponseListener<Object> responseListener = new ResponseListener<Object>() {

            @Override
            public void onSuccess(Object response) {

                try {
                    JSONObject jsonObj = (JSONObject) response;
                    String authToken = (String) jsonObj.get("token");
                    Util.postSuccess(listener, authToken);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(ServiceCommandError error) {
                Util.postError(listener, error);
            }
        };

        ServiceCommand<ResponseListener<Object>> request = new ServiceCommand<ResponseListener<Object>>(this, uri, null, true, responseListener);
        request.send();
    }

    public void getLaunchPoints(final LaunchPointsListener listener) {
        String uri = "ssap://com.webos.applicationManager/listLaunchPoints";

        ResponseListener<Object> responseListener = new ResponseListener<Object>() {

            @Override
            public void onSuccess(Object response) {

                try {
                    JSONObject jsonObj = (JSONObject) response;
                    JSONArray launchPoints = (JSONArray) jsonObj.get("launchPoints");
                    Util.postSuccess(listener, launchPoints);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(ServiceCommandError error) {
                Util.postError(listener, error);
            }
        };

        ServiceCommand<ResponseListener<Object>> request = new ServiceCommand<ResponseListener<Object>>(this, uri, null, true, responseListener);
        request.send();
    }

    @Override
    public void sendCommand(ServiceCommand<?> command) {
        if (socket != null) {
            try {
                socket.sendCommand(command);
            } catch (WebsocketNotConnectedException err) {
                Log.e("Tms", "WebsocketNotConnectedException");
            }
        }
    }

    @Override
    public void unsubscribe(URLServiceSubscription<?> subscription) {
        if (socket != null)
            socket.unsubscribe(subscription);
    }

    @Override
    protected void updateCapabilities() {
        List<String> capabilities = new ArrayList<String>();

        if (DiscoveryManager.getInstance().getPairingLevel() == PairingLevel.ON) {
            for (String capability : TextInputControl.Capabilities) {
                capabilities.add(capability);
            }
            for (String capability : MouseControl.Capabilities) {
                capabilities.add(capability);
            }
            for (String capability : KeyControl.Capabilities) {
                capabilities.add(capability);
            }
            for (String capability : MediaPlayer.Capabilities) {
                capabilities.add(capability);
            }
            for (String capability : Launcher.Capabilities) {
                capabilities.add(capability);
            }
            for (String capability : TVControl.Capabilities) {
                capabilities.add(capability);
            }
            for (String capability : ExternalInputControl.Capabilities) {
                capabilities.add(capability);
            }
            for (String capability : VolumeControl.Capabilities) {
                capabilities.add(capability);
            }
            for (String capability : ToastControl.Capabilities) {
                capabilities.add(capability);
            }

            capabilities.add(PowerControl.Off);
        } else {
            for (String capability : VolumeControl.Capabilities) {
                capabilities.add(capability);
            }
            for (String capability : MediaPlayer.Capabilities) {
                capabilities.add(capability);
            }

            capabilities.add(Application);
            capabilities.add(Application_Params);
            capabilities.add(Application_Close);
            capabilities.add(Browser);
            capabilities.add(Browser_Params);
            capabilities.add(Hulu);
            capabilities.add(Netflix);
            capabilities.add(Netflix_Params);
            capabilities.add(YouTube);
            capabilities.add(YouTube_Params);
            capabilities.add(AppStore);
            capabilities.add(AppStore_Params);
            capabilities.add(AppState);
            capabilities.add(AppState_Subscribe);
        }

        if (serviceDescription != null && serviceDescription.getVersion() != null) {
            if (serviceDescription.getVersion().contains("4.0.0") || serviceDescription.getVersion().contains("4.0.1")) {
                capabilities.add(Launch);
                capabilities.add(Launch_Params);

                capabilities.add(Play);
                capabilities.add(Pause);
                capabilities.add(Stop);
                capabilities.add(Seek);
                capabilities.add(Position);
                capabilities.add(Duration);
                capabilities.add(PlayState);

                capabilities.add(WebAppLauncher.Close);
            } else {
                for (String capability : WebAppLauncher.Capabilities) {
                    capabilities.add(capability);
                }
                for (String capability : MediaControl.Capabilities) {
                    if (capability.equalsIgnoreCase(MediaControl.Previous) || capability.equalsIgnoreCase(MediaControl.Next))
                        continue;

                    capabilities.add(capability);
                }
            }
        }

        setCapabilities(capabilities);
    }

    public List<String> getPermissions() {
        if (permissions != null)
            return permissions;

        List<String> defaultPermissions = new ArrayList<String>();
        for (String perm : kWebOSTVServiceOpenPermissions) {
            defaultPermissions.add(perm);
        }

        if (DiscoveryManager.getInstance().getPairingLevel() == PairingLevel.ON) {
            for (String perm : kWebOSTVServiceProtectedPermissions) {
                defaultPermissions.add(perm);
            }

            for (String perm : kWebOSTVServicePersonalActivityPermissions) {
                defaultPermissions.add(perm);
            }
        }

        permissions = defaultPermissions;

        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;

        WebOSTVServiceConfig config = (WebOSTVServiceConfig) serviceConfig;

        if (config.getClientKey() != null) {
            config.setClientKey(null);

            if (isConnected()) {
                Log.w("Connect SDK", "Permissions changed -- you will need to re-pair to the TV.");
                disconnect();
            }
        }
    }

    private ProgramInfo parseRawProgramInfo(JSONObject programRawData) {
        String programId;
        String programName;

        ProgramInfo programInfo = new ProgramInfo();
        programInfo.setRawData(programRawData);
        
        programId = programRawData.optString("programId");
        programName = programRawData.optString("programName");
        ChannelInfo channelInfo = parseRawChannelData(programRawData);

        programInfo.setId(programId);
        programInfo.setName(programName);
        programInfo.setChannelInfo(channelInfo);

        return programInfo;
    }

    private ChannelInfo parseRawChannelData(JSONObject channelRawData) {
        String channelName = null;
        String channelId = null;
        String channelNumber = null;
        int minorNumber;
        int majorNumber;

        ChannelInfo channelInfo = new ChannelInfo();
        channelInfo.setRawData(channelRawData);

        try {
            if (!channelRawData.isNull("channelName")) 
                channelName = (String) channelRawData.get("channelName");

            if (!channelRawData.isNull("channelId")) 
                channelId = (String) channelRawData.get("channelId");

            channelNumber = channelRawData.optString("channelNumber");

            if (!channelRawData.isNull("majorNumber"))
                majorNumber = (Integer) channelRawData.get("majorNumber");
            else 
                majorNumber = parseMajorNumber(channelNumber);

            if (!channelRawData.isNull("minorNumber"))
                minorNumber = (Integer) channelRawData.get("minorNumber");
            else
                minorNumber = parseMinorNumber(channelNumber);

            channelInfo.setName(channelName);
            channelInfo.setId(channelId);
            channelInfo.setNumber(channelNumber);
            channelInfo.setMajorNumber(majorNumber);
            channelInfo.setMinorNumber(minorNumber);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return channelInfo;
    }

    private int parseMinorNumber(String channelNumber) {
        if (channelNumber != null) {
            String tokens[] = channelNumber.split("-");
            return Integer.valueOf(tokens[tokens.length-1]);
        }
        else 
            return 0;
    }

    private int parseMajorNumber(String channelNumber) {
        if (channelNumber != null) {
            String tokens[] = channelNumber.split("-");
            return Integer.valueOf(tokens[0]);
        }
        else 
            return 0;
    }

    private List<ExternalInputInfo> externalnputInfoFromJSONArray(JSONArray inputList) {
        List<ExternalInputInfo> externalInputInfoList = new ArrayList<ExternalInputInfo>();

        for (int i = 0; i < inputList.length(); i++) {
            try {
                JSONObject input = (JSONObject) inputList.get(i);

                String id = input.getString("id");
                String name = input.getString("label");
                boolean connected = input.getBoolean("connected");
                String iconURL = input.getString("icon");

                ExternalInputInfo inputInfo = new ExternalInputInfo();
                inputInfo.setRawData(input);
                inputInfo.setId(id);
                inputInfo.setName(name);
                inputInfo.setConnected(connected);
                inputInfo.setIconURL(iconURL);

                externalInputInfoList.add(inputInfo);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return externalInputInfoList;
    }

//    @Override
//    public LaunchSession decodeLaunchSession(String type, JSONObject obj) throws JSONException {
//        if ("webostv".equals(type)) {
//            LaunchSession launchSession = LaunchSession.launchSessionFromJSONObject(obj);
//            launchSession.setService(this);
//            return launchSession;
//        }
//        return null;
//    }

    @Override
    public void getPlayState(PlayStateListener listener) {
        Util.postError(listener, ServiceCommandError.notSupported());
    }

    @Override
    public ServiceSubscription<PlayStateListener> subscribePlayState(PlayStateListener listener) {
        Util.postError(listener, ServiceCommandError.notSupported());

        return null;
    }

    @Override
    public boolean isConnectable() {
        return true;
    }

    @Override
    public void sendPairingKey(String pairingKey) {
        if (this.socket != null) {
            this.socket.sendPairingKey(pairingKey);
        }
    }

    public static interface ServiceInfoListener extends ResponseListener<JSONArray> { }

    public static interface SystemInfoListener extends ResponseListener<JSONObject> {
    }

    public static interface CurrentTimeListener extends ResponseListener<JSONObject> {}

    public void getSDXHeader(ResponseListener<Object> listener) {
        getSDXHeader(false, listener);
    }
    public ServiceSubscription<ResponseListener> subscribeSDXHeader(ResponseListener<Object> listener) {
        return (ServiceSubscription<ResponseListener>) getSDXHeader(true, listener);
    }
    private ServiceCommand<ResponseListener<Object>> getSDXHeader(boolean isSubscription, final ResponseListener<Object> listener) {
        ServiceCommand<ResponseListener<Object>> request;
        String uri = "ssap://com.webos.service.sdx/getHttpHeaderForServiceRequest";
        ResponseListener<Object> responseListener = new ResponseListener<Object>() {

            @Override
            public void onSuccess(Object response) {
                Util.postSuccess(listener, response);
            }

            @Override
            public void onError(ServiceCommandError error) {
                Util.postError(listener, error);
            }
        };

        if (isSubscription)
            request = new URLServiceSubscription<ResponseListener<Object>>(this, uri, null, true, responseListener);
        else
            request = new ServiceCommand<ResponseListener<Object>>(this, uri, null, true, responseListener);

        request.send();

        return request;
    }

    //2015.03.06 CIC hj - channelList/valueList값 같이 넘김
//    final float IPChannelMinOS = 2.2f;
//    final String[] IPChannelCountry = {"KR"};
    public ServiceCommand<ResponseListener<Object>> getValueChannelList(String contry, final ChannelListListener listener) {
        ServiceCommand<ResponseListener<Object>> request;

        ResponseListener<Object> responseListener = new ResponseListener<Object>() {

            @Override
            public void onSuccess(Object response) {
                try {
                    JSONObject jsonObj = (JSONObject) response;
                    ArrayList<ChannelInfo> list = new ArrayList<ChannelInfo>();
                    ChannelInfo value = new ChannelInfo();
                    try {
                        String valueList = jsonObj.getString("valueList");
                        value.setId(valueList);
                    } catch(JSONException e){
                        value.setId("");
                        e.printStackTrace();
                    }
                    list.add(value);

                    JSONArray array = (JSONArray) jsonObj.get("channelList");
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject object = (JSONObject) array.get(i);

                        ChannelInfo channel = parseRawChannelData(object);
                        list.add(channel);

//                        Log.d("hj", i + "번 채널 : " + object.toString());
                    }

                    ChannelInfo url = new ChannelInfo();
                    try {
                        String channelLogoServerUrl = jsonObj.getString("channelLogoServerUrl");
                        url.setId(channelLogoServerUrl);
                    } catch(JSONException e){
                        url.setId("");
                        e.printStackTrace();
                    }
                    list.add(url);

                    Util.postSuccess(listener, list);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(ServiceCommandError error) {
                Util.postError(listener, error);
            }
        };

        JSONObject payload = null;

//        boolean isIPChannelContry = false;
//        for(int i=0; i<IPChannelCountry.length; i++) {
//            if(IPChannelCountry[i].equals(contry)) {
//                isIPChannelContry = true;
//                break;
//            }
//        }
//        if(isIPChannelContry) {
//            String osVersion = WebOSTVServiceSocketClient.deviceOSReleaseVersion;
//            if (!osVersion.equals("")) {
//                osVersion = osVersion.substring(0, 3);
//                try {
//                    if (Float.valueOf(osVersion) >= IPChannelMinOS) {
//                        payload = new JSONObject();
//                        JSONArray modes = new JSONArray();
//                        modes.put("Tuner");
//                        modes.put("IP");
//                        payload.put("channelMode", modes);
//                    }
//                } catch (Exception e) {
//                    Log.e("getValueChannelList", e.toString());
//                    Log.e("getValueChannelList", "error of osVersion string to float!!!");
//                }
//            }
//        }
        request = new ServiceCommand<ResponseListener<Object>>(this, CHANNEL_LIST, payload, true, responseListener);

        request.send();

        return request;
    }



/*    //2015.02.24 CIC hj - 숫자키 / 대쉬 / 엔터 키 추가
    public void num(int num, ResponseListener<Object> listener) {
        sendSpecialKey(String.valueOf(num));
    }
    public void dash(ResponseListener<Object> listener) {
        sendSpecialKey("DASH");
    }
    public void enter(ResponseListener<Object> listener) {
        sendSpecialKey("ENTER");
    }
*/
    //2015.03.04 hj - 녹화/컬러키 추가
    public void red(ResponseListener<Object> listener) {
        sendSpecialKey("RED", null);
    }
    public void green(ResponseListener<Object> listener) {
        sendSpecialKey("GREEN", null);
    }
    public void yellow(ResponseListener<Object> listener) {
        sendSpecialKey("YELLOW", null);
    }
    public void blue(ResponseListener<Object> listener) {
        sendSpecialKey("BLUE", null);
    }
    public void recode(ResponseListener<Object> listener) {
        sendSpecialKey("RECORD", listener);
    }
    public void exit(ResponseListener<Object> listener) {
        sendSpecialKey("EXIT", null);
    }

    //2015/04.28 hj - 일본키 추가
    public void sendJapanKey(String type , ResponseListener<Object> listener){
        sendSpecialKey(type, null);
    }

    //2015.05.08 cic hj - 3D h/w 버튼
    public void button_3d(ResponseListener<Object> listener){
        sendSpecialKey("3D_MODE", null);
    }

    //2016.08.19 channel up / down key 버튼 추가
    public void chanUp (ResponseListener<Object> listener){
        sendSpecialKey("CHANNELUP", null);
    }

    public void chanDown (ResponseListener<Object> listener){
        sendSpecialKey("CHANNELDOWN", null);
    }

/*    //2015.01.29 CIC hj - sdx값 로딩
    public static interface CurrentProgramListener extends ResponseListener<ProgramInfo> {
    }

    public ServiceSubscription<WebOSTVService.CurrentProgramListener> subscribeCurrentProgram(CurrentProgramListener listener) {
        return (ServiceSubscription<WebOSTVService.CurrentProgramListener>) getCurrentProgram(true, listener);
    }
*/
    //2015.02.24 CIC hj - AppInfo 보냄
    public ServiceCommand<ResponseListener<Object>> getAppInfo(String id, final ResponseListener<Object> listener) {
        String uri = "ssap://com.webos.applicationManager/getAppInfo";

        ServiceCommand<ResponseListener<Object>> request;

        ResponseListener<Object> responseListener = new ResponseListener<Object>() {

            @Override
            public void onSuccess(Object response) {
                Util.postSuccess(listener, (JSONObject) response);
            }

            @Override
            public void onError(ServiceCommandError error) {
                Util.postError(listener, error);
            }
        };
        JSONObject payload = new JSONObject();
        try {
            payload.put("id", id);
        } catch (JSONException e){

        }
        request = new ServiceCommand<ResponseListener<Object>>(this, uri, payload, true, responseListener);
        request.send();

        return request;
    }

    //2015.02.24 CIC hj - DeepLink
    public ServiceCommand<ResponseListener<Object>> launchContent(JSONObject payload, final ResponseListener<Object> listener) {
        String uri = "ssap://com.webos.applicationManager/launch";

        ServiceCommand<ResponseListener<Object>> request;

        ResponseListener<Object> responseListener = new ResponseListener<Object>() {

            @Override
            public void onSuccess(Object response) {
                Util.postSuccess(listener, (JSONObject) response);
            }

            @Override
            public void onError(ServiceCommandError error) {
                Util.postError(listener, error);
            }
        };
        request = new ServiceCommand<ResponseListener<Object>>(this, uri, payload, true, responseListener);
        request.send();

        return request;
    }

    // 2015.04.1 LGSI - BT Agent - Raghavendra Ramesh
    public ServiceCommand<ResponseListener<Object>> setSoundOutput(String output, final ResponseListener<Object> listener) {
        String uri = "ssap://com.webos.service.apiadapter/audio/changeSoundOutput";

        ServiceCommand<ResponseListener<Object>> request;

        ResponseListener<Object> responseListener = new ResponseListener<Object>() {

            @Override
            public void onSuccess(Object response) {
                Util.postSuccess(listener, (JSONObject) response);
            }

            @Override
            public void onError(ServiceCommandError error) {
                Util.postError(listener, error);
            }
        };
        JSONObject payload = new JSONObject();
        try {
            payload.put("output", output);
        } catch (JSONException e){

        }
        request = new ServiceCommand<ResponseListener<Object>>(this, uri, payload, true, responseListener);
        request.send();

        return request;
    }

    public ServiceSubscription<ResponseListener> subscribeSoundOutput(ResponseListener listener) {
        // timeout modified from 20 seconds to 10 seconds, for performance improvement
        return (ServiceSubscription<ResponseListener>) getSoundOutput(true, listener);
    }

    // 2015.10.20 CIC - BT Agent - Haejung Kim - getSoundOutput
    private ServiceCommand<ResponseListener<Object>> getSoundOutput(boolean isSubscription, final ResponseListener<Object> listener) {
        ServiceCommand<ResponseListener<Object>> request;
        String url = "ssap://com.webos.service.apiadapter/audio/getSoundOutput";

        ResponseListener<Object> responseListener = new ResponseListener<Object>() {

            @Override
            public void onSuccess(Object response) {
                JSONObject jsonObj = (JSONObject) response;
                Util.postSuccess(listener, jsonObj);
            }

            @Override
            public void onError(ServiceCommandError error) {
                Util.postError(listener, error);
            }
        };

        JSONObject payload = new JSONObject();
//        try {
//            payload.put("deviceClass", deviceclass);
//            payload.put("seconds", seconds);
//        } catch (JSONException e){
//
//        }

        if (isSubscription) {
            request = new URLServiceSubscription<ResponseListener<Object>>(this, url, payload, true, responseListener);
        } else
            request = new ServiceCommand<ResponseListener<Object>>(this, url, payload, true, responseListener);

        request.send();

        return request;
    }


    // 2015.04.2 LGSI - BT Agent - Raghavendra Ramesh
      @SuppressWarnings("unchecked")
    public ServiceSubscription<ResponseListener> subscribeFindBTDevices(ResponseListener listener) {
		// timeout modified from 20 seconds to 10 seconds, for performance improvement
        return (ServiceSubscription<ResponseListener>) findBTAudioDevices(true, 4, 10, listener);
    }

    // 2015.04.2 LGSI - BT Agent - Raghavendra Ramesh
    private ServiceCommand<ResponseListener<Object>> findBTAudioDevices(boolean isSubscription, int deviceclass, int seconds, final ResponseListener<Object> listener) {
        ServiceCommand<ResponseListener<Object>> request;
        String BTDEVICE = "ssap://com.webos.service.bluetooth/gap/findDevices";

        ResponseListener<Object> responseListener = new ResponseListener<Object>() {

            @Override
            public void onSuccess(Object response) {
                JSONObject jsonObj = (JSONObject) response;
                Util.postSuccess(listener, jsonObj);
            }

            @Override
            public void onError(ServiceCommandError error) {
                Util.postError(listener, error);
            }
        };

        JSONObject payload = new JSONObject();
        try {
            payload.put("deviceClass", deviceclass);
            payload.put("seconds", seconds);
        } catch (JSONException e){

        }

        if (isSubscription) {
            request = new URLServiceSubscription<ResponseListener<Object>>(this, BTDEVICE, payload, true, responseListener);
        } else
            request = new ServiceCommand<ResponseListener<Object>>(this, BTDEVICE, payload, true, responseListener);

        request.send();

        return request;
    }

    //2015.04.30 cic hj - getStates
    public ServiceCommand<ResponseListener<Object>> getStatesOfBTAudioDevice(final ResponseListener<Object> listener) {

        String CONNECT = "ssap://com.webos.service.bluetooth/service/getStates";

        ServiceCommand<ResponseListener<Object>> request;

        ResponseListener<Object> responseListener = new ResponseListener<Object>() {

            @Override
            public void onSuccess(Object response) {

                JSONObject jsonObj = (JSONObject) response;
                Util.postSuccess(listener, jsonObj);

            }

            @Override
            public void onError(ServiceCommandError error) {
                Util.postError(listener, error);
            }
        };
        JSONObject payload = new JSONObject();
        try {
            payload.put("service", "audio");
        } catch (JSONException e){

        }
        request = new ServiceCommand<ResponseListener<Object>>(this, CONNECT, payload, true, responseListener);
        request.send();

        return request;
    }

    // 2015.04.2 LGSI - BT Agent - Raghavendra Ramesh
    public ServiceCommand<ResponseListener<Object>> connectBTAudioDevice(String mac, final ResponseListener<Object> listener) {

        String CONNECT = "ssap://com.webos.service.bluetooth/service/connect";

        ServiceCommand<ResponseListener<Object>> request;

        ResponseListener<Object> responseListener = new ResponseListener<Object>() {

            @Override
            public void onSuccess(Object response) {

                JSONObject jsonObj = (JSONObject) response;
                Util.postSuccess(listener, jsonObj);

            }

            @Override
            public void onError(ServiceCommandError error) {
                Util.postError(listener, error);
            }
        };
        JSONObject payload = new JSONObject();
        try {
            payload.put("service", "audio");
            payload.put("address", mac);
        } catch (JSONException e){

        }
        request = new ServiceCommand<ResponseListener<Object>>(this, CONNECT, payload, true, responseListener);
        request.send();

        return request;
    }

    // 2015.04.2 LGSI - BT Agent - Raghavendra Ramesh
    public ServiceCommand<ResponseListener<Object>> disconnectBTAudioDevice(String mac, final ResponseListener<Object> listener) {

        String DISCONNECT = "ssap://com.webos.service.bluetooth/service/disconnect";

        ServiceCommand<ResponseListener<Object>> request;

        ResponseListener<Object> responseListener = new ResponseListener<Object>() {

            @Override
            public void onSuccess(Object response) {

                JSONObject jsonObj = (JSONObject) response;
                Util.postSuccess(listener, jsonObj);

            }

            @Override
            public void onError(ServiceCommandError error) {
                Util.postError(listener, error);
            }
        };
        JSONObject payload = new JSONObject();
        try {
            payload.put("service", "audio");
            payload.put("address", mac);
        } catch (JSONException e){

        }
        request = new ServiceCommand<ResponseListener<Object>>(this, DISCONNECT, payload, true, responseListener);
        request.send();

        return request;
    }

    // 2015.04.2 LGSI - BT Agent - Raghavendra Ramesh
    public ServiceCommand<ResponseListener<Object>> getTrustedBTAudioDevice(final ResponseListener<Object> listener) {

        String TRUSTEDDEVICE = "ssap://com.webos.service.bluetooth/gap/getTrustedDevices";

        ServiceCommand<ResponseListener<Object>> request;

        ResponseListener<Object> responseListener = new ResponseListener<Object>() {

            @Override
            public void onSuccess(Object response) {

                JSONObject jsonObj = (JSONObject) response;
                Util.postSuccess(listener, jsonObj);

            }

            @Override
            public void onError(ServiceCommandError error) {
                Util.postError(listener, error);
            }
        };
        JSONObject payload = new JSONObject();
        try {
            payload.put("service", "audio");

        } catch (JSONException e){

        }
        request = new ServiceCommand<ResponseListener<Object>>(this, TRUSTEDDEVICE, payload, true, responseListener);
        request.send();

        return request;
    }

    // 2015.04.15 LGSI - BT Agent - Raghavendra Ramesh
    public ServiceCommand<ResponseListener<Object>> removeTrustedBTAudioDevice(String mac, final ResponseListener<Object> listener) {

        String REMOVETRUSTEDDEVICE = "ssap://com.webos.service.bluetooth/gap/removeTrustedDevice";

        ServiceCommand<ResponseListener<Object>> request;

        ResponseListener<Object> responseListener = new ResponseListener<Object>() {

            @Override
            public void onSuccess(Object response) {

                JSONObject jsonObj = (JSONObject) response;

                Util.postSuccess(listener, jsonObj);

            }

            @Override
            public void onError(ServiceCommandError error) {
                Util.postError(listener, error);
            }

        };

        JSONObject payload = new JSONObject();

        try {
            payload.put("address", mac);

        } catch (JSONException e){

        }
        request = new ServiceCommand<ResponseListener<Object>>(this, REMOVETRUSTEDDEVICE, payload, true, responseListener);
        request.send();

        return request;
    }
    
    public ServiceCommand<ResponseListener<Object>> getCurrentSWInfo(final ResponseListener<Object> listener) {
        String uri = "ssap://com.webos.service.update/getCurrentSWInformation";

        ServiceCommand<ResponseListener<Object>> request;

        ResponseListener<Object> responseListener = new ResponseListener<Object>() {

            @Override
            public void onSuccess(Object response) {
                JSONObject jsonObj = (JSONObject) response;
                Util.postSuccess(listener, (JSONObject) response);
            }

            @Override
            public void onError(ServiceCommandError error) {
                Util.postError(listener, error);
            }
        };

        request = new ServiceCommand<ResponseListener<Object>>(this, uri, null, true, responseListener);
        request.send();

        return request;
    }

    public ServiceCommand<ResponseListener<Object>> startUpdateByRemoteApp(String httpURL, final ResponseListener<Object> listener) {
        String uri = "ssap://com.webos.service.update/startUpdateByRemoteApp";

        ServiceCommand<ResponseListener<Object>> request;

        ResponseListener<Object> responseListener = new ResponseListener<Object>() {

            @Override
            public void onSuccess(Object response) {
                JSONObject jsonObj = (JSONObject) response;
                Util.postSuccess(listener, (JSONObject) response);
            }

            @Override
            public void onError(ServiceCommandError error) {
                Util.postError(listener, error);
            }
        };

        JSONObject payload = new JSONObject();
        try {
            payload.put("server_url", httpURL);
        } catch (JSONException e){

        }

        request = new ServiceCommand<ResponseListener<Object>>(this, uri, payload, true, responseListener);
        request.send();

        return request;
    }

    public ServiceSubscription<ResponseListener> subscribeFOTAInstallProgress(ResponseListener listener) {
        return (ServiceSubscription<ResponseListener>) getFOTAInstallProgress(true, listener);
    }

    private ServiceCommand<ResponseListener<Object>> getFOTAInstallProgress(boolean isSubscription, final ResponseListener<Object> listener) {
        ServiceCommand<ResponseListener<Object>> request;
        String uri = "ssap://com.webos.service.update/getProgress";

        ResponseListener<Object> responseListener = new ResponseListener<Object>() {

            @Override
            public void onSuccess(Object response) {
                JSONObject jsonObj = (JSONObject) response;
                Log.d("FOTA", jsonObj.toString());
                Util.postSuccess(listener, jsonObj);
            }

            @Override
            public void onError(ServiceCommandError error) {
                Util.postError(listener, error);
            }
        };

        JSONObject payload = new JSONObject();
        try {
            payload.put("subscribe", true);
        } catch (JSONException e){

        }

        if (isSubscription) {
            request = new URLServiceSubscription<ResponseListener<Object>>(this, uri, payload, true, responseListener);
        } else
            request = new ServiceCommand<ResponseListener<Object>>(this, uri, payload, true, responseListener);

        request.send();

        return request;
    }

    public ServiceSubscription<ResponseListener> subscribeFOTAInstallStatus(ResponseListener listener) {
        return (ServiceSubscription<ResponseListener>) getFOTAInstallStatus(true, listener);
    }

    private ServiceCommand<ResponseListener<Object>> getFOTAInstallStatus(boolean isSubscription, final ResponseListener<Object> listener) {
        ServiceCommand<ResponseListener<Object>> request;
        String uri = "ssap://com.webos.service.update/getStatus";

        ResponseListener<Object> responseListener = new ResponseListener<Object>() {

            @Override
            public void onSuccess(Object response) {
                JSONObject jsonObj = (JSONObject) response;
                Log.d("FOTA", jsonObj.toString());
                Util.postSuccess(listener, jsonObj);
            }

            @Override
            public void onError(ServiceCommandError error) {
                Util.postError(listener, error);
            }
        };

        JSONObject payload = new JSONObject();
        try {
            payload.put("subscribe", true);
        } catch (JSONException e){

        }

        if (isSubscription) {
            request = new URLServiceSubscription<ResponseListener<Object>>(this, uri, payload, true, responseListener);
        } else
            request = new ServiceCommand<ResponseListener<Object>>(this, uri, payload, true, responseListener);

        request.send();

        return request;
    }

    public void touchRaw(String rawData) {
        if (mouseSocket != null)
            mouseSocket.touchRaw(rawData);
        else {
            connectMouse();
        }
    }

    // 2015.04.20 LGSI - BT Agent - Raghavendra Ramesh
    public ServiceSubscription<ResponseListener> subscribeNotifications(ResponseListener listener) {
        return (ServiceSubscription<ResponseListener>) getNotification(true, listener);
    }


    // 2015.04.20 LGSI - BT Agent - Raghavendra Ramesh
    private ServiceCommand<ResponseListener<Object>> getNotification(boolean isSubscription, final ResponseListener<Object> listener) {
        ServiceCommand<ResponseListener<Object>> request;
        String SUBSCRIBE = "ssap://com.webos.service.bluetooth/service/subscribeNotifications";

        ResponseListener<Object> responseListener = new ResponseListener<Object>() {

            @Override
            public void onSuccess(Object response) {
                JSONObject jsonObj = (JSONObject) response;
                Util.postSuccess(listener, jsonObj);
            }

            @Override
            public void onError(ServiceCommandError error) {
                Util.postError(listener, error);
            }
        };

        JSONObject payload = new JSONObject();
        try {
            payload.put("subscribe", isSubscription);
        } catch (JSONException e){

        }

        if (isSubscription) {
            request = new URLServiceSubscription<ResponseListener<Object>>(this, SUBSCRIBE, payload, true, responseListener);
        } else
            request = new ServiceCommand<ResponseListener<Object>>(this, SUBSCRIBE, payload, true, responseListener);

        request.send();

        return request;
    }



    //2015.06.24 hj - UAC
    public ServiceCommand<ResponseListener<Object>> setUserInfo(JSONObject payload, final ResponseListener<Object> listener) {

        String url = "ssap://user/setUserInfo";

        ServiceCommand<ResponseListener<Object>> request;

        ResponseListener<Object> responseListener = new ResponseListener<Object>() {

            @Override
            public void onSuccess(Object response) {

                JSONObject jsonObj = (JSONObject) response;
                Util.postSuccess(listener, jsonObj);

            }

            @Override
            public void onError(ServiceCommandError error) {
                Util.postError(listener, error);
            }
        };
        request = new ServiceCommand<ResponseListener<Object>>(this, url, payload, true, responseListener);
        request.send();

        return request;
    }

    public ServiceCommand<ResponseListener<Object>> delelteUserInfo(JSONObject payload, final ResponseListener<Object> listener) {

        String url = "ssap://user/resetUserInfo";

        ServiceCommand<ResponseListener<Object>> request;

        ResponseListener<Object> responseListener = new ResponseListener<Object>() {

            @Override
            public void onSuccess(Object response) {

                JSONObject jsonObj = (JSONObject) response;
                Util.postSuccess(listener, jsonObj);

            }

            @Override
            public void onError(ServiceCommandError error) {
                Util.postError(listener, error);
            }
        };
        request = new ServiceCommand<ResponseListener<Object>>(this, url, payload, true, responseListener);
        request.send();

        return request;
    }

    //2015.06.29 hj - My Starter
    public ServiceCommand<ResponseListener<Object>> setUserSchedule(JSONObject payload, final ResponseListener<Object> listener) {

        String url = "ssap://user/setUserSchedule";

        ServiceCommand<ResponseListener<Object>> request;

        ResponseListener<Object> responseListener = new ResponseListener<Object>() {

            @Override
            public void onSuccess(Object response) {

                JSONObject jsonObj = (JSONObject) response;
                Util.postSuccess(listener, jsonObj);

            }

            @Override
            public void onError(ServiceCommandError error) {
                Util.postError(listener, error);
            }
        };
        request = new ServiceCommand<ResponseListener<Object>>(this, url, payload, true, responseListener);
        request.send();

        return request;
    }

    //2015.06.30 hj - wifi-mac
    public ServiceCommand<ResponseListener<Object>> getWifiMacAddress(final ResponseListener<Object> listener) {

        String DISCONNECT = "ssap://com.webos.service.connectionmanager/getinfo";

        ServiceCommand<ResponseListener<Object>> request;

        ResponseListener<Object> responseListener = new ResponseListener<Object>() {

            @Override
            public void onSuccess(Object response) {

                JSONObject jsonObj = (JSONObject) response;
                Util.postSuccess(listener, jsonObj);

            }

            @Override
            public void onError(ServiceCommandError error) {
                Util.postError(listener, error);
            }
        };
        request = new ServiceCommand<ResponseListener<Object>>(this, DISCONNECT, null, true, responseListener);
        request.send();

        return request;
    }

    //2016.06.03 js 추가중
    // ~webOS 3.5 : getP2pState, webOS 4.0~ : getState
    public ServiceSubscription<ResponseListener> subscribeP2pState(ResponseListener<Object> listener) {
        return (ServiceSubscription<ResponseListener>) getP2pState(true, listener);
    }
    private ServiceCommand<ResponseListener<Object>> getP2pState(boolean isSubscription, final ResponseListener<Object> listener) {
        ServiceCommand<ResponseListener<Object>> request;
        String uri = "";
        if(TmsConfig.getFeatureType() >= TmsConfig.VER_40_UP) {
            uri = "ssap://com.webos.service.miracast/getState";
        } else {
            uri = "ssap://com.webos.service.miracast/getP2pState";
        }

        ResponseListener<Object> responseListener = new ResponseListener<Object>() {

            @Override
            public void onSuccess(Object response) {
                Util.postSuccess(listener, response);
            }

            @Override
            public void onError(ServiceCommandError error) {
                Util.postError(listener, error);
            }
        };

        if (isSubscription)
            request = new URLServiceSubscription<ResponseListener<Object>>(this, uri, null, true, responseListener);
        else
            request = new ServiceCommand<ResponseListener<Object>>(this, uri, null, true, responseListener);

        request.send();

        return request;
    }

    //2015.07.17 cic hj - OnTimer 추가
    public ServiceCommand<ResponseListener<Object>> setTimer(JSONObject payload, final ResponseListener<Object> listener) {

        String url = "ssap://timer/setSettings";

        ServiceCommand<ResponseListener<Object>> request;

        ResponseListener<Object> responseListener = new ResponseListener<Object>() {

            @Override
            public void onSuccess(Object response) {
                JSONObject jsonObj = (JSONObject) response;
                Util.postSuccess(listener, jsonObj);

            }

            @Override
            public void onError(ServiceCommandError error) {
                Util.postError(listener, error);
            }
        };
        request = new ServiceCommand<ResponseListener<Object>>(this, url, payload, true, responseListener);
        request.send();

        return request;
    }

    public void getOnTimer(JSONObject payload, ResponseListener<Object> listener) {
        getOnTimer(payload, false, listener);
    }
    public ServiceSubscription<ResponseListener> subscribeOnTimer(JSONObject payload,ResponseListener<Object> listener) {
        return (ServiceSubscription<ResponseListener>) getOnTimer(payload, true, listener);
    }
    private ServiceCommand<ResponseListener<Object>> getOnTimer(JSONObject payload, boolean isSubscription, final ResponseListener<Object> listener) {
        ServiceCommand<ResponseListener<Object>> request;
        String uri = "ssap://timer/getSettings";
        ResponseListener<Object> responseListener = new ResponseListener<Object>() {

            @Override
            public void onSuccess(Object response) {
                Util.postSuccess(listener, response);
            }

            @Override
            public void onError(ServiceCommandError error) {
                Util.postError(listener, error);
            }
        };

        if (isSubscription)
            request = new URLServiceSubscription<ResponseListener<Object>>(this, uri, payload, true, responseListener);
        else
            request = new ServiceCommand<ResponseListener<Object>>(this, uri, payload, true, responseListener);

        request.send();

        return request;
    }


    public ServiceCommand<ResponseListener<Object>> launchOverlayApp(final ResponseListener<Object> listener) {
        String url = "";
        JSONObject payload = new JSONObject();
        if(TmsConfig.getFeatureType() >= TmsConfig.VER_40_UP) {
            url = "ssap://com.webos.service.miracast/launchOverlayApp";
            try {
                payload.put("sender", "com.lgsmartplatform.redirect.SampleMobileWebApp.miracast");
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        ServiceCommand<ResponseListener<Object>> request;
        ResponseListener<Object> responseListener = new ResponseListener<Object>() {
            @Override
            public void onSuccess(Object response) {
                JSONObject jsonObj = (JSONObject) response;
                Util.postSuccess(listener, jsonObj);
            }
            @Override
            public void onError(ServiceCommandError error) {
                Util.postError(listener, error);
            }
        };
        request = new ServiceCommand<ResponseListener<Object>>(this, url, payload, true, responseListener);
        request.send();
        return request;
    }

    //kheo new API checkMircast 4.0~
    /*
       Name : checkMiracast
       Parameter : isAPMode - for AP mode(true), request getConnectedDeviceInfo service
                              for p2p mode(false), request getstatus service.
                              calling checkMiracast at very firsttime, parameter isAPMode should be true,
                              so we can get the connectionmode.
     */
    public ServiceCommand<ResponseListener<Object>> getConnectionMode(final ResponseListener<Object> listener) {
        String url = "";
        JSONObject payload = new JSONObject();
        url = "ssap://com.webos.service.miracast/getConnectedDeviceInfo";
        try {
            payload.put("subscribe", true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ServiceCommand<ResponseListener<Object>> request;
        ResponseListener<Object> responseListener = new ResponseListener<Object>() {
            @Override
            public void onSuccess(Object response) {
                JSONObject jsonObj = (JSONObject) response;
                Util.postSuccess(listener, jsonObj);
            }
            @Override
            public void onError(ServiceCommandError error) {
                Util.postError(listener, error);
            }
        };
        request = new ServiceCommand<ResponseListener<Object>>(this, url, payload, true, responseListener);
        request.send();
        return request;
    }



    public ServiceCommand<ResponseListener<Object>> getMiracastInfo(boolean isAPMode, final ResponseListener<Object> listener) {
        String url = "";
        JSONObject payload = new JSONObject();
        if(TmsConfig.getFeatureType() >= TmsConfig.VER_40_UP) {
            if(isAPMode) {
                url = "ssap://com.webos.service.miracast/getConnectedDeviceInfo";
            } else {
                url = "ssap://com.webos.service.connectionmanager/getstatus";
                try {
                    payload.put("subscribe", true);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        ServiceCommand<ResponseListener<Object>> request;
        ResponseListener<Object> responseListener = new ResponseListener<Object>() {
            @Override
            public void onSuccess(Object response) {
                JSONObject jsonObj = (JSONObject) response;
                Util.postSuccess(listener, jsonObj);
            }
            @Override
            public void onError(ServiceCommandError error) {
                Util.postError(listener, error);
            }
        };
        request = new ServiceCommand<ResponseListener<Object>>(this, url, payload, true, responseListener);
        request.send();
        return request;
    }

    // ~webOS 3.5 : close,  webOS 4.0~ : sendRTSPRequest
    public ServiceCommand<ResponseListener<Object>> closeMiracast(final ResponseListener<Object> listener) {
        String url = "";
        JSONObject payload = new JSONObject();
        if(TmsConfig.getFeatureType() >= TmsConfig.VER_40_UP) {
            url = "ssap://com.webos.service.miracast/sendRTSPRequest";
            try {
                payload.put("message", "teardown");
                payload.put("extraInfo", "tms");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            url = "ssap://com.webos.service.miracast/close";
            try {
                payload.put("reason", "tms");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        ServiceCommand<ResponseListener<Object>> request;

        ResponseListener<Object> responseListener = new ResponseListener<Object>() {

            @Override
            public void onSuccess(Object response) {

                JSONObject jsonObj = (JSONObject) response;
                Util.postSuccess(listener, jsonObj);

            }

            @Override
            public void onError(ServiceCommandError error) {
                Util.postError(listener, error);
            }
        };
        request = new ServiceCommand<ResponseListener<Object>>(this, url, payload, true, responseListener);
        request.send();

        return request;
    }

    // ~webOS 3.5 : setUACSettings,  webOS 4.0~ : setWFDConnectionMode
    public ServiceCommand<ResponseListener<Object>> enableMiracast(String uacMode, final ResponseListener<Object> listener) {
        String url = "";
        if(TmsConfig.getFeatureType() >= TmsConfig.VER_40_UP) {
            url = "ssap://com.webos.service.miracast/setWFDConnectionMode";
        } else {
            url = "ssap://com.webos.service.miracast/setUACSettings";
        }
        JSONObject payload = new JSONObject();
        try {
            payload.put("UACMode", uacMode);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ServiceCommand<ResponseListener<Object>> request;

        ResponseListener<Object> responseListener = new ResponseListener<Object>() {

            @Override
            public void onSuccess(Object response) {

                JSONObject jsonObj = (JSONObject) response;
                Util.postSuccess(listener, jsonObj);

            }

            @Override
            public void onError(ServiceCommandError error) {
                Util.postError(listener, error);
            }
        };
        request = new ServiceCommand<ResponseListener<Object>>(this, url, payload, true, responseListener);
        request.send();

        return request;
    }

    // webOS 4.0~
    public ServiceCommand<ResponseListener<Object>> getWFDConnectionMode(boolean isSubscribe, final ResponseListener<Object> listener) {

        String url = "ssap://com.webos.service.miracast/getWFDConnectionMode";
        JSONObject payload = new JSONObject();
        try {
            payload.put("subscribe", isSubscribe);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ServiceCommand<ResponseListener<Object>> request;

        ResponseListener<Object> responseListener = new ResponseListener<Object>() {

            @Override
            public void onSuccess(Object response) {

                JSONObject jsonObj = (JSONObject) response;
                Util.postSuccess(listener, jsonObj);

            }

            @Override
            public void onError(ServiceCommandError error) {
                Util.postError(listener, error);
            }
        };
        if(isSubscribe)
            request = new URLServiceSubscription<ResponseListener<Object>>(this, url, payload, true, responseListener);
        else
            request = new ServiceCommand<ResponseListener<Object>>(this, url, payload, true, responseListener);
        request.send();

        return request;
    }

    // ~webOS 3.5 : setUACSettings,  webOS 4.0~ : setWFDConnectionMode
    public ServiceCommand<ResponseListener<Object>> disableMiracast(final ResponseListener<Object> listener) {

        String url = "";
        if(TmsConfig.getFeatureType() >= TmsConfig.VER_40_UP) {
            url = "ssap://com.webos.service.miracast/setWFDConnectionMode";
        } else {
            url = "ssap://com.webos.service.miracast/setUACSettings";
        }
        JSONObject payload = new JSONObject();
        try {
            payload.put("UACMode", "disabled");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ServiceCommand<ResponseListener<Object>> request;

        ResponseListener<Object> responseListener = new ResponseListener<Object>() {

            @Override
            public void onSuccess(Object response) {

                JSONObject jsonObj = (JSONObject) response;
                Util.postSuccess(listener, jsonObj);

            }

            @Override
            public void onError(ServiceCommandError error) {
                Util.postError(listener, error);
            }
        };
        request = new ServiceCommand<ResponseListener<Object>>(this, url, payload, true, responseListener);
        request.send();

        return request;
    }

    public ServiceCommand<ResponseListener<Object>> isWiFiOnly(final ResponseListener<Object> listener) {
        String uri = "ssap://com.webos.service.bluetooth/gap/isWiFiOnly";

        ServiceCommand<ResponseListener<Object>> request;

        ResponseListener<Object> responseListener = new ResponseListener<Object>() {

            @Override
            public void onSuccess(Object response) {
                JSONObject jsonObj = (JSONObject) response;
                Util.postSuccess(listener, (JSONObject) response);
            }

            @Override
            public void onError(ServiceCommandError error) {
                Util.postError(listener, error);
            }
        };

        request = new ServiceCommand<ResponseListener<Object>>(this, uri, null, true, responseListener);
        request.send();

        return request;
    }

    // ~webOS 3.5
    public ServiceCommand<ResponseListener<Object>> getUibcKeyEvent(boolean isSubscribe, final ResponseListener<Object> listener) {
        String uri = "ssap://com.webos.service.miracast/uibc/getUibcKeyEvent";

        ServiceCommand<ResponseListener<Object>> request;
        JSONObject payload = new JSONObject();
        try {
            payload.put("subscribe", isSubscribe);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ResponseListener<Object> responseListener = new ResponseListener<Object>() {

            @Override
            public void onSuccess(Object response) {
                JSONObject jsonObj = (JSONObject) response;
                Util.postSuccess(listener, (JSONObject) response);
            }

            @Override
            public void onError(ServiceCommandError error) {
                Util.postError(listener, error);
            }
        };

        if(isSubscribe)
            request = new URLServiceSubscription<ResponseListener<Object>>(this, uri, payload, true, responseListener);
        else
            request = new ServiceCommand<ResponseListener<Object>>(this, uri, payload, true, responseListener);
        request.send();

        return request;
    }

    // ~webOS 3.5
    public ServiceCommand<ResponseListener<Object>> getConnectionStatus(final ResponseListener<Object> listener) {
        String uri = "ssap://com.webos.service.miracast/getConnectionStatus";

        ServiceCommand<ResponseListener<Object>> request;

        ResponseListener<Object> responseListener = new ResponseListener<Object>() {

            @Override
            public void onSuccess(Object response) {
                JSONObject jsonObj = (JSONObject) response;
                Util.postSuccess(listener, (JSONObject) response);
            }

            @Override
            public void onError(ServiceCommandError error) {
                Util.postError(listener, error);
            }
        };
        request = new ServiceCommand<ResponseListener<Object>>(this, uri, null, true, responseListener);
        request.send();

        return request;
    }

    public ServiceCommand<ResponseListener<Object>> setUserData(JSONObject payload, final ResponseListener<Object> listener) {

        String url = "ssap://user/setUserData";

        ServiceCommand<ResponseListener<Object>> request;

        ResponseListener<Object> responseListener = new ResponseListener<Object>() {

            @Override
            public void onSuccess(Object response) {

                JSONObject jsonObj = (JSONObject) response;
                Util.postSuccess(listener, jsonObj);

            }

            @Override
            public void onError(ServiceCommandError error) {
                Util.postError(listener, error);
            }
        };
        request = new ServiceCommand<ResponseListener<Object>>(this, url, payload, true, responseListener);
        request.send();

        return request;
    }

    public ServiceCommand<ResponseListener<Object>> getTwinConfig(boolean isSubscribe, final ResponseListener<Object> listener) {

        String url = "ssap://config/getConfigs";

        ServiceCommand<ResponseListener<Object>> request;
        JSONObject payload = new JSONObject();
        try {
            payload.put("configNames", "tv.model.twinTV");
            if(isSubscribe)
                payload.put("subscribe", true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ResponseListener<Object> responseListener = new ResponseListener<Object>() {

            @Override
            public void onSuccess(Object response) {
                JSONObject jsonObj = (JSONObject) response;
                Util.postSuccess(listener, (JSONObject) response);
            }

            @Override
            public void onError(ServiceCommandError error) {
                Util.postError(listener, error);
            }
        };

        if(isSubscribe)
            request = new URLServiceSubscription<ResponseListener<Object>>(this, url, payload, true, responseListener);
        else
            request = new ServiceCommand<ResponseListener<Object>>(this, url, payload, true, responseListener);
        request.send();

        return request;
    }

//    public ServiceCommand<ResponseListener<Object>> getTwinSetting(boolean isSubscribe, final ResponseListener<Object> listener) {
//
//        String url = "ssap://settings/getSystemSettings";
//
//        ServiceCommand<ResponseListener<Object>> request;
//        JSONObject payload = new JSONObject();
//        try {
//            payload.put("category", "twinTv");
//            JSONArray jsonArray = new JSONArray();
//            jsonArray.put("status");
//            jsonArray.put("role");
//            jsonArray.put("systemMode");
//            payload.put("keys",jsonArray);
//            if(isSubscribe)
//                payload.put("subscribe", true);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        ResponseListener<Object> responseListener = new ResponseListener<Object>() {
//
//            @Override
//            public void onSuccess(Object response) {
//                JSONObject jsonObj = (JSONObject) response;
//                Util.postSuccess(listener, (JSONObject) response);
//            }
//
//            @Override
//            public void onError(ServiceCommandError error) {
//                Util.postError(listener, error);
//            }
//        };
//
//        if(isSubscribe)
//            request = new URLServiceSubscription<ResponseListener<Object>>(this, url, payload, true, responseListener);
//        else
//            request = new ServiceCommand<ResponseListener<Object>>(this, url, payload, true, responseListener);
//        request.send();
//
//        return request;
//    }

    //15.10.27 cic hj - subscribeTwinSetting 추가
    public ServiceSubscription<ResponseListener> subscribeTwinSetting(ResponseListener<Object> listener) {
        return (ServiceSubscription<ResponseListener>) getTwinSetting(true, listener);
    }
    private ServiceCommand<ResponseListener<Object>> getTwinSetting(boolean isSubscription, final ResponseListener<Object> listener) {
        ServiceCommand<ResponseListener<Object>> request;
        String uri = "ssap://settings/getSystemSettings";

        JSONObject payload = new JSONObject();
        try {
            payload.put("category", "twinTv");
            JSONArray jsonArray = new JSONArray();
            jsonArray.put("status");  // complete / none/ undefined / inprogress
            jsonArray.put("role");    // undefined / master / slave
            jsonArray.put("systemMode"); // wide / split
            payload.put("keys",jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ResponseListener<Object> responseListener = new ResponseListener<Object>() {

            @Override
            public void onSuccess(Object response) {
                Util.postSuccess(listener, response);
            }

            @Override
            public void onError(ServiceCommandError error) {
                Util.postError(listener, error);
            }
        };

        if (isSubscription)
            request = new URLServiceSubscription<ResponseListener<Object>>(this, uri, payload, true, responseListener);
        else
            request = new ServiceCommand<ResponseListener<Object>>(this, uri, payload, true, responseListener);

        request.send();

        return request;
    }


    //15.11.18 cic hj - subscribeHostMessage 추가
    public ServiceSubscription<ResponseListener> subscribeHostMessage(ResponseListener<Object> listener) {
        return (ServiceSubscription<ResponseListener>) getHostMessage(true, listener);
    }
    private ServiceCommand<ResponseListener<Object>> getHostMessage(boolean isSubscription, final ResponseListener<Object> listener) {
        ServiceCommand<ResponseListener<Object>> request;
        String uri = "ssap://system/getHostMessage";

        ResponseListener<Object> responseListener = new ResponseListener<Object>() {

            @Override
            public void onSuccess(Object response) {
                Util.postSuccess(listener, response);
            }

            @Override
            public void onError(ServiceCommandError error) {
                Util.postError(listener, error);
            }
        };

        if (isSubscription)
            request = new URLServiceSubscription<ResponseListener<Object>>(this, uri, null, true, responseListener);
        else
            request = new ServiceCommand<ResponseListener<Object>>(this, uri, null, true, responseListener);

        request.send();

        return request;
    }

    //15.11.18 cic hj - subscribeHostMessage 추가
    public ServiceSubscription<ResponseListener> subscribePowerState(ResponseListener<Object> listener) {
        return (ServiceSubscription<ResponseListener>) getPowerState(true, listener);
    }
    private ServiceCommand<ResponseListener<Object>> getPowerState(boolean isSubscription, final ResponseListener<Object> listener) {
        ServiceCommand<ResponseListener<Object>> request;
        String uri = "ssap://com.webos.service.tvpower/power/getPowerState";

        ResponseListener<Object> responseListener = new ResponseListener<Object>() {

            @Override

            public void onSuccess(Object response) {
                Util.postSuccess(listener, response);
            }

            @Override
            public void onError(ServiceCommandError error) {
                Util.postError(listener, error);
            }
        };

        if (isSubscription)
            request = new URLServiceSubscription<ResponseListener<Object>>(this, uri, null, true, responseListener);
        else
            request = new ServiceCommand<ResponseListener<Object>>(this, uri, null, true, responseListener);

        request.send();

        return request;
    }

    //2015.12.07 cic hj - bt agent api 추
    public ServiceCommand<ResponseListener<Object>> getConfig(JSONObject payload, final ResponseListener<Object> listener) {
        String uri = "ssap://config/getConfigs";

        ServiceCommand<ResponseListener<Object>> request;

        ResponseListener<Object> responseListener = new ResponseListener<Object>() {

            @Override
            public void onSuccess(Object response) {
                JSONObject jsonObj = (JSONObject) response;
                Util.postSuccess(listener, (JSONObject) response);
            }

            @Override
            public void onError(ServiceCommandError error) {
                Util.postError(listener, error);
            }
        };

        request = new ServiceCommand<ResponseListener<Object>>(this, uri, payload, true, responseListener);
        request.send();

        return request;
    }

    public ServiceCommand<ResponseListener<Object>> sendAlert(JSONObject payload, final ResponseListener<Object> listener) {
        String uri = "ssap://system.notifications/createAlert";

        ServiceCommand<ResponseListener<Object>> request;

        ResponseListener<Object> responseListener = new ResponseListener<Object>() {

            @Override
            public void onSuccess(Object response) {
                Util.postSuccess(listener, response);
            }

            @Override
            public void onError(ServiceCommandError error) {
                Util.postError(listener, error);
            }
        };

        request = new ServiceCommand<>(this, uri, payload, true, responseListener);
        request.send();

        return request;
    }

    //2016.07.28 alert popup 추가
    public void closeAlert(JSONObject payload, ResponseListener<Object> listener) {
        String uri = "ssap://system.notifications/closeAlert";
        ServiceCommand<ResponseListener<Object>> request = new ServiceCommand<ResponseListener<Object>>(this, uri, payload, true, listener);
        request.send();
    }
}
