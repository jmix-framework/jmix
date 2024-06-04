/*
 * Copyright 2024 Haulmont.
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

package serialization.series;

import elemental.json.JsonValue;
import io.jmix.chartsflowui.kit.component.JmixChart;
import io.jmix.chartsflowui.kit.component.model.ChartOptions;
import io.jmix.chartsflowui.kit.component.model.series.*;
import io.jmix.chartsflowui.kit.component.model.shared.Align;
import io.jmix.chartsflowui.kit.component.model.shared.Orientation;
import io.jmix.chartsflowui.kit.component.model.shared.SelectedMode;
import io.jmix.chartsflowui.kit.component.serialization.JmixChartSerializer;
import org.json.JSONException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import serialization.AbstractSerializationTest;

import java.io.IOException;
import java.net.URISyntaxException;

public class FunnelSeriesSerializationTest extends AbstractSerializationTest {

    @Test
    @DisplayName("Serialization of all FunnelSeries options")
    public void funnelSeriesSerializationTest() throws IOException, URISyntaxException, JSONException {
        JmixChartSerializer chartSerializer = new JmixChartSerializer();

        ChartOptions chartOptions = new ChartOptions(new JmixChart());

        FunnelSeries funnelSeries = new FunnelSeries()
                .withId("funnelId")
                .withName("funnelName")
                .withColorBy(ColorBy.SERIES)
                .withMin(5)
                .withMax(105)
                .withMinSize("1%")
                .withMaxSize("150%")
                .withOrientation(Orientation.HORIZONTAL)
                .withSort(FunnelSeries.SortType.DESCENDING)
                .withGap(12)
                .withLegendHoverLink(false)
                .withFunnelAlign(Align.RIGHT)
                .withLabelLine(
                        new FunnelSeries.LabelLine()
                                .withShow(false)
                                .withLength(12)
                )
                .withEmphasis(
                        new FunnelSeries.Emphasis()
                                .withDisabled(true)
                                .withFocus(FocusType.NONE)
                                .withBlurScope(BlurScopeType.COORDINATE_SYSTEM)
                )
                .withSelect(
                        new FunnelSeries.Select()
                                .withDisabled(false)
                )
                .withSelectedMode(SelectedMode.DISABLED)
                .withZLevel(1)
                .withZ(2)
                .withLeft("20%")
                .withTop("15%")
                .withRight("20%")
                .withBottom("24%")
                .withWidth("100%")
                .withHeight("100%")
                .withSeriesLayoutBy(AbstractAxisAwareSeries.SeriesLayoutType.ROW)
                .withDatasetIndex(1)
                .withDataGroupId("group")
                .withSilent(false)
                .withAnimation(true)
                .withAnimationThreshold(1501)
                .withAnimationDuration(1500)
                .withAnimationEasing("backOut")
                .withAnimationDelay(15)
                .withAnimationDurationUpdate(150)
                .withAnimationEasingUpdate("cubicOut")
                .withAnimationDelayUpdate(15);

        chartOptions.addSeries(funnelSeries);
        JsonValue serializedJson = chartSerializer.serialize(chartOptions);

        String expectedJson = readFile("series/funnel-series-result.json");

        JSONAssert.assertEquals(expectedJson, serializedJson.toJson(), true);
    }
}
