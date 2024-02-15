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

package io.jmix.chartsflowui.kit.component.model;

import elemental.json.JsonException;
import elemental.json.JsonValue;
import elemental.json.impl.JreJsonFactory;
import io.jmix.chartsflowui.kit.component.JmixChart;
import io.jmix.chartsflowui.kit.component.model.axis.*;
import io.jmix.chartsflowui.kit.component.model.datazoom.AbstractDataZoom;
import io.jmix.chartsflowui.kit.component.model.legend.AbstractLegend;
import io.jmix.chartsflowui.kit.component.model.series.AbstractSeries;
import io.jmix.chartsflowui.kit.component.model.shared.Color;
import io.jmix.chartsflowui.kit.component.model.shared.TextStyle;
import io.jmix.chartsflowui.kit.component.model.toolbox.Toolbox;
import io.jmix.chartsflowui.kit.component.model.visualMap.AbstractVisualMap;
import jakarta.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Basic chart options.
 */
public class ChartOptions extends ChartObservableObject {

    protected Title title;
    protected AbstractLegend<?> legend;
    protected Tooltip tooltip;
    protected Toolbox toolbox;
    protected AxisPointer axisPointer;
    protected DataSet dataSet;

    protected Aria aria;
    protected Brush brush;
    protected List<AbstractVisualMap<?>> visualMap;

    protected final JmixChart chart;
    protected JsonValue nativeJson;
    protected JreJsonFactory parser = new JreJsonFactory();

    protected List<AbstractSeries<?>> seriesList;
    protected List<XAxis> xAxes;
    protected List<YAxis> yAxes;
    protected RadiusAxis radiusAxis;
    protected AngleAxis angleAxis;

    protected List<Grid> grids;
    protected List<AbstractDataZoom<?>> dataZoom;

    protected Polar polar;
    protected Radar radar;

    protected List<Color> colorPalette;
    protected Color backgroundColor;
    protected TextStyle textStyle;

    protected Boolean animation;
    protected Integer animationThreshold;
    protected Integer animationDuration;
    protected String animationEasing;
    protected Integer animationDelay;
    protected Integer animationDurationUpdate;
    protected String animationEasingUpdate;
    protected Integer animationDelayUpdate;
    protected StateAnimation stateAnimation;

    protected BlendMode blendMode;
    protected Integer hoverLayerThreshold;
    protected Boolean useUtc;

    public ChartOptions(JmixChart chart) {
        this.chart = chart;
    }

    public JsonValue getNativeJson() {
        return nativeJson;
    }

    protected String getNativeJsonString() {
        return this.nativeJson == null ? null : this.nativeJson.toJson();
    }

    public void setNativeJson(String nativeJson) {
        if (StringUtils.equals(getNativeJsonString(), nativeJson)) {
            return;
        }

        JsonValue jsonObject = null;
        if (nativeJson != null) {
            try {
                jsonObject = parser.parse(nativeJson);
            } catch (JsonException e) {
                throw new IllegalStateException("Unable to parse JSON options");
            }
        }

        this.nativeJson = jsonObject;
        markAsDirty();
    }

    public void addSeries(AbstractSeries<?> series) {
        if (seriesList == null) {
            seriesList = new ArrayList<>();
        }

        if (seriesList.contains(series)) {
            return;
        }

        seriesList.add(series);
        addChild(series);
    }

    public void removeSeries(AbstractSeries<?> series) {
        if (seriesList != null && seriesList.remove(series)) {
            removeChild(series);
        }
    }

    public List<AbstractSeries<?>> getSeries() {
        return seriesList == null ? null : Collections.unmodifiableList(seriesList);
    }

    public void addXAxis(XAxis axis) {
        if (xAxes == null) {
            xAxes = new ArrayList<>();
        }

        if (xAxes.contains(axis)) {
            return;
        }

        xAxes.add(axis);
        addChild(axis);
    }

    public void removeXAxis(XAxis axis) {
        if (xAxes != null && xAxes.remove(axis)) {
            removeChild(axis);
        }
    }

    public List<XAxis> getXAxes() {
        return xAxes == null ? null : Collections.unmodifiableList(xAxes);
    }

    public void addYAxis(YAxis Axis) {
        if (yAxes == null) {
            yAxes = new ArrayList<>();
        }

        if (yAxes.contains(Axis)) {
            return;
        }

        yAxes.add(Axis);
        addChild(Axis);
    }

    public void removeYAxis(YAxis Axis) {
        if (yAxes != null && yAxes.remove(Axis)) {
            removeChild(Axis);
        }
    }

    public List<YAxis> getYAxes() {
        return yAxes == null ? null : Collections.unmodifiableList(yAxes);
    }

    public void setRadiusAxis(RadiusAxis axis) {
        if (this.radiusAxis != null) {
            removeChild(this.radiusAxis);
        }

        this.radiusAxis = axis;
        addChild(axis);
    }

    public RadiusAxis getRadiusAxis() {
        return radiusAxis;
    }

    public void setAngleAxis(AngleAxis axis) {
        if (this.angleAxis != null) {
            removeChild(this.angleAxis);
        }

        this.angleAxis = axis;
        addChild(axis);
    }

    public AngleAxis getAngleAxis() {
        return angleAxis;
    }

    public void addGrid(Grid grid) {
        if (grids == null) {
            grids = new ArrayList<>();
        }

        if (grids.contains(grid)) {
            return;
        }

        grids.add(grid);
        addChild(grid);
    }

    public void removeGrid(Grid grid) {
        if (grid != null && grids.remove(grid)) {
            removeChild(grid);
        }
    }

    public List<Grid> getGrids() {
        return grids == null ? null : Collections.unmodifiableList(grids);
    }

    public void addDataZoom(AbstractDataZoom<?> dataZoom) {
        if (this.dataZoom == null) {
            this.dataZoom = new ArrayList<>();
        }

        if (this.dataZoom.contains(dataZoom)) {
            return;
        }

        this.dataZoom.add(dataZoom);
        addChild(dataZoom);
    }

    public void removeDataZoom(AbstractDataZoom<?> dataZoom) {
        if (dataZoom != null && this.dataZoom.remove(dataZoom)) {
            removeChild(dataZoom);
        }
    }

    public List<AbstractDataZoom<?>> getDataZoom() {
        return dataZoom == null ? null : Collections.unmodifiableList(dataZoom);
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

    public AbstractLegend<?> getLegend() {
        return legend;
    }

    public void setLegend(AbstractLegend<?> legend) {
        if (this.legend != null) {
            removeChild(this.legend);
        }

        this.legend = legend;
        addChild(legend);
    }

    public Tooltip getTooltip() {
        return tooltip;
    }

    public void setTooltip(Tooltip tooltip) {
        if (this.tooltip != null) {
            removeChild(this.tooltip);
        }

        this.tooltip = tooltip;
        addChild(tooltip);
    }

    public Toolbox getToolbox() {
        return toolbox;
    }

    public void setToolbox(Toolbox toolbox) {
        if (this.toolbox != null) {
            removeChild(this.toolbox);
        }

        this.toolbox = toolbox;
        addChild(toolbox);
    }

    public AxisPointer getAxisPointer() {
        return axisPointer;
    }

    public void setAxisPointer(AxisPointer axisPointer) {
        if (this.axisPointer != null) {
            removeChild(this.axisPointer);
        }

        this.axisPointer = axisPointer;
        addChild(axisPointer);
    }

    public Polar getPolar() {
        return polar;
    }

    public void setPolar(Polar polar) {
        if (this.polar != null) {
            removeChild(this.polar);
        }

        this.polar = polar;
        addChild(polar);
    }

    public Radar getRadar() {
        return radar;
    }

    public void setRadar(Radar radar) {
        if (this.radar != null) {
            removeChild(this.radar);
        }

        this.radar = radar;
        addChild(radar);
    }

    public DataSet getDataSet() {
        return dataSet;
    }

    public void setDataSet(DataSet dataSet) {
        if (this.dataSet == dataSet) {
            return;
        }

        if (this.dataSet != null) {
            removeChild(this.dataSet);
        }

        this.dataSet = dataSet;

        if (dataSet != null) {
            dataSet.setChart(chart);
        }
        addChild(dataSet);
    }

    public Aria getAria() {
        return aria;
    }

    public void setAria(Aria aria) {
        if (this.aria != null) {
            removeChild(this.aria);
        }

        this.aria = aria;
        addChild(aria);
    }

    public Brush getBrush() {
        return brush;
    }

    public void setBrush(Brush brush) {
        if (this.brush != null) {
            removeChild(this.brush);
        }

        this.brush = brush;
        addChild(brush);
    }

    public void addVisualMap(AbstractVisualMap<?> visualMap) {
        if (this.visualMap == null) {
            this.visualMap = new ArrayList<>();
        }

        if (this.visualMap.contains(visualMap)) {
            return;
        }

        this.visualMap.add(visualMap);
        addChild(visualMap);
    }

    public void removeVisualMap(AbstractVisualMap<?> visualMap) {
        if (visualMap != null && this.visualMap.remove(visualMap)) {
            removeChild(visualMap);
        }
    }

    public List<AbstractVisualMap<?>> getVisualMap() {
        return visualMap == null ? null : Collections.unmodifiableList(visualMap);
    }

    public List<Color> getColorPalette() {
        return colorPalette == null ? null : Collections.unmodifiableList(colorPalette);
    }

    public void setColorPalette(Color... palette) {
        for (Color color : palette) {
            addColorToPalette(color);
        }
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        markAsDirty();
    }

    public void addColorToPalette(Color color) {
        if (colorPalette == null) {
            colorPalette = new ArrayList<>();
        }

        colorPalette.add(color);
        markAsDirty();
    }

    public void clearColorPalette() {
        this.colorPalette = null;
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

    public Integer getAnimationDuration() {
        return animationDuration;
    }

    public void setAnimationDuration(Integer animationDuration) {
        this.animationDuration = animationDuration;
        markAsDirty();
    }

    public String getAnimationEasing() {
        return animationEasing;
    }

    public void setAnimationEasing(String animationEasing) {
        this.animationEasing = animationEasing;
        markAsDirty();
    }

    public Integer getAnimationDelay() {
        return animationDelay;
    }

    public void setAnimationDelay(Integer animationDelay) {
        this.animationDelay = animationDelay;
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

    public StateAnimation getStateAnimation() {
        return stateAnimation;
    }

    public void setStateAnimation(StateAnimation stateAnimation) {
        this.stateAnimation = stateAnimation;
        markAsDirty();
    }

    public BlendMode getBlendMode() {
        return blendMode;
    }

    public void setBlendMode(BlendMode blendMode) {
        this.blendMode = blendMode;
        markAsDirty();
    }

    public Integer getHoverLayerThreshold() {
        return hoverLayerThreshold;
    }

    public void setHoverLayerThreshold(Integer hoverLayerThreshold) {
        this.hoverLayerThreshold = hoverLayerThreshold;
        markAsDirty();
    }

    public Boolean getUseUtc() {
        return useUtc;
    }

    public void setUseUtc(Boolean useUtc) {
        this.useUtc = useUtc;
        markAsDirty();
    }

    @Override
    public void markAsDirtyInDepth() {
        super.markAsDirtyInDepth();
    }

    @Override
    public void unmarkDirtyInDepth() {
        super.unmarkDirtyInDepth();
    }

    @Override
    public void setChartObjectChangeListener(Consumer<ObjectChangeEvent> listener) {
        super.setChartObjectChangeListener(listener);
    }

    @Override
    public boolean isDirtyInDepth() {
        return super.isDirtyInDepth();
    }

    /**
     * State switching animation configurations. Can be configured in each series individually.
     */
    public static class StateAnimation extends ChartObservableObject {

        protected Integer duration;

        protected String easing;

        public Integer getDuration() {
            return duration;
        }

        public void setDuration(Integer duration) {
            this.duration = duration;
            markAsDirty();
        }

        public String getEasing() {
            return easing;
        }

        public void setEasing(String easing) {
            this.easing = easing;
            markAsDirty();
        }

        public StateAnimation withDuration(Integer duration) {
            setDuration(duration);
            return this;
        }

        public StateAnimation withEasing(String easing) {
            setEasing(easing);
            return this;
        }
    }

    /**
     * Type of compositing operation to apply when drawing a new shape.
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/CanvasRenderingContext2D/globalCompositeOperation">CanvasRenderingContext2D.globalCompositeOperation [MDN]</a>
     */
    public enum BlendMode implements HasEnumId {
        SOURCE_OVER("source-over"),
        SOURCE_IN("source-in"),
        SOURCE_OUT("source-out"),
        SOURCE_ATOP("source-atop"),
        DESTINATION_OVER("destination-over"),
        DESTINATION_IN("destination-in"),
        DESTINATION_OUT("destination-out"),
        DESTINATION_ATOP("destination-atop"),
        LIGHTER("lighter"),
        COPY("copy"),
        XOR("xor"),
        MULTIPLY("multiply"),
        SCREEN("screen"),
        OVERLAY("overlay"),
        DARKEN("darken"),
        LIGHTEN("lighten"),
        COLOR_DODGE("color-dodge"),
        COLOR_BURN("color-burn"),
        HARD_LIGHT("hard-light"),
        SOFT_LIGHT("soft-light"),
        DIFFERENCE("difference"),
        EXCLUSION("exclusion"),
        HUE("hue"),
        SATURATION("saturation"),
        COLOR("color"),
        LUMINOSITY("luminosity");

        private final String id;

        BlendMode(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }

        @Nullable
        public static BlendMode fromId(String id) {
            for (BlendMode at : BlendMode.values()) {
                if (at.getId().equals(id)) {
                    return at;
                }
            }
            return null;
        }
    }
}
