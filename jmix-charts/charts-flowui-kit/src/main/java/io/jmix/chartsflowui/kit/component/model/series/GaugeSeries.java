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

package io.jmix.chartsflowui.kit.component.model.series;

import io.jmix.chartsflowui.kit.component.model.ChartObservableObject;
import io.jmix.chartsflowui.kit.component.model.DataSet;
import io.jmix.chartsflowui.kit.component.model.HasBorder;
import io.jmix.chartsflowui.kit.component.model.HasShadow;
import io.jmix.chartsflowui.kit.component.model.series.mark.MarkArea;
import io.jmix.chartsflowui.kit.component.model.series.mark.MarkLine;
import io.jmix.chartsflowui.kit.component.model.series.mark.MarkPoint;
import io.jmix.chartsflowui.kit.component.model.shared.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gauge charts use needles to show information as a reading on a dial.<br/>
 * <b>Note</b>: Gauge series is the only series that requires an independent description of data
 * in a {@link GaugeSeries#data}. All other series use the {@link DataSet} as a data provider.<br/>
 * More detailed information is provided in the documentation.
 *
 * @see <a href="https://echarts.apache.org/en/option.html#series-gauge">GaugeSeries documentation</a>
 */
public class GaugeSeries extends AbstractSeries<GaugeSeries> {

    protected String[] center;

    protected String radius;

    protected Boolean legendHoverLink;

    protected Integer startAngle;

    protected Integer endAngle;

    protected Boolean clockwise;

    protected List<DataItem> data;

    protected Integer min;

    protected Integer max;

    protected Integer splitNumber;

    protected AxisLine axisLine;

    protected Progress progress;

    protected SplitLine splitLine;

    protected AxisTick axisTick;

    protected Label axisLabel;

    protected ItemStyle itemStyle;

    protected Pointer pointer;

    protected Anchor anchor;

    protected Emphasis emphasis;

    protected Title title;

    protected Detail detail;

    protected MarkPoint markPoint;

    protected MarkLine markLine;

    protected MarkArea markArea;

    protected Boolean animation;

    protected Integer animationThreshold;

    protected Integer animationDurationUpdate;

    protected String animationEasingUpdate;

    protected Integer animationDelayUpdate;

    public GaugeSeries() {
        super(SeriesType.GAUGE);
    }

    /**
     * Data item to describe series data.
     */
    public static class DataItem extends ChartObservableObject {

        protected Title title;

        protected Detail detail;

        protected String name;

        protected Double value;

        protected ItemStyle itemStyle;

        public Title getTitle() {
            return title;
        }

        public void setTitle(Title title) {
            if (this.title != null) {
                removeChild(this.title);
            }

            this.title = title;
            addChild(title);
        }

        public Detail getDetail() {
            return detail;
        }

        public void setDetail(Detail detail) {
            if (this.detail != null) {
                removeChild(this.detail);
            }

            this.detail = detail;
            addChild(detail);
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
            markAsDirty();
        }

        public Double getValue() {
            return value;
        }

        public void setValue(Double value) {
            this.value = value;
            markAsDirty();
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

        public DataItem withTitle(Title title) {
            setTitle(title);
            return this;
        }

        public DataItem withDetail(Detail detail) {
            setDetail(detail);
            return this;
        }

        public DataItem withName(String name) {
            setName(name);
            return this;
        }

        public DataItem withValue(Double value) {
            setValue(value);
            return this;
        }

        public DataItem withItemStyle(ItemStyle itemStyle) {
            setItemStyle(itemStyle);
            return this;
        }
    }

    /**
     * The related configuration about the axis line of gauge chart.
     *
     * @see <a href="https://echarts.apache.org/en/option.html#series-gauge.axisLine">GaugeSeries.axisLine</a>
     */
    public static class AxisLine extends ChartObservableObject {

        protected Boolean show;

        protected Boolean roundCap;

        protected LineStyle lineStyle;

        /**
         * The style of the axis line of gauge chart.
         *
         * @see <a href="https://echarts.apache.org/en/option.html#series-gauge.axisLine.lineStyle">GaugeSeries.axisLine.lineStyle</a>
         */
        public static class LineStyle extends ChartObservableObject
                implements HasShadow<LineStyle> {

            protected Map<Double, Color> colorPalette;

            protected Integer width;

            protected Integer shadowBlur;

            protected Color shadowColor;

            protected Integer shadowOffsetX;

            protected Integer shadowOffsetY;

            protected Double opacity;

            public Map<Double, Color> getColorPalette() {
                return this.colorPalette;
            }

            public void setColorPalette(Map<Double, Color> colorPalette) {
                this.colorPalette = colorPalette;
                markAsDirty();
            }

            public void addColorToPalette(Double range, Color color) {
                if (colorPalette == null) {
                    colorPalette = new HashMap<>();
                }

                colorPalette.put(range, color);
                markAsDirty();
            }

            public void removeColorFromPalette(Double range) {
                if (colorPalette != null && colorPalette.remove(range) != null) {
                    markAsDirty();
                }
            }

            public Integer getWidth() {
                return width;
            }

            public void setWidth(Integer width) {
                this.width = width;
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

            public LineStyle withColorPalette(Map<Double, Color> colorPalette) {
                setColorPalette(colorPalette);
                return this;
            }

            public LineStyle withColorPalette(Double range, Color color) {
                addColorToPalette(range, color);
                return this;
            }

            public LineStyle withWidth(Integer width) {
                setWidth(width);
                return this;
            }

            public LineStyle withOpacity(Double opacity) {
                setOpacity(opacity);
                return this;
            }
        }

        public Boolean getShow() {
            return show;
        }

        public void setShow(Boolean show) {
            this.show = show;
            markAsDirty();
        }

        public Boolean getRoundCap() {
            return roundCap;
        }

        public void setRoundCap(Boolean roundCap) {
            this.roundCap = roundCap;
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

        public AxisLine withRoundCap(Boolean roundCap) {
            setRoundCap(roundCap);
            return this;
        }

        public AxisLine withLineStyle(LineStyle lineStyle) {
            setLineStyle(lineStyle);
            return this;
        }
    }

    /**
     * Component that used to show current progress.
     *
     * @see <a href="https://echarts.apache.org/en/option.html#series-gauge.progress">GaugeSeries.progress</a>
     */
    public static class Progress extends ChartObservableObject {

        protected Boolean show;

        protected Boolean overlap;

        protected Integer width;

        protected Boolean roundCap;

        protected Boolean clip;

        protected ItemStyle itemStyle;

        public Boolean getShow() {
            return show;
        }

        public void setShow(Boolean show) {
            this.show = show;
            markAsDirty();
        }

        public Boolean getOverlap() {
            return overlap;
        }

        public void setOverlap(Boolean overlap) {
            this.overlap = overlap;
            markAsDirty();
        }

        public Integer getWidth() {
            return width;
        }

        public void setWidth(Integer width) {
            this.width = width;
            markAsDirty();
        }

        public Boolean getRoundCap() {
            return roundCap;
        }

        public void setRoundCap(Boolean roundCap) {
            this.roundCap = roundCap;
            markAsDirty();
        }

        public Boolean getClip() {
            return clip;
        }

        public void setClip(Boolean clip) {
            this.clip = clip;
            markAsDirty();
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

        public Progress withShow(Boolean show) {
            setShow(show);
            return this;
        }

        public Progress withOverlap(Boolean overlap) {
            setOverlap(overlap);
            return this;
        }

        public Progress withWidth(Integer width) {
            setWidth(width);
            return this;
        }

        public Progress withRoundCap(Boolean roundCap) {
            setRoundCap(roundCap);
            return this;
        }

        public Progress withClip(Boolean clip) {
            setClip(clip);
            return this;
        }

        public Progress withItemStyle(ItemStyle itemStyle) {
            setItemStyle(itemStyle);
            return this;
        }
    }

    /**
     * The style of split line.
     *
     * @see <a href="https://echarts.apache.org/en/option.html#series-gauge.splitLine">GaugeSeries.splitLine</a>
     */
    public static class SplitLine extends ChartObservableObject {

        protected Boolean show;

        protected Integer length;

        protected Integer distance;

        protected LineStyle lineStyle;

        public Boolean getShow() {
            return show;
        }

        public void setShow(Boolean show) {
            this.show = show;
            markAsDirty();
        }

        public Integer getLength() {
            return length;
        }

        public void setLength(Integer length) {
            this.length = length;
            markAsDirty();
        }

        public Integer getDistance() {
            return distance;
        }

        public void setDistance(Integer distance) {
            this.distance = distance;
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

        public SplitLine withShow(Boolean show) {
            setShow(show);
            return this;
        }

        public SplitLine withLength(Integer length) {
            setLength(length);
            return this;
        }

        public SplitLine withDistance(Integer distance) {
            setDistance(distance);
            return this;
        }

        public SplitLine withLineStyle(LineStyle lineStyle) {
            setLineStyle(lineStyle);
            return this;
        }
    }

    /**
     * The tick line style.
     *
     * @see <a href="https://echarts.apache.org/en/option.html#series-gauge.axisTick">GaugeSeries.axisTick</a>
     */
    public static class AxisTick extends ChartObservableObject {

        protected Boolean show;

        protected Integer splitNumber;

        protected Integer length;

        protected Integer distance;

        protected LineStyle lineStyle;

        public Boolean getShow() {
            return show;
        }

        public void setShow(Boolean show) {
            this.show = show;
            markAsDirty();
        }

        public Integer getSplitNumber() {
            return splitNumber;
        }

        public void setSplitNumber(Integer splitNumber) {
            this.splitNumber = splitNumber;
            markAsDirty();
        }

        public Integer getLength() {
            return length;
        }

        public void setLength(Integer length) {
            this.length = length;
            markAsDirty();
        }

        public Integer getDistance() {
            return distance;
        }

        public void setDistance(Integer distance) {
            this.distance = distance;
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

        public AxisTick withShow(Boolean show) {
            setShow(show);
            return this;
        }

        public AxisTick withSplitNumber(Integer splitNumber) {
            setSplitNumber(splitNumber);
            return this;
        }

        public AxisTick withLength(Integer length) {
            setLength(length);
            return this;
        }

        public AxisTick withDistance(Integer distance) {
            setDistance(distance);
            return this;
        }

        public AxisTick withLineStyle(LineStyle lineStyle) {
            setLineStyle(lineStyle);
            return this;
        }
    }

    /**
     * Gauge chart pointer.
     *
     * @see <a href="https://echarts.apache.org/en/option.html#series-gauge.pointer">GaugeSeries.pointer</a>
     */
    public static class Pointer extends ChartObservableObject {

        protected Boolean show;

        protected Boolean showAbove;

        protected String icon;

        protected String[] offsetCenter;

        protected String length;

        protected Integer width;

        protected Boolean keepAspect;

        protected ItemStyle itemStyle;

        public Boolean getShow() {
            return show;
        }

        public void setShow(Boolean show) {
            this.show = show;
            markAsDirty();
        }

        public Boolean getShowAbove() {
            return showAbove;
        }

        public void setShowAbove(Boolean showAbove) {
            this.showAbove = showAbove;
            markAsDirty();
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
            markAsDirty();
        }

        public String[] getOffsetCenter() {
            return offsetCenter;
        }

        public void setOffsetCenter(String xOffset, String yOffset) {
            this.offsetCenter = new String[]{xOffset, yOffset};
            markAsDirty();
        }

        public String getLength() {
            return length;
        }

        public void setLength(String length) {
            this.length = length;
            markAsDirty();
        }

        public Integer getWidth() {
            return width;
        }

        public void setWidth(Integer width) {
            this.width = width;
            markAsDirty();
        }

        public Boolean getKeepAspect() {
            return keepAspect;
        }

        public void setKeepAspect(Boolean keepAspect) {
            this.keepAspect = keepAspect;
            markAsDirty();
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

        public Pointer withShow(Boolean show) {
            setShow(show);
            return this;
        }

        public Pointer withShowAbove(Boolean showAbove) {
            setShowAbove(showAbove);
            return this;
        }

        public Pointer withIcon(String icon) {
            setIcon(icon);
            return this;
        }

        public Pointer withOffsetCenter(String xOffset, String yOffset) {
            setOffsetCenter(xOffset, yOffset);
            return this;
        }

        public Pointer withLength(String length) {
            setLength(length);
            return this;
        }

        public Pointer withWidth(Integer width) {
            setWidth(width);
            return this;
        }

        public Pointer withKeepAspect(Boolean keepAspect) {
            setKeepAspect(keepAspect);
            return this;
        }

        public Pointer withItemStyle(ItemStyle itemStyle) {
            setItemStyle(itemStyle);
            return this;
        }
    }

    /**
     * The fixed point of a pointer in a dial.
     *
     * @see <a href="https://echarts.apache.org/en/option.html#series-gauge.anchor">GaugeSeries.anchor</a>
     */
    public static class Anchor extends ChartObservableObject {

        protected Boolean show;

        protected Boolean showAbove;

        protected Integer size;

        protected String icon;

        protected String[] offsetCenter;

        protected Boolean keepAspect;

        protected ItemStyle itemStyle;

        public Boolean getShow() {
            return show;
        }

        public void setShow(Boolean show) {
            this.show = show;
            markAsDirty();
        }

        public Boolean getShowAbove() {
            return showAbove;
        }

        public void setShowAbove(Boolean showAbove) {
            this.showAbove = showAbove;
            markAsDirty();
        }

        public Integer getSize() {
            return size;
        }

        public void setSize(Integer size) {
            this.size = size;
            markAsDirty();
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
            markAsDirty();
        }

        public String[] getOffsetCenter() {
            return offsetCenter;
        }

        public void setOffsetCenter(String xOffset, String yOffset) {
            this.offsetCenter = new String[]{xOffset, yOffset};
            markAsDirty();
        }

        public Boolean getKeepAspect() {
            return keepAspect;
        }

        public void setKeepAspect(Boolean keepAspect) {
            this.keepAspect = keepAspect;
            markAsDirty();
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

        public Anchor withShow(Boolean show) {
            setShow(show);
            return this;
        }

        public Anchor withShowAbove(Boolean showAbove) {
            setShowAbove(showAbove);
            return this;
        }

        public Anchor withSize(Integer size) {
            setSize(size);
            return this;
        }

        public Anchor withIcon(String icon) {
            setIcon(icon);
            return this;
        }

        public Anchor withOffsetCenter(String xOffset, String yOffset) {
            setOffsetCenter(xOffset, yOffset);
            return this;
        }

        public Anchor withKeepAspect(Boolean keepAspect) {
            setKeepAspect(keepAspect);
            return this;
        }

        public Anchor withItemStyle(ItemStyle itemStyle) {
            setItemStyle(itemStyle);
            return this;
        }
    }

    /**
     * Component to configure the emphasis state.
     */
    public static class Emphasis extends ChartObservableObject {

        protected Boolean disabled;

        protected ItemStyle itemStyle;

        public Boolean getDisabled() {
            return disabled;
        }

        public void setDisabled(Boolean disabled) {
            this.disabled = disabled;
            markAsDirty();
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

        public Emphasis withDisabled(Boolean disabled) {
            setDisabled(disabled);
            return this;
        }

        public Emphasis withItemStyle(ItemStyle itemStyle) {
            setItemStyle(itemStyle);
            return this;
        }
    }

    /**
     * Component to configure the title of gauge chart.
     */
    public static class Title extends AbstractGaugeText<Title> {
    }

    /**
     * The detail about gauge chart which is used to show data.
     */
    public static class Detail extends AbstractGaugeText<Detail> {

        protected String formatter;

        protected JsFunction formatterFunction;

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

        public Detail withFormatter(String formatter) {
            setFormatter(formatter);
            return this;
        }

        public Detail withFormatterFunction(JsFunction formatterFunction) {
            setFormatterFunction(formatterFunction);
            return this;
        }

        public Detail withFormatterFunction(String formatterFunction) {
            setFormatterFunction(formatterFunction);
            return this;
        }
    }

    /**
     * Base class for gauge text components.
     *
     * @param <T> origin text component class type
     */
    public abstract static class AbstractGaugeText<T extends AbstractGaugeText<T>> extends AbstractRichText<T>
            implements HasShadow<T>, HasPadding<T>, HasBorder<T> {

        protected Boolean show;

        protected String[] offsetCenter;

        protected Boolean valueAnimation;

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

        public String[] getOffsetCenter() {
            return offsetCenter;
        }

        public void setOffsetCenter(String xOffset, String yOffset) {
            this.offsetCenter = new String[]{xOffset, yOffset};
            markAsDirty();
        }

        public Boolean getValueAnimation() {
            return valueAnimation;
        }

        public void setValueAnimation(Boolean valueAnimation) {
            this.valueAnimation = valueAnimation;
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

        @SuppressWarnings("unchecked")
        public T withShow(Boolean show) {
            setShow(show);
            return (T) this;
        }

        @SuppressWarnings("unchecked")
        public T withOffsetCenter(String xOffset, String yOffset) {
            setOffsetCenter(xOffset, yOffset);
            return (T) this;
        }

        @SuppressWarnings("unchecked")
        public T withValueAnimation(Boolean valueAnimation) {
            setValueAnimation(valueAnimation);
            return (T) this;
        }

        @SuppressWarnings("unchecked")
        public T withBackgroundColor(Color backgroundColor) {
            setBackgroundColor(backgroundColor);
            return (T) this;
        }

        @SuppressWarnings("unchecked")
        public T withBorderType(String borderType) {
            setBorderType(borderType);
            return (T) this;
        }

        @SuppressWarnings("unchecked")
        public T withBorderDashOffset(Integer borderDashOffset) {
            setBorderDashOffset(borderDashOffset);
            return (T) this;
        }
    }

    public String[] getCenter() {
        return center;
    }

    public void setCenter(String x, String y) {
        this.center = new String[]{x, y};
        markAsDirty();
    }

    public String getRadius() {
        return radius;
    }

    public void setRadius(String radius) {
        this.radius = radius;
        markAsDirty();
    }

    public Boolean getLegendHoverLink() {
        return legendHoverLink;
    }

    public void setLegendHoverLink(Boolean legendHoverLink) {
        this.legendHoverLink = legendHoverLink;
        markAsDirty();
    }

    public Integer getStartAngle() {
        return startAngle;
    }

    public void setStartAngle(Integer startAngle) {
        this.startAngle = startAngle;
        markAsDirty();
    }

    public Integer getEndAngle() {
        return endAngle;
    }

    public void setEndAngle(Integer endAngle) {
        this.endAngle = endAngle;
        markAsDirty();
    }

    public Boolean getClockwise() {
        return clockwise;
    }

    public List<DataItem> getData() {
        return data;
    }

    public void setData(List<DataItem> data) {
        if (this.data != null) {
            this.data.forEach(this::addChild);
        }

        this.data = data;
        if (data != null) {
            data.forEach(this::addChild);
        }
    }

    public void setData(DataItem... data) {
        setData(data == null ? null : List.of(data));
    }

    public void removeData(DataItem dataItem) {
        if (data != null && data.remove(dataItem)) {
            removeChild(dataItem);
        }
    }

    public void addData(DataItem dataItem) {
        if (data == null) {
            data = new ArrayList<>();
        }

        if (data.contains(dataItem)) {
            return;
        }

        if (dataItem != null) {
            data.add(dataItem);
            addChild(dataItem);
        }
    }

    public void setClockwise(Boolean clockwise) {
        this.clockwise = clockwise;
        markAsDirty();
    }

    public Integer getMin() {
        return min;
    }

    public void setMin(Integer min) {
        this.min = min;
        markAsDirty();
    }

    public Integer getMax() {
        return max;
    }

    public void setMax(Integer max) {
        this.max = max;
        markAsDirty();
    }

    public Integer getSplitNumber() {
        return splitNumber;
    }

    public void setSplitNumber(Integer splitNumber) {
        this.splitNumber = splitNumber;
        markAsDirty();
    }

    public AxisLine getAxisLine() {
        return axisLine;
    }

    public void setAxisLine(AxisLine axisLine) {
        if (this.axisLine != null) {
            removeChild(this.axisLine);
        }

        this.axisLine = axisLine;
        addChild(axisLine);
    }

    public Progress getProgress() {
        return progress;
    }

    public void setProgress(Progress progress) {
        if (this.progress != null) {
            removeChild(this.progress);
        }

        this.progress = progress;
        addChild(progress);
    }

    public SplitLine getSplitLine() {
        return splitLine;
    }

    public void setSplitLine(SplitLine splitLine) {
        if (this.splitLine != null) {
            removeChild(this.splitLine);
        }

        this.splitLine = splitLine;
        addChild(splitLine);
    }

    public AxisTick getAxisTick() {
        return axisTick;
    }

    public void setAxisTick(AxisTick axisTick) {
        if (this.axisTick != null) {
            removeChild(this.axisTick);
        }

        this.axisTick = axisTick;
        addChild(axisTick);
    }

    public Label getAxisLabel() {
        return axisLabel;
    }

    public void setAxisLabel(Label axisLabel) {
        if (this.axisLabel != null) {
            removeChild(this.axisLabel);
        }

        this.axisLabel = axisLabel;
        addChild(axisLabel);
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

    public Pointer getPointer() {
        return pointer;
    }

    public void setPointer(Pointer pointer) {
        if (this.pointer != null) {
            removeChild(this.pointer);
        }

        this.pointer = pointer;
        addChild(pointer);
    }

    public Anchor getAnchor() {
        return anchor;
    }

    public void setAnchor(Anchor anchor) {
        if (this.anchor != null) {
            removeChild(this.anchor);
        }

        this.anchor = anchor;
        addChild(anchor);
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

    public Title getTitle() {
        return title;
    }

    public void setTitle(Title title) {
        if (this.title != null) {
            removeChild(this.title);
        }

        this.title = title;
        addChild(title);
    }

    public Detail getDetail() {
        return detail;
    }

    public void setDetail(Detail detail) {
        if (this.detail != null) {
            removeChild(this.detail);
        }

        this.detail = detail;
        addChild(detail);
    }

    public MarkPoint getMarkPoint() {
        return markPoint;
    }

    public void setMarkPoint(MarkPoint markPoint) {
        if (this.markPoint != null) {
            removeChild(this.markPoint);
        }

        this.markPoint = markPoint;
        addChild(markPoint);
    }

    public MarkLine getMarkLine() {
        return markLine;
    }

    public void setMarkLine(MarkLine markLine) {
        if (this.markLine != null) {
            removeChild(this.markLine);
        }

        this.markLine = markLine;
        addChild(markLine);
    }

    public MarkArea getMarkArea() {
        return markArea;
    }

    public void setMarkArea(MarkArea markArea) {
        if (this.markArea != null) {
            removeChild(this.markArea);
        }

        this.markArea = markArea;
        addChild(markArea);
    }

    public Boolean getAnimation() {
        return animation;
    }

    public void setAnimation(Boolean animation) {
        this.animation = animation;
        markAsDirty();
    }

    public Integer getAnimationThreshold() {
        return animationThreshold;
    }

    public void setAnimationThreshold(Integer animationThreshold) {
        this.animationThreshold = animationThreshold;
        markAsDirty();
    }

    public Integer getAnimationDurationUpdate() {
        return animationDurationUpdate;
    }

    public void setAnimationDurationUpdate(Integer animationDurationUpdate) {
        this.animationDurationUpdate = animationDurationUpdate;
        markAsDirty();
    }

    public String getAnimationEasingUpdate() {
        return animationEasingUpdate;
    }

    public void setAnimationEasingUpdate(String animationEasingUpdate) {
        this.animationEasingUpdate = animationEasingUpdate;
        markAsDirty();
    }

    public Integer getAnimationDelayUpdate() {
        return animationDelayUpdate;
    }

    public void setAnimationDelayUpdate(Integer animationDelayUpdate) {
        this.animationDelayUpdate = animationDelayUpdate;
        markAsDirty();
    }

    public GaugeSeries withCenter(String x, String y) {
        setCenter(x, y);
        return this;
    }

    public GaugeSeries withRadius(String radius) {
        setRadius(radius);
        return this;
    }

    public GaugeSeries withLegendHoverLink(Boolean legendHoverLink) {
        setLegendHoverLink(legendHoverLink);
        return this;
    }

    public GaugeSeries withStartAngle(Integer startAngle) {
        setStartAngle(startAngle);
        return this;
    }

    public GaugeSeries withEndAngle(Integer endAngle) {
        setEndAngle(endAngle);
        return this;
    }

    public GaugeSeries withClockwise(Boolean clockwise) {
        setClockwise(clockwise);
        return this;
    }


    public GaugeSeries withData(DataItem... data) {
        setData(data);
        return this;
    }

    public GaugeSeries withData(DataItem dataItem) {
        addData(dataItem);
        return this;
    }

    public GaugeSeries withMin(Integer min) {
        setMin(min);
        return this;
    }

    public GaugeSeries withMax(Integer max) {
        setMax(max);
        return this;
    }

    public GaugeSeries withSplitNumber(Integer splitNumber) {
        setSplitNumber(splitNumber);
        return this;
    }

    public GaugeSeries withAxisLine(AxisLine axisLine) {
        setAxisLine(axisLine);
        return this;
    }

    public GaugeSeries withProgress(Progress progress) {
        setProgress(progress);
        return this;
    }

    public GaugeSeries withSplitLine(SplitLine splitLine) {
        setSplitLine(splitLine);
        return this;
    }

    public GaugeSeries withAxisTick(AxisTick axisTick) {
        setAxisTick(axisTick);
        return this;
    }

    public GaugeSeries withAxisLabel(Label axisLabel) {
        setAxisLabel(axisLabel);
        return this;
    }

    public GaugeSeries withPointer(Pointer pointer) {
        setPointer(pointer);
        return this;
    }

    public GaugeSeries withItemStyle(ItemStyle itemStyle) {
        setItemStyle(itemStyle);
        return this;
    }

    public GaugeSeries withAnchor(Anchor anchor) {
        setAnchor(anchor);
        return this;
    }

    public GaugeSeries withEmphasis(Emphasis emphasis) {
        setEmphasis(emphasis);
        return this;
    }

    public GaugeSeries withTitle(Title title) {
        setTitle(title);
        return this;
    }

    public GaugeSeries withDetail(Detail detail) {
        setDetail(detail);
        return this;
    }

    public GaugeSeries withMarkPoint(MarkPoint markPoint) {
        setMarkPoint(markPoint);
        return this;
    }

    public GaugeSeries withMarkLine(MarkLine markLine) {
        setMarkLine(markLine);
        return this;
    }

    public GaugeSeries withMarkArea(MarkArea markArea) {
        setMarkArea(markArea);
        return this;
    }

    public GaugeSeries withAnimation(Boolean animation) {
        setAnimation(animation);
        return this;
    }

    public GaugeSeries withAnimationThreshold(Integer animationThreshold) {
        setAnimationThreshold(animationThreshold);
        return this;
    }

    public GaugeSeries withAnimationDurationUpdate(Integer animationDurationUpdate) {
        setAnimationDurationUpdate(animationDurationUpdate);
        return this;
    }

    public GaugeSeries withAnimationEasingUpdate(String animationEasingUpdate) {
        setAnimationEasingUpdate(animationEasingUpdate);
        return this;
    }

    public GaugeSeries withAnimationDelayUpdate(Integer animationDelayUpdate) {
        setAnimationDelayUpdate(animationDelayUpdate);
        return this;
    }
}
