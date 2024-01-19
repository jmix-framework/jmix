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

package component_xml_load;

import component_xml_load.view.*;
import io.jmix.chartsflowui.component.Chart;
import io.jmix.chartsflowui.kit.component.model.*;
import io.jmix.chartsflowui.kit.component.model.axis.*;
import io.jmix.chartsflowui.kit.component.model.datazoom.AbstractDataZoom;
import io.jmix.chartsflowui.kit.component.model.datazoom.InsideDataZoom;
import io.jmix.chartsflowui.kit.component.model.datazoom.SliderDataZoom;
import io.jmix.chartsflowui.kit.component.model.legend.AbstractLegend;
import io.jmix.chartsflowui.kit.component.model.legend.LegendType;
import io.jmix.chartsflowui.kit.component.model.shared.*;
import io.jmix.chartsflowui.kit.component.model.toolbox.BrushFeature;
import io.jmix.chartsflowui.kit.component.model.toolbox.Emphasis;
import io.jmix.chartsflowui.kit.component.model.toolbox.Toolbox;
import io.jmix.chartsflowui.kit.component.model.toolbox.ToolboxFeature;
import io.jmix.chartsflowui.kit.component.model.visualMap.AbstractVisualMap;
import io.jmix.chartsflowui.kit.component.model.visualMap.ContinuousVisualMap;
import io.jmix.chartsflowui.kit.component.model.visualMap.PiecewiseVisualMap;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;
import org.springframework.boot.test.context.SpringBootTest;
import test_support.AbstractXmlLoadTest;
import test_support.ChartsFlowuiTestConfiguration;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static io.jmix.chartsflowui.kit.component.model.ChartOptions.BlendMode.LIGHTER;
import static io.jmix.chartsflowui.kit.component.model.Title.Target.BLANK;
import static io.jmix.chartsflowui.kit.component.model.shared.Color.*;
import static io.jmix.chartsflowui.kit.component.model.shared.FontStyle.ITALIC;
import static io.jmix.chartsflowui.kit.component.model.shared.Overflow.BREAK_ALL;
import static org.junit.jupiter.api.Assertions.*;

@UiTest(viewBasePackages = {"component_xml_load.view"})
@SpringBootTest(classes = {ChartsFlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class})
public class ChartOptionXmlLoadTest extends AbstractXmlLoadTest {

    @Test
    @DisplayName("Load Chart options from XML")
    public void loadChartOptionsFromXmlTest() {
        Chart chart = navigateTo(ChartOptionTestView.class).chartId;

        assertEquals("chartId", chart.getId().orElse(null));
        assertLinesMatch(Stream.of("cssClassName1", "cssClassName2"), chart.getClassNames().stream());
        assertEquals("red", chart.getStyle().get("color"));
        assertIterableEquals(List.of(RED, GREEN, BLUE), chart.getColorPalette());
        assertEquals(BLUE, chart.getBackgroundColor());
        assertTrue(chart.getAnimation());
        assertEquals(1501, chart.getAnimationThreshold());
        assertEquals(1500, chart.getAnimationDuration());
        assertEquals("backOut", chart.getAnimationEasing());
        assertEquals(15, chart.getAnimationDelay());
        assertEquals(150, chart.getAnimationDurationUpdate());
        assertEquals("cubicOut", chart.getAnimationEasingUpdate());
        assertEquals(15, chart.getAnimationDelayUpdate());
        assertEquals(LIGHTER, chart.getBlendMode());
        assertEquals(6000, chart.getHoverLayerThreshold());
        assertTrue(chart.getUseUtc());
        assertEquals("50px", chart.getHeight());
        assertEquals("55px", chart.getMaxHeight());
        assertEquals("120px", chart.getMaxWidth());
        assertEquals("40px", chart.getMinHeight());
        assertEquals("80px", chart.getMinWidth());
        assertTrue(chart.isVisible());
        assertEquals("100px", chart.getWidth());

        assertTrue(new ReflectionEquals(
                        new ChartOptions.StateAnimation()
                                .withDuration(500)
                                .withEasing("cubicIn"),
                        "listener"
                ).matches(chart.getStateAnimation())
        );

        assertTrue(new ReflectionEquals(
                        new TextStyle()
                                .withColor(BLACK)
                                .withFontStyle(ITALIC)
                                .withFontWeight("bolder")
                                .withFontFamily("monospace")
                                .withFontSize(15)
                                .withLineHeight(13)
                                .withWidth(150)
                                .withHeight(250)
                                .withTextBorderColor(BISQUE)
                                .withTextBorderWidth(123D)
                                .withTextBorderType("dashed")
                                .withTextBorderDashOffset(2)
                                .withTextShadowColor(DEEPPINK)
                                .withTextShadowBlur(12)
                                .withTextShadowOffsetX(42)
                                .withTextShadowOffsetY(42)
                                .withOverflow(BREAK_ALL)
                                .withEllipsis("......"),
                        "listener"
                ).matches(chart.getTextStyle())
        );

        assertTrue(chart.getAria().getEnabled());
    }

    @Test
    @DisplayName("Load Title from XML")
    public void loadTitleFromXmlTest() {
        Chart chart = navigateTo(ChartOptionTitleTestView.class).titleChartId;

        Title title = chart.getTitle();

        assertEquals("title", title.getId());
        assertFalse(title.isShow());
        assertEquals("Title text", title.getText());
        assertEquals("someLink", title.getLink());
        assertEquals(BLANK, title.getTarget());
        assertEquals("Title subtext", title.getSubtext());
        assertEquals("someLink", title.getSublink());
        assertEquals(BLANK, title.getSubtarget());
        assertEquals(DEEPPINK, title.getBackgroundColor());
        assertEquals(DARKTURQUOISE, title.getBorderColor());
        assertEquals(4, title.getBorderRadius());
        assertEquals(5, title.getBorderWidth());
        assertEquals("10%", title.getBottom());
        assertEquals(2, title.getItemGap());
        assertEquals("50%", title.getLeft());
        assertEquals("50%", title.getRight());
        assertEquals(13, title.getShadowBlur());
        assertEquals(DARKVIOLET, title.getShadowColor());
        assertEquals(4, title.getShadowOffsetX());
        assertEquals(5, title.getShadowOffsetY());
        assertEquals(Title.TextAlign.AUTO, title.getTextAlign());
        assertEquals(Title.TextVerticalAlign.AUTO, title.getTextVerticalAlign());
        assertEquals("50%", title.getTop());
        assertFalse(title.isTriggerEvent());
        assertEquals(1, title.getZ());
        assertEquals(3, title.getZLevel());
        assertTrue(new ReflectionEquals(
                new Padding(1, 3)
        ).matches(title.getPadding()));

        Title.TextStyle textStyle = title.getTextStyle();

        assertEquals(DARKSLATEGREY, textStyle.getColor());
        assertEquals("....", textStyle.getEllipsis());
        assertEquals("monospace", textStyle.getFontFamily());
        assertEquals(4, textStyle.getFontSize());
        assertEquals(ITALIC, textStyle.getFontStyle());
        assertEquals("bolder", textStyle.getFontWeight());
        assertEquals(12, textStyle.getHeight());
        assertEquals(5, textStyle.getLineHeight());
        assertEquals(BREAK_ALL, textStyle.getOverflow());
        assertEquals(DARKSLATEGREY, textStyle.getTextBorderColor());
        assertEquals(3, textStyle.getTextBorderDashOffset());
        assertEquals("dashed", textStyle.getTextBorderType());
        assertEquals(1, textStyle.getTextBorderWidth());
        assertEquals(3, textStyle.getTextShadowBlur());
        assertEquals(DARKSLATEGRAY, textStyle.getTextShadowColor());
        assertEquals(1, textStyle.getTextShadowOffsetX());
        assertEquals(1, textStyle.getTextShadowOffsetY());
        assertEquals(5, textStyle.getWidth());

        Map<String, RichStyle> richStyles = textStyle.getRichStyles();
        RichStyle richStyle = richStyles.get("testStyle");

        assertNotNull(richStyle);
        assertEquals(Align.CENTER, richStyle.getAlign());
        assertEquals(DEEPPINK, richStyle.getBackgroundColor());
        assertEquals(DIMGREY, richStyle.getBorderColor());
        assertEquals(2, richStyle.getBorderDashOffset());
        assertEquals(3, richStyle.getBorderRadius());
        assertEquals("dashed", richStyle.getBorderType());
        assertEquals(12, richStyle.getBorderWidth());
        assertEquals(DIMGRAY, richStyle.getColor());
        assertEquals("monospace", richStyle.getFontFamily());
        assertEquals(3, richStyle.getFontSize());
        assertEquals(ITALIC, richStyle.getFontStyle());
        assertEquals("bolder", richStyle.getFontWeight());
        assertEquals(15, richStyle.getHeight());
        assertEquals(12, richStyle.getLineHeight());

        RichStyle secondRichStyle = richStyles.get("secondTestStyle");

        assertNotNull(secondRichStyle);
        assertEquals(12, secondRichStyle.getShadowBlur());
        assertEquals(DIMGRAY, secondRichStyle.getShadowColor());
        assertEquals(4, secondRichStyle.getShadowOffsetX());
        assertEquals(1, secondRichStyle.getShadowOffsetY());
        assertEquals(DEEPSKYBLUE, secondRichStyle.getTextBorderColor());
        assertEquals(3, secondRichStyle.getTextBorderDashOffset());
        assertEquals("dashed", secondRichStyle.getTextBorderType());
        assertEquals(2, secondRichStyle.getTextBorderWidth());
        assertEquals(1, secondRichStyle.getTextShadowBlur());
        assertEquals(DARKTURQUOISE, secondRichStyle.getTextShadowColor());
        assertEquals(1, secondRichStyle.getTextShadowOffsetX());
        assertEquals(1, secondRichStyle.getTextShadowOffsetY());
        assertEquals(VerticalAlign.BOTTOM, secondRichStyle.getVerticalAlign());
        assertEquals(4, secondRichStyle.getWidth());
        assertTrue(new ReflectionEquals(
                new Padding(1, 4, 5, 6)
        ).matches(secondRichStyle.getPadding()));

        Title.SubtextStyle subtextStyle = title.getSubtextStyle();

        assertEquals(Align.CENTER, subtextStyle.getAlign());
        assertEquals(DARKSLATEGREY, subtextStyle.getColor());
        assertEquals("....", subtextStyle.getEllipsis());
        assertEquals("monospace", subtextStyle.getFontFamily());
        assertEquals(4, subtextStyle.getFontSize());
        assertEquals(ITALIC, subtextStyle.getFontStyle());
        assertEquals("bolder", subtextStyle.getFontWeight());
        assertEquals(12, subtextStyle.getHeight());
        assertEquals(5, subtextStyle.getLineHeight());
        assertEquals(BREAK_ALL, subtextStyle.getOverflow());
        assertEquals(DARKSLATEGREY, subtextStyle.getTextBorderColor());
        assertEquals(3, subtextStyle.getTextBorderDashOffset());
        assertEquals("dashed", subtextStyle.getTextBorderType());
        assertEquals(1, subtextStyle.getTextBorderWidth());
        assertEquals(3, subtextStyle.getTextShadowBlur());
        assertEquals(DARKSLATEGRAY, subtextStyle.getTextShadowColor());
        assertEquals(1, subtextStyle.getTextShadowOffsetX());
        assertEquals(1, subtextStyle.getTextShadowOffsetY());
        assertEquals(VerticalAlign.BOTTOM, subtextStyle.getVerticalAlign());
        assertEquals(5, subtextStyle.getWidth());
    }

    @Test
    @DisplayName("Load Legend from XML")
    public void loadLegendFromXmlTest() {
        Chart chart = navigateTo(ChartOptionLegendTestView.class).legendChartId;

        AbstractLegend<?> legend = chart.getLegend();

        assertEquals("legend", legend.getId());
        assertEquals(LegendType.PLAIN, legend.getType());
        assertTrue(legend.getShow());
        assertTrue(legend.getSelector());
        assertEquals(0, legend.getZLevel());
        assertEquals(2, legend.getZ());
        assertEquals("10", legend.getLeft());
        assertEquals("10", legend.getTop());
        assertEquals("10", legend.getRight());
        assertEquals("10", legend.getBottom());
        assertEquals("100", legend.getWidth());
        assertEquals("100", legend.getHeight());
        assertEquals(Orientation.HORIZONTAL, legend.getOrientation());
        assertEquals(AbstractLegend.Align.AUTO, legend.getAlign());
        assertTrue(new ReflectionEquals(
                new Padding(5, 5)
        ).matches(legend.getPadding()));
        assertEquals(10, legend.getItemGap());
        assertEquals(25, legend.getItemWidth());
        assertEquals(14, legend.getItemHeight());
        assertEquals(0, legend.getSymbolRotate());
        assertEquals("Legend {name}", legend.getFormatter());
        assertEquals("function (name) { return 'Legend ' + name; }", legend.getFormatterFunction().getCode());
        assertEquals(SelectedMode.SINGLE, legend.getSelectedMode());
        assertEquals(GRAY, legend.getInactiveColor());
        assertEquals(GRAY, legend.getInactiveBorderColor());
        assertEquals(1, legend.getInactiveBorderWidth());
        assertEquals(BLUE, legend.getBackgroundColor());
        assertEquals(BLACK, legend.getBorderColor());
        assertEquals(1, legend.getBorderWidth());
        assertEquals(4, legend.getBorderRadius());
        assertEquals(10, legend.getShadowBlur());
        assertEquals(BLACK, legend.getShadowColor());
        assertEquals(0, legend.getShadowOffsetX());
        assertEquals(0, legend.getShadowOffsetY());
        assertEquals(AbstractLegend.Position.START, legend.getSelectorPosition());
        assertEquals(7, legend.getSelectorItemGap());
        assertEquals(10, legend.getSelectorButtonGap());

        ItemStyle itemStyle = legend.getItemStyle();

        assertEquals(WHITE, itemStyle.getColor());
        assertEquals(BLACK, itemStyle.getBorderColor());
        assertEquals(1, itemStyle.getBorderWidth());
        assertEquals("inherit", itemStyle.getBorderType());
        assertEquals(5, itemStyle.getBorderDashOffset());
        assertEquals(HasLineStyle.Cap.ROUND, itemStyle.getCap());
        assertEquals(HasLineStyle.Join.ROUND, itemStyle.getJoin());
        assertEquals(10, itemStyle.getMiterLimit());
        assertEquals(1, itemStyle.getShadowBlur());
        assertEquals(BLACK, itemStyle.getShadowColor());
        assertEquals(0, itemStyle.getShadowOffsetX());
        assertEquals(0, itemStyle.getShadowOffsetY());
        assertEquals(0.9, itemStyle.getOpacity());

        LineStyle lineStyle = legend.getLineStyle();

        assertEquals(BLACK, lineStyle.getColor());
        assertEquals(1, lineStyle.getWidth());
        assertEquals(HasLineStyle.Cap.ROUND, lineStyle.getCap());
        assertEquals(HasLineStyle.Join.ROUND, lineStyle.getJoin());
        assertEquals(0, lineStyle.getMiterLimit());
        assertEquals(0, lineStyle.getShadowBlur());
        assertEquals(0, lineStyle.getMiterLimit());
        assertEquals(BLACK, lineStyle.getShadowColor());
        assertEquals(0, lineStyle.getShadowOffsetX());
        assertEquals(0, lineStyle.getShadowOffsetY());
        assertEquals(0.9, lineStyle.getOpacity());

        AbstractLegend.TextStyle textStyle = legend.getTextStyle();

        assertEquals(DARKSLATEGREY, textStyle.getColor());
        assertEquals("....", textStyle.getEllipsis());
        assertEquals("monospace", textStyle.getFontFamily());
        assertEquals(4, textStyle.getFontSize());
        assertEquals(ITALIC, textStyle.getFontStyle());
        assertEquals("bolder", textStyle.getFontWeight());
        assertEquals(12, textStyle.getHeight());
        assertEquals(5, textStyle.getLineHeight());
        assertEquals(BREAK_ALL, textStyle.getOverflow());
        assertEquals(DARKSLATEGREY, textStyle.getTextBorderColor());
        assertEquals(3, textStyle.getTextBorderDashOffset());
        assertEquals("dashed", textStyle.getTextBorderType());
        assertEquals(1, textStyle.getTextBorderWidth());
        assertEquals(3, textStyle.getTextShadowBlur());
        assertEquals(DARKSLATEGRAY, textStyle.getTextShadowColor());
        assertEquals(1, textStyle.getTextShadowOffsetX());
        assertEquals(1, textStyle.getTextShadowOffsetY());
        assertEquals(5, textStyle.getWidth());

        Tooltip tooltip = legend.getTooltip();
        assertEquals(0, tooltip.getPadding().getLeft());

        AbstractLegend.Emphasis emphasis = legend.getEmphasis();
        AbstractLegend.SelectorLabel selectorLabel = emphasis.getSelectorLabel();
        assertTrue(selectorLabel.getShow());
        assertEquals(5, selectorLabel.getDistance());
        assertEquals(5, selectorLabel.getRotate());
        assertEquals(5, selectorLabel.getOffset()[0]);
        assertEquals(2, selectorLabel.getOffset()[1]);
        assertEquals(BLACK, selectorLabel.getColor());
        assertEquals("normal", selectorLabel.getFontStyle().getId());
        assertEquals("NORMAL", selectorLabel.getFontWeight());
        assertEquals("sans-serif", selectorLabel.getFontFamily());
        assertEquals(12, selectorLabel.getFontSize());
        assertEquals(Align.CENTER, selectorLabel.getAlign());
        assertEquals(VerticalAlign.MIDDLE, selectorLabel.getVerticalAlign());
        assertEquals(12, selectorLabel.getLineHeight());
        assertEquals(WHITE, selectorLabel.getBackgroundColor());
        assertEquals(BLACK, selectorLabel.getBorderColor());
        assertEquals(1, selectorLabel.getBorderWidth());
        assertEquals("solid", selectorLabel.getBorderType());
        assertEquals(5, selectorLabel.getBorderDashOffset());
        assertEquals(5, selectorLabel.getBorderRadius());
        assertEquals(5, selectorLabel.getPadding().getLeft());
        assertEquals(BLACK, selectorLabel.getShadowColor());
        assertEquals(1, selectorLabel.getShadowBlur());
        assertEquals(5, selectorLabel.getShadowOffsetX());
        assertEquals(5, selectorLabel.getShadowOffsetY());
        assertEquals(200, selectorLabel.getWidth());
        assertEquals(100, selectorLabel.getHeight());
        assertEquals(BLACK, selectorLabel.getTextBorderColor());
        assertEquals(200, selectorLabel.getTextBorderWidth());
        assertEquals("solid", selectorLabel.getTextBorderType());
        assertEquals(0, selectorLabel.getTextBorderDashOffset());
        assertEquals(BLACK, selectorLabel.getTextShadowColor());
        assertEquals(0, selectorLabel.getTextShadowBlur());
        assertEquals(0, selectorLabel.getTextShadowOffsetX());
        assertEquals(0, selectorLabel.getTextShadowOffsetY());
        assertEquals(Overflow.BREAK, selectorLabel.getOverflow());
        Map<String, RichStyle> richStyleMap = selectorLabel.getRichStyles();
        assertEquals(12, richStyleMap.get("legendTestStyle").getShadowBlur());
    }

    @Test
    @DisplayName("Load Grid from XML")
    public void loadGridFromXmlTest() {
        Chart chart = navigateTo(ChartOptionGridTestView.class).gridChartId;
        Grid grid = chart.getGrids().stream().findAny().orElseThrow();

        assertEquals("grid", grid.getId());
        assertTrue(grid.getShow());
        assertEquals(0, grid.getZLevel());
        assertEquals(0, grid.getZ());
        assertEquals("0", grid.getLeft());
        assertEquals("0", grid.getTop());
        assertEquals("0", grid.getRight());
        assertEquals("0", grid.getBottom());
        assertEquals("200", grid.getWidth());
        assertEquals("100", grid.getHeight());
        assertTrue(grid.getContainLabel());
        assertEquals(WHITE, grid.getBackgroundColor());
        assertEquals(BLACK, grid.getBorderColor());
        assertEquals(200, grid.getBorderWidth());
        assertEquals(5, grid.getShadowBlur());
        assertEquals(BLACK, grid.getShadowColor());
        assertEquals(5, grid.getShadowOffsetX());
        assertEquals(5, grid.getShadowOffsetY());

        InnerTooltip tooltip = grid.getTooltip();
        assertEquals(0, tooltip.getPadding().getLeft());
    }

    @Test
    @DisplayName("Load Polar options from XML")
    public void loadPolarOptionsFromXmlTest() {
        Chart chart = navigateTo(ChartOptionPolarTestView.class).polarChartId;

        assertEquals("polarChartId", chart.getId().orElse(null));
        Polar polar = chart.getPolar();
        assertEquals("polar", polar.getId());
        assertEquals(0, polar.getZ());
        assertEquals(0, polar.getZLevel());
        assertEquals("50%", polar.getCenter()[0]);
        assertEquals("10", polar.getRadius()[0]);
    }

    @Test
    @DisplayName("Load Radar options from XML")
    public void loadRadarOptionsFromXmlTest() {
        Chart chart = navigateTo(ChartOptionRadarTestView.class).radarChartId;

        Radar radar = chart.getRadar();

        List<Radar.Indicator> indicators = radar.getIndicators();
        assertEquals(2, indicators.size());

        Radar.Indicator indicator1 = indicators.get(0);
        assertEquals("indicator1", indicator1.getName());
        assertEquals(300, indicator1.getMax());
        assertEquals(10, indicator1.getMin());
        assertEquals(BURLYWOOD, indicator1.getColor());

        Radar.Indicator indicator2 = indicators.get(1);
        assertEquals("indicator2", indicator2.getName());
        assertEquals(250, indicator2.getMax());
        assertEquals(20, indicator2.getMin());
        assertEquals(CHOCOLATE, indicator2.getColor());

        assertEquals("radar", radar.getId());
        assertEquals(0, radar.getNameGap());
        assertEquals("100", radar.getRadius()[0]);
        assertEquals("0", radar.getCenter()[0]);
        assertEquals(0, radar.getZ());
        assertEquals(0, radar.getZLevel());
        assertEquals(5, radar.getSplitNumber());
        assertFalse(radar.getTriggerEvent());
        assertTrue(radar.getSilent());
        assertTrue(radar.getScale());
        assertEquals(Radar.Shape.CIRCLE, radar.getShape());
        assertEquals(0, radar.getStartAngle());

        SplitArea splitArea = radar.getSplitArea();
        assertEquals(5, splitArea.getInterval());

        AreaStyle areaStyle = splitArea.getAreaStyle();
        assertEquals(0, areaStyle.getShadowOffsetX());

        SplitLine splitLine = radar.getSplitLine();
        assertEquals(0, splitLine.getInterval());

        AxisLine axisLine = radar.getAxisLine();
        assertFalse(axisLine.getShow());

        AxisTick axisTick = radar.getAxisTick();
        assertTrue(axisTick.getShow());

        LineStyle lineStyle = axisTick.getLineStyle();
        assertEquals(BLACK, lineStyle.getColor());

        AxisLabel axisLabel = radar.getAxisLabel();
        assertTrue(axisLabel.getShow());
    }

    @Test
    @DisplayName("Load Data Zoom options from XML")
    public void loadDataZoomOptionsFromXmlTest() {
        Chart chart = navigateTo(ChartOptionDataZoomTestView.class).dataZoomChartId;
        List<AbstractDataZoom<?>> dataZooms = (List<AbstractDataZoom<?>>) chart.getDataZoom();

        InsideDataZoom insideDataZoom = (InsideDataZoom) dataZooms.get(0);
        assertEquals("insideDataZoom", insideDataZoom.getId());
        assertEquals(Orientation.HORIZONTAL, insideDataZoom.getOrientation());
        assertEquals(0, insideDataZoom.getAngleAxisIndexes()[0]);
        assertFalse(insideDataZoom.getDisabled());
        assertEquals(100.1, insideDataZoom.getEnd());
        assertEquals("100", insideDataZoom.getEndValue());
        assertEquals(AbstractDataZoom.FilterMode.EMPTY, insideDataZoom.getFilterMode());
        assertEquals(100, insideDataZoom.getMaxSpan());
        assertEquals("1000", insideDataZoom.getMaxValueSpan());
        assertEquals(0, insideDataZoom.getMinSpan());
        assertEquals("0", insideDataZoom.getMinValueSpan());
        assertFalse(insideDataZoom.getMoveOnMouseMove());
        assertFalse(insideDataZoom.getMoveOnMouseWheel());
        assertFalse(insideDataZoom.getPreventDefaultMouseMove());
        assertEquals(10, insideDataZoom.getRadiusAxisIndexes()[1]);
        assertEquals(AbstractDataZoom.RangeMode.VALUE, insideDataZoom.getRangeMode()[0]);
        assertEquals(AbstractDataZoom.RangeMode.PERCENT, insideDataZoom.getRangeMode()[1]);
        assertEquals(0, insideDataZoom.getStart());
        assertEquals("0", insideDataZoom.getStartValue());
        assertEquals(100, insideDataZoom.getThrottle());
        assertEquals(10, insideDataZoom.getXAxisIndexes()[1]);
        assertEquals(10, insideDataZoom.getYAxisIndexes()[1]);
        assertFalse(insideDataZoom.getZoomLock());
        assertFalse(insideDataZoom.getZoomOnMouseWheel());

        SliderDataZoom sliderDataZoom = (SliderDataZoom) dataZooms.get(1);
        assertEquals("slider", sliderDataZoom.getId());
        assertEquals(Orientation.HORIZONTAL, sliderDataZoom.getOrientation());
        assertEquals(0, sliderDataZoom.getAngleAxisIndexes()[0]);
        assertEquals(100, sliderDataZoom.getEnd());
        assertEquals("100", sliderDataZoom.getEndValue());
        assertEquals(AbstractDataZoom.FilterMode.FILTER, sliderDataZoom.getFilterMode());
        assertEquals(100, sliderDataZoom.getMaxSpan());
        assertEquals("100", sliderDataZoom.getMaxValueSpan());
        assertEquals(0, sliderDataZoom.getMinSpan());
        assertEquals("0", sliderDataZoom.getMinValueSpan());
        assertEquals(10, sliderDataZoom.getRadiusAxisIndexes()[1]);
        assertEquals(AbstractDataZoom.RangeMode.VALUE, insideDataZoom.getRangeMode()[0]);
        assertEquals(AbstractDataZoom.RangeMode.PERCENT, insideDataZoom.getRangeMode()[1]);
        assertEquals(0, sliderDataZoom.getStart());
        assertEquals("0", sliderDataZoom.getStartValue());
        assertEquals(100, sliderDataZoom.getThrottle());
        assertEquals(10, sliderDataZoom.getXAxisIndexes()[1]);
        assertEquals(10, sliderDataZoom.getYAxisIndexes()[1]);
        assertFalse(sliderDataZoom.getZoomLock());
    }

    @Test
    @DisplayName("Load Visual Map options from XML")
    public void loadVisualMapOptionsFromXmlTest() {
        Chart chart = navigateTo(ChartOptionVisualMapTestView.class).visualMapChartId;
        List<AbstractVisualMap<?>> visualMaps = chart.getVisualMap();

        //noinspection unchecked
        AbstractVisualMap<ContinuousVisualMap> continuousVisualMap =
                (AbstractVisualMap<ContinuousVisualMap>) visualMaps.get(0);

        assertEquals("continuousVisualMap", continuousVisualMap.getId());
        assertEquals(Orientation.VERTICAL, continuousVisualMap.getOrientation());
        assertEquals(0, continuousVisualMap.getMin());
        assertEquals(100, continuousVisualMap.getMax());
        assertEquals(10, continuousVisualMap.getZLevel());
        assertEquals(1200, continuousVisualMap.getZ());
        assertEquals(2, continuousVisualMap.getBorderWidth());
        assertEquals(BLACK, continuousVisualMap.getBorderColor());
        assertEquals("0", continuousVisualMap.getTop());
        assertEquals("0", continuousVisualMap.getLeft());
        assertEquals("0", continuousVisualMap.getRight());
        assertEquals("0", continuousVisualMap.getBottom());
        assertEquals(WHITE, continuousVisualMap.getBackgroundColor());
        assertTrue(continuousVisualMap.getShow());
        assertTrue(continuousVisualMap.getInverse());
        assertEquals(1, continuousVisualMap.getPrecision());
        assertEquals("Continuous Visual Map", continuousVisualMap.getFormatter());
        assertEquals(5, continuousVisualMap.getPadding().getLeft());
        assertEquals(AbstractVisualMap.MapAlign.AUTO, continuousVisualMap.getAlign());
        assertEquals(10, continuousVisualMap.getItemHeight());
        assertEquals("1", continuousVisualMap.getDimension());
        assertTrue(continuousVisualMap.getHoverLink());
        assertEquals(0, continuousVisualMap.getSeriesIndex()[0]);
        assertEquals("Start", continuousVisualMap.getText()[0]);
        assertEquals(10, continuousVisualMap.getTextGap());

        //noinspection unchecked
        AbstractVisualMap<PiecewiseVisualMap> piecewiseVisualMap =
                (AbstractVisualMap<PiecewiseVisualMap>) visualMaps.get(1);

        assertEquals("piecewiseVisualMap", piecewiseVisualMap.getId());
        assertEquals(Orientation.HORIZONTAL, piecewiseVisualMap.getOrientation());
        assertEquals(0, piecewiseVisualMap.getMin());
        assertEquals(100, piecewiseVisualMap.getMax());
        assertEquals(10, piecewiseVisualMap.getZLevel());
        assertEquals(1200, piecewiseVisualMap.getZ());
        assertEquals(2, piecewiseVisualMap.getBorderWidth());
        assertEquals(BLACK, piecewiseVisualMap.getBorderColor());
        assertEquals("0", piecewiseVisualMap.getTop());
        assertEquals("0", piecewiseVisualMap.getLeft());
        assertEquals("0", piecewiseVisualMap.getRight());
        assertEquals("0", piecewiseVisualMap.getBottom());
        assertEquals(WHITE, piecewiseVisualMap.getBackgroundColor());
        assertTrue(piecewiseVisualMap.getShow());
        assertEquals(Orientation.HORIZONTAL, piecewiseVisualMap.getOrientation());
        assertTrue(piecewiseVisualMap.getInverse());
        assertEquals(1, piecewiseVisualMap.getPrecision());
        assertEquals("Piecewise Visual Map", piecewiseVisualMap.getFormatter());
        assertEquals(0, piecewiseVisualMap.getPadding().getLeft());
        assertEquals(AbstractVisualMap.MapAlign.AUTO, piecewiseVisualMap.getAlign());
        assertEquals(10, piecewiseVisualMap.getItemHeight());
        assertEquals("1", piecewiseVisualMap.getDimension());
        assertTrue(piecewiseVisualMap.getHoverLink());
        assertEquals(0, piecewiseVisualMap.getSeriesIndex()[0]);
        assertEquals("Start", piecewiseVisualMap.getText()[0]);
        assertEquals(10, piecewiseVisualMap.getTextGap());
    }

    @Test
    @DisplayName("Load Tooltip options from XML")
    public void loadTooltipOptionsFromXmlTest() {
        Chart chart = navigateTo(ChartOptionTooltipTestView.class).tooltipChartId;
        Tooltip tooltip = chart.getTooltip();

        assertTrue(tooltip.getShow());
        assertEquals(AbstractTooltip.Trigger.ITEM, tooltip.getTrigger());
        assertEquals(10, tooltip.getPadding().getLeft());
        assertEquals("Tooltip", tooltip.getFormatter());
        assertEquals(WHITE, tooltip.getBackgroundColor());
        assertEquals(BLACK, tooltip.getBorderColor());
        assertEquals(1, tooltip.getBorderWidth());
        assertEquals("0", tooltip.getPosition().getCoordinates()[0]);
        assertTrue(tooltip.getAlwaysShowContent());
        assertFalse(tooltip.getAppendToBody());
        assertEquals("tooltip-chart", tooltip.getClassName());
        assertTrue(tooltip.getConfine());
        assertTrue(tooltip.getEnterable());
        assertEquals("box-shadow: 0 0 3px rgba(0, 0, 0, 0.3);", tooltip.getExtraCssText());
        assertEquals(300, tooltip.getHideDelay());
        assertEquals(Tooltip.OrderType.SERIES_ASC, tooltip.getOrder());
        assertEquals("5", tooltip.getPosition().getCoordinates()[1]);
        assertEquals(Tooltip.RenderMode.HTML, tooltip.getRenderMode());
        assertTrue(tooltip.getShowContent());
        assertEquals(100, tooltip.getShowDelay());
        assertEquals(0.2, tooltip.getTransitionDuration());
        assertEquals(TriggerOnMode.CLICK, tooltip.getTriggerOn());
        assertEquals("(value) => '$' + value.toFixed(2)", tooltip.getValueFormatter());
        assertEquals(ITALIC, tooltip.getTextStyle().getFontStyle());

        AbstractTooltip.AxisPointer axisPointer = tooltip.getAxisPointer();
        assertEquals(AbstractTooltip.AxisPointer.IndicatorType.LINE, axisPointer.getType());
        assertEquals(BLACK, axisPointer.getLabel().getBorderColor());
        assertEquals(BLACK, axisPointer.getLineStyle().getColor());
        assertEquals(0, axisPointer.getLineStyle().getShadowOffsetX());
    }

    @Test
    @DisplayName("Load AxisPointer options from XML")
    public void loadAxisPointerOptionsFromXmlTest() {
        Chart chart = navigateTo(ChartOptionAxisPointerTestView.class).axisPointerChartId;
        AxisPointer axisPointer = chart.getAxisPointer();

        assertEquals("axisPointer", axisPointer.getId());
        assertTrue(axisPointer.getShow());
        assertEquals(AbstractAxisPointer.IndicatorType.LINE, axisPointer.getType());
        assertEquals(0, axisPointer.getZ());
        assertTrue(axisPointer.getSnap());
        assertEquals(100, axisPointer.getValue());
        assertTrue(axisPointer.getTriggerEmphasis());
        assertFalse(axisPointer.getTriggerTooltip());
        assertTrue(axisPointer.getStatus());
        assertEquals(BLACK, axisPointer.getLabel().getBorderColor());
        assertEquals(BLACK, axisPointer.getLineStyle().getColor());
        assertEquals(0, axisPointer.getLineStyle().getShadowOffsetX());

        AbstractAxisPointer.Handle handle = axisPointer.getHandle();
        assertTrue(handle.getShow());
        assertEquals(BLACK, handle.getColor());
        assertEquals(2, handle.getShadowBlur());
        assertEquals(BLACK, handle.getShadowColor());
        assertEquals(0, handle.getShadowOffsetX());
        assertEquals(5, handle.getShadowOffsetY());
        assertEquals(50, handle.getThrottle());
        assertEquals(10, handle.getMargin());
        assertEquals("lumo:align-center", handle.getIcon());
        assertEquals(40, handle.getSize()[0]);
    }

    @Test
    @DisplayName("Load Toolbox options from XML")
    public void loadToolboxOptionsFromXmlTest() {
        Chart chart = navigateTo(ChartOptionToolboxTestView.class).toolboxChartId;
        Toolbox toolbox = chart.getToolbox();

        assertEquals("toolbox", toolbox.getId());
        assertTrue(toolbox.getShow());
        assertEquals(5, toolbox.getItemGap());
        assertEquals(Orientation.HORIZONTAL, toolbox.getOrientation());
        assertEquals("0", toolbox.getBottom());
        assertEquals("0", toolbox.getRight());
        assertEquals("10", toolbox.getLeft());
        assertEquals("0", toolbox.getTop());
        assertEquals(0, toolbox.getZ());
        assertEquals(0, toolbox.getZLevel());
        assertEquals("10", toolbox.getHeight());
        assertEquals("100", toolbox.getWidth());
        assertEquals(10, toolbox.getItemSize());
        assertTrue(toolbox.getShowTitle());

        Map<String, ToolboxFeature> features = toolbox.getFeatures();
        BrushFeature brushFeature = (BrushFeature) features.get("brush");
        assertEquals(BrushFeature.BrushType.POLYGON, brushFeature.getTypes()[0]);
        assertEquals("image://clear", brushFeature.getIcon().getClear());

        assertEquals("image://clear", brushFeature.getTitle().getClear());

        Emphasis emphasis = toolbox.getEmphasis();

        Emphasis.IconStyle emphasisIconStyle = emphasis.getIconStyle();
        assertEquals(BLACK, emphasisIconStyle.getBorderColor());

        ItemStyle iconStyle = toolbox.getIconStyle();
        assertEquals(BLACK, iconStyle.getBorderColor());

        Tooltip tooltip = toolbox.getTooltip();
        assertEquals("Toolbox Tooltip", tooltip.getValueFormatter());
    }

    @Test
    @DisplayName("Load Brush options from XML")
    public void loadBrushOptionsFromXmlTest() {
        Chart chart = navigateTo(ChartOptionBrushTestView.class).brushChartId;
        Brush brush = chart.getBrush();

        assertEquals("brush", brush.getId());
        assertEquals(Brush.SeriesIndex.ALL, brush.getSeriesIndex());
        assertEquals(5, brush.getXAxisIndex().getIndexes()[1]);
        assertEquals(10, brush.getYAxisIndex().getIndexes()[2]);
        assertEquals(5, brush.getBrushLink().getBrushLinkIndexes()[1]);
        assertEquals(10, brush.getGeoIndex().getSingleIndex());
        assertEquals(Brush.BrushMode.SINGLE, brush.getBrushMode());
        assertTrue(brush.getTransformable());

        Brush.BrushStyle brushStyle = brush.getBrushStyle();
        assertEquals(BLACK, brushStyle.getColor());
        assertEquals(1, brushStyle.getBorderWidth());
        assertEquals(WHITE, brushStyle.getBorderColor());

        VisualEffect inBrush = brush.getInBrush();
        assertEquals(BLACK, inBrush.getColor()[0]);

        VisualEffect outOfBrush = brush.getOutOfBrush();
        assertEquals(BLACK, outOfBrush.getColor()[0]);
    }
}
