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

package io.jmix.charts.model.settings;

import io.jmix.charts.model.AbstractChartObject;
import io.jmix.charts.model.Color;
import io.jmix.charts.model.Scrollbar;
import io.jmix.charts.model.axis.CategoryAxesSettings;
import io.jmix.charts.model.graph.GraphType;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioElement;
import io.jmix.ui.meta.StudioProperty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * Defines set of properties for chart scrollbar. If there is no default value specified, default value of
 * {@link Scrollbar} class will be used.
 * <br>
 * See documentation for properties of ChartScrollbarSettings JS object. <br>
 *
 * <a href="http://docs.amcharts.com/3/javascriptstockchart/ChartScrollbarSettings">http://docs.amcharts.com/3/javascriptstockchart/ChartScrollbarSettings</a>
 */
@StudioElement(
        caption = "ChartScrollbarSettings",
        xmlElement = "chartScrollbarSettings",
        xmlns = "http://jmix.io/schema/ui/charts",
        xmlnsAlias = "chart")
public class ChartScrollbarSettings extends AbstractChartObject {

    private static final long serialVersionUID = -6544903589207480657L;

    private Boolean autoGridCount;

    private Double backgroundAlpha;

    private Color backgroundColor;

    private Color color;

    private Integer dragIconHeight;

    private Integer dragIconWidth;

    private Boolean enabled;

    private Integer fontSize;

    private String graph;

    private Double graphFillAlpha;

    private Color graphFillColor;

    private Double graphLineAlpha;

    private Color graphLineColor;

    private GraphType graphType;

    private Double gridAlpha;

    private Color gridColor;

    private Integer gridCount;

    private Integer height;

    private Boolean hideResizeGrips;

    private Boolean markPeriodChange;

    private ChartScrollbarPosition position;

    private Boolean resizeEnabled;

    private Double scrollDuration;

    private Double selectedBackgroundAlpha;

    private Color selectedBackgroundColor;

    private Double selectedGraphFillAlpha;

    private Color selectedGraphFillColor;

    private Double selectedGraphLineAlpha;

    private Color selectedGraphLineColor;

    private Boolean updateOnReleaseOnly;

    private String usePeriod;

    /**
     * @return true if number of gridCount is specified automatically, according to the axis size
     */
    public Boolean getAutoGridCount() {
        return autoGridCount;
    }

    /**
     * Set autoGridCount to false if you don't want that the number of gridCount will be specified
     * automatically, according to the axis size. If not set the default value is true.
     *
     * @param autoGridCount autoGridCount option
     * @return chart scrollbar settings
     */
    @StudioProperty(defaultValue = "true")
    public ChartScrollbarSettings setAutoGridCount(Boolean autoGridCount) {
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
     * Sets background opacity.
     *
     * @param backgroundAlpha background opacity
     * @return chart scrollbar settings
     */
    @StudioProperty
    @Max(1)
    @Min(0)
    public ChartScrollbarSettings setBackgroundAlpha(Double backgroundAlpha) {
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
     * Sets background color of the scrollbar.
     *
     * @param backgroundColor background color
     * @return chart scrollbar settings
     */
    @StudioProperty(type = PropertyType.OPTIONS)
    public ChartScrollbarSettings setBackgroundColor(Color backgroundColor) {
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
     * Sets text color.
     *
     * @param color text color
     * @return chart scrollbar settings
     */
    @StudioProperty(type = PropertyType.OPTIONS)
    public ChartScrollbarSettings setColor(Color color) {
        this.color = color;
        return this;
    }

    /**
     * @return height of resize grip image
     */
    public Integer getDragIconHeight() {
        return dragIconHeight;
    }

    /**
     * Sets height of resize grip image. Note, you should also update the image in amcharts/images folder if you don't
     * want it to be distorted because of resizing. If not set the default value is 18.
     *
     * @param dragIconHeight icon height
     * @return chart scrollbar settings
     */
    @StudioProperty(defaultValue = "18")
    public ChartScrollbarSettings setDragIconHeight(Integer dragIconHeight) {
        this.dragIconHeight = dragIconHeight;
        return this;
    }

    /**
     * @return width of resize grip image
     */
    public Integer getDragIconWidth() {
        return dragIconWidth;
    }

    /**
     * Sets width of resize grip image. Note, you should also update the image in amcharts/images folder if you don't
     * want it to be distorted because of resizing. If not set the default value is 11.
     *
     * @param dragIconWidth icon width
     * @return chart scrollbar settings
     */
    @StudioProperty(defaultValue = "11")
    public ChartScrollbarSettings setDragIconWidth(Integer dragIconWidth) {
        this.dragIconWidth = dragIconWidth;
        return this;
    }

    /**
     * @return enabled option
     */
    public Boolean getEnabled() {
        return enabled;
    }

    /**
     * Set enabled false if you don't need scrollbar. If not set the default value is true.
     *
     * @param enabled enabled option
     * @return chart scrollbar settings
     */
    @StudioProperty(defaultValue = "true")
    public ChartScrollbarSettings setEnabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * @return font size
     */
    public Integer getFontSize() {
        return fontSize;
    }

    /**
     * Sets font size
     *
     * @param fontSize font size
     * @return chart scrollbar settings
     */
    @StudioProperty
    public ChartScrollbarSettings setFontSize(Integer fontSize) {
        this.fontSize = fontSize;
        return this;
    }

    /**
     * @return graph id
     */
    public String getGraph() {
        return graph;
    }

    /**
     * Sets graph which will be displayed in the scrollbar.
     *
     * @param graph graph id
     * @return chart scrollbar settings
     */
    @StudioProperty
    public ChartScrollbarSettings setGraph(String graph) {
        this.graph = graph;
        return this;
    }

    /**
     * @return graph fill opacity
     */
    public Double getGraphFillAlpha() {
        return graphFillAlpha;
    }

    /**
     * Sets graph fill opacity.
     *
     * @param graphFillAlpha graph fill opacity
     * @return chart scrollbar settings
     */
    @StudioProperty
    @Max(1)
    @Min(0)
    public ChartScrollbarSettings setGraphFillAlpha(Double graphFillAlpha) {
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
     * Sets graph fill color.
     *
     * @param graphFillColor graph fill color
     * @return chart scrollbar settings
     */
    @StudioProperty(type = PropertyType.OPTIONS)
    public ChartScrollbarSettings setGraphFillColor(Color graphFillColor) {
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
     * Sets graph line opacity.
     *
     * @param graphLineAlpha graph line opacity
     * @return chart scrollbar settings
     */
    @StudioProperty
    @Max(1)
    @Min(0)
    public ChartScrollbarSettings setGraphLineAlpha(Double graphLineAlpha) {
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
     * Sets graph line color.
     *
     * @param graphLineColor graph line color
     * @return chart scrollbar settings
     */
    @StudioProperty(type = PropertyType.OPTIONS)
    public ChartScrollbarSettings setGraphLineColor(Color graphLineColor) {
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
     * Sets type of chart scrollbar's graph. By default the graph type is the same as the original graph's type,
     * however in case of candlestick or ohlc you might want to show line graph in the scrollbar. Possible values
     * are: line, column, step, smoothedLine, candlestick, ohlc.
     *
     * @param graphType graph type
     * @return chart scrollbar settings
     */
    @StudioProperty(type = PropertyType.ENUMERATION)
    public ChartScrollbarSettings setGraphType(GraphType graphType) {
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
     * Sets grid opacity.
     *
     * @param gridAlpha grid opacity
     * @return chart scrollbar settings
     */
    @StudioProperty
    @Max(1)
    @Min(0)
    public ChartScrollbarSettings setGridAlpha(Double gridAlpha) {
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
     * Sets grid color.
     *
     * @param gridColor grid color
     * @return chart scrollbar settings
     */
    @StudioProperty(type = PropertyType.OPTIONS)
    public ChartScrollbarSettings setGridColor(Color gridColor) {
        this.gridColor = gridColor;
        return this;
    }

    /**
     * @return grid count
     */
    public Integer getGridCount() {
        return gridCount;
    }

    /**
     * Sets grid count. You should set autoGridCount to false in order this property to work.
     *
     * @param gridCount grid count
     * @return chart scrollbar settings
     */
    @StudioProperty
    public ChartScrollbarSettings setGridCount(Integer gridCount) {
        this.gridCount = gridCount;
        return this;
    }

    /**
     * @return height of scrollbar, in pixels.
     */
    public Integer getHeight() {
        return height;
    }

    /**
     * Sets height of scrollbar, in pixels. If not set the default value is 40.
     *
     * @param height height, in pixels
     * @return chart scrollbar settings
     */
    @StudioProperty(defaultValue = "40")
    public ChartScrollbarSettings setHeight(Integer height) {
        this.height = height;
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
     * @return chart scrollbar settings
     */
    @StudioProperty(defaultValue = "false")
    public ChartScrollbarSettings setHideResizeGrips(Boolean hideResizeGrips) {
        this.hideResizeGrips = hideResizeGrips;
        return this;
    }

    /**
     * @return true if markPeriodChange is enabled
     */
    public Boolean getMarkPeriodChange() {
        return markPeriodChange;
    }

    /**
     * Specifies if category axis of scrollbar should mark period change with a different date format.
     *
     * @param markPeriodChange markPeriodChange option
     * @return chart scrollbar settings
     */
    @StudioProperty
    public ChartScrollbarSettings setMarkPeriodChange(Boolean markPeriodChange) {
        this.markPeriodChange = markPeriodChange;
        return this;
    }

    /**
     * @return position of a scrollbar
     */
    public ChartScrollbarPosition getPosition() {
        return position;
    }

    /**
     * Sets position of a scrollbar. Possible values are "top" and "bottom". If not set the default value is BOTTOM.
     *
     * @param position position of a scrollbar
     * @return chart scrollbar settings
     */
    @StudioProperty(type = PropertyType.ENUMERATION, defaultValue = "BOTTOM")
    public ChartScrollbarSettings setPosition(ChartScrollbarPosition position) {
        this.position = position;
        return this;
    }

    /**
     * @return true if resizing is enabled
     */
    public Boolean getResizeEnabled() {
        return resizeEnabled;
    }

    /**
     * Set resizeEnabled to false if you want to disable a resizing. If not set the default value is true.
     *
     * @param resizeEnabled resizeEnabled option
     * @return chart scrollbar settings
     */
    @StudioProperty(defaultValue = "true")
    public ChartScrollbarSettings setResizeEnabled(Boolean resizeEnabled) {
        this.resizeEnabled = resizeEnabled;
        return this;
    }

    /**
     * @return duration of scrolling, in seconds
     */
    public Double getScrollDuration() {
        return scrollDuration;
    }

    /**
     * Sets duration of scrolling, when the user clicks on scrollbar's background, in seconds. Note,
     * updateOnReleaseOnly should be set to false in order animation to happen. If not set the default value is true.
     *
     * @param scrollDuration duration of scrolling, in seconds
     * @return chart scrollbar settings
     */
    @StudioProperty(defaultValue = "true")
    public ChartScrollbarSettings setScrollDuration(Double scrollDuration) {
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
     * Sets selected background opacity.
     *
     * @param selectedBackgroundAlpha opacity
     * @return chart scrollbar settings
     */
    @StudioProperty
    @Max(1)
    @Min(0)
    public ChartScrollbarSettings setSelectedBackgroundAlpha(Double selectedBackgroundAlpha) {
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
     * Sets selected background color.
     *
     * @param selectedBackgroundColor color
     * @return chart scrollbar settings
     */
    @StudioProperty(type = PropertyType.OPTIONS)
    public ChartScrollbarSettings setSelectedBackgroundColor(Color selectedBackgroundColor) {
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
     * Sets selected graph's fill opacity.
     *
     * @param selectedGraphFillAlpha opacity
     * @return chart scrollbar settings
     */
    @StudioProperty
    @Max(1)
    @Min(0)
    public ChartScrollbarSettings setSelectedGraphFillAlpha(Double selectedGraphFillAlpha) {
        this.selectedGraphFillAlpha = selectedGraphFillAlpha;
        return this;
    }

    /**
     * @return selected graph's fill color
     */
    public Color getSelectedGraphFillColor() {
        return selectedGraphFillColor;
    }

    /**
     * Sets selected graph's fill color.
     *
     * @param selectedGraphFillColor color
     * @return chart scrollbar settings
     */
    @StudioProperty(type = PropertyType.OPTIONS)
    public ChartScrollbarSettings setSelectedGraphFillColor(Color selectedGraphFillColor) {
        this.selectedGraphFillColor = selectedGraphFillColor;
        return this;
    }

    /**
     * @return selected graph'sline opacity
     */
    public Double getSelectedGraphLineAlpha() {
        return selectedGraphLineAlpha;
    }

    /**
     * Sets selected graph's line opacity.
     *
     * @param selectedGraphLineAlpha opacity
     * @return chart scrollbar settings
     */
    @StudioProperty
    @Max(1)
    @Min(0)
    public ChartScrollbarSettings setSelectedGraphLineAlpha(Double selectedGraphLineAlpha) {
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
     * Sets selected graph's line color.
     *
     * @param selectedGraphLineColor color
     * @return chart scrollbar settings
     */
    @StudioProperty(type = PropertyType.OPTIONS)
    public ChartScrollbarSettings setSelectedGraphLineColor(Color selectedGraphLineColor) {
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
     * button. Useful when working with large data sets.
     *
     * @param updateOnReleaseOnly updateOnReleaseOnly option
     * @return chart scrollbar settings
     */
    @StudioProperty
    public ChartScrollbarSettings setUpdateOnReleaseOnly(Boolean updateOnReleaseOnly) {
        this.updateOnReleaseOnly = updateOnReleaseOnly;
        return this;
    }

    /**
     * @return used date period
     */
    public String getUsePeriod() {
        return usePeriod;
    }

    /**
     * Sets the date period for the scrollbar. This is very important feature for those, who work with large data sets.
     * You can tell scrollbar what period it should use for it's graph and save a lot of time for rendering of
     * this graph. For example, if your minPeriod is "DD" (days), set usePeriod = "WW" (weeks) and you will have 7
     * times less data points in scrollbar's graph. Note, the period you specify here should be set in
     * {@link CategoryAxesSettings#groupToPeriods}.
     *
     * @param usePeriod date period
     * @return chart scrollbar settings
     */
    @StudioProperty
    public ChartScrollbarSettings setUsePeriod(String usePeriod) {
        this.usePeriod = usePeriod;
        return this;
    }
}
