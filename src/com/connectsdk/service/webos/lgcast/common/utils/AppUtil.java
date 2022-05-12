/*
 * Copyright (c) 2020 LG Electronics Inc.
 * SPDX-License-Identifier: LicenseRef-LGE-Proprietary
 */
package com.connectsdk.service.webos.lgcast.common.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.Toast;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AppUtil {
    public static boolean openActivity(final Activity activity, final Intent intent, final int flag) {
        if (activity != null && intent != null) {
            if (flag > 0)
                intent.addFlags(flag);

            activity.startActivity(intent);
            activity.finish();
        }

        return (activity != null);
    }

    public static void closeActivity(final Activity activity, final int resultCode, final Intent data) {
        if (activity != null) {
            activity.setResult(resultCode, data);
            activity.finish();
        }
    }

    public static void closeActivity(final Activity activity, final int resultCode, final Intent data, final long delayMillis) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                closeActivity(activity, resultCode, data);
            }
        };

        ThreadUtil.runOnMainLooper(r, delayMillis);
    }

    public static String getPackageName(final Context context) {
        return (context != null) ? context.getPackageName() : null;
    }

    public static void showToast(final Context context, final int duration, final String format, final Object... args) {
        if (Looper.myLooper() == Looper.getMainLooper())
            Toast.makeText(context, StringUtil.format(format, args), duration).show();
        else
            new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(context, StringUtil.format(format, args), duration).show());
    }

    public static void showToast(final Context context, final String format, final Object... args) {
        showToast(context, Toast.LENGTH_SHORT, format, args);
    }

    public static void showToastLong(final Context context, final String format, final Object... args) {
        showToast(context, Toast.LENGTH_LONG, format, args);
    }

    public static boolean isAvailable(final Context context, final String packageName) {
        try {
            if (context == null || packageName == null)
                throw new NullPointerException("context is null");

            int state = context.getPackageManager().getApplicationEnabledSetting(packageName);
            return (state == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT || state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getVersionName(final Context context, final String packageName) {
        try {
            if (context == null || packageName == null)
                throw new NullPointerException("context is null");

            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(packageName, 0);
            return (packageInfo != null) ? packageInfo.versionName : "0";
        } catch (Exception e) {
            e.printStackTrace();
            return "0";
        }
    }

    public static int getVersionCode(final Context context, final String packageName) {
        try {
            if (context == null || packageName == null)
                throw new NullPointerException("context is null");

            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(packageName, 0);
            return (packageInfo != null) ? packageInfo.versionCode : -1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static byte[] getAppSignature(final PackageManager packageManager, final String packageName, final String algorithm) {
        try {
            if (packageManager == null || packageName == null)
                throw new Exception("Invalid arguments");

            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
            Signature[] signatures = (packageInfo != null) ? packageInfo.signatures : new Signature[0];

            for (Signature signature : signatures) {
                java.security.MessageDigest md = java.security.MessageDigest.getInstance((algorithm == null) ? "SHA-1" : algorithm);
                md.update(signature.toByteArray());
                return md.digest();
            }

            throw new Exception("No signature found");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isEnabled(final Context context, final String packageName, final String className) {
        try {
            if (context == null || packageName == null || className == null)
                throw new Exception("Invalid arguments");

            switch (context.getPackageManager().getComponentEnabledSetting(new ComponentName(packageName, className))) {
                case PackageManager.COMPONENT_ENABLED_STATE_DEFAULT:
                case PackageManager.COMPONENT_ENABLED_STATE_ENABLED:
                    return true;

                case PackageManager.COMPONENT_ENABLED_STATE_DISABLED:
                case PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER:
                default:
                    return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean setEnabled(final Context context, final String packageName, final String className, final boolean enabled) {
        try {
            if (context == null || packageName == null || className == null)
                throw new Exception("Invalid arguments");

            ComponentName component = new ComponentName(packageName, className);
            context.getPackageManager().setComponentEnabledSetting(component, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static int getUserId() {
        try {
            Class<UserHandle> userHandle = UserHandle.class;
            Method myUserId = userHandle.getMethod("myUserId");

            Integer userId = (Integer) myUserId.invoke(userHandle, new Object[0]);
            return (userId != null) ? userId : -1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static int getUID(final Context context, String packageName) {
        try {
            PackageManager packageManager = (context != null) ? context.getPackageManager() : null;
            ApplicationInfo aInfo = (packageManager != null && packageName != null) ? packageManager.getApplicationInfo(packageName, 0) : null;
            return (aInfo != null) ? aInfo.uid : -1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static int getBinderCallerId(final Context context) {
        return Binder.getCallingPid();
    }

    public static String getBinderCallerName(final Context context) {
        int uid = Binder.getCallingUid();
        String[] packages = context.getPackageManager().getPackagesForUid(uid);

        if (packages != null)
            for (String packageName : packages)
                return packageName;

        return null;
    }

    @SuppressWarnings("deprecation")
    public static String getTopActivity(final Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        return am.getRunningTasks(1).get(0).topActivity.getClassName();
    }

    public static boolean isServiceRunning(final Context context, final Class cls) {
        if (context == null || cls == null)
            return false;

        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServices = manager.getRunningServices(Integer.MAX_VALUE);

        for (ActivityManager.RunningServiceInfo service : runningServices)
            if (cls.getName().equals(service.service.getClassName()))
                return true;

        return false;
    }

    public static void sendPendingIntentToActivity(final Context context, final Intent intent) {
        try {
            PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            pi.send();
        } catch (Exception e) {
            Logger.error(e);
        }
    }

    public static void moveToDetailSetting(final Context context) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
        intent.setData(uri);
        context.startActivity(intent);
    }

    public static void moveToAccessibilitySetting(final Context context) {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        context.startActivity(intent);
    }

    public static ActivityManager.RunningServiceInfo getServiceInfo(Context context, String className) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceInfos = am.getRunningServices(100);

        for (Iterator iterator = serviceInfos.iterator(); iterator.hasNext(); ) {
            ActivityManager.RunningServiceInfo serviceInfo = (ActivityManager.RunningServiceInfo) iterator.next();
            String serviceClassName = serviceInfo.service.getClassName();

            if (serviceClassName.equals(className) == true)
                return serviceInfo;
        }

        return null;
    }

    public static boolean isForegroundService(Context context, String className) {
        ActivityManager.RunningServiceInfo serviceInfo = AppUtil.getServiceInfo(context, className);
        return (serviceInfo != null) ? serviceInfo.foreground : false;
    }

    public static Activity getCurrentActivity() {
        try {
            Class activityThreadClass = Class.forName("android.app.ActivityThread");
            Object activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(null);
            Field activitiesField = activityThreadClass.getDeclaredField("mActivities");
            activitiesField.setAccessible(true);

            Map<Object, Object> activities = (Map<Object, Object>) activitiesField.get(activityThread);
            if (activities == null) return null;

            for (Object activityRecord : activities.values()) {
                Class activityRecordClass = activityRecord.getClass();
                Field pausedField = activityRecordClass.getDeclaredField("paused");
                pausedField.setAccessible(true);

                if (pausedField.getBoolean(activityRecord) == false) {
                    Field activityField = activityRecordClass.getDeclaredField("activity");
                    activityField.setAccessible(true);
                    Activity activity = (Activity) activityField.get(activityRecord);
                    return activity;
                }
            }

            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static DisplayMetrics getDisplayMetrics(Context context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        display.getRealMetrics(displayMetrics);
        return displayMetrics;
    }

    public static Point getDisplaySize(Context context) {
        if (context == null) throw new IllegalArgumentException();
        Point displaySize = new Point();
        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        display.getRealSize(displaySize);
        return displaySize;
    }

    public static Point getDisplaySizeInLandscape(Context context) {
        Point displaySize = getDisplaySize(context);
        if (isLandscape(context) == true) return new Point(displaySize.x, displaySize.y);
        else return new Point(displaySize.y, displaySize.x);
    }

    public static int getRotationDegree(Context context) {
        if (context == null) throw new IllegalArgumentException();
        WindowManager windowService = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int currentRotation = windowService.getDefaultDisplay().getRotation();
        if (Surface.ROTATION_0 == currentRotation) return 0;
        else if (Surface.ROTATION_90 == currentRotation) return 90;
        else if (Surface.ROTATION_180 == currentRotation) return 180;
        else if (Surface.ROTATION_270 == currentRotation) return 270;
        else return -1;
    }

    public static int getOrientation(Context context) {
        if (context == null) throw new IllegalArgumentException();
        WindowManager windowService = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int currentRotation = windowService.getDefaultDisplay().getRotation();
        if (Surface.ROTATION_0 == currentRotation) return Configuration.ORIENTATION_PORTRAIT;
        else if (Surface.ROTATION_180 == currentRotation) return Configuration.ORIENTATION_PORTRAIT;
        else if (Surface.ROTATION_90 == currentRotation) return Configuration.ORIENTATION_LANDSCAPE;
        else if (Surface.ROTATION_270 == currentRotation) return Configuration.ORIENTATION_LANDSCAPE;
        else return Configuration.ORIENTATION_UNDEFINED;
    }

    public static boolean isLandscape(Context context) {
        return getOrientation(context) == Configuration.ORIENTATION_LANDSCAPE;
    }

    public static boolean isPortrait(Context context) {
        return getOrientation(context) == Configuration.ORIENTATION_PORTRAIT;
    }

    public static int getActivityOrientation(Activity activity) {
        if (activity == null) throw new IllegalArgumentException();
        return activity.getResources().getConfiguration().orientation;
    }

    public static Rect getWindowVisibleDisplayFrame(Activity activity) {
        if (activity != null) {
            Rect rect = new Rect();
            activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
            return rect;
        } else {
            return null;
        }
    }
}
