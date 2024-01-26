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
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.data.provider.KeyMapper;
import com.vaadin.flow.internal.ExecutionContext;
import com.vaadin.flow.internal.StateTree;
import com.vaadin.flow.shared.Registration;
import elemental.json.JsonObject;
import elemental.json.JsonValue;
import elemental.json.impl.JreJsonFactory;
import io.jmix.chartsflowui.kit.component.event.*;
import io.jmix.chartsflowui.kit.component.model.*;
import io.jmix.chartsflowui.kit.component.model.axis.*;
import io.jmix.chartsflowui.kit.component.model.datazoom.AbstractDataZoom;
import io.jmix.chartsflowui.kit.component.model.legend.AbstractLegend;
import io.jmix.chartsflowui.kit.component.model.series.AbstractSeries;
import io.jmix.chartsflowui.kit.component.model.shared.Color;
import io.jmix.chartsflowui.kit.component.model.shared.TextStyle;
import io.jmix.chartsflowui.kit.component.model.toolbox.Toolbox;
import io.jmix.chartsflowui.kit.component.model.visualMap.AbstractVisualMap;
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

    protected Map<String, Registration> eventRegistrations = new HashMap<>();

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

    protected Registration getRemovalCallback(String eventName, Class clazz) {
        return () -> {
            eventRegistrations.get(eventName).remove();
            if (!getEventBus().hasListener(clazz)
                    && eventRegistrations.get(eventName) != null) {
                eventRegistrations.get(eventName).remove();
                eventRegistrations.remove(eventName);
            }
        };
    }

    public Registration addClickEventListener(ComponentEventListener<JmixChartClickEvent> listener) {
        eventRegistrations.put(JmixChartClickEvent.EVENT_NAME, getEventBus().addListener(JmixChartClickEvent.class, listener));
        return getRemovalCallback(JmixChartClickEvent.EVENT_NAME, JmixChartClickEvent.class);
    }

    public Registration addDoubleClickEventListener(ComponentEventListener<JmixChartDoubleClickEvent> listener) {
        eventRegistrations.put(JmixChartDoubleClickEvent.EVENT_NAME, getEventBus().addListener(JmixChartDoubleClickEvent.class, listener));
        return getRemovalCallback(JmixChartDoubleClickEvent.EVENT_NAME, JmixChartDoubleClickEvent.class);
    }

    public Registration addMouseDownEventListener(ComponentEventListener<JmixChartMouseDownEvent> listener) {
        eventRegistrations.put(JmixChartMouseDownEvent.EVENT_NAME, getEventBus().addListener(JmixChartMouseDownEvent.class, listener));
        return getRemovalCallback(JmixChartMouseDownEvent.EVENT_NAME, JmixChartMouseDownEvent.class);
    }

    public Registration addMouseMoveEventListener(ComponentEventListener<JmixChartMouseMoveEvent> listener) {
        eventRegistrations.put(JmixChartMouseMoveEvent.EVENT_NAME, getEventBus().addListener(JmixChartMouseMoveEvent.class, listener));
        return getRemovalCallback(JmixChartMouseMoveEvent.EVENT_NAME, JmixChartMouseMoveEvent.class);
    }

    public Registration addMouseOutEventListener(ComponentEventListener<JmixChartMouseOutEvent> listener) {
        eventRegistrations.put(JmixChartMouseOutEvent.EVENT_NAME, getEventBus().addListener(JmixChartMouseOutEvent.class, listener));
        return getRemovalCallback(JmixChartMouseOutEvent.EVENT_NAME, JmixChartMouseOutEvent.class);
    }

    public Registration addMouseOverEventListener(ComponentEventListener<JmixChartMouseOverEvent> listener) {
        eventRegistrations.put(JmixChartMouseOverEvent.EVENT_NAME, getEventBus().addListener(JmixChartMouseOverEvent.class, listener));
        return getRemovalCallback(JmixChartMouseOverEvent.EVENT_NAME, JmixChartMouseOverEvent.class);
    }

    public Registration addMouseUpEventListener(ComponentEventListener<JmixChartMouseUpEvent> listener) {
        eventRegistrations.put(JmixChartMouseUpEvent.EVENT_NAME, getEventBus().addListener(JmixChartMouseUpEvent.class, listener));
        return getRemovalCallback(JmixChartMouseUpEvent.EVENT_NAME, JmixChartMouseUpEvent.class);
    }

    public Registration addGlobalOutEventListener(ComponentEventListener<JmixChartGlobalOutEvent> listener) {
        eventRegistrations.put(JmixChartMouseOverEvent.EVENT_NAME, getEventBus().addListener(JmixChartGlobalOutEvent.class, listener));
        return getRemovalCallback(JmixChartGlobalOutEvent.EVENT_NAME, JmixChartGlobalOutEvent.class);
    }

    public Registration addHighlightEventListener(ComponentEventListener<JmixChartHighlightEvent> listener) {
        eventRegistrations.put(JmixChartHighlightEvent.EVENT_NAME, getEventBus().addListener(JmixChartHighlightEvent.class, listener));
        return getRemovalCallback(JmixChartHighlightEvent.EVENT_NAME, JmixChartHighlightEvent.class);
    }

    public Registration addDownplayEventListener(ComponentEventListener<JmixChartDownplayEvent> listener) {
        eventRegistrations.put(JmixChartDownplayEvent.EVENT_NAME, getEventBus().addListener(JmixChartDownplayEvent.class, listener));
        return getRemovalCallback(JmixChartDownplayEvent.EVENT_NAME, JmixChartDownplayEvent.class);
    }

    public Registration addSelectChangedEventListener(ComponentEventListener<JmixChartSelectChangedEvent> listener) {
        eventRegistrations.put(JmixChartSelectChangedEvent.EVENT_NAME, getEventBus().addListener(JmixChartSelectChangedEvent.class, listener));
        return getRemovalCallback(JmixChartSelectChangedEvent.EVENT_NAME, JmixChartSelectChangedEvent.class);
    }
    public Registration addLegendSelectChangedEventListener(ComponentEventListener<JmixChartLegendSelectChangedEvent> listener) {
        eventRegistrations.put(JmixChartLegendSelectChangedEvent.EVENT_NAME, getEventBus().addListener(JmixChartLegendSelectChangedEvent.class, listener));
        return getRemovalCallback(JmixChartLegendSelectChangedEvent.EVENT_NAME, JmixChartLegendSelectChangedEvent.class);
    }
    public Registration addLegendSelectctedEventListener(ComponentEventListener<JmixChartLegendSelectedEvent> listener) {
        eventRegistrations.put(JmixChartLegendSelectedEvent.EVENT_NAME, getEventBus().addListener(JmixChartLegendSelectedEvent.class, listener));
        return getRemovalCallback(JmixChartLegendSelectedEvent.EVENT_NAME, JmixChartLegendSelectedEvent.class);
    }
    public Registration addLegendUnselectctedEventListener(ComponentEventListener<JmixChartLegendUnselectedEvent> listener) {
        eventRegistrations.put(JmixChartLegendUnselectedEvent.EVENT_NAME, getEventBus().addListener(JmixChartLegendUnselectedEvent.class, listener));
        return getRemovalCallback(JmixChartLegendUnselectedEvent.EVENT_NAME, JmixChartLegendUnselectedEvent.class);
    }
    public Registration addLegendSelectctAllEventListener(ComponentEventListener<JmixChartLegendSelectAllEvent> listener) {
        eventRegistrations.put(JmixChartLegendSelectAllEvent.EVENT_NAME, getEventBus().addListener(JmixChartLegendSelectAllEvent.class, listener));
        return getRemovalCallback(JmixChartLegendSelectAllEvent.EVENT_NAME, JmixChartLegendSelectAllEvent.class);
    }
    public Registration addLegendInverseDelectEventListener(ComponentEventListener<JmixChartLegendInverseSelectEvent> listener) {
        eventRegistrations.put(JmixChartLegendInverseSelectEvent.EVENT_NAME, getEventBus().addListener(JmixChartLegendInverseSelectEvent.class, listener));
        return getRemovalCallback(JmixChartLegendInverseSelectEvent.EVENT_NAME, JmixChartLegendInverseSelectEvent.class);
    }
    public Registration addLegendScrollEventListener(ComponentEventListener<JmixChartLegendScrollEvent> listener) {
        eventRegistrations.put(JmixChartLegendScrollEvent.EVENT_NAME, getEventBus().addListener(JmixChartLegendScrollEvent.class, listener));
        return getRemovalCallback(JmixChartLegendScrollEvent.EVENT_NAME, JmixChartLegendScrollEvent.class);
    }
    public Registration addDataZoomEventListener(ComponentEventListener<JmixChartDataZoomEvent> listener) {
        eventRegistrations.put(JmixChartDataZoomEvent.EVENT_NAME, getEventBus().addListener(JmixChartDataZoomEvent.class, listener));
        return getRemovalCallback(JmixChartDataZoomEvent.EVENT_NAME, JmixChartDataZoomEvent.class);
    }
    public Registration addDataRangeSelectedEventListener(ComponentEventListener<JmixChartDataRangeSelectedEvent> listener) {
        eventRegistrations.put(JmixChartDataRangeSelectedEvent.EVENT_NAME, getEventBus().addListener(JmixChartDataRangeSelectedEvent.class, listener));
        return getRemovalCallback(JmixChartDataRangeSelectedEvent.EVENT_NAME, JmixChartDataRangeSelectedEvent.class);
    }
    public Registration addGraphRoamEventListener(ComponentEventListener<JmixChartGraphRoamEvent> listener) {
        eventRegistrations.put(JmixChartGraphRoamEvent.EVENT_NAME, getEventBus().addListener(JmixChartGraphRoamEvent.class, listener));
        return getRemovalCallback(JmixChartGraphRoamEvent.EVENT_NAME, JmixChartGraphRoamEvent.class);
    }
    public Registration addGeoRoamEventListener(ComponentEventListener<JmixChartGeoRoamEvent> listener) {
        eventRegistrations.put(JmixChartGeoRoamEvent.EVENT_NAME, getEventBus().addListener(JmixChartGeoRoamEvent.class, listener));
        return getRemovalCallback(JmixChartGeoRoamEvent.EVENT_NAME, JmixChartGeoRoamEvent.class);
    }
    public Registration addTreeRoamEventListener(ComponentEventListener<JmixChartTreeRoamEvent> listener) {
        eventRegistrations.put(JmixChartTreeRoamEvent.EVENT_NAME, getEventBus().addListener(JmixChartTreeRoamEvent.class, listener));
        return getRemovalCallback(JmixChartTreeRoamEvent.EVENT_NAME, JmixChartTreeRoamEvent.class);
    }
    public Registration addTimelineChangedEventListener(ComponentEventListener<JmixChartTimelineChangedEvent> listener) {
        eventRegistrations.put(JmixChartTimelineChangedEvent.EVENT_NAME, getEventBus().addListener(JmixChartTimelineChangedEvent.class, listener));
        return getRemovalCallback(JmixChartTimelineChangedEvent.EVENT_NAME, JmixChartTimelineChangedEvent.class);
    }
    public Registration addTimelinePlayChangedEventListener(ComponentEventListener<JmixChartTimelinePlayChangedEvent> listener) {
        eventRegistrations.put(JmixChartTimelinePlayChangedEvent.EVENT_NAME, getEventBus().addListener(JmixChartTimelinePlayChangedEvent.class, listener));
        return getRemovalCallback(JmixChartTimelinePlayChangedEvent.EVENT_NAME, JmixChartTimelinePlayChangedEvent.class);
    }
    public Registration addRestoreEventListener(ComponentEventListener<JmixChartRestoreEvent> listener) {
        eventRegistrations.put(JmixChartRestoreEvent.EVENT_NAME, getEventBus().addListener(JmixChartRestoreEvent.class, listener));
        return getRemovalCallback(JmixChartRestoreEvent.EVENT_NAME, JmixChartRestoreEvent.class);
    }
    public Registration addDataViewChangedEventListener(ComponentEventListener<JmixChartDataViewChangedEvent> listener) {
        eventRegistrations.put(JmixChartDataViewChangedEvent.EVENT_NAME, getEventBus().addListener(JmixChartDataViewChangedEvent.class, listener));
        return getRemovalCallback(JmixChartDataViewChangedEvent.EVENT_NAME, JmixChartDataViewChangedEvent.class);
    }
    public Registration addMagicTypeChangedEventListener(ComponentEventListener<JmixChartMagicTypeChangedEvent> listener) {
        eventRegistrations.put(JmixChartMagicTypeChangedEvent.EVENT_NAME, getEventBus().addListener(JmixChartMagicTypeChangedEvent.class, listener));
        return getRemovalCallback(JmixChartMagicTypeChangedEvent.EVENT_NAME, JmixChartMagicTypeChangedEvent.class);
    }
    public Registration addGeoSelectChangedEventListener(ComponentEventListener<JmixChartGeoSelectChangedEvent> listener) {
        eventRegistrations.put(JmixChartGeoSelectChangedEvent.EVENT_NAME, getEventBus().addListener(JmixChartGeoSelectChangedEvent.class, listener));
        return getRemovalCallback(JmixChartGeoSelectChangedEvent.EVENT_NAME, JmixChartGeoSelectChangedEvent.class);
    }
    public Registration addGeoSelectedEventListener(ComponentEventListener<JmixChartGeoSelectedEvent> listener) {
        eventRegistrations.put(JmixChartGeoSelectedEvent.EVENT_NAME, getEventBus().addListener(JmixChartGeoSelectedEvent.class, listener));
        return getRemovalCallback(JmixChartGeoSelectedEvent.EVENT_NAME, JmixChartGeoSelectedEvent.class);
    }
    public Registration addGeoUnselectedEventListener(ComponentEventListener<JmixChartGeoUnselectedEvent> listener) {
        eventRegistrations.put(JmixChartGeoUnselectedEvent.EVENT_NAME, getEventBus().addListener(JmixChartGeoUnselectedEvent.class, listener));
        return getRemovalCallback(JmixChartGeoUnselectedEvent.EVENT_NAME, JmixChartGeoUnselectedEvent.class);
    }
    public Registration addAxisAreaSelectedEventListener(ComponentEventListener<JmixChartAxisAreaSelectedEvent> listener) {
        eventRegistrations.put(JmixChartAxisAreaSelectedEvent.EVENT_NAME, getEventBus().addListener(JmixChartAxisAreaSelectedEvent.class, listener));
        return getRemovalCallback(JmixChartAxisAreaSelectedEvent.EVENT_NAME, JmixChartAxisAreaSelectedEvent.class);
    }
    public Registration addBrushEventListener(ComponentEventListener<JmixChartBrushEvent> listener) {
        eventRegistrations.put(JmixChartBrushEvent.EVENT_NAME, getEventBus().addListener(JmixChartBrushEvent.class, listener));
        return getRemovalCallback(JmixChartBrushEvent.EVENT_NAME, JmixChartBrushEvent.class);
    }
    public Registration addBrushEndEventListener(ComponentEventListener<JmixChartBrushEndEvent> listener) {
        eventRegistrations.put(JmixChartBrushEndEvent.EVENT_NAME, getEventBus().addListener(JmixChartBrushEndEvent.class, listener));
        return getRemovalCallback(JmixChartBrushEndEvent.EVENT_NAME, JmixChartBrushEndEvent.class);
    }
    public Registration addBrushSelectedEventListener(ComponentEventListener<JmixChartBrushSelectedEvent> listener) {
        eventRegistrations.put(JmixChartBrushSelectedEvent.EVENT_NAME, getEventBus().addListener(JmixChartBrushSelectedEvent.class, listener));
        return getRemovalCallback(JmixChartBrushSelectedEvent.EVENT_NAME, JmixChartBrushSelectedEvent.class);
    }
    public Registration addGlobalCursorTakenEventListener(ComponentEventListener<JmixChartGlobalCursorTakenEvent> listener) {
        eventRegistrations.put(JmixChartGlobalCursorTakenEvent.EVENT_NAME, getEventBus().addListener(JmixChartGlobalCursorTakenEvent.class, listener));
        return getRemovalCallback(JmixChartGlobalCursorTakenEvent.EVENT_NAME, JmixChartGlobalCursorTakenEvent.class);
    }
    public Registration addRenderedEventListener(ComponentEventListener<JmixChartRenderedEvent> listener) {
        eventRegistrations.put(JmixChartRenderedEvent.EVENT_NAME, getEventBus().addListener(JmixChartRenderedEvent.class, listener));
        return getRemovalCallback(JmixChartRenderedEvent.EVENT_NAME, JmixChartRenderedEvent.class);
    }
    public Registration addFinishedEventListener(ComponentEventListener<JmixChartFinishedEvent> listener) {
        eventRegistrations.put(JmixChartFinishedEvent.EVENT_NAME, getEventBus().addListener(JmixChartFinishedEvent.class, listener));
        return getRemovalCallback(JmixChartFinishedEvent.EVENT_NAME, JmixChartFinishedEvent.class);
    }

    public void setNativeJson(String json) {
        options.setNativeJson(json);
    }

    public JsonValue getNativeJson() {
        return options.getNativeJson();
    }

    public void addSeries(AbstractSeries<?> series) {
        options.addSeries(series);
    }

    public void removeSeries(AbstractSeries<?> series) {
        options.removeSeries(series);
    }

    public Collection<AbstractSeries<?>> getSeries() {
        return options.getSeries();
    }

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

    public <S extends AbstractSeries<S>> S getSeries(String seriesId) {
        S series = getSeriesOrNull(seriesId);
        if (series == null) {
            String message = String.format("Chart doesn't contain a series with the provided ID: '%s'", seriesId);
            throw new IllegalArgumentException(message);
        }

        return series;
    }

    public void addXAxis(XAxis axis) {
        options.addXAxis(axis);
    }

    public void removeAxis(XAxis axis) {
        options.removeXAxis(axis);
    }

    public List<XAxis> getXAxes() {
        return options.getXAxes();
    }

    public void addYAxis(YAxis axis) {
        options.addYAxis(axis);
    }

    public void removeYAxis(YAxis axis) {
        options.removeYAxis(axis);
    }

    public List<YAxis> getYAxes() {
        return options.getYAxes();
    }

    public void setRadiusAxis(RadiusAxis axis) {
        options.setRadiusAxis(axis);
    }

    public RadiusAxis getRadiusAxis() {
        return options.getRadiusAxis();
    }

    public void setAngleAxis(AngleAxis axis) {
        options.setAngleAxis(axis);
    }

    public AngleAxis getAngleAxis() {
        return options.getAngleAxis();
    }

    public void addGrid(Grid grid) {
        options.addGrid(grid);
    }

    public void removeGrid(Grid gird) {
        options.removeGrid(gird);
    }

    public Collection<Grid> getGrids() {
        return options.getGrids();
    }

    public Collection<AbstractDataZoom<?>> getDataZoom() {
        return options.getDataZoom();
    }

    public void addDataZoom(AbstractDataZoom<?> dataZoom) {
        options.addDataZoom(dataZoom);
    }

    public void removeDataZoom(AbstractDataZoom<?> dataZoom) {
        options.removeDataZoom(dataZoom);
    }

    public Title getTitle() {
        return options.getTitle();
    }

    public void setTitle(Title title) {
        options.setTitle(title);
    }

    public AbstractLegend<?> getLegend() {
        return options.getLegend();
    }

    public void setLegend(AbstractLegend<?> legend) {
        options.setLegend(legend);
    }

    public Tooltip getTooltip() {
        return options.getTooltip();
    }

    public void setTooltip(Tooltip tooltip) {
        options.setTooltip(tooltip);
    }

    public Toolbox getToolbox() {
        return options.getToolbox();
    }

    public void setToolbox(Toolbox toolbox) {
        options.setToolbox(toolbox);
    }

    public AxisPointer getAxisPointer() {
        return options.getAxisPointer();
    }

    public void setAxisPointer(AxisPointer axisPointer) {
        options.setAxisPointer(axisPointer);
    }

    public Polar getPolar() {
        return options.getPolar();
    }

    public void setPolar(Polar polar) {
        options.setPolar(polar);
    }

    public Radar getRadar() {
        return options.getRadar();
    }

    public void setRadar(Radar radar) {
        options.setRadar(radar);
    }

    public DataSet getDataSet() {
        return options.getDataSet();
    }

    public void setDataSet(DataSet dataSet) {
        options.setDataSet(dataSet);
    }

    public Aria getAria() {
        return options.getAria();
    }

    public void setAria(Aria aria) {
        options.setAria(aria);
    }

    public Brush getBrush() {
        return options.getBrush();
    }

    public void setBrush(Brush brush) {
        options.setBrush(brush);
    }

    public List<AbstractVisualMap<?>> getVisualMap() {
        return options.getVisualMap();
    }

    public void addVisualMap(AbstractVisualMap<?> visualMap) {
        options.addVisualMap(visualMap);
    }

    public void removeVisualMap(AbstractVisualMap<?> visualMap) {
        options.removeVisualMap(visualMap);
    }

    public List<Color> getColorPalette() {
        return options.getColorPalette();
    }

    public void setColorPalette(Color... palette) {
        options.setColorPalette(palette);
    }

    public void addColorToPalette(Color color) {
        options.addColorToPalette(color);
    }

    public void clearColorPalette() {
        options.clearColorPalette();
    }

    public Color getBackgroundColor() {
        return options.getBackgroundColor();
    }

    public void setBackgroundColor(Color backgroundColor) {
        options.setBackgroundColor(backgroundColor);
    }

    public TextStyle getTextStyle() {
        return options.getTextStyle();
    }

    public void setTextStyle(TextStyle textStyle) {
        options.setTextStyle(textStyle);
    }

    public Boolean getAnimation() {
        return options.getAnimation();
    }

    public void setAnimation(Boolean animation) {
        options.setAnimation(animation);
    }

    public Integer getAnimationThreshold() {
        return options.getAnimationThreshold();
    }

    public void setAnimationThreshold(Integer animationThreshold) {
        options.setAnimationThreshold(animationThreshold);
    }

    public Integer getAnimationDuration() {
        return options.getAnimationDuration();
    }

    public void setAnimationDuration(Integer animationDuration) {
        options.setAnimationDuration(animationDuration);
    }

    public String getAnimationEasing() {
        return options.getAnimationEasing();
    }

    public void setAnimationEasing(String animationEasing) {
        options.setAnimationEasing(animationEasing);
    }

    public Integer getAnimationDelay() {
        return options.getAnimationDelay();
    }

    public void setAnimationDelay(Integer animationDelay) {
        options.setAnimationDelay(animationDelay);
    }

    public Integer getAnimationDurationUpdate() {
        return options.getAnimationDurationUpdate();
    }

    public void setAnimationDurationUpdate(Integer animationDurationUpdate) {
        options.setAnimationDurationUpdate(animationDurationUpdate);
    }

    public String getAnimationEasingUpdate() {
        return options.getAnimationEasingUpdate();
    }

    public void setAnimationEasingUpdate(String animationEasingUpdate) {
        options.setAnimationEasingUpdate(animationEasingUpdate);
    }

    public Integer getAnimationDelayUpdate() {
        return options.getAnimationDelayUpdate();
    }

    public void setAnimationDelayUpdate(Integer animationDelayUpdate) {
        options.setAnimationDelayUpdate(animationDelayUpdate);
    }

    public ChartOptions.StateAnimation getStateAnimation() {
        return options.getStateAnimation();
    }

    public void setStateAnimation(ChartOptions.StateAnimation stateAnimation) {
        options.setStateAnimation(stateAnimation);
    }

    public ChartOptions.BlendMode getBlendMode() {
        return options.getBlendMode();
    }

    public void setBlendMode(ChartOptions.BlendMode blendMode) {
        options.setBlendMode(blendMode);
    }

    public Integer getHoverLayerThreshold() {
        return options.getHoverLayerThreshold();
    }

    public void setHoverLayerThreshold(Integer hoverLayerThreshold) {
        options.setHoverLayerThreshold(hoverLayerThreshold);
    }

    public Boolean getUseUtc() {
        return options.getUseUtc();
    }

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
