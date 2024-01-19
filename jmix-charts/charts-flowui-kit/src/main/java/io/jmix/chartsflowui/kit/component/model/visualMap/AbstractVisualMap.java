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

package io.jmix.chartsflowui.kit.component.model.visualMap;

import io.jmix.chartsflowui.kit.component.model.ChartObservableObject;
import io.jmix.chartsflowui.kit.component.model.HasEnumId;
import io.jmix.chartsflowui.kit.component.model.HasPosition;
import io.jmix.chartsflowui.kit.component.model.shared.*;
import jakarta.annotation.Nullable;

public abstract class AbstractVisualMap<T extends AbstractVisualMap<T>> extends ChartObservableObject
        implements HasPadding<T>, HasPosition<T> {

    protected String id;

    protected VisualMapType type;

    protected Double min;

    protected Double max;

    protected Boolean inverse;

    protected Double precision;

    protected Double itemWidth;

    protected Double itemHeight;

    protected MapAlign align;

    protected String[] text;

    protected Double textGap;

    protected Boolean show;

    protected String dimension;

    protected Integer[] seriesIndex;

    protected Boolean hoverLink;

    protected VisualEffect inRange;

    protected VisualEffect outOfRange;

    protected VisualMapController controller;

    protected Double zLevel;

    protected Double z;

    protected String left;

    protected String top;

    protected String right;

    protected String bottom;

    protected Padding padding;

    protected Color backgroundColor;

    protected Color borderColor;

    protected Double borderWidth;

    protected TextStyle textStyle;

    protected String formatter;

    protected JsFunction formatterFunction;

    protected Orientation orientation;

    protected AbstractVisualMap(VisualMapType type) {
        this.type = type;
    }

    public enum MapAlign implements HasEnumId {
        AUTO("auto"),
        LEFT("left"),
        TOP("top"),
        BOTTOM("bottom"),
        RIGHT("right");

        private final String id;

        MapAlign(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }

        @Nullable
        public static MapAlign fromId(String id) {
            for (MapAlign at : MapAlign.values()) {
                if (at.getId().equals(id)) {
                    return at;
                }
            }
            return null;
        }
    }

    public static class VisualMapController extends ChartObservableObject {

        protected VisualEffect inRange;

        protected VisualEffect outOfRange;

        public VisualEffect getInRange() {
            return inRange;
        }

        public void setInRange(VisualEffect inRange) {
            if (this.inRange != null) {
                removeChild(this.inRange);
            }

            this.inRange = inRange;
            addChild(inRange);
        }

        public VisualEffect getOutOfRange() {
            return outOfRange;
        }

        public void setOutOfRange(VisualEffect outOfRange) {
            if (this.outOfRange != null) {
                removeChild(this.outOfRange);
            }

            this.outOfRange = outOfRange;
            addChild(outOfRange);
        }

        public VisualMapController withInRange(VisualEffect inRange) {
            setInRange(inRange);
            return this;
        }

        public VisualMapController withOutOfRange(VisualEffect outOfRange) {
            setOutOfRange(outOfRange);
            return this;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
        markAsDirty();
    }

    public VisualMapType getType() {
        return type;
    }

    public Double getMin() {
        return min;
    }

    public void setMin(Double min) {
        this.min = min;
        markAsDirty();
    }

    public Double getMax() {
        return max;
    }

    public void setMax(Double max) {
        this.max = max;
        markAsDirty();
    }

    public Boolean getInverse() {
        return inverse;
    }

    public void setInverse(Boolean inverse) {
        this.inverse = inverse;
        markAsDirty();
    }

    public Double getPrecision() {
        return precision;
    }

    public void setPrecision(Double precision) {
        this.precision = precision;
        markAsDirty();
    }

    public Double getItemWidth() {
        return itemWidth;
    }

    public void setItemWidth(Double itemWidth) {
        this.itemWidth = itemWidth;
        markAsDirty();
    }

    public Double getItemHeight() {
        return itemHeight;
    }

    public void setItemHeight(Double itemHeight) {
        this.itemHeight = itemHeight;
        markAsDirty();
    }

    public MapAlign getAlign() {
        return align;
    }

    public void setAlign(MapAlign align) {
        this.align = align;
        markAsDirty();
    }

    public String[] getText() {
        return text;
    }

    public void setText(String startText, String endTest) {
        this.text = new String[]{startText, endTest};
        markAsDirty();
    }

    public Double getTextGap() {
        return textGap;
    }

    public void setTextGap(Double textGap) {
        this.textGap = textGap;
        markAsDirty();
    }

    public Boolean getShow() {
        return show;
    }

    public void setShow(Boolean show) {
        this.show = show;
        markAsDirty();
    }

    public String getDimension() {
        return dimension;
    }

    public void setDimension(String dimension) {
        this.dimension = dimension;
        markAsDirty();
    }

    public Integer[] getSeriesIndex() {
        return seriesIndex;
    }

    public void setSeriesIndex(Integer... seriesIndex) {
        this.seriesIndex = seriesIndex;
        markAsDirty();
    }

    public Boolean getHoverLink() {
        return hoverLink;
    }

    public void setHoverLink(Boolean hoverLink) {
        this.hoverLink = hoverLink;
        markAsDirty();
    }

    public VisualEffect getInRange() {
        return inRange;
    }

    public void setInRange(VisualEffect inRange) {
        if (this.inRange != null) {
            removeChild(this.inRange);
        }

        this.inRange = inRange;
        addChild(inRange);
    }

    public VisualEffect getOutOfRange() {
        return outOfRange;
    }

    public void setOutOfRange(VisualEffect outOfRange) {
        if (this.outOfRange != null) {
            removeChild(this.outOfRange);
        }

        this.outOfRange = outOfRange;
        addChild(outOfRange);
    }

    public VisualMapController getController() {
        return controller;
    }

    public void setController(VisualMapController controller) {
        if (this.controller != null) {
            removeChild(this.controller);
        }

        this.controller = controller;
        addChild(controller);
    }

    public Double getZLevel() {
        return zLevel;
    }

    public void setZLevel(Double zLevel) {
        this.zLevel = zLevel;
        markAsDirty();
    }

    public Double getZ() {
        return z;
    }

    public void setZ(Double z) {
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

    public Double getBorderWidth() {
        return borderWidth;
    }

    public void setBorderWidth(Double borderWidth) {
        this.borderWidth = borderWidth;
        markAsDirty();
    }

    public TextStyle getTextStyle() {
        return textStyle;
    }

    public void setTextStyle(TextStyle textStyle) {
        if (this.textStyle != null) {
            removeChild(this.textStyle);
        }

        this.textStyle = textStyle;
        addChild(textStyle);
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

    public Orientation getOrientation() {
        return orientation;
    }

    public void setOrientation(Orientation orientation) {
        this.orientation = orientation;
        markAsDirty();
    }

    @SuppressWarnings("unchecked")
    public T withId(String id) {
        setId(id);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withMin(Double min) {
        setMin(min);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withMax(Double max) {
        setMax(max);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withInverse(Boolean inverse) {
        setInverse(inverse);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withPrecision(Double precision) {
        setPrecision(precision);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withItemWidth(Double itemWidth) {
        setItemWidth(itemWidth);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withItemHeight(Double itemHeight) {
        setItemHeight(itemHeight);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withAlign(MapAlign align) {
        setAlign(align);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withText(String startText, String endText) {
        setText(startText, endText);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withTextGap(Double textGap) {
        setTextGap(textGap);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withShow(Boolean show) {
        setShow(show);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withDimension(String dimension) {
        setDimension(dimension);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withSeriesIndex(Integer... seriesIndex) {
        setSeriesIndex(seriesIndex);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withHoverLink(Boolean hoverLink) {
        setHoverLink(hoverLink);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withInRange(VisualEffect inRange) {
        setInRange(inRange);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withOutOfRange(VisualEffect outOfRange) {
        setOutOfRange(outOfRange);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withController(VisualMapController controller) {
        setController(controller);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withZLevel(Double zLevel) {
        setZLevel(zLevel);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withZ(Double z) {
        setZ(z);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withBackgroundColor(Color backgroundColor) {
        setBackgroundColor(backgroundColor);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withBorderColor(Color borderColor) {
        setBorderColor(borderColor);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withBorderWidth(Double borderWidth) {
        setBorderWidth(borderWidth);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withTextStyle(TextStyle textStyle) {
        setTextStyle(textStyle);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withFormatter(String formatter) {
        setFormatter(formatter);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withFormatterFunction(JsFunction formatterFunction) {
        setFormatterFunction(formatterFunction);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withFormatterFunction(String formatterFunction) {
        setFormatterFunction(formatterFunction);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withOrientation(Orientation orient) {
        setOrientation(orient);
        return (T) this;
    }
}
