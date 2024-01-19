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
import io.jmix.chartsflowui.kit.component.model.shared.HasSymbols;
import io.jmix.chartsflowui.kit.component.model.shared.SelectedMode;
import io.jmix.chartsflowui.kit.component.serialization.JmixChartSerializer;
import org.json.JSONException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import serialization.AbstractSerializationTest;

import java.io.IOException;
import java.net.URISyntaxException;

public class EffectScatterSeriesSerializationTest extends AbstractSerializationTest {

    @Test
    @DisplayName("Serialization of all EffectScatterSeries options")
    public void effectScatterSeriesSerializationTest() throws IOException, URISyntaxException, JSONException {
        JmixChartSerializer chartSerializer = new JmixChartSerializer();

        ChartOptions chartOptions = new ChartOptions(new JmixChart());

        EffectScatterSeries effectScatterSeries = new EffectScatterSeries()
                .withId("effectScatterId")
                .withName("effectScatterName")
                .withColorBy(ColorBy.DATA)
                .withLegendHoverLink(false)
                .withEffectType("ripple")

                .withShowEffectOn(EffectScatterSeries.EffectType.EMPHASIS)
                .withRippleEffect(
                        new EffectScatterSeries.RippleEffect()
                                .withColor(Color.IVORY)
                                .withNumber(3)
                                .withPeriod(4)
                                .withScale(2.5)
                                .withBrushType(EffectScatterSeries.RippleEffect.BrushType.STROKE)
                )
                .withCoordinateSystem(CoordinateSystem.CARTESIAN_2_D)
                .withXAxisIndex(1)
                .withYAxisIndex(2)
                .withPolarIndex(3)
                .withGeoIndex(4)
                .withCalendarIndex(5)
                .withSymbol(HasSymbols.SymbolType.DIAMOND)
                .withSymbolSize(12)
                .withSymbolRotate(12)
                .withSymbolKeepAspect(true)
                .withSymbolOffset("0", "50%")
                .withCursor("pointer")
                .withLabelLine(
                        new EffectScatterSeries.LabelLine()
                                .withShow(true)
                                .withShowAbove(false)
                                .withLength(12)
                                .withSmooth(false)
                                .withMinTurnAngle(32)
                )
                .withEmphasis(
                        new EffectScatterSeries.Emphasis()
                                .withDisabled(false)
                                .withScale(1.4)
                                .withFocus(FocusType.SERIES)
                                .withBlurScope(BlurScopeType.COORDINATE_SYSTEM)
                )
                .withSelect(
                        new EffectScatterSeries.Select()
                                .withDisabled(false)
                )
                .withSelectedMode(SelectedMode.DISABLED)
                .withSeriesLayoutBy(AbstractAxisAwareSeries.SeriesLayoutType.COLUMN)
                .withDatasetIndex(1)
                .withEncode(
                        new Encode()
                                .withX("2")
                                .withAngle("3")
                )
                .withClip(false)
                .withZLevel(11)
                .withZ(1)
                .withSilent(false)
                .withAnimation(true)
                .withAnimationThreshold(1501)
                .withAnimationDuration(1500)
                .withAnimationEasing("backOut")
                .withAnimationDelay(15)
                .withAnimationDurationUpdate(150)
                .withAnimationEasingUpdate("cubicOut")
                .withAnimationDelayUpdate(15);

        chartOptions.addSeries(effectScatterSeries);
        JsonValue serializedJson = chartSerializer.serialize(chartOptions);

        String expectedJson = readFile("series/effect-scatter-series-result.json");

        JSONAssert.assertEquals(expectedJson, serializedJson.toJson(), true);
    }
}
