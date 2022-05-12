/*
 * Copyright (c) 2020 LG Electronics Inc.
 * SPDX-License-Identifier: LicenseRef-LGE-Proprietary
 */
package com.connectsdk.service.webos.lgcast.common.utils;

import android.content.Context;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IOUtil {
    public static int read(InputStream inputStream, byte[] buffer, int offset, int length) {
        try {
            if (inputStream == null) throw new IOException();
            if (buffer == null) throw new IOException();
            return inputStream.read(buffer, offset, length);
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static String readLine2(final InputStream input) throws Exception {
        try {
            if (input == null)
                throw new Exception("Invalid arguments");

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[1];

            while (input.read(data) > 0) {
                // HTTP 200 OK\r\n
                // \r\n

                if (data[0] == '\r')
                    continue;

                if (data[0] == '\n')
                    break;

                buffer.write(data[0]);
            }

            return (buffer.size() > 0) ? buffer.toString() : null;
        } catch (Exception e) {
            //com.lge.lib.util.log.Logger.W(e.toString());
            throw e;
        }
    }

    public static String readString(final InputStream input, final long length) throws IOException {
        if (input == null || length < 0)
            throw new IOException("Invalid arguments");

        byte[] data = readData(input, length);
        return (data != null) ? new String(data) : null;
    }

    public static byte[] readData(final InputStream input, final long length) throws IOException {
        if (input == null || length < 0)
            throw new IOException("Invalid arguments");

        long read = 0;
        long sum = 0;
        byte[] buffer = new byte[4 * 1024];
        ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();

        while (sum < length) {
            if ((read = input.read(buffer, 0, (int) Math.min(length - sum, buffer.length))) <= 0)
                break;

            byteArrayStream.write(buffer, 0, (int) read);
            sum += read;
        }

        return (byteArrayStream.size() > 0) ? byteArrayStream.toByteArray() : new byte[0];
    }

    public static void writeString(final OutputStream output, final String string) throws IOException {
        writeData(output, (string != null) ? string.getBytes() : null);
    }

    public static void writeData(final OutputStream output, final byte[] data) throws IOException {
        if (output == null)
            throw new IOException("Invalid stream");

        if (data == null)
            return;

        if (data.length > 0) {
            output.write(data, 0, data.length);
            output.flush();
        }
    }

    public static long copy(final String inputPath, final String outputPath) throws IOException {
        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            if (inputPath == null || outputPath == null)
                throw new IOException("Invalid file");

            inputStream = new FileInputStream(inputPath);
            outputStream = new FileOutputStream(outputPath);
            return copy(inputStream, outputStream);
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } finally {
            close(inputStream);
            close(outputStream);
        }
    }

    public static long copy(final InputStream input, final OutputStream output) throws IOException {
        return copy(input, output, Integer.MAX_VALUE);
    }

    public static long copy(final InputStream input, final OutputStream output, final long length) throws IOException {
        if (input == null || output == null)
            throw new IOException("Invalid stream");

        int maxSize = 0;
        int readSize = 0;
        long totalWrite = 0;
        long remainedSize = length;
        byte[] buffer = new byte[8 * 1024];

        while (true) {
            maxSize = (int) Math.min(buffer.length, remainedSize);
            readSize = input.read(buffer, 0, maxSize);

            if (readSize <= 0)
                break;

            output.write(buffer, 0, readSize);
            remainedSize -= readSize;
            totalWrite += readSize;

            if (remainedSize <= 0)
                break;
        }

        return totalWrite;
    }

    public static void close(final Closeable closeable) {
        try {
            if (closeable != null)
                closeable.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String readRawResourceText(Context context, int rawResId) {
        InputStream inputStream = null;

        try {
            byte[] buffer = new byte[128];
            inputStream = (context != null) ? context.getResources().openRawResource(rawResId) : null;
            if (inputStream == null) throw new Exception("Invalid resource");

            int len = inputStream.read(buffer, 0, 128);
            String val = (len > 0) ? new String(buffer, 0, len) : null;
            return (val != null)? val.trim() : null;
        } catch (Exception e) {
            return null;
        } finally {
            try {
                if (inputStream != null) inputStream.close();
            } catch (Exception e) {
            }
        }
    }
}
