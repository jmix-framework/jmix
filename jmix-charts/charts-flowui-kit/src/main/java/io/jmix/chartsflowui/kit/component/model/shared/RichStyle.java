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
import org.jspecify.annotations.Nullable;

/**
 * Common rich text styles. Can be used in labels of series, axis or other components.
 * More detailed information is provided in the documentation.
 *
 * @see <a href="https://echarts.apache.org/en/tutorial.html#Rich%20Text">RichText documentation </a>
 */
public class RichStyle extends ChartObservableObject
        implements HasShadow<RichStyle>, HasBorder<RichStyle>, HasText<RichStyle>,
        HasPadding<RichStyle>, HasAlign<RichStyle> {

    protected Color color;

    protected FontStyle fontStyle;

    protected String fontWeight;

    protected String fontFamily;

    protected Integer fontSize;

    protected Align align;

    protected VerticalAlign verticalAlign;

    protected Integer lineHeight;

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

    @Nullable
    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
        markAsDirty();
    }

    @Nullable
    public FontStyle getFontStyle() {
        return fontStyle;
    }

    public void setFontStyle(FontStyle fontStyle) {
        this.fontStyle = fontStyle;
        markAsDirty();
    }

    @Nullable
    public String getFontWeight() {
        return fontWeight;
    }

    public void setFontWeight(String fontWeight) {
        this.fontWeight = fontWeight;
        markAsDirty();
    }

    @Nullable
    public String getFontFamily() {
        return fontFamily;
    }

    public void setFontFamily(String fontFamily) {
        this.fontFamily = fontFamily;
        markAsDirty();
    }

    @Nullable
    public Integer getFontSize() {
        return fontSize;
    }

    public void setFontSize(Integer fontSize) {
        this.fontSize = fontSize;
        markAsDirty();
    }

    @Nullable
    @Override
    public Align getAlign() {
        return align;
    }

    @Override
    public void setAlign(Align align) {
        this.align = align;
        markAsDirty();
    }

    @Nullable
    @Override
    public VerticalAlign getVerticalAlign() {
        return verticalAlign;
    }

    @Override
    public void setVerticalAlign(VerticalAlign verticalAlign) {
        this.verticalAlign = verticalAlign;
        markAsDirty();
    }

    @Nullable
    public Integer getLineHeight() {
        return lineHeight;
    }

    public void setLineHeight(Integer lineHeight) {
        this.lineHeight = lineHeight;
        markAsDirty();
    }

    @Nullable
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        markAsDirty();
    }

    @Nullable
    @Override
    public Color getBorderColor() {
        return borderColor;
    }

    @Override
    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
        markAsDirty();
    }

    @Nullable
    @Override
    public Integer getBorderWidth() {
        return borderWidth;
    }

    @Override
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
    public Integer getBorderRadius() {
        return borderRadius;
    }

    @Override
    public void setBorderRadius(Integer borderRadius) {
        this.borderRadius = borderRadius;
        markAsDirty();
    }

    @Nullable
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
    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
        markAsDirty();
    }

    @Nullable
    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
        markAsDirty();
    }

    @Nullable
    @Override
    public Color getTextBorderColor() {
        return textBorderColor;
    }

    @Override
    public void setTextBorderColor(Color textBorderColor) {
        this.textBorderColor = textBorderColor;
        markAsDirty();
    }

    @Nullable
    @Override
    public Double getTextBorderWidth() {
        return textBorderWidth;
    }

    @Override
    public void setTextBorderWidth(Double textBorderWidth) {
        this.textBorderWidth = textBorderWidth;
        markAsDirty();
    }

    @Nullable
    @Override
    public String getTextBorderType() {
        return textBorderType;
    }

    @Override
    public void setTextBorderType(String textBorderType) {
        this.textBorderType = textBorderType;
        markAsDirty();
    }

    @Nullable
    @Override
    public Integer getTextBorderDashOffset() {
        return textBorderDashOffset;
    }

    @Override
    public void setTextBorderDashOffset(Integer textBorderDashOffset) {
        this.textBorderDashOffset = textBorderDashOffset;
        markAsDirty();
    }

    @Nullable
    @Override
    public Color getTextShadowColor() {
        return textShadowColor;
    }

    @Override
    public void setTextShadowColor(Color textShadowColor) {
        this.textShadowColor = textShadowColor;
        markAsDirty();
    }

    @Nullable
    @Override
    public Integer getTextShadowBlur() {
        return textShadowBlur;
    }

    @Override
    public void setTextShadowBlur(Integer textShadowBlur) {
        this.textShadowBlur = textShadowBlur;
        markAsDirty();
    }

    @Nullable
    @Override
    public Integer getTextShadowOffsetX() {
        return textShadowOffsetX;
    }

    @Override
    public void setTextShadowOffsetX(Integer textShadowOffsetX) {
        this.textShadowOffsetX = textShadowOffsetX;
        markAsDirty();
    }

    @Nullable
    @Override
    public Integer getTextShadowOffsetY() {
        return textShadowOffsetY;
    }

    @Override
    public void setTextShadowOffsetY(Integer textShadowOffsetY) {
        this.textShadowOffsetY = textShadowOffsetY;
        markAsDirty();
    }

    public RichStyle withColor(Color color) {
        setColor(color);
        return this;
    }

    public RichStyle withFontStyle(FontStyle fontStyle) {
        setFontStyle(fontStyle);
        return this;
    }

    public RichStyle withFontWeight(String fontWeight) {
        setFontWeight(fontWeight);
        return this;
    }

    public RichStyle withFontFamily(String fontFamily) {
        setFontFamily(fontFamily);
        return this;
    }

    public RichStyle withFontSize(Integer fontSize) {
        setFontSize(fontSize);
        return this;
    }

    public RichStyle withLineHeight(Integer lineHeight) {
        setLineHeight(lineHeight);
        return this;
    }

    public RichStyle withBackgroundColor(Color backgroundColor) {
        setBackgroundColor(backgroundColor);
        return this;
    }

    public RichStyle withBorderType(String borderType) {
        setBorderType(borderType);
        return this;
    }

    public RichStyle withBorderDashOffset(Integer borderDashOffset) {
        setBorderDashOffset(borderDashOffset);
        return this;
    }

    public RichStyle withWidth(Integer width) {
        setWidth(width);
        return this;
    }

    public RichStyle withHeight(Integer height) {
        setHeight(height);
        return this;
    }
}
