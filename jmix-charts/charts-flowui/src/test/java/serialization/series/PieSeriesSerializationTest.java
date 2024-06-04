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
import io.jmix.chartsflowui.kit.component.model.shared.ItemStyle;
import io.jmix.chartsflowui.kit.component.model.shared.SelectedMode;
import io.jmix.chartsflowui.kit.component.serialization.JmixChartSerializer;
import org.json.JSONException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import serialization.AbstractSerializationTest;

import java.io.IOException;
import java.net.URISyntaxException;

public class PieSeriesSerializationTest extends AbstractSerializationTest {

    @Test
    @DisplayName("Serialization of all PieSeries options")
    public void pieSeriesSerializationTest() throws IOException, URISyntaxException, JSONException {
        JmixChartSerializer chartSerializer = new JmixChartSerializer();

        ChartOptions chartOptions = new ChartOptions(new JmixChart());

        PieSeries pieSeries = new PieSeries()
                .withId("pieId")
                .withName("pieName")
                .withColorBy(ColorBy.SERIES)
                .withLegendHoverLink(false)
                .withGeoIndex(32)
                .withCalendarIndex(2)
                .withSelectedMode(SelectedMode.SINGLE)
                .withSelectedOffset(11)
                .withClockwise(false)
                .withStartAngle(45)
                .withMinAngle(12)
                .withMinShowLabelAngle(32)
                .withRoseType(PieSeries.RoseType.RADIUS)
                .withAvoidLabelOverlap(false)
                .withStillShowZeroSum(false)
                .withPercentPrecision(1)
                .withCursor("pointer")
                .withZLevel(12)
                .withZ(32)
                .withLeft("32")
                .withTop("2")
                .withRight("43%")
                .withBottom("12%")
                .withWidth("32")
                .withHeight("12")
                .withShowEmptyCircle(false)
                .withEmptyCircleStyle(
                        new ItemStyle()
                                .withColor(Color.IVORY)
                                .withBorderColor(Color.IVORY)
                                .withBorderWidth(1)
                                .withBorderType("dashed")
                                .withBorderDashOffset(1)
                                .withCap(HasLineStyle.Cap.BUTT)
                                .withJoin(HasLineStyle.Join.BEVEL)
                                .withMiterLimit(12)
                                .withShadowBlur(12)
                                .withShadowColor(Color.IVORY)
                                .withShadowOffsetX(12)
                                .withShadowOffsetY(12)
                                .withOpacity(0.34)
                )
                .withLabelLine(
                        new PieSeries.LabelLine()
                                .withShow(false)
                                .withShowAbove(false)
                                .withLength(23)
                                .withLength2(12)
                                .withSmooth(true)
                                .withMinTurnAngle(81)
                                .withMaxSurfaceAngle(21)
                )
                .withEmphasis(
                        new PieSeries.Emphasis()
                                .withDisabled(true)
                                .withScale(false)
                                .withScaleSize(11)
                                .withFocus(FocusType.SERIES)
                                .withBlurScope(BlurScopeType.GLOBAL)
                )
                .withCenter("54%", "42%")
                .withRadius("12", "63%")

                .withSeriesLayoutBy(AbstractAxisAwareSeries.SeriesLayoutType.ROW)
                .withDatasetIndex(2)
                .withEncode(
                        new Encode()
                                .withX("2")
                                .withY("12")
                )
                .withDataGroupId("group")
                .withSilent(true)
                .withAnimationType(PieSeries.AnimationType.SCALE)
                .withAnimationTypeUpdate(PieSeries.AnimationUpdateType.EXPANSION)
                .withAnimation(true)
                .withAnimationThreshold(1501)
                .withAnimationDuration(1500)
                .withAnimationEasing("backOut")
                .withAnimationDelay(15)
                .withAnimationDurationUpdate(150)
                .withAnimationEasingUpdate("cubicOut")
                .withAnimationDelayUpdate(15);

        chartOptions.addSeries(pieSeries);
        JsonValue serializedJson = chartSerializer.serialize(chartOptions);

        String expectedJson = readFile("series/pie-series-result.json");

        JSONAssert.assertEquals(expectedJson, serializedJson.toJson(), true);
    }
}
