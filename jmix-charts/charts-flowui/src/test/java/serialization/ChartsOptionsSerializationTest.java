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
import io.jmix.chartsflowui.kit.component.model.*;
import io.jmix.chartsflowui.kit.component.model.axis.*;
import io.jmix.chartsflowui.kit.component.model.datazoom.AbstractDataZoom;
import io.jmix.chartsflowui.kit.component.model.legend.AbstractLegend;
import io.jmix.chartsflowui.kit.component.model.legend.ScrollableLegend;
import io.jmix.chartsflowui.kit.component.model.shared.*;
import io.jmix.chartsflowui.kit.component.model.toolbox.*;
import io.jmix.chartsflowui.kit.component.serialization.JmixChartSerializer;
import org.json.JSONException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;
import java.net.URISyntaxException;

public class ChartsOptionsSerializationTest extends AbstractSerializationTest {

    @Test
    @DisplayName("Serialization of all Title options")
    public void titleSerializationTest() throws IOException, URISyntaxException, JSONException {
        JmixChartSerializer chartSerializer = new JmixChartSerializer();

        ChartOptions chartOptions = new ChartOptions(new JmixChart());

        Title title = new Title()
                .withId("titleId")
                .withShow(true)
                .withText("titleText")
                .withLink("titleLink")
                .withTarget(Title.Target.SELF)
                .withTextStyle(
                        new Title.TextStyle()
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
                .withSubtext("titleSubtext")
                .withSublink("titleSublink")
                .withSubtarget(Title.Target.SELF)
                .withSubtextStyle(
                        new Title.SubtextStyle()
                                .withAlign(Align.CENTER)
                                .withVerticalAlign(VerticalAlign.TOP)
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
                .withTextAlign(Title.TextAlign.CENTER)
                .withTextVerticalAlign(Title.TextVerticalAlign.MIDDLE)
                .withTriggerEvent(true)
                .withPadding(1, 2, 3, 4)
                .withItemGap(123)
                .withZLevel(4)
                .withZ(23)
                .withLeft("30%")
                .withTop("30%")
                .withRight("30%")
                .withBottom("30%")
                .withBackgroundColor(Color.IVORY)
                .withBorderColor(Color.IVORY)
                .withBorderWidth(4)
                .withBorderRadius(41)
                .withShadowBlur(4)
                .withShadowColor(Color.IVORY)
                .withShadowOffsetX(31)
                .withShadowOffsetY(321);

        chartOptions.setTitle(title);
        JsonValue serializedJson = chartSerializer.serialize(chartOptions);

        String expectedJson = readFile("title-result.json");

        JSONAssert.assertEquals(expectedJson, serializedJson.toJson(), true);
    }

    @Test
    @DisplayName("Serialization of all Legend options")
    public void legendSerializationTest() throws IOException, URISyntaxException, JSONException {
        JmixChartSerializer chartSerializer = new JmixChartSerializer();

        ChartOptions chartOptions = new ChartOptions(new JmixChart());

        AbstractLegend<?> scrollableLegend = new ScrollableLegend()
                .withId("legendId")
                .withShow(true)
                .withZLevel(31)
                .withZ(5)
                .withLeft("30%")
                .withTop("30%")
                .withRight("30%")
                .withBottom("30%")
                .withWidth("50%")
                .withHeight("50%")
                .withOrientation(Orientation.VERTICAL)
                .withAlign(AbstractLegend.Align.LEFT)
                .withPadding(1, 2, 3, 4)
                .withItemGap(51)
                .withItemWidth(41)
                .withItemHeight(50)
                .withItemStyle(
                        new ItemStyle()
                                .withColor(Color.IVORY)
                                .withBorderColor(Color.IVORY)
                                .withBorderWidth(123)
                                .withBorderType("dashed")
                                .withBorderDashOffset(31)
                                .withCap(HasLineStyle.Cap.SQUARE)
                                .withJoin(HasLineStyle.Join.MITER)
                                .withMiterLimit(30)
                                .withShadowBlur(32)
                                .withShadowColor(Color.IVORY)
                                .withShadowOffsetX(45)
                                .withShadowOffsetY(41)
                                .withOpacity(0.45)
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
                .withSymbolRotate(30)
                .withFormatter("Legend {name}")
                .withSelectedMode(SelectedMode.DISABLED)
                .withInactiveColor(Color.IVORY)
                .withInactiveBorderColor(Color.IVORY)
                .withInactiveBorderWidth(321)
                .withSelectedSeries("series 1", true)
                .withSelectedSeries("series 2", false)
                .withTextStyle(
                        new AbstractLegend.TextStyle()
                                .withBackgroundColor(Color.IVORY)
                                .withBorderColor(Color.IVORY)
                                .withBorderWidth(321)
                                .withBorderType("dashed")
                                .withBorderDashOffset(312)
                                .withBorderRadius(75)
                                .withPadding(1, 2, 3, 4)
                                .withShadowColor(Color.IVORY)
                                .withShadowBlur(312)
                                .withShadowOffsetX(12)
                                .withShadowOffsetY(84)
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
                .withTooltip(new Tooltip()
                        .withShow(true))
                .withIcon("diamond")
                .withBackgroundColor(Color.IVORY)
                .withBorderColor(Color.IVORY)
                .withBorderWidth(412)
                .withBorderRadius(842)
                .withShadowBlur(12)
                .withShadowColor(Color.IVORY)
                .withShadowOffsetX(312)
                .withShadowOffsetY(61)
                .withScrollDataIndex(3)
                .withPageButtonItemGap(6)
                .withPageButtonGap(7)
                .withPageButtonPosition(AbstractLegend.Position.START)
                .withPageFormatter("{current}/{total}")
                .withPageIcons(
                        new ScrollableLegend.PageIcons()
                                .withHorizontal("prev", "next")
                                .withVertical("prev", "next")
                )
                .withPageIconColor(Color.IVORY)
                .withPageIconInactiveColor(Color.IVORY)
                .withPageIconSize(74)
                .withPageTextStyle(
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
                .withAnimation(true)
                .withAnimationDurationUpdate(321)
                .withEmphasis(
                        new AbstractLegend.Emphasis()
                                .withSelectorLabel(
                                        new AbstractLegend.SelectorLabel()
                                                .withShow(true)
                                                .withDistance(7)
                                                .withRotate(90)
                                                .withOffset(5, 12)
                                                .withColor(Color.IVORY)
                                                .withFontStyle(FontStyle.ITALIC)
                                                .withFontWeight("bolder")
                                                .withFontFamily("monospace")
                                                .withFontSize(51)
                                                .withAlign(Align.CENTER)
                                                .withVerticalAlign(VerticalAlign.TOP)
                                                .withLineHeight(20)
                                                .withBackgroundColor(Color.IVORY)
                                                .withBorderColor(Color.IVORY)
                                                .withBorderWidth(20)
                                                .withBorderType("dashed")
                                                .withBorderDashOffset(40)
                                                .withBorderRadius(30)
                                                .withPadding(1, 2, 3, 4)
                                                .withShadowColor(Color.IVORY)
                                                .withShadowBlur(32)
                                                .withShadowOffsetX(23)
                                                .withShadowOffsetY(85)
                                                .withWidth(200)
                                                .withHeight(200)
                                                .withTextBorderColor(Color.IVORY)
                                                .withTextBorderWidth(2.4)
                                                .withTextBorderType("dashed")
                                                .withTextBorderDashOffset(12)
                                                .withTextShadowColor(Color.IVORY)
                                                .withTextShadowBlur(23)
                                                .withTextShadowOffsetX(10)
                                                .withTextShadowOffsetY(53)
                                                .withOverflow(Overflow.BREAK_ALL)
                                                .withEllipsis("......")
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
                )
                .withSelector(true)
                .withSelectorLabel(
                        new AbstractLegend.SelectorLabel()
                                .withShow(true)
                                .withDistance(7)
                                .withRotate(90)
                                .withOffset(5, 12)
                                .withColor(Color.IVORY)
                                .withFontStyle(FontStyle.ITALIC)
                                .withFontWeight("bolder")
                                .withFontFamily("monospace")
                                .withFontSize(51)
                                .withAlign(Align.CENTER)
                                .withVerticalAlign(VerticalAlign.TOP)
                                .withLineHeight(20)
                                .withBackgroundColor(Color.IVORY)
                                .withBorderColor(Color.IVORY)
                                .withBorderWidth(20)
                                .withBorderType("dashed")
                                .withBorderDashOffset(40)
                                .withBorderRadius(30)
                                .withPadding(1, 2, 3, 4)
                                .withShadowColor(Color.IVORY)
                                .withShadowBlur(32)
                                .withShadowOffsetX(23)
                                .withShadowOffsetY(85)
                                .withWidth(200)
                                .withHeight(200)
                                .withTextBorderColor(Color.IVORY)
                                .withTextBorderWidth(2.4)
                                .withTextBorderType("dashed")
                                .withTextBorderDashOffset(12)
                                .withTextShadowColor(Color.IVORY)
                                .withTextShadowBlur(23)
                                .withTextShadowOffsetX(10)
                                .withTextShadowOffsetY(53)
                                .withOverflow(Overflow.BREAK_ALL)
                                .withEllipsis("......")
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
                .withSelectorPosition(AbstractLegend.Position.END)
                .withSelectorItemGap(12)
                .withSelectorButtonGap(75);

        chartOptions.setLegend(scrollableLegend);
        JsonValue serializedJson = chartSerializer.serialize(chartOptions);

        String expectedJson = readFile("legend-result.json");

        JSONAssert.assertEquals(expectedJson, serializedJson.toJson(), true);
    }

    @Test
    @DisplayName("Serialization of all Grid options")
    public void gridSerializationTest() throws IOException, URISyntaxException, JSONException {
        JmixChartSerializer chartSerializer = new JmixChartSerializer();

        ChartOptions chartOptions = new ChartOptions(new JmixChart());

        Grid grid = new Grid()
                .withId("gridId")
                .withShow(true)
                .withZLevel(123)
                .withZ(84)
                .withLeft("30%")
                .withTop("30%")
                .withRight("30%")
                .withBottom("30%")
                .withWidth("50%")
                .withHeight("50%")
                .withContainLabel(true)
                .withBackgroundColor(Color.IVORY)
                .withBorderColor(Color.IVORY)
                .withBorderWidth(13)
                .withShadowBlur(32)
                .withShadowColor(Color.IVORY)
                .withShadowOffsetX(12)
                .withShadowOffsetY(31)
                .withTooltip(
                        new InnerTooltip()
                                .withShow(true)
                                .withTrigger(AbstractTooltip.Trigger.AXIS)
                                .withFormatter("{b0}: {c0}<br />{b1}: {c1}")
                                .withValueFormatter("(value) => '$' + value.toFixed(2)")
                                .withBackgroundColor(Color.IVORY)
                                .withBorderColor(Color.IVORY)
                                .withBorderWidth(213)
                                .withPadding(1, 2, 3, 4)
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
                                .withExtraCssText("box-shadow: 0 0 3px rgba(0, 0, 0, 0.3);")
                );

        chartOptions.addGrid(grid);
        JsonValue serializedJson = chartSerializer.serialize(chartOptions);

        String expectedJson = readFile("grid-result.json");

        JSONAssert.assertEquals(expectedJson, serializedJson.toJson(), true);
    }

    @Test
    @DisplayName("Serialization of all Polar options")
    public void polarSerializationTest() throws IOException, URISyntaxException, JSONException {
        JmixChartSerializer chartSerializer = new JmixChartSerializer();

        ChartOptions chartOptions = new ChartOptions(new JmixChart());

        Polar polar = new Polar()
                .withId("polarId")
                .withZLevel(51)
                .withZ(5)
                .withCenter("51%", "47%")
                .withRadius("25%", "75%")
                .withTooltip(
                        new InnerTooltip()
                                .withShow(true)
                                .withTrigger(AbstractTooltip.Trigger.AXIS)
                                .withAxisPointer(
                                        new AbstractTooltip.AxisPointer()
                                                .withType(AbstractTooltip.AxisPointer.IndicatorType.CROSS)
                                                .withAxis(AbstractTooltip.AxisPointer.AxisType.RADIUS)
                                                .withSnap(true)
                                                .withZ(15)
                                                .withAnimation(true)
                                )
                                .withPosition(AbstractTooltip.Position.ItemTriggerPosition.LEFT)
                                .withFormatter("{b0}: {c0}<br />{b1}: {c1}")
                                .withValueFormatter("(value) => '$' + value.toFixed(2)")
                                .withBackgroundColor(Color.IVORY)
                                .withBorderColor(Color.IVORY)
                                .withBorderWidth(213)
                                .withPadding(1, 2, 3, 4)
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
                                .withExtraCssText("box-shadow: 0 0 3px rgba(0, 0, 0, 0.3);")
                );

        chartOptions.setPolar(polar);
        JsonValue serializedJson = chartSerializer.serialize(chartOptions);

        String expectedJson = readFile("polar-result.json");

        JSONAssert.assertEquals(expectedJson, serializedJson.toJson(), true);
    }

    @Test
    @DisplayName("Serialization of all Radar options")
    public void radarSerializationTest() throws IOException, URISyntaxException, JSONException {
        JmixChartSerializer chartSerializer = new JmixChartSerializer();

        ChartOptions chartOptions = new ChartOptions(new JmixChart());

        Radar radar = new Radar()
                .withId("radarId")
                .withZLevel(15)
                .withZ(12)
                .withCenter("213", "23")
                .withRadius("50%", "50%")
                .withStartAngle(45)
                .withAxisName(new Radar.AxisName()
                        .withShow(true)
                        .withFormatter("[{value}]")
                        .withColor(Color.IVORY)
                        .withFontStyle(FontStyle.ITALIC)
                        .withFontWeight("bold")
                        .withFontFamily("monospace")
                        .withFontSize(40)
                        .withLineHeight(56)
                        .withBackgroundColor(Color.IVORY)
                        .withBorderColor(Color.IVORY)
                        .withBorderWidth(321)
                        .withBorderType("dashed")
                        .withBorderDashOffset(312)
                        .withBorderRadius(75)
                        .withPadding(1, 2, 3, 4)
                        .withShadowColor(Color.IVORY)
                        .withShadowBlur(312)
                        .withShadowOffsetX(12)
                        .withShadowOffsetY(84)
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
                .withNameGap(123)
                .withSplitNumber(43)
                .withShape(Radar.Shape.CIRCLE)
                .withScale(true)
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
                .withIndicators(
                        new Radar.Indicator()
                                .withName("first")
                                .withMax(6000)
                                .withMin(1000)
                                .withColor(Color.IVORY),
                        new Radar.Indicator()
                                .withName("second")
                                .withMax(10000)
                                .withMin(5000)
                                .withColor(Color.IVORY),
                        new Radar.Indicator()
                                .withName("third")
                                .withMax(2000)
                                .withMin(500)
                                .withColor(Color.IVORY)
                );

        chartOptions.setRadar(radar);
        JsonValue serializedJson = chartSerializer.serialize(chartOptions);

        String expectedJson = readFile("radar-result.json");

        JSONAssert.assertEquals(expectedJson, serializedJson.toJson(), true);
    }

    @Test
    @DisplayName("Serialization of all Tooltip options")
    public void tooltipSerializationTest() throws IOException, URISyntaxException, JSONException {
        JmixChartSerializer chartSerializer = new JmixChartSerializer();

        ChartOptions chartOptions = new ChartOptions(new JmixChart());

        Tooltip tooltip = new Tooltip()
                .withShow(true)
                .withTrigger(AbstractTooltip.Trigger.AXIS)
                .withAxisPointer(
                        new AbstractTooltip.AxisPointer()
                                .withType(AbstractTooltip.AxisPointer.IndicatorType.CROSS)
                                .withAxis(AbstractTooltip.AxisPointer.AxisType.RADIUS)
                                .withSnap(true)
                                .withZ(12)
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
                                .withCrossStyle(
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
                                .withAnimation(true)
                                .withAnimationThreshold(1501)
                                .withAnimationDuration(1500)
                                .withAnimationEasing("backOut")
                                .withAnimationDelay(15)
                                .withAnimationDurationUpdate(150)
                                .withAnimationEasingUpdate("cubicOut")
                                .withAnimationDelayUpdate(15)
                )
                .withShowContent(true)
                .withAlwaysShowContent(true)
                .withTriggerOn(TriggerOnMode.MOUSE_MOVE_CLICK)
                .withShowDelay(500)
                .withHideDelay(100)
                .withEnterable(false)
                .withRenderMode(Tooltip.RenderMode.RICH_TEXT)
                .withConfine(true)
                .withAppendToBody(true)
                .withClassName("className")
                .withTransitionDuration(0.500)
                .withPosition(AbstractTooltip.Position.ItemTriggerPosition.TOP)
                .withFormatter("formatter")
                .withValueFormatter("valueFormatter")
                .withBackgroundColor(Color.IVORY)
                .withBorderColor(Color.IVORY)
                .withBorderWidth(15)
                .withPadding(1, 2, 3, 4)
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
                .withExtraCssText("extraCssText")
                .withOrder(Tooltip.OrderType.SERIES_DESC);

        chartOptions.setTooltip(tooltip);
        JsonValue serializedJson = chartSerializer.serialize(chartOptions);

        String expectedJson = readFile("tooltip-result.json");

        JSONAssert.assertEquals(expectedJson, serializedJson.toJson(), true);
    }

    @Test
    @DisplayName("Serialization of all AxisPointer options")
    public void axisPointerSerializationTest() throws IOException, URISyntaxException, JSONException {
        JmixChartSerializer chartSerializer = new JmixChartSerializer();

        ChartOptions chartOptions = new ChartOptions(new JmixChart());

        AxisPointer axisPointer = new AxisPointer()
                .withId("axisPointerId")
                .withShow(true)
                .withType(AxisPointer.IndicatorType.SHADOW)
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
                        new AxisPointer.Handle()
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
                .withTriggerOn(TriggerOnMode.MOUSE_MOVE_CLICK);

        chartOptions.setAxisPointer(axisPointer);
        JsonValue serializedJson = chartSerializer.serialize(chartOptions);

        String expectedJson = readFile("axis-pointer-result.json");

        JSONAssert.assertEquals(expectedJson, serializedJson.toJson(), true);
    }

    @Test
    @DisplayName("Serialization of all base chart options")
    public void baseChartOptionsSerializationTest() throws IOException, URISyntaxException, JSONException {
        JmixChartSerializer chartSerializer = new JmixChartSerializer();

        ChartOptions chartOptions = new ChartOptions(new JmixChart());

        chartOptions.setColorPalette(Color.AQUA, Color.RED, Color.GREEN);
        chartOptions.setBackgroundColor(Color.BLACK);
        chartOptions.setTextStyle(
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
        );
        chartOptions.setAnimation(true);
        chartOptions.setAnimationThreshold(1501);
        chartOptions.setAnimationDuration(1500);
        chartOptions.setAnimationEasing("backOut");
        chartOptions.setAnimationDelay(15);
        chartOptions.setAnimationDurationUpdate(150);
        chartOptions.setAnimationEasingUpdate("cubicOut");
        chartOptions.setAnimationDelayUpdate(15);
        chartOptions.setStateAnimation(
                new ChartOptions.StateAnimation()
                        .withDuration(500)
                        .withEasing("cubicIn")
        );
        chartOptions.setBlendMode(ChartOptions.BlendMode.LIGHTER);
        chartOptions.setHoverLayerThreshold(6000);
        chartOptions.setUseUtc(true);

        JsonValue serializedJson = chartSerializer.serialize(chartOptions);

        String expectedJson = readFile("base-chart-options-result.json");

        JSONAssert.assertEquals(expectedJson, serializedJson.toJson(), true);
    }

    @Test
    @DisplayName("Serialization of Toolbox options")
    public void toolboxSerializationTest() throws IOException, URISyntaxException, JSONException {
        JmixChartSerializer chartSerializer = new JmixChartSerializer();

        ChartOptions chartOptions = new ChartOptions(new JmixChart());

        Toolbox toolbox = new Toolbox()
                .withId("toolboxId")
                .withShow(true)
                .withOrientation(Orientation.VERTICAL)
                .withItemSize(15)
                .withItemGap(7)
                .withShowTitle(false)
                .withFeatures(
                        new SaveAsImageFeature()
                                .withType(SaveAsImageFeature.SaveType.JPG)
                                .withName("saveAsImage")
                                .withBackgroundColor(Color.IVORY)
                                .withConnectedBackgroundColor(Color.IVORY)
                                .withExcludeComponents("toolbox", "tooltip")
                                .withShow(false)
                                .withTitle("Save")
                                .withIcon("someIcon")
                                .withPixelRatio(15)
                                .withEmphasis(
                                        new Emphasis()
                                                .withIconStyle(
                                                        new Emphasis.IconStyle()
                                                                .withPadding(1, 2, 3, 4)
                                                )
                                ),
                        new RestoreFeature()
                                .withShow(false)
                                .withTitle("restore")
                                .withIcon("someIcon"),
                        new DataZoomFeature()
                                .withShow(false)
                                .withTitle(
                                        new DataZoomFeature.Title()
                                                .withZoom("titleZoom")
                                                .withBack("titleBack")
                                )
                                .withIcon(
                                        new DataZoomFeature.Icon()
                                                .withZoom("iconZoom")
                                                .withBack("iconBack")
                                )
                                .withFilterMode(AbstractDataZoom.FilterMode.WEAK_FILTER),
                        new MagicTypeFeature()
                                .withShow(false)
                                .withTypes(
                                        MagicTypeFeature.MagicType.LINE,
                                        MagicTypeFeature.MagicType.BAR,
                                        MagicTypeFeature.MagicType.STACK
                                )
                                .withTitle(
                                        new MagicTypeFeature.Title()
                                                .withLine("line")
                                                .withBar("bar")
                                                .withStack("stack")
                                                .withTiled("tile")
                                )
                                .withIcon(
                                        new MagicTypeFeature.Icon()
                                                .withLine("lineIcon")
                                                .withBar("barIcon")
                                                .withStack("stackIcon")
                                ),
                        new BrushFeature()
                                .withTypes(BrushFeature.BrushType.POLYGON)
                                .withIcon(
                                        new BrushFeature.Icon()
                                                .withPolygon("polygonIcon")
                                )
                                .withTitle(
                                        new BrushFeature.Title()
                                                .withPolygon("polygonTitle")
                                )
                )
                .withZLevel(14)
                .withZ(4)
                .withLeft("100%")
                .withRight("100%")
                .withTop("100%")
                .withBottom("100%")
                .withWidth("100%")
                .withHeight("100%")
                .withEmphasis(
                        new Emphasis()
                                .withIconStyle(
                                        new Emphasis.IconStyle()
                                                .withPadding(1, 2, 3, 4)
                                )
                );

        chartOptions.setToolbox(toolbox);

        JsonValue serializedJson = chartSerializer.serialize(chartOptions);

        String expectedJson = readFile("toolbox-result.json");

        JSONAssert.assertEquals(expectedJson, serializedJson.toJson(), true);
    }

    @Test
    @DisplayName("Serialization of all Aria options")
    public void ariaSerializationTest() throws IOException, URISyntaxException, JSONException {
        JmixChartSerializer chartSerializer = new JmixChartSerializer();

        ChartOptions chartOptions = new ChartOptions(
                new JmixChart()
        );

        Aria aria = new Aria()
                .withEnabled(true)
                .withLabel(
                        new Aria.Label()
                                .withEnabled(true)
                                .withDescription("This is a test chart description.")
                                .withGeneral(
                                        new Aria.Label.General()
                                                .withWithTitle("This is a test chart about {title}.")
                                                .withWithoutTitle("This is a test chart.")
                                )
                                .withSeries(
                                        new Aria.Label.Series()
                                                .withMaxCount(5)
                                                .withSingle(
                                                        new Aria.Label.Series.Single()
                                                                .withPrefix(" with {seriesCount} series count.")
                                                                .withWithName(" with type {seriesType} named {seriesName}.")
                                                                .withWithoutName(" with type {seriesType}.")
                                                )
                                                .withMultiple(
                                                        new Aria.Label.Series.Multiple()
                                                                .withPrefix(" It consists of {seriesCount} series count.")
                                                                .withWithName(" The {seriesId} series is a {seriesType} representing {seriesName}.")
                                                                .withWithoutName(" The {seriesId} series is a {seriesType}.")
                                                                .withSeparator(
                                                                        new Separator()
                                                                                .withMiddle(";")
                                                                                .withEnd(".")
                                                                )
                                                )
                                )
                                .withData(
                                        new Aria.Label.Data()
                                                .withMaxCount(5)
                                                .withAllData("whose data is --")
                                                .withPartialData("where the first {displayCnt} term is --")
                                                .withWithName("The data for {name} is {value}")
                                                .withWithoutName("{value}")
                                                .withSeparator(
                                                        new Separator()
                                                                .withMiddle(",")
                                                                .withEnd("")
                                                )
                                )
                )
                .withDecal(
                        new Aria.Decal()
                                .withShow(true)
                                .withDecals(
                                        new Decal()
                                                .withSymbol("rect")
                                                .withSymbolSize(0.8)
                                                .withSymbolKeepAspect(true)
                                                .withColor(Color.IVORY)
                                                .withBackgroundColor(Color.IVORY)
                                                .withDashArrayX(
                                                        new Integer[][]{
                                                                {10},
                                                                {2, 5},
                                                                {8, 3, 6}
                                                        }
                                                )
                                                .withDashArrayY(5, 2)
                                                .withRotation(0.5)
                                                .withMaxTileWidth(512)
                                                .withMaxTileHeight(512),
                                        new Decal()
                                                .withSymbol("circle")
                                                .withSymbolSize(0.6)
                                                .withSymbolKeepAspect(false)
                                                .withColor(Color.IVORY)
                                                .withBackgroundColor(Color.IVORY)
                                                .withDashArrayX(5, 2)
                                                .withDashArrayY(5)
                                                .withRotation(0.3)
                                                .withMaxTileWidth(256)
                                                .withMaxTileHeight(256)
                                )
                );
        chartOptions.setAria(aria);

        JsonValue serializedJson = chartSerializer.serialize(chartOptions);

        String expectedJson = readFile("aria-result.json");

        JSONAssert.assertEquals(expectedJson, serializedJson.toJson(), true);
    }

    @Test
    @DisplayName("Serialization of all brush options")
    public void brushSerializationTest() throws IOException, URISyntaxException, JSONException {
        JmixChartSerializer chartSerializer = new JmixChartSerializer();

        ChartOptions chartOptions = new ChartOptions(new JmixChart());

        Brush brush = new Brush()
                .withId("myBrush")
                .withToolboxes(
                        Brush.Toolbox.RECT,
                        Brush.Toolbox.POLYGON,
                        Brush.Toolbox.KEEP,
                        Brush.Toolbox.CLEAR
                )
                .withBrushLink(0, 1)
                .withSeriesIndex(Brush.SeriesIndex.ALL)
                .withGeoIndex(Brush.BrushSelectMode.ALL)
                .withXAxisIndex(1)
                .withYAxisIndex(0, 1)
                .withBrushType(Brush.BrushType.RECT)
                .withBrushMode(Brush.BrushMode.SINGLE)
                .withTransformable(true)
                .withBrushStyle(
                        new Brush.BrushStyle()
                                .withBorderWidth(1)
                                .withColor(Color.IVORY)
                                .withBorderColor(Color.IVORY)
                )
                .withThrottleType(Brush.ThrottleType.FIX_RATE)
                .withThrottleDelay(300.0)
                .withRemoveOnClick(true)
                .withInBrush(
                        new VisualEffect()
                                .withSymbol("circle")
                                .withSymbolSize(10, 10)
                                .withColor(Color.IVORY)
                                .withOpacity(1.0, 1.0)
                )
                .withOutOfBrush(
                        new VisualEffect()
                                .withSymbol("circle")
                                .withSymbolSize(8, 8)
                                .withColor(Color.IVORY)
                                .withOpacity(0.7, 0.7)
                )
                .withZ(10000);

        chartOptions.setBrush(brush);

        JsonValue serializedJson = chartSerializer.serialize(chartOptions);

        String expectedJson = readFile("brush-result.json");

        JSONAssert.assertEquals(expectedJson, serializedJson.toJson(), true);
    }
}
