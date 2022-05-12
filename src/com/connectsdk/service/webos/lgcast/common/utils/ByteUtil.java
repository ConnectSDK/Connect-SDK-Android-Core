/*
 * Copyright (c) 2020 LG Electronics Inc.
 * SPDX-License-Identifier: LicenseRef-LGE-Proprietary
 */
package com.connectsdk.service.webos.lgcast.common.utils;

import android.graphics.Bitmap;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class ByteUtil {
    public static ByteBuffer toByteBuffer(byte[] byteArray) {
        if (byteArray == null)
            return null;

        ByteBuffer bb = ByteBuffer.allocate(byteArray.length);
        bb.put(byteArray);
        bb.rewind();
        return bb;
    }

    public static byte[] toByteArray(ByteBuffer byteBuffer) {
        if (byteBuffer == null)
            return null;

        byte[] byteArray = new byte[byteBuffer.rewind().remaining()];
        byteBuffer.get(byteArray);
        return byteArray;
    }

    public static byte[] toByteArray(short[] byteArray) {
        if (byteArray == null)
            return null;

        byte[] bytes = new byte[byteArray.length * 2];
        int i = 0;

        for (int var4 = byteArray.length; i < var4; ++i) {
            int var10001 = i * 2;
            short var5 = byteArray[i];
            short var6 = 255;
            int var9 = var10001;
            boolean var7 = false;
            short var10 = (short) (var5 & var6);
            bytes[var9] = (byte) var10;
            bytes[i * 2 + 1] = (byte) (byteArray[i] >> 8);
            byteArray[i] = 0;
        }

        return bytes;
    }

    public static Bitmap toBitmap(ByteBuffer byteBuffer, int width, int height, Bitmap.Config config) {
        if (byteBuffer == null)
            return null;

        Bitmap bitmap = Bitmap.createBitmap(width, height, config);
        bitmap.copyPixelsFromBuffer(byteBuffer.rewind());
        return bitmap;
    }

    public static ByteBuffer copy(final ByteBuffer byteBuffer) {
        if (byteBuffer == null)
            return null;

        ByteBuffer copy = ByteBuffer.allocate(byteBuffer.rewind().remaining());
        copy.put(byteBuffer);
        copy.rewind();
        return copy;
    }

    public static int size(final ByteBuffer byteBuffer) {
        return (byteBuffer != null) ? byteBuffer.rewind().remaining() : -1;
    }

    public static byte[] copyOf(byte[] bytes) {
        return (bytes != null) ? Arrays.copyOf(bytes, bytes.length) : null;
    }

    public static byte[] copyOf(byte[] bytes, int offset, int length) {
        return (length > 0) ? Arrays.copyOfRange(bytes, offset, offset + length) : null;
    }
}
