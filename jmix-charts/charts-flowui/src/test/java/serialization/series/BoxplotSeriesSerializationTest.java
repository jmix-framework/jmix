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

public class BoxplotSeriesSerializationTest extends AbstractSerializationTest {

    @Test
    @DisplayName("Serialization of all BoxplotSeries options")
    public void boxplotSeriesSerializationTest() throws IOException, URISyntaxException, JSONException {
        JmixChartSerializer chartSerializer = new JmixChartSerializer();

        ChartOptions chartOptions = new ChartOptions(new JmixChart());

        BoxplotSeries boxplotSeries = new BoxplotSeries()
                .withId("boxplotId")
                .withName("boxplotName")
                .withCoordinateSystem(CoordinateSystem.CARTESIAN_2_D)
                .withXAxisIndex(1)
                .withYAxisIndex(2)
                .withColorBy(ColorBy.DATA)
                .withLegendHoverLink(true)
                .withHoverAnimation(false)
                .withLayout(Orientation.VERTICAL)
                .withBoxWidth("5", "50%")
                .withEmphasis(
                        new BoxplotSeries.Emphasis()
                                .withDisabled(false)
                                .withFocus(FocusType.NONE)
                                .withBlurScope(BlurScopeType.COORDINATE_SYSTEM)
                )
                .withSelect(
                        new BoxplotSeries.Select()
                                .withDisabled(false)
                )
                .withSelectedMode(SelectedMode.DISABLED)
                .withDataGroupId("group")
                .withZLevel(1)
                .withZ(2)
                .withSilent(true)
                .withAnimationDuration(900)
                .withAnimationEasing("elasticOut")
                .withAnimationDelay(1);

        chartOptions.addSeries(boxplotSeries);
        JsonValue serializedJson = chartSerializer.serialize(chartOptions);

        String expectedJson = readFile("series/boxplot-series-result.json");

        JSONAssert.assertEquals(expectedJson, serializedJson.toJson(), true);
    }
}
