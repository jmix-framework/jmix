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


import io.jmix.charts.model.JsFunction;
import io.jmix.charts.model.chart.impl.AbstractChart;
import io.jmix.charts.model.date.DatePeriod;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioElement;
import io.jmix.ui.meta.StudioProperty;

/**
 * Defines category axis in chart. <br>
 *
 * See documentation for properties of CategoryAxis JS Object. <br>
 *
 * <a href="http://docs.amcharts.com/3/javascriptcharts/CategoryAxis">http://docs.amcharts.com/3/javascriptcharts/CategoryAxis</a>
 */
@StudioElement(
        caption = "CategoryAxis",
        xmlElement = "categoryAxis",
        xmlns = "http://jmix.io/schema/ui/charts",
        xmlnsAlias = "chart")
public class CategoryAxis extends AbstractAxis<CategoryAxis> {

    private static final long serialVersionUID = -8181114623535627249L;

    private Boolean autoWrap;

    private JsFunction categoryFunction;

    private String classNameField;

    private Boolean equalSpacing;

    private String forceShowField;

    private GridPosition gridPosition;

    private String labelColorField;

    private JsFunction labelFunction;

    private DatePeriod minPeriod;

    private Boolean parseDates;

    private Boolean startOnAxis;

    private String tickPosition;

    private Boolean twoLineMode;

    private String widthField;

    /**
     * @return true if axis labels (only when it is horizontal) are wrapped
     */
    public Boolean getAutoWrap() {
        return autoWrap;
    }

    /**
     * Set autoWrap to true if axis labels (only when it is horizontal) should be wrapped if they don't fit in the
     * allocated space. If axis is vertical, you should set axis.ignoreAxisWidth to true in order this feature to work.
     * If not set the default value is false.
     *
     * @param autoWrap autoWrap option
     * @return category axis
     */
    @StudioProperty(defaultValue = "false")
    public CategoryAxis setAutoWrap(Boolean autoWrap) {
        this.autoWrap = autoWrap;
        return this;
    }

    /**
     * @return true if data points is placed at equal intervals
     */
    public Boolean getEqualSpacing() {
        return equalSpacing;
    }

    /**
     * Set equalSpacing to true if want dates to be parsed (displayed on the axis, balloons, etc), but data points to
     * be placed at equal intervals (omitting dates with no data). In case your category axis values are Date objects
     * and parseDates is set to true. If not set the default value is false.
     *
     * @param equalSpacing equalSpacing option
     * @return category axis
     */
    @StudioProperty(defaultValue = "false")
    public CategoryAxis setEqualSpacing(Boolean equalSpacing) {
        this.equalSpacing = equalSpacing;
        return this;
    }

    /**
     * @return force show field name
     */
    public String getForceShowField() {
        return forceShowField;
    }

    /**
     * Sets force show field name from data provider which specifies if the category value should always be shown.
     * For example: forceShowField = "forceShow" and data contains true or false. Note, this works only when
     * parseDates is set to false.
     *
     * @param forceShowField force show field string
     * @return category axis
     */
    @StudioProperty
    public CategoryAxis setForceShowField(String forceShowField) {
        this.forceShowField = forceShowField;
        return this;
    }

    /**
     * @return grid position
     */
    public GridPosition getGridPosition() {
        return gridPosition;
    }

    /**
     * Sets grid position: grid line is placed on the center of a cell or on the beginning of a cell. Possible
     * values are: "start" and "middle" This setting doesn't work if parseDates is set to true. If not set the
     * default value is MIDDLE.
     *
     * @param gridPosition grid position
     * @return category axis
     */
    @StudioProperty(type = PropertyType.ENUMERATION, defaultValue = "MIDDLE")
    public CategoryAxis setGridPosition(GridPosition gridPosition) {
        this.gridPosition = gridPosition;
        return this;
    }

    /**
     * @return the shortest period of your data
     */
    public DatePeriod getMinPeriod() {
        return minPeriod;
    }

    /**
     * Sets the shortest period of your data. This should be set only if parseDates is set to true. If not set the
     * default value is "DD".
     *
     * @param minPeriod the shortest period of your data
     * @return category axis
     */
    @StudioProperty(type = PropertyType.ENUMERATION, defaultValue = "DAYS")
    public CategoryAxis setMinPeriod(DatePeriod minPeriod) {
        this.minPeriod = minPeriod;
        return this;
    }

    /**
     * @return true if parseDates is enabled
     */
    public Boolean getParseDates() {
        return parseDates;
    }

    /**
     * Set parseDates to true if your category axis values are date-based. In this case the chart will parse dates
     * and will place your data points at irregular intervals. If you want dates to be parsed, but data points to be
     * placed at equal intervals, set both parseDates and equalSpacing to true.
     * <p>
     * Note, if you are specifying dates as strings in your data, i.e. "2015-01-05", we strongly recommend setting
     * dataDateFormat as well.
     * <p>
     * Important, if parseDates is set to true, the data points need to come pre-ordered in ascending order. Data with
     * incorrect order might result in visual and functional glitches on the chart.
     * <p>
     * If not set the default value is false.
     *
     * @param parseDates parseDates option
     * @return category axis
     */
    @StudioProperty(defaultValue = "false")
    public CategoryAxis setParseDates(Boolean parseDates) {
        this.parseDates = parseDates;
        return this;
    }

    /**
     * @return true if the graph starts on axis
     */
    public Boolean getStartOnAxis() {
        return startOnAxis;
    }

    /**
     * Set startOnAxis to true if the graph should start on axis. In case you display columns, it is recommended to set
     * startOnAxis to false. If parseDates is set to true, startOnAxis will always be false, unless equalSpacing is
     * set to true.
     *
     * @param startOnAxis startOnAxis option
     * @return category axis
     */
    @StudioProperty
    public CategoryAxis setStartOnAxis(Boolean startOnAxis) {
        this.startOnAxis = startOnAxis;
        return this;
    }

    /**
     * @return JS function that is used as categoryValue for current item
     */
    public JsFunction getCategoryFunction() {
        return categoryFunction;
    }

    /**
     * Sets JS function that returns the value that should be used as categoryValue for current item.
     * If categoryFunction is set, the return value of the custom data function takes precedence over categoryField.
     * When a chart calls this method, it passes category value, data item from chart's data provider and reference to
     * categoryAxis: categoryFunction(category, dataItem, categoryAxis); This method can be used both when category
     * axis parses dates and when it doesn't. If axis parses dates, your categoryFunction should return Date object.
     *
     * @param categoryFunction JS function
     * @return category axis
     */
    public CategoryAxis setCategoryFunction(JsFunction categoryFunction) {
        this.categoryFunction = categoryFunction;
        return this;
    }

    /**
     * @return label color field
     */
    public String getLabelColorField() {
        return labelColorField;
    }

    /**
     * Sets label color field. Works only with non-date-based data.
     *
     * @param labelColorField label color field string
     * @return category axis
     */
    @StudioProperty(type = PropertyType.PROPERTY_PATH_REF)
    public CategoryAxis setLabelColorField(String labelColorField) {
        this.labelColorField = labelColorField;
        return this;
    }

    /**
     * @return position of an axis tick
     */
    public String getTickPosition() {
        return tickPosition;
    }

    /**
     * Sets position of an axis tick. Available settings: "middle", "start". Works only with non-date-based data. If
     * not set the default value is false. If not set the default value is "middle".
     *
     * @param tickPosition tick position string
     * @return category axis
     */
    @StudioProperty(type = PropertyType.OPTIONS, options = {"middle", "start"}, defaultValue = "middle")
    public CategoryAxis setTickPosition(String tickPosition) {
        this.tickPosition = tickPosition;
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
     */
    @StudioProperty(defaultValue = "false")
    public void setTwoLineMode(Boolean twoLineMode) {
        this.twoLineMode = twoLineMode;
    }

    /**
     * @return JS function to format category axis labels
     */
    public JsFunction getLabelFunction() {
        return labelFunction;
    }

    /**
     * Sets JS function to format category axis labels. If this function is set, then it is called with the
     * following parameters passed: if dates are not parsed - labelFunction(valueText, serialDataItem, categoryAxis),
     * if dates are parsed - labelFunction(valueText, date, categoryAxis). Js function should return string which
     * will be displayed on the axis.
     *
     * @param labelFunction Js function
     * @return category axis
     */
    public CategoryAxis setLabelFunction(JsFunction labelFunction) {
        this.labelFunction = labelFunction;
        return this;
    }

    /**
     * @return width field name
     */
    public String getWidthField() {
        return widthField;
    }

    /**
     * Sets relative width for your columns using this field name from data provider.
     *
     * @param widthField width field name
     * @return category axis
     */
    @StudioProperty(type = PropertyType.PROPERTY_PATH_REF)
    public CategoryAxis setWidthField(String widthField) {
        this.widthField = widthField;
        return this;
    }

    /**
     * @return class name field
     */
    public String getClassNameField() {
        return classNameField;
    }

    /**
     * Sets CSS class name field. If classNameField is set and
     * {@link AbstractChart#addClassNames AbstractChart.addClassNames} is enabled, the category axis labels, ticks
     * and grid will have this class name set. Note, this will not work
     * if the axis is date-based.
     *
     * @param classNameField class name field string
     * @return category axis
     */
    @StudioProperty(type = PropertyType.PROPERTY_PATH_REF)
    public CategoryAxis setClassNameField(String classNameField) {
        this.classNameField = classNameField;
        return this;
    }
}