package com.connectsdk.service;

import android.util.Log;

import com.connectsdk.core.AppInfo;
import com.connectsdk.core.MediaInfo;
import com.connectsdk.core.Util;
import com.connectsdk.discovery.DiscoveryFilter;
import com.connectsdk.discovery.DiscoveryManager;
import com.connectsdk.service.capability.CapabilityMethods;
import com.connectsdk.service.capability.Launcher;
import com.connectsdk.service.capability.MediaControl;
import com.connectsdk.service.capability.MediaPlayer;
import com.connectsdk.service.capability.WebAppLauncher;
import com.connectsdk.service.capability.listeners.ResponseListener;
import com.connectsdk.service.command.ServiceCommand;
import com.connectsdk.service.command.ServiceCommandError;
import com.connectsdk.service.command.ServiceSubscription;
import com.connectsdk.service.config.ServiceConfig;
import com.connectsdk.service.config.ServiceDescription;
import com.connectsdk.service.firetv.FireTVSocket;
import com.connectsdk.service.firetv.FireTVWebAppSession;
import com.connectsdk.service.sessions.LaunchSession;
import com.connectsdk.service.sessions.WebAppSession;

import org.java_websocket.WebSocket;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by oleksii.frolov on 1/22/2015.
 */
public class FireTVService extends DeviceService implements MediaPlayer, MediaControl, WebAppLauncher, ServiceCommand.ServiceCommandProcessor {

	public static final String ID = "FireTV";
	private static final String TAG = "FireTVService";
	private static final String APP_ID = "com.connectsdk.firetvcontainer";

	private volatile FireTVSocket mWebSocket;
	private DIALService dialService;
	private LaunchSession mAppState;
	private HandshakeThread handshakeThread;

	public static DiscoveryFilter discoveryFilter() {
		return new DiscoveryFilter(ID, "urn:dial-multiscreen-org:service:dial:1", "FireTV");
	}

	public FireTVService(ServiceDescription serviceDescription, ServiceConfig serviceConfig) {
		super(serviceDescription, serviceConfig);
	}

	@Override
	public boolean isConnected() {
		return connected;
	}

	@Override
	public boolean isConnectable() {
		return true;
	}


	@Override
	public void connect() {
		Log.d(TAG, TAG + " connect");
		super.connect();

		connected = true;
		reportConnected(true);
	}

	@Override
	public void disconnect() {
		Log.d(TAG, TAG + " disconnect");
		super.disconnect();
		connected = false;
		if (mWebSocket != null) {
			mWebSocket.close();
			mWebSocket = null;
			dialService = null;
		}
	}

	@Override
	protected void updateCapabilities() {
		List<String> capabilities = new ArrayList<String>();

		capabilities.add(WebAppLauncher.Launch);
		capabilities.add(WebAppLauncher.Message_Send);
		capabilities.add(WebAppLauncher.Message_Receive);
		capabilities.add(WebAppLauncher.Message_Send_JSON);
		capabilities.add(WebAppLauncher.Message_Receive_JSON);
		capabilities.add(WebAppLauncher.Connect);
		capabilities.add(WebAppLauncher.Disconnect);
		capabilities.add(WebAppLauncher.Join);
		capabilities.add(WebAppLauncher.Close);

		setCapabilities(capabilities);
	}

	@Override
	public MediaControl getMediaControl() {
		return this;
	}

	@Override
	public CapabilityPriorityLevel getMediaControlCapabilityLevel() {
		return CapabilityPriorityLevel.HIGH;
	}

	@Override
	public void play(ResponseListener<Object> listener) {

	}

	@Override
	public void pause(ResponseListener<Object> listener) {

	}

	@Override
	public void stop(ResponseListener<Object> listener) {

	}

	@Override
	public void rewind(ResponseListener<Object> listener) {

	}

	@Override
	public void fastForward(ResponseListener<Object> listener) {

	}

	@Override
	public void previous(ResponseListener<Object> listener) {

	}

	@Override
	public void next(ResponseListener<Object> listener) {

	}

	@Override
	public void seek(long position, ResponseListener<Object> listener) {

	}

	@Override
	public void getDuration(DurationListener listener) {

	}

	@Override
	public void getPosition(PositionListener listener) {

	}

	@Override
	public void getPlayState(PlayStateListener listener) {

	}

	@Override
	public ServiceSubscription<PlayStateListener> subscribePlayState(PlayStateListener listener) {
		return null;
	}

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

	}

	@Override
	public ServiceSubscription<MediaInfoListener> subscribeMediaInfo(MediaInfoListener listener) {
		return null;
	}

	@Override
	public void displayImage(String url, String mimeType, String title, String description, String iconSrc, final LaunchListener listener) {
		String method = "displayImage";

		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("url", url);

		ServiceCommand<ResponseListener<Object>> request = new ServiceCommand<ResponseListener<Object>>(this, method, new JSONObject(parameters), new ResponseListener<Object>() {
			@Override
			public void onSuccess(Object object) {
				LaunchSession session = createLaunchSession(FireTVService.this, LaunchSession.LaunchSessionType.Media);
				listener.onSuccess(new MediaLaunchObject(session, FireTVService.this, null));
			}

			@Override
			public void onError(ServiceCommandError error) {
				listener.onError(error);
			}
		});
		request.send();
	}

	@Override
	public void playMedia(String url, String mimeType, String title, String description, String iconSrc, boolean shouldLoop, LaunchListener listener) {

	}

	@Override
	public void closeMedia(LaunchSession launchSession, ResponseListener<Object> listener) {

	}

	@Override
	public void displayImage(MediaInfo mediaInfo, LaunchListener listener) {

	}

	@Override
	public void playMedia(MediaInfo mediaInfo, boolean shouldLoop, LaunchListener listener) {

	}

	@Override
	public void sendCommand(final ServiceCommand<?> command) {
		Util.runInBackground(new Runnable() {
			@Override
			public void run() {
				Log.d(TAG, TAG + " sendCommand");
				if (mWebSocket != null && mWebSocket.getReadyState() == WebSocket.READYSTATE.OPEN) {
					processCommand(command);
				} else {
					try {
						launchApp(command);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
	}

	private LaunchSession createLaunchSession(DeviceService service, LaunchSession.LaunchSessionType type) {
		LaunchSession session = new LaunchSession();
		session.setAppName(APP_ID);
		session.setAppId(APP_ID);
		session.setService(service);
		session.setSessionId(APP_ID + "/run");
		session.setSessionType(type);
		return session;
	}

	void processCommand(final ServiceCommand<?> command) {
		if (mWebSocket != null) {
			mWebSocket.send(command.getPayload().toString());
			Util.postSuccess(command.getResponseListener(), null);
		}
	}

	private void launchApp(final ServiceCommand<?> command) throws IOException, JSONException {
		launchApp(new ResponseListener<LaunchSession>() {
			@Override
			public void onSuccess(LaunchSession object) {
				processCommand(command);
			}

			@Override
			public void onError(ServiceCommandError error) {
				Util.postError(command.getResponseListener(), error);
			}
		});
	}

	private void launchApp(final ResponseListener<LaunchSession> listener) throws IOException, JSONException {
		launchApp(APP_ID, null, listener);
	}

	private void launchApp(String appId, JSONObject parameter, final ResponseListener<LaunchSession> listener) throws IOException, JSONException {
		AppInfo appInfo = new AppInfo();
		appInfo.setId(appId);
		appInfo.setName(appId);

		handshakeThread = new HandshakeThread(listener);
		int port = handshakeThread.getPort();
		handshakeThread.start();
		
		JSONObject params = new JSONObject();
		params.put("action", "SendPort");
		params.put("hostname", Util.getIpAddress(DiscoveryManager.getInstance().getContext()).getHostAddress());
		params.put("port", port);
		if (parameter != null) {
			params.put("parameter", parameter);
		}

		getDIALService().launchAppWithInfo(appInfo, params.toString(), new Launcher.AppLaunchListener() {
			@Override
			public void onSuccess(final LaunchSession object) {
				mAppState = object;
				handshakeThread.setLaunchSession(object);
			}

			@Override
			public void onError(ServiceCommandError error) {
				listener.onError(error);
			}
		});
	}

	DIALService getDIALService() {
		if (dialService == null) {
			dialService = getService(DIALService.class);
		}
		return dialService;
	}

	@Override
	public WebAppLauncher getWebAppLauncher() {
		return this;
	}

	@Override
	public CapabilityPriorityLevel getWebAppLauncherCapabilityLevel() {
		return CapabilityPriorityLevel.HIGH;
	}

	@Override
	public void launchWebApp(String webAppId, final WebAppSession.LaunchListener listener) {
		launchWebApp(webAppId, null, listener);
	}

	@Override
	public void launchWebApp(String webAppId, boolean relaunchIfRunning, WebAppSession.LaunchListener listener) {
		Util.postError(listener, ServiceCommandError.notSupported());
	}

	@Override
	public void launchWebApp(String webAppId, JSONObject params, final WebAppSession.LaunchListener listener) {
		try {
			launchApp(webAppId, params, new ResponseListener<LaunchSession>() {
				@Override
				public void onSuccess(LaunchSession object) {
					WebAppSession webAppSession = new FireTVWebAppSession(mWebSocket, object, FireTVService.this);
					Util.postSuccess(listener, webAppSession);
				}

				@Override
				public void onError(ServiceCommandError error) {
					Util.postError(listener, error);
				}
			});
		} catch (IOException e) {
			Util.postError(listener, new ServiceCommandError(0, e.getMessage(), e));
		} catch (JSONException e) {
			Util.postError(listener, new ServiceCommandError(0, e.getMessage(), e));
		}
	}

	@Override
	public void launchWebApp(String webAppId, JSONObject params, boolean relaunchIfRunning, WebAppSession.LaunchListener listener) {
		Util.postError(listener, ServiceCommandError.notSupported());
	}

	@Override
	public void joinWebApp(LaunchSession webAppLaunchSession, WebAppSession.LaunchListener listener) {
		launchWebApp(webAppLaunchSession.getAppId(), listener);
	}

	@Override
	public void joinWebApp(String webAppId, WebAppSession.LaunchListener listener) {
		launchWebApp(webAppId, listener);
	}

	@Override
	public void closeWebApp(LaunchSession launchSession, ResponseListener<Object> listener) {
		launchSession.close(listener);
	}


	class HandshakeThread extends Thread {

		private final ResponseListener<LaunchSession> listener;
		private ServerSocket server;
		private LaunchSession session;
		private CountDownLatch countDownLatch = new CountDownLatch(1);

		public HandshakeThread(ResponseListener<LaunchSession> listener) {
			try {
				server = new ServerSocket(0);
			} catch (IOException e) {
				e.printStackTrace();
			}
			this.listener = listener;
		}

		public int getPort() {
			return server.getLocalPort();
		}

		public void setLaunchSession(LaunchSession session) {
			this.session = session;
			countDownLatch.countDown();
		}

		public void run() {
			try {
				Socket socket = server.accept();
				BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				JSONObject response = new JSONObject(reader.readLine());
				reader.close();
				socket.close();
				server.close();

				mWebSocket = new FireTVSocket(response.getString("url"));
				if (mWebSocket.connectBlocking()) {
					countDownLatch.await(10, TimeUnit.SECONDS);
					listener.onSuccess(session);
				}
			} catch (Exception e) {
				Util.postError(listener, null);
				e.printStackTrace();
			}
		}
	}


}
