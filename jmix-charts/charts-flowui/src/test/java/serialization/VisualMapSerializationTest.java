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

package serialization;

import elemental.json.JsonValue;
import io.jmix.chartsflowui.kit.component.JmixChart;
import io.jmix.chartsflowui.kit.component.model.ChartOptions;
import io.jmix.chartsflowui.kit.component.model.shared.*;
import io.jmix.chartsflowui.kit.component.model.visualMap.AbstractVisualMap;
import io.jmix.chartsflowui.kit.component.model.visualMap.ContinuousVisualMap;
import io.jmix.chartsflowui.kit.component.model.visualMap.PiecewiseVisualMap;
import io.jmix.chartsflowui.kit.component.serialization.JmixChartSerializer;
import org.json.JSONException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;
import java.net.URISyntaxException;

public class VisualMapSerializationTest extends AbstractSerializationTest {

    @Test
    @DisplayName("Serialization of continuous visualMap options")
    public void continuousVisualMapSerializationTest() throws IOException, URISyntaxException, JSONException {
        JmixChartSerializer chartSerializer = new JmixChartSerializer();

        ChartOptions chartOptions = new ChartOptions(new JmixChart());

        ContinuousVisualMap visualMap = new ContinuousVisualMap()
                .withId("myVisualMap")
                .withMin(0.0)
                .withText("startText", "endText")
                .withMax(100.0)
                .withInverse(false)
                .withPrecision(0.0)
                .withItemWidth(20.0)
                .withItemHeight(140.0)
                .withAlign(AbstractVisualMap.MapAlign.AUTO)
                .withTextGap(1.0)
                .withShow(true)
                .withDimension("value")
                .withSeriesIndex(0)
                .withHoverLink(true)
                .withInRange(
                        new VisualEffect()
                                .withColor(Color.IVORY)
                                .withSymbolSize(30, 100)
                )
                .withOutOfRange(
                        new VisualEffect()
                                .withColor(Color.IVORY)
                                .withSymbolSize(30, 100)
                )
                .withController(
                        new AbstractVisualMap.VisualMapController()
                                .withInRange(
                                        new VisualEffect()
                                                .withColor(Color.IVORY)
                                                .withSymbolSize(30, 100)
                                )
                )
                .withZLevel(10.0)
                .withZ(4.0)
                .withPadding(1, 2, 3, 4)
                .withTextGap(10.0)
                .withShow(true)
                .withTextStyle(
                        new TextStyle()
                                .withColor(Color.IVORY)
                                .withFontSize(12)
                )
                .withFormatter("value")
                .withBorderColor(Color.IVORY)
                .withRange(20, 80)
                .withBackgroundColor(Color.IVORY)
                .withBorderWidth(1.0)
                .withTop("top")
                .withLeft("center")
                .withRight("auto")
                .withBottom("auto")
                .withCalculable(true)
                .withRealtime(false)
                .withHandleIcon("iconName")
                .withHandleSize("120%")
                .withHandleStyle(
                        new ItemStyle()
                                .withColor(Color.IVORY)
                                .withBorderColor(Color.IVORY)
                )
                .withIndicatorSize("50%")
                .withIndicatorStyle(
                        new ItemStyle()
                                .withColor(Color.IVORY)
                                .withBorderColor(Color.IVORY)
                                .withBorderWidth(2)
                )
                .withIndicatorIcon("circle")
                .withOrientation(Orientation.HORIZONTAL);

        chartOptions.addVisualMap(visualMap);

        JsonValue serializedJson = chartSerializer.serialize(chartOptions);

        String expectedJson = readFile("visualMap/continuous-visual-map-result.json");

        JSONAssert.assertEquals(expectedJson, serializedJson.toJson(), true);
    }

    @Test
    @DisplayName("Serialization of all piecewise visualMap options")
    public void piecewiseVisualMapSerializationTest() throws IOException, URISyntaxException, JSONException {
        JmixChartSerializer chartSerializer = new JmixChartSerializer();

        ChartOptions chartOptions = new ChartOptions(new JmixChart());

        PiecewiseVisualMap visualMap = new PiecewiseVisualMap()
                .withId("myVisualMap")
                .withMin(0.0)
                .withText("startText", "endText")
                .withMax(100.0)
                .withInverse(false)
                .withPrecision(0.0)
                .withItemWidth(20.0)
                .withItemHeight(140.0)
                .withAlign(AbstractVisualMap.MapAlign.AUTO)
                .withTextGap(1.0)
                .withShow(true)
                .withDimension("value")
                .withSeriesIndex(0)
                .withHoverLink(true)
                .withInRange(
                        new VisualEffect()
                                .withColor(Color.IVORY)
                                .withSymbolSize(30, 100)
                )
                .withOutOfRange(
                        new VisualEffect()
                                .withColor(Color.IVORY)
                                .withSymbolSize(30, 100)
                )
                .withController(
                        new AbstractVisualMap.VisualMapController()
                                .withInRange(new VisualEffect()
                                        .withColor(Color.IVORY)
                                        .withSymbolSize(30, 100))
                )
                .withZLevel(10.0)
                .withZ(4.0)
                .withPadding(1, 2, 3, 4)
                .withTextGap(10.0)
                .withShow(true)
                .withTextStyle(
                        new TextStyle()
                                .withColor(Color.IVORY)
                                .withFontSize(12)
                )
                .withFormatter("value")
                .withBorderColor(Color.IVORY)
                .withBackgroundColor(Color.IVORY)
                .withBorderWidth(1.0)
                .withTop("top")
                .withLeft("center")
                .withRight("auto")
                .withBottom("auto")
                .withOrientation(Orientation.HORIZONTAL)
                .withSplitNumber(1)
                .withPieces(
                        new PiecewiseVisualMap.Piece()
                                .withColor(Color.BLACK)
                                .withValue(4.0)
                                .withMax(14.0),
                        new PiecewiseVisualMap.Piece()
                                .withColor(Color.BLACK)
                                .withValue(4.0)
                                .withMax(14.0)
                )
                .withCategories("demon hunter", "not demon hunter", "berserk")
                .withMinOpen(true)
                .withMaxOpen(true)
                .withSelectedMode(SelectedMode.MULTIPLE)
                .withShowLabel(true)
                .withItemGap(3)
                .withItemSymbol(HasSymbols.SymbolType.DIAMOND);

        chartOptions.addVisualMap(visualMap);

        JsonValue serializedJson = chartSerializer.serialize(chartOptions);

        System.out.println(serializedJson.toJson());

        String expectedJson = readFile("visualMap/piecewise-visual-map-result.json");

        JSONAssert.assertEquals(expectedJson, serializedJson.toJson(), true);
    }
}
