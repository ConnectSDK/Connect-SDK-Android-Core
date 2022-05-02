package com.connectsdk.service.webos.lgcast.remotecamera.service;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.RggbChannelVector;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.util.Range;
import android.util.Size;
import com.connectsdk.service.webos.lgcast.common.utils.Logger;
import com.connectsdk.service.webos.lgcast.remotecamera.RemoteCameraConfig;
import java.util.Arrays;
import java.util.List;

public class CameraUtility {
    public static String findCameraId(Context context, int lensFacing) {
        try {
            CameraManager cameraManager = (context != null) ? (CameraManager) context.getSystemService(Context.CAMERA_SERVICE) : null;
            if (cameraManager == null) throw new Exception("Invalid argument");

            for (String cameraId : cameraManager.getCameraIdList()) {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
                if (characteristics.get(characteristics.LENS_FACING) == lensFacing) return cameraId;
            }

            throw new Exception("Not found camera: " + lensFacing);
        } catch (Exception e) {
            Logger.error(e);
            return null;
        }
    }

    public static CameraCharacteristics getCameraCharacteristics(Context context, String cameraId) {
        try {
            CameraManager cameraManager = (context != null) ? (CameraManager) context.getSystemService(Context.CAMERA_SERVICE) : null;
            if (cameraManager == null || cameraId == null) throw new Exception("Invalid arguments");
            return cameraManager.getCameraCharacteristics(cameraId);
        } catch (Exception e) {
            Logger.error(e);
            return null;
        }
    }

    public static List<Size> getSupportedPreviewSizes(Context context) {
        String cameraId = findCameraId(context, CameraCharacteristics.LENS_FACING_FRONT);
        CameraCharacteristics characteristics = (cameraId != null) ? getCameraCharacteristics(context, cameraId) : null;
        StreamConfigurationMap info = (characteristics != null) ? characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP) : null;
        return (info != null) ? Arrays.asList(info.getOutputSizes(ImageFormat.YUV_420_888)) : Arrays.asList(new Size[0]);
    }

    public static int calculateBrightness(CameraCharacteristics cameraCharacteristics, int value) {
        if (cameraCharacteristics == null) return -1;
        if (value < RemoteCameraConfig.Properties.MIN_BRIGHTNESS || value > RemoteCameraConfig.Properties.MAX_BRIGHTNESS) return -1;

        Range<Integer> compensationRange = cameraCharacteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE);
        int minCompensationRange = compensationRange.getLower();
        int maxCompensationRange = compensationRange.getUpper();
        return (int) (minCompensationRange + (maxCompensationRange - minCompensationRange) * (value / 100f));
    }

    public static RggbChannelVector calculateWhiteBalance(int value) {
        if (value < RemoteCameraConfig.Properties.MIN_WHITE_BALANCE || value > RemoteCameraConfig.Properties.MAX_WHITE_BALANCE) return null;
        float temperature = value / 100;
        float red;
        float green;
        float blue;

        // Calculate red
        if (temperature <= 66) {
            red = 255;
        } else {
            red = temperature - 60;
            red = (float) (329.698727446 * (Math.pow((double) red, -0.1332047592)));
            if (red < 0) red = 0;
            if (red > 255) red = 255;
        }

        // Calculate green
        if (temperature <= 66) {
            green = temperature;
            green = (float) (99.4708025861 * Math.log(green) - 161.1195681661);
            if (green < 0) green = 0;
            if (green > 255) green = 255;
        } else {
            green = temperature - 60;
            green = (float) (288.1221695283 * (Math.pow((double) green, -0.0755148492)));
            if (green < 0) green = 0;
            if (green > 255) green = 255;
        }

        // calculate blue
        if (temperature >= 66) {
            blue = 255;
        } else if (temperature <= 19) {
            blue = 0;
        } else {
            blue = temperature - 10;
            blue = (float) (138.5177312231 * Math.log(blue) - 305.0447927307);
            if (blue < 0) blue = 0;
            if (blue > 255) blue = 255;
        }

        return new RggbChannelVector((red / 255) * 2, (green / 255), (green / 255), (blue / 255) * 2);
    }
}