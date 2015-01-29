package com.connectsdk.service.firetv;

import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

/**
 * Created by oleksii.frolov on 1/26/2015.
 */
public class FireTVSocket extends WebSocketClient {

	public FireTVSocket(String serverURI) {
		super(URI.create(serverURI));
	}

	@Override
	public void onOpen(ServerHandshake serverHandshake) {
		Log.d("", "ws open");
	}

	@Override
	public void onMessage(String s) {
		Log.d("", "ws message " + s);
	}

	@Override
	public void onClose(int i, String s, boolean b) {
		Log.d("", "ws close ");
	}

	@Override
	public void onError(Exception e) {
		Log.d("", "ws error " + e.getMessage());
		close();
	}
}
