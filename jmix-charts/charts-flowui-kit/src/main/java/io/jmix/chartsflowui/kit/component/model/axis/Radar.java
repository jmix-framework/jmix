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

import io.jmix.chartsflowui.kit.component.model.*;
import io.jmix.chartsflowui.kit.component.model.series.RadarSeries;
import io.jmix.chartsflowui.kit.component.model.shared.*;
import jakarta.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Coordinate for {@link RadarSeries}. Radar chart coordinate is different from polar coordinate, in that every axis
 * indicator of the radar chart coordinate is an individual dimension.
 * {@link Radar#indicators} are required to be displayed. More detailed information is provided in the documentation.
 *
 * @see <a href="https://echarts.apache.org/en/option.html#radar">Radar documentation</a>
 */
public class Radar extends ChartObservableObject {

    protected String id;

    protected Integer zLevel;

    protected Integer z;

    protected String[] center;

    protected String[] radius;

    protected Integer startAngle;

    protected AxisName axisName;

    protected Integer nameGap;

    protected Integer splitNumber;

    protected Shape shape;

    protected Boolean scale;

    protected Boolean silent;

    protected Boolean triggerEvent;

    protected AxisLine axisLine;

    protected AxisTick axisTick;

    protected AxisLabel axisLabel;

    protected SplitLine splitLine;

    protected SplitArea splitArea;

    protected List<Indicator> indicators;

    /**
     * Radar render type.
     */
    public enum Shape implements HasEnumId {
        POLYGON("polygon"),
        CIRCLE("circle");

        private final String id;

        Shape(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }

        @Nullable
        public static Shape fromId(String id) {
            for (Shape at : Shape.values()) {
                if (at.getId().equals(id)) {
                    return at;
                }
            }
            return null;
        }
    }

    /**
     * Name options for radar indicators.
     *
     * @see <a href="https://echarts.apache.org/en/option.html#radar.axisName">Radar.axisName</a>
     */
    public static class AxisName extends AbstractRichText<AxisName>
            implements HasBorder<AxisName>, HasShadow<AxisName>, HasPadding<AxisName> {

        protected Boolean show;

        protected String formatter;

        protected JsFunction formatterFunction;

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

        public AxisName withShow(Boolean show) {
            setShow(show);
            return this;
        }

        public AxisName withFormatter(String formatter) {
            setFormatter(formatter);
            return this;
        }

        public AxisName withFormatterFunction(JsFunction formatterFunction) {
            setFormatterFunction(formatterFunction);
            return this;
        }

        public AxisName withFormatterFunction(String formatterFunction) {
            setFormatterFunction(formatterFunction);
            return this;
        }

        public AxisName withBackgroundColor(Color backgroundColor) {
            setBackgroundColor(backgroundColor);
            return this;
        }

        public AxisName withBorderType(String borderType) {
            setBorderType(borderType);
            return this;
        }

        public AxisName withBorderDashOffset(Integer borderDashOffset) {
            setBorderDashOffset(borderDashOffset);
            return this;
        }
    }

    /**
     * Indicator of radar chart, which is used to assign multiple variables(dimensions) in radar chart.
     * Required attribute to displaying radar coordinates.
     *
     * @see <a href="https://echarts.apache.org/en/option.html#radar.indicator">Radar.indicator</a>
     */
    public static class Indicator extends ChartObservableObject {

        protected String name;

        protected Integer max;

        protected Integer min;

        protected Color color;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
            markAsDirty();
        }

        public Integer getMax() {
            return max;
        }

        public void setMax(Integer max) {
            this.max = max;
            markAsDirty();
        }

        public Integer getMin() {
            return min;
        }

        public void setMin(Integer min) {
            this.min = min;
            markAsDirty();
        }

        public Color getColor() {
            return color;
        }

        public void setColor(Color color) {
            this.color = color;
            markAsDirty();
        }

        public Indicator withName(String name) {
            setName(name);
            return this;
        }

        public Indicator withMax(Integer max) {
            setMax(max);
            return this;
        }

        public Indicator withMin(Integer min) {
            setMin(min);
            return this;
        }

        public Indicator withColor(Color color) {
            setColor(color);
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

    public Integer getZLevel() {
        return zLevel;
    }

    public void setZLevel(Integer zLevel) {
        this.zLevel = zLevel;
        markAsDirty();
    }

    public Integer getZ() {
        return z;
    }

    public void setZ(Integer z) {
        this.z = z;
        markAsDirty();
    }

    public String[] getCenter() {
        return center;
    }

    public void setCenter(String x, String y) {
        this.center = new String[]{x, y};
        markAsDirty();
    }

    public String[] getRadius() {
        return radius;
    }

    public void setRadius(String inner, String outer) {
        this.radius = new String[]{inner, outer};
        markAsDirty();
    }

    public Integer getStartAngle() {
        return startAngle;
    }

    public void setStartAngle(Integer startAngle) {
        this.startAngle = startAngle;
        markAsDirty();
    }

    public AxisName getAxisName() {
        return axisName;
    }

    public void setAxisName(AxisName axisName) {
        if (this.axisName != null) {
            removeChild(this.axisName);
        }

        this.axisName = axisName;
        addChild(axisName);
    }

    public Integer getNameGap() {
        return nameGap;
    }

    public void setNameGap(Integer nameGap) {
        this.nameGap = nameGap;
        markAsDirty();
    }

    public Integer getSplitNumber() {
        return splitNumber;
    }

    public void setSplitNumber(Integer splitNumber) {
        this.splitNumber = splitNumber;
        markAsDirty();
    }

    public Shape getShape() {
        return shape;
    }

    public void setShape(Shape shape) {
        this.shape = shape;
        markAsDirty();
    }

    public Boolean getScale() {
        return scale;
    }

    public void setScale(Boolean scale) {
        this.scale = scale;
        markAsDirty();
    }

    public Boolean getSilent() {
        return silent;
    }

    public void setSilent(Boolean silent) {
        this.silent = silent;
        markAsDirty();
    }

    public Boolean getTriggerEvent() {
        return triggerEvent;
    }

    public void setTriggerEvent(Boolean triggerEvent) {
        this.triggerEvent = triggerEvent;
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

    public AxisLabel getAxisLabel() {
        return axisLabel;
    }

    public void setAxisLabel(AxisLabel axisLabel) {
        if (this.axisLabel != null) {
            removeChild(this.axisLabel);
        }
        this.axisLabel = axisLabel;
        addChild(axisLabel);
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

    public SplitArea getSplitArea() {
        return splitArea;
    }

    public void setSplitArea(SplitArea splitArea) {
        if (this.splitArea != null) {
            removeChild(this.splitArea);
        }

        this.splitArea = splitArea;
        addChild(splitArea);
    }

    public List<Indicator> getIndicators() {
        return indicators;
    }

    public void setIndicators(List<Indicator> indicators) {
        if (this.indicators != null) {
            this.indicators.forEach(this::removeChild);
        }

        this.indicators = indicators;
        if (indicators != null) {
            indicators.forEach(this::addChild);
        }
    }

    public void setIndicators(Indicator... indicators) {
        setIndicators(indicators == null ? null : List.of(indicators));
    }

    public void removeIndicator(Indicator indicator) {
        if (indicators != null && indicators.remove(indicator)) {
            removeChild(indicator);
        }
    }

    public void addIndicator(Indicator indicator) {
        if (indicators == null) {
            indicators = new ArrayList<>();
        }

        if (indicators.contains(indicator)) {
            return;
        }

        indicators.add(indicator);
        addChild(indicator);
    }

    public Radar withId(String id) {
        setId(id);
        return this;
    }

    public Radar withZLevel(Integer zLevel) {
        setZLevel(zLevel);
        return this;
    }

    public Radar withZ(Integer z) {
        setZ(z);
        return this;
    }

    public Radar withCenter(String x, String y) {
        setCenter(x, y);
        return this;
    }

    public Radar withRadius(String inner, String outer) {
        setRadius(inner, outer);
        return this;
    }

    public Radar withStartAngle(Integer startAngle) {
        setStartAngle(startAngle);
        return this;
    }

    public Radar withAxisName(AxisName axisName) {
        setAxisName(axisName);
        return this;
    }

    public Radar withNameGap(Integer nameGap) {
        setNameGap(nameGap);
        return this;
    }

    public Radar withSplitNumber(Integer splitNumber) {
        setSplitNumber(splitNumber);
        return this;
    }

    public Radar withShape(Shape shape) {
        setShape(shape);
        return this;
    }

    public Radar withScale(Boolean scale) {
        setScale(scale);
        return this;
    }

    public Radar withSilent(Boolean silent) {
        setSilent(silent);
        return this;
    }

    public Radar withTriggerEvent(Boolean triggerEvent) {
        setTriggerEvent(triggerEvent);
        return this;
    }

    public Radar withAxisLine(AxisLine axisLine) {
        setAxisLine(axisLine);
        return this;
    }

    public Radar withAxisTick(AxisTick axisTick) {
        setAxisTick(axisTick);
        return this;
    }

    public Radar withAxisLabel(AxisLabel axisLabel) {
        setAxisLabel(axisLabel);
        return this;
    }

    public Radar withSplitLine(SplitLine splitLine) {
        setSplitLine(splitLine);
        return this;
    }

    public Radar withSplitArea(SplitArea splitArea) {
        setSplitArea(splitArea);
        return this;
    }

    public Radar withIndicators(List<Indicator> indicators) {
        setIndicators(indicators);
        return this;
    }

    public Radar withIndicators(Indicator... indicators) {
        setIndicators(indicators);
        return this;
    }

    public Radar withIndicator(Indicator indicator) {
        addIndicator(indicator);
        return this;
    }
}
