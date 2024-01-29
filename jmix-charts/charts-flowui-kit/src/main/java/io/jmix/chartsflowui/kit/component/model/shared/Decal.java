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
 * The style of the decal pattern.
 */
public class Decal extends ChartObservableObject {

    protected HasSymbols.Symbol symbol;

    protected Double symbolSize;

    protected Boolean symbolKeepAspect;

    protected Color color;

    protected Color backgroundColor;

    protected DashArray dashArrayX;

    protected DashArray dashArrayY;

    protected Double rotation;

    protected Integer maxTileWidth;

    protected Integer maxTileHeight;

    /**
     * DashArray controls the horizontal pattern.<br/>
     * Possible values:
     * <ul>
     *     <li>
     *         If it is of type {@link DashArray#number}, it means that the pattern and the blank space
     *         are of this value respectively. For example, {@code 5} means the pattern with width 5 is displayed first,
     *         then 5 pixels empty, then the pattern with width 5 is displayed.
     *     </li>
     *     <li>
     *         n the case of {@link DashArray#array} type, it means that the pattern and empty space are loops
     *         of an array of values. For example: {@code [5, 10, 2, 6]} means the pattern is 5 pixels wide,
     *         then 10 pixels empty, then the pattern is 2 pixels wide, then 6 pixels empty,
     *         then the pattern is 5 pixels wide.
     *     </li>
     *     <li>
     *         If of type {@link DashArray#twoDimensionalArray}, it means that each row is a loop with an array
     *         of values for the pattern and blank space. For example: {@code [10, [2, 5]]} means that the first
     *         line will be 10 pixels by 10 pixels and empty space, the second line will be 2 pixels by 2 pixels
     *         and empty space, and the third line will be 10 pixels by 10 pixels and empty space.
     *     </li>
     * </ul>
     * <p>
     * More detailed information is provided in the documentation.
     *
     * @see <a href="https://echarts.apache.org/en/option.html#legend.itemStyle.decal">Decal documentation</a>
     */
    public static class DashArray {

        protected Integer number;

        protected Integer[] array;

        protected Integer[][] twoDimensionalArray;

        public DashArray(Integer number) {
            this.number = number;
        }

        public DashArray(Integer... array) {
            this.array = array;
        }

        public DashArray(Integer[][] twoDimensionalArray) {
            this.twoDimensionalArray = twoDimensionalArray;
        }

        public Integer getNumber() {
            return number;
        }

        public Integer[] getArray() {
            return array;
        }

        public Integer[][] getTwoDimensionalArray() {
            return twoDimensionalArray;
        }
    }

    public HasSymbols.Symbol getSymbol() {
        return symbol;
    }

    public void setSymbol(String icon) {
        this.symbol = new HasSymbols.Symbol(icon);
        markAsDirty();
    }

    public void setSymbol(HasSymbols.SymbolType symbolType) {
        this.symbol = new HasSymbols.Symbol(symbolType);
        markAsDirty();
    }

    public Double getSymbolSize() {
        return symbolSize;
    }

    public void setSymbolSize(Double symbolSize) {
        this.symbolSize = symbolSize;
        markAsDirty();
    }

    public Boolean getSymbolKeepAspect() {
        return symbolKeepAspect;
    }

    public void setSymbolKeepAspect(Boolean symbolKeepAspect) {
        this.symbolKeepAspect = symbolKeepAspect;
        markAsDirty();
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
        markAsDirty();
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        markAsDirty();
    }

    public DashArray getDashArrayX() {
        return dashArrayX;
    }

    public void setDashArrayX(Integer number) {
        this.dashArrayX = new DashArray(number);
        markAsDirty();
    }

    public void setDashArrayX(Integer... array) {
        this.dashArrayX = new DashArray(array);
        markAsDirty();
    }

    public void setDashArrayX(Integer[][] twoDimensionalArray) {
        this.dashArrayX = new DashArray(twoDimensionalArray);
        markAsDirty();
    }

    public DashArray getDashArrayY() {
        return dashArrayY;
    }

    public void setDashArrayY(Integer number) {
        this.dashArrayY = new DashArray(number);
        markAsDirty();
    }

    public void setDashArrayY(Integer... array) {
        this.dashArrayY = new DashArray(array);
        markAsDirty();
    }

    public void setDashArrayY(Integer[][] twoDimensionalArray) {
        this.dashArrayY = new DashArray(twoDimensionalArray);
        markAsDirty();
    }

    public Double getRotation() {
        return rotation;
    }

    public void setRotation(Double rotation) {
        this.rotation = rotation;
        markAsDirty();
    }

    public Integer getMaxTileWidth() {
        return maxTileWidth;
    }

    public void setMaxTileWidth(Integer maxTileWidth) {
        this.maxTileWidth = maxTileWidth;
        markAsDirty();
    }

    public Integer getMaxTileHeight() {
        return maxTileHeight;
    }

    public void setMaxTileHeight(Integer maxTileHeight) {
        this.maxTileHeight = maxTileHeight;
        markAsDirty();
    }

    public Decal withSymbol(String icon) {
        setSymbol(icon);
        return this;
    }

    public Decal withSymbol(HasSymbols.SymbolType symbolType) {
        setSymbol(symbolType);
        return this;
    }

    public Decal withSymbolSize(Double symbolSize) {
        setSymbolSize(symbolSize);
        return this;
    }

    public Decal withSymbolKeepAspect(Boolean symbolKeepAspect) {
        setSymbolKeepAspect(symbolKeepAspect);
        return this;
    }

    public Decal withColor(Color color) {
        setColor(color);
        return this;
    }

    public Decal withBackgroundColor(Color backgroundColor) {
        setBackgroundColor(backgroundColor);
        return this;
    }

    public Decal withDashArrayX(Integer number) {
        setDashArrayX(number);
        return this;
    }

    public Decal withDashArrayX(Integer... array) {
        setDashArrayX(array);
        return this;
    }

    public Decal withDashArrayX(Integer[][] twoDimensionalArray) {
        setDashArrayX(twoDimensionalArray);
        return this;
    }

    public Decal withDashArrayY(Integer number) {
        setDashArrayY(number);
        return this;
    }

    public Decal withDashArrayY(Integer... array) {
        setDashArrayY(array);
        return this;
    }

    public Decal withDashArrayY(Integer[][] twoDimensionalArray) {
        setDashArrayY(twoDimensionalArray);
        return this;
    }

    public Decal withRotation(Double rotation) {
        setRotation(rotation);
        return this;
    }

    public Decal withMaxTileWidth(Integer maxTileWidth) {
        setMaxTileWidth(maxTileWidth);
        return this;
    }

    public Decal withMaxTileHeight(Integer maxTileHeight) {
        setMaxTileHeight(maxTileHeight);
        return this;
    }
}
