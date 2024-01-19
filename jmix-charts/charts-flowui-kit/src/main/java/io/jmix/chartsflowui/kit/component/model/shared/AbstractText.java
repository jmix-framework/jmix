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

import io.jmix.chartsflowui.kit.component.model.*;

public abstract class AbstractText<T extends AbstractText<T>> extends ChartObservableObject
        implements HasText<T> {

    protected Color color;

    protected FontStyle fontStyle;

    protected String fontWeight;

    protected String fontFamily;

    protected Integer fontSize;

    protected Integer lineHeight;

    protected Integer width;

    protected Integer height;

    protected Color textBorderColor;

    protected Double textBorderWidth;

    protected String textBorderType;

    protected Integer textBorderDashOffset;

    protected Color textShadowColor;

    protected Integer textShadowBlur;

    protected Integer textShadowOffsetX;

    protected Integer textShadowOffsetY;

    protected Overflow overflow;

    protected String ellipsis;

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
        markAsDirty();
    }

    public FontStyle getFontStyle() {
        return fontStyle;
    }

    public void setFontStyle(FontStyle fontStyle) {
        this.fontStyle = fontStyle;
        markAsDirty();
    }

    public String getFontWeight() {
        return fontWeight;
    }

    public void setFontWeight(String fontWeight) {
        this.fontWeight = fontWeight;
        markAsDirty();
    }

    public String getFontFamily() {
        return fontFamily;
    }

    public void setFontFamily(String fontFamily) {
        this.fontFamily = fontFamily;
        markAsDirty();
    }

    public Integer getFontSize() {
        return fontSize;
    }

    public void setFontSize(Integer fontSize) {
        this.fontSize = fontSize;
        markAsDirty();
    }

    public Integer getLineHeight() {
        return lineHeight;
    }

    public void setLineHeight(Integer lineHeight) {
        this.lineHeight = lineHeight;
        markAsDirty();
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
        markAsDirty();
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
        markAsDirty();
    }

    @Override
    public Color getTextBorderColor() {
        return textBorderColor;
    }

    @Override
    public void setTextBorderColor(Color textBorderColor) {
        this.textBorderColor = textBorderColor;
        markAsDirty();
    }

    @Override
    public Double getTextBorderWidth() {
        return textBorderWidth;
    }

    @Override
    public void setTextBorderWidth(Double textBorderWidth) {
        this.textBorderWidth = textBorderWidth;
        markAsDirty();
    }

    @Override
    public String getTextBorderType() {
        return textBorderType;
    }

    @Override
    public void setTextBorderType(String textBorderType) {
        this.textBorderType = textBorderType;
        markAsDirty();
    }

    @Override
    public Integer getTextBorderDashOffset() {
        return textBorderDashOffset;
    }

    @Override
    public void setTextBorderDashOffset(Integer textBorderDashOffset) {
        this.textBorderDashOffset = textBorderDashOffset;
        markAsDirty();
    }

    @Override
    public Color getTextShadowColor() {
        return textShadowColor;
    }

    @Override
    public void setTextShadowColor(Color textShadowColor) {
        this.textShadowColor = textShadowColor;
        markAsDirty();
    }

    @Override
    public Integer getTextShadowBlur() {
        return textShadowBlur;
    }

    @Override
    public void setTextShadowBlur(Integer textShadowBlur) {
        this.textShadowBlur = textShadowBlur;
        markAsDirty();
    }

    @Override
    public Integer getTextShadowOffsetX() {
        return textShadowOffsetX;
    }

    @Override
    public void setTextShadowOffsetX(Integer textShadowOffsetX) {
        this.textShadowOffsetX = textShadowOffsetX;
        markAsDirty();
    }

    @Override
    public Integer getTextShadowOffsetY() {
        return textShadowOffsetY;
    }

    @Override
    public void setTextShadowOffsetY(Integer textShadowOffsetY) {
        this.textShadowOffsetY = textShadowOffsetY;
        markAsDirty();
    }

    public Overflow getOverflow() {
        return overflow;
    }

    public void setOverflow(Overflow overflow) {
        this.overflow = overflow;
        markAsDirty();
    }

    public String getEllipsis() {
        return ellipsis;
    }

    public void setEllipsis(String ellipsis) {
        this.ellipsis = ellipsis;
        markAsDirty();
    }

    @SuppressWarnings("unchecked")
    public T withColor(Color color) {
        setColor(color);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withFontStyle(FontStyle fontStyle) {
        setFontStyle(fontStyle);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withFontWeight(String fontWeight) {
        setFontWeight(fontWeight);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withFontFamily(String fontFamily) {
        setFontFamily(fontFamily);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withFontSize(Integer fontSize) {
        setFontSize(fontSize);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withLineHeight(Integer lineHeight) {
        setLineHeight(lineHeight);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withWidth(Integer width) {
        setWidth(width);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withHeight(Integer height) {
        setHeight(height);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withOverflow(Overflow overflow) {
        setOverflow(overflow);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withEllipsis(String ellipsis) {
        setEllipsis(ellipsis);
        return (T) this;
    }
}
