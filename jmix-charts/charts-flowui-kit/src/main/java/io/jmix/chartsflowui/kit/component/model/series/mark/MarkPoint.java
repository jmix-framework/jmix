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

package io.jmix.chartsflowui.kit.component.model.series.mark;

import io.jmix.chartsflowui.kit.component.model.shared.HasSymbols;
import io.jmix.chartsflowui.kit.component.model.shared.ItemStyle;

import java.util.ArrayList;
import java.util.List;

/**
 * Mark point in a chart. Used to mark any single values on the graph.
 * More detailed information is provided in the documentation.
 *
 * @see <a href="https://echarts.apache.org/en/option.html#series-line.markPoint">MarkPoint documentation</a>
 */
public class MarkPoint extends AbstractMark<MarkPoint>
        implements HasSymbols<MarkPoint> {

    protected HasSymbols.Symbol symbol;

    protected Integer symbolSize;

    protected Integer symbolRotate;

    protected Boolean symbolKeepAspect;

    protected String[] symbolOffset;

    protected ItemStyle itemStyle;

    protected Emphasis emphasis;

    protected Blur blur;

    protected List<Point> data;

    /**
     * Component to configure the emphasis state.
     */
    public static class Emphasis extends AbstractMarkElement<Emphasis> {

        protected Boolean disabled;

        public Boolean getDisabled() {
            return disabled;
        }

        public void setDisabled(Boolean disabled) {
            this.disabled = disabled;
            markAsDirty();
        }

        public Emphasis withDisabled(Boolean disabled) {
            setDisabled(disabled);
            return this;
        }
    }

    /**
     * Component to configure the blur state.
     */
    public static class Blur extends AbstractMarkElement<Blur> {
    }

    /**
     * Point to display.
     */
    public static class Point extends AbstractMarkElement<Point>
            implements HasSymbols<Point> {

        protected String name;

        protected PointDataType type;

        protected Integer valueIndex;

        protected String valueDim;

        protected Coordinate coordinate;

        protected String x;

        protected String y;

        protected HasSymbols.Symbol symbol;

        protected Integer symbolSize;

        protected Integer symbolRotate;

        protected Boolean symbolKeepAspect;

        protected String[] symbolOffset;

        protected Emphasis emphasis;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
            markAsDirty();
        }

        public PointDataType getType() {
            return type;
        }

        public void setType(PointDataType type) {
            this.type = type;
            markAsDirty();
        }

        public Integer getValueIndex() {
            return valueIndex;
        }

        public void setValueIndex(Integer valueIndex) {
            this.valueIndex = valueIndex;
            markAsDirty();
        }

        public String getValueDim() {
            return valueDim;
        }

        public void setValueDim(String valueDim) {
            this.valueDim = valueDim;
            markAsDirty();
        }

        public Coordinate getCoordinate() {
            return coordinate;
        }

        public void setCoordinate(Double... coordinate) {
            this.coordinate = new Coordinate(coordinate);
            markAsDirty();
        }

        public void setCoordinate(String... coordinate) {
            this.coordinate = new Coordinate(coordinate);
            markAsDirty();
        }

        public String getX() {
            return x;
        }

        public void setX(String x) {
            this.x = x;
            markAsDirty();
        }

        public String getY() {
            return y;
        }

        public void setY(String y) {
            this.y = y;
            markAsDirty();
        }

        @Override
        public Symbol getSymbol() {
            return symbol;
        }

        @Override
        public void setSymbol(String icon) {
            this.symbol = new Symbol(icon);
            markAsDirty();
        }

        @Override
        public void setSymbol(SymbolType symbolType) {
            this.symbol = new Symbol(symbolType);
            markAsDirty();
        }

        @Override
        public Integer getSymbolSize() {
            return symbolSize;
        }

        @Override
        public void setSymbolSize(Integer symbolSize) {
            this.symbolSize = symbolSize;
            markAsDirty();
        }

        @Override
        public Integer getSymbolRotate() {
            return symbolRotate;
        }

        @Override
        public void setSymbolRotate(Integer symbolRotate) {
            this.symbolRotate = symbolRotate;
            markAsDirty();
        }

        @Override
        public Boolean getSymbolKeepAspect() {
            return symbolKeepAspect;
        }

        @Override
        public void setSymbolKeepAspect(Boolean symbolKeepAspect) {
            this.symbolKeepAspect = symbolKeepAspect;
            markAsDirty();
        }

        @Override
        public String[] getSymbolOffset() {
            return symbolOffset;
        }

        @Override
        public void setSymbolOffset(String xOffset, String yOffset) {
            this.symbolOffset = new String[]{xOffset, yOffset};
            markAsDirty();
        }

        public Emphasis getEmphasis() {
            return emphasis;
        }

        public void setEmphasis(Emphasis emphasis) {
            if (this.emphasis != null) {
                removeChild(this.emphasis);
            }

            this.emphasis = emphasis;
            addChild(emphasis);
        }

        public Point withName(String name) {
            setName(name);
            return this;
        }

        public Point withType(PointDataType type) {
            setType(type);
            return this;
        }

        public Point withValueIndex(Integer valueIndex) {
            setValueIndex(valueIndex);
            return this;
        }

        public Point withValueDim(String valueDim) {
            setValueDim(valueDim);
            return this;
        }

        public Point withCoordinate(String... coordinate) {
            setCoordinate(coordinate);
            return this;
        }

        public Point withCoordinate(Double... coordinate) {
            setCoordinate(coordinate);
            return this;
        }

        public Point withX(String x) {
            setX(x);
            return this;
        }

        public Point withY(String y) {
            setY(y);
            return this;
        }

        public Point withEmphasis(Emphasis emphasis) {
            setEmphasis(emphasis);
            return this;
        }
    }

    @Override
    public HasSymbols.Symbol getSymbol() {
        return symbol;
    }

    @Override
    public void setSymbol(String icon) {
        this.symbol = new Symbol(icon);
        markAsDirty();
    }

    @Override
    public void setSymbol(SymbolType symbolType) {
        this.symbol = new Symbol(symbolType);
        markAsDirty();
    }

    @Override
    public Integer getSymbolSize() {
        return symbolSize;
    }

    @Override
    public void setSymbolSize(Integer symbolSize) {
        this.symbolSize = symbolSize;
        markAsDirty();
    }

    @Override
    public Integer getSymbolRotate() {
        return symbolRotate;
    }

    @Override
    public void setSymbolRotate(Integer symbolRotate) {
        this.symbolRotate = symbolRotate;
        markAsDirty();
    }

    @Override
    public Boolean getSymbolKeepAspect() {
        return symbolKeepAspect;
    }

    @Override
    public void setSymbolKeepAspect(Boolean symbolKeepAspect) {
        this.symbolKeepAspect = symbolKeepAspect;
        markAsDirty();
    }

    @Override
    public String[] getSymbolOffset() {
        return symbolOffset;
    }

    @Override
    public void setSymbolOffset(String xOffset, String yOffset) {
        this.symbolOffset = new String[]{xOffset, yOffset};
        markAsDirty();
    }

    public ItemStyle getItemStyle() {
        return itemStyle;
    }

    public void setItemStyle(ItemStyle itemStyle) {
        this.itemStyle = itemStyle;
        markAsDirty();
    }

    public Emphasis getEmphasis() {
        return emphasis;
    }

    public void setEmphasis(Emphasis emphasis) {
        this.emphasis = emphasis;
        markAsDirty();
    }

    public Blur getBlur() {
        return blur;
    }

    public void setBlur(Blur blur) {
        this.blur = blur;
        markAsDirty();
    }

    public List<Point> getData() {
        return data;
    }

    public void setData(List<Point> data) {
        if (this.data != null) {
            this.data.forEach(this::removeChild);
        }

        this.data = data;
        if (data != null) {
            data.forEach(this::addChild);
        }
    }

    public void setData(Point... data) {
        setData(data == null ? null : List.of(data));
    }

    public void removeData(Point point) {
        if (data != null && data.remove(point)) {
            removeChild(point);
        }
    }

    public void addData(Point point) {
        if (data == null) {
            data = new ArrayList<>();
        }

        if (data.contains(point)) {
            return;
        }

        if (point != null) {
            data.add(point);
            addChild(point);
        }
    }

    public MarkPoint withItemStyle(ItemStyle itemStyle) {
        setItemStyle(itemStyle);
        return this;
    }

    public MarkPoint withEmphasis(Emphasis emphasis) {
        setEmphasis(emphasis);
        return this;
    }

    public MarkPoint withBlur(Blur blur) {
        setBlur(blur);
        return this;
    }

    public MarkPoint withData(Point... data) {
        setData(data);
        return this;
    }

    public MarkPoint withData(Point point) {
        addData(point);
        return this;
    }
}
