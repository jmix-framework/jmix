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
import io.jmix.chartsflowui.kit.component.model.HasLineStyle;
import io.jmix.chartsflowui.kit.component.model.datazoom.AbstractDataZoom;
import io.jmix.chartsflowui.kit.component.model.datazoom.InsideDataZoom;
import io.jmix.chartsflowui.kit.component.model.datazoom.SliderDataZoom;
import io.jmix.chartsflowui.kit.component.model.shared.*;
import io.jmix.chartsflowui.kit.component.serialization.JmixChartSerializer;
import org.json.JSONException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;
import java.net.URISyntaxException;

public class DataZoomSerializationTest extends AbstractSerializationTest {

    @Test
    @DisplayName("Serialization of all InsideDataZoom options")
    public void insideDataZoomSerializationTest() throws IOException, URISyntaxException, JSONException {
        JmixChartSerializer chartSerializer = new JmixChartSerializer();

        ChartOptions chartOptions = new ChartOptions(new JmixChart());

        InsideDataZoom insideDataZoom = new InsideDataZoom()
                .withId("insideDataZoomId")
                .withDisabled(false)
                .withXAxisIndexes(4)
                .withYAxisIndexes(5)
                .withRadiusAxisIndexes(4, 5)
                .withAngleAxisIndexes(1, 6)
                .withFilterMode(AbstractDataZoom.FilterMode.WEAK_FILTER)
                .withStart(15D)
                .withEnd(98D)
                .withStartValue("startValue")
                .withEndValue("endValue")
                .withMinSpan(40D)
                .withMaxSpan(100D)
                .withMinValueSpan("minValueSpan")
                .withMaxValueSpan("maxValueSpan")
                .withOrientation(Orientation.VERTICAL)
                .withZoomLock(true)
                .withThrottle(500)
                .withRangeMode(AbstractDataZoom.RangeMode.PERCENT, AbstractDataZoom.RangeMode.VALUE)
                .withZoomOnMouseWheel(false)
                .withMoveOnMouseMove(false)
                .withMoveOnMouseWheel(true)
                .withPreventDefaultMouseMove(false);

        chartOptions.addDataZoom(insideDataZoom);
        JsonValue serializedJson = chartSerializer.serialize(chartOptions);

        String expectedJson = readFile("datazoom/inside-data-zoom-result.json");

        JSONAssert.assertEquals(expectedJson, serializedJson.toJson(), true);
    }

    @Test
    @DisplayName("Serialization of all SliderDataZoom options")
    public void sliderDataZoomSerializationTest() throws IOException, URISyntaxException, JSONException {
        JmixChartSerializer chartSerializer = new JmixChartSerializer();

        ChartOptions chartOptions = new ChartOptions(new JmixChart());

        SliderDataZoom sliderDataZoom = new SliderDataZoom()
                .withId("sliderId")
                .withShow(false)
                .withBackgroundColor(Color.IVORY)
                .withDataBackground(
                        new SliderDataZoom.DataBackground()
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
                                .withAreaStyle(
                                        new AreaStyle()
                                                .withColors(Color.IVORY, Color.IVORY)
                                                .withShadowBlur(59)
                                                .withShadowColor(Color.IVORY)
                                                .withShadowOffsetX(12)
                                                .withShadowOffsetY(24)
                                                .withOpacity(0.41)
                                )
                )
                .withSelectedDataBackground(
                        new SliderDataZoom.DataBackground()
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
                                .withAreaStyle(
                                        new AreaStyle()
                                                .withColors(Color.IVORY, Color.IVORY)
                                                .withShadowBlur(59)
                                                .withShadowColor(Color.IVORY)
                                                .withShadowOffsetX(12)
                                                .withShadowOffsetY(24)
                                                .withOpacity(0.41)
                                )
                )
                .withFillerColor(Color.IVORY)
                .withBorderColor(Color.IVORY)
                .withBorderRadius(15)
                .withHandleIcon("iconName")
                .withHandleSize("50%")
                .withHandleStyle(
                        new ItemStyle()
                                .withColor(Color.IVORY)
                                .withBorderColor(Color.IVORY)
                                .withBorderWidth(421)
                                .withBorderType("dashed")
                                .withBorderDashOffset(41)
                                .withCap(HasLineStyle.Cap.SQUARE)
                                .withJoin(HasLineStyle.Join.MITER)
                                .withMiterLimit(41)
                                .withShadowBlur(59)
                                .withShadowColor(Color.IVORY)
                                .withShadowOffsetX(12)
                                .withShadowOffsetY(24)
                                .withOpacity(0.41)
                )
                .withMoveHandleIcon("moveHandleIcon")
                .withMoveHandleSize(70)
                .withMoveHandleStyle(
                        new ItemStyle()
                                .withColor(Color.IVORY)
                                .withBorderColor(Color.IVORY)
                                .withBorderWidth(421)
                                .withBorderType("dashed")
                                .withBorderDashOffset(41)
                                .withCap(HasLineStyle.Cap.SQUARE)
                                .withJoin(HasLineStyle.Join.MITER)
                                .withMiterLimit(41)
                                .withShadowBlur(59)
                                .withShadowColor(Color.IVORY)
                                .withShadowOffsetX(12)
                                .withShadowOffsetY(24)
                                .withOpacity(0.41)
                )
                .withLabelPrecision(10)
                .withLabelFormatter("value")
                .withShowDetail(false)
                .withShowDataShadow(true)
                .withRealtime(false)
                .withTextStyle(
                        new TextStyle()
                                .withColor(Color.IVORY)
                                .withFontStyle(FontStyle.ITALIC)
                                .withFontWeight("bold")
                                .withFontFamily("monospace")
                                .withFontSize(40)
                                .withLineHeight(56)
                                .withWidth(50)
                                .withHeight(47)
                                .withTextBorderColor(Color.IVORY)
                                .withTextBorderWidth(2.5)
                                .withTextBorderType("dashed")
                                .withTextBorderDashOffset(3)
                                .withTextShadowColor(Color.IVORY)
                                .withTextShadowBlur(4)
                                .withTextShadowOffsetX(4)
                                .withTextShadowOffsetY(5)
                                .withOverflow(Overflow.BREAK_ALL)
                                .withEllipsis("........")
                )
                .withXAxisIndexes(4)
                .withYAxisIndexes(5)
                .withRadiusAxisIndexes(4, 5)
                .withAngleAxisIndexes(1, 6)
                .withFilterMode(AbstractDataZoom.FilterMode.WEAK_FILTER)
                .withStart(15D)
                .withEnd(98D)
                .withStartValue("startValue")
                .withEndValue("endValue")
                .withMinSpan(40D)
                .withMaxSpan(100D)
                .withMinValueSpan("minValueSpan")
                .withMaxValueSpan("maxValueSpan")
                .withOrientation(Orientation.VERTICAL)
                .withZoomLock(true)
                .withThrottle(500)
                .withRangeMode(AbstractDataZoom.RangeMode.PERCENT, AbstractDataZoom.RangeMode.VALUE)
                .withZLevel(15)
                .withZ(14)
                .withLeft("20%")
                .withTop("20%")
                .withRight("20%")
                .withBottom("20%")
                .withWidth("100%")
                .withHeight("100%")
                .withBrushSelect(false)
                .withBrushStyle(
                        new ItemStyle()
                                .withColor(Color.IVORY)
                                .withBorderColor(Color.IVORY)
                                .withBorderWidth(421)
                                .withBorderType("dashed")
                                .withBorderDashOffset(41)
                                .withCap(HasLineStyle.Cap.SQUARE)
                                .withJoin(HasLineStyle.Join.MITER)
                                .withMiterLimit(41)
                                .withShadowBlur(59)
                                .withShadowColor(Color.IVORY)
                                .withShadowOffsetX(12)
                                .withShadowOffsetY(24)
                                .withOpacity(0.41)
                )
                .withEmphasis(
                        new SliderDataZoom.Emphasis()
                                .withHandleStyle(
                                        new ItemStyle()
                                                .withColor(Color.IVORY)
                                                .withBorderColor(Color.IVORY)
                                                .withBorderWidth(421)
                                                .withBorderType("dashed")
                                                .withBorderDashOffset(41)
                                                .withCap(HasLineStyle.Cap.SQUARE)
                                                .withJoin(HasLineStyle.Join.MITER)
                                                .withMiterLimit(41)
                                                .withShadowBlur(59)
                                                .withShadowColor(Color.IVORY)
                                                .withShadowOffsetX(12)
                                                .withShadowOffsetY(24)
                                                .withOpacity(0.41)
                                )
                                .withMoveHandleStyle(
                                        new ItemStyle()
                                                .withColor(Color.IVORY)
                                                .withBorderColor(Color.IVORY)
                                                .withBorderWidth(421)
                                                .withBorderType("dashed")
                                                .withBorderDashOffset(41)
                                                .withCap(HasLineStyle.Cap.SQUARE)
                                                .withJoin(HasLineStyle.Join.MITER)
                                                .withMiterLimit(41)
                                                .withShadowBlur(59)
                                                .withShadowColor(Color.IVORY)
                                                .withShadowOffsetX(12)
                                                .withShadowOffsetY(24)
                                                .withOpacity(0.41)
                                )
                );

        chartOptions.addDataZoom(sliderDataZoom);
        JsonValue serializedJson = chartSerializer.serialize(chartOptions);

        String expectedJson = readFile("datazoom/slider-data-zoom-result.json");

        JSONAssert.assertEquals(expectedJson, serializedJson.toJson(), true);
    }
}
