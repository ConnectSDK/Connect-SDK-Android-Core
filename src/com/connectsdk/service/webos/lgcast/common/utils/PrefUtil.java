package com.connectsdk.service.webos.lgcast.common.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefUtil {
    public static boolean contains(Context context, String key) {
        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        return prefs.contains(key);
    }

    public static void remove(Context context, String key) {
        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(key);
        editor.commit();
    }

    public static void set(Context context, String key, String value) {
        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static void set(Context context, String key, boolean value) {
        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static void set(Context context, String key, int value) {
        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static void set(Context context, String key, long value) {
        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(key, value);
        editor.commit();
    }

    public static String get(Context context, String key, String defaultValue) {
        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        return prefs.getString(key, defaultValue);
    }

    public static boolean get(Context context, String key, boolean defaultValue) {
        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        return prefs.getBoolean(key, defaultValue);
    }

    public static int get(Context context, String key, int defaultValue) {
        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        return prefs.getInt(key, defaultValue);
    }

    public static long get(Context context, String key, long defaultValue) {
        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        return prefs.getLong(key, defaultValue);
    }
}
