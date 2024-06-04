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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import elemental.json.JsonValue;
import elemental.json.impl.JreJsonFactory;
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

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JmixChartSerializer {

    private static final Logger log = LoggerFactory.getLogger(JmixChartSerializer.class);

    protected ObjectMapper objectMapper = new ObjectMapper();
    protected JreJsonFactory jsonFactory = new JreJsonFactory();

    public JmixChartSerializer() {
        initSerializer();
    }

    protected void initSerializer() {
        initMapper();
    }

    protected void initMapper() {
        SimpleModule module = createModule();
        getSerializers().forEach(module::addSerializer);

        objectMapper.registerModule(module);
        objectMapper.setFilterProvider(createFilterProvider());

        initMapperMixIns();

        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
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

    protected void initMapperMixIns() {
        objectMapper.addMixIn(ChartOptions.class, JmixChartMixins.ChartOptions.class);
        objectMapper.addMixIn(Title.class, JmixChartMixins.Title.class);
        objectMapper.addMixIn(AbstractRichText.class, JmixChartMixins.AbstractRichText.class);
        objectMapper.addMixIn(AbstractLegend.class, JmixChartMixins.AbstractLegend.class);
        objectMapper.addMixIn(Brush.class, JmixChartMixins.Brush.class);
        objectMapper.addMixIn(Grid.class, JmixChartMixins.Grid.class);
        objectMapper.addMixIn(AxisLine.class, JmixChartMixins.AbstractAxis.AxisLine.class);
        objectMapper.addMixIn(AreaStyle.class, JmixChartMixins.AreaStyle.class);
        objectMapper.addMixIn(Radar.class, JmixChartMixins.Radar.class);
        objectMapper.addMixIn(AbstractDataZoom.class, JmixChartMixins.AbstractDataZoom.class);
        objectMapper.addMixIn(AbstractVisualMap.class, JmixChartMixins.AbstractVisualMap.class);
        objectMapper.addMixIn(AbstractBorderedTextStyle.class, JmixChartMixins.AbstractBorderedTextStyle.class);
        objectMapper.addMixIn(Toolbox.class, JmixChartMixins.Toolbox.class);
        objectMapper.addMixIn(MagicTypeFeature.class, JmixChartMixins.MagicTypeFeature.class);
        objectMapper.addMixIn(BrushFeature.class, JmixChartMixins.BrushFeature.class);
        objectMapper.addMixIn(Emphasis.IconStyle.class, JmixChartMixins.ToolboxEmphasisIconStyle.class);
        objectMapper.addMixIn(LineSeries.LabelLine.class, JmixChartMixins.LabelLine.class);
        objectMapper.addMixIn(ScatterSeries.LabelLine.class, JmixChartMixins.LabelLine.class);
        objectMapper.addMixIn(EffectScatterSeries.LabelLine.class, JmixChartMixins.LabelLine.class);
        objectMapper.addMixIn(MarkPoint.Point.class, JmixChartMixins.MarkPoint.Point.class);
        objectMapper.addMixIn(MarkLine.Point.class, JmixChartMixins.MarkLine.Point.class);
        objectMapper.addMixIn(MarkArea.PointPair.class, JmixChartMixins.MarkArea.PointPair.class);
        objectMapper.addMixIn(MarkArea.Point.class, JmixChartMixins.MarkArea.Point.class);
        objectMapper.addMixIn(LineSeries.class, JmixChartMixins.Series.class);
        objectMapper.addMixIn(BarSeries.class, JmixChartMixins.Series.class);
        objectMapper.addMixIn(ScatterSeries.class, JmixChartMixins.Series.class);
        objectMapper.addMixIn(EffectScatterSeries.class, JmixChartMixins.Series.class);
        objectMapper.addMixIn(BoxplotSeries.class, JmixChartMixins.Series.class);
        objectMapper.addMixIn(CandlestickSeries.class, JmixChartMixins.Series.class);
        objectMapper.addMixIn(CandlestickSeries.ItemStyle.class, JmixChartMixins.CandlestickSeries.ItemStyle.class);
        objectMapper.addMixIn(FunnelSeries.class, JmixChartMixins.FunnelSeries.class);
    }

    protected SimpleFilterProvider createFilterProvider() {
        return new SimpleFilterProvider();
    }

    public JsonValue parseRawJson(String rawJson) {
        return jsonFactory.parse(rawJson);
    }

    public JsonValue serialize(ChartOptions options) {
        JsonValue serializedValue = serialize(options, ChartOptions.class);
        options.unmarkDirtyInDepth();
        return serializedValue;
    }

    public JsonValue serializeDataSet(DataSet dataSet) {
        return serialize(dataSet, DataSet.class);
    }

    public JsonValue serializeChangedItems(ChartIncrementalChanges<? extends DataItem> changedItems) {
        return serialize(changedItems, ChartIncrementalChanges.class);
    }

    protected JsonValue serialize(Object object, Class<?> objectClass) {
        String rawJson;

        log.debug("Starting serialize {}", objectClass.getSimpleName());

        try {
            rawJson = objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(String.format("Cannot serialize %s", objectClass.getSimpleName()), e);
        }

        log.debug("Serialized {}: {}", objectClass.getSimpleName(), rawJson);

        return parseRawJson(rawJson);
    }
}
