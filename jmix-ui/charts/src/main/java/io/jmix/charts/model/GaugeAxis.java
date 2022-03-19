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

import io.jmix.charts.component.AngularGaugeChart;
import io.jmix.charts.model.axis.UnitPosition;
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
 * Creates an axis for {@link AngularGaugeChart AngularGaugeChart} charts, multiple can be assigned.
 * <br>
 * See documentation for properties of GaugeAxis JS Object. <br>
 *
 * <a href="http://docs.amcharts.com/3/javascriptcharts/GaugeAxis">http://docs.amcharts.com/3/javascriptcharts/GaugeAxis</a>
 */
@StudioElement(
        caption = "GaugeAxis",
        xmlElement = "axis",
        xmlns = "http://jmix.io/schema/ui/charts",
        xmlnsAlias = "chart")
public class GaugeAxis extends AbstractChartObject {

    private static final long serialVersionUID = -27560253244597238L;

    private Double axisAlpha;

    private Color axisColor;

    private Integer axisThickness;

    private Double bandAlpha;

    private List<Float> bandGradientRatio;

    private Double bandOutlineAlpha;

    private Color bandOutlineColor;

    private Integer bandOutlineThickness;

    private List<GaugeBand> bands;

    private String bottomText;

    private Boolean bottomTextBold;

    private Color bottomTextColor;

    private Integer bottomTextFontSize;

    private Integer bottomTextYOffset;

    private String centerX;

    private String centerY;

    private Color color;

    private Integer endAngle;

    private Double endValue;

    private Integer fontSize;

    private Integer gridCount;

    private Boolean gridInside;

    private String id;

    private Boolean inside;

    private Double labelFrequency;

    private JsFunction labelFunction;

    private Integer labelOffset;

    private Boolean labelsEnabled;

    private Double minorTickInterval;

    private Integer minorTickLength;

    private String radius;

    private Boolean showFirstLabel;

    private Boolean showLastLabel;

    private Integer startAngle;

    private Double startValue;

    private Double tickAlpha;

    private Color tickColor;

    private Integer tickLength;

    private Integer tickThickness;

    private String topText;

    private Boolean topTextBold;

    private Color topTextColor;

    private Integer topTextFontSize;

    private Integer topTextYOffset;

    private String unit;

    private UnitPosition unitPosition;

    private Boolean usePrefixes;

    private Integer valueInterval;

    /**
     * @return axis opacity
     */
    public Double getAxisAlpha() {
        return axisAlpha;
    }

    /**
     * Sets axis opacity. If not set the default value is 1.
     *
     * @param axisAlpha opacity
     * @return gauge axis
     */
    @StudioProperty(defaultValue = "1")
    @Max(1)
    @Min(0)
    public GaugeAxis setAxisAlpha(Double axisAlpha) {
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
     * Sets axis color. If not set the default value is #000000.
     *
     * @param axisColor color
     * @return gauge axis
     */
    @StudioProperty(type = PropertyType.OPTIONS, defaultValue = "#000000")
    public GaugeAxis setAxisColor(Color axisColor) {
        this.axisColor = axisColor;
        return this;
    }

    /**
     * @return thickness of the axis outline
     */
    public Integer getAxisThickness() {
        return axisThickness;
    }

    /**
     * Sets thickness of the axis outline. If not set the default value is 1.
     *
     * @param axisThickness axis thickness
     * @return gauge axis
     */
    @StudioProperty(defaultValue = "1")
    public GaugeAxis setAxisThickness(Integer axisThickness) {
        this.axisThickness = axisThickness;
        return this;
    }

    /**
     * @return opacity of band fills
     */
    public Double getBandAlpha() {
        return bandAlpha;
    }

    /**
     * Sets opacity of band fills. If not set the default value is 1.
     *
     * @param bandAlpha opacity
     * @return gauge axis
     */
    @StudioProperty(defaultValue = "1")
    @Max(1)
    @Min(0)
    public GaugeAxis setBandAlpha(Double bandAlpha) {
        this.bandAlpha = bandAlpha;
        return this;
    }

    /**
     * @return opacity of band outlines
     */
    public Double getBandOutlineAlpha() {
        return bandOutlineAlpha;
    }

    /**
     * Sets opacity of band outlines. If not set the default value is 0.
     *
     * @param bandOutlineAlpha opacity
     * @return gauge axis
     */
    @StudioProperty(defaultValue = "0")
    @Max(1)
    @Min(0)
    public GaugeAxis setBandOutlineAlpha(Double bandOutlineAlpha) {
        this.bandOutlineAlpha = bandOutlineAlpha;
        return this;
    }

    /**
     * @return color of band outlines
     */
    public Color getBandOutlineColor() {
        return bandOutlineColor;
    }

    /**
     * Sets color of band outlines. If not set the default value is #000000.
     *
     * @param bandOutlineColor color
     * @return gauge axis
     */
    @StudioProperty(type = PropertyType.OPTIONS, defaultValue = "#000000")
    public GaugeAxis setBandOutlineColor(Color bandOutlineColor) {
        this.bandOutlineColor = bandOutlineColor;
        return this;
    }

    /**
     * @return thickness of band outlines
     */
    public Integer getBandOutlineThickness() {
        return bandOutlineThickness;
    }

    /**
     * Sets thickness of band outlines. If not set the default value is 0.
     *
     * @param bandOutlineThickness thickness
     * @return gauge axis
     */
    @StudioProperty(defaultValue = "0")
    public GaugeAxis setBandOutlineThickness(Integer bandOutlineThickness) {
        this.bandOutlineThickness = bandOutlineThickness;
        return this;
    }

    /**
     * @return text displayed below the axis center
     */
    public String getBottomText() {
        return bottomText;
    }

    /**
     * Sets text displayed below the axis center.
     *
     * @param bottomText bottom text
     * @return gauge axis
     */
    @StudioProperty
    public GaugeAxis setBottomText(String bottomText) {
        this.bottomText = bottomText;
        return this;
    }

    /**
     * @return true if text is bold
     */
    public Boolean getBottomTextBold() {
        return bottomTextBold;
    }

    /**
     * Set bottomTextBold to false if text shouldn't be bold. If not set the default value is true.
     *
     * @param bottomTextBold bottomTextBold option
     * @return gauge axis
     */
    @StudioProperty(defaultValue = "true")
    public GaugeAxis setBottomTextBold(Boolean bottomTextBold) {
        this.bottomTextBold = bottomTextBold;
        return this;
    }

    /**
     * @return bottom text color
     */
    public Color getBottomTextColor() {
        return bottomTextColor;
    }

    /**
     * Sets bottom text color.
     *
     * @param bottomTextColor color
     * @return gauge axis
     */
    @StudioProperty(type = PropertyType.OPTIONS)
    public GaugeAxis setBottomTextColor(Color bottomTextColor) {
        this.bottomTextColor = bottomTextColor;
        return this;
    }

    /**
     * @return font size of bottom text
     */
    public Integer getBottomTextFontSize() {
        return bottomTextFontSize;
    }

    /**
     * Sets font size of bottom text.
     *
     * @param bottomTextFontSize font size
     * @return gauge axis
     */
    @StudioProperty
    public GaugeAxis setBottomTextFontSize(Integer bottomTextFontSize) {
        this.bottomTextFontSize = bottomTextFontSize;
        return this;
    }

    /**
     * @return Y offset of bottom text
     */
    public Integer getBottomTextYOffset() {
        return bottomTextYOffset;
    }

    /**
     * Sets Y offset of bottom text. If not set the default value is 0.
     *
     * @param bottomTextYOffset Y offset
     * @return gauge axis
     */
    @StudioProperty(defaultValue = "0")
    public GaugeAxis setBottomTextYOffset(Integer bottomTextYOffset) {
        this.bottomTextYOffset = bottomTextYOffset;
        return this;
    }

    /**
     * @return X position of the axis, relative to the center of the gauge
     */
    public String getCenterX() {
        return centerX;
    }

    /**
     * Sets X position of the axis, relative to the center of the gauge. If not set the default value is 0%.
     *
     * @param centerX X position of the axis
     * @return gauge axis
     */
    @StudioProperty(defaultValue = "0%")
    public GaugeAxis setCenterX(String centerX) {
        this.centerX = centerX;
        return this;
    }

    /**
     * @return Y position of the axis, relative to the center of the gauge
     */
    public String getCenterY() {
        return centerY;
    }

    /**
     * Sets Y position of the axis, relative to the center of the gauge. If not set the default value is 0%.
     *
     * @param centerY Y position of the axis
     * @return gauge axis
     */
    @StudioProperty(defaultValue = "0%")
    public GaugeAxis setCenterY(String centerY) {
        this.centerY = centerY;
        return this;
    }

    /**
     * @return axis end angle
     */
    public Integer getEndAngle() {
        return endAngle;
    }

    /**
     * Sets axis end angle. Valid values are from - 180 to 180. If not set the default value is 120.
     *
     * @param endAngle angle
     * @return gauge axis
     */
    @StudioProperty(defaultValue = "120")
    @Max(180)
    @Min(-180)
    public GaugeAxis setEndAngle(Integer endAngle) {
        this.endAngle = endAngle;
        return this;
    }

    /**
     * @return axis end (maximum) value
     */
    public Double getEndValue() {
        return endValue;
    }

    /**
     * Sets axis end (maximum) value.
     *
     * @param endValue end value
     * @return gauge axis
     */
    @StudioProperty
    public GaugeAxis setEndValue(Double endValue) {
        this.endValue = endValue;
        return this;
    }

    /**
     * @return true if grid is drawn inside the axis
     */
    public Boolean getGridInside() {
        return gridInside;
    }

    /**
     * Set gridInside to false if grid shouldn't be drawn inside the axis. If not set the default value is true.
     *
     * @param gridInside gridInside option
     * @return gauge axis
     */
    @StudioProperty(defaultValue = "true")
    public GaugeAxis setGridInside(Boolean gridInside) {
        this.gridInside = gridInside;
        return this;
    }

    /**
     * @return true if labels is placed inside the axis
     */
    public Boolean getInside() {
        return inside;
    }

    /**
     * Set inside to false if labels should be placed outside the axis. If not set the default value is true.
     *
     * @param inside inside option
     * @return gauge axis
     */
    @StudioProperty(defaultValue = "true")
    public GaugeAxis setInside(Boolean inside) {
        this.inside = inside;
        return this;
    }

    /**
     * @return frequency of labels
     */
    public Double getLabelFrequency() {
        return labelFrequency;
    }

    /**
     * Sets frequency of labels. If not set the default value is 1.
     *
     * @param labelFrequency frequency
     * @return gauge axis
     */
    @StudioProperty(defaultValue = "1")
    public GaugeAxis setLabelFrequency(Double labelFrequency) {
        this.labelFrequency = labelFrequency;
        return this;
    }

    /**
     * @return distance from axis to the labels
     */
    public Integer getLabelOffset() {
        return labelOffset;
    }

    /**
     * Sets distance from axis to the labels. If not set the default value is 15.
     *
     * @param labelOffset label offset
     * @return gauge axis
     */
    @StudioProperty(defaultValue = "15")
    public GaugeAxis setLabelOffset(Integer labelOffset) {
        this.labelOffset = labelOffset;
        return this;
    }

    /**
     * @return interval, at which minor ticks is placed
     */
    public Double getMinorTickInterval() {
        return minorTickInterval;
    }

    /**
     * Sets interval, at which minor ticks should be placed.
     *
     * @param minorTickInterval minor tick interval
     * @return gauge axis
     */
    @StudioProperty
    public GaugeAxis setMinorTickInterval(Double minorTickInterval) {
        this.minorTickInterval = minorTickInterval;
        return this;
    }

    /**
     * @return length of a minor tick
     */
    public Integer getMinorTickLength() {
        return minorTickLength;
    }

    /**
     * Sets length of a minor tick. If not set the default value is 5.
     *
     * @param minorTickLength length
     * @return gauge axis
     */
    @StudioProperty(defaultValue = "5")
    public GaugeAxis setMinorTickLength(Integer minorTickLength) {
        this.minorTickLength = minorTickLength;
        return this;
    }

    /**
     * @return axis radius
     */
    public String getRadius() {
        return radius;
    }

    /**
     * Sets axis radius. If not set the default value is 95%.
     *
     * @param radius radius
     * @return gauge axis
     */
    @StudioProperty(defaultValue = "95%")
    public GaugeAxis setRadius(String radius) {
        this.radius = radius;
        return this;
    }

    /**
     * @return true if the first label is shown
     */
    public Boolean getShowFirstLabel() {
        return showFirstLabel;
    }

    /**
     * Set showFirstLabel to false if the first label shouldn't be shown. If not set the default value is true.
     *
     * @param showFirstLabel showFirstLabel option
     * @return gauge axis
     */
    @StudioProperty(defaultValue = "true")
    public GaugeAxis setShowFirstLabel(Boolean showFirstLabel) {
        this.showFirstLabel = showFirstLabel;
        return this;
    }

    /**
     * @return true if the last label is shown
     */
    public Boolean getShowLastLabel() {
        return showLastLabel;
    }

    /**
     * Set showLastLabel to false if the last label shouldn't be shown. If not set the default value is true.
     *
     * @param showLastLabel showLastLabel option
     * @return gauge axis
     */
    @StudioProperty(defaultValue = "true")
    public GaugeAxis setShowLastLabel(Boolean showLastLabel) {
        this.showLastLabel = showLastLabel;
        return this;
    }

    /**
     * @return axis start angle
     */
    public Integer getStartAngle() {
        return startAngle;
    }

    /**
     * Sets axis start angle. Valid values are from - 180 to 180. If not set the default value is -120.
     *
     * @param startAngle start angle
     * @return gauge axis
     */
    @StudioProperty(defaultValue = "-120")
    @Max(180)
    @Min(-180)
    public GaugeAxis setStartAngle(Integer startAngle) {
        this.startAngle = startAngle;
        return this;
    }

    /**
     * @return axis start (minimum) value
     */
    public Double getStartValue() {
        return startValue;
    }

    /**
     * Sets axis start (minimum) value. If not set the default value is 0.
     *
     * @param startValue start value
     * @return gauge axis
     */
    @StudioProperty(defaultValue = "0")
    public GaugeAxis setStartValue(Double startValue) {
        this.startValue = startValue;
        return this;
    }

    /**
     * @return opacity of axis ticks
     */
    public Double getTickAlpha() {
        return tickAlpha;
    }

    /**
     * Sets opacity of axis ticks. If not set the default value is 1.
     *
     * @param tickAlpha opacity
     * @return gauge axis
     */
    @StudioProperty(defaultValue = "1")
    @Max(1)
    @Min(0)
    public GaugeAxis setTickAlpha(Double tickAlpha) {
        this.tickAlpha = tickAlpha;
        return this;
    }

    /**
     * @return color of axis ticks
     */
    public Color getTickColor() {
        return tickColor;
    }

    /**
     * Sets color of axis ticks. If not set the default value is #555555.
     *
     * @param tickColor tick color
     * @return gauge axis
     */
    @StudioProperty(type = PropertyType.OPTIONS, defaultValue = "#555555")
    public GaugeAxis setTickColor(Color tickColor) {
        this.tickColor = tickColor;
        return this;
    }

    /**
     * @return length of a major tick
     */
    public Integer getTickLength() {
        return tickLength;
    }

    /**
     * Sets length of a major tick. If not set the default value is 10.
     *
     * @param tickLength tick length
     * @return gauge axis
     */
    @StudioProperty(defaultValue = "10")
    public GaugeAxis setTickLength(Integer tickLength) {
        this.tickLength = tickLength;
        return this;
    }

    /**
     * @return tick thickness
     */
    public Integer getTickThickness() {
        return tickThickness;
    }

    /**
     * Sets tick thickness. If not set the default value is 1.
     *
     * @param tickThickness tick thickness
     * @return gauge axis
     */
    @StudioProperty(defaultValue = "1")
    public GaugeAxis setTickThickness(Integer tickThickness) {
        this.tickThickness = tickThickness;
        return this;
    }

    /**
     * @return text displayed above the axis center
     */
    public String getTopText() {
        return topText;
    }

    /**
     * Sets text displayed above the axis center.
     *
     * @param topText top text
     * @return gauge axis
     */
    @StudioProperty
    public GaugeAxis setTopText(String topText) {
        this.topText = topText;
        return this;
    }

    /**
     * @return true if top text is bold
     */
    public Boolean getTopTextBold() {
        return topTextBold;
    }

    /**
     * Sets topTextBold to false if text shouldn't be bold. If not set the default value is true.
     *
     * @param topTextBold topTextBold option
     * @return gauge axis
     */
    @StudioProperty(defaultValue = "true")
    public GaugeAxis setTopTextBold(Boolean topTextBold) {
        this.topTextBold = topTextBold;
        return this;
    }

    /**
     * @return color of top text
     */
    public Color getTopTextColor() {
        return topTextColor;
    }

    /**
     * Sets color of top text.
     *
     * @param topTextColor color
     * @return gauge axis
     */
    @StudioProperty(type = PropertyType.OPTIONS)
    public GaugeAxis setTopTextColor(Color topTextColor) {
        this.topTextColor = topTextColor;
        return this;
    }

    /**
     * @return font size of top text
     */
    public Integer getTopTextFontSize() {
        return topTextFontSize;
    }

    /**
     * Sets font size of top text.
     *
     * @param topTextFontSize font size
     * @return gauge axis
     */
    @StudioProperty
    public GaugeAxis setTopTextFontSize(Integer topTextFontSize) {
        this.topTextFontSize = topTextFontSize;
        return this;
    }

    /**
     * @return Y offset of top text
     */
    public Integer getTopTextYOffset() {
        return topTextYOffset;
    }

    /**
     * Sets Y offset of top text. If not set the default value is 0.
     *
     * @param topTextYOffset Y offset
     * @return gauge axis
     */
    @StudioProperty(defaultValue = "0")
    public GaugeAxis setTopTextYOffset(Integer topTextYOffset) {
        this.topTextYOffset = topTextYOffset;
        return this;
    }

    /**
     * @return string which is placed next to axis labels
     */
    public String getUnit() {
        return unit;
    }

    /**
     * Sets a string which can be placed next to axis labels.
     *
     * @param unit unit string
     * @return gauge axis
     */
    @StudioProperty
    public GaugeAxis setUnit(String unit) {
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
     * Sets position of the unit. If not set the default value is RIGHT.
     *
     * @param unitPosition unit position
     * @return gauge axis
     */
    @StudioProperty(type = PropertyType.ENUMERATION, defaultValue = "RIGHT")
    public GaugeAxis setUnitPosition(UnitPosition unitPosition) {
        this.unitPosition = unitPosition;
        return this;
    }

    /**
     * @return value interval
     */
    public Integer getValueInterval() {
        return valueInterval;
    }

    /**
     * Sets interval, at which ticks with values should be placed.
     *
     * @param valueInterval value interval
     * @return gauge axis
     */
    @StudioProperty
    public GaugeAxis setValueInterval(Integer valueInterval) {
        this.valueInterval = valueInterval;
        return this;
    }

    /**
     * @return list of bands
     */
    public List<GaugeBand> getBands() {
        return bands;
    }

    /**
     * Sets list of bands.
     *
     * @param bands list of bands
     * @return gauge axis
     */
    @StudioElementsGroup(caption = "Bands", xmlElement = "bands")
    public GaugeAxis setBands(List<GaugeBand> bands) {
        this.bands = bands;
        return this;
    }

    /**
     * Adds bands.
     *
     * @param bands bands
     * @return gauge axis
     */
    public GaugeAxis addBands(GaugeBand... bands) {
        if (bands != null) {
            if (this.bands == null) {
                this.bands = new ArrayList<>();
            }
            this.bands.addAll(Arrays.asList(bands));
        }
        return this;
    }

    /**
     * @return number of grid lines
     */
    public Integer getGridCount() {
        return gridCount;
    }

    /**
     * Sets number of grid lines. Note, GaugeAxis doesn't adjust gridCount, so you should check your values and
     * choose a proper gridCount which would result grids at round numbers. If not set the default value is 5.
     *
     * @param gridCount number of grid lines
     * @return gauge axis
     */
    @StudioProperty(defaultValue = "5")
    public GaugeAxis setGridCount(Integer gridCount) {
        this.gridCount = gridCount;
        return this;
    }

    /**
     * @return unique id of an axis
     */
    public String getId() {
        return id;
    }

    /**
     * Sets unique id of an axis.
     *
     * @param id id
     * @return gauge axis
     */
    @StudioProperty
    public GaugeAxis setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * @return true if labels on the axis is shown
     */
    public Boolean getLabelsEnabled() {
        return labelsEnabled;
    }

    /**
     * Sets labelsEnabled to false if labels on the axis shouldn't be shown. If not set the default value is true.
     *
     * @param labelsEnabled labelsEnabled option
     * @return gauge axis
     */
    @StudioProperty(defaultValue = "true")
    public GaugeAxis setLabelsEnabled(Boolean labelsEnabled) {
        this.labelsEnabled = labelsEnabled;
        return this;
    }

    /**
     * @return if small and big numbers use prefixes
     */
    public Boolean getUsePrefixes() {
        return usePrefixes;
    }

    /**
     * Set usePrefixes to true if small and big numbers should use prefixes to make them more readable. If not set
     * the default value is false.
     *
     * @param usePrefixes usePrefixes option
     * @return gauge axis
     */
    @StudioProperty(defaultValue = "false")
    public GaugeAxis setUsePrefixes(Boolean usePrefixes) {
        this.usePrefixes = usePrefixes;
        return this;
    }

    /**
     * @return JS function to format axis labels
     */
    public JsFunction getLabelFunction() {
        return labelFunction;
    }

    /**
     * Sets JS function to format axis labels. This function is called and value is passed as a attribute:
     * labelFunction(value);
     *
     * @param labelFunction JS function
     * @return gauge axis
     */
    public GaugeAxis setLabelFunction(JsFunction labelFunction) {
        this.labelFunction = labelFunction;
        return this;
    }

    /**
     * @return labels color of the axis
     */
    public Color getColor() {
        return color;
    }

    /**
     * Sets labels color of the axis.
     *
     * @param color color
     * @return gauge axis
     */
    @StudioProperty(type = PropertyType.OPTIONS)
    public GaugeAxis setColor(Color color) {
        this.color = color;
        return this;
    }

    /**
     * @return font size for axis labels
     */
    public Integer getFontSize() {
        return fontSize;
    }

    /**
     * Sets font size for axis labels.
     *
     * @param fontSize font size
     * @return gauge axis
     */
    @StudioProperty
    public GaugeAxis setFontSize(Integer fontSize) {
        this.fontSize = fontSize;
        return this;
    }

    /**
     * @return list gradient ratios
     */
    public List<Float> getBandGradientRatio() {
        return bandGradientRatio;
    }

    /**
     * Sets list of gradient ratio. Will make bands to be filled with color gradients. Negative value means the color
     * will be darker than the original, and positive number means the color will be lighter.
     *
     * @param bandGradientRatio list gradient ratios
     * @return gauge axis
     */
    @StudioProperty(type = PropertyType.STRING)
    public GaugeAxis setBandGradientRatio(List<Float> bandGradientRatio) {
        this.bandGradientRatio = bandGradientRatio;
        return this;
    }
}