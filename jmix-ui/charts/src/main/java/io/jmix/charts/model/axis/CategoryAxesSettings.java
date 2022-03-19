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

package io.jmix.charts.model.axis;


import io.jmix.charts.model.AbstractChartObject;
import io.jmix.charts.model.Color;
import io.jmix.charts.model.chart.impl.StockPanel;
import io.jmix.charts.model.date.DateFormat;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioElement;
import io.jmix.ui.meta.StudioElementsGroup;
import io.jmix.ui.meta.StudioProperty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Defines set of properties for all CategoryAxes of {@link StockPanel}. If there is no default value specified,
 * default value of {@link CategoryAxis} class will be used.
 * <br>
 * See documentation for properties of CategoryAxesSettings JS object. <br>
 *
 * <a href="http://docs.amcharts.com/3/javascriptstockchart/CategoryAxesSettings">http://docs.amcharts.com/3/javascriptstockchart/CategoryAxesSettings</a>
 */
@StudioElement(
        caption = "CategoryAxesSettings",
        xmlElement = "categoryAxesSettings",
        xmlns = "http://jmix.io/schema/ui/charts",
        xmlnsAlias = "chart")
public class CategoryAxesSettings extends AbstractChartObject {

    private static final long serialVersionUID = -4456035547141357578L;

    private Boolean alwaysGroup;

    private Boolean autoGridCount;

    private Double axisAlpha;

    private Color axisColor;

    private Integer axisHeight;

    private Integer axisThickness;

    private Boolean boldLabels;

    private Boolean boldPeriodBeginning;

    private Color color;

    private Integer dashLength;

    private List<DateFormat> dateFormats;

    private Boolean equalSpacing;

    private Double fillAlpha;

    private Color fillColor;

    private Integer fontSize;

    private Double gridAlpha;

    private Color gridColor;

    private Integer gridCount;

    private Integer gridThickness;

    private List<String> groupToPeriods;

    private Boolean inside;

    private Integer labelOffset;

    private Integer labelRotation;

    private Boolean labelsEnabled;

    private Boolean markPeriodChange;

    private Integer maxSeries;

    private Integer minHorizontalGap;

    private Double minorGridAlpha;

    private Boolean minorGridEnabled;

    private String minPeriod;

    private CategoryAxesPosition position;

    private Boolean startOnAxis;

    private Integer tickLength;

    private Boolean twoLineMode;

    /**
     * @return true if chart always group values to the first period available
     */
    public Boolean getAlwaysGroup() {
        return alwaysGroup;
    }

    /**
     * Set alwaysGroup to true and if groupToPeriods doesn't have minPeriod value included, chart will always group
     * values to the first period available. If not set the default value is false.
     *
     * @param alwaysGroup alwaysGroup option
     * @return category axes settings
     */
    @StudioProperty(defaultValue = "false")
    public CategoryAxesSettings setAlwaysGroup(Boolean alwaysGroup) {
        this.alwaysGroup = alwaysGroup;
        return this;
    }

    /**
     * @return true if the number of gridCount is specified automatically, according to the axis size.
     */
    public Boolean getAutoGridCount() {
        return autoGridCount;
    }

    /**
     * Set autoGridCount to false if you don't want the number of gridCount are specified automatically,
     * according to the axis size. If not set the default value is true.
     *
     * @param autoGridCount autoGridCount option
     * @return category axes settings
     */
    @StudioProperty(defaultValue = "true")
    public CategoryAxesSettings setAutoGridCount(Boolean autoGridCount) {
        this.autoGridCount = autoGridCount;
        return this;
    }

    /**
     * @return axis opacity
     */
    public Double getAxisAlpha() {
        return axisAlpha;
    }

    /**
     * Sets axis opacity. If not set the default value is 0.
     *
     * @param axisAlpha axis opacity
     * @return category axes settings
     */
    @StudioProperty(defaultValue = "0")
    @Max(1)
    @Min(0)
    public CategoryAxesSettings setAxisAlpha(Double axisAlpha) {
        this.axisAlpha = axisAlpha;
        return this;
    }

    /**
     * @return axis color
     */
    public Color getAxisColor() {
        return axisColor;
    }

    /**
     * Sets axis color.
     *
     * @param axisColor axis color
     * @return category axes settings
     */
    @StudioProperty(type = PropertyType.OPTIONS)
    public CategoryAxesSettings setAxisColor(Color axisColor) {
        this.axisColor = axisColor;
        return this;
    }

    /**
     * @return height of category axes
     */
    public Integer getAxisHeight() {
        return axisHeight;
    }

    /**
     * Sets height of category axes. Set it to 0 if you set inside property to true. If not set the default value is 28.
     *
     * @param axisHeight height of category axes
     * @return category axes settings
     */
    @StudioProperty(defaultValue = "28")
    public CategoryAxesSettings setAxisHeight(Integer axisHeight) {
        this.axisHeight = axisHeight;
        return this;
    }

    /**
     * @return thickness of the axis
     */
    public Integer getAxisThickness() {
        return axisThickness;
    }

    /**
     * Sets thickness of the axis.
     *
     * @param axisThickness thickness of the axis
     * @return category axes settings
     */
    @StudioProperty
    public CategoryAxesSettings setAxisThickness(Integer axisThickness) {
        this.axisThickness = axisThickness;
        return this;
    }

    /**
     * @return true if chart highlights the beginning of the periods in bold
     */
    public Boolean getBoldPeriodBeginning() {
        return boldPeriodBeginning;
    }

    /**
     * Set boldPeriodBeginning to false if you want chart will not try to highlight the beginning of the periods,
     * like month, in bold. Works when parse dates are on for the category axis. If not set the default value is true.
     *
     * @param boldPeriodBeginning bold period beginning option
     * @return category axes settings
     */
    @StudioProperty(defaultValue = "true")
    public CategoryAxesSettings setBoldPeriodBeginning(Boolean boldPeriodBeginning) {
        this.boldPeriodBeginning = boldPeriodBeginning;
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
     * @return category axes settings
     */
    @StudioProperty(type = PropertyType.OPTIONS)
    public CategoryAxesSettings setColor(Color color) {
        this.color = color;
        return this;
    }

    /**
     * @return length of a dash
     */
    public Integer getDashLength() {
        return dashLength;
    }

    /**
     * Sets length of a dash.
     *
     * @param dashLength length of a dash
     * @return category axes settings
     */
    @StudioProperty
    public CategoryAxesSettings setDashLength(Integer dashLength) {
        this.dashLength = dashLength;
        return this;
    }

    /**
     * @return list of date formats of different periods
     */
    public List<DateFormat> getDateFormats() {
        return dateFormats;
    }

    /**
     * Sets list of date formats of different periods. Possible period values: fff - milliseconds, ss - seconds, mm -
     * minutes, hh - hours, DD - days, MM - months, WW - weeks, YYYY - years.
     *
     * @param dateFormats list of date formats
     * @return category axes settings
     */
    @StudioElementsGroup(caption = "Date Formats", xmlElement = "dateFormats")
    public CategoryAxesSettings setDateFormats(List<DateFormat> dateFormats) {
        this.dateFormats = dateFormats;
        return this;
    }

    /**
     * Adds date formats.
     *
     * @param dateFormats date formats
     * @return category axes settings
     */
    public CategoryAxesSettings addDateFormats(DateFormat... dateFormats) {
        if (dateFormats != null) {
            if (this.dateFormats == null) {
                this.dateFormats = new ArrayList<>();
            }
            this.dateFormats.addAll(Arrays.asList(dateFormats));
        }
        return this;
    }

    /**
     * @return true if data points is placed at equal intervals (omitting dates with no data)
     */
    public Boolean getEqualSpacing() {
        return equalSpacing;
    }

    /**
     * Set equalSpacing to true if you want data points to be placed at equal intervals (omitting dates with no data).
     * If not set the default value is false.
     *
     * @param equalSpacing equalSpacing option
     * @return category axes settings
     */
    @StudioProperty(defaultValue = "false")
    public CategoryAxesSettings setEqualSpacing(Boolean equalSpacing) {
        this.equalSpacing = equalSpacing;
        return this;
    }

    /**
     * @return fill opacity
     */
    public Double getFillAlpha() {
        return fillAlpha;
    }

    /**
     * Sets fill opacity. Every second space between grid lines can be filled with fillColor.
     *
     * @param fillAlpha fill opacity
     * @return category axes settings
     */
    @StudioProperty
    @Max(1)
    @Min(0)
    public CategoryAxesSettings setFillAlpha(Double fillAlpha) {
        this.fillAlpha = fillAlpha;
        return this;
    }

    /**
     * @return fill color
     */
    public Color getFillColor() {
        return fillColor;
    }

    /**
     * Sets fill color. Every second space between grid lines can be filled with color. Set fillAlpha to a value
     * greater than 0 to see the fills.
     *
     * @param fillColor fill color
     * @return category axes settings
     */
    @StudioProperty(type = PropertyType.OPTIONS)
    public CategoryAxesSettings setFillColor(Color fillColor) {
        this.fillColor = fillColor;
        return this;
    }

    /**
     * @return font size
     */
    public Integer getFontSize() {
        return fontSize;
    }

    /**
     * Sets text size.
     *
     * @param fontSize text size
     * @return category axes settings
     */
    @StudioProperty
    public CategoryAxesSettings setFontSize(Integer fontSize) {
        this.fontSize = fontSize;
        return this;
    }

    /**
     * @return opacity of grid lines
     */
    public Double getGridAlpha() {
        return gridAlpha;
    }

    /**
     * Sets opacity of grid lines.
     *
     * @param gridAlpha opacity of grid lines
     * @return category axes settings
     */
    @StudioProperty
    @Max(1)
    @Min(0)
    public CategoryAxesSettings setGridAlpha(Double gridAlpha) {
        this.gridAlpha = gridAlpha;
        return this;
    }

    /**
     * @return color of grid lines
     */
    public Color getGridColor() {
        return gridColor;
    }

    /**
     * Sets color of grid lines.
     *
     * @param gridColor color of grid lines
     * @return category axes settings
     */
    @StudioProperty(type = PropertyType.OPTIONS)
    public CategoryAxesSettings setGridColor(Color gridColor) {
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
     * Sets number of grid lines. You should set autoGridCount to false in order this property not to be ignored. If
     * not set the default value is 10.
     *
     * @param gridCount number of grid lines
     * @return category axes settings
     */
    @StudioProperty(defaultValue = "10")
    public CategoryAxesSettings setGridCount(Integer gridCount) {
        this.gridCount = gridCount;
        return this;
    }

    /**
     * @return thickness of grid lines
     */
    public Integer getGridThickness() {
        return gridThickness;
    }

    /**
     * Sets thickness of grid lines.
     *
     * @param gridThickness grid thickness
     * @return category axes settings
     */
    @StudioProperty
    public CategoryAxesSettings setGridThickness(Integer gridThickness) {
        this.gridThickness = gridThickness;
        return this;
    }

    /**
     * @return list of groupToPeriods strings
     */
    public List<String> getGroupToPeriods() {
        return groupToPeriods;
    }

    /**
     * Sets list of groupToPeriods strings. Periods to which data will be grouped in case there are more data items in
     * the selected period than specified in maxSeries property. Possible values: "ss", "10ss", "mm", "DD", "WW","MM",
     * "YYYY". If not set the default value is
     *
     * <pre>{@code
     * ["ss", "10ss", "30ss",
     *  "mm", "10mm", "30mm",
     *  "hh", "DD",   "WW",
     *  "MM", "YYYY"]}
     * </pre>
     *
     * @param groupToPeriods list of groupToPeriods strings
     * @return category axes settings
     */
    @StudioProperty(type = PropertyType.STRING, defaultValue = "ss,10ss,30ss,mm,10mm,30mm,hh,DD,WW,MM,YYYYY")
    public CategoryAxesSettings setGroupToPeriods(List<String> groupToPeriods) {
        this.groupToPeriods = groupToPeriods;
        return this;
    }

    /**
     * Adds groupToPeriods strings.
     *
     * @param groupToPeriods groupToPeriods
     * @return category axes settings
     */
    public CategoryAxesSettings addGroupToPeriods(String... groupToPeriods) {
        if (groupToPeriods != null) {
            if (this.groupToPeriods == null) {
                this.groupToPeriods = new ArrayList<>();
            }
            this.groupToPeriods.addAll(Arrays.asList(groupToPeriods));
        }
        return this;
    }

    /**
     * @return true if values placed inside of plot area
     */
    public Boolean getInside() {
        return inside;
    }

    /**
     * Set inside to true if values should be placed inside of plot area. If not set the default value is false.
     *
     * @param inside inside option
     * @return category axes settings
     */
    @StudioProperty(defaultValue = "false")
    public CategoryAxesSettings setInside(Boolean inside) {
        this.inside = inside;
        return this;
    }

    /**
     * @return offset of axis labels
     */
    public Integer getLabelOffset() {
        return labelOffset;
    }

    /**
     * Sets offset of axis labels. If not set the default value is 0.
     *
     * @param labelOffset offset of axis labels
     * @return category axes settings
     */
    @StudioProperty(defaultValue = "0")
    public CategoryAxesSettings setLabelOffset(Integer labelOffset) {
        this.labelOffset = labelOffset;
        return this;
    }

    /**
     * @return rotation angle of a label
     */
    public Integer getLabelRotation() {
        return labelRotation;
    }

    /**
     * Sets rotation angle of a label.
     *
     * @param labelRotation rotation angle of a label
     * @return category axes settings
     */
    @StudioProperty
    public CategoryAxesSettings setLabelRotation(Integer labelRotation) {
        this.labelRotation = labelRotation;
        return this;
    }

    /**
     * @return true if axis displays category axis labels and value axis values.
     */
    public Boolean getLabelsEnabled() {
        return labelsEnabled;
    }

    /**
     * Set labelsEnabled to false if you don't wont to display category axis labels and value axis values. If not set
     * the default value is true.
     *
     * @param labelsEnabled labelsEnabled option
     * @return category axes settings
     */
    @StudioProperty(defaultValue = "true")
    public CategoryAxesSettings setLabelsEnabled(Boolean labelsEnabled) {
        this.labelsEnabled = labelsEnabled;
        return this;
    }

    /**
     * @return true if period should be marked by a different date format
     */
    public Boolean getMarkPeriodChange() {
        return markPeriodChange;
    }

    /**
     * Set markPeriodChange to false if period shouldn't be marked by a different date format. If not set the default
     * value is true.
     *
     * @param markPeriodChange markPeriodChange option
     * @return category axes settings
     */
    @StudioProperty(defaultValue = "true")
    public CategoryAxesSettings setMarkPeriodChange(Boolean markPeriodChange) {
        this.markPeriodChange = markPeriodChange;
        return this;
    }

    /**
     * @return maximum series shown at a time
     */
    public Integer getMaxSeries() {
        return maxSeries;
    }

    /**
     * Sets maximum series shown at a time. In case there are more data points in the selection than maxSeries, the
     * chart will group data to longer periods, for example - you have 250 days in the selection, and maxSeries is
     * 150 - the chart will group data to weeks. If not set the default value is 150.
     *
     * @param maxSeries maximum series shown at a time
     * @return category axes settings
     */
    @StudioProperty(defaultValue = "150")
    public CategoryAxesSettings setMaxSeries(Integer maxSeries) {
        this.maxSeries = maxSeries;
        return this;
    }

    /**
     * @return minimum cell width required for one span between grid lines
     */
    public Integer getMinHorizontalGap() {
        return minHorizontalGap;
    }

    /**
     * Sets minimum cell width required for one span between grid lines. minHorizontalGap is used when calculating
     * grid count. If not set the default value is 75.
     *
     * @param minHorizontalGap minimum cell width
     * @return category axes settings
     */
    @StudioProperty(defaultValue = "75")
    public CategoryAxesSettings setMinHorizontalGap(Integer minHorizontalGap) {
        this.minHorizontalGap = minHorizontalGap;
        return this;
    }

    /**
     * @return the shortest period of your data
     */
    public String getMinPeriod() {
        return minPeriod;
    }

    /**
     * Sets the shortest period of your data. "fff" - millisecond, "ss" - second, "mm" - minute, "hh" - hour, "DD" -
     * day, "MM" - month, 'YYYY' - year. It's also possible to supply a number for increments, i.e. "15mm" which will
     * instruct the chart that your data is supplied in 15 minute increments. If not set the default value is "DD".
     *
     * @param minPeriod the shortest period
     * @return category axes settings
     */
    @StudioProperty(type = PropertyType.OPTIONS, options = {"fff", "ss", "mm", "hh", "DD", "MM", "YYYY"},
            defaultValue = "DD")
    public CategoryAxesSettings setMinPeriod(String minPeriod) {
        this.minPeriod = minPeriod;
        return this;
    }

    /**
     * @return position of category axes
     */
    public CategoryAxesPosition getPosition() {
        return position;
    }

    /**
     * Sets position of category axes.
     *
     * @param position position of category axes
     * @return category axes settings
     */
    @StudioProperty(type = PropertyType.ENUMERATION)
    public CategoryAxesSettings setPosition(CategoryAxesPosition position) {
        this.position = position;
        return this;
    }

    /**
     * @return true if the graph shouldn't start on axis
     */
    public Boolean getStartOnAxis() {
        return startOnAxis;
    }

    /**
     * Set startOnAxis to false if the graph should start on axis. In case you display columns, it is recommended to set
     * this to false. startOnAxis can be set to true only if equalSpacing is set to true. If not set the default
     * value is false.
     *
     * @param startOnAxis startOnAxis option
     * @return category axes settings
     */
    @StudioProperty(defaultValue = "false")
    public CategoryAxesSettings setStartOnAxis(Boolean startOnAxis) {
        this.startOnAxis = startOnAxis;
        return this;
    }

    /**
     * @return tick length
     */
    public Integer getTickLength() {
        return tickLength;
    }

    /**
     * Sets tick length. If not set the default value is 0.
     *
     * @param tickLength tick length
     * @return category axes settings
     */
    @StudioProperty(defaultValue = "0")
    public CategoryAxesSettings setTickLength(Integer tickLength) {
        this.tickLength = tickLength;
        return this;
    }

    /**
     * @return true category axis displays date strings of bot small and big period, in two rows
     */
    public Boolean getTwoLineMode() {
        return twoLineMode;
    }

    /**
     * Set twoLineMode to true, category axis will display date strings of bot small and big period, in two rows, at
     * the position where bigger period changes. Works only when parseDates is set to true and equalSpacing is false.
     * If not set the default value is false.
     *
     * @param twoLineMode twoLineMode option
     * @return category axes settings
     */
    @StudioProperty(defaultValue = "false")
    public CategoryAxesSettings setTwoLineMode(Boolean twoLineMode) {
        this.twoLineMode = twoLineMode;
        return this;
    }

    /**
     * @return true if axis labels are bold
     */
    public Boolean getBoldLabels() {
        return boldLabels;
    }

    /**
     * Set to true if axis labels should be bold.
     *
     * @param boldLabels boldLabels option
     * @return category axes settings
     */
    @StudioProperty
    public CategoryAxesSettings setBoldLabels(Boolean boldLabels) {
        this.boldLabels = boldLabels;
        return this;
    }

    /**
     * @return opacity of minor grid
     */
    public Double getMinorGridAlpha() {
        return minorGridAlpha;
    }

    /**
     * Sets opacity of minor grid. In order minor to be visible, you should set minorGridEnabled to true.
     *
     * @param minorGridAlpha opacity of minor grid
     * @return category axes settings
     */
    @StudioProperty
    @Max(1)
    @Min(0)
    public CategoryAxesSettings setMinorGridAlpha(Double minorGridAlpha) {
        this.minorGridAlpha = minorGridAlpha;
        return this;
    }

    /**
     * @return true if minor grid is displayed
     */
    public Boolean getMinorGridEnabled() {
        return minorGridEnabled;
    }

    /**
     * Set to true if minor grid should be displayed. Note, if equalSpacing is set to true, this setting will be
     * ignored.
     *
     * @param minorGridEnabled minorGridEnabled option
     * @return category axes settings
     */
    @StudioProperty
    public CategoryAxesSettings setMinorGridEnabled(Boolean minorGridEnabled) {
        this.minorGridEnabled = minorGridEnabled;
        return this;
    }
}
