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

import com.google.common.collect.Maps;
import component_xml_load.view.series.*;
import io.jmix.chartsflowui.component.Chart;
import io.jmix.chartsflowui.kit.component.model.HasLineStyle;
import io.jmix.chartsflowui.kit.component.model.series.Label;
import io.jmix.chartsflowui.kit.component.model.series.*;
import io.jmix.chartsflowui.kit.component.model.series.mark.MarkArea;
import io.jmix.chartsflowui.kit.component.model.series.mark.MarkLine;
import io.jmix.chartsflowui.kit.component.model.series.mark.MarkPoint;
import io.jmix.chartsflowui.kit.component.model.series.mark.PointDataType;
import io.jmix.chartsflowui.kit.component.model.shared.*;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import test_support.AbstractXmlLoadTest;
import test_support.ChartsFlowuiTestConfiguration;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static io.jmix.chartsflowui.kit.component.model.HasLineStyle.Cap.ROUND;
import static io.jmix.chartsflowui.kit.component.model.shared.Color.*;
import static org.junit.jupiter.api.Assertions.*;


@UiTest(viewBasePackages = {"component_xml_load.view.series"})
@SpringBootTest(classes = {ChartsFlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class})
public class ChartsSeriesXmlLoadTest extends AbstractXmlLoadTest {

    @Test
    @DisplayName("Load Line Series chart from XML")
    public void loadLineSeriesFromXmlTest() {
        Chart chart = navigateTo(LineSeriesChartTestView.class).lineSeriesChartId;
        LineSeries lineSeries = chart.getSeries("line");

        assertEquals("line", lineSeries.getId());
        assertEquals(HasSymbols.SymbolType.CIRCLE, lineSeries.getSymbol().getType());
        assertEquals("line", lineSeries.getName());
        assertEquals(ColorBy.SERIES, lineSeries.getColorBy());
        assertEquals(CoordinateSystem.CARTESIAN_2_D, lineSeries.getCoordinateSystem());
        assertEquals(0, lineSeries.getXAxisIndex());
        assertEquals(0, lineSeries.getYAxisIndex());
        assertEquals(0, lineSeries.getPolarIndex());
        assertEquals(4, lineSeries.getSymbolSize());
        assertEquals(5, lineSeries.getSymbolRotate());
        assertFalse(lineSeries.getSymbolKeepAspect());
        assertEquals("0", lineSeries.getSymbolOffset()[0]);
        assertTrue(lineSeries.getShowSymbol());
        assertTrue(lineSeries.getShowAllSymbol());
        assertTrue(lineSeries.getLegendHoverLink());
        assertEquals("some", lineSeries.getStack());
        assertEquals(HasStack.StackStrategy.SAME_SIGN, lineSeries.getStackStrategy());
        assertEquals("pointer", lineSeries.getCursor());
        assertFalse(lineSeries.getConnectNulls());
        assertTrue(lineSeries.getClip());
        assertFalse(lineSeries.getTriggerLineEvent());
        assertEquals(LineSeries.Step.START, lineSeries.getStep());
        assertEquals(SelectedMode.SINGLE, lineSeries.getSelectedMode());
        assertEquals(0.0, lineSeries.getSmooth());
        assertEquals(LineSeries.SmoothMonotoneType.X, lineSeries.getSmoothMonotone());
        assertEquals(SamplingType.AVERAGE, lineSeries.getSampling());
        assertEquals(AbstractAxisAwareSeries.SeriesLayoutType.COLUMN, lineSeries.getSeriesLayoutBy());
        assertEquals(0, lineSeries.getDatasetIndex());
        assertEquals("dataGroupId", lineSeries.getDataGroupId());
        assertEquals(0, lineSeries.getZLevel());
        assertEquals(2, lineSeries.getZ());
        assertTrue(lineSeries.getSilent());
        assertTrue(lineSeries.getAnimation());
        assertEquals(2000, lineSeries.getAnimationThreshold());
        assertEquals(1000, lineSeries.getAnimationDuration());
        assertEquals("linear", lineSeries.getAnimationEasing());
        assertEquals(0, lineSeries.getAnimationDelay());
        assertEquals(300, lineSeries.getAnimationDurationUpdate());
        assertEquals("cubicOut", lineSeries.getAnimationEasingUpdate());
        assertEquals(0, lineSeries.getAnimationDelayUpdate());

        Label label = lineSeries.getLabel();
        assertEquals(200, label.getWidth());

        LineSeries.EndLabel endlabel = lineSeries.getEndLabel();
        assertTrue(endlabel.getValueAnimation());

        LineSeries.LabelLine labelLine = lineSeries.getLabelLine();
        assertTrue(labelLine.getShowAbove());

        LineStyle labelLineStyle = labelLine.getLineStyle();
        assertEquals(Color.BLACK, labelLineStyle.getColor());

        AbstractSeries.LabelLayout labelLayout = lineSeries.getLabelLayout();
        assertTrue(labelLayout.getHideOverlap());
        assertEquals(AbstractSeries.LabelLayout.MoveOverlapPosition.SHIFT_X, labelLayout.getMoveOverlap());
        assertEquals("0", labelLayout.getX());
        assertEquals("0", labelLayout.getY());
        assertEquals(0, labelLayout.getDx());
        assertEquals(0, labelLayout.getDy());
        assertEquals(0, labelLayout.getRotate());
        assertEquals(200, labelLayout.getWidth());
        assertEquals(100, labelLayout.getHeight());
        assertEquals(Align.CENTER, labelLayout.getAlign());
        assertEquals(VerticalAlign.MIDDLE, labelLayout.getVerticalAlign());
        assertEquals(12, labelLayout.getFontSize());
        assertTrue(labelLayout.getDraggable());
        assertArrayEquals(new Integer[][]{{1, 2}, {3, 4}, {5, 6}}, labelLayout.getLabelLinePoints());

        LineSeries.ItemStyle itemStyle = lineSeries.getItemStyle();
        assertEquals(0.9, itemStyle.getOpacity());

        LineSeries.AreaStyle areaStyle = lineSeries.getAreaStyle();
        assertEquals(0.6, areaStyle.getOpacity());

        LineSeries.Emphasis emphasis = lineSeries.getEmphasis();
        assertEquals(0.7, emphasis.getScale());

        Label emphasisLabel = emphasis.getLabel();
        assertEquals(5, emphasisLabel.getDistance());

        ElementLabelLine emphasisLabelLine = emphasis.getLabelLine();
        assertTrue(emphasisLabelLine.getShow());

        LineStyle emphasisLineStyle = emphasis.getLineStyle();
        assertEquals(0, emphasisLineStyle.getWidth());

        ItemStyle emphasisItemStyle = emphasis.getItemStyle();
        assertEquals(ROUND, emphasisItemStyle.getCap());

        LineSeries.AbstractLineElement.AreaStyle emphasisAreaStyle = emphasis.getAreaStyle();
        assertEquals(1, emphasisAreaStyle.getShadowBlur());

        LineSeries.Select select = lineSeries.getSelect();
        assertFalse(emphasis.getDisabled());

        Label selectLabel = select.getLabel();
        assertEquals(5, selectLabel.getDistance());

        ElementLabelLine selectLabelLine = select.getLabelLine();
        assertTrue(selectLabelLine.getShow());

        LineStyle selectLineStyle = select.getLineStyle();
        assertEquals(0, selectLineStyle.getWidth());

        ItemStyle selectItemStyle = select.getItemStyle();
        assertEquals(ROUND, selectItemStyle.getCap());

        LineSeries.AbstractLineElement.AreaStyle selectAreaStyle = select.getAreaStyle();
        assertEquals(1, selectAreaStyle.getShadowBlur());

        LineSeries.EndLabel endLabel = select.getEndLabel();
        assertTrue(endLabel.getValueAnimation());

        MarkPoint markPoint = lineSeries.getMarkPoint();
        assertEquals(HasSymbols.SymbolType.PIN, markPoint.getSymbol().getType());
        assertEquals(50, markPoint.getSymbolSize());
        assertEquals(0, markPoint.getSymbolRotate());
        assertFalse(markPoint.getSymbolKeepAspect());
        assertEquals("5", markPoint.getSymbolOffset()[1]);
        assertFalse(markPoint.getSilent());
        assertTrue(markPoint.getAnimation());
        assertEquals(2000, markPoint.getAnimationThreshold());
        assertEquals(1000, markPoint.getAnimationDuration());
        assertEquals("linear", markPoint.getAnimationEasing());
        assertEquals(0, markPoint.getAnimationDelay());
        assertEquals(300, markPoint.getAnimationDurationUpdate());
        assertEquals("cubicOut", markPoint.getAnimationEasingUpdate());
        assertEquals(0, markPoint.getAnimationDelayUpdate());

        Label markPointLabel = markPoint.getLabel();
        assertEquals(5, markPointLabel.getOffset()[1]);

        ItemStyle markPointItemStyle = markPoint.getItemStyle();
        assertEquals(ROUND, markPointItemStyle.getCap());

        MarkPoint.Emphasis markPointEmphasis = markPoint.getEmphasis();
        assertFalse(markPointEmphasis.getDisabled());

        Label markPointEmphasisLabel = markPointEmphasis.getLabel();
        assertEquals(FontStyle.NORMAL, markPointEmphasisLabel.getFontStyle());

        ItemStyle markPointEmphasisItemStyle = markPointEmphasis.getItemStyle();
        assertEquals(BLACK, markPointEmphasisItemStyle.getBorderColor());

        MarkLine markLine = lineSeries.getMarkLine();
        assertFalse(markLine.getSilent());
        assertEquals(10, markLine.getSymbolSize()[0]);
        assertEquals(2, markLine.getPrecision());

        Label markLineLabel = markLine.getLabel();
        assertEquals(Label.Position.PositionType.TOP, markLineLabel.getPosition().getPositionType());
        assertEquals(5, markLineLabel.getDistance());

        LineStyle markLineLineStyle = markLine.getLineStyle();
        assertEquals(HasLineStyle.Join.BEVEL, markLineLineStyle.getJoin());

        MarkLine.Emphasis markLineEmphasis = markLine.getEmphasis();
        assertFalse(markLineEmphasis.getDisabled());

        Label markLineEmphasisLabel = markLineEmphasis.getLabel();
        assertEquals(0, markLineEmphasisLabel.getRotate());

        LineStyle markLineEmphasisLineStyle = markLineEmphasis.getLineStyle();
        assertEquals(0, markLineEmphasisLineStyle.getDashOffset());

        MarkArea markArea = lineSeries.getMarkArea();
        assertFalse(markArea.getSilent());

        List<MarkArea.PointPair> pointPaiList = markArea.getData();
        MarkArea.PointPair firstPair = pointPaiList.get(0);
        MarkArea.Point leftTop = firstPair.getLeftTopPoint();

        assertEquals(PointDataType.AVERAGE, leftTop.getType());
        assertEquals(0, leftTop.getValueIndex());
        assertEquals("5", leftTop.getValueDim());
        assertEquals("0", leftTop.getX());
        assertEquals("0", leftTop.getY());
        assertEquals(10, leftTop.getValue());

        Label markAreaLabel = markArea.getLabel();
        assertEquals(5, markAreaLabel.getDistance());

        ItemStyle markAreaItemStyle = markArea.getItemStyle();
        assertEquals(5, markAreaItemStyle.getBorderDashOffset());

        MarkArea.Emphasis markAreaEmphasis = markArea.getEmphasis();
        assertFalse(markAreaEmphasis.getDisabled());

        Label markAreaEmphasisLabel = markAreaEmphasis.getLabel();
        assertEquals(5, markAreaEmphasisLabel.getDistance());

        ItemStyle markAreaEmphasisItemStyle = markAreaEmphasis.getItemStyle();
        assertEquals(10, markAreaEmphasisItemStyle.getMiterLimit());

        MarkArea.Blur markAreaBlur = markArea.getBlur();
        Label markAreaBlurLabel = markAreaBlur.getLabel();
        assertEquals(5, markAreaBlurLabel.getDistance());

        ItemStyle markAreaBlurItemStyle = markAreaBlur.getItemStyle();
        assertEquals(10, markAreaBlurItemStyle.getMiterLimit());

        AbstractSeries.Tooltip tooltip = lineSeries.getTooltip();
        assertEquals("Line Chart Tooltip", tooltip.getValueFormatter());

        TextStyle tooltipTextStyle = tooltip.getTextStyle();
        assertEquals(BLACK, tooltipTextStyle.getColor());
    }

    @Test
    @DisplayName("Load Bar Series chart from XML")
    public void loadBarSeriesFromXmlTest() {
        Chart chart = navigateTo(BarSeriesChartTestView.class).barSeriesChartId;
        BarSeries barSeries = chart.getSeries("bar");

        assertTrue(barSeries.getRoundCap());
        assertFalse(barSeries.getRealtimeSort());
        assertTrue(barSeries.getShowBackground());
        assertEquals("20%", barSeries.getBarCategoryGap());
        assertEquals("30%", barSeries.getBarGap());
        assertEquals("200", barSeries.getBarWidth());
        assertEquals("300", barSeries.getBarMaxWidth());
        assertEquals("50", barSeries.getBarMinWidth());
        assertEquals(100, barSeries.getBarMinHeight());
        assertEquals(0, barSeries.getBarMinAngle());
        assertTrue(barSeries.getLarge());
        assertEquals(500, barSeries.getLargeThreshold());
        assertEquals(5000, barSeries.getProgressive());
        assertEquals(3000, barSeries.getProgressiveThreshold());

        Label label = barSeries.getLabel();
        assertEquals(200, label.getWidth());

        BarSeries.LabelLine labelLine = barSeries.getLabelLine();
        assertTrue(labelLine.getShow());

        LineStyle labelLineStyle = labelLine.getLineStyle();
        assertEquals(BLACK, labelLineStyle.getColor());

        ItemStyleWithDecal itemStyle = barSeries.getItemStyle();
        assertEquals(0.9, itemStyle.getOpacity());

        Decal decal = itemStyle.getDecal();
        assertEquals(HasSymbols.SymbolType.RECTANGLE, decal.getSymbol().getType());

        AbstractSeries.LabelLayout labelLayout = barSeries.getLabelLayout();
        assertEquals(Align.CENTER, labelLayout.getAlign());

        BarSeries.Emphasis emphasis = barSeries.getEmphasis();
        assertEquals(FocusType.NONE, emphasis.getFocus());
        assertEquals(BlurScopeType.COORDINATE_SYSTEM, emphasis.getBlurScope());

        Label emphasisLabel = emphasis.getLabel();
        assertEquals(5, emphasisLabel.getDistance());

        BarSeries.LabelLine emphasisLabelLine = emphasis.getLabelLine();
        assertTrue(emphasisLabelLine.getShow());

        BarSeries.ItemStyle emphasisItemStyle = emphasis.getItemStyle();
        assertEquals(WHITE, emphasisItemStyle.getColor());

        BarSeries.Blur blur = barSeries.getBlur();
        Label blurLabel = blur.getLabel();
        assertEquals("Line", blurLabel.getFormatter());

        BarSeries.Select select = barSeries.getSelect();
        assertFalse(emphasis.getDisabled());

        Label selectLabel = select.getLabel();
        assertEquals(5, selectLabel.getDistance());

        BarSeries.LabelLine selectLabelLine = select.getLabelLine();
        assertTrue(selectLabelLine.getShow());

        BarSeries.ItemStyle selectItemStyle = select.getItemStyle();
        assertEquals(WHITE, selectItemStyle.getColor());

        MarkPoint markPoint = barSeries.getMarkPoint();
        assertEquals(50, markPoint.getSymbolSize());

        Label markPointLabel = markPoint.getLabel();
        assertEquals(5, markPointLabel.getOffset()[1]);

        ItemStyle markPointItemStyle = markPoint.getItemStyle();
        assertEquals(ROUND, markPointItemStyle.getCap());

        MarkPoint.Emphasis markPointEmphasis = markPoint.getEmphasis();
        assertFalse(markPointEmphasis.getDisabled());

        Label markPointEmphasisLabel = markPointEmphasis.getLabel();
        assertEquals(FontStyle.NORMAL, markPointEmphasisLabel.getFontStyle());

        ItemStyle markPointEmphasisItemStyle = markPointEmphasis.getItemStyle();
        assertEquals(BLACK, markPointEmphasisItemStyle.getBorderColor());

        MarkLine markLine = barSeries.getMarkLine();
        assertFalse(markLine.getSilent());
        assertEquals(10, markLine.getSymbolSize()[0]);
        assertEquals(2, markLine.getPrecision());

        Label markLineLabel = markLine.getLabel();
        assertLinesMatch(Stream.of("5", "15"), Arrays.stream(markLineLabel.getPosition().getCoordinates()));
        assertEquals(5, markLineLabel.getDistance());

        LineStyle markLineLineStyle = markLine.getLineStyle();
        assertEquals(HasLineStyle.Join.BEVEL, markLineLineStyle.getJoin());

        MarkLine.Emphasis markLineEmphasis = markLine.getEmphasis();
        assertFalse(markLineEmphasis.getDisabled());

        Label markLineEmphasisLabel = markLineEmphasis.getLabel();
        assertEquals(0, markLineEmphasisLabel.getRotate());

        LineStyle markLineEmphasisLineStyle = markLineEmphasis.getLineStyle();
        assertEquals(0, markLineEmphasisLineStyle.getDashOffset());

        MarkArea markArea = barSeries.getMarkArea();
        assertFalse(markArea.getSilent());

        Label markAreaLabel = markArea.getLabel();
        assertEquals(5, markAreaLabel.getDistance());

        ItemStyle markAreaItemStyle = markArea.getItemStyle();
        assertEquals(5, markAreaItemStyle.getBorderDashOffset());

        MarkArea.Emphasis markAreaEmphasis = markArea.getEmphasis();
        assertFalse(markAreaEmphasis.getDisabled());

        Label markAreaEmphasisLabel = markAreaEmphasis.getLabel();
        assertEquals(5, markAreaEmphasisLabel.getDistance());

        ItemStyle markAreaEmphasisItemStyle = markAreaEmphasis.getItemStyle();
        assertEquals(10, markAreaEmphasisItemStyle.getMiterLimit());

        MarkArea.Blur markAreaBlur = markArea.getBlur();
        Label markAreaBlurLabel = markAreaBlur.getLabel();
        assertEquals(5, markAreaBlurLabel.getDistance());

        ItemStyle markAreaBlurItemStyle = markAreaBlur.getItemStyle();
        assertEquals(10, markAreaBlurItemStyle.getMiterLimit());

        AbstractSeries.Tooltip tooltip = barSeries.getTooltip();
        assertEquals("Bar Chart Tooltip", tooltip.getValueFormatter());

        TextStyle tooltipTextStyle = tooltip.getTextStyle();
        assertEquals(BLACK, tooltipTextStyle.getColor());
    }

    @Test
    @DisplayName("Load Pie Series chart from XML")
    public void loadPieSeriesFromXmlTest() {
        Chart chart = navigateTo(PieSeriesChartTestView.class).pieSeriesChartId;
        PieSeries pieSeries = chart.getSeries("pie");

        assertEquals(ColorBy.DATA, pieSeries.getColorBy());
        assertEquals(0, pieSeries.getGeoIndex());
        assertEquals(0, pieSeries.getCalendarIndex());
        assertEquals(0, pieSeries.getSelectedOffset());
        assertTrue(pieSeries.getClockwise());
        assertEquals(5, pieSeries.getStartAngle());
        assertEquals(5, pieSeries.getMinAngle());
        assertEquals(0, pieSeries.getMinShowLabelAngle());
        assertEquals(PieSeries.RoseType.AREA, pieSeries.getRoseType());
        assertTrue(pieSeries.getAvoidLabelOverlap());
        assertTrue(pieSeries.getStillShowZeroSum());
        assertEquals(2, pieSeries.getPercentPrecision());
        assertEquals("0", pieSeries.getLeft());
        assertEquals("0", pieSeries.getTop());
        assertEquals("0", pieSeries.getRight());
        assertEquals("0", pieSeries.getBottom());
        assertEquals("100", pieSeries.getWidth());
        assertEquals("100", pieSeries.getHeight());
        assertTrue(pieSeries.getShowEmptyCircle());
        assertEquals("50%", pieSeries.getCenter()[0]);
        assertEquals("0", pieSeries.getRadius()[0]);
        assertEquals(AbstractAxisAwareSeries.SeriesLayoutType.COLUMN, pieSeries.getSeriesLayoutBy());
        assertEquals(PieSeries.AnimationType.EXPANSION, pieSeries.getAnimationType());
        assertEquals(PieSeries.AnimationUpdateType.EXPANSION, pieSeries.getAnimationTypeUpdate());

        Label label = pieSeries.getLabel();
        assertEquals(200, label.getWidth());

        PieSeries.LabelLine labelLine = pieSeries.getLabelLine();
        assertTrue(labelLine.getShow());
        assertEquals(100, labelLine.getLength());
        assertFalse(labelLine.getSmooth());
        assertEquals(90, labelLine.getMinTurnAngle());
        assertEquals(0, labelLine.getMaxSurfaceAngle());

        LineStyle labelLineStyle = labelLine.getLineStyle();
        assertEquals(BLACK, labelLineStyle.getColor());

        AbstractSeries.LabelLayout labelLayout = pieSeries.getLabelLayout();
        assertEquals(Align.CENTER, labelLayout.getAlign());

        PieSeries.Emphasis emphasis = pieSeries.getEmphasis();
        assertEquals(FocusType.NONE, emphasis.getFocus());
        assertEquals(BlurScopeType.COORDINATE_SYSTEM, emphasis.getBlurScope());
        assertTrue(emphasis.getScale());
        assertEquals(10, emphasis.getScaleSize());

        Label emphasisLabel = emphasis.getLabel();
        assertEquals(5, emphasisLabel.getDistance());

        ElementLabelLine emphasisLabelLine = emphasis.getLabelLine();
        assertTrue(emphasisLabelLine.getShow());

        ItemStyle emphasisItemStyle = emphasis.getItemStyle();
        assertEquals(WHITE, emphasisItemStyle.getColor());

        Label blurLabel = pieSeries.getBlur().getLabel();
        assertEquals("Line", blurLabel.getFormatter());

        MarkPoint markPoint = pieSeries.getMarkPoint();
        assertEquals(50, markPoint.getSymbolSize());

        Label markPointLabel = markPoint.getLabel();
        assertEquals(5, markPointLabel.getOffset()[1]);

        ItemStyle markPointItemStyle = markPoint.getItemStyle();
        assertEquals(ROUND, markPointItemStyle.getCap());

        MarkPoint.Emphasis markPointEmphasis = markPoint.getEmphasis();
        assertFalse(markPointEmphasis.getDisabled());

        Label markPointEmphasisLabel = markPointEmphasis.getLabel();
        assertEquals(FontStyle.NORMAL, markPointEmphasisLabel.getFontStyle());

        ItemStyle markPointEmphasisItemStyle = markPointEmphasis.getItemStyle();
        assertEquals(BLACK, markPointEmphasisItemStyle.getBorderColor());

        MarkLine markLine = pieSeries.getMarkLine();
        assertFalse(markLine.getSilent());
        assertEquals(10, markLine.getSymbolSize()[0]);
        assertEquals(2, markLine.getPrecision());

        Label markLineLabel = markLine.getLabel();
        assertEquals(Label.Position.PositionType.TOP, markLineLabel.getPosition().getPositionType());
        assertEquals(5, markLineLabel.getDistance());

        LineStyle markLineLineStyle = markLine.getLineStyle();
        assertEquals(HasLineStyle.Join.BEVEL, markLineLineStyle.getJoin());

        MarkLine.Emphasis markLineEmphasis = markLine.getEmphasis();
        assertFalse(markLineEmphasis.getDisabled());

        Label markLineEmphasisLabel = markLineEmphasis.getLabel();
        assertEquals(0, markLineEmphasisLabel.getRotate());

        LineStyle markLineEmphasisLineStyle = markLineEmphasis.getLineStyle();
        assertEquals(0, markLineEmphasisLineStyle.getDashOffset());

        MarkArea markArea = pieSeries.getMarkArea();
        assertFalse(markArea.getSilent());

        Label markAreaLabel = markArea.getLabel();
        assertEquals(5, markAreaLabel.getDistance());

        ItemStyle markAreaItemStyle = markArea.getItemStyle();
        assertEquals(5, markAreaItemStyle.getBorderDashOffset());

        MarkArea.Emphasis markAreaEmphasis = markArea.getEmphasis();
        assertFalse(markAreaEmphasis.getDisabled());

        Label markAreaEmphasisLabel = markAreaEmphasis.getLabel();
        assertEquals(5, markAreaEmphasisLabel.getDistance());

        ItemStyle markAreaEmphasisItemStyle = markAreaEmphasis.getItemStyle();
        assertEquals(10, markAreaEmphasisItemStyle.getMiterLimit());

        MarkArea.Blur markAreaBlur = markArea.getBlur();
        Label markAreaBlurLabel = markAreaBlur.getLabel();
        assertEquals(5, markAreaBlurLabel.getDistance());

        ItemStyle markAreaBlurItemStyle = markAreaBlur.getItemStyle();
        assertEquals(10, markAreaBlurItemStyle.getMiterLimit());

        AbstractSeries.Tooltip tooltip = pieSeries.getTooltip();
        assertEquals("Pie Chart Tooltip", tooltip.getValueFormatter());

        TextStyle tooltipTextStyle = tooltip.getTextStyle();
        assertEquals(BLACK, tooltipTextStyle.getColor());
    }

    @Test
    @DisplayName("Load Scatter Series chart from XML")
    public void loadScatterSeriesFromXmlTest() {
        Chart chart = navigateTo(ScatterSeriesChartTestView.class).scatterSeriesChartId;
        ScatterSeries scatterSeries = chart.getSeries("scatter");

        assertEquals(ColorBy.DATA, scatterSeries.getColorBy());
        assertEquals(CoordinateSystem.CARTESIAN_2_D, scatterSeries.getCoordinateSystem());
        assertEquals(0, scatterSeries.getXAxisIndex());
        assertEquals(0, scatterSeries.getYAxisIndex());
        assertEquals(0, scatterSeries.getPolarIndex());
        assertEquals(HasSymbols.SymbolType.CIRCLE, scatterSeries.getSymbol().getType());
        assertFalse(scatterSeries.getSymbolKeepAspect());
        assertFalse(scatterSeries.getLarge());
        assertEquals(2000, scatterSeries.getLargeThreshold());
        assertEquals(400, scatterSeries.getProgressive());
        assertEquals(3000, scatterSeries.getProgressiveThreshold());
        assertTrue(scatterSeries.getClip());

        Label label = scatterSeries.getLabel();
        assertEquals(200, label.getWidth());

        ScatterSeries.LabelLine labelLine = scatterSeries.getLabelLine();
        assertTrue(labelLine.getShow());
        assertEquals(100, labelLine.getLength());
        assertFalse(labelLine.getSmooth());
        assertEquals(90, labelLine.getMinTurnAngle());

        LineStyle labelLineStyle = labelLine.getLineStyle();
        assertEquals(BLACK, labelLineStyle.getColor());

        AbstractSeries.LabelLayout labelLayout = scatterSeries.getLabelLayout();
        assertEquals(Align.CENTER, labelLayout.getAlign());

        ScatterSeries.Emphasis emphasis = scatterSeries.getEmphasis();
        assertEquals(FocusType.NONE, emphasis.getFocus());
        assertEquals(BlurScopeType.COORDINATE_SYSTEM, emphasis.getBlurScope());
        assertEquals(1, emphasis.getScale());

        Label emphasisLabel = emphasis.getLabel();
        assertEquals(5, emphasisLabel.getDistance());

        ElementLabelLine emphasisLabelLine = emphasis.getLabelLine();
        assertTrue(emphasisLabelLine.getShow());

        ItemStyle emphasisItemStyle = emphasis.getItemStyle();
        assertEquals(WHITE, emphasisItemStyle.getColor());

        ScatterSeries.Blur blur = scatterSeries.getBlur();
        Label blurLabel = blur.getLabel();
        assertEquals("Series", blurLabel.getFormatter());

        MarkPoint markPoint = scatterSeries.getMarkPoint();
        assertEquals(50, markPoint.getSymbolSize());

        Label markPointLabel = markPoint.getLabel();
        assertEquals(5, markPointLabel.getOffset()[1]);

        ItemStyle markPointItemStyle = markPoint.getItemStyle();
        assertEquals(ROUND, markPointItemStyle.getCap());

        MarkPoint.Emphasis markPointEmphasis = markPoint.getEmphasis();
        assertFalse(markPointEmphasis.getDisabled());

        Label markPointEmphasisLabel = markPointEmphasis.getLabel();
        assertEquals(FontStyle.NORMAL, markPointEmphasisLabel.getFontStyle());

        ItemStyle markPointEmphasisItemStyle = markPointEmphasis.getItemStyle();
        assertEquals(BLACK, markPointEmphasisItemStyle.getBorderColor());

        MarkLine markLine = scatterSeries.getMarkLine();
        assertFalse(markLine.getSilent());
        assertEquals(10, markLine.getSymbolSize()[0]);
        assertEquals(2, markLine.getPrecision());

        Label markLineLabel = markLine.getLabel();
        assertEquals(Label.Position.PositionType.TOP, markLineLabel.getPosition().getPositionType());
        assertEquals(5, markLineLabel.getDistance());

        LineStyle markLineLineStyle = markLine.getLineStyle();
        assertEquals(HasLineStyle.Join.BEVEL, markLineLineStyle.getJoin());

        MarkLine.Emphasis markLineEmphasis = markLine.getEmphasis();
        assertFalse(markLineEmphasis.getDisabled());

        Label markLineEmphasisLabel = markLineEmphasis.getLabel();
        assertEquals(0, markLineEmphasisLabel.getRotate());

        LineStyle markLineEmphasisLineStyle = markLineEmphasis.getLineStyle();
        assertEquals(0, markLineEmphasisLineStyle.getDashOffset());

        MarkArea markArea = scatterSeries.getMarkArea();
        assertFalse(markArea.getSilent());

        Label markAreaLabel = markArea.getLabel();
        assertEquals(5, markAreaLabel.getDistance());

        ItemStyle markAreaItemStyle = markArea.getItemStyle();
        assertEquals(5, markAreaItemStyle.getBorderDashOffset());

        MarkArea.Emphasis markAreaEmphasis = markArea.getEmphasis();
        assertFalse(markAreaEmphasis.getDisabled());

        Label markAreaEmphasisLabel = markAreaEmphasis.getLabel();
        assertEquals(5, markAreaEmphasisLabel.getDistance());

        ItemStyle markAreaEmphasisItemStyle = markAreaEmphasis.getItemStyle();
        assertEquals(10, markAreaEmphasisItemStyle.getMiterLimit());

        MarkArea.Blur markAreaBlur = markArea.getBlur();
        Label markAreaBlurLabel = markAreaBlur.getLabel();
        assertEquals(5, markAreaBlurLabel.getDistance());

        ItemStyle markAreaBlurItemStyle = markAreaBlur.getItemStyle();
        assertEquals(10, markAreaBlurItemStyle.getMiterLimit());

        AbstractSeries.Tooltip tooltip = scatterSeries.getTooltip();
        assertEquals("Scatter Chart Tooltip", tooltip.getValueFormatter());

        TextStyle tooltipTextStyle = tooltip.getTextStyle();
        assertEquals(BLACK, tooltipTextStyle.getColor());
    }

    @Test
    @DisplayName("Load Effect Scatter Series chart from XML")
    public void loadEffectScatterSeriesFromXmlTest() {
        Chart chart = navigateTo(EffectScatterSeriesChartTestView.class).effectScatterSeriesChartId;
        EffectScatterSeries effectScatterSeries = chart.getSeries("effectScatter");

        assertEquals(ColorBy.DATA, effectScatterSeries.getColorBy());
        assertEquals("ripple", effectScatterSeries.getEffectType());
        assertEquals(EffectScatterSeries.EffectType.RENDER, effectScatterSeries.getShowEffectOn());

        EffectScatterSeries.RippleEffect rippleEffect = effectScatterSeries.getRippleEffect();
        assertEquals(BLACK, rippleEffect.getColor());
        assertEquals(3, rippleEffect.getNumber());
        assertEquals(4, rippleEffect.getPeriod());
        assertEquals(2.5, rippleEffect.getScale());
        assertEquals(EffectScatterSeries.RippleEffect.BrushType.FILL, rippleEffect.getBrushType());

        Label label = effectScatterSeries.getLabel();
        assertEquals(200, label.getWidth());

        RichStyle labelRichStyle = label.getRichStyles().get("effectScatterRichStyle");
        assertEquals(10, labelRichStyle.getLineHeight());

        EffectScatterSeries.LabelLine labelLine = effectScatterSeries.getLabelLine();
        assertTrue(labelLine.getShow());
        assertEquals(100, labelLine.getLength());
        assertFalse(labelLine.getSmooth());
        assertEquals(90, labelLine.getMinTurnAngle());

        LineStyle labelLineStyle = labelLine.getLineStyle();
        assertEquals(BLACK, labelLineStyle.getColor());

        AbstractSeries.LabelLayout labelLayout = effectScatterSeries.getLabelLayout();
        assertEquals(Align.CENTER, labelLayout.getAlign());

        EffectScatterSeries.Emphasis emphasis = effectScatterSeries.getEmphasis();
        assertEquals(FocusType.NONE, emphasis.getFocus());
        assertEquals(BlurScopeType.COORDINATE_SYSTEM, emphasis.getBlurScope());
        assertEquals(1, emphasis.getScale());

        Label emphasisLabel = emphasis.getLabel();
        assertEquals(5, emphasisLabel.getDistance());

        ElementLabelLine emphasisLabelLine = emphasis.getLabelLine();
        assertTrue(emphasisLabelLine.getShow());

        ItemStyle emphasisItemStyle = emphasis.getItemStyle();
        assertEquals(WHITE, emphasisItemStyle.getColor());

        EffectScatterSeries.Blur blur = effectScatterSeries.getBlur();
        Label blurLabel = blur.getLabel();
        assertEquals("Effective Scatter", blurLabel.getFormatter());

        ElementLabelLine blurLabelLine = blur.getLabelLine();
        LineStyle blurLabelLineStyle = blurLabelLine.getLineStyle();
        assertEquals(0.9, blurLabelLineStyle.getOpacity());

        MarkPoint markPoint = effectScatterSeries.getMarkPoint();
        assertEquals(50, markPoint.getSymbolSize());

        Label markPointLabel = markPoint.getLabel();
        assertEquals(5, markPointLabel.getOffset()[1]);

        ItemStyle markPointItemStyle = markPoint.getItemStyle();
        assertEquals(ROUND, markPointItemStyle.getCap());

        MarkPoint.Emphasis markPointEmphasis = markPoint.getEmphasis();
        assertFalse(markPointEmphasis.getDisabled());

        Label markPointEmphasisLabel = markPointEmphasis.getLabel();
        assertEquals(FontStyle.NORMAL, markPointEmphasisLabel.getFontStyle());

        ItemStyle markPointEmphasisItemStyle = markPointEmphasis.getItemStyle();
        assertEquals(BLACK, markPointEmphasisItemStyle.getBorderColor());

        MarkLine markLine = effectScatterSeries.getMarkLine();
        assertFalse(markLine.getSilent());
        assertEquals(10, markLine.getSymbolSize()[0]);
        assertEquals(2, markLine.getPrecision());

        Label markLineLabel = markLine.getLabel();
        assertEquals(Label.Position.PositionType.TOP, markLineLabel.getPosition().getPositionType());
        assertEquals(5, markLineLabel.getDistance());

        LineStyle markLineLineStyle = markLine.getLineStyle();
        assertEquals(HasLineStyle.Join.BEVEL, markLineLineStyle.getJoin());

        MarkLine.Emphasis markLineEmphasis = markLine.getEmphasis();
        assertFalse(markLineEmphasis.getDisabled());

        Label markLineEmphasisLabel = markLineEmphasis.getLabel();
        assertEquals(0, markLineEmphasisLabel.getRotate());

        LineStyle markLineEmphasisLineStyle = markLineEmphasis.getLineStyle();
        assertEquals(0, markLineEmphasisLineStyle.getDashOffset());

        MarkArea markArea = effectScatterSeries.getMarkArea();
        assertFalse(markArea.getSilent());

        Label markAreaLabel = markArea.getLabel();
        assertEquals(5, markAreaLabel.getDistance());

        ItemStyle markAreaItemStyle = markArea.getItemStyle();
        assertEquals(5, markAreaItemStyle.getBorderDashOffset());

        MarkArea.Emphasis markAreaEmphasis = markArea.getEmphasis();
        assertFalse(markAreaEmphasis.getDisabled());

        Label markAreaEmphasisLabel = markAreaEmphasis.getLabel();
        assertEquals(5, markAreaEmphasisLabel.getDistance());

        ItemStyle markAreaEmphasisItemStyle = markAreaEmphasis.getItemStyle();
        assertEquals(10, markAreaEmphasisItemStyle.getMiterLimit());

        MarkArea.Blur markAreaBlur = markArea.getBlur();
        Label markAreaBlurLabel = markAreaBlur.getLabel();
        assertEquals(5, markAreaBlurLabel.getDistance());

        ItemStyle markAreaBlurItemStyle = markAreaBlur.getItemStyle();
        assertEquals(10, markAreaBlurItemStyle.getMiterLimit());

        AbstractSeries.Tooltip tooltip = effectScatterSeries.getTooltip();
        assertEquals("Scatter Chart Tooltip", tooltip.getValueFormatter());

        TextStyle tooltipTextStyle = tooltip.getTextStyle();
        assertEquals(BLACK, tooltipTextStyle.getColor());
    }

    @Test
    @DisplayName("Load Radar Series chart from XML")
    public void loadRadarSeriesFromXmlTest() {
        Chart chart = navigateTo(RadarSeriesChartTestView.class).radarSeriesChartId;
        RadarSeries radarSeries = chart.getSeries("radar");

        assertEquals(ColorBy.DATA, radarSeries.getColorBy());
        assertEquals(0, radarSeries.getRadarIndex());
        assertEquals(HasSymbols.SymbolType.CIRCLE, radarSeries.getSymbol().getType());

        Label label = radarSeries.getLabel();
        assertEquals(200, label.getWidth());

        RichStyle labelRichStyle = label.getRichStyles().get("radarRichStyle");
        assertEquals(10, labelRichStyle.getLineHeight());

        AbstractSeries.LabelLayout labelLayout = radarSeries.getLabelLayout();
        assertEquals(AbstractSeries.LabelLayout.MoveOverlapPosition.SHIFT_X, labelLayout.getMoveOverlap());
        assertEquals(5, labelLayout.getLabelLinePoints()[0][0]);
        assertEquals(5, labelLayout.getLabelLinePoints()[1][0]);
        assertEquals(10, labelLayout.getLabelLinePoints()[0][1]);

        RadarSeries.Emphasis emphasis = radarSeries.getEmphasis();
        assertEquals(FocusType.NONE, emphasis.getFocus());
        assertEquals(BlurScopeType.COORDINATE_SYSTEM, emphasis.getBlurScope());

        Label emphasisLabel = emphasis.getLabel();
        assertEquals(5, emphasisLabel.getDistance());

        ItemStyle emphasisItemStyle = emphasis.getItemStyle();
        assertEquals(WHITE, emphasisItemStyle.getColor());

        RadarSeries.Blur blur = radarSeries.getBlur();
        Label blurLabel = blur.getLabel();
        assertEquals("Radar", blurLabel.getFormatter());

        ItemStyle blurItemStyle = blur.getItemStyle();
        assertEquals("inherit", blurItemStyle.getBorderType());

        RadarSeries.Select select = radarSeries.getSelect();
        Label selectLabel = select.getLabel();
        assertEquals("Radar", selectLabel.getFormatter());

        ItemStyle selectItemStyle = select.getItemStyle();
        assertEquals("inherit", selectItemStyle.getBorderType());

        AbstractSeries.Tooltip tooltip = radarSeries.getTooltip();
        assertEquals("Radar Chart Tooltip", tooltip.getValueFormatter());

        TextStyle tooltipTextStyle = tooltip.getTextStyle();
        assertEquals(BLACK, tooltipTextStyle.getColor());
    }

    @Test
    @DisplayName("Load BoxPlot Series chart from XML")
    public void loadBoxplotSeriesFromXmlTest() {
        Chart chart = navigateTo(BoxplotSeriesChartTestView.class).boxplotSeriesChartId;
        BoxplotSeries boxplotSeries = chart.getSeries("boxplot");

        assertEquals("boxplot", boxplotSeries.getName());
        assertEquals(ColorBy.DATA, boxplotSeries.getColorBy());
        assertEquals("7", boxplotSeries.getBoxWidth()[0]);
        assertEquals("50", boxplotSeries.getBoxWidth()[1]);

        BoxplotSeries.Emphasis emphasis = boxplotSeries.getEmphasis();
        assertEquals(FocusType.NONE, emphasis.getFocus());
        assertEquals(BlurScopeType.COORDINATE_SYSTEM, emphasis.getBlurScope());

        ItemStyle emphasisItemStyle = emphasis.getItemStyle();
        assertEquals(WHITE, emphasisItemStyle.getColor());

        BoxplotSeries.Blur blur = boxplotSeries.getBlur();
        ItemStyle blurItemStyle = blur.getItemStyle();
        assertEquals("inherit", blurItemStyle.getBorderType());

        BoxplotSeries.Select select = boxplotSeries.getSelect();
        ItemStyle selectItemStyle = select.getItemStyle();
        assertEquals("inherit", selectItemStyle.getBorderType());

        MarkPoint markPoint = boxplotSeries.getMarkPoint();
        assertEquals(50, markPoint.getSymbolSize());

        Label markPointLabel = markPoint.getLabel();
        assertEquals(5, markPointLabel.getOffset()[1]);

        ItemStyle markPointItemStyle = markPoint.getItemStyle();
        assertEquals(ROUND, markPointItemStyle.getCap());

        MarkPoint.Emphasis markPointEmphasis = markPoint.getEmphasis();
        assertFalse(markPointEmphasis.getDisabled());

        Label markPointEmphasisLabel = markPointEmphasis.getLabel();
        assertEquals(FontStyle.NORMAL, markPointEmphasisLabel.getFontStyle());

        ItemStyle markPointEmphasisItemStyle = markPointEmphasis.getItemStyle();
        assertEquals(BLACK, markPointEmphasisItemStyle.getBorderColor());

        MarkLine markLine = boxplotSeries.getMarkLine();
        assertFalse(markLine.getSilent());
        assertEquals(10, markLine.getSymbolSize()[0]);
        assertEquals(2, markLine.getPrecision());

        Label markLineLabel = markLine.getLabel();
        assertEquals(Label.Position.PositionType.TOP, markLineLabel.getPosition().getPositionType());
        assertEquals(5, markLineLabel.getDistance());

        LineStyle markLineLineStyle = markLine.getLineStyle();
        assertEquals(HasLineStyle.Join.BEVEL, markLineLineStyle.getJoin());

        MarkLine.Emphasis markLineEmphasis = markLine.getEmphasis();
        assertFalse(markLineEmphasis.getDisabled());

        Label markLineEmphasisLabel = markLineEmphasis.getLabel();
        assertEquals(0, markLineEmphasisLabel.getRotate());

        LineStyle markLineEmphasisLineStyle = markLineEmphasis.getLineStyle();
        assertEquals(0, markLineEmphasisLineStyle.getDashOffset());

        MarkArea markArea = boxplotSeries.getMarkArea();
        assertFalse(markArea.getSilent());

        Label markAreaLabel = markArea.getLabel();
        assertEquals(5, markAreaLabel.getDistance());

        ItemStyle markAreaItemStyle = markArea.getItemStyle();
        assertEquals(5, markAreaItemStyle.getBorderDashOffset());

        MarkArea.Emphasis markAreaEmphasis = markArea.getEmphasis();
        assertFalse(markAreaEmphasis.getDisabled());

        Label markAreaEmphasisLabel = markAreaEmphasis.getLabel();
        assertEquals(5, markAreaEmphasisLabel.getDistance());

        ItemStyle markAreaEmphasisItemStyle = markAreaEmphasis.getItemStyle();
        assertEquals(10, markAreaEmphasisItemStyle.getMiterLimit());

        MarkArea.Blur markAreaBlur = markArea.getBlur();
        Label markAreaBlurLabel = markAreaBlur.getLabel();
        assertEquals(5, markAreaBlurLabel.getDistance());

        ItemStyle markAreaBlurItemStyle = markAreaBlur.getItemStyle();
        assertEquals(10, markAreaBlurItemStyle.getMiterLimit());

        AbstractSeries.Tooltip tooltip = boxplotSeries.getTooltip();
        assertEquals("Boxplot Chart Tooltip", tooltip.getValueFormatter());

        TextStyle tooltipTextStyle = tooltip.getTextStyle();
        assertEquals(BLACK, tooltipTextStyle.getColor());
    }

    @Test
    @DisplayName("Load Candlestick Series chart from XML")
    public void loadCandlestickSeriesFromXmlTest() {
        Chart chart = navigateTo(CandlestickSeriesChartTestView.class).candlestickSeriesChartId;
        CandlestickSeries candlestickSeries = chart.getSeries("candlestick");

        assertEquals("candlestick", candlestickSeries.getName());
        assertEquals(ColorBy.DATA, candlestickSeries.getColorBy());
        assertEquals("100", candlestickSeries.getBarWidth());
        assertEquals("50", candlestickSeries.getBarMinWidth());
        assertEquals("200", candlestickSeries.getBarMaxWidth());

        CandlestickSeries.ItemStyle itemStyle = candlestickSeries.getItemStyle();
        assertEquals(BLACK, itemStyle.getBearishBorderColor());
        assertEquals(BLACK, itemStyle.getBearishColor());
        assertEquals(RED, itemStyle.getBullishBorderColor());
        assertEquals(RED, itemStyle.getBullishColor());
        assertEquals(BLUE, itemStyle.getDojiBorderColor());

        CandlestickSeries.Emphasis emphasis = candlestickSeries.getEmphasis();
        assertEquals(FocusType.NONE, emphasis.getFocus());
        assertEquals(BlurScopeType.COORDINATE_SYSTEM, emphasis.getBlurScope());

        CandlestickSeries.ItemStyle emphasisItemStyle = emphasis.getItemStyle();
        assertEquals(BLACK, emphasisItemStyle.getBearishBorderColor());

        CandlestickSeries.Blur blur = candlestickSeries.getBlur();
        CandlestickSeries.ItemStyle blurItemStyle = blur.getItemStyle();
        assertEquals(RED, blurItemStyle.getBullishColor());

        CandlestickSeries.Select select = candlestickSeries.getSelect();
        CandlestickSeries.ItemStyle selectItemStyle = select.getItemStyle();
        assertEquals(BLUE, selectItemStyle.getDojiBorderColor());

        MarkPoint markPoint = candlestickSeries.getMarkPoint();
        assertEquals(50, markPoint.getSymbolSize());

        Label markPointLabel = markPoint.getLabel();
        assertEquals(5, markPointLabel.getOffset()[1]);

        ItemStyle markPointItemStyle = markPoint.getItemStyle();
        assertEquals(ROUND, markPointItemStyle.getCap());

        MarkPoint.Emphasis markPointEmphasis = markPoint.getEmphasis();
        assertFalse(markPointEmphasis.getDisabled());

        Label markPointEmphasisLabel = markPointEmphasis.getLabel();
        assertEquals(FontStyle.NORMAL, markPointEmphasisLabel.getFontStyle());

        ItemStyle markPointEmphasisItemStyle = markPointEmphasis.getItemStyle();
        assertEquals(BLACK, markPointEmphasisItemStyle.getBorderColor());

        MarkLine markLine = candlestickSeries.getMarkLine();
        assertFalse(markLine.getSilent());
        assertEquals(10, markLine.getSymbolSize()[0]);
        assertEquals(2, markLine.getPrecision());

        Label markLineLabel = markLine.getLabel();
        assertEquals(Label.Position.PositionType.TOP, markLineLabel.getPosition().getPositionType());
        assertEquals(5, markLineLabel.getDistance());

        LineStyle markLineLineStyle = markLine.getLineStyle();
        assertEquals(HasLineStyle.Join.BEVEL, markLineLineStyle.getJoin());

        MarkLine.Emphasis markLineEmphasis = markLine.getEmphasis();
        assertFalse(markLineEmphasis.getDisabled());

        Label markLineEmphasisLabel = markLineEmphasis.getLabel();
        assertEquals(0, markLineEmphasisLabel.getRotate());

        LineStyle markLineEmphasisLineStyle = markLineEmphasis.getLineStyle();
        assertEquals(0, markLineEmphasisLineStyle.getDashOffset());

        MarkArea markArea = candlestickSeries.getMarkArea();
        assertFalse(markArea.getSilent());

        Label markAreaLabel = markArea.getLabel();
        assertEquals(5, markAreaLabel.getDistance());

        ItemStyle markAreaItemStyle = markArea.getItemStyle();
        assertEquals(5, markAreaItemStyle.getBorderDashOffset());

        MarkArea.Emphasis markAreaEmphasis = markArea.getEmphasis();
        assertFalse(markAreaEmphasis.getDisabled());

        Label markAreaEmphasisLabel = markAreaEmphasis.getLabel();
        assertEquals(5, markAreaEmphasisLabel.getDistance());

        ItemStyle markAreaEmphasisItemStyle = markAreaEmphasis.getItemStyle();
        assertEquals(10, markAreaEmphasisItemStyle.getMiterLimit());

        MarkArea.Blur markAreaBlur = markArea.getBlur();
        Label markAreaBlurLabel = markAreaBlur.getLabel();
        assertEquals(5, markAreaBlurLabel.getDistance());

        ItemStyle markAreaBlurItemStyle = markAreaBlur.getItemStyle();
        assertEquals(10, markAreaBlurItemStyle.getMiterLimit());

        AbstractSeries.Tooltip tooltip = candlestickSeries.getTooltip();
        assertEquals("Candlestick Chart Tooltip", tooltip.getValueFormatter());

        TextStyle tooltipTextStyle = tooltip.getTextStyle();
        assertEquals(BLACK, tooltipTextStyle.getColor());
    }

    @Test
    @DisplayName("Load Funnel Series chart from XML")
    public void loadFunnelSeriesFromXmlTest() {
        Chart chart = navigateTo(FunnelSeriesChartTestView.class).funnelSeriesChartId;
        FunnelSeries funnelSeries = chart.getSeries("funnel");

        assertEquals("funnel", funnelSeries.getName());
        assertEquals(ColorBy.DATA, funnelSeries.getColorBy());
        assertEquals(0, funnelSeries.getMin());
        assertEquals(100, funnelSeries.getMax());
        assertEquals("0%", funnelSeries.getMinSize());
        assertEquals("100%", funnelSeries.getMaxSize());

        ItemStyle itemStyle = funnelSeries.getItemStyle();
        assertEquals(10, itemStyle.getMiterLimit());

        FunnelSeries.Emphasis emphasis = funnelSeries.getEmphasis();
        assertEquals(FocusType.NONE, emphasis.getFocus());
        assertEquals(BlurScopeType.COORDINATE_SYSTEM, emphasis.getBlurScope());

        ItemStyle emphasisItemStyle = emphasis.getItemStyle();
        assertEquals(HasLineStyle.Join.BEVEL, emphasisItemStyle.getJoin());

        FunnelSeries.Blur blur = funnelSeries.getBlur();
        ItemStyle blurItemStyle = blur.getItemStyle();
        assertEquals("solid", blurItemStyle.getBorderType());

        FunnelSeries.Select select = funnelSeries.getSelect();
        ItemStyle selectItemStyle = select.getItemStyle();
        assertEquals(HasLineStyle.Join.BEVEL, selectItemStyle.getJoin());

        MarkPoint markPoint = funnelSeries.getMarkPoint();
        assertEquals(50, markPoint.getSymbolSize());

        Label markPointLabel = markPoint.getLabel();
        assertEquals(5, markPointLabel.getOffset()[1]);

        ItemStyle markPointItemStyle = markPoint.getItemStyle();
        assertEquals(ROUND, markPointItemStyle.getCap());

        MarkPoint.Emphasis markPointEmphasis = markPoint.getEmphasis();
        assertFalse(markPointEmphasis.getDisabled());

        Label markPointEmphasisLabel = markPointEmphasis.getLabel();
        assertEquals(FontStyle.NORMAL, markPointEmphasisLabel.getFontStyle());

        ItemStyle markPointEmphasisItemStyle = markPointEmphasis.getItemStyle();
        assertEquals(BLACK, markPointEmphasisItemStyle.getBorderColor());

        MarkLine markLine = funnelSeries.getMarkLine();
        assertFalse(markLine.getSilent());
        assertEquals(10, markLine.getSymbolSize()[0]);
        assertEquals(2, markLine.getPrecision());

        Label markLineLabel = markLine.getLabel();
        assertEquals(Label.Position.PositionType.TOP, markLineLabel.getPosition().getPositionType());
        assertEquals(5, markLineLabel.getDistance());

        LineStyle markLineLineStyle = markLine.getLineStyle();
        assertEquals(HasLineStyle.Join.BEVEL, markLineLineStyle.getJoin());

        MarkLine.Emphasis markLineEmphasis = markLine.getEmphasis();
        assertFalse(markLineEmphasis.getDisabled());

        Label markLineEmphasisLabel = markLineEmphasis.getLabel();
        assertEquals(0, markLineEmphasisLabel.getRotate());

        LineStyle markLineEmphasisLineStyle = markLineEmphasis.getLineStyle();
        assertEquals(0, markLineEmphasisLineStyle.getDashOffset());

        MarkArea markArea = funnelSeries.getMarkArea();
        assertFalse(markArea.getSilent());

        Label markAreaLabel = markArea.getLabel();
        assertEquals(5, markAreaLabel.getDistance());

        ItemStyle markAreaItemStyle = markArea.getItemStyle();
        assertEquals(5, markAreaItemStyle.getBorderDashOffset());

        MarkArea.Emphasis markAreaEmphasis = markArea.getEmphasis();
        assertFalse(markAreaEmphasis.getDisabled());

        Label markAreaEmphasisLabel = markAreaEmphasis.getLabel();
        assertEquals(5, markAreaEmphasisLabel.getDistance());

        ItemStyle markAreaEmphasisItemStyle = markAreaEmphasis.getItemStyle();
        assertEquals(10, markAreaEmphasisItemStyle.getMiterLimit());

        MarkArea.Blur markAreaBlur = markArea.getBlur();
        Label markAreaBlurLabel = markAreaBlur.getLabel();
        assertEquals(5, markAreaBlurLabel.getDistance());

        ItemStyle markAreaBlurItemStyle = markAreaBlur.getItemStyle();
        assertEquals(10, markAreaBlurItemStyle.getMiterLimit());

        AbstractSeries.Tooltip tooltip = funnelSeries.getTooltip();
        assertEquals("Funnel Chart Tooltip", tooltip.getValueFormatter());

        TextStyle tooltipTextStyle = tooltip.getTextStyle();
        assertEquals(BLACK, tooltipTextStyle.getColor());
    }

    @Test
    @DisplayName("Load Gauge Series chart from XML")
    public void loadGaugeSeriesFromXmlTest() {
        Chart chart = navigateTo(GaugeSeriesChartTestView.class).gaugeSeriesChartId;
        GaugeSeries gaugeSeries = chart.getSeries("gauge");

        assertEquals("gauge", gaugeSeries.getName());
        assertEquals(ColorBy.DATA, gaugeSeries.getColorBy());
        assertEquals(0, gaugeSeries.getMin());
        assertEquals(100, gaugeSeries.getMax());
        assertEquals("50%", gaugeSeries.getCenter()[0]);
        assertEquals("75%", gaugeSeries.getRadius());
        assertEquals(225, gaugeSeries.getStartAngle());
        assertEquals(-45, gaugeSeries.getEndAngle());
        assertTrue(gaugeSeries.getClockwise());
        assertEquals(10, gaugeSeries.getSplitNumber());

        GaugeSeries.AxisLine axisLine = gaugeSeries.getAxisLine();
        assertFalse(axisLine.getRoundCap());

        GaugeSeries.AxisLine.LineStyle axisLineStyle = axisLine.getLineStyle();
        assertEquals(BLACK, axisLineStyle.getShadowColor());
        assertTrue(Maps.difference(
                Map.of(
                        0.1, Color.RED,
                        0.2, Color.GREEN,
                        0.3, Color.BLUE,
                        1.0, Color.BLACK
                ), axisLineStyle.getColorPalette()).areEqual());

        GaugeSeries.Progress progress = gaugeSeries.getProgress();
        assertTrue(progress.getShow());
        assertTrue(progress.getOverlap());
        assertEquals(10, progress.getWidth());
        assertFalse(progress.getRoundCap());
        assertFalse(progress.getClip());

        ItemStyle progressItemStyle = progress.getItemStyle();
        assertEquals(BLACK, progressItemStyle.getShadowColor());

        GaugeSeries.SplitLine splitLine = gaugeSeries.getSplitLine();
        assertEquals(10, splitLine.getLength());
        assertEquals(10, splitLine.getDistance());

        LineStyle splitLineStyle = splitLine.getLineStyle();
        assertEquals(1, splitLineStyle.getWidth());

        GaugeSeries.AxisTick axisTick = gaugeSeries.getAxisTick();
        assertEquals(10, axisTick.getLength());

        LineStyle axisTickLineStyle = axisTick.getLineStyle();
        assertEquals(0, axisTickLineStyle.getShadowBlur());

        Label axisLabel = gaugeSeries.getAxisLabel();
        assertEquals(Align.CENTER, axisLabel.getAlign());

        GaugeSeries.Pointer pointer = gaugeSeries.getPointer();
        assertTrue(pointer.getShow());
        assertTrue(pointer.getShowAbove());
        assertEquals("lumo:align-center", pointer.getIcon());
        assertEquals("0", pointer.getOffsetCenter()[0]);
        assertEquals("60%", pointer.getLength());
        assertEquals(6, pointer.getWidth());
        assertFalse(pointer.getKeepAspect());

        ItemStyle pointerItemStyle = pointer.getItemStyle();
        assertEquals(1, pointerItemStyle.getBorderWidth());

        GaugeSeries.Anchor anchor = gaugeSeries.getAnchor();
        assertTrue(anchor.getShow());
        assertTrue(anchor.getShowAbove());
        assertEquals("lumo:align-center", anchor.getIcon());
        assertEquals("0", anchor.getOffsetCenter()[0]);
        assertFalse(anchor.getKeepAspect());

        ItemStyle anchorItemStyle = anchor.getItemStyle();
        assertEquals(1, anchorItemStyle.getBorderWidth());

        ItemStyle itemStyle = gaugeSeries.getItemStyle();
        assertEquals(10, itemStyle.getMiterLimit());

        GaugeSeries.Emphasis emphasis = gaugeSeries.getEmphasis();
        ItemStyle emphasisItemStyle = emphasis.getItemStyle();
        assertEquals(HasLineStyle.Join.BEVEL, emphasisItemStyle.getJoin());

        GaugeSeries.Title title = gaugeSeries.getTitle();
        assertFalse(title.getShow());
        assertEquals("20%", title.getOffsetCenter()[1]);
        assertEquals(BLACK, title.getColor());
        assertEquals(FontStyle.NORMAL, title.getFontStyle());
        assertTrue(title.getValueAnimation());

        GaugeSeries.Detail detail = gaugeSeries.getDetail();
        assertEquals("Gauge Detail", detail.getFormatter());
        assertEquals(Align.CENTER, detail.getRichStyles().get("testStyle").getAlign());

        MarkPoint markPoint = gaugeSeries.getMarkPoint();

        assertEquals(50, markPoint.getSymbolSize());

        Label markPointLabel = markPoint.getLabel();
        assertEquals(5, markPointLabel.getOffset()[1]);

        ItemStyle markPointItemStyle = markPoint.getItemStyle();
        assertEquals(ROUND, markPointItemStyle.getCap());

        MarkPoint.Emphasis markPointEmphasis = markPoint.getEmphasis();
        assertFalse(markPointEmphasis.getDisabled());

        Label markPointEmphasisLabel = markPointEmphasis.getLabel();
        assertEquals(FontStyle.NORMAL, markPointEmphasisLabel.getFontStyle());

        ItemStyle markPointEmphasisItemStyle = markPointEmphasis.getItemStyle();
        assertEquals(BLACK, markPointEmphasisItemStyle.getBorderColor());

        MarkLine markLine = gaugeSeries.getMarkLine();
        assertFalse(markLine.getSilent());
        assertEquals(10, markLine.getSymbolSize()[0]);
        assertEquals(2, markLine.getPrecision());

        Label markLineLabel = markLine.getLabel();
        assertEquals(Label.Position.PositionType.TOP, markLineLabel.getPosition().getPositionType());
        assertEquals(5, markLineLabel.getDistance());

        LineStyle markLineLineStyle = markLine.getLineStyle();
        assertEquals(HasLineStyle.Join.BEVEL, markLineLineStyle.getJoin());

        MarkLine.Emphasis markLineEmphasis = markLine.getEmphasis();
        assertFalse(markLineEmphasis.getDisabled());

        Label markLineEmphasisLabel = markLineEmphasis.getLabel();
        assertEquals(0, markLineEmphasisLabel.getRotate());

        LineStyle markLineEmphasisLineStyle = markLineEmphasis.getLineStyle();
        assertEquals(0, markLineEmphasisLineStyle.getDashOffset());

        MarkArea markArea = gaugeSeries.getMarkArea();
        assertFalse(markArea.getSilent());

        Label markAreaLabel = markArea.getLabel();
        assertEquals(5, markAreaLabel.getDistance());

        ItemStyle markAreaItemStyle = markArea.getItemStyle();
        assertEquals(5, markAreaItemStyle.getBorderDashOffset());

        MarkArea.Emphasis markAreaEmphasis = markArea.getEmphasis();
        assertFalse(markAreaEmphasis.getDisabled());

        Label markAreaEmphasisLabel = markAreaEmphasis.getLabel();
        assertEquals(5, markAreaEmphasisLabel.getDistance());

        ItemStyle markAreaEmphasisItemStyle = markAreaEmphasis.getItemStyle();
        assertEquals(10, markAreaEmphasisItemStyle.getMiterLimit());

        MarkArea.Blur markAreaBlur = markArea.getBlur();
        Label markAreaBlurLabel = markAreaBlur.getLabel();
        assertEquals(5, markAreaBlurLabel.getDistance());

        ItemStyle markAreaBlurItemStyle = markAreaBlur.getItemStyle();
        assertEquals(10, markAreaBlurItemStyle.getMiterLimit());

        AbstractSeries.Tooltip tooltip = gaugeSeries.getTooltip();
        assertEquals("Gauge Chart Tooltip", tooltip.getValueFormatter());

        TextStyle tooltipTextStyle = tooltip.getTextStyle();
        assertEquals(BLACK, tooltipTextStyle.getColor());
    }
}
