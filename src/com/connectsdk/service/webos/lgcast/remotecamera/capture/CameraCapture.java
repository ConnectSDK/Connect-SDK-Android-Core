package com.connectsdk.service.webos.lgcast.remotecamera.capture;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.Message;
import android.view.Surface;
import androidx.annotation.NonNull;
import com.connectsdk.service.webos.lgcast.common.utils.HandlerThreadEx;
import com.connectsdk.service.webos.lgcast.common.utils.Logger;
import com.connectsdk.service.webos.lgcast.common.utils.ThreadUtil;
import com.connectsdk.service.webos.lgcast.remotecamera.RemoteCameraConfig;
import com.connectsdk.service.webos.lgcast.remotecamera.service.CameraProperty;
import com.connectsdk.service.webos.lgcast.remotecamera.service.CameraUtility;
import com.lge.lib.mediacapture.iface.MediaData;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

public class CameraCapture {
    public interface ErrorCallback {
        void onError(String error);
    }

    private Context mContext;
    private Surface mPreviewSurface;
    private ErrorCallback mErrorCallback;

    private ImageReader mImageReader;
    private HandlerThreadEx mImageReaderHandler;

    private CameraDevice mCameraDevice;
    private HandlerThreadEx mCameraDeviceHandler;

    private AtomicReference<Handler> mVideoStreamHandler;

    private CameraCaptureBulder mCameraCaptureBulder;
    private CameraCaptureSession mCameraCaptureSession;

    //--private Timer mCountTimer;
    //--private AtomicInteger mCaptureCount;
    //--private AtomicInteger mCaptureSize;

    public CameraCapture(Context context, Surface previewSurface, ErrorCallback errorCallback) {
        mContext = context;
        mPreviewSurface = previewSurface;
        mErrorCallback = (errorCallback != null) ? errorCallback : error -> Logger.debug("");
        mVideoStreamHandler = new AtomicReference();
    }

    @SuppressLint("MissingPermission")
    public void start(@NonNull CameraProperty cameraProperty) throws Exception {
        Logger.print("start");

        //--mCountTimer = (true) ? TimerUtil.schedule(() -> {
        //--    String bb = StringUtil.toHumanReadableSize2(mCaptureSize.getAndSet(0) * 8);
        //--    Logger.debug("### CAMERA CAPTURE: framerate=%d (fps)  bitrate=%s (bps) ###", mCaptureCount.getAndSet(0), bb);
        //--}, 1000, 1000) : null;
        //--mCaptureCount = new AtomicInteger(0);
        //--mCaptureSize = new AtomicInteger(0);

        mImageReaderHandler = new HandlerThreadEx("Image Reader Handler");
        mImageReaderHandler.start();

        boolean isMaxResolution = (cameraProperty.width == RemoteCameraConfig.Properties.MAX_WIDTH) && (cameraProperty.height == RemoteCameraConfig.Properties.MAX_HEIGHT);
        int jpegQuality = (isMaxResolution == true) ? RemoteCameraConfig.Camera.MIN_JPEG_QUALITY : RemoteCameraConfig.Camera.DEFAULT_JPEG_QUALITY;

        mImageReader = ImageReader.newInstance(cameraProperty.width, cameraProperty.height, ImageFormat.YUV_420_888, 10);
        mImageReader.setOnImageAvailableListener(imageReader -> onImageAvailable(imageReader, jpegQuality), mImageReaderHandler.getHandler());

        mCameraDeviceHandler = new HandlerThreadEx("Camera Device Handler");
        mCameraDeviceHandler.start();

        CameraManager cameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        String cameraId = CameraUtility.findCameraId(mContext, cameraProperty.facing);

        cameraManager.openCamera(cameraId, new CameraDevice.StateCallback() {
            @Override
            public void onOpened(CameraDevice cameraDevice) {
                Logger.error("Camera device opened (cameraId=%s)", cameraDevice.getId());
                mCameraDevice = cameraDevice;
                executeCapture(cameraProperty, cameraId, mPreviewSurface, mImageReader.getSurface());
            }

            @Override
            public void onDisconnected(CameraDevice cameraDevice) {
                Logger.error("Camera device disconnected (cameraID=%s)", cameraDevice.getId());
                mErrorCallback.onError("camera disconnected");
            }

            @Override
            public void onError(CameraDevice cameraDevice, int error) {
                Logger.error("Camera device error (cameraID=%s, error=%d)", cameraDevice.getId(), error);
                mErrorCallback.onError("camera error=" + error);
            }
        }, mCameraDeviceHandler.getHandler());
    }

    public void stop() {
        Logger.print("stop");
        if (mCameraDevice != null) mCameraDevice.close();
        mCameraDevice = null;

        if (mImageReader != null) mImageReader.close();
        mImageReader = null;

        if (mCameraDeviceHandler != null) mCameraDeviceHandler.quit();
        mCameraDeviceHandler = null;

        if (mImageReaderHandler != null) mImageReaderHandler.quit();
        mImageReaderHandler = null;

        //--if (mCountTimer != null) mCountTimer.cancel();
        //--mCountTimer = null;
    }

    public void startStreaming(Handler videoStreamHandler) {
        mVideoStreamHandler.set(videoStreamHandler);
    }

    public void stopStreaming() {
        mVideoStreamHandler.set(null);
    }

    public boolean restartPreview(@NonNull CameraProperty cameraProperty) {
        Logger.print("restartPreview");

        try {
            stop();
            ThreadUtil.sleep(1);
            start(cameraProperty);
            return true;
        } catch (Exception e) {
            Logger.error(e);
            return false;
        }
    }

    public boolean changeBrightness(int value) {
        Logger.print("changeBrightness (value=%d)", value);

        try {
            if (mCameraCaptureBulder.setBrightness(value) == false) throw new Exception("setBrightness failed");
            mCameraCaptureSession.setRepeatingRequest(mCameraCaptureBulder.build(), null, null);
            return true;
        } catch (Exception e) {
            Logger.error(e);
            return false;
        }
    }

    public boolean changeWhiteBalance(int value) {
        Logger.print("changeWhiteBalance (value=%d)", value);

        try {
            if (mCameraCaptureBulder.setWhiteBalance(value) == false) throw new Exception("setWhiteBalance failed");
            mCameraCaptureSession.setRepeatingRequest(mCameraCaptureBulder.build(), null, null);
            return true;
        } catch (Exception e) {
            Logger.error(e);
            return false;
        }
    }

    public boolean changeAutoWhiteBalance(boolean auto) {
        Logger.print("changeAutoWhiteBalance (auto=%s)", auto);

        try {
            if (mCameraCaptureBulder.setAutoWhiteBalance(auto) == false) throw new Exception("setAutoWhiteBalance failed");
            mCameraCaptureSession.setRepeatingRequest(mCameraCaptureBulder.build(), null, null);
            return true;
        } catch (Exception e) {
            Logger.error(e);
            return false;
        }
    }

    private void executeCapture(@NonNull CameraProperty cameraProperty, @NonNull String cameraId, Surface previewSurface, Surface readerSurface) {
        Logger.print("executeCapture");

        try {
            if (previewSurface == null || readerSurface == null) throw new Exception("Invalid arguments");
            mCameraCaptureBulder = new CameraCaptureBulder(mContext, mCameraDevice, cameraId);
            mCameraCaptureBulder.addTarget(previewSurface);
            mCameraCaptureBulder.addTarget(readerSurface);

            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface, readerSurface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Logger.debug("onConfigured");

                    try {
                        mCameraCaptureBulder.setBrightness(cameraProperty.brightness);
                        mCameraCaptureBulder.setWhiteBalance(cameraProperty.whiteBalance);
                        mCameraCaptureBulder.setAutoWhiteBalance(cameraProperty.autoWhiteBalance);

                        mCameraCaptureSession = cameraCaptureSession;
                        mCameraCaptureSession.setRepeatingRequest(mCameraCaptureBulder.build(), null, null);
                    } catch (Exception e) {
                        Logger.error(e);
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Logger.error("onConfigureFailed"); // No operation
                }
            }, null);
        } catch (Exception e) {
            Logger.error(e);
            mErrorCallback.onError("capture error=" + e.getMessage());
        }
    }

    private void onImageAvailable(ImageReader imageReader, int jpegQuality) {
        Image image = imageReader.acquireLatestImage();
        if (image == null) return;

        byte[] jpeg = JPEGEncoder.getJpegStream(image, jpegQuality);
        sendJpegFrame(jpeg);
        image.close();

        //--mCaptureCount.incrementAndGet();
        //--mCaptureSize.addAndGet(jpeg.length);
    }

    private void sendJpegFrame(byte[] jpeg) {
        Message msg = Message.obtain();
        msg.obj = new MediaData(0, jpeg);
        if (jpeg != null && mVideoStreamHandler.get() != null) mVideoStreamHandler.get().sendMessage(msg);
    }
}
