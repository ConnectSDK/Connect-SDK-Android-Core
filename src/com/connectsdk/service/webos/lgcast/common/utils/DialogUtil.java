package com.connectsdk.service.webos.lgcast.common.utils;

import android.app.AlertDialog;
import android.content.Context;
import java.util.List;

public class DialogUtil {
    public interface DialogClickListener {
        void onClick(int index);
    }

    public static void showDialog(Context context, String title, String message, DialogClickListener onClickOk, DialogClickListener onClickCancel) {
        showDialog(context, false, title, message, onClickOk, onClickCancel);
    }

    public static void showDialog(Context context, boolean cancelable, String title, String message, DialogClickListener onClickOk, DialogClickListener onClickCancel) {
        ThreadUtil.runOnMainLooper(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setCancelable(cancelable);
            if (title != null) builder.setTitle(title);
            if (message != null) builder.setMessage(message);
            if (onClickOk != null) builder.setPositiveButton(android.R.string.ok, (dialog, index) -> onClickOk.onClick(index));
            if (onClickCancel != null) builder.setNegativeButton(android.R.string.cancel, (dialog, index) -> onClickCancel.onClick(index));
            builder.show();
        });
    }

    //public static void showSimpleDialog(final Context context, final String title, final String message, final DialogClickListener onClickOk) {
    //    ThreadUtil.runOnMainLooper(() -> {
    //        AlertDialog.Builder builder = new AlertDialog.Builder(context);
    //        builder.setCancelable(false);
    //
    //        if (title != null)
    //            builder.setTitle(title);
    //
    //        if (message != null)
    //            builder.setMessage(message);
    //
    //        if (onClickOk != null) {
    //            builder.setPositiveButton(android.R.string.ok, (dialog, index) -> {
    //                onClickOk.onClick(index);
    //                dialog.dismiss();
    //            });
    //        }
    //        builder.show();
    //    });
    //}
    //
    //public static void showCancelableDialog(final Context context, final String title, final String message, final DialogClickListener onClickOk) {
    //    AlertDialog.Builder builder = new AlertDialog.Builder(context);
    //    builder.setTitle(title);
    //    builder.setMessage(message);
    //    builder.setCancelable(true);
    //    if (onClickOk != null) {
    //        builder.setPositiveButton(android.R.string.ok, (dialog, index) -> {
    //            onClickOk.onClick(index);
    //            dialog.dismiss();
    //        });
    //    }
    //    builder.setNegativeButton(android.R.string.cancel, null);
    //    builder.show();
    //}

    public static void showSingleChoiceOption(Context context, String title, List<String> items, DialogClickListener onClick) {
        showSingleChoiceOption(context, title, items, -1, true, onClick);
    }

    public static void showSingleChoiceOption(Context context, String title, List<String> items, boolean cancelable, DialogClickListener onClick) {
        showSingleChoiceOption(context, title, items, -1, cancelable, onClick);
    }

    public static void showSingleChoiceOption(Context context, String title, List<String> items, int selected, DialogClickListener onClick) {
        showSingleChoiceOption(context, title, items, selected, true, onClick);
    }

    public static void showSingleChoiceOption(Context context, String title, List<String> items, int selected, boolean cancelable, DialogClickListener onClick) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setCancelable(cancelable);

        builder.setSingleChoiceItems(items.toArray(new String[items.size()]), selected, (dialog, index) -> {
            if (onClick != null)
                onClick.onClick(index);
            dialog.dismiss();
        });

        //builder.setOnCancelListener((dialog) -> {
        //    if (onCancel != null)
        //        onCancel.onCancel(dialog);
        //   dialog.dismiss();
        //});

        builder.show();
    }
}
