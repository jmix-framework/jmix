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


package io.jmix.chartsflowui.kit.component.model;

import io.jmix.chartsflowui.kit.component.model.shared.Color;
import io.jmix.chartsflowui.kit.component.model.shared.InnerTooltip;

public class Grid extends ChartObservableObject
        implements HasPosition<Grid>, HasShadow<Grid> {

    protected String id;

    protected Boolean show;

    protected Integer zLevel;

    protected Integer z;

    protected String left;

    protected String top;

    protected String right;

    protected String bottom;

    protected String width;

    protected String height;

    protected Boolean containLabel;

    protected Color backgroundColor;

    protected Color borderColor;

    protected Integer borderWidth;

    protected Integer shadowBlur;

    protected Color shadowColor;

    protected Integer shadowOffsetX;

    protected Integer shadowOffsetY;

    protected InnerTooltip tooltip;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
        markAsDirty();
    }

    public Boolean getShow() {
        return show;
    }

    public void setShow(Boolean show) {
        this.show = show;
        markAsDirty();
    }

    public Integer getZLevel() {
        return zLevel;
    }

    public void setZLevel(Integer zLevel) {
        this.zLevel = zLevel;
        markAsDirty();
    }

    public Integer getZ() {
        return z;
    }

    public void setZ(Integer z) {
        this.z = z;
        markAsDirty();
    }

    @Override
    public String getLeft() {
        return left;
    }

    @Override
    public void setLeft(String left) {
        this.left = left;
        markAsDirty();
    }

    @Override
    public String getTop() {
        return top;
    }

    @Override
    public void setTop(String top) {
        this.top = top;
        markAsDirty();
    }

    @Override
    public String getRight() {
        return right;
    }

    @Override
    public void setRight(String right) {
        this.right = right;
        markAsDirty();
    }

    @Override
    public String getBottom() {
        return bottom;
    }

    @Override
    public void setBottom(String bottom) {
        this.bottom = bottom;
        markAsDirty();
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
        markAsDirty();
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
        markAsDirty();
    }

    public Boolean getContainLabel() {
        return containLabel;
    }

    public void setContainLabel(Boolean containLabel) {
        this.containLabel = containLabel;
        markAsDirty();
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        markAsDirty();
    }

    public Color getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
        markAsDirty();
    }

    public Integer getBorderWidth() {
        return borderWidth;
    }

    public void setBorderWidth(Integer borderWidth) {
        this.borderWidth = borderWidth;
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

    public InnerTooltip getTooltip() {
        return tooltip;
    }

    public void setTooltip(InnerTooltip tooltip) {
        if (this.tooltip != null) {
            removeChild(this.tooltip);
        }

        this.tooltip = tooltip;
        addChild(tooltip);
    }

    public Grid withId(String id) {
        setId(id);
        return this;
    }

    public Grid withShow(Boolean show) {
        setShow(show);
        return this;
    }

    public Grid withZLevel(Integer zLevel) {
        setZLevel(zLevel);
        return this;
    }

    public Grid withZ(Integer z) {
        setZ(z);
        return this;
    }

    public Grid withWidth(String width) {
        setWidth(width);
        return this;
    }

    public Grid withHeight(String height) {
        setHeight(height);
        return this;
    }

    public Grid withContainLabel(Boolean containLabel) {
        setContainLabel(containLabel);
        return this;
    }

    public Grid withBackgroundColor(Color backgroundColor) {
        setBackgroundColor(backgroundColor);
        return this;
    }

    public Grid withBorderColor(Color borderColor) {
        setBorderColor(borderColor);
        return this;
    }

    public Grid withBorderWidth(Integer borderWidth) {
        setBorderWidth(borderWidth);
        return this;
    }

    public Grid withTooltip(InnerTooltip tooltip) {
        setTooltip(tooltip);
        return this;
    }
}
