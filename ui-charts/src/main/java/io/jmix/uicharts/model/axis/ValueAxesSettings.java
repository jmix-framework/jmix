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

package io.jmix.uicharts.model.axis;

import io.jmix.uicharts.model.*;
import io.jmix.uicharts.model.label.Label;
import io.jmix.uicharts.model.settings.PanelsSettings;

/**
 * Defines set of properties for all {@link ValueAxis}. If there is no default value specified, default value of
 * {@link ValueAxis} class will be used.
 * <p>
 * See documentation for properties of {@link ValueAxesSettings} JS object. <br>
 *
 * <a href="http://docs.amcharts.com/3/javascriptstockchart/ValueAxesSettings">http://docs.amcharts.com/3/javascriptstockchart/ValueAxesSettings</a>
 */
public class ValueAxesSettings extends AbstractChartObject {

    private static final long serialVersionUID = 5770922216765077570L;

    private Boolean autoGridCount;

    private Double axisAlpha;

    private Color axisColor;

    private Integer axisThickness;

    private Color color;

    private Integer dashLength;

    private Double fillAlpha;

    private Color fillColor;

    private Double gridAlpha;

    private Color gridColor;

    private Integer gridCount;

    private Integer gridThickness;

    private Boolean includeGuidesInMinMax;

    private Boolean includeHidden;

    private Boolean inside;

    private Boolean integersOnly;

    private Double labelFrequency;

    private Integer labelOffset;

    private Boolean labelsEnabled;

    private Boolean logarithmic;

    private Double maximum;

    private Double minimum;

    private Double minMaxMultiplier;

    private Double minorGridAlpha;

    private Boolean minorGridEnabled;

    private Integer minVerticalGap;

    private Integer offset;

    private ValueAxisPosition position;

    private Integer precision;

    private Boolean reversed;

    private Boolean showFirstLabel;

    private Boolean showLastLabel;

    private StackType stackType;

    private Boolean strictMinMax;

    private Integer tickLength;

    private String unit;

    private UnitPosition unitPosition;

    /**
     * @return true if the number for gridCount is specified automatically, according to the axis size
     */
    public Boolean getAutoGridCount() {
        return autoGridCount;
    }

    /**
     * Set autoGridCount to false the number for gridCount shouldn't be specified automatically, according to the axis
     * size. If not set the default value is true.
     *
     * @param autoGridCount autoGridCount option
     */
    public ValueAxesSettings setAutoGridCount(Boolean autoGridCount) {
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
     * @param axisAlpha opacity
     */
    public ValueAxesSettings setAxisAlpha(Double axisAlpha) {
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
     * @param axisColor color
     */
    public ValueAxesSettings setAxisColor(Color axisColor) {
        this.axisColor = axisColor;
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
     * @param axisThickness thickness
     */
    public ValueAxesSettings setAxisThickness(Integer axisThickness) {
        this.axisThickness = axisThickness;
        return this;
    }

    /**
     * @return {@link Label label} color
     */
    public Color getColor() {
        return color;
    }

    /**
     * Sets {@link Label label} color.
     *
     * @param color color
     */
    public ValueAxesSettings setColor(Color color) {
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
     * Sets length of a dash. By default, the grid line is not dashed.
     *
     * @param dashLength dash length
     */
    public ValueAxesSettings setDashLength(Integer dashLength) {
        this.dashLength = dashLength;
        return this;
    }

    /**
     * @return fill opacity
     */
    public Double getFillAlpha() {
        return fillAlpha;
    }

    /**
     * Sets fill opacity. Every second space between grid lines can be filled with color.
     *
     * @param fillAlpha opacity
     */
    public ValueAxesSettings setFillAlpha(Double fillAlpha) {
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
     * @param fillColor color
     */
    public ValueAxesSettings setFillColor(Color fillColor) {
        this.fillColor = fillColor;
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
     * @param gridAlpha opacity
     */
    public ValueAxesSettings setGridAlpha(Double gridAlpha) {
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
     * @param gridColor color
     */
    public ValueAxesSettings setGridColor(Color gridColor) {
        this.gridColor = gridColor;
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
     * @param gridThickness thickness
     */
    public ValueAxesSettings setGridThickness(Integer gridThickness) {
        this.gridThickness = gridThickness;
        return this;
    }

    /**
     * @return true if includeGuidesInMinMax is enabled
     */
    public Boolean getIncludeGuidesInMinMax() {
        return includeGuidesInMinMax;
    }

    /**
     * Specifies whether guide values should be included when calculating minimum and maximum of the axis.
     *
     * @param includeGuidesInMinMax includeGuidesInMinMax option
     */
    public ValueAxesSettings setIncludeGuidesInMinMax(Boolean includeGuidesInMinMax) {
        this.includeGuidesInMinMax = includeGuidesInMinMax;
        return this;
    }

    /**
     * @return true if includeHidden is enabled
     */
    public Boolean getIncludeHidden() {
        return includeHidden;
    }

    /**
     * Specifies whether the axis should include hidden graphs when calculating minimum and maximum values.
     *
     * @param includeHidden includeHidden option
     */
    public ValueAxesSettings setIncludeHidden(Boolean includeHidden) {
        this.includeHidden = includeHidden;
        return this;
    }

    /**
     * @return true if values is placed inside plot area
     */
    public Boolean getInside() {
        return inside;
    }

    /**
     * Set inside to false if values should be placed inside plot area. In case you set this to false, you'll have to
     * adjust {@link PanelsSettings#marginLeft} or {@link PanelsSettings#marginRight} in order labels to be visible.
     * Note, if you set this property to false, you might also consider setting showLastLabel to true. If not set the
     * default value is true.
     *
     * @param inside inside option
     */
    public ValueAxesSettings setInside(Boolean inside) {
        this.inside = inside;
        return this;
    }

    /**
     * @return true if integersOnly is enabled
     */
    public Boolean getIntegersOnly() {
        return integersOnly;
    }

    /**
     * Specifies whether values on axis can only be integers or both integers and doubles.
     *
     * @param integersOnly integersOnly option
     */
    public ValueAxesSettings setIntegersOnly(Boolean integersOnly) {
        this.integersOnly = integersOnly;
        return this;
    }

    /**
     * @return frequency at which labels is placed
     */
    public Double getLabelFrequency() {
        return labelFrequency;
    }

    /**
     * Sets frequency at which labels should be placed.
     *
     * @param labelFrequency label frequency
     */
    public ValueAxesSettings setLabelFrequency(Double labelFrequency) {
        this.labelFrequency = labelFrequency;
        return this;
    }

    /**
     * @return label offset
     */
    public Integer getLabelOffset() {
        return labelOffset;
    }

    /**
     * Sets label offset. You can use it to adjust position of axis labels. If not set the default value is 0.
     *
     * @param labelOffset label offset
     */
    public ValueAxesSettings setLabelOffset(Integer labelOffset) {
        this.labelOffset = labelOffset;
        return this;
    }

    /**
     * @return true if value labels is displayed
     */
    public Boolean getLabelsEnabled() {
        return labelsEnabled;
    }

    /**
     * Set labelsEnabled to false if value labels shouldn't be displayed.
     *
     * @param labelsEnabled labelsEnabled option
     */
    public ValueAxesSettings setLabelsEnabled(Boolean labelsEnabled) {
        this.labelsEnabled = labelsEnabled;
        return this;
    }

    /**
     * @return true if value axis is logarithmic
     */
    public Boolean getLogarithmic() {
        return logarithmic;
    }

    /**
     * Set logarithmic to true if value axis is logarithmic.
     *
     * @param logarithmic logarithmic option
     */
    public ValueAxesSettings setLogarithmic(Boolean logarithmic) {
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
     */
    public ValueAxesSettings setMaximum(Double maximum) {
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
     */
    public ValueAxesSettings setMinimum(Double minimum) {
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
     * I.e. if set to 1.2 the scope of values will increase by 20%.
     *
     * @param minMaxMultiplier minimum and maximum multiplier
     */
    public ValueAxesSettings setMinMaxMultiplier(Double minMaxMultiplier) {
        this.minMaxMultiplier = minMaxMultiplier;
        return this;
    }

    /**
     * @return the distance of the axis to the plot area, in pixels
     */
    public Integer getOffset() {
        return offset;
    }

    /**
     * Sets the distance of the axis to the plot area, in pixels. Useful if you have more then one axis on the same
     * side.
     *
     * @param offset offset
     */
    public ValueAxesSettings setOffset(Integer offset) {
        this.offset = offset;
        return this;
    }

    /**
     * @return position of the value axis
     */
    public ValueAxisPosition getPosition() {
        return position;
    }

    /**
     * Sets position of the value axis. Possible values are "left" and "right".
     *
     * @param position position
     */
    public ValueAxesSettings setPosition(ValueAxisPosition position) {
        this.position = position;
        return this;
    }

    /**
     * @return true if value axis is reversed
     */
    public Boolean getReversed() {
        return reversed;
    }

    /**
     * Set reversed to true if value axis should be reversed (smaller values on top).
     *
     * @param reversed reversed option
     */
    public ValueAxesSettings setReversed(Boolean reversed) {
        this.reversed = reversed;
        return this;
    }

    /**
     * @return true if first label of value axis is displayed
     */
    public Boolean getShowFirstLabel() {
        return showFirstLabel;
    }

    /**
     * Set showFirstLabel to false if first label of value axis shouldn't be displayed. If not set the default value
     * is true.
     *
     * @param showFirstLabel showFirstLabel option
     */
    public ValueAxesSettings setShowFirstLabel(Boolean showFirstLabel) {
        this.showFirstLabel = showFirstLabel;
        return this;
    }

    /**
     * @return true if last label of value axis is displayed
     */
    public Boolean getShowLastLabel() {
        return showLastLabel;
    }

    /**
     * Set showLastLabel to true if last label of value axis should be displayed. If not set the default value is false.
     *
     * @param showLastLabel showLastLabel option
     */
    public ValueAxesSettings setShowLastLabel(Boolean showLastLabel) {
        this.showLastLabel = showLastLabel;
        return this;
    }

    /**
     * @return stacking mode of the axis
     */
    public StackType getStackType() {
        return stackType;
    }

    /**
     * Sets stacking mode of the axis. Possible values are: "none", "regular", "100%", "3d".
     *
     * @param stackType stack type
     */
    public ValueAxesSettings setStackType(StackType stackType) {
        this.stackType = stackType;
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
     */
    public ValueAxesSettings setTickLength(Integer tickLength) {
        this.tickLength = tickLength;
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
     */
    public ValueAxesSettings setUnit(String unit) {
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
     * Sets position of the unit. Possible values are "left" or "right".
     *
     * @param unitPosition unit position
     */
    public ValueAxesSettings setUnitPosition(UnitPosition unitPosition) {
        this.unitPosition = unitPosition;
        return this;
    }

    /**
     * @return approximate number of grid lines
     */
    public Integer getGridCount() {
        return gridCount;
    }

    /**
     * Sets approximate number of grid lines. autoGridCount should be set to false, otherwise this property will be
     * ignored.
     *
     * @param gridCount grid count
     */
    public ValueAxesSettings setGridCount(Integer gridCount) {
        this.gridCount = gridCount;
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
     * @param minorGridAlpha opacity
     */
    public ValueAxesSettings setMinorGridAlpha(Double minorGridAlpha) {
        this.minorGridAlpha = minorGridAlpha;
        return this;
    }

    /**
     * @return true if minor grid is enabled
     */
    public Boolean getMinorGridEnabled() {
        return minorGridEnabled;
    }

    /**
     * Specifies if minor grid should be displayed. Note, if equalSpacing is set to true, this setting will be ignored.
     *
     * @param minorGridEnabled minorGridEnabled option
     */
    public ValueAxesSettings setMinorGridEnabled(Boolean minorGridEnabled) {
        this.minorGridEnabled = minorGridEnabled;
        return this;
    }

    /**
     * @return minimum vertical gap
     */
    public Integer getMinVerticalGap() {
        return minVerticalGap;
    }

    /**
     * Sets minimum vertical gap. This property is used when calculating grid count (when autoGridCount is true). It
     * specifies minimum cell height required for one span between grid lines.
     *
     * @param minVerticalGap minimum vertical gap
     */
    public ValueAxesSettings setMinVerticalGap(Integer minVerticalGap) {
        this.minVerticalGap = minVerticalGap;
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
     */
    public ValueAxesSettings setPrecision(Integer precision) {
        this.precision = precision;
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
     * beginning and end of plot area and grid would be at equal intervals. Set strictMinMax to true, if the
     * chart shouldn't adjust minimum and maximum of value axis.
     *
     * @param strictMinMax strictMinMax option
     */
    public ValueAxesSettings setStrictMinMax(Boolean strictMinMax) {
        this.strictMinMax = strictMinMax;
        return this;
    }
}
