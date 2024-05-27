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

public class ScatterSeriesSerializationTest extends AbstractSerializationTest {

    @Test
    @DisplayName("Serialization of all ScatterSeries options")
    public void scatterSeriesSerializationTest() throws IOException, URISyntaxException, JSONException {
        JmixChartSerializer chartSerializer = new JmixChartSerializer();

        ChartOptions chartOptions = new ChartOptions(new JmixChart());

        ScatterSeries scatterSeries = new ScatterSeries()
                .withId("scatterId")
                .withName("scatterName")
                .withColorBy(ColorBy.DATA)
                .withCoordinateSystem(CoordinateSystem.CARTESIAN_2_D)
                .withXAxisIndex(1)
                .withYAxisIndex(2)
                .withPolarIndex(3)
                .withGeoIndex(4)
                .withCalendarIndex(5)
                .withLegendHoverLink(false)
                .withSymbol(HasSymbols.SymbolType.DIAMOND)
                .withSymbolSize(3)
                .withSymbolRotate(2)
                .withSymbolKeepAspect(true)
                .withSymbolOffset("0", "50%")
                .withLarge(true)
                .withLargeThreshold(3000)
                .withCursor("pointer")
                .withLabelLine(
                        new ScatterSeries.LabelLine()
                                .withShow(true)
                                .withShowAbove(false)
                                .withLength(32)
                                .withSmooth(true)
                                .withMinTurnAngle(15)
                )
                .withEmphasis(
                        new ScatterSeries.Emphasis()
                                .withDisabled(false)
                                .withScale(1.4)
                                .withFocus(FocusType.SELF)
                                .withBlurScope(BlurScopeType.GLOBAL)
                )
                .withSelect(
                        new ScatterSeries.Select()
                                .withDisabled(false)
                )
                .withSelectedMode(SelectedMode.SINGLE)
                .withProgressive(500)
                .withProgressiveThreshold(1500)
                .withEncode(
                        new Encode()
                                .withY("2")
                                .withX("1")
                )
                .withSeriesLayoutBy(AbstractAxisAwareSeries.SeriesLayoutType.ROW)
                .withDatasetIndex(1)
                .withDataGroupId("group")
                .withClip(true)
                .withZLevel(1)
                .withZ(2)
                .withSilent(false)
                .withAnimation(true)
                .withAnimationThreshold(1501)
                .withAnimationDuration(1500)
                .withAnimationEasing("backOut")
                .withAnimationDelay(15)
                .withAnimationDurationUpdate(150)
                .withAnimationEasingUpdate("cubicOut")
                .withAnimationDelayUpdate(15);

        chartOptions.addSeries(scatterSeries);
        JsonValue serializedJson = chartSerializer.serialize(chartOptions);

        String expectedJson = readFile("series/scatter-series-result.json");

        JSONAssert.assertEquals(expectedJson, serializedJson.toJson(), true);
    }
}
