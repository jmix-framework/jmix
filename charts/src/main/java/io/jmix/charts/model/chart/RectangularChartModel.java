/*
 * Copyright 2021 Haulmont.
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

package io.jmix.charts.model.chart;



import io.jmix.charts.model.*;
import io.jmix.charts.model.cursor.Cursor;
import io.jmix.charts.model.trendline.TrendLine;
import io.jmix.ui.meta.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.List;

@StudioProperties(properties = {
        @StudioProperty(name = "marginBottom", type = PropertyType.INTEGER, defaultValue = "20"),
        @StudioProperty(name = "marginLeft", type = PropertyType.INTEGER, defaultValue = "20"),
        @StudioProperty(name = "marginRight", type = PropertyType.INTEGER, defaultValue = "20"),
        @StudioProperty(name = "marginTop", type = PropertyType.INTEGER, defaultValue = "20")
})
public interface RectangularChartModel<T extends RectangularChartModel>
        extends CoordinateChartModel<T>, HasMargins<T> {
    /**
     * @return chart cursor
     */
    Cursor getChartCursor();

    /**
     * Sets cursor of a chart.
     *
     * @param chartCursor the chart cursor
     * @return  rectangular chart model
     */
    @StudioElement
    T setChartCursor(Cursor chartCursor);

    /**
     * @return chart scrollbar
     */
    Scrollbar getChartScrollbar();

    /**
     * Sets chart scrollbar.
     *
     * @param chartScrollbar the chart scrollbar
     * @return  rectangular chart model
     */
    @StudioElement(caption = "Chart Scrollbar", xmlElement = "chartScrollbar")
    T setChartScrollbar(Scrollbar chartScrollbar);

    /**
     * @return list of trend lines
     */
    List<TrendLine> getTrendLines();

    /**
     * Sets the list of trend lines.
     *
     * @param trendLines list of trend lines
     * @return  rectangular chart model
     */
    @StudioElementsGroup(caption = "Trend Lines", xmlElement = "trendLines")
    T setTrendLines(List<TrendLine> trendLines);

    /**
     * Adds trend lines.
     *
     * @param trendLines the trend lines
     * @return  rectangular chart model
     */
    T addTrendLines(TrendLine... trendLines);

    /**
     * @return angle of the 3D part of plot area
     */
    Integer getAngle();

    /**
     * Sets the angle of the 3D part of plot area. This creates a 3D effect (if the depth3D is greater than 0). If
     * not set the default value is 0.
     *
     * @param angle the angle
     * @return  rectangular chart model
     */
    @StudioProperty(defaultValue = "0")
    T setAngle(Integer angle);

    /**
     * @return auto margin offset
     */
    Integer getAutoMarginOffset();

    /**
     * Sets space left from axis labels/title to the chart's outside border, if autoMargins set to true. If not set
     * the default value is 10.
     *
     * @param autoMarginOffset the auto margin offset
     * @return  rectangular chart model
     */
    @StudioProperty(defaultValue = "10")
    T setAutoMarginOffset(Integer autoMarginOffset);

    /**
     * @return true if auto margins is enabled
     */
    Boolean getAutoMargins();

    /**
     * Specifies if margins of a chart should be calculated automatically so that labels of axes would fit. The chart
     * will adjust only margins with axes. Other margins will use values set with marginRight, marginTop, marginLeft
     * and marginBottom properties. If not set the default value is true.
     *
     * @param autoMargins auto margins option
     * @return  rectangular chart model
     */
    @StudioProperty(defaultValue = "true")
    T setAutoMargins(Boolean autoMargins);

    /**
     * @return depth of the 3D part of plot area
     */
    Integer getDepth3D();

    /**
     * Sets the depth of the 3D part of plot area. This creates a 3D effect (if the angle is greater than 0). If not
     * set the default value is 0.
     *
     * @param depth3D the depth 3D
     * @return  rectangular chart model
     */
    @StudioProperty(defaultValue = "0")
    T setDepth3D(Integer depth3D);

    /**
     * @return true if margins update is enabled
     */
    Boolean getMarginsUpdated();

    /**
     * Set to false if you need margins to be recalculated on next
     * <a href="http://docs.amcharts.com/3/javascriptcharts/AmChart#validateNow">validateNow()</a> call. If not set
     * the default value is false.
     *
     * @param marginsUpdated the margin update option
     * @return  rectangular chart model
     */
    @StudioProperty(defaultValue = "false")
    T setMarginsUpdated(Boolean marginsUpdated);

    /**
     * @return opacity of plot area's border
     */
    Double getPlotAreaBorderAlpha();

    /**
     * Sets the opacity of plot area's border. Value range is 0 - 1. If not set the default value is 0.
     *
     * @param plotAreaBorderAlpha opacity of plot area's border
     * @return  rectangular chart model
     */
    @StudioProperty(defaultValue = "0")
    @Max(1)
    @Min(0)
    T setPlotAreaBorderAlpha(Double plotAreaBorderAlpha);

    /**
     * @return plot area border color
     */
    Color getPlotAreaBorderColor();

    /**
     * Sets the color of the plot area's border. Set it to a value higher than 0 to make it visible. If not set the
     * default value is #000000.
     *
     * @param plotAreaBorderColor the plot area border color
     * @return  rectangular chart model
     */
    @StudioProperty(type = PropertyType.OPTIONS, defaultValue = "#000000")
    T setPlotAreaBorderColor(Color plotAreaBorderColor);

    /**
     * @return opacity of plot area
     */
    Double getPlotAreaFillAlphas();

    /**
     * Sets opacity of plot area. If not set the default value is 0.
     *
     * @param plotAreaFillAlphas opacity
     * @return  rectangular chart model
     */
    @StudioProperty(defaultValue = "0")
    @Max(1)
    @Min(0)
    T setPlotAreaFillAlphas(Double plotAreaFillAlphas);

    /**
     * @return list of plot area colors
     */
    List<Color> getPlotAreaFillColors();

    /**
     * Sets the list of plot area colors. Create several colors if you need to generate gradients or create one if you
     * need to get a solid color. If not set the default value is #FFFFFF.
     *
     * @param plotAreaFillColors list of plot area colors
     * @return  rectangular chart model
     */
    @StudioCollection(xmlElement = "plotAreaFillColors",
            itemXmlElement = "color",
            itemCaption = "Plot Area Fill Color",
            itemProperties = {
                    @StudioProperty(name = "value", type = PropertyType.ENUMERATION,
                            options = {"@link io.jmix.charts.model.Color"})
            })
    T setPlotAreaFillColors(List<Color> plotAreaFillColors);

    /**
     * @return plot area gradient angle
     */
    Integer getPlotAreaGradientAngle();

    /**
     * Sets the gradient angle, if you are using gradients to fill the plot area. The only allowed values are
     * horizontal and vertical: 0, 90, 180, 270. If not set the default value is 0.
     *
     * @param plotAreaGradientAngle the plot area gradient angle
     * @return  rectangular chart model
     */
    @StudioProperty(type = PropertyType.ENUMERATION, options = {"0", "90", "180", "270"}, defaultValue = "0")
    T setPlotAreaGradientAngle(Integer plotAreaGradientAngle);

    /**
     * @return opacity of zoom-out button background
     */
    Double getZoomOutButtonAlpha();

    /**
     * Sets opacity of zoom-out button background. If not set the default value is 0.
     *
     * @param zoomOutButtonAlpha opacity of zoom-out button background
     * @return  rectangular chart model
     */
    @StudioProperty(defaultValue = "0")
    @Max(1)
    @Min(0)
    T setZoomOutButtonAlpha(Double zoomOutButtonAlpha);

    /**
     * @return zoom-out button background color
     */
    Color getZoomOutButtonColor();

    /**
     * Sets zoom-out button background color. If not set the default value is #e5e5e5.
     *
     * @param zoomOutButtonColor zoom-out button background color
     * @return  rectangular chart model
     */
    @StudioProperty(type = PropertyType.OPTIONS, defaultValue = "#E5E5E5")
    T setZoomOutButtonColor(Color zoomOutButtonColor);

    /**
     * @return zoom out button image name
     */
    String getZoomOutButtonImage();

    /**
     * Sets the name of zoom-out button image. Note, you don't have to set image extension. If svgIcons is set to true,
     * ".svg" will be added to the file name if SVG is supported by the browser, otherwise – ".png". If not set the
     * default value is "lens".
     *
     * @param zoomOutButtonImage zoom out button image name string
     * @return  rectangular chart model
     */
    @StudioProperty(defaultValue = "lens")
    T setZoomOutButtonImage(String zoomOutButtonImage);

    /**
     * @return zoom out button image size
     */
    Integer getZoomOutButtonImageSize();

    /**
     * Sets the size of zoom-out button image. If not set the default value is 17.
     *
     * @param zoomOutButtonImageSize the zoom out button image size
     * @return  rectangular chart model
     */
    @StudioProperty(defaultValue = "17")
    T setZoomOutButtonImageSize(Integer zoomOutButtonImageSize);

    /**
     * @return zoom out button padding
     */
    Integer getZoomOutButtonPadding();

    /**
     * Sets padding around the text and image. If not set the default value is 8.
     *
     * @param zoomOutButtonPadding the zoom out button padding
     * @return  rectangular chart model
     */
    @StudioProperty(defaultValue = "8")
    T setZoomOutButtonPadding(Integer zoomOutButtonPadding);

    /**
     * @return opacity of zoom-out button background
     */
    Double getZoomOutButtonRollOverAlpha();

    /**
     * Sets opacity of zoom-out button background when mouse is over it. If not set the default value is 1.
     *
     * @param zoomOutButtonRollOverAlpha opacity of zoom-out button background
     * @return  rectangular chart model
     */
    @StudioProperty(defaultValue = "1")
    @Max(1)
    @Min(0)
    T setZoomOutButtonRollOverAlpha(Double zoomOutButtonRollOverAlpha);

    /**
     * @return zoom out text
     */
    String getZoomOutText();

    /**
     * Sets text in the zoom-out button. If not set the default value is "Show all".
     *
     * @param zoomOutText zoom out text string
     * @return  rectangular chart model
     */
    @StudioProperty(defaultValue = "Show all")
    T setZoomOutText(String zoomOutText);

    /**
     * @return maximum zoom factor
     */
    Integer getMaxZoomFactor();

    /**
     * Sets the maximum zoom factor value axes. If not set the default value is 20.
     *
     * @param maxZoomFactor maximum zoom factor
     * @return  rectangular chart model
     */
    @StudioProperty(defaultValue = "20")
    T setMaxZoomFactor(Integer maxZoomFactor);

    /**
     * @return minimum margin bottom
     */
    Integer getMinMarginBottom();

    /**
     * Sets the minimum margin bottom. If bottom side has a value axis and autoMargins is set to true, the margin of
     * this side will be not less than set on minMarginBottom property.
     *
     * @param minMarginBottom the minimum margin bottom
     * @return  rectangular chart model
     */
    @StudioProperty
    T setMinMarginBottom(Integer minMarginBottom);

    /**
     * @return minimum margin left
     */
    Integer getMinMarginLeft();

    /**
     * Sets the minimum margin left. If left side has a value axis and autoMargins is set to true, the margin of this
     * side will be not less than set on minMarginLeft property.
     *
     * @param minMarginLeft the minimum margin left
     * @return  rectangular chart model
     */
    @StudioProperty
    T setMinMarginLeft(Integer minMarginLeft);

    /**
     * @return minimum margin right
     */
    Integer getMinMarginRight();

    /**
     * Sets the minimum margin right. If right side has a value axis and autoMargins is set to true, the margin of
     * this side will be not less than set on minMarginRight property.
     *
     * @param minMarginRight the minimum margin right
     * @return  rectangular chart model
     */
    @StudioProperty
    T setMinMarginRight(Integer minMarginRight);

    /**
     * @return minimum margin top
     */
    Integer getMinMarginTop();

    /**
     * Sets the minimum margin top. If top side has a value axis and autoMargins is set to true, the margin of this
     * side will be not less than set on minMarginTop property.
     *
     * @param minMarginTop the minimum margin top
     * @return  rectangular chart model
     */
    @StudioProperty
    T setMinMarginTop(Integer minMarginTop);

    /**
     * @return zoom out button tab index
     */
    Integer getZoomOutButtonTabIndex();

    /**
     * In case you set it to some number, the chart will set focus on zoom-out button when user clicks tab key. When a
     * focus is set, screen readers like NVDA Screen reader will read zoomOutText. If user clicks Enter when a focus is
     * set, the chart will zoom-out. Note, not all browsers and readers support this.
     *
     * @param zoomOutButtonTabIndex the zoom out button tab index
     * @return  rectangular chart model
     */
    @StudioProperty
    T setZoomOutButtonTabIndex(Integer zoomOutButtonTabIndex);
}