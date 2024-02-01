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
import io.jmix.chartsflowui.kit.component.model.HasShadow;
import io.jmix.chartsflowui.kit.component.model.shared.*;

/**
 * Options component related to axis label.
 *
 * @see <a href="https://echarts.apache.org/en/option.html#xAxis.axisLabel">AxisLabel documentation</a>
 */
public class AxisLabel extends AbstractRichText<AxisLabel>
        implements HasAlign<AxisLabel>, HasShadow<AxisLabel>, HasPadding<AxisLabel>, HasBorder<AxisLabel> {

    protected Boolean show;

    protected Integer interval;

    protected JsFunction intervalFunction;

    protected Boolean inside;

    protected Integer rotate;

    protected Integer margin;

    protected String formatter;

    protected JsFunction formatterFunction;

    protected JsFunction colorFunction;

    protected Boolean showMinLabel;

    protected Boolean showMaxLabel;

    protected Boolean hideOverlap;

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

    public Integer getInterval() {
        return interval;
    }

    public void setInterval(Integer interval) {
        this.interval = interval;
        markAsDirty();
    }

    public JsFunction getIntervalFunction() {
        return intervalFunction;
    }

    public void setIntervalFunction(JsFunction intervalFunction) {
        this.intervalFunction = intervalFunction;
        markAsDirty();
    }

    public void setIntervalFunction(String intervalFunction) {
        this.intervalFunction = new JsFunction(intervalFunction);
        markAsDirty();
    }

    public Boolean getInside() {
        return inside;
    }

    public void setInside(Boolean inside) {
        this.inside = inside;
        markAsDirty();
    }

    public Integer getRotate() {
        return rotate;
    }

    public void setRotate(Integer rotate) {
        this.rotate = rotate;
        markAsDirty();
    }

    public Integer getMargin() {
        return margin;
    }

    public void setMargin(Integer margin) {
        this.margin = margin;
        markAsDirty();
    }

    public String getFormatter() {
        return formatter;
    }

    public void setFormatter(String formatter) {
        this.formatter = formatter;
        markAsDirty();
    }

    public JsFunction getFormatterFunction() {
        return formatterFunction;
    }

    public void setFormatterFunction(JsFunction formatterFunction) {
        this.formatterFunction = formatterFunction;
        markAsDirty();
    }

    public void setFormatterFunction(String formatterFunction) {
        this.formatterFunction = new JsFunction(formatterFunction);
        markAsDirty();
    }

    public JsFunction getColorFunction() {
        return colorFunction;
    }

    public void setColorFunction(JsFunction colorFunction) {
        this.colorFunction = colorFunction;
        markAsDirty();
    }

    public void setColorFunction(String colorFunction) {
        this.colorFunction = new JsFunction(colorFunction);
        markAsDirty();
    }

    public Boolean getShowMinLabel() {
        return showMinLabel;
    }

    public void setShowMinLabel(Boolean showMinLabel) {
        this.showMinLabel = showMinLabel;
        markAsDirty();
    }

    public Boolean getShowMaxLabel() {
        return showMaxLabel;
    }

    public void setShowMaxLabel(Boolean showMaxLabel) {
        this.showMaxLabel = showMaxLabel;
        markAsDirty();
    }

    public Boolean getHideOverlap() {
        return hideOverlap;
    }

    public void setHideOverlap(Boolean hideOverlap) {
        this.hideOverlap = hideOverlap;
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

    public AxisLabel withShow(Boolean show) {
        setShow(show);
        return this;
    }

    public AxisLabel withInterval(Integer interval) {
        setInterval(interval);
        return this;
    }

    public AxisLabel withIntervalFunction(JsFunction intervalFunction) {
        setIntervalFunction(intervalFunction);
        return this;
    }

    public AxisLabel withIntervalFunction(String intervalFunction) {
        setIntervalFunction(intervalFunction);
        return this;
    }

    public AxisLabel withInside(Boolean inside) {
        setInside(inside);
        return this;
    }

    public AxisLabel withRotate(Integer rotate) {
        setRotate(rotate);
        return this;
    }

    public AxisLabel withMargin(Integer margin) {
        setMargin(margin);
        return this;
    }

    public AxisLabel withFormatter(String formatter) {
        setFormatter(formatter);
        return this;
    }

    public AxisLabel withFormatterFunction(JsFunction formatterFunction) {
        setFormatterFunction(formatterFunction);
        return this;
    }

    public AxisLabel withFormatterFunction(String formatterFunctionCode) {
        setFormatterFunction(formatterFunctionCode);
        return this;
    }

    public AxisLabel withColorFunction(JsFunction colorFunction) {
        setColorFunction(colorFunction);
        return this;
    }

    public AxisLabel withColorFunction(String colorFunction) {
        setColorFunction(colorFunction);
        return this;
    }

    public AxisLabel withShowMinLabel(Boolean showMinLabel) {
        setShowMinLabel(showMinLabel);
        return this;
    }

    public AxisLabel withShowMaxLabel(Boolean showMaxLabel) {
        setShowMaxLabel(showMaxLabel);
        return this;
    }

    public AxisLabel withHideOverlap(Boolean hideOverlap) {
        setHideOverlap(hideOverlap);
        return this;
    }

    public AxisLabel withBackgroundColor(Color backgroundColor) {
        setBackgroundColor(backgroundColor);
        return this;
    }

    public AxisLabel withBorderType(String borderType) {
        setBorderType(borderType);
        return this;
    }

    public AxisLabel withBorderDashOffset(Integer borderDashOffset) {
        setBorderDashOffset(borderDashOffset);
        return this;
    }
}
