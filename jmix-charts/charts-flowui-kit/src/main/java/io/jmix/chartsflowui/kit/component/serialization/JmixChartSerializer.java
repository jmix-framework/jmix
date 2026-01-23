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

package io.jmix.chartsflowui.kit.component.serialization;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.jmix.chartsflowui.kit.component.model.*;
import io.jmix.chartsflowui.kit.component.model.axis.AxisLine;
import io.jmix.chartsflowui.kit.component.model.axis.Radar;
import io.jmix.chartsflowui.kit.component.model.datazoom.AbstractDataZoom;
import io.jmix.chartsflowui.kit.component.model.legend.AbstractLegend;
import io.jmix.chartsflowui.kit.component.model.series.*;
import io.jmix.chartsflowui.kit.component.model.series.mark.MarkArea;
import io.jmix.chartsflowui.kit.component.model.series.mark.MarkLine;
import io.jmix.chartsflowui.kit.component.model.series.mark.MarkPoint;
import io.jmix.chartsflowui.kit.component.model.shared.AbstractBorderedTextStyle;
import io.jmix.chartsflowui.kit.component.model.shared.AbstractRichText;
import io.jmix.chartsflowui.kit.component.model.shared.AreaStyle;
import io.jmix.chartsflowui.kit.component.model.toolbox.BrushFeature;
import io.jmix.chartsflowui.kit.component.model.toolbox.Emphasis;
import io.jmix.chartsflowui.kit.component.model.toolbox.MagicTypeFeature;
import io.jmix.chartsflowui.kit.component.model.toolbox.Toolbox;
import io.jmix.chartsflowui.kit.component.model.visualMap.AbstractVisualMap;
import io.jmix.chartsflowui.kit.data.chart.DataItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.std.SimpleFilterProvider;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JmixChartSerializer {

    private static final Logger log = LoggerFactory.getLogger(JmixChartSerializer.class);

    protected ObjectMapper objectMapper;

    public JmixChartSerializer() {
        initSerializer();
    }

    protected void initSerializer() {
        initMapper();
    }

    protected void initMapper() {
        SimpleModule module = createModule();
        getSerializers().forEach(module::addSerializer);

        JsonMapper.Builder builder = JsonMapper.builder()
                .changeDefaultPropertyInclusion(incl ->
                        incl.withValueInclusion(JsonInclude.Include.NON_NULL))
                .addModule(module)
                .filterProvider(createFilterProvider());

        initMapperMixIns(builder);

        objectMapper = builder.build();
    }

    protected SimpleModule createModule() {
        return new SimpleModule();
    }

    protected List<AbstractSerializer<?>> getSerializers() {
        return Stream.of(
                new EnumIdSerializer(),
                new BrushLinkSerializer(),
                new ColorSerializer(),
                new IndexItemSerializer(),
                new DashArraySerializer(),
                new PaddingSerializer(),
                new SelectedModeSerializer(),
                new BoundaryGapSerializer(),
                new TooltipPositionSerializer(),
                new ChartSymbolSerializer(),
                new MarkPointDataCoordinateSerializer(),
                new MarkLineDataSerializer(),
                new LineSeriesOriginSerializer(),
                new LineSeriesLabelPositionSerializer(),
                new GaugeSeriesAxisLineStyleSerializer(),
                new JsFunctionSerializer()
        ).collect(Collectors.toList());
    }

    protected void initMapperMixIns(JsonMapper.Builder builder) {
        builder.addMixIn(ChartOptions.class, JmixChartMixins.ChartOptions.class);
        builder.addMixIn(Title.class, JmixChartMixins.Title.class);
        builder.addMixIn(AbstractRichText.class, JmixChartMixins.AbstractRichText.class);
        builder.addMixIn(AbstractLegend.class, JmixChartMixins.AbstractLegend.class);
        builder.addMixIn(Brush.class, JmixChartMixins.Brush.class);
        builder.addMixIn(Grid.class, JmixChartMixins.Grid.class);
        builder.addMixIn(AxisLine.class, JmixChartMixins.AbstractAxis.AxisLine.class);
        builder.addMixIn(AreaStyle.class, JmixChartMixins.AreaStyle.class);
        builder.addMixIn(Radar.class, JmixChartMixins.Radar.class);
        builder.addMixIn(AbstractDataZoom.class, JmixChartMixins.AbstractDataZoom.class);
        builder.addMixIn(AbstractVisualMap.class, JmixChartMixins.AbstractVisualMap.class);
        builder.addMixIn(AbstractBorderedTextStyle.class, JmixChartMixins.AbstractBorderedTextStyle.class);
        builder.addMixIn(Toolbox.class, JmixChartMixins.Toolbox.class);
        builder.addMixIn(MagicTypeFeature.class, JmixChartMixins.MagicTypeFeature.class);
        builder.addMixIn(BrushFeature.class, JmixChartMixins.BrushFeature.class);
        builder.addMixIn(Emphasis.IconStyle.class, JmixChartMixins.ToolboxEmphasisIconStyle.class);
        builder.addMixIn(LineSeries.LabelLine.class, JmixChartMixins.LabelLine.class);
        builder.addMixIn(ScatterSeries.LabelLine.class, JmixChartMixins.LabelLine.class);
        builder.addMixIn(EffectScatterSeries.LabelLine.class, JmixChartMixins.LabelLine.class);
        builder.addMixIn(MarkPoint.Point.class, JmixChartMixins.MarkPoint.Point.class);
        builder.addMixIn(MarkLine.Point.class, JmixChartMixins.MarkLine.Point.class);
        builder.addMixIn(MarkArea.PointPair.class, JmixChartMixins.MarkArea.PointPair.class);
        builder.addMixIn(MarkArea.Point.class, JmixChartMixins.MarkArea.Point.class);
        builder.addMixIn(LineSeries.class, JmixChartMixins.Series.class);
        builder.addMixIn(BarSeries.class, JmixChartMixins.Series.class);
        builder.addMixIn(ScatterSeries.class, JmixChartMixins.Series.class);
        builder.addMixIn(EffectScatterSeries.class, JmixChartMixins.Series.class);
        builder.addMixIn(BoxplotSeries.class, JmixChartMixins.Series.class);
        builder.addMixIn(CandlestickSeries.class, JmixChartMixins.Series.class);
        builder.addMixIn(CandlestickSeries.ItemStyle.class, JmixChartMixins.CandlestickSeries.ItemStyle.class);
        builder.addMixIn(FunnelSeries.class, JmixChartMixins.FunnelSeries.class);
    }

    protected SimpleFilterProvider createFilterProvider() {
        return new SimpleFilterProvider();
    }

    public JsonNode parseRawJson(String rawJson) {
        return objectMapper.readTree(rawJson);
    }

    public JsonNode serialize(ChartOptions options) {
        JsonNode serializedValue = serialize(options, ChartOptions.class);
        options.unmarkDirtyInDepth();
        return serializedValue;
    }

    public JsonNode serializeDataSet(DataSet dataSet) {
        return serialize(dataSet, DataSet.class);
    }

    public JsonNode serializeChangedItems(ChartIncrementalChanges<? extends DataItem> changedItems) {
        return serialize(changedItems, ChartIncrementalChanges.class);
    }

    protected JsonNode serialize(Object object, Class<?> objectClass) {
        String rawJson;

        log.debug("Starting serialize {}", objectClass.getSimpleName());

        try {
            rawJson = objectMapper.writeValueAsString(object);
        } catch (JacksonException e) {
            throw new IllegalStateException(String.format("Cannot serialize %s", objectClass.getSimpleName()), e);
        }

        log.debug("Serialized {}: {}", objectClass.getSimpleName(), rawJson);

        return parseRawJson(rawJson);
    }
}
