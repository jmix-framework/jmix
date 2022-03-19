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

import io.jmix.charts.model.*;
import io.jmix.charts.model.stock.StockLegend;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioElement;
import io.jmix.ui.meta.StudioProperty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * Common settings of legends. If there is no default value specified, default value of {@link StockLegend} class
 * will be used.
 * <br>
 * See documentation for properties of LegendSettings JS object. <br>
 *
 * <a href="http://docs.amcharts.com/3/javascriptstockchart/LegendSettings">http://docs.amcharts.com/3/javascriptstockchart/LegendSettings</a>
 */
@StudioElement(
        caption = "LegendSettings",
        xmlElement = "legendSettings",
        xmlns = "http://jmix.io/schema/ui/charts",
        xmlnsAlias = "chart")
public class LegendSettings extends AbstractChartObject {

    private static final long serialVersionUID = 7958786618547667346L;

    private Align align;

    private Boolean equalWidths;

    private Integer horizontalGap;

    private String labelText;

    private Integer marginBottom;

    private Integer marginTop;

    private Double markerBorderAlpha;

    private Color markerBorderColor;

    private Integer markerBorderThickness;

    private Color markerDisabledColor;

    private Integer markerLabelGap;

    private Integer markerSize;

    private MarkerType markerType;

    private LegendSettingsPosition position;

    private Boolean reversedOrder;

    private Color rollOverColor;

    private Double rollOverGraphAlpha;

    private Boolean switchable;

    private Color switchColor;

    private SwitchType switchType;

    private Boolean textClickEnabled;

    private Boolean useMarkerColorForLabels;

    private String valueTextComparing;

    private String valueTextRegular;

    private Integer valueWidth;

    private Integer verticalGap;

    /**
     * @return alignment of legend entries
     */
    public Align getAlign() {
        return align;
    }

    /**
     * Sets alignment of legend entries. Possible values are: "left", "right" and "center".
     *
     * @param align alignment of legend entries
     * @return legend settings
     */
    @StudioProperty(type = PropertyType.ENUMERATION)
    public LegendSettings setAlign(Align align) {
        this.align = align;
        return this;
    }

    /**
     * @return true if each legend entry takes the same space as the longest legend entry
     */
    public Boolean getEqualWidths() {
        return equalWidths;
    }

    /**
     * Set equalWidths to true if each legend entry should take up much space as the longest legend entry. If not set
     * the default value is false.
     *
     * @param equalWidths equalWidths option
     * @return legend settings
     */
    @StudioProperty(defaultValue = "false")
    public LegendSettings setEqualWidths(Boolean equalWidths) {
        this.equalWidths = equalWidths;
        return this;
    }

    /**
     * @return horizontal space between legend item and left/right border
     */
    public Integer getHorizontalGap() {
        return horizontalGap;
    }

    /**
     * Sets horizontal space between legend item and left/right border.
     *
     * @param horizontalGap horizontal gap
     * @return legend settings
     */
    @StudioProperty
    public LegendSettings setHorizontalGap(Integer horizontalGap) {
        this.horizontalGap = horizontalGap;
        return this;
    }

    /**
     * @return label text
     */
    public String getLabelText() {
        return labelText;
    }

    /**
     * Sets the text which will be displayed in the legend. Tag [[title]] will be replaced with the title of the graph.
     *
     * @param labelText text
     * @return legend settings
     */
    @StudioProperty
    public LegendSettings setLabelText(String labelText) {
        this.labelText = labelText;
        return this;
    }

    /**
     * @return space below the last row of the legend, in pixels
     */
    public Integer getMarginBottom() {
        return marginBottom;
    }

    /**
     * Sets space below the last row of the legend, in pixels. If not set the default value is 0.
     *
     * @param marginBottom margin bottom
     * @return legend settings
     */
    @StudioProperty(defaultValue = "0")
    public LegendSettings setMarginBottom(Integer marginBottom) {
        this.marginBottom = marginBottom;
        return this;
    }

    /**
     * @return space above the first row of the legend, in pixels
     */
    public Integer getMarginTop() {
        return marginTop;
    }

    /**
     * Sets space above the first row of the legend, in pixels. If not set the default value is 0.
     *
     * @param marginTop margin top
     * @return legend settings
     */
    @StudioProperty(defaultValue = "0")
    public LegendSettings setMarginTop(Integer marginTop) {
        this.marginTop = marginTop;
        return this;
    }

    /**
     * @return opacity of marker border
     */
    public Double getMarkerBorderAlpha() {
        return markerBorderAlpha;
    }

    /**
     * Sets opacity of marker border.
     *
     * @param markerBorderAlpha opacity
     * @return legend settings
     */
    @StudioProperty
    @Max(1)
    @Min(0)
    public LegendSettings setMarkerBorderAlpha(Double markerBorderAlpha) {
        this.markerBorderAlpha = markerBorderAlpha;
        return this;
    }

    /**
     * @return marker border color
     */
    public Color getMarkerBorderColor() {
        return markerBorderColor;
    }

    /**
     * Sets marker border color.
     *
     * @param markerBorderColor color
     * @return legend settings
     */
    @StudioProperty(type = PropertyType.OPTIONS)
    public LegendSettings setMarkerBorderColor(Color markerBorderColor) {
        this.markerBorderColor = markerBorderColor;
        return this;
    }

    /**
     * @return thickness of the legend border
     */
    public Integer getMarkerBorderThickness() {
        return markerBorderThickness;
    }

    /**
     * Sets thickness of the legend border.
     *
     * @param markerBorderThickness thickness
     * @return legend settings
     */
    @StudioProperty
    public LegendSettings setMarkerBorderThickness(Integer markerBorderThickness) {
        this.markerBorderThickness = markerBorderThickness;
        return this;
    }

    /**
     * @return color of the disabled marker
     */
    public Color getMarkerDisabledColor() {
        return markerDisabledColor;
    }

    /**
     * Sets color of the disabled marker (when the graph is hidden).
     *
     * @param markerDisabledColor color
     * @return legend settings
     */
    @StudioProperty(type = PropertyType.OPTIONS)
    public LegendSettings setMarkerDisabledColor(Color markerDisabledColor) {
        this.markerDisabledColor = markerDisabledColor;
        return this;
    }

    /**
     * @return space between legend marker and legend text, in pixels
     */
    public Integer getMarkerLabelGap() {
        return markerLabelGap;
    }

    /**
     * Sets space between legend marker and legend text, in pixels.
     *
     * @param markerLabelGap marker label gap
     * @return legend settings
     */
    @StudioProperty
    public LegendSettings setMarkerLabelGap(Integer markerLabelGap) {
        this.markerLabelGap = markerLabelGap;
        return this;
    }

    /**
     * @return size of the legend marker
     */
    public Integer getMarkerSize() {
        return markerSize;
    }

    /**
     * Sets size of the legend marker (key).
     *
     * @param markerSize marker size
     * @return legend settings
     */
    @StudioProperty
    public LegendSettings setMarkerSize(Integer markerSize) {
        this.markerSize = markerSize;
        return this;
    }

    /**
     * @return legend marker type
     */
    public MarkerType getMarkerType() {
        return markerType;
    }

    /**
     * Sets shape of the legend marker (key). Possible values are: square, circle, diamond, triangleUp, triangleDown,
     * triangleLeft, triangleDown, bubble, none.
     *
     * @param markerType marker type
     * @return legend settings
     */
    @StudioProperty(type = PropertyType.ENUMERATION)
    public LegendSettings setMarkerType(MarkerType markerType) {
        this.markerType = markerType;
        return this;
    }

    /**
     * @return true if legend entries placed in reversed order
     */
    public Boolean getReversedOrder() {
        return reversedOrder;
    }

    /**
     * Set reversedOrder to true if legend entries should be placed in reversed order.
     *
     * @param reversedOrder reversedOrder option
     * @return legend settings
     */
    @StudioProperty
    public LegendSettings setReversedOrder(Boolean reversedOrder) {
        this.reversedOrder = reversedOrder;
        return this;
    }

    /**
     * @return legend item text color on roll-over
     */
    public Color getRollOverColor() {
        return rollOverColor;
    }

    /**
     * Sets legend item text color on roll-over.
     *
     * @param rollOverColor color
     * @return legend settings
     */
    @StudioProperty(type = PropertyType.OPTIONS)
    public LegendSettings setRollOverColor(Color rollOverColor) {
        this.rollOverColor = rollOverColor;
        return this;
    }

    /**
     * @return opacity of the graphs
     */
    public Double getRollOverGraphAlpha() {
        return rollOverGraphAlpha;
    }

    /**
     * Sets the opacity of the graphs. When you roll-over the legend entry, all other graphs can reduce their
     * opacity, so that the graph you rolled-over would be distinguished.
     *
     * @param rollOverGraphAlpha opacity
     * @return legend settings
     */
    @StudioProperty
    public LegendSettings setRollOverGraphAlpha(Double rollOverGraphAlpha) {
        this.rollOverGraphAlpha = rollOverGraphAlpha;
        return this;
    }

    /**
     * @return true if graphs are shown/hidden by click on the legend marker
     */
    public Boolean getSwitchable() {
        return switchable;
    }

    /**
     * Set switchable to false if graphs shouldn't be shown/hidden by click on the legend marker.
     *
     * @param switchable switchable option
     * @return legend settings
     */
    @StudioProperty
    public LegendSettings setSwitchable(Boolean switchable) {
        this.switchable = switchable;
        return this;
    }

    /**
     * @return legend switch color
     */
    public Color getSwitchColor() {
        return switchColor;
    }

    /**
     * Sets legend switch color.
     *
     * @param switchColor color
     * @return legend settings
     */
    @StudioProperty(type = PropertyType.OPTIONS)
    public LegendSettings setSwitchColor(Color switchColor) {
        this.switchColor = switchColor;
        return this;
    }

    /**
     * @return switch type
     */
    public SwitchType getSwitchType() {
        return switchType;
    }

    /**
     * Sets legend switch type (in case the legend is switchable). Possible values are: "x" and "v".
     *
     * @param switchType switch type
     * @return legend settings
     */
    @StudioProperty(type = PropertyType.ENUMERATION)
    public LegendSettings setSwitchType(SwitchType switchType) {
        this.switchType = switchType;
        return this;
    }

    /**
     * @return true if the legend text is clickable
     */
    public Boolean getTextClickEnabled() {
        return textClickEnabled;
    }

    /**
     * Set textClickEnabled to true if the legend text should be clickable. Clicking on legend text can show/hide
     * value balloons if they are enabled. If not set the default value is false.
     *
     * @param textClickEnabled textClickEnabled option
     * @return legend settings
     */
    @StudioProperty(defaultValue = "false")
    public LegendSettings setTextClickEnabled(Boolean textClickEnabled) {
        this.textClickEnabled = textClickEnabled;
        return this;
    }

    /**
     * @return true if legend labels use the same color as corresponding markers
     */
    public Boolean getUseMarkerColorForLabels() {
        return useMarkerColorForLabels;
    }

    /**
     * Set useMarkerColorForLabels to true if legend labels should use the same color as corresponding markers.
     *
     * @param useMarkerColorForLabels useMarkerColorForLabels option
     * @return legend settings
     */
    @StudioProperty
    public LegendSettings setUseMarkerColorForLabels(Boolean useMarkerColorForLabels) {
        this.useMarkerColorForLabels = useMarkerColorForLabels;
        return this;
    }

    /**
     * @return value text comparing
     */
    public String getValueTextComparing() {
        return valueTextComparing;
    }

    /**
     * Sets the text which will be displayed in the value portion of the legend when graph is comparable and at least
     * one dataSet is selected for comparing. You can use tags like [[value]], [[open]], [[high]], [[low]], [[close]],
     * [[percents]], [[description]].
     *
     * @param valueTextComparing value text comparing
     * @return legend settings
     */
    @StudioProperty
    public LegendSettings setValueTextComparing(String valueTextComparing) {
        this.valueTextComparing = valueTextComparing;
        return this;
    }

    /**
     * @return value text regular
     */
    public String getValueTextRegular() {
        return valueTextRegular;
    }

    /**
     * Sets the text which will be displayed in the value portion of the legend. You can use tags like [[value]],
     * [[open]], [[high]], [[low]], [[close]], [[percents]], [[description]].
     *
     * @param valueTextRegular value text regular
     * @return legend settings
     */
    @StudioProperty
    public LegendSettings setValueTextRegular(String valueTextRegular) {
        this.valueTextRegular = valueTextRegular;
        return this;
    }

    /**
     * @return width of the value text
     */
    public Integer getValueWidth() {
        return valueWidth;
    }

    /**
     * Sets width of the value text. Increase this value if your values do not fit in the allocated space.
     *
     * @param valueWidth width
     * @return legend settings
     */
    @StudioProperty
    public LegendSettings setValueWidth(Integer valueWidth) {
        this.valueWidth = valueWidth;
        return this;
    }

    /**
     * @return vertical space between legend items, in pixels
     */
    public Integer getVerticalGap() {
        return verticalGap;
    }

    /**
     * Sets vertical space between legend items, in pixels.
     *
     * @param verticalGap vertical gap
     * @return legend settings
     */
    @StudioProperty
    public LegendSettings setVerticalGap(Integer verticalGap) {
        this.verticalGap = verticalGap;
        return this;
    }

    /**
     * @return position of legend in panels
     */
    public LegendSettingsPosition getPosition() {
        return position;
    }

    /**
     * Sets position of legend in panels. Possible values are: "bottom" and "top".
     *
     * @param position position
     * @return legend settings
     */
    @StudioProperty(type = PropertyType.ENUMERATION)
    public LegendSettings setPosition(LegendSettingsPosition position) {
        this.position = position;
        return this;
    }
}
