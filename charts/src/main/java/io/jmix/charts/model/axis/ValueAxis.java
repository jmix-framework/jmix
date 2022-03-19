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


import io.jmix.charts.model.*;
import io.jmix.charts.model.chart.impl.AbstractChart;
import io.jmix.charts.model.chart.impl.RadarChartModelImpl;
import io.jmix.charts.model.chart.impl.SerialChartModelImpl;
import io.jmix.charts.model.chart.impl.XYChartModelImpl;
import io.jmix.charts.model.date.DatePeriod;
import io.jmix.charts.model.date.Duration;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioElement;
import io.jmix.ui.meta.StudioProperty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.Date;
import java.util.Map;

/**
 * Create an axis for {@link SerialChartModelImpl}, {@link RadarChartModelImpl}, {@link XYChartModelImpl}, charts, multiple can be assigned.
 * Gets automatically populated, one for SerialChart and two for XYChart charts, if none has been specified.
 * <br>
 * See documentation for properties of ValueAxis JS Object. <br>
 *
 * <a href="http://docs.amcharts.com/3/javascriptcharts/ValueAxis">http://docs.amcharts.com/3/javascriptcharts/ValueAxis</a>
 */
@StudioElement(
        caption = "Value Axis",
        xmlElement = "valueAxis",
        xmlns = "http://jmix.io/schema/ui/charts",
        xmlnsAlias = "chart")
public class ValueAxis extends AbstractAxis<ValueAxis> {

    private static final long serialVersionUID = -8718385614937510600L;

    private Double axisFrequency;

    private Integer axisTitleOffset;

    private JsFunction balloonTextFunction;

    private Double baseValue;

    private Duration duration;

    private Map<Duration, String> durationUnits;

    private GridType gridType;

    private String id;

    private Boolean includeAllValues;

    private Boolean includeGuidesInMinMax;

    private Boolean includeHidden;

    private Boolean integersOnly;

    private JsFunction labelFunction;

    private Boolean logarithmic;

    private Double maximum;

    private Date maximumDate;

    private Double minimum;

    private Date minimumDate;

    private Double minMaxMultiplier;

    private PointPosition pointPosition;

    private Integer precision;

    private Boolean radarCategoriesEnabled;

    private Boolean recalculateToPercents;

    private Boolean reversed;

    private StackType stackType;

    private Boolean strictMinMax;

    private Double synchronizationMultiplier;

    private String synchronizeWith;

    private String totalText;

    private Color totalTextColor;

    private Integer totalTextOffset;

    private Double treatZeroAs;

    private ValueAxisType type;

    private String unit;

    private UnitPosition unitPosition;

    private Boolean usePrefixes;

    private Boolean useScientificNotation;

    private Double zeroGridAlpha;

    private Boolean autoWrap;

    private DatePeriod minPeriod;

    /**
     * @return the distance from axis to the axis title (category)
     */
    public Integer getAxisTitleOffset() {
        return axisTitleOffset;
    }

    /**
     * Sets the distance from axis to the axis title (category). Work with radar chart only. If not set the default
     * value is 10.
     *
     * @param axisTitleOffset axis title offset
     * @return value axis
     */
    @StudioProperty(defaultValue = "10")
    public ValueAxis setAxisTitleOffset(Integer axisTitleOffset) {
        this.axisTitleOffset = axisTitleOffset;
        return this;
    }

    /**
     * @return the base value of the axis
     */
    public Double getBaseValue() {
        return baseValue;
    }

    /**
     * Sets the base value of the axis. If not set the default value is 0.
     *
     * @param baseValue base value
     * @return value axis
     */
    @StudioProperty(defaultValue = "0")
    public ValueAxis setBaseValue(Double baseValue) {
        this.baseValue = baseValue;
        return this;
    }

    /**
     * @return the duration unit
     */
    public Duration getDuration() {
        return duration;
    }

    /**
     * Set the duration unit if your values represents time units, and you want value axis labels to be formatted as
     * duration. Possible values are: "ss", "mm", "hh" and "DD".
     *
     * @param duration duration
     * @return value axis
     */
    @StudioProperty(type = PropertyType.ENUMERATION)
    public ValueAxis setDuration(Duration duration) {
        this.duration = duration;
        return this;
    }

    /**
     * @return duration units map
     */
    public Map<Duration, String> getDurationUnits() {
        return durationUnits;
    }

    /**
     * If duration property is set, you can specify what string should be displayed next to day, hour, minute and
     * second. I.e. pairs like: "DD": "d.", "hh": ":", "mm": ":", "ss": " ". If not set the default value is
     * <pre>{@code
     * {DD:'d. ', hh:':', mm:':',ss:''}}
     * </pre>
     *
     * @param durationUnits duration units map
     * @return value axis
     */
    public ValueAxis setDurationUnits(Map<Duration, String> durationUnits) {
        this.durationUnits = durationUnits;
        return this;
    }

    /**
     * @return grid type
     */
    public GridType getGridType() {
        return gridType;
    }

    /**
     * Sets grid type. Possible values are: "polygons" and "circles". Set "circles" for polar charts. Work with radar
     * chart only. If not set the default value is POLYGONS.
     *
     * @param gridType grid type
     * @return value axis
     */
    @StudioProperty(type = PropertyType.ENUMERATION, defaultValue = "POLYGONS")
    public ValueAxis setGridType(GridType gridType) {
        this.gridType = gridType;
        return this;
    }

    /**
     * @return unique id of value axis
     */
    public String getId() {
        return id;
    }

    /**
     * Sets unique id of value axis. It is not required to set it, unless you need to tell the graph which exact value
     * axis it should use.
     *
     * @param id id
     * @return value axis
     */
    @StudioProperty
    public ValueAxis setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * @return true the guide values are included when calculating minimum and maximum of the axis
     */
    public Boolean getIncludeGuidesInMinMax() {
        return includeGuidesInMinMax;
    }

    /**
     * Set includeGuidesInMinMax to true the guide values should be included when calculating minimum and maximum of
     * the axis. If not set the default value is false.
     *
     * @param includeGuidesInMinMax includeGuidesInMinMax option
     * @return value axis
     */
    @StudioProperty(defaultValue = "false")
    public ValueAxis setIncludeGuidesInMinMax(Boolean includeGuidesInMinMax) {
        this.includeGuidesInMinMax = includeGuidesInMinMax;
        return this;
    }

    /**
     * @return true if the axis includes hidden graphs when calculating minimum and maximum values
     */
    public Boolean getIncludeHidden() {
        return includeHidden;
    }

    /**
     * Sets includeHidden to true if the axis should include hidden graphs when calculating minimum and maximum
     * values. If not set the default value is false.
     *
     * @param includeHidden includeHidden option
     * @return value axis
     */
    @StudioProperty(defaultValue = "false")
    public ValueAxis setIncludeHidden(Boolean includeHidden) {
        this.includeHidden = includeHidden;
        return this;
    }

    /**
     * @return true if values on the axis are only integers
     */
    public Boolean getIntegersOnly() {
        return integersOnly;
    }

    /**
     * Set integersOnly to true if values on the axis can only be integers. If not set the default value is false.
     *
     * @param integersOnly integersOnly option
     * @return value axis
     */
    @StudioProperty(defaultValue = "false")
    public ValueAxis setIntegersOnly(Boolean integersOnly) {
        this.integersOnly = integersOnly;
        return this;
    }

    /**
     * @return true if this value axis scale is logarithmic
     */
    public Boolean getLogarithmic() {
        return logarithmic;
    }

    /**
     * Set logarithmic to true if this value axis scale should be logarithmic. If not set the default value is false.
     *
     * @param logarithmic logarithmic option
     * @return value axis
     */
    @StudioProperty(defaultValue = "false")
    public ValueAxis setLogarithmic(Boolean logarithmic) {
        this.logarithmic = logarithmic;
        return this;
    }

    /**
     * @return maximum value
     */
    public Double getMaximum() {
        return maximum;
    }

    /**
     * Set maximum value if you don't want maximum value to be calculated by the chart. This value might still be
     * adjusted so that it would be possible to draw grid at rounded intervals.
     *
     * @param maximum maximum value
     * @return value axis
     */
    @StudioProperty
    public ValueAxis setMaximum(Double maximum) {
        this.maximum = maximum;
        return this;
    }

    /**
     * @return minimum value
     */
    public Double getMinimum() {
        return minimum;
    }

    /**
     * Set minimum value if you don't want minimum value to be calculated by the chart. This value might still be
     * adjusted so that it would be possible to draw grid at rounded intervals.
     *
     * @param minimum minimum value
     * @return value axis
     */
    @StudioProperty
    public ValueAxis setMinimum(Double minimum) {
        this.minimum = minimum;
        return this;
    }

    /**
     * @return minimum and maximum multiplier
     */
    public Double getMinMaxMultiplier() {
        return minMaxMultiplier;
    }

    /**
     * Sets minimum and maximum multiplier. If set value axis scale (min and max numbers) will be multiplied by it.
     * I.e. if set to 1.2 the scope of values will increase by 20%. If not set the default value is 1.
     *
     * @param minMaxMultiplier minimum and maximum multiplier
     * @return value axis
     */
    @StudioProperty(defaultValue = "1")
    public ValueAxis setMinMaxMultiplier(Double minMaxMultiplier) {
        this.minMaxMultiplier = minMaxMultiplier;
        return this;
    }

    /**
     * @return precision (number of decimals) of values
     */
    public Integer getPrecision() {
        return precision;
    }

    /**
     * Sets precision (number of decimals) of values.
     *
     * @param precision precision
     * @return value axis
     */
    @StudioProperty
    public ValueAxis setPrecision(Integer precision) {
        this.precision = precision;
        return this;
    }

    /**
     * @return true if categories (axes titles) are displayed near axes
     */
    public Boolean getRadarCategoriesEnabled() {
        return radarCategoriesEnabled;
    }

    /**
     * Set radarCategoriesEnabled to false if categories (axes titles) shouldn't be displayed near axes. If not set
     * the default value is true.
     *
     * @param radarCategoriesEnabled radarCategoriesEnabled option
     * @return value axis
     */
    @StudioProperty(defaultValue = "true")
    public ValueAxis setRadarCategoriesEnabled(Boolean radarCategoriesEnabled) {
        this.radarCategoriesEnabled = radarCategoriesEnabled;
        return this;
    }

    /**
     * @return true if graphs values are recalculated to percents
     */
    public Boolean getRecalculateToPercents() {
        return recalculateToPercents;
    }

    /**
     * Set recalculateToPercents to true, if graphs values should be recalculated to percents. Note, that this
     * setting will work only on serial chart (and stock), not on any other charts that are using {@link ValueAxis},
     * like XY chart. If not set the default value is false.
     *
     * @param recalculateToPercents recalculateToPercents option
     * @return value axis
     */
    @StudioProperty(defaultValue = "false")
    public ValueAxis setRecalculateToPercents(Boolean recalculateToPercents) {
        this.recalculateToPercents = recalculateToPercents;
        return this;
    }

    /**
     * @return true if value axis is reversed
     */
    public Boolean getReversed() {
        return reversed;
    }

    /**
     * Set reversed to true if value axis should be reversed (smaller values on top). If not set the default value is
     * false.
     *
     * @param reversed reversed option
     * @return value axis
     */
    @StudioProperty(defaultValue = "false")
    public ValueAxis setReversed(Boolean reversed) {
        this.reversed = reversed;
        return this;
    }

    /**
     * @return stacking mode of the axis
     */
    public StackType getStackType() {
        return stackType;
    }

    /**
     * Sets	stacking mode of the axis. Possible values are: "none", "regular", "100%", "3d". Note, only graphs of one
     * type will be stacked. If not set the default value is NONE.
     *
     * @param stackType stack type
     * @return value axis
     */
    @StudioProperty(type = PropertyType.ENUMERATION, defaultValue = "NONE")
    public ValueAxis setStackType(StackType stackType) {
        this.stackType = stackType;
        return this;
    }

    /**
     * @return synchronization multiplier
     */
    public Double getSynchronizationMultiplier() {
        return synchronizationMultiplier;
    }

    /**
     * Set the synchronization multiplier, in case you synchronize one value axis with another.
     *
     * @param synchronizationMultiplier synchronization multiplier
     * @return value axis
     */
    @StudioProperty
    public ValueAxis setSynchronizationMultiplier(Double synchronizationMultiplier) {
        this.synchronizationMultiplier = synchronizationMultiplier;
        return this;
    }

    /**
     * @return total text
     */
    public String getTotalText() {
        return totalText;
    }

    /**
     * Sets total text. If this value axis is stacked and has columns, setting to "[[total]]" will make it to
     * display total value above the most-top column.
     *
     * @param totalText total text
     * @return value axis
     */
    @StudioProperty
    public ValueAxis setTotalText(String totalText) {
        this.totalText = totalText;
        return this;
    }

    /**
     * @return color of total text
     */
    public Color getTotalTextColor() {
        return totalTextColor;
    }

    /**
     * Sets color of total text.
     *
     * @param totalTextColor color
     * @return value axis
     */
    @StudioProperty(type = PropertyType.OPTIONS)
    public ValueAxis setTotalTextColor(Color totalTextColor) {
        this.totalTextColor = totalTextColor;
        return this;
    }

    /**
     * @return unit which is added to the value label
     */
    public String getUnit() {
        return unit;
    }

    /**
     * Sets unit which will be added to the value label.
     *
     * @param unit unit
     * @return value axis
     */
    @StudioProperty
    public ValueAxis setUnit(String unit) {
        this.unit = unit;
        return this;
    }

    /**
     * @return position of the unit
     */
    public UnitPosition getUnitPosition() {
        return unitPosition;
    }

    /**
     * Sets position of the unit. Possible values are LEFT and RIGHT. If not set the default value is RIGHT.
     *
     * @param unitPosition unit position
     * @return value axis
     */
    @StudioProperty(type = PropertyType.ENUMERATION, defaultValue = "RIGHT")
    public ValueAxis setUnitPosition(UnitPosition unitPosition) {
        this.unitPosition = unitPosition;
        return this;
    }

    /**
     * @return true if prefixes are used for big and small numbers
     */
    public Boolean getUsePrefixes() {
        return usePrefixes;
    }

    /**
     * Set usePrefixes to true, if prefixes should be used for big and small numbers. You can set list of prefixes
     * directly to the chart via
     * {@link AbstractChart#prefixesOfSmallNumbers prefixesOfSmallNumbers} and
     * {@link AbstractChart#prefixesOfBigNumbers prefixesOfBigNumbers}. If not set the default value is false.
     *
     * @param usePrefixes usePrefixes option
     * @return value axis
     */
    @StudioProperty(defaultValue = "false")
    public ValueAxis setUsePrefixes(Boolean usePrefixes) {
        this.usePrefixes = usePrefixes;
        return this;
    }

    /**
     * @return true if values are formatted using scientific notation (5e+8, 5e-8...)
     */
    public Boolean getUseScientificNotation() {
        return useScientificNotation;
    }

    /**
     * Set useScientificNotation to true if values should always be formatted using scientific notation (5e+8, 5e-8...).
     * Otherwise only values bigger then 1e+21 and smaller then 1e-7 will be displayed in scientific notation. If not
     * set the default value is false.
     *
     * @param useScientificNotation useScientificNotation option
     * @return value axis
     */
    @StudioProperty(defaultValue = "false")
    public ValueAxis setUseScientificNotation(Boolean useScientificNotation) {
        this.useScientificNotation = useScientificNotation;
        return this;
    }

    /**
     * @return synchronized value axis id
     */
    public String getSynchronizeWith() {
        return synchronizeWith;
    }

    /**
     * One value axis can be synchronized with another value axis. You can use id of the axis here. You should set
     * synchronizationMultiplier in order for this to work.
     *
     * @param synchronizeWith id of the axis
     * @return value axis
     */
    @StudioProperty
    public ValueAxis setSynchronizeWith(String synchronizeWith) {
        this.synchronizeWith = synchronizeWith;
        return this;
    }

    /**
     * @return distance from data point to total text
     */
    public Integer getTotalTextOffset() {
        return totalTextOffset;
    }

    /**
     * Sets distance from data point to total text. If not set the default value is 0.
     *
     * @param totalTextOffset total text offset
     * @return value axis
     */
    @StudioProperty(defaultValue = "0")
    public ValueAxis setTotalTextOffset(Integer totalTextOffset) {
        this.totalTextOffset = totalTextOffset;
        return this;
    }

    /**
     * @return treatZeroAs value
     */
    public Double getTreatZeroAs() {
        return treatZeroAs;
    }

    /**
     * This allows you to have logarithmic value axis and have zero values in the data. You must set it to greater
     * than 0 value in order to work. If not set the default value is 0.
     *
     * @param treatZeroAs treatZeroAs value
     * @return value axis
     */
    @StudioProperty(defaultValue = "0")
    public ValueAxis setTreatZeroAs(Double treatZeroAs) {
        this.treatZeroAs = treatZeroAs;
        return this;
    }

    /**
     * @return true if minimum and maximum of value axis doesn't change while zooming/scrolling
     */
    public Boolean getIncludeAllValues() {
        return includeAllValues;
    }

    /**
     * Set includeAllValues to true if minimum and maximum of value axis shouldn't change while zooming/scrolling. If
     * not set the default value is false.
     *
     * @param includeAllValues includeAllValues option
     * @return value axis
     */
    @StudioProperty(defaultValue = "false")
    public ValueAxis setIncludeAllValues(Boolean includeAllValues) {
        this.includeAllValues = includeAllValues;
        return this;
    }

    /**
     * @return JS function to format value axis labels
     */
    public JsFunction getLabelFunction() {
        return labelFunction;
    }

    /**
     * Sets JS function to format value axis labels. This function is called and these parameters are
     * passed: labelFunction(value, valueText, valueAxis); Where value is numeric value, valueText is formatted
     * string and valueAxis is a reference to valueAxis object. If axis type is "date", labelFunction will pass
     * different arguments: labelFunction(valueText, date, valueAxis). Your function should return string.
     *
     * @param labelFunction JS function
     * @return value axis
     */
    public ValueAxis setLabelFunction(JsFunction labelFunction) {
        this.labelFunction = labelFunction;
        return this;
    }

    /**
     * @return maximum date of the axis
     */
    public Date getMaximumDate() {
        return maximumDate;
    }

    /**
     * Sets maximum date of the axis, if your value axis is date-based.
     *
     * @param maximumDate maximum date
     * @return value axis
     */
    @StudioProperty
    public ValueAxis setMaximumDate(Date maximumDate) {
        this.maximumDate = maximumDate;
        return this;
    }

    /**
     * @return minimum date of the axis
     */
    public Date getMinimumDate() {
        return minimumDate;
    }

    /**
     * Sets minimum date of the axis, if your value axis is date-based.
     *
     * @param minimumDate minimum date
     * @return value axis
     */
    @StudioProperty
    public ValueAxis setMinimumDate(Date minimumDate) {
        this.minimumDate = minimumDate;
        return this;
    }

    /**
     * @return point position
     */
    public PointPosition getPointPosition() {
        return pointPosition;
    }

    /**
     * Sets point position. If you set it to “middle”, labels and data points will be placed in the middle between
     * axes. Works with radar charts only. If not set the default value is START.
     *
     * @param pointPosition point position
     * @return value axis
     */
    @StudioProperty(type = PropertyType.ENUMERATION, defaultValue = "START")
    public ValueAxis setPointPosition(PointPosition pointPosition) {
        this.pointPosition = pointPosition;
        return this;
    }

    /**
     * @return true if the chart doesn't adjust minimum and maximum of value axis
     */
    public Boolean getStrictMinMax() {
        return strictMinMax;
    }

    /**
     * If you set minimum and maximum for your axis, chart adjusts them so that grid would start and end on the
     * beginning and end of plot area and grid would be at equal intervals. If you set strictMinMax to true, the
     * chart will not adjust minimum and maximum of value axis. If not set the default value is false.
     *
     * @param strictMinMax strictMinMax option
     * @return value axis
     */
    @StudioProperty(defaultValue = "false")
    public ValueAxis setStrictMinMax(Boolean strictMinMax) {
        this.strictMinMax = strictMinMax;
        return this;
    }

    /**
     * @return type of value axis
     */
    public ValueAxisType getType() {
        return type;
    }

    /**
     * Sets type of value axis. If your values in data provider are dates and you want this axis to show dates
     * instead of numbers, set it to "date". If not set the default value is NUMERIC.
     *
     * @param type type
     * @return value axis
     */
    @StudioProperty(type = PropertyType.ENUMERATION, defaultValue = "NUMERIC")
    public ValueAxis setType(ValueAxisType type) {
        this.type = type;
        return this;
    }

    /**
     * @return opacity of a zero grid line
     */
    public Double getZeroGridAlpha() {
        return zeroGridAlpha;
    }

    /**
     * Sets opacity of a zero grid line. By default it is equal to 2 x gridAlpha.
     *
     * @param zeroGridAlpha opacity
     * @return value axis
     */
    @StudioProperty
    @Max(1)
    @Min(0)
    public ValueAxis setZeroGridAlpha(Double zeroGridAlpha) {
        this.zeroGridAlpha = zeroGridAlpha;
        return this;
    }

    /**
     * @return axis frequency
     */
    public Double getAxisFrequency() {
        return axisFrequency;
    }

    /**
     * Sets axis frequency. If you have a big number of axes, this property will help you to show every X axis only.
     * Works with radar chart only. If not set the default value is 1.
     *
     * @param axisFrequency axis frequency
     * @return value axis
     */
    @StudioProperty(defaultValue = "1")
    public ValueAxis setAxisFrequency(Double axisFrequency) {
        this.axisFrequency = axisFrequency;
        return this;
    }

    /**
     * @return JS function to format balloon text of the axis
     */
    public JsFunction getBalloonTextFunction() {
        return balloonTextFunction;
    }

    /**
     * Sets JS function to format balloon text of the axis. This function is called and balloon text or date
     * (if axis is date-based) is passed as an argument. It should return string which will be displayed in the balloon.
     *
     * @param balloonTextFunction JS function
     * @return value axis
     */
    public ValueAxis setBalloonTextFunction(JsFunction balloonTextFunction) {
        this.balloonTextFunction = balloonTextFunction;
        return this;
    }

    /**
     * @return true if autoWrap is enabled
     */
    public Boolean getAutoWrap() {
        return autoWrap;
    }

    /**
     * If this is set to true and the label does not fit, it will be wrapped. Works only on horizontal value axes. If
     * not set the default value is false.
     *
     * @param autoWrap autoWrap option
     */
    @StudioProperty(defaultValue = "false")
    public void setAutoWrap(Boolean autoWrap) {
        this.autoWrap = autoWrap;
    }

    /**
     * @return the shortest period of your data
     */
    public DatePeriod getMinPeriod() {
        return minPeriod;
    }

    /**
     * Sets the shortest period of your data. This will work only if you set the type of your value axis to "date".
     * If not set the default value is "DD".
     *
     * @param minPeriod shortest period
     */
    @StudioProperty(type = PropertyType.ENUMERATION, defaultValue = "DAYS")
    public void setMinPeriod(DatePeriod minPeriod) {
        this.minPeriod = minPeriod;
    }
}