/*
 * Copyright (c) 2020 LG Electronics Inc.
 * SPDX-License-Identifier: LicenseRef-LGE-Proprietary
 */
package com.connectsdk.service.webos.lgcast.common.utils;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StringUtil {
    public static final String CR = "\r";
    public static final String LF = "\n";
    public static final String TAB = "\t";
    public static final String CRLF = "\r\n";
    public static final String SPACE = " ";
    public static final String EMPTY = "";

    public static boolean empty(String string) {
        return string == null || string.length() == 0;
    }

    public static String format(String format, Object... args) {
        try {
            return (args != null && args.length != 0) ? String.format(format, args) : format;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static int length(String value) {
        return (value != null) ? value.length() : -1;
    }

    public static int toInteger(String value) {
        try {
            return (value != null) ? Integer.parseInt(value) : -1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static int toInteger(String value, int defaultValue) {
        try {
            return (value != null && value.length() != 0) ? Integer.parseInt(value) : defaultValue;
        } catch (Exception e) {
            e.printStackTrace();
            return defaultValue;
        }
    }

    public static long toLong(String value) {
        try {
            return (value != null) ? Long.parseLong(value) : -1L;
        } catch (Exception e) {
            e.printStackTrace();
            return -1L;
        }
    }

    public static long toLong(String value, long defaultValue) {
        try {
            return (value != null) ? Long.parseLong(value) : defaultValue;
        } catch (Exception e) {
            e.printStackTrace();
            return defaultValue;
        }
    }

    public static long toLong(String value, int radix, long defaultValue) {
        try {
            return (value != null) ? Long.parseLong(value, radix) : defaultValue;
        } catch (Exception e) {
            e.printStackTrace();
            return defaultValue;
        }
    }

    public static byte[] toBytes(String value) {
        return (value != null) ? value.getBytes() : null;
    }

    public static String toString(int value) {
        try {
            return Integer.toString(value);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String toString(long value) {
        try {
            return Long.toString(value);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String toString(boolean value) {
        return (value == true) ? "true" : "false";
    }

    public static String toString(Intent intent) {
        StringBuffer buffer = new StringBuffer();
        String action = (intent != null) ? intent.getAction() : null;
        Bundle bundle = (intent != null) ? intent.getExtras() : null;

        if (action != null)
            buffer.append("Action: " + action + "\n");

        if (bundle != null)
            for (String key : bundle.keySet())
                buffer.append("Data: " + key + "=" + bundle.get(key) + "\n");

        return buffer.toString();
    }

    public static String toString(Bundle bundle) {
        StringBuffer buffer = new StringBuffer();

        if (bundle != null)
            for (String key : bundle.keySet())
                buffer.append(key + "=" + bundle.get(key) + "\n");

        return buffer.toString();
    }

    public static String toString(List<?> list, String delim) {
        StringBuffer buffer = new StringBuffer();

        if (list != null)
            for (Object obj : list)
                buffer.append(((buffer.length() > 0) ? delim : "") + obj.toString());

        return buffer.toString();
    }

    public static String toString(Object[] array, String delim) {
        StringBuffer buffer = new StringBuffer();

        if (array != null)
            for (Object obj : array)
                buffer.append(obj.toString() + delim);

        return buffer.toString();
    }

    public static String toString(byte[] bytes) {
        int sum = 0;

        if (bytes != null)
            for (byte b : bytes)
                sum += b;

        return (sum != 0) ? new String(bytes) : null;
    }

    public static String toHexString(byte[] bytes) {
        return toHexString(bytes, " ");
    }

    public static String toHexString(byte[] bytes, String delim) {
        StringBuffer buffer = new StringBuffer();

        if (bytes != null)
            for (byte b : bytes)
                buffer.append(((buffer.length() > 0) ? delim : "") + String.format("%02x", b & 0xff));

        return buffer.toString();
    }

    public static String toLower(String value) {
        return toLower(value, null);
    }

    public static String toLower(String value, String defaultValue) {
        return (value != null) ? value.toLowerCase(Locale.US) : defaultValue;
    }

    public static String toUpperCase(String value) {
        return toUpperCase(value, null);
    }

    public static String toUpperCase(String value, String defaultValue) {
        return (value != null) ? value.toUpperCase(Locale.US) : defaultValue;
    }

    public static ArrayList<String> split(String message, String delim) {
        return split(message, delim, 0);
    }

    public static ArrayList<String> split(String message, String delim, int limit) {
        if (message == null || delim == null)
            return new ArrayList<String>();

        ArrayList<String> tokenList = new ArrayList<String>();
        String[] tokens = (limit > 0) ? message.split(delim, limit) : message.split(delim);

        if (tokens != null) {
            for (String token : tokens)
                if (StringUtil.empty(token.trim()) == false)
                    tokenList.add(token.trim());
        }

        return tokenList;
    }

    public static String strip(String data) // trim and if empty set to null
    {
        data = (StringUtil.empty(data) == false) ? data.trim() : null;
        data = (StringUtil.empty(data) == false) ? data : null;
        return data;
    }

    public static String encodeURL(String url) {
        // To encode unreserved characters in uri
        // reference : RFC 3986 - Section 2.3 : Uniform Resource Identifier
        // URLEncoder class encodes a string using the format required by application/x-www-form-urlencoded MIME content type.
        // reference : http://developer.android.com/reference/java/net/URLEncoder.html
        // The URI contains characters which should be never encoded.
        // Thus we need to re-convert unreserved characters as follows.
        try {
            return (url != null) ? URLEncoder.encode(url, "UTF-8").replace("+", "%20").replace("*", "%2A").replace("%7E", "~") : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String decodeURL(String url) {
        try {
            return (url != null) ? URLDecoder.decode(url, "UTF-8") : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String appendURLParameter(String url, String name, String value) {
        return (url != null && name != null) ?
                Uri.parse(url).buildUpon().appendQueryParameter(name, value).toString() : url;
    }

    public static String encodeBase64(String message) {
        return encodeBase64(message, Base64.DEFAULT);
    }

    public static String encodeBase64(String message, int flags) {
        try {
            return (message != null) ? Base64.encodeToString(message.getBytes(StandardCharsets.UTF_8), flags) : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String decodeBase64(String message) {
        return decodeBase64(message, Base64.DEFAULT);
    }

    public static String decodeBase64(String message, int flags) {
        try {
            return (message != null) ? new String(Base64.decode(message, flags), StandardCharsets.UTF_8) : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String toXmlString(String message) {
        CharBuffer in = CharBuffer.wrap(message);
        StringBuffer out = new StringBuffer();

        for (int i = 0; i < in.length(); i++) {
            switch (in.get(i)) {
                case '"':
                    out.append("&quot;");
                    break;
                case '&':
                    out.append("&amp;");
                    break;
                case '\'':
                    out.append("&apos;");
                    break;
                case '<':
                    out.append("&lt;");
                    break;
                case '>':
                    out.append("&gt;");
                    break;
                default:
                    out.append(in.get(i));
                    break;
            }
        }

        return out.toString();
    }

    public static String toRandomCase(String value) {
        if (value == null)
            return null;

        char[] chars = value.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            int rand = (int) (Math.random() * 100);
            chars[i] = ((rand % 2) == 0) ? Character.toLowerCase(chars[i]) : Character.toUpperCase(chars[i]);
        }

        return new String(chars);
    }

    public static String toUnicode(String string) {
        StringBuffer buffer = new StringBuffer();
        int length = (string != null) ? string.length() : 0;

        for (int i = 0; i < length; i++) {
            char ch = string.charAt(i);

            if (ch >= 0x0020 && ch <= 0x007e) // ASCII: No need to convert
            {
                buffer.append(ch);
                continue;
            }

            String unicode = StringUtil.format("\\u%04x", ch & 0xFFFF);
            buffer.append(unicode);
        }

        return buffer.toString();
    }

    public static String toHumanReadableSize(long byteSize) {
        float kbSize = (float) byteSize / (1024);
        float mbSize = (float) byteSize / (1048576 /*1024 * 1024*/);
        float gbSize = (float) byteSize / (1073741824 /*1024 * 1024 * 1024*/);

        if ((int) gbSize > 0) return String.format(Locale.US, "%.02fGB", gbSize);
        else if ((int) mbSize > 0) return String.format(Locale.US, "%.02fMB", mbSize);
        else if ((int) kbSize > 0) return String.format(Locale.US, "%.02fKB", kbSize);
        else return String.format(Locale.US, "%dB", byteSize);
    }

    public static String toHumanReadableSize2(long size) {
        float sizeK = (float) size / (1024);
        float sizeM = (float) size / (1048576 /*1024 * 1024*/);
        float sizeG = (float) size / (1073741824 /*1024 * 1024 * 1024*/);

        if ((int) sizeG > 0) return String.format(Locale.US, "%.02fG", sizeG);
        else if ((int) sizeM > 0) return String.format(Locale.US, "%.02fM", sizeM);
        else if ((int) sizeK > 0) return String.format(Locale.US, "%.02fK", sizeK);
        else return String.format(Locale.US, "%d", size);
    }

    public static String after(String string, String after) {
        int pos = (string != null && after != null) ? string.indexOf(after) : -1;
        return (pos != -1) ? string.substring(pos + after.length()) : string;
    }
}
