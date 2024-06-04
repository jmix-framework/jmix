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
import io.jmix.chartsflowui.kit.component.model.series.ColorBy;
import io.jmix.chartsflowui.kit.component.model.series.GaugeSeries;
import io.jmix.chartsflowui.kit.component.model.shared.Color;
import io.jmix.chartsflowui.kit.component.serialization.JmixChartSerializer;
import org.json.JSONException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import serialization.AbstractSerializationTest;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

public class GaugeSeriesSerializationTest extends AbstractSerializationTest {

    @Test
    @DisplayName("Serialization of all GaugeSeries options")
    public void gaugeSeriesSerializationTest() throws IOException, URISyntaxException, JSONException {
        JmixChartSerializer chartSerializer = new JmixChartSerializer();

        ChartOptions chartOptions = new ChartOptions(new JmixChart());

        GaugeSeries gaugeSeries = new GaugeSeries()
                .withId("gaugeId")
                .withName("gaugeName")
                .withColorBy(ColorBy.SERIES)
                .withZLevel(1)
                .withZ(2)
                .withCenter("50%", "50%")
                .withRadius("75%")
                .withLegendHoverLink(false)
                .withStartAngle(240)
                .withEndAngle(-45)
                .withClockwise(false)
                .withMin(0)
                .withMax(100)
                .withSplitNumber(11)
                .withAxisLine(
                        new GaugeSeries.AxisLine()
                                .withShow(false)
                                .withRoundCap(true)
                                .withLineStyle(
                                        new GaugeSeries.AxisLine.LineStyle()
                                                .withColorPalette(
                                                        Map.of(
                                                                0.1, Color.RED,
                                                                0.2, Color.GREEN,
                                                                0.3, Color.BLUE,
                                                                1.0, Color.BLACK
                                                        )
                                                )
                                                .withWidth(15)
                                                .withShadowBlur(15)
                                                .withShadowColor(Color.BLACK)
                                                .withShadowOffsetX(1)
                                                .withShadowOffsetY(2)
                                                .withOpacity(0.5)
                                )
                )
                .withProgress(
                        new GaugeSeries.Progress()
                                .withShow(true)
                                .withOverlap(true)
                                .withWidth(11)
                                .withRoundCap(true)
                                .withClip(false)
                )
                .withSplitLine(
                        new GaugeSeries.SplitLine()
                                .withShow(false)
                                .withLength(11)
                                .withDistance(11)
                )
                .withAxisTick(
                        new GaugeSeries.AxisTick()
                                .withShow(false)
                                .withSplitNumber(1)
                                .withLength(1)
                                .withDistance(11)
                )
                .withPointer(
                        new GaugeSeries.Pointer()
                                .withShow(false)
                                .withShowAbove(false)
                                .withIcon("diamond")
                                .withOffsetCenter("10%", "10%")
                                .withLength("60%")
                                .withWidth(11)
                                .withKeepAspect(true)
                )
                .withAnchor(
                        new GaugeSeries.Anchor()
                                .withShow(false)
                                .withShowAbove(false)
                                .withSize(11)
                                .withIcon("diamond")
                                .withOffsetCenter("10%", "10%")
                                .withKeepAspect(false)
                )
                .withEmphasis(
                        new GaugeSeries.Emphasis()
                                .withDisabled(false)
                )
                .withSilent(true)
                .withAnimation(true)
                .withAnimationThreshold(1501)
                .withAnimationDuration(1500)
                .withAnimationEasing("backOut")
                .withAnimationDelay(15)
                .withAnimationDurationUpdate(150)
                .withAnimationEasingUpdate("cubicOut")
                .withAnimationDelayUpdate(15);

        chartOptions.addSeries(gaugeSeries);
        JsonValue serializedJson = chartSerializer.serialize(chartOptions);

        String expectedJson = readFile("series/gauge-series-result.json");

        JSONAssert.assertEquals(expectedJson, serializedJson.toJson(), true);
    }
}
