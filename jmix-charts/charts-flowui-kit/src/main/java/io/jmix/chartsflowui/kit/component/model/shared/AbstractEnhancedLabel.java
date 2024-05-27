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

import io.jmix.chartsflowui.kit.component.model.HasAlign;
import io.jmix.chartsflowui.kit.component.model.HasBorder;
import io.jmix.chartsflowui.kit.component.model.HasShadow;

/**
 * The base class for enhanced label.
 *
 * @param <T> origin enhanced label class type
 */
public abstract class AbstractEnhancedLabel<T extends AbstractEnhancedLabel<T>> extends AbstractRichText<T>
        implements HasShadow<T>, HasBorder<T>, HasAlign<T>, HasPadding<T> {

    protected Boolean show;

    protected Integer distance;

    protected Integer rotate;

    protected Integer[] offset;

    protected Align align;

    protected VerticalAlign verticalAlign;

    protected Color backgroundColor;

    protected Color borderColor;

    protected Integer borderWidth;

    protected String borderType;

    protected Integer borderDashOffset;

    protected Integer borderRadius;

    protected Padding padding;

    protected Color shadowColor;

    protected Integer shadowBlur;

    protected Integer shadowOffsetX;

    protected Integer shadowOffsetY;

    public Boolean getShow() {
        return show;
    }

    public void setShow(Boolean show) {
        this.show = show;
        markAsDirty();
    }

    public Integer getDistance() {
        return distance;
    }

    public void setDistance(Integer distance) {
        this.distance = distance;
        markAsDirty();
    }

    public Integer getRotate() {
        return rotate;
    }

    public void setRotate(Integer rotate) {
        this.rotate = rotate;
        markAsDirty();
    }

    public Integer[] getOffset() {
        return offset;
    }

    public void setOffset(Integer horizontal, Integer vertical) {
        this.offset = new Integer[]{horizontal, vertical};
        markAsDirty();
    }

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
    public Color getShadowColor() {
        return shadowColor;
    }

    @Override
    public void setShadowColor(Color shadowColor) {
        this.shadowColor = shadowColor;
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

    @SuppressWarnings("unchecked")
    public T withShow(Boolean show) {
        setShow(show);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withDistance(Integer distance) {
        setDistance(distance);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withRotate(Integer rotate) {
        setRotate(rotate);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withOffset(Integer horizontal, Integer vertical) {
        setOffset(horizontal, vertical);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withBackgroundColor(Color backgroundColor) {
        setBackgroundColor(backgroundColor);
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
}
