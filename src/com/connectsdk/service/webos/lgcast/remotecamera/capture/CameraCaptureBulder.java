package com.connectsdk.service.webos.lgcast.remotecamera.capture;

import android.content.Context;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.ColorSpaceTransform;
import android.hardware.camera2.params.RggbChannelVector;
import android.view.Surface;
import com.connectsdk.service.webos.lgcast.remotecamera.service.CameraUtility;

public class CameraCaptureBulder {
    private CameraCharacteristics mCameraCharacteristics;
    private CaptureRequest.Builder mCameraRequestBuilder;

    public CameraCaptureBulder(Context context, CameraDevice cameraDevice, String cameraId) throws Exception {
        mCameraCharacteristics = CameraUtility.getCameraCharacteristics(context, cameraId);
        mCameraRequestBuilder = (cameraDevice != null) ? cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW) : null;
        if (mCameraCharacteristics == null || mCameraRequestBuilder == null) throw new Exception("Invalid arguments");

        mCameraRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
        mCameraRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
    }

    public void addTarget(Surface outputTarget) {
        if (outputTarget != null) mCameraRequestBuilder.addTarget(outputTarget);
    }

    public boolean setBrightness(int value) {
        int brightness = CameraUtility.calculateBrightness(mCameraCharacteristics, value);
        if (brightness == -1) return false;

        mCameraRequestBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, brightness);
        return true;
    }

    public boolean setWhiteBalance(int value) {
        RggbChannelVector rggb = CameraUtility.calculateWhiteBalance(value);
        if (rggb == null) return false;

        mCameraRequestBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_OFF);
        mCameraRequestBuilder.set(CaptureRequest.COLOR_CORRECTION_MODE, CaptureRequest.COLOR_CORRECTION_MODE_TRANSFORM_MATRIX);
        mCameraRequestBuilder.set(CaptureRequest.COLOR_CORRECTION_TRANSFORM, new ColorSpaceTransform(new int[]{
                1, 1, 0, 1, 0, 1,
                0, 1, 1, 1, 0, 1,
                0, 1, 0, 1, 1, 1
        }));
        mCameraRequestBuilder.set(CaptureRequest.COLOR_CORRECTION_GAINS, rggb);
        return true;
    }

    public boolean setAutoWhiteBalance(boolean auto) {
        int awbMode = (auto == true) ? CaptureRequest.CONTROL_AWB_MODE_AUTO : CaptureRequest.CONTROL_AWB_MODE_OFF;
        mCameraRequestBuilder.set(CaptureRequest.CONTROL_AWB_MODE, awbMode);
        return true;
    }

    public CaptureRequest build() {
        return mCameraRequestBuilder.build();
    }
}
