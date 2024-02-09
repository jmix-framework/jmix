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

import io.jmix.chartsflowui.kit.component.model.ChartObservableObject;
import io.jmix.chartsflowui.kit.component.model.series.Label;
import io.jmix.chartsflowui.kit.component.model.shared.HasSymbols;
import io.jmix.chartsflowui.kit.component.model.shared.JsFunction;
import io.jmix.chartsflowui.kit.component.model.shared.LineStyle;

import java.util.ArrayList;
import java.util.List;

/**
 * Mark line in a chart. Used to display borders, lines and so on.
 * More detailed information is provided in the documentation.
 *
 * @see <a href="https://echarts.apache.org/en/option.html#series-line.markLine">MarkLine documentation</a>
 */
public class MarkLine extends AbstractMark<MarkLine> {

    protected HasSymbols.Symbol[] symbol;

    protected Integer[] symbolSize;

    protected Integer precision;

    protected LineStyle lineStyle;

    protected Emphasis emphasis;

    protected Blur blur;

    protected Data data;

    /**
     * Component to configure the emphasis state.
     */
    public static class Emphasis extends AbstractMarkLineElement<Emphasis> {

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
    public static class Blur extends AbstractMarkLineElement<Blur> {
    }

    /**
     * Component to configure data for the mark line. The line can be configured with single point or two points.
     * <ul>
     *     <li>
     *         Single point example:
     *         <pre>{@code
     *              <charts:markLine>
     *                  <charts:data>
     *                      // Max line
     *                      <charts:singlePointLine type="MAX"/>
     *                   </charts:data>
     *              </charts:markLine>
     *         }</pre>
     *     </li>
     *     <li>
     *         Two points example:
     *         <pre>{@code
     *              <charts:markLine>
     *                  <charts:data>
     *                      // two points line
     *                      <charts:pairPointLine>
     *                          <charts:startPoint numberCoordinate="100, 200"/>
     *                          <charts:endPoint stringCoordinate="50%, 25%"/>
     *                      </charts:pairPointLine>
     *                  </charts:data>
     *              </charts:markLine>
     *         }</pre>
     *     </li>
     * </ul>
     */
    public static class Data extends ChartObservableObject {

        protected List<Point> singlePointLines;

        protected List<PointPair> pairPointLines;

        public List<Point> getSinglePointLines() {
            return singlePointLines;
        }

        public void setSinglePointLines(List<Point> singlePointLines) {
            if (this.singlePointLines != null) {
                this.singlePointLines.forEach(this::removeChild);
            }

            this.singlePointLines = singlePointLines;
            if (singlePointLines != null) {
                singlePointLines.forEach(this::addChild);
            }
        }

        public void setSinglePointLines(Point... singlePointLines) {
            setSinglePointLines(singlePointLines == null ? null : List.of(singlePointLines));
        }

        public void removeSinglePointLine(Point point) {
            if (singlePointLines != null && singlePointLines.remove(point)) {
                removeChild(point);
            }
        }

        public void addSinglePointLine(Point point) {
            if (singlePointLines == null) {
                singlePointLines = new ArrayList<>();
            }

            if (singlePointLines.contains(point)) {
                return;
            }

            if (point != null) {
                singlePointLines.add(point);
                addChild(point);
            }
        }

        public List<PointPair> getPairPointLines() {
            return pairPointLines;
        }

        public void setPairPointLines(List<PointPair> pairPointLines) {
            if (this.pairPointLines != null) {
                this.pairPointLines.forEach(pointPair -> {
                    removeChild(pointPair.getStartPoint());
                    removeChild(pointPair.getEndPoint());
                });
            }

            this.pairPointLines = pairPointLines;
            if (pairPointLines != null) {
                pairPointLines.forEach(pointPair -> {
                    addChild(pointPair.getStartPoint());
                    addChild(pointPair.getEndPoint());
                });
            }
        }

        public void setPairPointLines(PointPair... pairPointLines) {
            setPairPointLines(pairPointLines == null ? null : List.of(pairPointLines));
        }

        public void removePairPointLine(PointPair pointPair) {
            if (pairPointLines != null && pairPointLines.remove(pointPair)) {
                removeChild(pointPair.getStartPoint());
                removeChild(pointPair.getEndPoint());
            }
        }

        public void addPairPointLine(PointPair pointPair) {
            if (pairPointLines == null) {
                pairPointLines = new ArrayList<>();
            }

            if (pairPointLines.contains(pointPair)) {
                return;
            }

            if (pointPair != null) {
                pairPointLines.add(pointPair);
                addChild(pointPair.getStartPoint());
                addChild(pointPair.getEndPoint());
            }
        }

        public Data withSinglePointLines(Point... singlePointLine) {
            setSinglePointLines(singlePointLine);
            return this;
        }

        public Data withPairPointLines(PointPair... pairPointLine) {
            setPairPointLines(pairPointLine);
            return this;
        }
    }

    /**
     * Base class for mark line elements.
     *
     * @param <T> origin class type
     */
    public static abstract class AbstractMarkLineElement<T extends AbstractMarkLineElement<T>>
            extends ChartObservableObject {

        protected Label label;

        protected LineStyle lineStyle;

        public Label getLabel() {
            return label;
        }

        public void setLabel(Label label) {
            if (this.label != null) {
                removeChild(this.label);
            }

            this.label = label;
            addChild(label);
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

        @SuppressWarnings("unchecked")
        public T withLabel(Label label) {
            setLabel(label);
            return (T) this;
        }

        @SuppressWarnings("unchecked")
        public T withLineStyle(LineStyle lineStyle) {
            setLineStyle(lineStyle);
            return (T) this;
        }
    }

    /**
     * A class containing a pair of points, start and end.
     */
    public static class PointPair {

        protected Point startPoint;

        protected Point endPoint;

        public PointPair(Point startPoint, Point endPoint) {
            this.startPoint = startPoint;
            this.endPoint = endPoint;
        }

        public Point getStartPoint() {
            return startPoint;
        }

        public Point getEndPoint() {
            return endPoint;
        }
    }

    /**
     * Component to configure mark line point.
     */
    public static class Point extends AbstractMarkLineElement<Point>
            implements HasSymbols<Point> {

        protected LineDataType type;

        protected Integer valueIndex;

        protected String valueDim;

        protected Coordinate coordinate;

        protected String name;

        protected String x;

        protected String y;

        protected String xAxis;

        protected String yAxis;

        protected Double value;

        protected HasSymbols.Symbol symbol;

        protected Integer symbolSize;

        protected JsFunction symbolSizeFunction;

        protected Integer symbolRotate;

        protected Boolean symbolKeepAspect;

        protected String[] symbolOffset;

        protected Emphasis emphasis;

        protected Blur blur;

        public LineDataType getType() {
            return type;
        }

        public void setType(LineDataType type) {
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

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
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

        public String getXAxis() {
            return xAxis;
        }

        public void setXAxis(String xAxis) {
            this.xAxis = xAxis;
            markAsDirty();
        }

        public String getYAxis() {
            return yAxis;
        }

        public void setYAxis(String yAxis) {
            this.yAxis = yAxis;
            markAsDirty();
        }

        public Double getValue() {
            return value;
        }

        public void setValue(Double value) {
            this.value = value;
            markAsDirty();
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
        public JsFunction getSymbolSizeFunction() {
            return symbolSizeFunction;
        }

        @Override
        public void setSymbolSizeFunction(JsFunction symbolSizeFunction) {
            this.symbolSizeFunction = symbolSizeFunction;
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

        public Blur getBlur() {
            return blur;
        }

        public void setBlur(Blur blur) {
            if (this.blur != null) {
                removeChild(this.blur);
            }

            this.blur = blur;
            addChild(blur);
        }

        public Point withType(LineDataType type) {
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

        public Point withName(String name) {
            setName(name);
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

        public Point withXAxis(String xAxis) {
            setXAxis(xAxis);
            return this;
        }

        public Point withYAxis(String yAxis) {
            setYAxis(yAxis);
            return this;
        }

        public Point withValue(Double value) {
            setValue(value);
            return this;
        }

        public Point withEmphasis(Emphasis emphasis) {
            setEmphasis(emphasis);
            return this;
        }

        public Point withBlur(Blur blur) {
            setBlur(blur);
            return this;
        }
    }

    public HasSymbols.Symbol[] getSymbol() {
        return symbol;
    }

    public void setSymbol(HasSymbols.SymbolType startSymbol, HasSymbols.SymbolType endSymbol) {
        this.symbol = new HasSymbols.Symbol[]{
                new HasSymbols.Symbol(startSymbol),
                new HasSymbols.Symbol(endSymbol)
        };
        markAsDirty();
    }

    public void setSymbol(String startSymbolIcon, String endSymbolIcon) {
        this.symbol = new HasSymbols.Symbol[]{
                new HasSymbols.Symbol(startSymbolIcon),
                new HasSymbols.Symbol(endSymbolIcon)
        };
        markAsDirty();
    }

    public Integer[] getSymbolSize() {
        return symbolSize;
    }

    public void setSymbolSize(Integer startSymbolSize, Integer endSymbolSize) {
        this.symbolSize = new Integer[]{startSymbolSize, endSymbolSize};
        markAsDirty();
    }

    public Integer getPrecision() {
        return precision;
    }

    public void setPrecision(Integer precision) {
        this.precision = precision;
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

    public Blur getBlur() {
        return blur;
    }

    public void setBlur(Blur blur) {
        if (this.blur != null) {
            removeChild(this.blur);
        }

        this.blur = blur;
        addChild(this.blur);
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        if (this.data != null) {
            removeChild(this.data);
        }

        this.data = data;
        addChild(data);
    }

    public MarkLine withSymbol(HasSymbols.SymbolType startSymbol, HasSymbols.SymbolType endSymbol) {
        setSymbol(startSymbol, endSymbol);
        return this;
    }

    public MarkLine withSymbol(String startSymbolIcon, String endSymbolIcon) {
        setSymbol(startSymbolIcon, endSymbolIcon);
        return this;
    }

    public MarkLine withSymbolSize(Integer startSymbolSize, Integer endSymbolSize) {
        setSymbolSize(startSymbolSize, endSymbolSize);
        return this;
    }

    public MarkLine withPrecision(Integer precision) {
        setPrecision(precision);
        return this;
    }

    public MarkLine withLineStyle(LineStyle lineStyle) {
        setLineStyle(lineStyle);
        return this;
    }

    public MarkLine withEmphasis(Emphasis emphasis) {
        setEmphasis(emphasis);
        return this;
    }

    public MarkLine withBlur(Blur blur) {
        setBlur(blur);
        return this;
    }

    public MarkLine withData(Data data) {
        setData(data);
        return this;
    }
}
