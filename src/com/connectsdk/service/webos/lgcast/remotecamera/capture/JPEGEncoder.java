/*
 * Copyright (c) 2020 LG Electronics Inc.
 * SPDX-License-Identifier: LicenseRef-LGE-Proprietary
 */
package com.connectsdk.service.webos.lgcast.remotecamera.capture;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class JPEGEncoder {
    public static byte[] getJpegStream(Image image, int quality) {
        byte[] jpeg = null;
        if (image != null && image.getFormat() == ImageFormat.YUV_420_888) jpeg = yuv420ToJpeg(image, quality);
        else if (image != null && image.getFormat() == ImageFormat.JPEG) jpeg = imageToJpeg(image);
        return jpeg;
    }

    private static byte[] yuv420ToJpeg(Image image, int quality) {
        ByteBuffer yBuffer = image.getPlanes()[0].getBuffer();
        ByteBuffer vuBuffer = image.getPlanes()[2].getBuffer();

        int ySize = yBuffer.remaining();
        int vuSize = vuBuffer.remaining();
        byte[] nv21 = new byte[ySize + vuSize];

        yBuffer.get(nv21, 0, ySize);
        vuBuffer.get(nv21, ySize, vuSize);

        /*mirrorNV21(nv21, image.getWidth(), image.getHeight());*/

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        YuvImage yuv = new YuvImage(nv21, ImageFormat.NV21, image.getWidth(), image.getHeight(), null);
        yuv.compressToJpeg(new Rect(0, 0, image.getWidth(), image.getHeight()), quality, out);
        return out.toByteArray();
    }

    private static byte[] imageToJpeg(Image image) {
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer buffer = planes[0].getBuffer();
        byte[] jpeg = new byte[buffer.capacity()];
        buffer.get(jpeg);
        return jpeg;
    }

    //public static byte[] mirrorNV21(byte[] nv21_data, int width, int height) {
    //    int i;
    //    int left, right;
    //    byte temp;
    //    int startPos = 0;
    //
    //    // mirror Y
    //    for (i = 0; i < height; i++) {
    //        left = startPos;
    //        right = startPos + width - 1;
    //        while (left < right) {
    //            temp = nv21_data[left];
    //            nv21_data[left] = nv21_data[right];
    //            nv21_data[right] = temp;
    //            left++;
    //            right--;
    //        }
    //        startPos += width;
    //    }
    //    // mirror U and V
    //    int offset = width * height;
    //    startPos = 0;
    //    for (i = 0; i < height / 2; i++) {
    //        left = offset + startPos;
    //        right = offset + startPos + width - 2;
    //        while (left < right) {
    //            temp = nv21_data[left];
    //            nv21_data[left] = nv21_data[right];
    //            nv21_data[right] = temp;
    //            left++;
    //            right--;
    //
    //            temp = nv21_data[left];
    //            nv21_data[left] = nv21_data[right];
    //            nv21_data[right] = temp;
    //            left++;
    //            right--;
    //        }
    //        startPos += width;
    //    }
    //    return nv21_data;
    //}
}
