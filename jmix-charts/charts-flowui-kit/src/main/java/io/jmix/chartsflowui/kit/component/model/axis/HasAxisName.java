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

package io.jmix.chartsflowui.kit.component.model.axis;

import io.jmix.chartsflowui.kit.component.model.HasAlign;
import io.jmix.chartsflowui.kit.component.model.HasBorder;
import io.jmix.chartsflowui.kit.component.model.HasEnumId;
import io.jmix.chartsflowui.kit.component.model.HasShadow;
import io.jmix.chartsflowui.kit.component.model.shared.*;
import jakarta.annotation.Nullable;

public interface HasAxisName<T> {

    String getName();

    void setName(String name);

    NameLocation getNameLocation();

    void setNameLocation(NameLocation nameLocation);

    NameTextStyle getNameTextStyle();

    void setNameTextStyle(NameTextStyle nameTextStyle);

    Integer getNameGap();

    void setNameGap(Integer nameGap);

    Integer getNameRotate();

    void setNameRotate(Integer nameRotate);

    Boolean getInverse();

    void setInverse(Boolean inverse);

    @SuppressWarnings("unchecked")
    default T withName(String name) {
        setName(name);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    default T withNameLocation(NameLocation nameLocation) {
        setNameLocation(nameLocation);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    default T withNameTextStyle(NameTextStyle nameTextStyle) {
        setNameTextStyle(nameTextStyle);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    default T withNameGap(Integer nameGap) {
        setNameGap(nameGap);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    default T withNameRotate(Integer nameRotate) {
        setNameRotate(nameRotate);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    default T withInverse(Boolean inverse) {
        setInverse(inverse);
        return (T) this;
    }

    enum NameLocation implements HasEnumId {
        END("end"),
        CENTER("center"),
        START("start");

        private final String id;

        NameLocation(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }

        @Nullable
        public static NameLocation fromId(String id) {
            for (NameLocation at : NameLocation.values()) {
                if (at.getId().equals(id)) {
                    return at;
                }
            }
            return null;
        }
    }

    class NameTextStyle extends AbstractRichText<NameTextStyle>
            implements HasBorder<NameTextStyle>, HasShadow<NameTextStyle>,
            HasAlign<NameTextStyle>, HasPadding<NameTextStyle> {

        protected Align align;

        protected VerticalAlign verticalAlign;

        protected Color backgroundColor;

        protected Color borderColor;

        protected Integer borderWidth;

        protected String borderType;

        protected Integer borderDashOffset;

        protected Integer borderRadius;

        protected Padding padding;

        protected Integer shadowBlur;

        protected Color shadowColor;

        protected Integer shadowOffsetX;

        protected Integer shadowOffsetY;

        @Override
        public Align getAlign() {
            return align;
        }

        @Override
        public void setAlign(Align align) {
            this.align = align;
            markAsDirty();
        }

        @Override
        public VerticalAlign getVerticalAlign() {
            return verticalAlign;
        }

        @Override
        public void setVerticalAlign(VerticalAlign verticalAlign) {
            this.verticalAlign = verticalAlign;
            markAsDirty();
        }

        public Color getBackgroundColor() {
            return backgroundColor;
        }

        public void setBackgroundColor(Color backgroundColor) {
            this.backgroundColor = backgroundColor;
            markAsDirty();
        }

        @Override
        public Color getBorderColor() {
            return borderColor;
        }

        @Override
        public void setBorderColor(Color borderColor) {
            this.borderColor = borderColor;
            markAsDirty();
        }

        @Override
        public Integer getBorderWidth() {
            return borderWidth;
        }

        @Override
        public void setBorderWidth(Integer borderWidth) {
            this.borderWidth = borderWidth;
            markAsDirty();
        }

        public String getBorderType() {
            return borderType;
        }

        public void setBorderType(String borderType) {
            this.borderType = borderType;
            markAsDirty();
        }

        public Integer getBorderDashOffset() {
            return borderDashOffset;
        }

        public void setBorderDashOffset(Integer borderDashOffset) {
            this.borderDashOffset = borderDashOffset;
            markAsDirty();
        }

        @Override
        public Integer getBorderRadius() {
            return borderRadius;
        }

        @Override
        public void setBorderRadius(Integer borderRadius) {
            this.borderRadius = borderRadius;
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

        @Override
        public Integer getShadowBlur() {
            return shadowBlur;
        }

        @Override
        public void setShadowBlur(Integer shadowBlur) {
            this.shadowBlur = shadowBlur;
            markAsDirty();
        }

        @Override
        public Color getShadowColor() {
            return shadowColor;
        }

        @Override
        public void setShadowColor(Color shadowColor) {
            this.shadowColor = shadowColor;
            markAsDirty();
        }

        @Override
        public Integer getShadowOffsetX() {
            return shadowOffsetX;
        }

        @Override
        public void setShadowOffsetX(Integer shadowOffsetX) {
            this.shadowOffsetX = shadowOffsetX;
            markAsDirty();
        }

        @Override
        public Integer getShadowOffsetY() {
            return shadowOffsetY;
        }

        @Override
        public void setShadowOffsetY(Integer shadowOffsetY) {
            this.shadowOffsetY = shadowOffsetY;
            markAsDirty();
        }

        public NameTextStyle withBackgroundColor(Color backgroundColor) {
            setBackgroundColor(backgroundColor);
            return this;
        }

        public NameTextStyle withBorderType(String borderType) {
            setBorderType(borderType);
            return this;
        }

        public NameTextStyle withBorderDashOffset(Integer borderDashOffset) {
            setBorderDashOffset(borderDashOffset);
            return this;
        }
    }
}
