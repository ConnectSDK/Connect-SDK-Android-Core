package com.connectsdk.service.webos.lgcast.remotecamera.capture;

import android.annotation.SuppressLint;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import com.connectsdk.service.webos.lgcast.common.utils.Logger;
import com.connectsdk.service.webos.lgcast.remotecamera.RemoteCameraConfig;
import com.lge.lib.mediacapture.iface.MediaData;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

public class MicCapture {
    private boolean mMicMute;
    private AtomicReference<Handler> mAudioStreamHandler;
    private AudioPcmTask mAudioPcmTask;

    /*private AtomicInteger mCaptureCount = new AtomicInteger(0);
    private AtomicInteger mCaptureSize = new AtomicInteger(0);
    private Timer mCountTimer = TimerUtil.schedule(() -> {
        String bb = StringUtil.toHumanReadableSize2(mCaptureSize.getAndSet(0) * 8);
        Logger.debug("### MIC CAPTURE: framerate=%d (fps)  bitrate=%s (bps) ###", mCaptureCount.getAndSet(0), bb);
    }, 1000, 1000);//*/

    public MicCapture() {
        mMicMute = false;
        mAudioStreamHandler = new AtomicReference();
    }

    public void startMicCapture(boolean micMute) {
        Logger.print("startMicCapture (micMute=%s)", micMute);
        mMicMute = micMute;
        mAudioStreamHandler.set(null);

        mAudioPcmTask = new AudioPcmTask();
        mAudioPcmTask.execute();
    }

    public void stopMicCapture() {
        Logger.print("stopMicCapture");
        if (mAudioPcmTask != null) mAudioPcmTask.cancel(true);
        mAudioPcmTask = null;
    }

    public boolean changeMicMute(boolean micMute) {
        Logger.print("changeMicMute (micMute=%s)", micMute);
        if (mMicMute == micMute) return false;
        mMicMute = micMute;
        return true;
    }

    public void startStreaming(Handler audioStreamHandler) {
        mAudioStreamHandler.set(audioStreamHandler);
    }

    public void stopStreaming() {
        mAudioStreamHandler.set(null);
    }

    @SuppressLint("MissingPermission")
    private class AudioPcmTask extends AsyncTask<Void, Void, Void> {
        final int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
        final int SAMPLE_RATE = RemoteCameraConfig.Mic.SAMPLING_RATE;
        final int ENCODING_BIT = RemoteCameraConfig.Mic.ENCODING_BIT;
        final int CHANNEL_MASK = RemoteCameraConfig.Mic.CHANNEL_MASK;
        final int BUFFER_SIZE = RemoteCameraConfig.Mic.BUFFER_SIZE;

        @Override
        public Void doInBackground(Void... params) {
            Logger.print("Start audio recording (BUFFER_SIZE=%d)", BUFFER_SIZE);
            AudioRecord audioRecord = new AudioRecord(AUDIO_SOURCE, SAMPLE_RATE, CHANNEL_MASK, ENCODING_BIT, BUFFER_SIZE);
            audioRecord.startRecording();

            while (!isCancelled()) {
                byte[] buffer = new byte[BUFFER_SIZE];
                int read = audioRecord.read(buffer, 0, BUFFER_SIZE);
                if (mMicMute == true) Arrays.fill(buffer, 0, read, (byte) 0x00);
                sendAudioSample(buffer);

                /*mCaptureCount.incrementAndGet();
                mCaptureSize.addAndGet(read);//*/
            }

            Logger.debug("stop audio recording");
            audioRecord.stop();
            audioRecord.release();
            return null;
        }
    }

    private void sendAudioSample(byte[] data) {
        Message msg = Message.obtain();
        msg.obj = new MediaData(0, data);
        if (mAudioStreamHandler.get() != null) mAudioStreamHandler.get().sendMessage(msg);
    }
}
