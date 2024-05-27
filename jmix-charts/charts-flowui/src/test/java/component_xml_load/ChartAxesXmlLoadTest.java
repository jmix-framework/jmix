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

import component_xml_load.view.axes.ChartOptionAngleAxisTestView;
import component_xml_load.view.axes.ChartOptionRadiusAxisTestView;
import component_xml_load.view.axes.ChartOptionXAxisTestView;
import component_xml_load.view.axes.ChartOptionYAxisTestView;
import io.jmix.chartsflowui.component.Chart;
import io.jmix.chartsflowui.kit.component.model.axis.*;
import io.jmix.chartsflowui.kit.component.model.shared.*;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import test_support.AbstractXmlLoadTest;
import test_support.ChartsFlowuiTestConfiguration;

import static io.jmix.chartsflowui.kit.component.model.shared.Color.BLACK;
import static io.jmix.chartsflowui.kit.component.model.shared.Color.WHITE;
import static io.jmix.chartsflowui.kit.component.model.shared.FontStyle.ITALIC;
import static io.jmix.chartsflowui.kit.component.model.shared.Overflow.BREAK;
import static org.junit.jupiter.api.Assertions.*;

@UiTest(viewBasePackages = {"component_xml_load.view.axes"})
@SpringBootTest(classes = {ChartsFlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class})
public class ChartAxesXmlLoadTest extends AbstractXmlLoadTest {

    @Test
    @DisplayName("Load XAxis from XML")
    public void loadXAxisFromXmlTest() {
        Chart chart = navigateTo(ChartOptionXAxisTestView.class).xAxisChartId;
        XAxis xAxis = chart.getXAxes().get(0);

        assertEquals("xAxis", xAxis.getId());
        assertEquals(0, xAxis.getGridIndex());
        assertTrue(xAxis.getAlignTicks());
        assertEquals(AbstractCartesianAxis.Position.BOTTOM, xAxis.getPosition());
        assertEquals(0, xAxis.getOffset());
        assertEquals(AxisType.CATEGORY, xAxis.getType());
        assertEquals("xAxis", xAxis.getName());
        assertEquals(HasAxisName.NameLocation.CENTER, xAxis.getNameLocation());
        assertEquals(15, xAxis.getNameGap());
        assertEquals(5, xAxis.getNameRotate());
        assertFalse(xAxis.getInverse());
        assertEquals("5%", xAxis.getBoundaryGap().getNonCategoryGap()[0]);
        assertEquals("5", xAxis.getMin());
        assertEquals("100", xAxis.getMax());
        assertFalse(xAxis.getScale());
        assertEquals(10, xAxis.getSplitNumber());
        assertEquals(10, xAxis.getMinInterval());
        assertEquals(10, xAxis.getMaxInterval());
        assertEquals(10, xAxis.getInterval());
        assertTrue(xAxis.getAnimation());
        assertNull(xAxis.getBoundaryGap().getCategoryGap());
        assertEquals("100", xAxis.getMax());
        assertEquals(2000, xAxis.getAnimationThreshold());
        assertEquals(1000, xAxis.getAnimationDuration());
        assertEquals("cubicOut", xAxis.getAnimationEasing());
        assertEquals(0, xAxis.getAnimationDelay());
        assertEquals("100", xAxis.getMax());
        assertEquals("100", xAxis.getMax());
        assertEquals(300, xAxis.getAnimationDurationUpdate());
        assertEquals("cubicOut", xAxis.getAnimationEasingUpdate());
        assertEquals(0, xAxis.getAnimationDelayUpdate());
        assertEquals(0, xAxis.getZ());
        assertEquals(0, xAxis.getZLevel());

        HasAxisName.NameTextStyle nameTextStyle = xAxis.getNameTextStyle();

        assertEquals(0, nameTextStyle.getShadowOffsetX());
        assertEquals(0, nameTextStyle.getShadowOffsetY());
        assertEquals(BLACK, nameTextStyle.getShadowColor());
        assertEquals(1, nameTextStyle.getShadowBlur());
        assertEquals(1, nameTextStyle.getBorderWidth());
        assertEquals(BLACK, nameTextStyle.getBorderColor());
        assertEquals(WHITE, nameTextStyle.getBackgroundColor());
        assertEquals(0, nameTextStyle.getPadding().getLeft());
        assertEquals("solid", nameTextStyle.getBorderType());
        assertEquals(5, nameTextStyle.getBorderRadius());
        assertEquals(0, nameTextStyle.getBorderDashOffset());
        assertEquals(VerticalAlign.MIDDLE, nameTextStyle.getVerticalAlign());
        assertEquals(Align.CENTER, nameTextStyle.getAlign());
        assertEquals(Align.CENTER, nameTextStyle.getAlign());

        AxisLine axisLine = xAxis.getAxisLine();

        assertFalse(axisLine.getShow());
        assertTrue(axisLine.getOnZero());
        assertEquals(0, axisLine.getOnZeroAxisIndex());
        assertEquals("none", axisLine.getSymbols()[0]);
        assertEquals(0, axisLine.getSymbolsOffset()[0]);
        assertEquals(5, axisLine.getSymbolsSize()[0]);

        AxisTick axisTick = xAxis.getAxisTick();

        assertTrue(axisTick.getShow());
        assertEquals(5, axisTick.getInterval());
        assertTrue(axisTick.getAlignWithLabel());
        assertTrue(axisTick.getInside());
        assertEquals(10, axisTick.getLength());

        LineStyle tickLineStyle = axisTick.getLineStyle();
        assertEquals(BLACK, tickLineStyle.getColor());

        MinorTick minorTick = xAxis.getMinorTick();

        assertEquals(10, minorTick.getLength());
        assertTrue(minorTick.getShow());
        assertEquals(5, minorTick.getSplitNumber());

        LineStyle minorTickLineStyle = minorTick.getLineStyle();
        assertEquals(BLACK, minorTickLineStyle.getColor());

        AxisLabel axisLabel = xAxis.getAxisLabel();

        assertTrue(axisLabel.getShow());
        assertTrue(axisLabel.getInside());
        assertEquals(5, axisLabel.getInterval());
        assertEquals(Align.CENTER, axisLabel.getAlign());
        assertEquals(VerticalAlign.MIDDLE, axisLabel.getVerticalAlign());
        assertEquals(0, axisLabel.getBorderDashOffset());
        assertEquals(5, axisLabel.getBorderRadius());
        assertEquals("solid", axisLabel.getBorderType());
        assertEquals(5, axisLabel.getPadding().getLeft());
        assertEquals(WHITE, axisLabel.getBackgroundColor());
        assertEquals(BLACK, axisLabel.getBorderColor());
        assertEquals(1, axisLabel.getBorderWidth());
        assertEquals(1, axisLabel.getShadowBlur());
        assertEquals(BLACK, axisLabel.getShadowColor());
        assertEquals(0, axisLabel.getShadowOffsetX());
        assertEquals(0, axisLabel.getShadowOffsetY());
        assertEquals(100, axisLabel.getHeight());
        assertEquals(200, axisLabel.getWidth());
        assertEquals(BREAK, axisLabel.getOverflow());
        assertEquals(0, axisLabel.getTextShadowOffsetX());
        assertEquals(0, axisLabel.getTextShadowOffsetY());
        assertEquals(1, axisLabel.getTextShadowBlur());
        assertEquals(BLACK, axisLabel.getTextShadowColor());
        assertEquals(0, axisLabel.getTextBorderDashOffset());
        assertEquals("dashed", axisLabel.getTextBorderType());
        assertEquals(1, axisLabel.getTextBorderWidth());
        assertEquals(BLACK, axisLabel.getTextBorderColor());
        assertEquals(10, axisLabel.getLineHeight());
        assertEquals(12, axisLabel.getFontSize());
        assertEquals("sans-serif", axisLabel.getFontFamily());
        assertEquals("500", axisLabel.getFontWeight());
        assertEquals(ITALIC, axisLabel.getFontStyle());
        assertEquals(BLACK, axisLabel.getColor());
        assertEquals(5, axisLabel.getRotate());
        assertEquals("Line X", axisLabel.getFormatter());
        assertEquals("..", axisLabel.getEllipsis());
        assertTrue(axisLabel.getHideOverlap());
        assertEquals(0, axisLabel.getMargin());
        assertTrue(axisLabel.getShowMaxLabel());
        assertFalse(axisLabel.getShowMinLabel());

        SplitLine splitLine = xAxis.getSplitLine();
        assertEquals(0, splitLine.getInterval());
        assertFalse(splitLine.getShow());
        assertEquals(BLACK, splitLine.getLineStyle().getColor());

        MinorSplitLine minorSplitLine = xAxis.getMinorSplitLine();
        assertTrue(minorSplitLine.getShow());
        assertEquals(BLACK, minorSplitLine.getLineStyle().getColor());

        SplitArea splitArea = xAxis.getSplitArea();
        assertEquals(5, splitArea.getInterval());
        assertTrue(splitArea.getShow());
        AreaStyle areaStyle = splitArea.getAreaStyle();
        assertEquals(0, areaStyle.getShadowOffsetX());
        assertEquals(0, areaStyle.getShadowOffsetY());
        assertEquals(BLACK, areaStyle.getShadowColor());
        assertEquals(1, areaStyle.getShadowBlur());
        assertEquals(0.9, areaStyle.getOpacity());
        assertEquals(BLACK, areaStyle.getColors()[0]);

        AbstractAxis.AxisPointer axisPointer = xAxis.getAxisPointer();
        assertTrue(axisPointer.getShow());
        assertEquals(AbstractAxisPointer.IndicatorType.LINE, axisPointer.getType());
        assertEquals(0, axisPointer.getZ());
        assertTrue(axisPointer.getSnap());
        assertTrue(axisPointer.getStatus());
        assertTrue(axisPointer.getTriggerEmphasis());
        assertTrue(axisPointer.getTriggerTooltip());
        assertEquals(50, axisPointer.getValue());

        Label axisPointerLabel = axisPointer.getLabel();
        assertTrue(axisPointerLabel.getShow());
        assertEquals(5, axisPointerLabel.getPadding().getLeft());
        assertEquals(WHITE, axisPointerLabel.getBackgroundColor());
        assertEquals(BLACK, axisPointerLabel.getBorderColor());
        assertEquals(1, axisPointerLabel.getBorderWidth());
        assertEquals(1, axisPointerLabel.getShadowBlur());
        assertEquals(BLACK, axisPointerLabel.getShadowColor());
        assertEquals(0, axisPointerLabel.getShadowOffsetX());
        assertEquals(0, axisPointerLabel.getShadowOffsetY());
        assertEquals(10, axisPointerLabel.getHeight());
        assertEquals(20, axisPointerLabel.getWidth());
        assertEquals(BREAK, axisPointerLabel.getOverflow());
        assertEquals(0, axisPointerLabel.getTextShadowOffsetX());
        assertEquals(0, axisPointerLabel.getTextShadowOffsetY());
        assertEquals(1, axisPointerLabel.getTextShadowBlur());
        assertEquals(BLACK, axisPointerLabel.getTextShadowColor());
        assertEquals(0, axisPointerLabel.getTextBorderDashOffset());
        assertEquals("dashed", axisPointerLabel.getTextBorderType());
        assertEquals(1, axisPointerLabel.getTextBorderWidth());
        assertEquals(BLACK, axisPointerLabel.getTextBorderColor());
        assertEquals(10, axisPointerLabel.getLineHeight());
        assertEquals(12, axisPointerLabel.getFontSize());
        assertEquals("sans-serif", axisPointerLabel.getFontFamily());
        assertEquals("500", axisPointerLabel.getFontWeight());
        assertEquals(ITALIC, axisPointerLabel.getFontStyle());
        assertEquals(BLACK, axisPointerLabel.getColor());
        assertEquals("Pointer X", axisPointerLabel.getFormatter());
        assertEquals("..", axisPointerLabel.getEllipsis());
        assertEquals(0, axisPointerLabel.getMargin());
        assertEquals(1, axisPointerLabel.getPrecision());

        assertEquals(BLACK, axisPointer.getLineStyle().getColor());

        ShadowStyle shadowStyle = axisPointer.getShadowStyle();
        assertEquals(0.9, shadowStyle.getOpacity());
    }

    @Test
    @DisplayName("Load YAxis from XML")
    public void loadYAxisFromXmlTest() {
        Chart chart = navigateTo(ChartOptionYAxisTestView.class).yAxisChartId;
        YAxis yAxis = chart.getYAxes().get(0);

        assertEquals("yAxis", yAxis.getId());
        assertEquals(0, yAxis.getGridIndex());
        assertTrue(yAxis.getAlignTicks());
        assertEquals(AbstractCartesianAxis.Position.BOTTOM, yAxis.getPosition());
        assertEquals(0, yAxis.getOffset());
        assertEquals(AxisType.CATEGORY, yAxis.getType());
        assertEquals("yAxis", yAxis.getName());
        assertEquals(HasAxisName.NameLocation.CENTER, yAxis.getNameLocation());
        assertEquals(15, yAxis.getNameGap());
        assertEquals(5, yAxis.getNameRotate());
        assertFalse(yAxis.getInverse());
        assertEquals("5%", yAxis.getBoundaryGap().getNonCategoryGap()[0]);
        assertEquals("5", yAxis.getMin());
        assertEquals("100", yAxis.getMax());
        assertFalse(yAxis.getScale());
        assertEquals(10, yAxis.getSplitNumber());
        assertEquals(10, yAxis.getMinInterval());
        assertEquals(10, yAxis.getMaxInterval());
        assertEquals(10, yAxis.getInterval());
        assertTrue(yAxis.getAnimation());
        assertNull(yAxis.getBoundaryGap().getCategoryGap());
        assertEquals("100", yAxis.getMax());
        assertEquals(2000, yAxis.getAnimationThreshold());
        assertEquals(1000, yAxis.getAnimationDuration());
        assertEquals("cubicOut", yAxis.getAnimationEasing());
        assertEquals(0, yAxis.getAnimationDelay());
        assertEquals("100", yAxis.getMax());
        assertEquals("100", yAxis.getMax());
        assertEquals(300, yAxis.getAnimationDurationUpdate());
        assertEquals("cubicOut", yAxis.getAnimationEasingUpdate());
        assertEquals(0, yAxis.getAnimationDelayUpdate());
        assertEquals(0, yAxis.getZ());
        assertEquals(0, yAxis.getZLevel());

        HasAxisName.NameTextStyle nameTextStyle = yAxis.getNameTextStyle();
        assertEquals(0, nameTextStyle.getShadowOffsetX());
        assertEquals(0, nameTextStyle.getShadowOffsetY());
        assertEquals(BLACK, nameTextStyle.getShadowColor());
        assertEquals(1, nameTextStyle.getShadowBlur());
        assertEquals(1, nameTextStyle.getBorderWidth());
        assertEquals(BLACK, nameTextStyle.getBorderColor());
        assertEquals(WHITE, nameTextStyle.getBackgroundColor());
        assertEquals(0, nameTextStyle.getPadding().getLeft());
        assertEquals("solid", nameTextStyle.getBorderType());
        assertEquals(5, nameTextStyle.getBorderRadius());
        assertEquals(0, nameTextStyle.getBorderDashOffset());
        assertEquals(VerticalAlign.MIDDLE, nameTextStyle.getVerticalAlign());
        assertEquals(Align.CENTER, nameTextStyle.getAlign());
        assertEquals(Align.CENTER, nameTextStyle.getAlign());

        AxisLine axisLine = yAxis.getAxisLine();
        assertFalse(axisLine.getShow());
        assertTrue(axisLine.getOnZero());
        assertEquals(0, axisLine.getOnZeroAxisIndex());
        assertEquals("none", axisLine.getSymbols()[0]);
        assertEquals(0, axisLine.getSymbolsOffset()[0]);
        assertEquals(5, axisLine.getSymbolsSize()[0]);

        AxisTick axisTick = yAxis.getAxisTick();
        assertTrue(axisTick.getShow());
        assertEquals(5, axisTick.getInterval());
        assertTrue(axisTick.getAlignWithLabel());
        assertTrue(axisTick.getInside());
        assertEquals(10, axisTick.getLength());

        LineStyle tickLineStyle = axisTick.getLineStyle();
        assertEquals(BLACK, tickLineStyle.getColor());

        MinorTick minorTick = yAxis.getMinorTick();

        assertEquals(10, minorTick.getLength());
        assertTrue(minorTick.getShow());
        assertEquals(5, minorTick.getSplitNumber());

        LineStyle minorTickLineStyle = minorTick.getLineStyle();
        assertEquals(BLACK, minorTickLineStyle.getColor());

        AxisLabel axisLabel = yAxis.getAxisLabel();
        assertTrue(axisLabel.getShow());
        assertTrue(axisLabel.getInside());
        assertEquals(5, axisLabel.getInterval());
        assertEquals(Align.CENTER, axisLabel.getAlign());
        assertEquals(VerticalAlign.MIDDLE, axisLabel.getVerticalAlign());
        assertEquals(0, axisLabel.getBorderDashOffset());
        assertEquals(5, axisLabel.getBorderRadius());
        assertEquals("solid", axisLabel.getBorderType());
        assertEquals(5, axisLabel.getPadding().getLeft());
        assertEquals(WHITE, axisLabel.getBackgroundColor());
        assertEquals(BLACK, axisLabel.getBorderColor());
        assertEquals(1, axisLabel.getBorderWidth());
        assertEquals(1, axisLabel.getShadowBlur());
        assertEquals(BLACK, axisLabel.getShadowColor());
        assertEquals(0, axisLabel.getShadowOffsetX());
        assertEquals(0, axisLabel.getShadowOffsetY());
        assertEquals(100, axisLabel.getHeight());
        assertEquals(200, axisLabel.getWidth());
        assertEquals(BREAK, axisLabel.getOverflow());
        assertEquals(0, axisLabel.getTextShadowOffsetX());
        assertEquals(0, axisLabel.getTextShadowOffsetY());
        assertEquals(1, axisLabel.getTextShadowBlur());
        assertEquals(BLACK, axisLabel.getTextShadowColor());
        assertEquals(0, axisLabel.getTextBorderDashOffset());
        assertEquals("dashed", axisLabel.getTextBorderType());
        assertEquals(1, axisLabel.getTextBorderWidth());
        assertEquals(BLACK, axisLabel.getTextBorderColor());
        assertEquals(10, axisLabel.getLineHeight());
        assertEquals(12, axisLabel.getFontSize());
        assertEquals("sans-serif", axisLabel.getFontFamily());
        assertEquals("500", axisLabel.getFontWeight());
        assertEquals(ITALIC, axisLabel.getFontStyle());
        assertEquals(BLACK, axisLabel.getColor());
        assertEquals(5, axisLabel.getRotate());
        assertEquals("Line Y", axisLabel.getFormatter());
        assertEquals("..", axisLabel.getEllipsis());
        assertTrue(axisLabel.getHideOverlap());
        assertEquals(0, axisLabel.getMargin());
        assertTrue(axisLabel.getShowMaxLabel());
        assertFalse(axisLabel.getShowMinLabel());

        SplitLine splitLine = yAxis.getSplitLine();
        assertEquals(0, splitLine.getInterval());
        assertFalse(splitLine.getShow());
        assertEquals(BLACK, splitLine.getLineStyle().getColor());

        MinorSplitLine minorSplitLine = yAxis.getMinorSplitLine();
        assertTrue(minorSplitLine.getShow());
        assertEquals(BLACK, minorSplitLine.getLineStyle().getColor());

        SplitArea splitArea = yAxis.getSplitArea();
        assertEquals(5, splitArea.getInterval());
        assertTrue(splitArea.getShow());

        AreaStyle areaStyle = splitArea.getAreaStyle();
        assertEquals(0, areaStyle.getShadowOffsetX());
        assertEquals(0, areaStyle.getShadowOffsetY());
        assertEquals(BLACK, areaStyle.getShadowColor());
        assertEquals(1, areaStyle.getShadowBlur());
        assertEquals(0.9, areaStyle.getOpacity());
        assertEquals(BLACK, areaStyle.getColors()[0]);

        AbstractAxis.AxisPointer axisPointer = yAxis.getAxisPointer();
        assertTrue(axisPointer.getShow());
        assertEquals(AbstractAxisPointer.IndicatorType.LINE, axisPointer.getType());
        assertEquals(0, axisPointer.getZ());
        assertTrue(axisPointer.getSnap());
        assertTrue(axisPointer.getStatus());
        assertTrue(axisPointer.getTriggerEmphasis());
        assertTrue(axisPointer.getTriggerTooltip());
        assertEquals(50, axisPointer.getValue());

        Label axisPointerLabel = axisPointer.getLabel();
        assertTrue(axisPointerLabel.getShow());
        assertEquals(5, axisPointerLabel.getPadding().getLeft());
        assertEquals(WHITE, axisPointerLabel.getBackgroundColor());
        assertEquals(BLACK, axisPointerLabel.getBorderColor());
        assertEquals(1, axisPointerLabel.getBorderWidth());
        assertEquals(1, axisPointerLabel.getShadowBlur());
        assertEquals(BLACK, axisPointerLabel.getShadowColor());
        assertEquals(0, axisPointerLabel.getShadowOffsetX());
        assertEquals(0, axisPointerLabel.getShadowOffsetY());
        assertEquals(10, axisPointerLabel.getHeight());
        assertEquals(20, axisPointerLabel.getWidth());
        assertEquals(BREAK, axisPointerLabel.getOverflow());
        assertEquals(0, axisPointerLabel.getTextShadowOffsetX());
        assertEquals(0, axisPointerLabel.getTextShadowOffsetY());
        assertEquals(1, axisPointerLabel.getTextShadowBlur());
        assertEquals(BLACK, axisPointerLabel.getTextShadowColor());
        assertEquals(0, axisPointerLabel.getTextBorderDashOffset());
        assertEquals("dashed", axisPointerLabel.getTextBorderType());
        assertEquals(1, axisPointerLabel.getTextBorderWidth());
        assertEquals(BLACK, axisPointerLabel.getTextBorderColor());
        assertEquals(10, axisPointerLabel.getLineHeight());
        assertEquals(12, axisPointerLabel.getFontSize());
        assertEquals("sans-serif", axisPointerLabel.getFontFamily());
        assertEquals("500", axisPointerLabel.getFontWeight());
        assertEquals(ITALIC, axisPointerLabel.getFontStyle());
        assertEquals(BLACK, axisPointerLabel.getColor());
        assertEquals("Pointer X", axisPointerLabel.getFormatter());
        assertEquals("..", axisPointerLabel.getEllipsis());
        assertEquals(0, axisPointerLabel.getMargin());
        assertEquals(1, axisPointerLabel.getPrecision());

        assertEquals(BLACK, axisPointer.getLineStyle().getColor());

        ShadowStyle shadowStyle = axisPointer.getShadowStyle();
        assertEquals(0.9, shadowStyle.getOpacity());
    }

    @Test
    @DisplayName("Load RadiusAxis options from XML")
    public void loadRadiusAxisOptionsFromXmlTest() {
        Chart chart = navigateTo(ChartOptionRadiusAxisTestView.class).radiusAxisChartId;

        RadiusAxis radiusAxis = chart.getRadiusAxis();
        assertEquals("radiusAxis", radiusAxis.getId());
        assertEquals("radiusAxis", radiusAxis.getName());
        assertFalse(radiusAxis.getInverse());
        assertEquals(0, radiusAxis.getNameRotate());
        assertEquals(HasAxisName.NameLocation.CENTER, radiusAxis.getNameLocation());
        assertEquals(15, radiusAxis.getNameGap());
        assertEquals(0, radiusAxis.getZ());
        assertEquals(0, radiusAxis.getZLevel());
        assertFalse(radiusAxis.getScale());
        assertTrue(radiusAxis.getSilent());
        assertTrue(radiusAxis.getTriggerEvent());
        assertEquals(5, radiusAxis.getSplitNumber());
        assertEquals("100", radiusAxis.getMax());
        assertNull(radiusAxis.getBoundaryGap().getCategoryGap());
        assertEquals("0", radiusAxis.getBoundaryGap().getNonCategoryGap()[0]);
        assertEquals(1000, radiusAxis.getAnimationThreshold());
        assertEquals("cubicOut", radiusAxis.getAnimationEasingUpdate());
        assertEquals("cubicOut", radiusAxis.getAnimationEasing());
        assertEquals(300, radiusAxis.getAnimationDurationUpdate());
        assertEquals(1000, radiusAxis.getAnimationDuration());
        assertEquals(0, radiusAxis.getAnimationDelayUpdate());
        assertEquals(0, radiusAxis.getAnimationDelay());
        assertTrue(radiusAxis.getAnimation());
        assertEquals(AxisType.VALUE, radiusAxis.getType());
        assertEquals(0, radiusAxis.getInterval());
        assertEquals(10, radiusAxis.getLogBase());
        assertEquals(10, radiusAxis.getMaxInterval());
        assertEquals(1, radiusAxis.getMinInterval());
        assertEquals("0", radiusAxis.getMin());
        assertEquals(0, radiusAxis.getPolarIndex());

        HasAxisName.NameTextStyle nameTextStyle = radiusAxis.getNameTextStyle();
        assertEquals(0, nameTextStyle.getShadowOffsetX());

        SplitArea splitArea = radiusAxis.getSplitArea();
        assertTrue(splitArea.getShow());

        AreaStyle areaStyle = splitArea.getAreaStyle();
        assertEquals(0, areaStyle.getShadowOffsetX());

        SplitLine splitLine = radiusAxis.getSplitLine();
        assertFalse(splitLine.getShow());

        LineStyle lineStyle = splitLine.getLineStyle();
        assertEquals(BLACK, lineStyle.getColor());

        AxisLine axisLine = radiusAxis.getAxisLine();
        assertFalse(axisLine.getShow());

        AxisTick axisTick = radiusAxis.getAxisTick();
        assertTrue(axisTick.getShow());

        LineStyle tickLineStyle = splitLine.getLineStyle();
        assertEquals(BLACK, tickLineStyle.getColor());

        AxisLabel axisLabel = radiusAxis.getAxisLabel();
        assertEquals(BLACK, axisLabel.getBorderColor());
    }

    @Test
    @DisplayName("Load AngleAxis options from XML")
    public void loadAngleAxisOptionsFromXmlTest() {
        Chart chart = navigateTo(ChartOptionAngleAxisTestView.class).angleAxisChartId;

        AngleAxis angleAxis = chart.getAngleAxis();
        assertEquals("angleAxis", angleAxis.getId());
        assertFalse(angleAxis.getClockwise());
        assertEquals(0, angleAxis.getStartAngle());
        assertEquals(0, angleAxis.getZ());
        assertEquals(0, angleAxis.getZLevel());
        assertFalse(angleAxis.getScale());
        assertTrue(angleAxis.getSilent());
        assertTrue(angleAxis.getTriggerEvent());
        assertEquals(5, angleAxis.getSplitNumber());
        assertEquals("100", angleAxis.getMax());
        assertEquals("0", angleAxis.getBoundaryGap().getNonCategoryGap()[0]);
        assertEquals(1000, angleAxis.getAnimationThreshold());
        assertEquals("cubicOut", angleAxis.getAnimationEasingUpdate());
        assertEquals("cubicOut", angleAxis.getAnimationEasing());
        assertEquals(300, angleAxis.getAnimationDurationUpdate());
        assertEquals(1000, angleAxis.getAnimationDuration());
        assertEquals(0, angleAxis.getAnimationDelayUpdate());
        assertEquals(0, angleAxis.getAnimationDelay());
        assertTrue(angleAxis.getAnimation());
        assertEquals(AxisType.VALUE, angleAxis.getType());
        assertEquals(0, angleAxis.getInterval());
        assertEquals(10, angleAxis.getLogBase());
        assertEquals(10, angleAxis.getMaxInterval());
        assertEquals(1, angleAxis.getMinInterval());
        assertEquals("0", angleAxis.getMin());
        assertEquals(0, angleAxis.getPolarIndex());

        SplitArea splitArea = angleAxis.getSplitArea();
        assertTrue(splitArea.getShow());

        AreaStyle areaStyle = splitArea.getAreaStyle();
        assertEquals(0, areaStyle.getShadowOffsetX());

        SplitLine splitLine = angleAxis.getSplitLine();
        assertFalse(splitLine.getShow());

        LineStyle lineStyle = splitLine.getLineStyle();
        assertEquals(BLACK, lineStyle.getColor());
        assertEquals("dashed", lineStyle.getType());

        AxisLine axisLine = angleAxis.getAxisLine();
        assertFalse(axisLine.getShow());

        AxisTick axisTick = angleAxis.getAxisTick();
        assertTrue(axisTick.getShow());
        assertEquals(0, axisTick.getLineStyle().getDashOffset());

        LineStyle tickLineStyle = splitLine.getLineStyle();
        assertEquals(BLACK, tickLineStyle.getColor());

        AxisLabel axisLabel = angleAxis.getAxisLabel();
        assertEquals(BLACK, axisLabel.getBorderColor());
    }
}
