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
import io.jmix.chartsflowui.kit.component.model.series.BlurScopeType;
import io.jmix.chartsflowui.kit.component.model.series.ColorBy;
import io.jmix.chartsflowui.kit.component.model.series.FocusType;
import io.jmix.chartsflowui.kit.component.model.series.RadarSeries;
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

public class RadarSeriesSerializationTest extends AbstractSerializationTest {

    @Test
    @DisplayName("Serialization of all RadarSeries options")
    public void radarSeriesSerializationTest() throws IOException, URISyntaxException, JSONException {
        JmixChartSerializer chartSerializer = new JmixChartSerializer();

        ChartOptions chartOptions = new ChartOptions(new JmixChart());

        RadarSeries radarSeries = new RadarSeries()
                .withId("radarId")
                .withName("radarName")
                .withColorBy(ColorBy.SERIES)
                .withRadarIndex(5)
                .withSymbol(HasSymbols.SymbolType.DIAMOND)
                .withSymbolSize(21)
                .withSymbolRotate(12)
                .withSymbolKeepAspect(false)
                .withSymbolOffset("0", "50%")
                .withAreaStyle(
                        new RadarSeries.AreaStyle()
                                .withColor(Color.IVORY)
                                .withShadowBlur(21)
                                .withShadowColor(Color.IVORY)
                                .withShadowOffsetX(3)
                                .withShadowOffsetY(12)
                                .withOpacity(0.5)
                )
                .withEmphasis(
                        new RadarSeries.Emphasis()
                                .withDisabled(false)
                                .withFocus(FocusType.NONE)
                                .withBlurScope(BlurScopeType.COORDINATE_SYSTEM)
                )
                .withSelect(
                        new RadarSeries.Select()
                                .withDisabled(false)
                )
                .withSelectedMode(SelectedMode.SINGLE)
                .withDataGroupId("group")
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

        chartOptions.addSeries(radarSeries);
        JsonValue serializedJson = chartSerializer.serialize(chartOptions);

        String expectedJson = readFile("series/radar-series-result.json");

        JSONAssert.assertEquals(expectedJson, serializedJson.toJson(), true);
    }
}
