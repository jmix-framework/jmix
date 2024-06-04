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
import io.jmix.chartsflowui.kit.component.model.shared.Color;
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

public class CandlestickSeriesSerializationTest extends AbstractSerializationTest {

    @Test
    @DisplayName("Serialization of all CandlestickSeries options")
    public void candlestickSeriesSerializationTest() throws IOException, URISyntaxException, JSONException {
        JmixChartSerializer chartSerializer = new JmixChartSerializer();

        ChartOptions chartOptions = new ChartOptions(new JmixChart());

        CandlestickSeries candlestickSeries = new CandlestickSeries()
                .withId("candlestickId")
                .withXAxisIndex(1)
                .withYAxisIndex(2)
                .withName("candlestickName")
                .withColorBy(ColorBy.SERIES)
                .withLegendHoverLink(false)
                .withHoverAnimation(false)
                .withLayout(Orientation.VERTICAL)
                .withBarWidth("20%")
                .withBarMinWidth("15%")
                .withBarMaxWidth("20%")
                .withItemStyle(
                        new CandlestickSeries.ItemStyle()
                                .withBullishColor(Color.valueOf("#FF1FF0"))
                                .withBearishColor(Color.valueOf("#F2FFF0"))
                                .withBullishBorderColor(Color.valueOf("#F3FFF0"))
                                .withBearishBorderColor(Color.valueOf("#F4FFF0"))
                                .withDojiBorderColor(Color.valueOf("#F5FFF0"))
                                .withBorderWidth(1D)
                                .withShadowBlur(12)
                                .withShadowColor(Color.valueOf("#F6FFF0"))
                                .withShadowOffsetX(1)
                                .withShadowOffsetY(2)
                                .withOpacity(0.5)
                )
                .withEmphasis(
                        new CandlestickSeries.Emphasis()
                                .withDisabled(false)
                                .withFocus(FocusType.NONE)
                                .withBlurScope(BlurScopeType.COORDINATE_SYSTEM)
                                .withItemStyle(
                                        new CandlestickSeries.ItemStyle()
                                                .withBullishColor(Color.valueOf("#FF1FF0"))
                                                .withBearishColor(Color.valueOf("#F2FFF0"))
                                                .withBullishBorderColor(Color.valueOf("#F3FFF0"))
                                                .withBearishBorderColor(Color.valueOf("#F4FFF0"))
                                                .withDojiBorderColor(Color.valueOf("#F5FFF0"))
                                                .withBorderWidth(1D)
                                                .withShadowBlur(12)
                                                .withShadowColor(Color.valueOf("#F6FFF0"))
                                                .withShadowOffsetX(1)
                                                .withShadowOffsetY(2)
                                                .withOpacity(0.5)
                                )
                )
                .withBlur(
                        new CandlestickSeries.Blur()
                                .withItemStyle(
                                        new CandlestickSeries.ItemStyle()
                                                .withBullishColor(Color.valueOf("#FF1FF0"))
                                                .withBearishColor(Color.valueOf("#F2FFF0"))
                                                .withBullishBorderColor(Color.valueOf("#F3FFF0"))
                                                .withBearishBorderColor(Color.valueOf("#F4FFF0"))
                                                .withDojiBorderColor(Color.valueOf("#F5FFF0"))
                                                .withBorderWidth(1D)
                                                .withShadowBlur(12)
                                                .withShadowColor(Color.valueOf("#F6FFF0"))
                                                .withShadowOffsetX(1)
                                                .withShadowOffsetY(2)
                                                .withOpacity(0.5)
                                )
                )
                .withSelect(
                        new CandlestickSeries.Select()
                                .withDisabled(false)
                                .withItemStyle(
                                        new CandlestickSeries.ItemStyle()
                                                .withBullishColor(Color.valueOf("#FF1FF0"))
                                                .withBearishColor(Color.valueOf("#F2FFF0"))
                                                .withBullishBorderColor(Color.valueOf("#F3FFF0"))
                                                .withBearishBorderColor(Color.valueOf("#F4FFF0"))
                                                .withDojiBorderColor(Color.valueOf("#F5FFF0"))
                                                .withBorderWidth(1D)
                                                .withShadowBlur(12)
                                                .withShadowColor(Color.valueOf("#F6FFF0"))
                                                .withShadowOffsetX(1)
                                                .withShadowOffsetY(2)
                                                .withOpacity(0.5)
                                )
                )
                .withSelectedMode(SelectedMode.DISABLED)
                .withLarge(false)
                .withLargeThreshold(700)
                .withProgressive(50000)
                .withProgressiveThreshold(1000)
                .withProgressiveChunkMode(ProgressiveChunkMode.SEQUENTIAL)
                .withDataGroupId("group")
                .withClip(false)
                .withZLevel(1)
                .withZ(2)
                .withSilent(false)
                .withAnimationDuration(400)
                .withAnimationEasing("linear")
                .withAnimationDelay(4);

        chartOptions.addSeries(candlestickSeries);
        JsonValue serializedJson = chartSerializer.serialize(chartOptions);

        String expectedJson = readFile("series/candlestick-series-result.json");

        JSONAssert.assertEquals(expectedJson, serializedJson.toJson(), true);
    }
}
