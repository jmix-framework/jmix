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
import io.jmix.chartsflowui.kit.component.model.HasLineStyle;
import io.jmix.chartsflowui.kit.component.model.series.*;
import io.jmix.chartsflowui.kit.component.model.shared.Color;
import io.jmix.chartsflowui.kit.component.model.shared.LineStyle;
import io.jmix.chartsflowui.kit.component.model.shared.SelectedMode;
import io.jmix.chartsflowui.kit.component.serialization.JmixChartSerializer;
import org.json.JSONException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import serialization.AbstractSerializationTest;

import java.io.IOException;
import java.net.URISyntaxException;

public class BarSeriesSerializationTest extends AbstractSerializationTest {

    @Test
    @DisplayName("Serialization of all BarSeries options")
    public void barSeriesSerializationTest() throws IOException, URISyntaxException, JSONException {
        JmixChartSerializer chartSerializer = new JmixChartSerializer();

        ChartOptions chartOptions = new ChartOptions(new JmixChart());

        BarSeries barSeries = new BarSeries()
                .withId("barId")
                .withName("barName")
                .withColorBy(ColorBy.DATA)
                .withLegendHoverLink(false)
                .withCoordinateSystem(CoordinateSystem.POLAR)
                .withXAxisIndex(1)
                .withYAxisIndex(2)
                .withPolarIndex(3)
                .withRoundCap(true)
                .withRealtimeSort(true)
                .withShowBackground(true)
                .withBackgroundStyle(
                        new BarSeries.BackgroundStyle()
                                .withColor(Color.IVORY)
                                .withBorderColor(Color.IVORY)
                                .withBorderWidth(12)
                                .withBorderType("dashed")
                                .withBorderRadius(31)
                                .withShadowBlur(32)
                                .withShadowColor(Color.IVORY)
                                .withShadowOffsetX(32)
                                .withShadowOffsetY(12)
                                .withOpacity(0.34)
                )
                .withLabelLine(
                        new BarSeries.LabelLine()
                                .withShow(true)
                                .withLineStyle(
                                        new LineStyle()
                                                .withColor(Color.IVORY)
                                                .withWidth(421)
                                                .withType("dashed")
                                                .withDashOffset(41)
                                                .withCap(HasLineStyle.Cap.SQUARE)
                                                .withJoin(HasLineStyle.Join.MITER)
                                                .withMiterLimit(41)
                                                .withShadowBlur(59)
                                                .withShadowColor(Color.IVORY)
                                                .withShadowOffsetX(12)
                                                .withShadowOffsetY(24)
                                                .withOpacity(0.41)
                                )
                )
                .withEmphasis(
                        new BarSeries.Emphasis()
                                .withDisabled(true)
                                .withFocus(FocusType.SELF)
                                .withBlurScope(BlurScopeType.GLOBAL)
                )
                .withSelect(
                        new BarSeries.Select()
                                .withDisabled(false)
                )
                .withSelectedMode(SelectedMode.MULTIPLE)
                .withStack("secondStack")
                .withStackStrategy(HasStack.StackStrategy.POSITIVE)
                .withSampling(SamplingType.LARGEST_TRIANGLE_THREE_BUCKET)
                .withCursor("pointer")
                .withBarWidth("60%")
                .withBarMaxWidth("60%")
                .withBarMinWidth("40%")
                .withBarMinHeight(321)
                .withBarMinAngle(12)
                .withBarGap("31%")
                .withBarCategoryGap("25%")
                .withLarge(true)
                .withLargeThreshold(400)
                .withProgressive(6000)
                .withProgressiveThreshold(4000)
                .withProgressiveChunkMode(ProgressiveChunkMode.SEQUENTIAL)
                .withEncode(
                        new Encode()
                                .withRadius("name")
                                .withAngle("angle")
                )
                .withSeriesLayoutBy(AbstractAxisAwareSeries.SeriesLayoutType.ROW)
                .withDatasetIndex(41)
                .withDataGroupId("group")
                .withClip(false)
                .withZLevel(123)
                .withZ(32)
                .withSilent(true)
                .withAnimation(true)
                .withAnimationThreshold(1501)
                .withAnimationDuration(1500)
                .withAnimationEasing("backOut")
                .withAnimationDelay(15)
                .withAnimationDurationUpdate(150)
                .withAnimationEasingUpdate("cubicOut")
                .withAnimationDelayUpdate(15);

        chartOptions.addSeries(barSeries);
        JsonValue serializedJson = chartSerializer.serialize(chartOptions);

        String expectedJson = readFile("series/bar-series-result.json");

        JSONAssert.assertEquals(expectedJson, serializedJson.toJson(), true);
    }
}
