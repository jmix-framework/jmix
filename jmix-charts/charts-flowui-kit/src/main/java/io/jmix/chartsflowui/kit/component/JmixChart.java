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

package io.jmix.chartsflowui.kit.component;

import com.google.common.base.Strings;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.data.provider.KeyMapper;
import com.vaadin.flow.internal.ExecutionContext;
import com.vaadin.flow.internal.StateTree;
import elemental.json.JsonObject;
import elemental.json.JsonValue;
import elemental.json.impl.JreJsonFactory;
import io.jmix.chartsflowui.kit.component.model.*;
import io.jmix.chartsflowui.kit.component.model.axis.*;
import io.jmix.chartsflowui.kit.component.model.datazoom.AbstractDataZoom;
import io.jmix.chartsflowui.kit.component.model.datazoom.InsideDataZoom;
import io.jmix.chartsflowui.kit.component.model.datazoom.SliderDataZoom;
import io.jmix.chartsflowui.kit.component.model.legend.AbstractLegend;
import io.jmix.chartsflowui.kit.component.model.legend.Legend;
import io.jmix.chartsflowui.kit.component.model.legend.ScrollableLegend;
import io.jmix.chartsflowui.kit.component.model.series.*;
import io.jmix.chartsflowui.kit.component.model.shared.Color;
import io.jmix.chartsflowui.kit.component.model.shared.InnerTooltip;
import io.jmix.chartsflowui.kit.component.model.shared.TextStyle;
import io.jmix.chartsflowui.kit.component.model.toolbox.Toolbox;
import io.jmix.chartsflowui.kit.component.model.visualMap.AbstractVisualMap;
import io.jmix.chartsflowui.kit.component.model.visualMap.ContinuousVisualMap;
import io.jmix.chartsflowui.kit.component.model.visualMap.PiecewiseVisualMap;
import io.jmix.chartsflowui.kit.component.serialization.ChartIncrementalChanges;
import io.jmix.chartsflowui.kit.component.serialization.JmixChartSerializer;
import io.jmix.chartsflowui.kit.data.chart.ChartItems;
import io.jmix.chartsflowui.kit.data.chart.DataItem;
import jakarta.annotation.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag("jmix-chart")
@NpmPackage(
        value = "echarts",
        version = "5.4.3"
)
@JsModule("./src/chart/jmix-chart.js")
public class JmixChart extends Component implements HasSize {

    protected ChartOptions options;

    protected JmixChartSerializer serializer;

    protected StateTree.ExecutionRegistration synchronizeChartOptionsExecution;
    protected StateTree.ExecutionRegistration synchronizeChartDataExecution;
    protected StateTree.ExecutionRegistration synchronizeChartIncrementalUpdateDataExecution;

    protected Map<DataSet, ChartIncrementalChanges<? extends DataItem>> changedItems = new HashMap<>();

    protected KeyMapper<Object> dataItemKeys = new KeyMapper<>();

    public JmixChart() {
        initComponent();
    }

    protected void initComponent() {
        options = createChartOptions();
        serializer = createSerializer();

        initChartOptionsChangeListener();
    }

    /**
     * Sets additional JSON options as a string.
     * This JSON can override options loaded from XML or from component Java API if they overlap.
     * <br/>
     * Example of using {@code nativeJson}:
     * <pre>{@code
     *  <charts:chart height="100%" width="100%">
     *     <charts:title text="Title"/>
     *     <charts:nativeJson>
     *         <![CDATA[
     *                 {
     *                  "title": {
     *                     "subtext": 'Subtitle'
     *                  }
     *                 }
     *          ]]>
     *     </charts:nativeJson>
     * </charts:chart>
     * }</pre>
     *
     * @param json additional JSON options
     */
    public void setNativeJson(String json) {
        options.setNativeJson(json);
    }

    /**
     * @return additional JSON options as a {@link JsonValue}
     */
    public JsonValue getNativeJson() {
        return options.getNativeJson();
    }

    /**
     * Adds a series to the chart options.
     *
     * @param series series to add
     * @see SeriesType
     */
    public void addSeries(AbstractSeries<?> series) {
        options.addSeries(series);
    }

    /**
     * Removes an existing series from the chart options.
     *
     * @param series series to remove
     */
    public void removeSeries(AbstractSeries<?> series) {
        options.removeSeries(series);
    }

    /**
     * @return immutable list of added series
     */
    public List<AbstractSeries<?>> getSeries() {
        return options.getSeries();
    }

    /**
     * @param seriesId ID of the series to find
     * @param <S>      series class
     * @return the series with the provided ID if it exists, {@code null} otherwise
     */
    @Nullable
    public <S extends AbstractSeries<S>> S getSeriesOrNull(String seriesId) {
        if (Strings.isNullOrEmpty(seriesId)) {
            return null;
        }

        //noinspection unchecked
        return (S) options.getSeries().stream()
                .filter(s -> seriesId.equals(s.getId()))
                .findAny()
                .orElse(null);
    }

    /**
     * @param seriesId ID of the series to find
     * @param <S>      series class
     * @return the series with the provided ID
     * @throws IllegalArgumentException when chart doesn't contain a series with the provided ID
     */
    public <S extends AbstractSeries<S>> S getSeries(String seriesId) {
        S series = getSeriesOrNull(seriesId);
        if (series == null) {
            String message = String.format("Chart doesn't contain a series with the provided ID: '%s'", seriesId);
            throw new IllegalArgumentException(message);
        }

        return series;
    }

    /**
     * Adds a {@link XAxis} in cartesian(rectangular) coordinate to the chart options.
     *
     * @param axis axis to add
     */
    public void addXAxis(XAxis axis) {
        options.addXAxis(axis);
    }

    /**
     * Removes an existing {@link XAxis} from the chart options.
     *
     * @param axis axis to remove
     */
    public void removeAxis(XAxis axis) {
        options.removeXAxis(axis);
    }

    /**
     * @return immutable list of added XAxes
     */
    public List<XAxis> getXAxes() {
        return options.getXAxes();
    }

    /**
     * Adds a {@link YAxis} in cartesian(rectangular) coordinate to the chart options.
     *
     * @param axis axis to add
     */
    public void addYAxis(YAxis axis) {
        options.addYAxis(axis);
    }

    /**
     * Removes an existing {@link YAxis} from the chart options.
     *
     * @param axis axis to remove
     */
    public void removeYAxis(YAxis axis) {
        options.removeYAxis(axis);
    }

    /**
     * @return immutable list of added YAxes
     */
    public List<YAxis> getYAxes() {
        return options.getYAxes();
    }

    /**
     * Sets a {@link RadiusAxis} of polar coordinate to the chart options or replaces existing one.
     *
     * @param axis axis to set
     */
    public void setRadiusAxis(RadiusAxis axis) {
        options.setRadiusAxis(axis);
    }

    /**
     * @return current {@link RadiusAxis}
     */
    public RadiusAxis getRadiusAxis() {
        return options.getRadiusAxis();
    }

    /**
     * Sets a {@link AngleAxis} of polar coordinate to the chart options or replaces existing one.
     *
     * @param axis axis to set
     */
    public void setAngleAxis(AngleAxis axis) {
        options.setAngleAxis(axis);
    }

    /**
     * @return current {@link AngleAxis}
     */
    public AngleAxis getAngleAxis() {
        return options.getAngleAxis();
    }

    /**
     * Adds a {@link Grid} to the chart options. Grid options is used to draw a grid in rectangular coordinate.
     * In a single grid, at most two X and Y axes each is allowed. {@link LineSeries}, {@link BarSeries},
     * and {@link ScatterSeries}({@link EffectScatterSeries}) can be drawn in the grid.
     *
     * @param grid grid to add
     */
    public void addGrid(Grid grid) {
        options.addGrid(grid);
    }

    /**
     * Removes an existing {@link Grid} from the chart options.
     *
     * @param gird grid to remove
     */
    public void removeGrid(Grid gird) {
        options.removeGrid(gird);
    }

    /**
     * @return immutable list of added grids
     */
    public Collection<Grid> getGrids() {
        return options.getGrids();
    }

    /**
     * @return immutable list of added data zooms
     */
    public Collection<AbstractDataZoom<?>> getDataZoom() {
        return options.getDataZoom();
    }

    /**
     * Adds a {@link AbstractDataZoom} component witch used for zooming a specific area in the chart.
     *
     * @param dataZoom data zoom to add
     * @see SliderDataZoom
     * @see InsideDataZoom
     */
    public void addDataZoom(AbstractDataZoom<?> dataZoom) {
        options.addDataZoom(dataZoom);
    }

    /**
     * Removes an existing {@link AbstractDataZoom} from the chart options.
     *
     * @param dataZoom data zoom to remove
     */
    public void removeDataZoom(AbstractDataZoom<?> dataZoom) {
        options.removeDataZoom(dataZoom);
    }

    /**
     * @return title component of the chart
     */
    public Title getTitle() {
        return options.getTitle();
    }

    /**
     * Sets a {@link Title} to the chart options or replaces existing one.
     *
     * @param title title to add
     */
    public void setTitle(Title title) {
        options.setTitle(title);
    }

    /**
     * @return legend component of the chart
     */
    public AbstractLegend<?> getLegend() {
        return options.getLegend();
    }

    /**
     * Sets a {@link AbstractLegend} to the chart options or replaces existing one.
     * Legend component shows symbol, color and name of different series.
     * You can click legends to toggle displaying series in the chart.
     *
     * @param legend legend to add
     * @see Legend
     * @see ScrollableLegend
     */
    public void setLegend(AbstractLegend<?> legend) {
        options.setLegend(legend);
    }

    /**
     * @return global tooltip component of the chart
     */
    public Tooltip getTooltip() {
        return options.getTooltip();
    }

    /**
     * Sets a global {@link Tooltip} options of the chart or replaces existing one.
     * Tooltip can be configured on different places.
     *
     * @param tooltip global tooltip to set
     * @see Grid#setTooltip(InnerTooltip)
     * @see AbstractSeries#setTooltip(AbstractSeries.Tooltip)
     */
    public void setTooltip(Tooltip tooltip) {
        options.setTooltip(tooltip);
    }

    /**
     * @return toolbox of the chart
     */
    public Toolbox getToolbox() {
        return options.getToolbox();
    }

    /**
     * Sets a {@link Toolbox} to the chart options or replaces existing one.
     * Toolbox is a group of utility tools, which includes different features.
     *
     * @param toolbox toolbox to set
     */
    public void setToolbox(Toolbox toolbox) {
        options.setToolbox(toolbox);
    }

    /**
     * @return global axis pointer of the chart
     */
    public AxisPointer getAxisPointer() {
        return options.getAxisPointer();
    }

    /**
     * Sets a {@link AxisPointer} to the chart options or replaces existing one.
     * AxisPointer is a tool for displaying reference line and axis value under mouse pointer.
     *
     * @param axisPointer axis pointer to add
     */
    public void setAxisPointer(AxisPointer axisPointer) {
        options.setAxisPointer(axisPointer);
    }

    /**
     * @return polar options of the chart
     */
    public Polar getPolar() {
        return options.getPolar();
    }

    /**
     * Sets a {@link Polar} to the chart options or replaces existing one.
     * Polar coordinate can be used in {@link ScatterSeries} and {@link LineSeries}. Every polar coordinate
     * has an {@link AngleAxis} and a {@link RadiusAxis}.
     *
     * @param polar radar to add
     */
    public void setPolar(Polar polar) {
        options.setPolar(polar);
    }

    /**
     * @return radar options of the chart
     */
    public Radar getRadar() {
        return options.getRadar();
    }

    /**
     * Sets a {@link Radar} to the chart options or replaces existing one.
     * Radar chart coordinate is different from polar coordinate, in that every axis
     * indicator of the radar chart coordinate is an individual dimension.
     * {@link Radar.Indicator} are required to be displayed.
     *
     * @param radar radar to set
     */
    public void setRadar(Radar radar) {
        options.setRadar(radar);
    }

    /**
     * @return data set of the chart
     */
    public DataSet getDataSet() {
        return options.getDataSet();
    }

    /**
     * Sets a {@link DataSet} to the chart options or replaces existing one.
     * Data set is the main data provider for the chart. All series added to the chart will use the data set.
     * The only series that requires its own data set is the {@link GaugeSeries}.
     *
     * @param dataSet data set to set
     */
    public void setDataSet(DataSet dataSet) {
        options.setDataSet(dataSet);
    }

    /**
     * @return aria of the chart
     */
    public Aria getAria() {
        return options.getAria();
    }

    /**
     * Sets a {@link Aria} to the chart options or replaces existing one.
     * Aria is options for the Accessible Rich Internet Applications Suite,
     * which is dedicated to making web content and web applications accessible.
     *
     * @param aria aria to set
     */
    public void setAria(Aria aria) {
        options.setAria(aria);
    }

    /**
     * @return brush of the chart
     */
    public Brush getBrush() {
        return options.getBrush();
    }

    /**
     * Sets a {@link Brush} to the chart options or replaces existing one.
     * Brush is an area-selecting component, with which user can select part of data from a chart to display in detail.
     *
     * @param brush brush to set
     */
    public void setBrush(Brush brush) {
        options.setBrush(brush);
    }

    /**
     * @return immutable list of added visual maps
     */
    public List<AbstractVisualMap<?>> getVisualMap() {
        return options.getVisualMap();
    }

    /**
     * Adds a {@link AbstractVisualMap} to the chart options.
     * Visual map is a type of component for visual encoding, which maps the data to visual channels.
     *
     * @param visualMap visual map to add
     * @see PiecewiseVisualMap
     * @see ContinuousVisualMap
     */
    public void addVisualMap(AbstractVisualMap<?> visualMap) {
        options.addVisualMap(visualMap);
    }

    /**
     * Removes an existing {@link AbstractVisualMap} from the chart options.
     *
     * @param visualMap visual map to remove
     */
    public void removeVisualMap(AbstractVisualMap<?> visualMap) {
        options.removeVisualMap(visualMap);
    }

    /**
     * @return immutable list of colors in the charts color palette
     */
    public List<Color> getColorPalette() {
        return options.getColorPalette();
    }

    /**
     * Sets a color list of palette for the chart.
     *
     * @param palette colors to set
     */
    public void setColorPalette(Color... palette) {
        options.setColorPalette(palette);
    }

    /**
     * Adds a single color to the chart color palette.
     *
     * @param color color to add
     */
    public void addColorToPalette(Color color) {
        options.addColorToPalette(color);
    }

    /**
     * Clears the chart color palette of all colors.
     */
    public void clearColorPalette() {
        options.clearColorPalette();
    }

    /**
     * @return background color of the chart
     */
    public Color getBackgroundColor() {
        return options.getBackgroundColor();
    }

    /**
     * Sets a background color for the chart or replaces existing one. No background color by default.
     *
     * @param backgroundColor color to set
     */
    public void setBackgroundColor(Color backgroundColor) {
        options.setBackgroundColor(backgroundColor);
    }

    /**
     * @return global font style of the chart
     */
    public TextStyle getTextStyle() {
        return options.getTextStyle();
    }

    /**
     * Sets a global {@link TextStyle} for the chart or replaces existing one.
     * Text style can be configured on different places.
     *
     * @param textStyle global text style to set
     */
    public void setTextStyle(TextStyle textStyle) {
        options.setTextStyle(textStyle);
    }

    /**
     * @return {@code true} if animation if enabled, {@code false} otherwise
     */
    public Boolean getAnimation() {
        return options.getAnimation();
    }

    /**
     * Sets the animation of the chart. The animation is enabled by default.
     *
     * @param animation whether to enable the animation
     */
    public void setAnimation(Boolean animation) {
        options.setAnimation(animation);
    }

    /**
     * @return graphic number threshold to animation
     */
    public Integer getAnimationThreshold() {
        return options.getAnimationThreshold();
    }

    /**
     * Sets a graphic number threshold to animation.
     * Animation will be disabled when graphic number is larger than threshold.
     *
     * @param animationThreshold number threshold to animation
     */
    public void setAnimationThreshold(Integer animationThreshold) {
        options.setAnimationThreshold(animationThreshold);
    }

    /**
     * @return duration of the first draw animation in milliseconds
     */
    public Integer getAnimationDuration() {
        return options.getAnimationDuration();
    }

    /**
     * Sets a duration of the first draw animation.
     *
     * @param animationDuration animation duration to set in milliseconds
     */
    public void setAnimationDuration(Integer animationDuration) {
        options.setAnimationDuration(animationDuration);
    }

    /**
     * @return easing method used for the first draw animation
     */
    public String getAnimationEasing() {
        return options.getAnimationEasing();
    }

    /**
     * Sets an animation easing method used for the first draw animation.
     *
     * @param animationEasing easing method to use
     * @see <a href="https://echarts.apache.org/examples/en/editor.html?c=line-easing">Easing effect example</a>
     */
    public void setAnimationEasing(String animationEasing) {
        options.setAnimationEasing(animationEasing);
    }

    /**
     * @return delay before updating the first draw animation in milliseconds
     */
    public Integer getAnimationDelay() {
        return options.getAnimationDelay();
    }

    /**
     * Sets a delay before updating the first draw animation.
     *
     * @param animationDelay delay to set in milliseconds
     */
    public void setAnimationDelay(Integer animationDelay) {
        options.setAnimationDelay(animationDelay);
    }

    /**
     * @return time for animation to complete in milliseconds
     */
    public Integer getAnimationDurationUpdate() {
        return options.getAnimationDurationUpdate();
    }

    /**
     * Sets a time for animation to complete.
     *
     * @param animationDurationUpdate time to set in milliseconds
     */
    public void setAnimationDurationUpdate(Integer animationDurationUpdate) {
        options.setAnimationDurationUpdate(animationDurationUpdate);
    }

    /**
     * @return easing method used for update animation
     */
    public String getAnimationEasingUpdate() {
        return options.getAnimationEasingUpdate();
    }

    /**
     * Sets an animation easing method used for update animation.
     *
     * @param animationEasingUpdate easing method to use
     * @see <a href="https://echarts.apache.org/examples/en/editor.html?c=line-easing">Easing effect example</a>
     */
    public void setAnimationEasingUpdate(String animationEasingUpdate) {
        options.setAnimationEasingUpdate(animationEasingUpdate);
    }

    /**
     * @return delay before updating animation in milliseconds
     */
    public Integer getAnimationDelayUpdate() {
        return options.getAnimationDelayUpdate();
    }

    /**
     * Sets a delay before updating animation.
     *
     * @param animationDelayUpdate delay to set in milliseconds
     */
    public void setAnimationDelayUpdate(Integer animationDelayUpdate) {
        options.setAnimationDelayUpdate(animationDelayUpdate);
    }

    /**
     * @return state switching animation configurations
     */
    public ChartOptions.StateAnimation getStateAnimation() {
        return options.getStateAnimation();
    }

    /**
     * Sets a {@link ChartOptions.StateAnimation} for the chart.
     *
     * @param stateAnimation configuration to set
     */
    public void setStateAnimation(ChartOptions.StateAnimation stateAnimation) {
        options.setStateAnimation(stateAnimation);
    }

    /**
     * @return the type of compositing operation to apply when drawing a new shape
     */
    public ChartOptions.BlendMode getBlendMode() {
        return options.getBlendMode();
    }

    /**
     * Sets the type of compositing operation to apply when drawing a new shape.
     * Blend mode is {@code source-over} by default.
     *
     * @param blendMode blend mode to set
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/CanvasRenderingContext2D/globalCompositeOperation">CanvasRenderingContext2D.globalCompositeOperation [MDN]</a>
     */
    public void setBlendMode(ChartOptions.BlendMode blendMode) {
        options.setBlendMode(blendMode);
    }

    /**
     * @return the number of elements when exceeded, a separate hover layer is used to render hovered elements
     */
    public Integer getHoverLayerThreshold() {
        return options.getHoverLayerThreshold();
    }

    /**
     * Sets the number of elements when exceeded, a separate hover layer is used to render hovered elements.
     *
     * @param hoverLayerThreshold number of elements to set
     */
    public void setHoverLayerThreshold(Integer hoverLayerThreshold) {
        options.setHoverLayerThreshold(hoverLayerThreshold);
    }

    /**
     * @return {@code true} if UTC is used in display, {@code false} otherwise
     */
    public Boolean getUseUtc() {
        return options.getUseUtc();
    }

    /**
     * Sets the use of UTC in display.
     * <ul>
     *     <li>
     *         {@code true}: when {@code axis.type} is {@code 'time'}, ticks is determined according to UTC,
     *         and {@code axisLabel} and {@code tooltip} use UTC by default.
     *     </li>
     *     <li>
     *         {@code false}: when {@code axis.type} is {@code 'time'}, ticks is determined according to local time,
     *         and {@code axisLabel} and {@code tooltip} use local time by default.
     *     </li>
     * </ul>
     *
     * @param useUtc whether to use UTC in display
     */
    public void setUseUtc(Boolean useUtc) {
        options.setUseUtc(useUtc);
    }

    protected void initChartOptionsChangeListener() {
        options.setChartObjectChangeListener(this::onChartOptionsChange);
    }

    protected void onChartOptionsChange(ChartObservableObject.ObjectChangeEvent event) {
        requestUpdateChartOptions();
    }

    protected void requestUpdateChartOptions() {
        // Do not call if it's still updating
        if (synchronizeChartOptionsExecution != null) {
            return;
        }

        getUI().ifPresent(ui ->
                synchronizeChartOptionsExecution = ui.beforeClientResponse(this, this::performUpdateChartOptions));
    }

    protected void requestUpdateChartDataSet() {
        // Do not call if it's still updating
        if (synchronizeChartDataExecution != null) {
            return;
        }

        getUI().ifPresent(ui ->
                synchronizeChartDataExecution = ui.beforeClientResponse(this, this::performUpdateChartData));
    }

    protected <T extends DataItem> void requestIncrementalUpdateChartDataSet(ChartItems.ItemSetChangeEvent<T> event) {
        if (options.isDirtyInDepth()) {
            // will be updated later
            return;
        }

        if (ChartItems.DataChangeOperation.REFRESH.equals(event.getOperation())) {
            requestUpdateChartDataSet();
            return;
        }

        addChangedDataItems(event.getOperation(), event.getItems());

        if (synchronizeChartIncrementalUpdateDataExecution != null) {
            return;
        }

        getUI().ifPresent(ui ->
                synchronizeChartIncrementalUpdateDataExecution = ui.beforeClientResponse(this,
                        this::performIncrementalUpdateChartDataSet));
    }

    protected void performUpdateChartOptions(ExecutionContext context) {
        JsonObject resultJson = new JreJsonFactory().createObject();

        if (options.isDirtyInDepth()) {
            JsonValue optionsJson = serializer.serialize(options);
            resultJson.put("options", optionsJson);
        }

        JsonValue nativeJson = getNativeJson();
        if (nativeJson != null) {
            resultJson.put("nativeJson", nativeJson);
        }

        getElement().callJsFunction("_updateChart", resultJson);

        synchronizeChartOptionsExecution = null;
    }

    protected void performUpdateChartData(ExecutionContext context) {
        JsonObject resultJson = new JreJsonFactory().createObject();

        // drop all keys before redraw
        dataItemKeys.removeAll();

        JsonValue dataJson = serializer.serializeDataSet(options.getDataSet());
        resultJson.put("dataset", dataJson);

        getElement().callJsFunction("_updateChartDataset", resultJson);

        synchronizeChartDataExecution = null;
        changedItems.clear();
    }

    protected void performIncrementalUpdateChartDataSet(ExecutionContext context) {
        JsonObject resultJson = new JreJsonFactory().createObject();

        JsonValue changedItemsJson = serializer.serializeChangedItems(changedItems.get(getDataSet()));
        resultJson.put("changedItems", changedItemsJson);

        for (ChartIncrementalChanges<?> changes : changedItems.values()) {
            Collection<? extends DataItem> removedItems = changes.getRemovedItems();
            if (removedItems != null) {
                removedItems.forEach(dataItemKeys::remove);
            }
        }

        getElement().callJsFunction("_incrementalUpdateChartDataset", resultJson);

        synchronizeChartIncrementalUpdateDataExecution = null;
        changedItems.clear();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        // If chart is reattached, mark all objects as dirty to render
        // them in client-side.
        options.markAsDirtyInDepth();

        requestUpdateChartOptions();
        requestUpdateChartDataSet();
    }

    protected <T extends DataItem> void addChangedDataItems(ChartItems.DataChangeOperation operation,
                                                            Collection<T> items) {
        //noinspection unchecked
        ChartIncrementalChanges<T> dataSetChanges =
                (ChartIncrementalChanges<T>) changedItems.computeIfAbsent(getDataSet(),
                        k -> new ChartIncrementalChanges<>());

        dataSetChanges.setSource(getDataSet().getSource());

        switch (operation) {
            case ADD -> dataSetChanges.addAddedItems(items);
            case REMOVE -> dataSetChanges.addRemovedItems(items);
            case UPDATE -> dataSetChanges.addUpdatedItems(items);
            case REFRESH -> { /* do nothing */ }
        }
    }

    protected ChartOptions createChartOptions() {
        return new ChartOptions(this);
    }

    protected JmixChartSerializer createSerializer() {
        return new JmixChartSerializer();
    }
}
