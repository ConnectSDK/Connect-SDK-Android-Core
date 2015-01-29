package com.connectsdk.service.firetv;

import com.connectsdk.core.Util;
import com.connectsdk.service.DeviceService;
import com.connectsdk.service.capability.MediaPlayer;
import com.connectsdk.service.capability.listeners.ResponseListener;
import com.connectsdk.service.command.ServiceCommandError;
import com.connectsdk.service.sessions.LaunchSession;
import com.connectsdk.service.sessions.WebAppSession;
import com.google.android.gms.cast.ApplicationMetadata;

import org.json.JSONObject;

/**
 * Created by oleksii.frolov on 1/28/2015.
 */
public class FireTVWebAppSession extends WebAppSession {

	private FireTVSocket mWebSocket;

	/**
	 * Instantiates a WebAppSession object with all the information necessary to
	 * interact with a web app.
	 *
	 * @param webSocket
	 * @param launchSession LaunchSession containing info about the web app session
	 * @param service
	 */
	public FireTVWebAppSession(FireTVSocket webSocket, LaunchSession launchSession, DeviceService service) {
		super(launchSession, service);
		this.mWebSocket = webSocket;
	}

	@Override
	public void connect(final ResponseListener<Object> listener) {
		listener.onSuccess(null);
	}

	@Override
	public void join(ResponseListener<Object> connectionListener) {
		connectionListener.onSuccess(launchSession);
	}

	public void disconnectFromWebApp() {
	}

	public void handleAppClose() {
	}

	@Override
	public void sendMessage(String message, final ResponseListener<Object> listener) {
		if (message == null) {
			Util.postError(listener, new ServiceCommandError(0, "Cannot send null message", null));
			return;
		}

		if (mWebSocket == null) {
			Util.postError(listener, new ServiceCommandError(0, "Cannot send a message to the web app without first connecting", null));
			return;
		}

		mWebSocket.send(message);
		Util.postSuccess(listener, null);
	}

	@Override
	public void sendMessage(JSONObject message, ResponseListener<Object> listener) {
		sendMessage(message.toString(), listener);
	}

	@Override
	public void close(ResponseListener<Object> listener) {
		if (mWebSocket != null) {
			mWebSocket.close();
		}
		if (launchSession != null) {
			launchSession.close(listener);
			launchSession = null;
		}
	}

	/****************
	 * Media Player *
	 ****************/
	@Override
	public MediaPlayer getMediaPlayer() {
		return this;
	}

	@Override
	public CapabilityPriorityLevel getMediaPlayerCapabilityLevel() {
		return CapabilityPriorityLevel.HIGH;
	}

	@Override
	public void playMedia(String url, String mimeType, String title, String description, String iconSrc, boolean shouldLoop, MediaPlayer.LaunchListener listener) {

	}

	@Override
	public void closeMedia(LaunchSession launchSession, ResponseListener<Object> listener) {

	}

	public ApplicationMetadata getMetadata() {
		return null;
	}

	public void setMetadata(ApplicationMetadata metadata) {

	}
}
