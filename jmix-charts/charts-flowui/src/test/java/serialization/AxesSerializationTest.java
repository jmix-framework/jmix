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

package serialization;

import elemental.json.JsonValue;
import io.jmix.chartsflowui.kit.component.JmixChart;
import io.jmix.chartsflowui.kit.component.model.ChartOptions;
import io.jmix.chartsflowui.kit.component.model.HasLineStyle;
import io.jmix.chartsflowui.kit.component.model.axis.*;
import io.jmix.chartsflowui.kit.component.model.shared.*;
import io.jmix.chartsflowui.kit.component.serialization.JmixChartSerializer;
import org.json.JSONException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;
import java.net.URISyntaxException;

public class AxesSerializationTest extends AbstractSerializationTest {

    @Test
    @DisplayName("Serialization of all XAxis options")
    public void xAxisSerializationTest() throws IOException, URISyntaxException, JSONException {
        JmixChartSerializer chartSerializer = new JmixChartSerializer();

        ChartOptions chartOptions = new ChartOptions(new JmixChart());

        XAxis xAxis = new XAxis()
                .withId("axisId")
                .withShow(true)
                .withGridIndex(5)
                .withAlignTicks(true)
                .withPosition(AbstractCartesianAxis.Position.TOP)
                .withOffset(15)
                .withName("xAxis")
                .withNameLocation(HasAxisName.NameLocation.START)
                .withNameTextStyle(
                        new HasAxisName.NameTextStyle()
                                .withColor(Color.IVORY)
                                .withFontStyle(FontStyle.ITALIC)
                                .withFontWeight("bold")
                                .withFontFamily("monospace")
                                .withFontSize(40)
                                .withAlign(Align.CENTER)
                                .withVerticalAlign(VerticalAlign.TOP)
                                .withLineHeight(56)
                                .withBackgroundColor(Color.IVORY)
                                .withBorderColor(Color.IVORY)
                                .withBorderWidth(4)
                                .withBorderType("dashed")
                                .withBorderDashOffset(51)
                                .withBorderRadius(41)
                                .withPadding(1, 2, 3, 4)
                                .withShadowBlur(4)
                                .withShadowColor(Color.IVORY)
                                .withShadowOffsetX(31)
                                .withShadowOffsetY(321)
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
                                .withRichStyle(
                                        "a", new RichStyle()
                                                .withColor(Color.IVORY)
                                                .withLineHeight(10)
                                )
                                .withRichStyle(
                                        "b", new RichStyle()
                                                .withBackgroundColor(Color.IVORY)
                                                .withHeight(40)
                                )
                                .withRichStyle(
                                        "x", new RichStyle()
                                                .withFontSize(18)
                                                .withFontFamily("Microsoft YaHei")
                                                .withBorderColor(Color.IVORY)
                                                .withBorderRadius(4)
                                )
                )
                .withNameGap(18)
                .withNameRotate(90)
                .withInverse(true)
                .withBoundaryGap("20%", "20%")
                .withMin("12")
                .withMax("14")
                .withScale(true)
                .withSplitNumber(7)
                .withMinInterval(9)
                .withMaxInterval(16)
                .withInterval(10)
                .withLogBase(2)
                .withSilent(true)
                .withTriggerEvent(true)
                .withAxisLine(
                        new AxisLine()
                                .withShow(false)
                                .withOnZero(false)
                                .withOnZeroAxisIndex(5)
                                .withSymbols("diamond", "arrow")
                                .withSymbolsSize(15, 10)
                                .withSymbolsOffset(4, 1)
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
                .withAxisTick(
                        new AxisTick()
                                .withShow(false)
                                .withAlignWithLabel(true)
                                .withInterval(15)
                                .withInside(true)
                                .withLength(10)
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
                .withMinorTick(
                        new MinorTick()
                                .withShow(true)
                                .withSplitNumber(1)
                                .withLength(10)
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
                .withAxisLabel(
                        new AxisLabel()
                                .withShow(true)
                                .withInterval(10)
                                .withInside(true)
                                .withRotate(15)
                                .withMargin(5)
                                .withFormatter("{value} kg")
                                .withShowMinLabel(true)
                                .withShowMaxLabel(true)
                                .withHideOverlap(true)
                                .withColor(Color.IVORY)
                                .withFontStyle(FontStyle.ITALIC)
                                .withFontWeight("bold")
                                .withFontFamily("monospace")
                                .withFontSize(40)
                                .withAlign(Align.CENTER)
                                .withVerticalAlign(VerticalAlign.TOP)
                                .withLineHeight(56)
                                .withBackgroundColor(Color.IVORY)
                                .withBorderColor(Color.IVORY)
                                .withBorderWidth(4)
                                .withBorderType("dashed")
                                .withBorderDashOffset(51)
                                .withBorderRadius(41)
                                .withPadding(1, 2, 3, 4)
                                .withShadowColor(Color.IVORY)
                                .withShadowOffsetX(12)
                                .withShadowOffsetY(24)
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
                                .withRichStyle(
                                        "a", new RichStyle()
                                                .withColor(Color.IVORY)
                                                .withLineHeight(10)
                                )
                                .withRichStyle(
                                        "b", new RichStyle()
                                                .withBackgroundColor(Color.IVORY)
                                                .withHeight(40)
                                )
                                .withRichStyle(
                                        "x", new RichStyle()
                                                .withFontSize(18)
                                                .withFontFamily("Microsoft YaHei")
                                                .withBorderColor(Color.IVORY)
                                                .withBorderRadius(4)
                                )
                )
                .withSplitLine(
                        new SplitLine()
                                .withShow(true)
                                .withInterval(15)
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
                .withMinorSplitLine(
                        new MinorSplitLine()
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
                .withSplitArea(
                        new SplitArea()
                                .withInterval(15)
                                .withShow(true)
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
                .withAxisPointer(
                        new AbstractAxis.AxisPointer()
                                .withShow(true)
                                .withType(AbstractAxis.AxisPointer.IndicatorType.SHADOW)
                                .withSnap(true)
                                .withZ(15)
                                .withLabel(
                                        new Label()
                                                .withShow(true)
                                                .withPrecision(5)
                                                .withFormatter("{value} kg")
                                                .withMargin(5)
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
                                                .withPadding(1, 2, 3, 4)
                                                .withBackgroundColor(Color.IVORY)
                                                .withBorderColor(Color.IVORY)
                                                .withBorderWidth(4)
                                                .withShadowBlur(15)
                                                .withShadowColor(Color.IVORY)
                                                .withShadowOffsetX(12)
                                                .withShadowOffsetY(24)
                                )
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
                                .withShadowStyle(
                                        new ShadowStyle()
                                                .withColor(Color.IVORY)
                                                .withShadowBlur(15)
                                                .withShadowColor(Color.IVORY)
                                                .withShadowOffsetX(15)
                                                .withShadowOffsetY(61)
                                                .withOpacity(0.14)
                                )
                                .withTriggerEmphasis(true)
                                .withTriggerTooltip(false)
                                .withValue(1)
                                .withStatus(false)
                                .withHandle(
                                        new AbstractAxis.AxisPointer.Handle()
                                                .withShow(true)
                                                .withIcon("jmix")
                                                .withSize(15, 51)
                                                .withMargin(15)
                                                .withColor(Color.IVORY)
                                                .withThrottle(50)
                                                .withShadowBlur(16)
                                                .withShadowColor(Color.IVORY)
                                                .withShadowOffsetX(16)
                                                .withShadowOffsetY(95)
                                )
                )
                .withAnimation(true)
                .withAnimationThreshold(1501)
                .withAnimationDuration(1500)
                .withAnimationEasing("backOut")
                .withAnimationDelay(15)
                .withAnimationDurationUpdate(150)
                .withAnimationEasingUpdate("cubicOut")
                .withAnimationDelayUpdate(15)
                .withZLevel(15)
                .withZ(12);

        chartOptions.addXAxis(xAxis);

        JsonValue serializedJson = chartSerializer.serialize(chartOptions);

        String expectedJson = readFile("axes/xaxis-result.json");

        JSONAssert.assertEquals(expectedJson, serializedJson.toJson(), true);
    }

    @Test
    @DisplayName("Serialization of specific YAxis options")
    public void yAxisSerializationTest() throws IOException, URISyntaxException, JSONException {
        JmixChartSerializer chartSerializer = new JmixChartSerializer();

        ChartOptions chartOptions = new ChartOptions(new JmixChart());

        chartOptions.addYAxis(new YAxis());

        JsonValue serializedJson = chartSerializer.serialize(chartOptions);

        String expectedJson = readFile("axes/yaxis-result.json");

        JSONAssert.assertEquals(expectedJson, serializedJson.toJson(), true);
    }

    @Test
    @DisplayName("Serialization of specific RadiusAxis options")
    public void radiusAxisSerializationTest() throws IOException, URISyntaxException, JSONException {
        JmixChartSerializer chartSerializer = new JmixChartSerializer();

        ChartOptions chartOptions = new ChartOptions(new JmixChart());

        chartOptions.setRadiusAxis(new RadiusAxis()
                .withPolarIndex(14));

        JsonValue serializedJson = chartSerializer.serialize(chartOptions);

        String expectedJson = readFile("axes/radiusaxis-result.json");

        JSONAssert.assertEquals(expectedJson, serializedJson.toJson(), true);
    }

    @Test
    @DisplayName("Serialization of specific AngleAxis options")
    public void angleAxisSerializationTest() throws IOException, URISyntaxException, JSONException {
        JmixChartSerializer chartSerializer = new JmixChartSerializer();

        ChartOptions chartOptions = new ChartOptions(new JmixChart());

        chartOptions.setAngleAxis(new AngleAxis()
                .withPolarIndex(42)
                .withStartAngle(45)
                .withClockwise(false));

        JsonValue serializedJson = chartSerializer.serialize(chartOptions);

        String expectedJson = readFile("axes/angleaxis-result.json");

        JSONAssert.assertEquals(expectedJson, serializedJson.toJson(), true);
    }
}
