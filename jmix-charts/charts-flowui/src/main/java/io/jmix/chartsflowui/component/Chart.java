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

package io.jmix.chartsflowui.component;

import io.jmix.chartsflowui.component.serialization.ChartSerializer;
import io.jmix.chartsflowui.kit.component.JmixChart;
import io.jmix.chartsflowui.kit.component.model.*;
import io.jmix.chartsflowui.kit.component.model.axis.*;
import io.jmix.chartsflowui.kit.component.model.datazoom.AbstractDataZoom;
import io.jmix.chartsflowui.kit.component.model.legend.AbstractLegend;
import io.jmix.chartsflowui.kit.component.model.series.AbstractSeries;
import io.jmix.chartsflowui.kit.component.model.shared.Color;
import io.jmix.chartsflowui.kit.component.model.shared.TextStyle;
import io.jmix.chartsflowui.kit.component.model.toolbox.Toolbox;
import io.jmix.chartsflowui.kit.component.model.visualMap.AbstractVisualMap;
import io.jmix.chartsflowui.kit.component.serialization.JmixChartSerializer;
import io.jmix.chartsflowui.kit.data.chart.DataItem;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.function.Function;

/**
 *
 */
public class Chart extends JmixChart
        implements ApplicationContextAware, InitializingBean {

    protected ApplicationContext applicationContext;
    protected Function<DataItem, String> dataItemKeyMapper;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    protected void initComponent() {
        options = createChartOptions();
    }

    @Override
    public void afterPropertiesSet() {
        initDataItemKeyMapper();
        serializer = createSerializer();
        initChartOptionsChangeListener();
    }

    public Chart withTitle(Title title) {
        setTitle(title);
        return this;
    }

    public Chart withLegend(AbstractLegend<?> legend) {
        setLegend(legend);
        return this;
    }

    public Chart withGrid(Grid grid) {
        addGrid(grid);
        return this;
    }

    public Chart withXAxis(XAxis axis) {
        addXAxis(axis);
        return this;
    }

    public Chart withYAxis(YAxis axis) {
        addYAxis(axis);
        return this;
    }

    public Chart withPolar(Polar polar) {
        setPolar(polar);
        return this;
    }

    public Chart withRadiusAxis(RadiusAxis axis) {
        setRadiusAxis(axis);
        return this;
    }

    public Chart withAngleAxis(AngleAxis axis) {
        setAngleAxis(axis);
        return this;
    }

    public Chart withRadar(Radar radar) {
        setRadar(radar);
        return this;
    }

    public Chart withDataZoom(AbstractDataZoom<?> dataZoom) {
        addDataZoom(dataZoom);
        return this;
    }

    public Chart withTooltip(Tooltip tooltip) {
        setTooltip(tooltip);
        return this;
    }

    public Chart withAxisPointer(AxisPointer axisPointer) {
        setAxisPointer(axisPointer);
        return this;
    }

    public Chart withToolbox(Toolbox toolbox) {
        setToolbox(toolbox);
        return this;
    }

    public Chart withDataSet(DataSet dataSet) {
        setDataSet(dataSet);
        return this;
    }

    public Chart withAria(Aria aria) {
        setAria(aria);
        return this;
    }

    public Chart withBrush(Brush brush) {
        setBrush(brush);
        return this;
    }

    public Chart withVisualMap(AbstractVisualMap<?> visualMap) {
        addVisualMap(visualMap);
        return this;
    }

    public Chart withSeries(AbstractSeries<?>... series) {
        for (AbstractSeries<?> abstractSeries : series) {
            addSeries(abstractSeries);
        }

        return this;
    }

    public Chart withColorToPalette(Color... color) {
        setColorPalette(color);
        return this;
    }

    public Chart withBackgroundColor(Color backgroundColor) {
        setBackgroundColor(backgroundColor);
        return this;
    }

    public Chart withTextStyle(TextStyle textStyle) {
        setTextStyle(textStyle);
        return this;
    }

    public Chart withAnimation(Boolean animation) {
        setAnimation(animation);
        return this;
    }

    public Chart withAnimationThreshold(Integer animationThreshold) {
        setAnimationThreshold(animationThreshold);
        return this;
    }

    public Chart withAnimationDuration(Integer animationDuration) {
        setAnimationDuration(animationDuration);
        return this;
    }

    public Chart withAnimationEasing(String animationEasing) {
        setAnimationEasing(animationEasing);
        return this;
    }

    public Chart withAnimationDelay(Integer animationDelay) {
        setAnimationDelay(animationDelay);
        return this;
    }

    public Chart withAnimationDurationUpdate(Integer animationDurationUpdate) {
        setAnimationDurationUpdate(animationDurationUpdate);
        return this;
    }

    public Chart withAnimationEasingUpdate(String animationEasingUpdate) {
        setAnimationEasingUpdate(animationEasingUpdate);
        return this;
    }

    public Chart withAnimationDelayUpdate(Integer animationDelayUpdate) {
        setAnimationDelayUpdate(animationDelayUpdate);
        return this;
    }

    public Chart withStateAnimation(ChartOptions.StateAnimation stateAnimation) {
        setStateAnimation(stateAnimation);
        return this;
    }

    public Chart withBlendMode(ChartOptions.BlendMode blendMode) {
        setBlendMode(blendMode);
        return this;
    }

    public Chart withHoverLayerThreshold(Integer hoverLayerThreshold) {
        setHoverLayerThreshold(hoverLayerThreshold);
        return this;
    }

    public Chart withUseUtc(Boolean useUtc) {
        setUseUtc(useUtc);
        return this;
    }

    public Chart withNativeJson(String nativeJson) {
        setNativeJson(nativeJson);
        return this;
    }

    protected void initDataItemKeyMapper() {
        dataItemKeyMapper = item -> dataItemKeys.key(item.getId());
    }

    @Override
    protected JmixChartSerializer createSerializer() {
        return applicationContext.getBean(ChartSerializer.class, dataItemKeyMapper);
    }
}
