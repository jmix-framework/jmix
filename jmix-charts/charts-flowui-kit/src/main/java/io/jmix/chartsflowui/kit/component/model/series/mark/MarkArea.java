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

import io.jmix.chartsflowui.kit.component.model.shared.ItemStyle;

import java.util.ArrayList;
import java.util.List;

/**
 * Component that used to mark an area in chart. For example, mark a time interval.
 * More detailed information is provided in the documentation.
 *
 * @see <a href="https://echarts.apache.org/en/option.html#series-line.markArea">MarkArea documentation</a>
 */
public class MarkArea extends AbstractMark<MarkArea> {

    protected ItemStyle itemStyle;

    protected Emphasis emphasis;

    protected Blur blur;

    protected List<PointPair> data;

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
     * A class containing a pair of points, left top point and right bottom point of area.
     */
    public static class PointPair {

        protected MarkArea.Point leftTopPoint;

        protected MarkArea.Point rightBottomPoint;

        public PointPair(Point leftTopPoint, Point rightBottomPoint) {
            this.leftTopPoint = leftTopPoint;
            this.rightBottomPoint = rightBottomPoint;
        }

        public Point getLeftTopPoint() {
            return leftTopPoint;
        }

        public Point getRightBottomPoint() {
            return rightBottomPoint;
        }
    }

    /**
     * Component to configure mark area point.
     */
    public static class Point extends AbstractMarkElement<Point> {

        protected PointDataType type;

        protected Integer valueIndex;

        protected String valueDim;

        protected Coordinate coordinate;

        protected String name;

        protected String x;

        protected String y;

        protected Double value;

        protected Emphasis emphasis;

        protected Blur blur;

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

        public Double getValue() {
            return value;
        }

        public void setValue(Double value) {
            this.value = value;
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

    public ItemStyle getItemStyle() {
        return itemStyle;
    }

    public void setItemStyle(ItemStyle itemStyle) {
        if (this.itemStyle != null) {
            removeChild(this.itemStyle);
        }

        this.itemStyle = itemStyle;
        addChild(itemStyle);
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

    public List<PointPair> getData() {
        return data;
    }

    public void setData(List<PointPair> data) {
        if (this.data != null) {
            this.data.forEach(pointPair -> {
                removeChild(pointPair.getLeftTopPoint());
                removeChild(pointPair.getRightBottomPoint());
            });
        }

        this.data = data;
        if (data != null) {
            data.forEach(pointPair -> {
                addChild(pointPair.getLeftTopPoint());
                addChild(pointPair.getRightBottomPoint());
            });
        }
    }

    public void setData(PointPair... data) {
        setData(data == null ? null : List.of(data));
    }

    public void removePointPair(PointPair pointPair) {
        if (data != null && data.remove(pointPair)) {
            removeChild(pointPair.getLeftTopPoint());
            removeChild(pointPair.getRightBottomPoint());
        }
    }

    public void addPointPair(PointPair pointPair) {
        if (data == null) {
            data = new ArrayList<>();
        }

        if (data.contains(pointPair)) {
            return;
        }

        if (pointPair != null) {
            data.add(pointPair);
            addChild(pointPair.getLeftTopPoint());
            addChild(pointPair.getRightBottomPoint());
        }
    }

    public MarkArea withItemStyle(ItemStyle itemStyle) {
        setItemStyle(itemStyle);
        return this;
    }

    public MarkArea withEmphasis(Emphasis emphasis) {
        setEmphasis(emphasis);
        return this;
    }

    public MarkArea withBlur(Blur blur) {
        setBlur(blur);
        return this;
    }

    public MarkArea withData(PointPair... data) {
        setData(data);
        return this;
    }
}
