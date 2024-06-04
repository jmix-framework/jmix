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

/**
 * Component for defining visual channels that will be displayed based on data values that are in the selected range.
 */
public class VisualEffect extends ChartObservableObject {

    protected String[] symbol;

    protected Integer[] symbolSize;

    protected Color[] color;

    protected Double[] colorAlpha;

    protected Double[] opacity;

    protected Double[] colorLightness;

    protected Double[] colorSaturation;

    protected Double[] colorHue;

    public String[] getSymbol() {
        return symbol;
    }

    public void setSymbol(String... symbol) {
        this.symbol = symbol;
        markAsDirty();
    }

    public Integer[] getSymbolSize() {
        return symbolSize;
    }

    public void setSymbolSize(Integer minSymbolSize, Integer maxSymbolSize) {
        this.symbolSize = new Integer[]{minSymbolSize, maxSymbolSize};
        markAsDirty();
    }

    public Color[] getColor() {
        return color;
    }

    public void setColor(Color... color) {
        this.color = color;
        markAsDirty();
    }

    public Double[] getColorAlpha() {
        return colorAlpha;
    }

    public void setColorAlpha(Double minColorAlpha, Double maxColorAlpha) {
        this.colorAlpha = new Double[]{minColorAlpha, maxColorAlpha};
        markAsDirty();
    }

    public Double[] getOpacity() {
        return opacity;
    }

    public void setOpacity(Double minOpacity, Double maxOpacity) {
        this.opacity = new Double[]{minOpacity, maxOpacity};
        markAsDirty();
    }

    public Double[] getColorLightness() {
        return colorLightness;
    }

    public void setColorLightness(Double minColorLightness, Double maxColorLightness) {
        this.colorLightness = new Double[]{minColorLightness, maxColorLightness};
        markAsDirty();
    }

    public Double[] getColorSaturation() {
        return colorSaturation;
    }

    public void setColorSaturation(Double minColorSaturation, Double maxColorSaturation) {
        this.colorSaturation = new Double[]{minColorSaturation, maxColorSaturation};
        markAsDirty();
    }

    public Double[] getColorHue() {
        return colorHue;
    }

    public void setColorHue(Double minColorHue, Double maxColorHue) {
        this.colorHue = new Double[]{minColorHue, maxColorHue};
        markAsDirty();
    }

    public VisualEffect withSymbol(String... symbol) {
        setSymbol(symbol);
        return this;
    }

    public VisualEffect withSymbolSize(Integer minSymbolSize, Integer maxSymbolSize) {
        setSymbolSize(minSymbolSize, maxSymbolSize);
        return this;
    }

    public VisualEffect withColor(Color... color) {
        setColor(color);
        return this;
    }

    public VisualEffect withColorAlpha(Double minColorAlpha, Double maxColorAlpha) {
        setColorAlpha(minColorAlpha, maxColorAlpha);
        return this;
    }

    public VisualEffect withOpacity(Double minOpacity, Double maxOpacity) {
        setOpacity(minOpacity, maxOpacity);
        return this;
    }

    public VisualEffect withColorLightness(Double minColorLightness, Double maxColorLightness) {
        setColorLightness(minColorLightness, maxColorLightness);
        return this;
    }

    public VisualEffect withColorSaturation(Double minColorSaturation, Double maxColorSaturation) {
        setColorSaturation(minColorSaturation, maxColorSaturation);
        return this;
    }

    public VisualEffect withColorHue(Double minColorHue, Double maxColorHue) {
        setColorHue(minColorHue, maxColorHue);
        return this;
    }
}
