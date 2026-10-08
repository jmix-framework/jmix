/*
 * Copyright 2026 Haulmont.
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

package io.jmix.chartsflowui.kit.meta.loader;

import com.vaadin.flow.component.Component;
import io.jmix.chartsflowui.kit.component.JmixChart;
import io.jmix.chartsflowui.kit.component.model.Title;
import io.jmix.chartsflowui.kit.component.model.axis.AxisType;
import io.jmix.chartsflowui.kit.component.model.axis.XAxis;
import io.jmix.chartsflowui.kit.component.model.axis.YAxis;
import io.jmix.chartsflowui.kit.component.model.legend.Legend;
import io.jmix.chartsflowui.kit.component.model.series.AbstractSeries;
import io.jmix.chartsflowui.kit.component.model.series.BarSeries;
import io.jmix.chartsflowui.kit.component.model.series.BoxplotSeries;
import io.jmix.chartsflowui.kit.component.model.series.CandlestickSeries;
import io.jmix.chartsflowui.kit.component.model.series.EffectScatterSeries;
import io.jmix.chartsflowui.kit.component.model.series.FunnelSeries;
import io.jmix.chartsflowui.kit.component.model.series.GaugeSeries;
import io.jmix.chartsflowui.kit.component.model.series.LineSeries;
import io.jmix.chartsflowui.kit.component.model.series.PieSeries;
import io.jmix.chartsflowui.kit.component.model.series.RadarSeries;
import io.jmix.chartsflowui.kit.component.model.series.ScatterSeries;
import io.jmix.flowui.kit.meta.component.preview.StudioPreviewComponentLoader;
import io.jmix.flowui.kit.meta.component.preview.StudioPreviewEnvironment;
import io.jmix.flowui.kit.meta.component.preview.loader.PreviewActionSupport;
import org.dom4j.Element;
import org.jspecify.annotations.Nullable;

/**
 * Studio preview loader for the {@code charts:chart} component.
 * <p>
 * A real {@link JmixChart} is used: even without data ECharts draws the declared title, legend
 * and axes, which is exactly what the runtime shows for an empty chart. Only the visual skeleton
 * of the options is mapped (title text, legend, axis types, series names) — data bindings and the
 * rest of the extensive options model stay design-time-irrelevant.
 */
public class StudioChartsPreviewLoader implements StudioPreviewComponentLoader {

    protected static final String CHARTS_SCHEMA = "http://jmix.io/schema/charts/ui";
    protected static final String CHART_ELEMENT = "chart";

    @Override
    public boolean isSupported(Element element) {
        return CHARTS_SCHEMA.equals(element.getNamespaceURI())
                && CHART_ELEMENT.equals(element.getName());
    }

    @Nullable
    @Override
    public Component load(Element componentElement, Element viewElement) {
        return load(componentElement, viewElement, StudioPreviewEnvironment.NOOP);
    }

    @Nullable
    @Override
    public Component load(Element componentElement, Element viewElement, StudioPreviewEnvironment environment) {
        JmixChart chart = new JmixChart();

        loadComponentBaseAttributes(chart, componentElement);

        loadTitle(componentElement, environment, chart);
        if (componentElement.element("legend") != null) {
            chart.setLegend(new Legend());
        }
        loadAxes(componentElement, chart);
        loadSeries(componentElement, chart);

        return chart;
    }

    protected void loadTitle(Element componentElement, StudioPreviewEnvironment environment, JmixChart chart) {
        Element titleElement = componentElement.element("title");
        if (titleElement == null) {
            return;
        }
        loadString(titleElement, "text").ifPresent(text -> {
            Title title = new Title();
            title.setText(PreviewActionSupport.resolveText(environment, text));
            chart.setTitle(title);
        });
    }

    protected void loadAxes(Element componentElement, JmixChart chart) {
        Element xAxes = componentElement.element("xAxes");
        if (xAxes != null) {
            for (Element axisElement : xAxes.elements("xAxis")) {
                XAxis axis = new XAxis();
                loadEnum(axisElement, AxisType.class, "type", axis::setType);
                chart.addXAxis(axis);
            }
        }
        Element yAxes = componentElement.element("yAxes");
        if (yAxes != null) {
            for (Element axisElement : yAxes.elements("yAxis")) {
                YAxis axis = new YAxis();
                loadEnum(axisElement, AxisType.class, "type", axis::setType);
                chart.addYAxis(axis);
            }
        }
    }

    protected void loadSeries(Element componentElement, JmixChart chart) {
        Element seriesElement = componentElement.element("series");
        if (seriesElement == null) {
            return;
        }
        for (Element series : seriesElement.elements()) {
            AbstractSeries<?> created = createSeries(series.getName());
            if (created != null) {
                loadString(series, "name", created::setName);
                chart.addSeries(created);
            }
        }
    }

    @Nullable
    protected AbstractSeries<?> createSeries(String tagName) {
        return switch (tagName) {
            case "bar" -> new BarSeries();
            case "line" -> new LineSeries();
            case "pie" -> new PieSeries();
            case "scatter" -> new ScatterSeries();
            case "effectScatter" -> new EffectScatterSeries();
            case "funnel" -> new FunnelSeries();
            case "gauge" -> new GaugeSeries();
            case "radar" -> new RadarSeries();
            case "boxplot" -> new BoxplotSeries();
            case "candlestick" -> new CandlestickSeries();
            default -> null;
        };
    }
}
