package com.connectsdk.service.webos.lgcast.common.connection;

import androidx.annotation.NonNull;
import org.json.JSONObject;

public interface ConnectionManagerListener {
    void onPairingRequested();

    void onPairingRejected();

    void onConnectionFailed(@NonNull String failureMessage);

    void onConnectionCompleted(@NonNull JSONObject jsonObj);

    void onReceivePlayCommand(@NonNull JSONObject jsonObj);

    void onReceiveStopCommand(@NonNull JSONObject jsonObj);

    void onReceiveGetParameter(@NonNull JSONObject jsonObj);

    void onReceiveSetParameter(@NonNull JSONObject jsonObj);

    void onError(@NonNull ConnectionManagerError connectionError, @NonNull String errorMessage);
}
