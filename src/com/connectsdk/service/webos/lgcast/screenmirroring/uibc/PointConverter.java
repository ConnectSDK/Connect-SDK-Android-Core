/*
 * Copyright (c) 2020 LG Electronics Inc.
 * SPDX-License-Identifier: LicenseRef-LGE-Proprietary
 */
package com.connectsdk.service.webos.lgcast.screenmirroring.uibc;

import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;
import com.connectsdk.service.webos.lgcast.common.utils.Logger;

public class PointConverter {
    public static class Property {
        public float mobileScreenWidth;
        public float mobileScreenHeight;
        public boolean isWiderThanTV;
        public float tvVideoWidth;
        public float tvVideoHeight;

        public void debug() {
            Logger.debug("mobileScreenWidth=" + mobileScreenWidth);
            Logger.debug("mobileScreenHeight=" + mobileScreenHeight);
            Logger.debug("tvVideoWidth=" + tvVideoWidth);
            Logger.debug("tvVideoHeight=" + tvVideoHeight);
            Logger.debug("\n");
        }
    }

    public static class POINT {
        public float videoX;
        public float videoY;
        public float screenX;
        public float screenY;

        public void debug() {
            Logger.debug("videoX=" + videoX);
            Logger.debug("videoY=" + videoY);
            Logger.debug("screenX=" + screenX);
            Logger.debug("screenY=" + screenY);
            Logger.debug("\n");
        }
    }

    private final Display mDisplay;
    private int mVideoMaxWidth;
    private int mVideoMaxHeight;
    private Property mScreenProperty;

    public PointConverter(Context context, int videoMaxWidth, int videoMaxHeight) {
        mDisplay = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        update(videoMaxWidth, videoMaxHeight);
    }

    public void update(int videoMaxWidth, int videoMaxHeight) {
        mVideoMaxWidth = videoMaxWidth;
        mVideoMaxHeight = videoMaxHeight;
        updateProperty();
    }

    public POINT convert(float tvX, float tvY) {
        if (mScreenProperty.isWiderThanTV) return convertToLandscape(mScreenProperty, tvX, tvY);
        else return convertToPortrait(mScreenProperty, tvX, tvY);
    }

    private POINT convertToPortrait(Property portraitProperty, float tvX, float tvY) {
        POINT p = new POINT();
        p.videoX = tvX - ((mVideoMaxWidth - portraitProperty.tvVideoWidth) / 2f);
        p.videoY = tvY;

        if (p.videoX < 0 || p.videoX > portraitProperty.tvVideoWidth) p.videoX = -1f;
        if (p.videoY < 0 || p.videoY > portraitProperty.tvVideoHeight) p.videoY = -1f;

        p.screenX = (p.videoX != -1f) ? (p.videoX * portraitProperty.mobileScreenWidth) / portraitProperty.tvVideoWidth : -1;
        p.screenY = (p.videoY != -1f) ? (p.videoY * portraitProperty.mobileScreenHeight) / portraitProperty.tvVideoHeight : -1;
        return p;
    }

    private POINT convertToLandscape(Property landscapeProperty, float tvX, float tvY) {
        POINT p = new POINT();
        p.videoX = tvX;
        p.videoY = tvY - ((mVideoMaxHeight - landscapeProperty.tvVideoHeight) / 2f);

        if (p.videoX < 0 || p.videoX > landscapeProperty.tvVideoWidth) p.videoX = -1f;
        if (p.videoY < 0 || p.videoY > landscapeProperty.tvVideoHeight) p.videoY = -1f;

        p.screenX = (p.videoX != -1f) ? (p.videoX * landscapeProperty.mobileScreenWidth) / landscapeProperty.tvVideoWidth : -1;
        p.screenY = (p.videoY != -1f) ? (p.videoY * landscapeProperty.mobileScreenHeight) / landscapeProperty.tvVideoHeight : -1;
        return p;
    }

    private void updateProperty() {
        mScreenProperty = new Property();

        Point displaySize = new Point();
        mDisplay.getRealSize(displaySize);
        mScreenProperty.mobileScreenWidth = (float) displaySize.x;
        mScreenProperty.mobileScreenHeight = (float) displaySize.y;

        float screenRatio = mScreenProperty.mobileScreenWidth / mScreenProperty.mobileScreenHeight;
        float tvRatio = (float) mVideoMaxWidth / mVideoMaxHeight;
        mScreenProperty.isWiderThanTV = screenRatio > tvRatio;

        float scale;
        if (mScreenProperty.isWiderThanTV) scale = mVideoMaxWidth / mScreenProperty.mobileScreenWidth;
        else scale = mVideoMaxHeight / mScreenProperty.mobileScreenHeight;

        mScreenProperty.tvVideoWidth = Math.round(scale * mScreenProperty.mobileScreenWidth);
        mScreenProperty.tvVideoHeight = Math.round(scale * mScreenProperty.mobileScreenHeight);
    }
}
