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

import io.jmix.chartsflowui.kit.component.model.HasShadow;

/**
 * General label for components.
 */
public class Label extends AbstractText<Label>
        implements HasShadow<Label>, HasPadding<Label> {

    protected Boolean show;

    protected Integer precision;

    protected String formatter;

    protected JsFunction formatterFunction;

    protected Integer margin;

    protected Padding padding;

    protected Color backgroundColor;

    protected Color borderColor;

    protected Integer borderWidth;

    protected Integer shadowBlur;

    protected Color shadowColor;

    protected Integer shadowOffsetX;

    protected Integer shadowOffsetY;

    public Boolean getShow() {
        return show;
    }

    public void setShow(Boolean show) {
        this.show = show;
        markAsDirty();
    }

    public Integer getPrecision() {
        return precision;
    }

    public void setPrecision(Integer precision) {
        this.precision = precision;
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

    public Integer getMargin() {
        return margin;
    }

    public void setMargin(Integer margin) {
        this.margin = margin;
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

    public Label withShow(Boolean show) {
        setShow(show);
        return this;
    }

    public Label withPrecision(Integer precision) {
        setPrecision(precision);
        return this;
    }

    public Label withFormatter(String formatter) {
        setFormatter(formatter);
        return this;
    }

    public Label withFormatterFunction(JsFunction formatterFunction) {
        setFormatterFunction(formatterFunction);
        return this;
    }

    public Label withFormatterFunction(String formatterFunctionCode) {
        setFormatterFunction(formatterFunctionCode);
        return this;
    }

    public Label withMargin(Integer margin) {
        setMargin(margin);
        return this;
    }

    public Label withBackgroundColor(Color backgroundColor) {
        setBackgroundColor(backgroundColor);
        return this;
    }

    public Label withBorderColor(Color borderColor) {
        setBorderColor(borderColor);
        return this;
    }

    public Label withBorderWidth(Integer borderWidth) {
        setBorderWidth(borderWidth);
        return this;
    }
}
