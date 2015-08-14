/*
 * SubtitleStyle
 * Connect SDK
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.connectsdk.core;

/**
 * This class contains all possible ways of styling a subtitle. Not all styles are and will be supported on all devices,
 * a service can implement the styles that can be used in that specific service. This class is immutable and
 * has a builder for easy construction.
 */
public class SubtitleTrackStyle {

    public enum WindowType { NONE, NORMAL, ROUNDED_CORNERS }
    public enum FontStyle { NORMAL, ITALIC, BOLD, BOLD_ITALIC }
    public enum FontFamily { CASUAL, CURSIVE, MONOSPACED_SANS_SERIF, MONOSPACED_SERIF, SANS_SERIF, SERIF, SMALL_CAPITALS }
    public enum EdgeType { NONE, OUTLINE, DROP_SHADOW, RAISED, DEPRESSED }

    final WindowType windowType;
    final Integer windowRoundedCornerRadius;
    final FontStyle fontStyle;
    final Integer fontScale;
    final FontFamily fontFamily;
    final EdgeType edgeType;
    final String windowColor;
    final String foregroundColor;
    final String backgroundColor;
    final String edgeColor;

    public static class Builder {
        WindowType windowType;
        Integer windowRoundedCornerRadius;
        FontStyle fontStyle;
        Integer fontScale;
        FontFamily fontFamily;
        EdgeType edgeType;
        String windowColor;
        String foregroundColor;
        String backgroundColor;
        String edgeColor;

        public Builder setWindowType(@NonNull WindowType windowType) {
            this.windowType = windowType;
            return this;
        }

        public Builder setWindowRoundedCornerRadius(@NonNull Integer windowRoundedCornerRadius) {
            this.windowRoundedCornerRadius = windowRoundedCornerRadius;
            return this;
        }

        public Builder setFontStyle(@NonNull FontStyle fontStyle) {
            this.fontStyle = fontStyle;
            return this;
        }

        public Builder setFontScale(@NonNull Integer fontScale) {
            this.fontScale = fontScale;
            return this;
        }

        public Builder setFontFamily(@NonNull FontFamily fontFamily) {
            this.fontFamily = fontFamily;
            return this;
        }

        public Builder setEdgeType(@NonNull EdgeType edgeType) {
            this.edgeType = edgeType;
            return this;
        }

        public Builder setWindowColor(@NonNull String windowColor) {
            this.windowColor = windowColor;
            return this;
        }

        public Builder setForegroundColor(@NonNull String foregroundColor) {
            this.foregroundColor = foregroundColor;
            return this;
        }

        public Builder setBackgroundColor(@NonNull String backgroundColor) {
            this.backgroundColor = backgroundColor;
            return this;
        }

        public Builder setEdgeColor(@NonNull String edgeColor) {
            this.edgeColor = edgeColor;
            return this;
        }

        public SubtitleTrackStyle build() {
            return new SubtitleTrackStyle(this);
        }
    }

    public SubtitleTrackStyle(Builder builder) {
        windowType = builder.windowType;
        windowRoundedCornerRadius = builder.windowRoundedCornerRadius;
        windowColor = builder.windowColor;
        foregroundColor = builder.foregroundColor;
        fontStyle = builder.fontStyle;
        fontScale = builder.fontScale;
        fontFamily = builder.fontFamily;
        edgeType = builder.edgeType;
        edgeColor = builder.edgeColor;
        backgroundColor = builder.backgroundColor;
    }

    public WindowType getWindowType() {
        return windowType;
    }

    public Integer getWindowRoundedCornerRadius() {
        return windowRoundedCornerRadius;
    }

    public FontStyle getFontStyle() {
        return fontStyle;
    }

    public Integer getFontScale() {
        return fontScale;
    }

    public FontFamily getFontFamily() {
        return fontFamily;
    }

    public EdgeType getEdgeType() {
        return edgeType;
    }

    public String getWindowColor() {
        return windowColor;
    }

    public String getForegroundColor() {
        return foregroundColor;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public String getEdgeColor() {
        return edgeColor;
    }

}