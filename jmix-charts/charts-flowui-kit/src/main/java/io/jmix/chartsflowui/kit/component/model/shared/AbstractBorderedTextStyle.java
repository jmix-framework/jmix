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

package io.jmix.chartsflowui.kit.component.model.shared;

import io.jmix.chartsflowui.kit.component.model.ChartObservableObject;
import io.jmix.chartsflowui.kit.component.model.HasLineStyle;
import io.jmix.chartsflowui.kit.component.model.HasShadow;
import org.jspecify.annotations.Nullable;

/**
 * The base class for text with border.
 *
 * @param <T> origin bordered text style class type
 */
public abstract class AbstractBorderedTextStyle<T extends AbstractBorderedTextStyle<T>> extends ChartObservableObject
        implements HasShadow<T>, HasLineStyle<T> {

    protected Color color;

    protected Color borderColor;

    protected Integer borderWidth;

    protected String borderType;

    protected Integer borderDashOffset;

    protected Cap borderCap;

    protected Join borderJoin;

    protected Integer borderMiterLimit;

    protected Integer shadowBlur;

    protected Color shadowColor;

    protected Integer shadowOffsetX;

    protected Integer shadowOffsetY;

    protected Double opacity;

    @Nullable
    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
        markAsDirty();
    }

    @Nullable
    public Color getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
        markAsDirty();
    }

    @Nullable
    public Integer getBorderWidth() {
        return borderWidth;
    }

    public void setBorderWidth(Integer borderWidth) {
        this.borderWidth = borderWidth;
        markAsDirty();
    }

    @Nullable
    public String getBorderType() {
        return borderType;
    }

    public void setBorderType(String borderType) {
        this.borderType = borderType;
        markAsDirty();
    }

    @Nullable
    public Integer getBorderDashOffset() {
        return borderDashOffset;
    }

    public void setBorderDashOffset(Integer borderDashOffset) {
        this.borderDashOffset = borderDashOffset;
        markAsDirty();
    }

    @Nullable
    @Override
    public Cap getCap() {
        return borderCap;
    }

    @Override
    public void setCap(Cap borderCap) {
        this.borderCap = borderCap;
        markAsDirty();
    }

    @Nullable
    @Override
    public Join getJoin() {
        return borderJoin;
    }

    @Override
    public void setJoin(Join borderJoin) {
        this.borderJoin = borderJoin;
        markAsDirty();
    }

    @Nullable
    @Override
    public Integer getMiterLimit() {
        return borderMiterLimit;
    }

    @Override
    public void setMiterLimit(Integer borderMiterLimit) {
        this.borderMiterLimit = borderMiterLimit;
        markAsDirty();
    }

    @Nullable
    @Override
    public Integer getShadowBlur() {
        return shadowBlur;
    }

    @Override
    public void setShadowBlur(Integer shadowBlur) {
        this.shadowBlur = shadowBlur;
        markAsDirty();
    }

    @Nullable
    @Override
    public Color getShadowColor() {
        return shadowColor;
    }

    @Override
    public void setShadowColor(Color shadowColor) {
        this.shadowColor = shadowColor;
        markAsDirty();
    }

    @Nullable
    @Override
    public Integer getShadowOffsetX() {
        return shadowOffsetX;
    }

    @Override
    public void setShadowOffsetX(Integer shadowOffsetX) {
        this.shadowOffsetX = shadowOffsetX;
        markAsDirty();
    }

    @Nullable
    @Override
    public Integer getShadowOffsetY() {
        return shadowOffsetY;
    }

    @Override
    public void setShadowOffsetY(Integer shadowOffsetY) {
        this.shadowOffsetY = shadowOffsetY;
        markAsDirty();
    }

    @Nullable
    public Double getOpacity() {
        return opacity;
    }

    public void setOpacity(Double opacity) {
        this.opacity = opacity;
        markAsDirty();
    }

    @SuppressWarnings("unchecked")
    public T withColor(Color color) {
        setColor(color);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withBorderColor(Color borderColor) {
        setBorderColor(borderColor);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withBorderWidth(Integer borderWidth) {
        setBorderWidth(borderWidth);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withBorderType(String borderType) {
        setBorderType(borderType);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withBorderDashOffset(Integer borderDashOffset) {
        setBorderDashOffset(borderDashOffset);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withOpacity(Double opacity) {
        setOpacity(opacity);
        return (T) this;
    }
}
