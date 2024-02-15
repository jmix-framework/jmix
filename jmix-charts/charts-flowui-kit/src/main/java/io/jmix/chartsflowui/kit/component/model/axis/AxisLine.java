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

import io.jmix.chartsflowui.kit.component.model.ChartObservableObject;
import io.jmix.chartsflowui.kit.component.model.shared.LineStyle;

/**
 * Options component related to axis line.
 *
 * @see <a href="https://echarts.apache.org/en/option.html#xAxis.axisLine">AxisLine documentation</a>
 */
public class AxisLine extends ChartObservableObject {

    protected Boolean show;

    protected Boolean onZero;

    protected Integer onZeroAxisIndex;

    protected String[] symbols;

    protected Integer[] symbolsSize;

    protected Integer[] symbolsOffset;

    protected LineStyle lineStyle;

    public Boolean getShow() {
        return show;
    }

    public void setShow(Boolean show) {
        this.show = show;
        markAsDirty();
    }

    public Boolean getOnZero() {
        return onZero;
    }

    public void setOnZero(Boolean onZero) {
        this.onZero = onZero;
        markAsDirty();
    }

    public Integer getOnZeroAxisIndex() {
        return onZeroAxisIndex;
    }

    public void setOnZeroAxisIndex(Integer onZeroAxisIndex) {
        this.onZeroAxisIndex = onZeroAxisIndex;
        markAsDirty();
    }

    public String[] getSymbols() {
        return symbols;
    }

    public void setSymbols(String startSymbol, String endSymbol) {
        this.symbols = new String[]{startSymbol, endSymbol};
        markAsDirty();
    }

    public Integer[] getSymbolsSize() {
        return symbolsSize;
    }

    public void setSymbolsSize(Integer startSymbolSize, Integer endSymbolSize) {
        this.symbolsSize = new Integer[]{startSymbolSize, endSymbolSize};
        markAsDirty();
    }

    public Integer[] getSymbolsOffset() {
        return symbolsOffset;
    }

    public void setSymbolsOffset(Integer startSymbolOffset, Integer endSymbolOffset) {
        this.symbolsOffset = new Integer[]{startSymbolOffset, endSymbolOffset};
        markAsDirty();
    }

    public LineStyle getLineStyle() {
        return lineStyle;
    }

    public void setLineStyle(LineStyle lineStyle) {
        if (this.lineStyle != null) {
            removeChild(this.lineStyle);
        }

        this.lineStyle = lineStyle;
        addChild(lineStyle);
    }

    public AxisLine withShow(Boolean show) {
        setShow(show);
        return this;
    }

    public AxisLine withOnZero(Boolean onZero) {
        setOnZero(onZero);
        return this;
    }

    public AxisLine withOnZeroAxisIndex(Integer onZeroAxisIndex) {
        setOnZeroAxisIndex(onZeroAxisIndex);
        return this;
    }

    public AxisLine withSymbols(String startSymbol, String endSymbol) {
        setSymbols(startSymbol, endSymbol);
        return this;
    }

    public AxisLine withSymbolsSize(Integer startSymbolSize, Integer endSymbolSize) {
        setSymbolsSize(startSymbolSize, endSymbolSize);
        return this;
    }

    public AxisLine withSymbolsOffset(Integer startSymbolOffset, Integer endSymbolOffset) {
        setSymbolsOffset(startSymbolOffset, endSymbolOffset);
        return this;
    }

    public AxisLine withLineStyle(LineStyle lineStyle) {
        setLineStyle(lineStyle);
        return this;
    }
}
