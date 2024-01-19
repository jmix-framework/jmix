/*
 * Copyright 2023 Haulmont.
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

package io.jmix.chartsflowui.kit.component.model.toolbox;

import io.jmix.chartsflowui.kit.component.model.ChartObservableObject;
import io.jmix.chartsflowui.kit.component.model.HasEnumId;
import io.jmix.chartsflowui.kit.component.model.shared.*;
import jakarta.annotation.Nullable;

public class Emphasis extends ChartObservableObject {

    protected IconStyle iconStyle;

    public static class IconStyle extends AbstractBorderedTextStyle<IconStyle>
            implements HasPadding<IconStyle> {
        protected TextPosition textPosition;

        protected Color textFill;

        protected Align textAlign;

        protected Color textBackgroundColor;

        protected Integer textBorderRadius;

        protected Padding padding;

        public enum TextPosition implements HasEnumId {
            LEFT("left"),
            RIGHT("right"),
            TOP("top"),
            BOTTOM("bottom");

            private final String id;

            TextPosition(String id) {
                this.id = id;
            }

            @Override
            public String getId() {
                return id;
            }

            @Nullable
            public static TextPosition fromId(String id) {
                for (TextPosition at : TextPosition.values()) {
                    if (at.getId().equals(id)) {
                        return at;
                    }
                }
                return null;
            }
        }

        public TextPosition getTextPosition() {
            return textPosition;
        }

        public void setTextPosition(TextPosition textPosition) {
            this.textPosition = textPosition;
            markAsDirty();
        }

        public Color getTextFill() {
            return textFill;
        }

        public void setTextFill(Color textFill) {
            this.textFill = textFill;
            markAsDirty();
        }

        public Align getTextAlign() {
            return textAlign;
        }

        public void setTextAlign(Align textAlign) {
            this.textAlign = textAlign;
            markAsDirty();
        }

        public Color getTextBackgroundColor() {
            return textBackgroundColor;
        }

        public void setTextBackgroundColor(Color textBackgroundColor) {
            this.textBackgroundColor = textBackgroundColor;
            markAsDirty();
        }

        public Integer getTextBorderRadius() {
            return textBorderRadius;
        }

        public void setTextBorderRadius(Integer textBorderRadius) {
            this.textBorderRadius = textBorderRadius;
            markAsDirty();
        }

        @Override
        public Padding getPadding() {
            return padding;
        }

        @Override
        public void setPadding(Integer padding) {
            this.padding = new Padding(padding);
            markAsDirty();
        }

        @Override
        public void setPadding(Integer vertical, Integer horizontal) {
            this.padding = new Padding(vertical, horizontal);
            markAsDirty();
        }

        @Override
        public void setPadding(Integer top, Integer right, Integer bottom, Integer left) {
            this.padding = new Padding(top, right, bottom, left);
            markAsDirty();
        }

        public IconStyle withTextPosition(TextPosition textPosition) {
            setTextPosition(textPosition);
            return this;
        }

        public IconStyle withTextFill(Color textFill) {
            setTextFill(textFill);
            return this;
        }

        public IconStyle withTextAlign(Align textAlign) {
            setTextAlign(textAlign);
            return this;
        }

        public IconStyle withTextBackgroundColor(Color textBackgroundColor) {
            setTextBackgroundColor(textBackgroundColor);
            return this;
        }

        public IconStyle withTextBorderRadius(Integer textBorderRadius) {
            setTextBorderRadius(textBorderRadius);
            return this;
        }
    }

    public IconStyle getIconStyle() {
        return iconStyle;
    }

    public void setIconStyle(IconStyle iconStyle) {
        if (this.iconStyle != null) {
            removeChild(this.iconStyle);
        }

        this.iconStyle = iconStyle;
        addChild(iconStyle);
    }

    public Emphasis withIconStyle(IconStyle iconStyle) {
        setIconStyle(iconStyle);
        return this;
    }
}
