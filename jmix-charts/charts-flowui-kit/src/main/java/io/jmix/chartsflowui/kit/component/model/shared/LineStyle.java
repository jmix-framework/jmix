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

public class LineStyle extends ChartObservableObject
        implements HasShadow<LineStyle>, HasLineStyle<LineStyle> {

    protected Color color;

    protected Integer width;

    protected String type;

    protected Integer dashOffset;

    protected HasLineStyle.Cap cap;

    protected HasLineStyle.Join join;

    protected Integer miterLimit;

    protected Integer shadowBlur;

    protected Color shadowColor;

    protected Integer shadowOffsetX;

    protected Integer shadowOffsetY;

    protected Double opacity;

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
        markAsDirty();
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
        markAsDirty();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
        markAsDirty();
    }

    public Integer getDashOffset() {
        return dashOffset;
    }

    public void setDashOffset(Integer dashOffset) {
        this.dashOffset = dashOffset;
        markAsDirty();
    }

    @Override
    public Cap getCap() {
        return cap;
    }

    @Override
    public void setCap(Cap cap) {
        this.cap = cap;
        markAsDirty();
    }

    @Override
    public Join getJoin() {
        return join;
    }

    @Override
    public void setJoin(Join join) {
        this.join = join;
        markAsDirty();
    }

    @Override
    public Integer getMiterLimit() {
        return miterLimit;
    }

    @Override
    public void setMiterLimit(Integer miterLimit) {
        this.miterLimit = miterLimit;
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

    public Double getOpacity() {
        return opacity;
    }

    public void setOpacity(Double opacity) {
        this.opacity = opacity;
        markAsDirty();
    }

    public LineStyle withColor(Color color) {
        setColor(color);
        return this;
    }

    public LineStyle withWidth(Integer width) {
        setWidth(width);
        return this;
    }

    public LineStyle withType(String type) {
        setType(type);
        return this;
    }

    public LineStyle withDashOffset(Integer dashOffset) {
        setDashOffset(dashOffset);
        return this;
    }

    public LineStyle withOpacity(Double opacity) {
        setOpacity(opacity);
        return this;
    }
}