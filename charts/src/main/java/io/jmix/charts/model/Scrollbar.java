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

package io.jmix.charts.model;


import io.jmix.charts.model.axis.ValueAxis;
import io.jmix.charts.model.chart.impl.SerialChartModelImpl;
import io.jmix.charts.model.chart.impl.XYChartModelImpl;
import io.jmix.charts.model.graph.AbstractGraph;
import io.jmix.charts.model.graph.GraphType;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioElement;
import io.jmix.ui.meta.StudioProperty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * Creates a scrollbar for {@link SerialChartModelImpl} and {@link XYChartModelImpl} charts.
 * <br>
 * See documentation for properties of ChartScrollbar JS Object.
 * <br>
 * <a href="http://docs.amcharts.com/3/javascriptcharts/ChartScrollbar">http://docs.amcharts.com/3/javascriptcharts/ChartScrollbar</a>
 */
@StudioElement(xmlns = "http://jmix.io/schema/ui/charts",
        xmlnsAlias = "chart")
public class Scrollbar extends AbstractChartObject {

    private static final long serialVersionUID = 6850646494521513508L;

    private String accessibleLabel;

    private Boolean autoGridCount;

    private Double backgroundAlpha;

    private Color backgroundColor;

    private Color color;

    private String dragCursorDown;

    private String dragCursorHover;

    private String dragIcon;

    private Integer dragIconHeight;

    private Integer dragIconWidth;

    private Boolean enabled;

    private String graph;

    private Double graphFillAlpha;

    private Color graphFillColor;

    private Double graphLineAlpha;

    private Color graphLineColor;

    private GraphType graphType;

    private Double gridAlpha;

    private Color gridColor;

    private Integer gridCount;

    private Boolean hideResizeGrips;

    private Boolean ignoreCustomColors;

    private Double maximum;

    private Double minimum;

    private Integer offset;

    private Boolean oppositeAxis;

    private Boolean resizeEnabled;

    private Integer scrollbarHeight;

    private Double scrollDuration;

    private Double selectedBackgroundAlpha;

    private Color selectedBackgroundColor;

    private Double selectedGraphFillAlpha;

    private Color selectedGraphFillColor;

    private Double selectedGraphLineAlpha;

    private Color selectedGraphLineColor;

    private Integer tabIndex;

    private Boolean updateOnReleaseOnly;

    private String hResizeCursor;

    private String hResizeCursorDown;

    private String hResizeCursorHover;

    private String vResizeCursor;

    private String vResizeCursorDown;

    private String vResizeCursorHover;

    /**
     * @return true if a number of gridCount is specified automatically, according to the axis size
     */
    public Boolean getAutoGridCount() {
        return autoGridCount;
    }

    /**
     * Set autoGridCount to true if a number of gridCount should be specified automatically, according to the axis size.
     * If not set the default value is false.
     *
     * @param autoGridCount autoGridCount option
     * @return scrollbar
     */
    @StudioProperty(defaultValue = "false")
    public Scrollbar setAutoGridCount(Boolean autoGridCount) {
        this.autoGridCount = autoGridCount;
        return this;
    }

    /**
     * @return background opacity
     */
    public Double getBackgroundAlpha() {
        return backgroundAlpha;
    }

    /**
     * Sets background opacity. If not set the default value is 1.
     *
     * @param backgroundAlpha background opacity
     * @return scrollbar
     */
    @StudioProperty(defaultValue = "1")
    @Max(1)
    @Min(0)
    public Scrollbar setBackgroundAlpha(Double backgroundAlpha) {
        this.backgroundAlpha = backgroundAlpha;
        return this;
    }

    /**
     * @return background color of the scrollbar
     */
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Sets background color of the scrollbar. If not set the default value is #D4D4D4.
     *
     * @param backgroundColor background color of the scrollbar
     * @return scrollbar
     */
    @StudioProperty(type = PropertyType.OPTIONS, defaultValue = "#D4D4D4")
    public Scrollbar setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        return this;
    }

    /**
     * @return text color
     */
    public Color getColor() {
        return color;
    }

    /**
     * Sets text color. If not set the default value is #FFFFFF.
     *
     * @param color text color
     * @return scrollbar
     */
    @StudioProperty(type = PropertyType.OPTIONS, defaultValue = "#FFFFFF")
    public Scrollbar setColor(Color color) {
        this.color = color;
        return this;
    }

    /**
     * @return drag icon height
     */
    public Integer getDragIconHeight() {
        return dragIconHeight;
    }

    /**
     * Sets height of resize grip image. Note, you should also update the image in amcharts/images folder if you don't
     * want it to be distorted because of resizing. If not set the default value is 35.
     *
     * @param dragIconHeight drag icon height
     * @return scrollbar
     */
    @StudioProperty(defaultValue = "35")
    public Scrollbar setDragIconHeight(Integer dragIconHeight) {
        this.dragIconHeight = dragIconHeight;
        return this;
    }

    /**
     * @return drag icon width
     */
    public Integer getDragIconWidth() {
        return dragIconWidth;
    }

    /**
     * Sets width of resize grip image. Note, you should also update the image in amcharts/images folder if you don't
     * want it to be distorted because of resizing. If not set the default value is 35.
     *
     * @param dragIconWidth drag icon width
     * @return scrollbar
     */
    @StudioProperty(defaultValue = "35")
    public Scrollbar setDragIconWidth(Integer dragIconWidth) {
        this.dragIconWidth = dragIconWidth;
        return this;
    }

    /**
     * @return graph fill opacity
     */
    public Double getGraphFillAlpha() {
        return graphFillAlpha;
    }

    /**
     * Sets graph fill opacity. Value range is 0 - 1. If not set the default value is 1.
     *
     * @param graphFillAlpha graph fill opacity
     * @return scrollbar
     */
    @StudioProperty(defaultValue = "1")
    @Max(1)
    @Min(0)
    public Scrollbar setGraphFillAlpha(Double graphFillAlpha) {
        this.graphFillAlpha = graphFillAlpha;
        return this;
    }

    /**
     * @return graph fill color
     */
    public Color getGraphFillColor() {
        return graphFillColor;
    }

    /**
     * Sets graph fill color. If not set the default value is #BBBBBB.
     *
     * @param graphFillColor graph fill color
     * @return scrollbar
     */
    @StudioProperty(type = PropertyType.OPTIONS, defaultValue = "#BBBBBB")
    public Scrollbar setGraphFillColor(Color graphFillColor) {
        this.graphFillColor = graphFillColor;
        return this;
    }

    /**
     * @return graph line opacity
     */
    public Double getGraphLineAlpha() {
        return graphLineAlpha;
    }

    /**
     * Sets graph line opacity. Value range is 0 - 1. If not set the default value is 0.
     *
     * @param graphLineAlpha graph line opacity
     * @return scrollbar
     */
    @StudioProperty(defaultValue = "0")
    @Max(1)
    @Min(0)
    public Scrollbar setGraphLineAlpha(Double graphLineAlpha) {
        this.graphLineAlpha = graphLineAlpha;
        return this;
    }

    /**
     * @return graph line color
     */
    public Color getGraphLineColor() {
        return graphLineColor;
    }

    /**
     * Sets graph line color. If not set the default value is #BBBBBB.
     *
     * @param graphLineColor graph line color
     * @return scrollbar
     */
    @StudioProperty(type = PropertyType.OPTIONS, defaultValue = "#BBBBBB")
    public Scrollbar setGraphLineColor(Color graphLineColor) {
        this.graphLineColor = graphLineColor;
        return this;
    }

    /**
     * @return graph type
     */
    public GraphType getGraphType() {
        return graphType;
    }

    /**
     * Sets the graph type. By default the graph type is the same as the original graph's type, however in case of
     * candlestick or ohlc you might want to show line graph in the scrollbar. Possible values are: line, column,
     * step, smoothedLine, candlestick, ohlc.
     *
     * @param graphType graph type
     * @return scrollbar
     */
    @StudioProperty(type = PropertyType.ENUMERATION)
    public Scrollbar setGraphType(GraphType graphType) {
        this.graphType = graphType;
        return this;
    }

    /**
     * @return grid opacity
     */
    public Double getGridAlpha() {
        return gridAlpha;
    }

    /**
     * Sets grid opacity. Value range is 0 - 1. If not set the default value is 0.7.
     *
     * @param gridAlpha grid opacity
     * @return scrollbar
     */
    @StudioProperty(defaultValue = "0.7")
    @Max(1)
    @Min(0)
    public Scrollbar setGridAlpha(Double gridAlpha) {
        this.gridAlpha = gridAlpha;
        return this;
    }

    /**
     * @return grid color
     */
    public Color getGridColor() {
        return gridColor;
    }

    /**
     * Sets grid color. If not set the default value is #FFFFFF.
     *
     * @param gridColor grid color
     * @return scrollbar
     */
    @StudioProperty(type = PropertyType.OPTIONS, defaultValue = "#FFFFFF")
    public Scrollbar setGridColor(Color gridColor) {
        this.gridColor = gridColor;
        return this;
    }

    /**
     * @return number of grid lines
     */
    public Integer getGridCount() {
        return gridCount;
    }

    /**
     * Sets the number of grid lines. If not set the default value is 0.
     *
     * @param gridCount number of grid lines
     * @return scrollbar
     */
    @StudioProperty(defaultValue = "0")
    public Scrollbar setGridCount(Integer gridCount) {
        this.gridCount = gridCount;
        return this;
    }

    /**
     * @return true if resize grips are hidden when the mouse is away from the scrollbar
     */
    public Boolean getHideResizeGrips() {
        return hideResizeGrips;
    }

    /**
     * Set hideResizeGrips to true if resize grips should be hidden when the mouse is away from the scrollbar. If not
     * set the default value is false.
     *
     * @param hideResizeGrips hideResizeGrips option
     * @return scrollbar
     */
    @StudioProperty(defaultValue = "false")
    public Scrollbar setHideResizeGrips(Boolean hideResizeGrips) {
        this.hideResizeGrips = hideResizeGrips;
        return this;
    }

    /**
     * @return maximum value of {@link ValueAxis} of scrollbar
     */
    public Double getMaximum() {
        return maximum;
    }

    /**
     * Sets maximum value of {@link ValueAxis} of scrollbar. Calculated automatically, if not set.
     *
     * @param maximum maximum value
     * @return scrollbar
     */
    @StudioProperty
    public Scrollbar setMaximum(Double maximum) {
        this.maximum = maximum;
        return this;
    }

    /**
     * @return minimum value of {@link ValueAxis} of scrollbar
     */
    public Double getMinimum() {
        return minimum;
    }

    /**
     * Sets minimum value of {@link ValueAxis} of scrollbar. Calculated automatically, if not set.
     *
     * @param minimum minimum value
     * @return scrollbar
     */
    @StudioProperty
    public Scrollbar setMinimum(Double minimum) {
        this.minimum = minimum;
        return this;
    }

    /**
     * @return true if this property is enabled
     */
    public Boolean getResizeEnabled() {
        return resizeEnabled;
    }

    /**
     * Set resizeEnabled to false if you want prevent the chart scroll bar to change selection scope. The grip
     * images will not be shown as well. The user would still be able to pan / move selection. If not set the default
     * value is true. If not set the default value is true.
     *
     * @param resizeEnabled resizeEnabled option
     * @return scrollbar
     */
    @StudioProperty(defaultValue = "true")
    public Scrollbar setResizeEnabled(Boolean resizeEnabled) {
        this.resizeEnabled = resizeEnabled;
        return this;
    }

    /**
     * @return scrollbar height
     */
    public Integer getScrollbarHeight() {
        return scrollbarHeight;
    }

    /**
     * Sets height (width, if chart is rotated) of a scrollbar. If not set the default value is 20.
     *
     * @param scrollbarHeight scrollbar height
     * @return scrollbar
     */
    @StudioProperty(defaultValue = "20")
    public Scrollbar setScrollbarHeight(Integer scrollbarHeight) {
        this.scrollbarHeight = scrollbarHeight;
        return this;
    }

    /**
     * @return scroll duration, in seconds
     */
    public Double getScrollDuration() {
        return scrollDuration;
    }

    /**
     * Sets duration of scrolling, when the user clicks on scrollbar's background, in seconds. Note,
     * updateOnReleaseOnly should be set to false in order animation to happen. If not set the default value is 1.
     *
     * @param scrollDuration scroll duration, in seconds
     * @return scrollbar
     */
    @StudioProperty(defaultValue = "1")
    public Scrollbar setScrollDuration(Double scrollDuration) {
        this.scrollDuration = scrollDuration;
        return this;
    }

    /**
     * @return selected background opacity
     */
    public Double getSelectedBackgroundAlpha() {
        return selectedBackgroundAlpha;
    }

    /**
     * Sets selected background opacity. If not set the default value is 1.
     *
     * @param selectedBackgroundAlpha opacity
     * @return scrollbar
     */
    @StudioProperty(defaultValue = "1")
    @Max(1)
    @Min(0)
    public Scrollbar setSelectedBackgroundAlpha(Double selectedBackgroundAlpha) {
        this.selectedBackgroundAlpha = selectedBackgroundAlpha;
        return this;
    }

    /**
     * @return selected background color
     */
    public Color getSelectedBackgroundColor() {
        return selectedBackgroundColor;
    }

    /**
     * Sets selected background color. If not set the default value is #EFEFEF.
     *
     * @param selectedBackgroundColor color
     * @return scrollbar
     */
    @StudioProperty(type = PropertyType.OPTIONS, defaultValue = "#EFEFEF")
    public Scrollbar setSelectedBackgroundColor(Color selectedBackgroundColor) {
        this.selectedBackgroundColor = selectedBackgroundColor;
        return this;
    }

    /**
     * @return selected graph's fill opacity
     */
    public Double getSelectedGraphFillAlpha() {
        return selectedGraphFillAlpha;
    }

    /**
     * Sets selected graph's fill opacity. Value range is 0 - 1. If not set the default value is 1.
     *
     * @param selectedGraphFillAlpha opacity
     * @return scrollbar
     */
    @StudioProperty(defaultValue = "1")
    @Max(1)
    @Min(0)
    public Scrollbar setSelectedGraphFillAlpha(Double selectedGraphFillAlpha) {
        this.selectedGraphFillAlpha = selectedGraphFillAlpha;
        return this;
    }

    /**
     * @return selected graph's fill color.
     */
    public Color getSelectedGraphFillColor() {
        return selectedGraphFillColor;
    }

    /**
     * Sets selected graph's fill color. If not set the default value is #888888.
     *
     * @param selectedGraphFillColor color.
     * @return scrollbar
     */
    @StudioProperty(type = PropertyType.OPTIONS, defaultValue = "#888888")
    public Scrollbar setSelectedGraphFillColor(Color selectedGraphFillColor) {
        this.selectedGraphFillColor = selectedGraphFillColor;
        return this;
    }

    /**
     * @return selected graph's line opacity
     */
    public Double getSelectedGraphLineAlpha() {
        return selectedGraphLineAlpha;
    }

    /**
     * Sets selected graph's line opacity. Value range is 0 - 1. If not set the default value is 0.
     *
     * @param selectedGraphLineAlpha opacity
     * @return scrollbar
     */
    @StudioProperty(defaultValue = "0")
    @Max(1)
    @Min(0)
    public Scrollbar setSelectedGraphLineAlpha(Double selectedGraphLineAlpha) {
        this.selectedGraphLineAlpha = selectedGraphLineAlpha;
        return this;
    }

    /**
     * @return selected graph's line color
     */
    public Color getSelectedGraphLineColor() {
        return selectedGraphLineColor;
    }

    /**
     * Sets selected graph's line color. If not set the default value is #888888.
     *
     * @param selectedGraphLineColor color
     * @return scrollbar
     */
    @StudioProperty(type = PropertyType.OPTIONS, defaultValue = "#888888")
    public Scrollbar setSelectedGraphLineColor(Color selectedGraphLineColor) {
        this.selectedGraphLineColor = selectedGraphLineColor;
        return this;
    }

    /**
     * @return true if the chart is updated only at the moment when user releases mouse button
     */
    public Boolean getUpdateOnReleaseOnly() {
        return updateOnReleaseOnly;
    }

    /**
     * Set updateOnReleaseOnly to true if the chart should be updated only at the moment when user releases mouse
     * button. If not set the default value is false.
     *
     * @param updateOnReleaseOnly updateOnReleaseOnly option
     * @return scrollbar
     */
    @StudioProperty(defaultValue = "false")
    public Scrollbar setUpdateOnReleaseOnly(Boolean updateOnReleaseOnly) {
        this.updateOnReleaseOnly = updateOnReleaseOnly;
        return this;
    }

    /**
     * @return graph id
     */
    public String getGraph() {
        return graph;
    }

    /**
     * Sets the graph which will be displayed in the scrollbar. Only serial chart's category scrollbar can display a
     * graph.
     *
     * @param graph graph id
     * @return scrollbar
     */
    public Scrollbar setGraph(String graph) {
        this.graph = graph;
        return this;
    }

    /**
     * @return true if scrollbar is enabled
     */
    public Boolean getEnabled() {
        return enabled;
    }

    /**
     * Set enabled to false if you don't want scrollbar should be enabled. You can hide/show scrollbar using this
     * property without actually removing it.
     *
     * @param enabled enabled option
     * @return scrollbar
     */
    @StudioProperty
    public Scrollbar setEnabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * @return distance from plot area to scrollbar, in pixels
     */
    public Integer getOffset() {
        return offset;
    }

    /**
     * Sets distance from plot area to scrollbar, in pixels. If not set the default value is 0.
     *
     * @param offset distance, in pixels
     * @return scrollbar
     */
    @StudioProperty(defaultValue = "0")
    public Scrollbar setOffset(Integer offset) {
        this.offset = offset;
        return this;
    }

    /**
     * @return icon file name
     */
    public String getDragIcon() {
        return dragIcon;
    }

    /**
     * Sets file name of scrollbar drag (resize grip) icon. You can find a set of icons in amcharts/images folder -
     * you can choose from these: dragIconRectBig, dragIconRectBigBlack, dragIconRectSmall, dragIconRectSmallBlack,
     * dragIconRoundBig, dragIconRoundBigBlack, dragIconRoundSmall, dragIconRoundSmallBlack. You can also use your
     * own custom icons. Don't forget to change dragIconWidth and dragIconHeight if you change icons. If not set the
     * default value is "dragIconRoundBig".
     *
     * @param dragIcon icon file name
     * @return scrollbar
     */
    @StudioProperty(type = PropertyType.OPTIONS, options = {
            "dragIconRectBig", "dragIconRectBigBlack", "dragIconRectSmall", "dragIconRectSmallBlack",
            "dragIconRoundBig", "dragIconRoundBigBlack", "dragIconRoundSmall", "dragIconRoundSmallBlack"
    }, defaultValue = "dragIconRoundBig")
    public Scrollbar setDragIcon(String dragIcon) {
        this.dragIcon = dragIcon;
        return this;
    }

    /**
     * @return true if custom colors is used
     */
    public Boolean getIgnoreCustomColors() {
        return ignoreCustomColors;
    }

    /**
     * Set ignoreCustomColors to true to use scrollbar's graph colors. If you have column type graph in your
     * scrollbar, and this graph has custom colors for one or more columns in data provider, those columns will be
     * colored with this custom color. If not set the default value is false.
     *
     * @param ignoreCustomColors ignoreCustomColors option
     * @return scrollbar
     */
    @StudioProperty(defaultValue = "false")
    public Scrollbar setIgnoreCustomColors(Boolean ignoreCustomColors) {
        this.ignoreCustomColors = ignoreCustomColors;
        return this;
    }

    /**
     * @return true if oppositeAxis is enabled
     */
    public Boolean getOppositeAxis() {
        return oppositeAxis;
    }

    /**
     * Set oppositeAxis to false and scrollbar will be placed next to category/value axis. However it won't adjust
     * its position regarding axis labels, so you might need to use offset property to move scrollbar away from
     * labels. By default, scrollbar is in the opposite side of plot area from the axis. If not set the default value
     * is true.
     *
     * @param oppositeAxis oppositeAxis option
     * @return scrollbar
     */
    @StudioProperty(defaultValue = "true")
    public Scrollbar setOppositeAxis(Boolean oppositeAxis) {
        this.oppositeAxis = oppositeAxis;
        return this;
    }

    /**
     * @return drag cursor down
     */
    public String getDragCursorDown() {
        return dragCursorDown;
    }

    /**
     * Sets drag cursor. Mouse cursor displayed when clicked on selected part of a scrollbar. If not set the default
     * value is "cursor: move; cursor: grab; cursor: -moz-grabbing; cursor: -webkit-grabbing;"
     *
     * @param dragCursorDown drag cursor down string
     * @return scrollbar
     */
    @StudioProperty(defaultValue = "cursor: move; cursor: grab; cursor: -moz-grabbing; cursor: -webkit-grabbing;")
    public Scrollbar setDragCursorDown(String dragCursorDown) {
        this.dragCursorDown = dragCursorDown;
        return this;
    }

    /**
     * @return drag cursor hover
     */
    public String getDragCursorHover() {
        return dragCursorHover;
    }

    /**
     * Sets drag cursor hover. Mouse cursor displayed when hovering over selected part of a scrollbar. If not set the
     * default value is "cursor: move; cursor: grab; cursor: -moz-grab; cursor: -webkit-grab;".
     *
     * @param dragCursorHover drag cursor hover string
     * @return scrollbar
     */
    @StudioProperty(defaultValue = "cursor: move; cursor: grab; cursor: -moz-grab; cursor: -webkit-grab;")
    public Scrollbar setDragCursorHover(String dragCursorHover) {
        this.dragCursorHover = dragCursorHover;
        return this;
    }

    /**
     * @return tab index
     */
    public Integer getTabIndex() {
        return tabIndex;
    }

    /**
     * Sets tab index to scrollbar. In case you set it to some number, the chart will set focus on grips and draggable
     * area of the scrollbar when user clicks tab key. When a focus is set, screen readers like NVDA Screen reader
     * will read label which is set using accessibleLabel property of ChartScrollbar. When a focus is set user can
     * zoom-in, zoom-out or pan the scrollbar using cursor keys. Note, not all browsers and readers support this.
     *
     * @param tabIndex tab index
     * @return scrollbar
     */
    @StudioProperty
    public Scrollbar setTabIndex(Integer tabIndex) {
        this.tabIndex = tabIndex;
        return this;
    }

    /**
     * @return accessible label string
     */
    public String getAccessibleLabel() {
        return accessibleLabel;
    }

    /**
     * Sets text which screen readers will read if user rolls-over or sets focus using tab key (this is possible only
     * if {@link AbstractGraph#tabIndex} is set to some number) on the grips or draggable part of a scrollbar. Text is
     * added as aria-label tag. Not all screen readers and browsers support this. Note, you should set tabIndex to
     * some number in order it would be possible to zoom chart using cursor keys. If not set the default value is
     * "Zoom chart using cursor arrows".
     *
     * @param accessibleLabel accessible label string
     * @return scrollbar
     */
    @StudioProperty(defaultValue = "Zoom chart using cursor arrows")
    public Scrollbar setAccessibleLabel(String accessibleLabel) {
        this.accessibleLabel = accessibleLabel;
        return this;
    }

    /**
     * @return mouse cursor type when user hovers horizontal cursor's resize grips
     */
    public String getHResizeCursor() {
        return hResizeCursor;
    }

    /**
     * Sets mouse cursor type shown when user hovers horizontal cursor's resize grips. Possible values are:
     * "e-resize", "n-resize", etc. If not set the default value is "ew-resize".
     *
     * @param hResizeCursor hResizeCursor value
     */
    @StudioProperty(defaultValue = "ew-resize")
    public void setHResizeCursor(String hResizeCursor) {
        this.hResizeCursor = hResizeCursor;
    }

    /**
     * @return CSS value of cursor when mouse is pressed down over horizontal cursor's resize grip
     */
    public String getHResizeCursorDown() {
        return hResizeCursorDown;
    }

    /**
     * Sets CSS value of cursor displayed when mouse is pressed down over horizontal cursor's resize grip. For instance,
     * "cursor: all-scroll;".
     *
     * @param hResizeCursorDown CSS value
     */
    @StudioProperty
    public void setHResizeCursorDown(String hResizeCursorDown) {
        this.hResizeCursorDown = hResizeCursorDown;
    }

    /**
     * @return CSS value of cursor when hovering over horizontal cursor's resize grip
     */
    public String getHResizeCursorHover() {
        return hResizeCursorHover;
    }

    /**
     * Sets CSS value of cursor displayed when hovering over horizontal cursor's resize grip.
     *
     * @param hResizeCursorHover CSS value
     */
    @StudioProperty
    public void setHResizeCursorHover(String hResizeCursorHover) {
        this.hResizeCursorHover = hResizeCursorHover;
    }

    /**
     * @return mouse cursor type when user hovers vertical cursor's resize grips
     */
    public String getVResizeCursor() {
        return vResizeCursor;
    }

    /**
     * Sets mouse cursor type shown when user hovers vertical cursor's resize grips. Possible values are:
     * "e-resize", "n-resize", etc. If not set the default value is "ns-resize".
     *
     * @param vResizeCursor vResizeCursor value
     */
    @StudioProperty(defaultValue = "ns-resize")
    public void setVResizeCursor(String vResizeCursor) {
        this.vResizeCursor = vResizeCursor;
    }

    /**
     * @return CSS value of cursor when mouse is pressed down over vertical cursor's resize grip
     */
    public String getVResizeCursorDown() {
        return vResizeCursorDown;
    }

    /**
     * Sets CSS value of cursor displayed when mouse is pressed down over vertical cursor's resize grip. For instance,
     * "cursor: all-scroll;".
     *
     * @param vResizeCursorDown CSS value
     */
    @StudioProperty
    public void setVResizeCursorDown(String vResizeCursorDown) {
        this.vResizeCursorDown = vResizeCursorDown;
    }

    /**
     * @return CSS value of cursor when hovering over vertical cursor's resize grip
     */
    public String getVResizeCursorHover() {
        return vResizeCursorHover;
    }

    /**
     * Sets CSS value of cursor displayed when hovering over vertical cursor's resize grip.
     *
     * @param vResizeCursorHover CSS value
     */
    @StudioProperty
    public void setVResizeCursorHover(String vResizeCursorHover) {
        this.vResizeCursorHover = vResizeCursorHover;
    }
}