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

package io.jmix.charts.model.legend;

import io.jmix.charts.model.*;
import io.jmix.charts.model.cursor.Cursor;
import io.jmix.charts.model.label.Label;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Defines the legend for the chart.
 * <br>
 * See documentation for properties of AmLegend JS Object. <br>
 *
 * <a href="http://docs.amcharts.com/3/javascriptcharts/AmLegend">http://docs.amcharts.com/3/javascriptcharts/AmLegend</a>
 */
@SuppressWarnings("unchecked")
public class AbstractLegend<T extends AbstractLegend> extends AbstractChartObject implements HasMargins<AbstractLegend> {

    private static final long serialVersionUID = -8561508956306637129L;

    private String accessibleLabel;

    private Align align;

    private Boolean autoMargins;

    private Double backgroundAlpha;

    private Color backgroundColor;

    private Double borderAlpha;

    private Color borderColor;

    private Integer bottom;

    private Color color;

    private List<LegendItem> data;

    private String divId;

    private Boolean enabled;

    private Boolean equalWidths;

    private Integer fontSize;

    private Boolean forceWidth;

    private Integer gradientRotation;

    private Integer horizontalGap;

    private String labelText;

    private Integer labelWidth;

    private Integer left;

    private Integer marginBottom;

    private Integer marginLeft;

    private Integer marginRight;

    private Integer marginTop;

    private Double markerBorderAlpha;

    private Color markerBorderColor;

    private Integer markerBorderThickness;

    private Color markerDisabledColor;

    private Integer markerLabelGap;

    private Integer markerSize;

    private MarkerType markerType;

    private Integer maxColumns;

    private String periodValueText;

    private LegendPosition position;

    private Boolean reversedOrder;

    private Integer right;

    private Color rollOverColor;

    private Double rollOverGraphAlpha;

    private Boolean showEntries;

    private Integer spacing;

    private Boolean switchable;

    private Color switchColor;

    private LegendSwitch switchType;

    private Integer tabIndex;

    private Boolean textClickEnabled;

    private Integer top;

    private Boolean useGraphSettings;

    private Boolean useMarkerColorForLabels;

    private Boolean useMarkerColorForValues;

    private ValueAlign valueAlign;

    private JsFunction valueFunction;

    private String valueText;

    private Integer valueWidth;

    private Integer verticalGap;

    private Integer width;

    private Boolean combineLegend;

    /**
     * @return alignment of legend entries
     */
    public Align getAlign() {
        return align;
    }

    /**
     * Sets alignment of legend entries. Possible values are: "left", "center", "right". If not set the default value is
     * LEFT.
     *
     * @param align align
     */
    public T setAlign(Align align) {
        this.align = align;
        return (T) this;
    }

    /**
     * @return true if margins of the legend is adjusted and made equal to chart's margins
     */
    public Boolean getAutoMargins() {
        return autoMargins;
    }

    /**
     * Set autoMargins to true if you want margins of the legend should be adjusted and made equal to chart's margins.
     * Used if chart is Serial or XY. If not set the default value is true.
     *
     * @param autoMargins autoMargins option
     */
    public T setAutoMargins(Boolean autoMargins) {
        this.autoMargins = autoMargins;
        return (T) this;
    }

    /**
     * @return opacity of legend's background
     */
    public Double getBackgroundAlpha() {
        return backgroundAlpha;
    }

    /**
     * Sets opacity of legend's background. Value range is 0 - 1. If not set the default value is 0.
     *
     * @param backgroundAlpha opacity
     */
    public T setBackgroundAlpha(Double backgroundAlpha) {
        this.backgroundAlpha = backgroundAlpha;
        return (T) this;
    }

    /**
     * @return background color
     */
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Sets background color. You should set backgroundAlpha to greater than 0 value in order background to be visible.
     * If not set the default value is #FFFFFF.
     *
     * @param backgroundColor background color
     */
    public T setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        return (T) this;
    }

    /**
     * @return opacity of chart's border
     */
    public Double getBorderAlpha() {
        return borderAlpha;
    }

    /**
     * Sets opacity of chart's border. Value range is 0 - 1. If not set the default value is 0.
     *
     * @param borderAlpha opacity
     */
    public T setBorderAlpha(Double borderAlpha) {
        this.borderAlpha = borderAlpha;
        return (T) this;
    }

    /**
     * @return color of legend's border
     */
    public Color getBorderColor() {
        return borderColor;
    }

    /**
     * Sets color of legend's border. You should set borderAlpha to greater than 0 in order border to be visible. If
     * not set the default value is #000000.
     *
     * @param borderColor border color
     */
    public T setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
        return (T) this;
    }

    /**
     * @return distance from bottom of the chart, in pixels
     */
    public Integer getBottom() {
        return bottom;
    }

    /**
     * Sets distance from bottom of the chart, in pixels if legend position is set to "absolute".
     *
     * @param bottom bottom
     */
    public T setBottom(Integer bottom) {
        this.bottom = bottom;
        return (T) this;
    }

    /**
     * @return text color
     */
    public Color getColor() {
        return color;
    }

    /**
     * Sets text color. If not set the default value is #000000.
     *
     * @param color text color
     */
    public T setColor(Color color) {
        this.color = color;
        return (T) this;
    }

    /**
     * @return list of legend items
     */
    public List<LegendItem> getData() {
        return data;
    }

    /**
     * Sets list of legend items.
     *
     * @param data list of legend items
     */
    public T setData(List<LegendItem> data) {
        this.data = data;
        return (T) this;
    }

    /**
     * Adds items.
     *
     * @param items items
     */
    public T addItems(LegendItem... items) {
        if (items != null) {
            if (this.data == null) {
                this.data = new ArrayList<>();
            }
            this.data.addAll(Arrays.asList(items));
        }
        return (T) this;
    }

    /**
     * @return true if each of legend entry is equal to the most wide entry
     */
    public Boolean getEqualWidths() {
        return equalWidths;
    }

    /**
     * Set equalWidths to false if each of legend entry shouldn't be equal to the most widest entry. Won't look good
     * if legend has more than one line. If not set the default value is true.
     *
     * @param equalWidths equals widths option
     */
    public T setEqualWidths(Boolean equalWidths) {
        this.equalWidths = equalWidths;
        return (T) this;
    }

    /**
     * @return font size.
     */
    public Integer getFontSize() {
        return fontSize;
    }

    /**
     * Sets font size. If not set the default value is 11.
     *
     * @param fontSize font size
     */
    public T setFontSize(Integer fontSize) {
        this.fontSize = fontSize;
        return (T) this;
    }

    /**
     * @return horizontal space between legend item and left/right border
     */
    public Integer getHorizontalGap() {
        return horizontalGap;
    }

    /**
     * Sets horizontal space between legend item and left/right border. If not set the default value is 0.
     *
     * @param horizontalGap horizontal gap
     */
    public T setHorizontalGap(Integer horizontalGap) {
        this.horizontalGap = horizontalGap;
        return (T) this;
    }

    /**
     * @return the text which will be displayed in the legend
     */
    public String getLabelText() {
        return labelText;
    }

    /**
     * Sets the text which will be displayed in the legend. Tag [[title]] will be replaced with the title of the graph.
     * If not set the default value is "[[title]]".
     *
     * @param labelText label text
     */
    public T setLabelText(String labelText) {
        this.labelText = labelText;
        return (T) this;
    }

    /**
     * @return distance from left side of the chart, in pixels
     */
    public Integer getLeft() {
        return left;
    }

    /**
     * Sets distance from left side of the chart, in pixels. In case legend position is set to "absolute".
     *
     * @param left distance from left side of the chart, in pixels
     */
    public T setLeft(Integer left) {
        this.left = left;
        return (T) this;
    }

    @Override
    public Integer getMarginBottom() {
        return marginBottom;
    }

    @Override
    public T setMarginBottom(Integer marginBottom) {
        this.marginBottom = marginBottom;
        return (T) this;
    }

    @Override
    public Integer getMarginLeft() {
        return marginLeft;
    }

    @Override
    public T setMarginLeft(Integer marginLeft) {
        this.marginLeft = marginLeft;
        return (T) this;
    }

    @Override
    public Integer getMarginRight() {
        return marginRight;
    }

    @Override
    public T setMarginRight(Integer marginRight) {
        this.marginRight = marginRight;
        return (T) this;
    }

    @Override
    public Integer getMarginTop() {
        return marginTop;
    }

    @Override
    public T setMarginTop(Integer marginTop) {
        this.marginTop = marginTop;
        return (T) this;
    }

    /**
     * @return marker border opacity
     */
    public Double getMarkerBorderAlpha() {
        return markerBorderAlpha;
    }

    /**
     * 	Sets marker border opacity. If not set the default value is 1.
     *
     * 	@param markerBorderAlpha marker border opacity
     */
    public T setMarkerBorderAlpha(Double markerBorderAlpha) {
        this.markerBorderAlpha = markerBorderAlpha;
        return (T) this;
    }

    /**
     * @return marker border color
     */
    public Color getMarkerBorderColor() {
        return markerBorderColor;
    }

    /**
     * Sets marker border color. If not set, will use the same color as marker.
     *
     * @param markerBorderColor marker border color
     */
    public T setMarkerBorderColor(Color markerBorderColor) {
        this.markerBorderColor = markerBorderColor;
        return (T) this;
    }

    /**
     * @return thickness of the legend border
     */
    public Integer getMarkerBorderThickness() {
        return markerBorderThickness;
    }

    /**
     * Sets thickness of the legend border. The value 0 means the line will be a "hairline" (1 px). In case marker
     * type is line, this style will be used for line thickness. If not set the default value is 1.
     *
     * @param markerBorderThickness marker border thickness
     */
    public T setMarkerBorderThickness(Integer markerBorderThickness) {
        this.markerBorderThickness = markerBorderThickness;
        return (T) this;
    }

    /**
     * @return color of the disabled marker (when the graph is hidden)
     */
    public Color getMarkerDisabledColor() {
        return markerDisabledColor;
    }

    /**
     * Sets the color of the disabled marker (when the graph is hidden). If not set the default value is #AAB3B3.
     *
     * @param markerDisabledColor marker disabled color
     */
    public T setMarkerDisabledColor(Color markerDisabledColor) {
        this.markerDisabledColor = markerDisabledColor;
        return (T) this;
    }

    /**
     * @return space between legend marker and legend text, in pixels
     */
    public Integer getMarkerLabelGap() {
        return markerLabelGap;
    }

    /**
     * Sets space between legend marker and legend text, in pixels. If not set the default value is 5.
     *
     * @param markerLabelGap marker label gap
     */
    public T setMarkerLabelGap(Integer markerLabelGap) {
        this.markerLabelGap = markerLabelGap;
        return (T) this;
    }

    /**
     * @return size of the legend marker
     */
    public Integer getMarkerSize() {
        return markerSize;
    }

    /**
     * Sets size of the legend marker (key). If not set the default value is 16.
     *
     * @param markerSize marker size
     */
    public T setMarkerSize(Integer markerSize) {
        this.markerSize = markerSize;
        return (T) this;
    }

    /**
     * @return shape of the legend marker (key)
     */
    public MarkerType getMarkerType() {
        return markerType;
    }

    /**
     * Sets shape of the legend marker (key). Possible values are: square, circle, diamond, triangleUp, triangleDown,
     * triangleLeft, triangleDown, bubble, line, none. If not set the default value is SQUARE.
     *
     * @param markerType marker type
     */
    public T setMarkerType(MarkerType markerType) {
        this.markerType = markerType;
        return (T) this;
    }

    /**
     * @return maximum number of columns in the legend
     */
    public Integer getMaxColumns() {
        return maxColumns;
    }

    /**
     * Sets maximum number of columns in the legend. If Legend's position is set to "right" or "left", maxColumns is
     * automatically set to 1.
     *
     * @param maxColumns maximum number of columns
     */
    public T setMaxColumns(Integer maxColumns) {
        this.maxColumns = maxColumns;
        return (T) this;
    }

    /**
     * @return text which will be displayed in the value portion of the legend when user is not hovering above any
     * data point
     */
    public String getPeriodValueText() {
        return periodValueText;
    }

    /**
     * Sets the text which will be displayed in the value portion of the legend when user is not hovering above any
     * data point. The tags should be made out of two parts - the name of a field (value / open / close / high / low)
     * and the value of the period you want to be show - open / close / high / low / sum / average / count. For example:
     * [[value.sum]] means that sum of all data points of value field in the selected period will be displayed.
     *
     * @param periodValueText period value text
     */
    public T setPeriodValueText(String periodValueText) {
        this.periodValueText = periodValueText;
        return (T) this;
    }

    /**
     * @return position of a legend
     */
    public LegendPosition getPosition() {
        return position;
    }

    /**
     * Sets position of a legend. Possible values are: "bottom", "top", "left", "right" and "absolute". In case
     * "absolute", you should set left and top properties too. If not set the default value is BOTTOM.
     *
     * @param position position
     */
    public T setPosition(LegendPosition position) {
        this.position = position;
        return (T) this;
    }

    /**
     * @return true if legend entries placed in reverse order
     */
    public Boolean getReversedOrder() {
        return reversedOrder;
    }

    /**
     * Set reversedOrder to true if legend entries should be placed in reversed order. If not set the default value
     * is false.
     *
     * @param reversedOrder reversed order option
     */
    public T setReversedOrder(Boolean reversedOrder) {
        this.reversedOrder = reversedOrder;
        return (T) this;
    }

    /**
     * @return distance from right side of the chart, in pixels
     */
    public Integer getRight() {
        return right;
    }

    /**
     * Sets distance from right side of the chart, in pixels if legend position is set to "absolute".
     *
     * @param right distance from right side of the chart, in pixels
     */
    public T setRight(Integer right) {
        this.right = right;
        return (T) this;
    }

    /**
     * @return legend item text color on roll-over
     */
    public Color getRollOverColor() {
        return rollOverColor;
    }

    /**
     * Sets legend item text color on roll-over. If not set the default value is #CC0000.
     *
     * @param rollOverColor roll over color
     */
    public T setRollOverColor(Color rollOverColor) {
        this.rollOverColor = rollOverColor;
        return (T) this;
    }

    /**
     * @return opacity of the non-hovered graphs
     */
    public Double getRollOverGraphAlpha() {
        return rollOverGraphAlpha;
    }

    /**
     * Sets the opacity of the non-hovered graphs. When you roll-over the legend entry, all other graphs can reduce
     * their opacity, so that the graph you rolled-over would be distinguished. If not set the default value is 1.
     *
     * @param rollOverGraphAlpha roll over graph alpha
     */
    public T setRollOverGraphAlpha(Double rollOverGraphAlpha) {
        this.rollOverGraphAlpha = rollOverGraphAlpha;
        return (T) this;
    }

    /**
     * @return true if all the legend entries are shown
     */
    public Boolean getShowEntries() {
        return showEntries;
    }

    /**
     * Set showEntries to false if you want to turn all the legend entries off. If not set the default value is true.
     *
     * @param showEntries showEntries option
     */
    public T setShowEntries(Boolean showEntries) {
        this.showEntries = showEntries;
        return (T) this;
    }

    /**
     * @return horizontal space between legend items, in pixels
     */
    public Integer getSpacing() {
        return spacing;
    }

    /**
     * Sets horizontal space between legend items, in pixels. If not set the default value is 10.
     *
     * @param spacing horizontal space between legend items, in pixels
     */
    public T setSpacing(Integer spacing) {
        this.spacing = spacing;
        return (T) this;
    }

    /**
     * @return true if graphs are shown/hidden by click on the legend marker
     */
    public Boolean getSwitchable() {
        return switchable;
    }

    /**
     * Set switchable to true if graphs should be shown/hidden by clicking on the legend marker. If not set the
     * default value is true.
     *
     * @param switchable switchable option
     */
    public T setSwitchable(Boolean switchable) {
        this.switchable = switchable;
        return (T) this;
    }

    /**
     * @return legend switch color.
     */
    public Color getSwitchColor() {
        return switchColor;
    }

    /**
     * Sets legend switch color. If not set the default value is #FFFFFF
     *
     * @param switchColor switch color
     */
    public T setSwitchColor(Color switchColor) {
        this.switchColor = switchColor;
        return (T) this;
    }

    /**
     * @return legend switch type
     */
    public LegendSwitch getSwitchType() {
        return switchType;
    }

    /**
     * Sets legend switch type (in case the legend is switchable). Possible values are "x" and "v". If not set the
     * default value is "x".
     *
     * @param switchType legend switch type
     */
    public T setSwitchType(LegendSwitch switchType) {
        this.switchType = switchType;
        return (T) this;
    }

    /**
     * @return true if balloon of the graph is shown/hidden by click on the text
     */
    public Boolean getTextClickEnabled() {
        return textClickEnabled;
    }

    /**
     * Set textClickEnabled to true if you want show/hide balloon of the graph by click on the text. Otherwise it
     * will show/hide graph/slice, if switchable is set to true. If not set the default value is false.
     */
    public T setTextClickEnabled(Boolean textClickEnabled) {
        this.textClickEnabled = textClickEnabled;
        return (T) this;
    }

    /**
     * @return distance from top of the chart
     */
    public Integer getTop() {
        return top;
    }

    /**
     * Sets distance from top of the chart, in pixels, if legend position is set to "absolute".
     *
     * @param top distance from top of the chart, in pixels
     */
    public T setTop(Integer top) {
        this.top = top;
        return (T) this;
    }

    /**
     * @return true if graph’s settings is used
     */
    public Boolean getUseGraphSettings() {
        return useGraphSettings;
    }

    /**
     * Set useGraphSettings to true if you want to use graph’s settings. Legend markers can mirror graph’s settings,
     * displaying a line and a real bullet as in the graph itself. Note, if you set graph colors in data provider,
     * they will not be reflected in the marker. If not set the default value is false.
     *
     * @param useGraphSettings use graph settings option
     */
    public T setUseGraphSettings(Boolean useGraphSettings) {
        this.useGraphSettings = useGraphSettings;
        return (T) this;
    }

    /**
     * @return true if {@link Label labels} use marker color
     */
    public Boolean getUseMarkerColorForLabels() {
        return useMarkerColorForLabels;
    }

    /**
     * Set useMarkerColorForLabels to true if you want {@link Label labels} should use marker color. If not set the
     * default value is false.
     *
     * @param useMarkerColorForLabels useMarkerColorForLabels option
     */
    public T setUseMarkerColorForLabels(Boolean useMarkerColorForLabels) {
        this.useMarkerColorForLabels = useMarkerColorForLabels;
        return (T) this;
    }

    /**
     * @return true if legend values use the same color as corresponding markers
     */
    public Boolean getUseMarkerColorForValues() {
        return useMarkerColorForValues;
    }

    /**
     * Set useMarkerColorForValues to true if legend values should use the same color as corresponding markers. If
     * not set the default value is false.
     *
     * @param useMarkerColorForValues useMarkerColorForValues option
     */
    public T setUseMarkerColorForValues(Boolean useMarkerColorForValues) {
        this.useMarkerColorForValues = useMarkerColorForValues;
        return (T) this;
    }

    /**
     * @return alignment of the value text
     */
    public ValueAlign getValueAlign() {
        return valueAlign;
    }

    /**
     * Sets alignment of the value text. Possible values are "left" and "right". If not set the default value is RIGHT.
     *
     * @param valueAlign alignment of the value text
     */
    public T setValueAlign(ValueAlign valueAlign) {
        this.valueAlign = valueAlign;
        return (T) this;
    }

    /**
     * @return text which is displayed in the value portion of the legend
     */
    public String getValueText() {
        return valueText;
    }

    /**
     * Sets the text which will be displayed in the value portion of the legend. You can use tags like [[value]],
     * [[open]], [[high]], [[low]], [[close]], [[percents]], [[description]]. If not set the default value is
     * "[[value]]".
     *
     * @param valueText value text
     */
    public T setValueText(String valueText) {
        this.valueText = valueText;
        return (T) this;
    }

    /**
     * @return width of the value text
     */
    public Integer getValueWidth() {
        return valueWidth;
    }

    /**
     * Sets width of the value text. If not set the default value is 50.
     *
     * @param valueWidth value width
     */
    public T setValueWidth(Integer valueWidth) {
        this.valueWidth = valueWidth;
        return (T) this;
    }

    /**
     * @return vertical space between legend items also between legend border and first and last legend row
     */
    public Integer getVerticalGap() {
        return verticalGap;
    }

    /**
     * Sets vertical space between legend items also between legend border and first and last legend row. If not set
     * the default value is 10.
     *
     * @param verticalGap vertical space
     */
    public T setVerticalGap(Integer verticalGap) {
        this.verticalGap = verticalGap;
        return (T) this;
    }

    /**
     * @return id of a div in which the legend is placed
     */
    public String getDivId() {
        return divId;
    }

    /**
     * Sets id of a div in case you want the legend to be placed in a separate container.
     *
     * @param divId id of a div
     */
    public T setDivId(String divId) {
        this.divId = divId;
        return (T) this;
    }

    /**
     * @return true if is enabled
     */
    public Boolean getEnabled() {
        return enabled;
    }

    /**
     * Specifies if legend is enabled or not. If not set the default value is true.
     *
     * @param enabled enabled option
     */
    public T setEnabled(Boolean enabled) {
        this.enabled = enabled;
        return (T) this;
    }

    /**
     * @return label width
     */
    public Integer getLabelWidth() {
        return labelWidth;
    }

    /**
     * Sets label width. If width of the label is bigger than labelWidth, it will be wrapped.
     *
     * @param labelWidth label width
     */
    public T setLabelWidth(Integer labelWidth) {
        this.labelWidth = labelWidth;
        return (T) this;
    }

    /**
     * @return JS function to format value labels
     */
    public JsFunction getValueFunction() {
        return valueFunction;
    }

    /**
     * Sets JS function to format value labels. Legend will call this method and will pass GraphDataItem and
     * formatted text of currently hovered item (works only with {@link Cursor} added to the chart). This method should
     * return string which will be displayed as value in the legend.
     *
     * @param valueFunction JS function
     */
    public T setValueFunction(JsFunction valueFunction) {
        this.valueFunction = valueFunction;
        return (T) this;
    }

    /**
     * @return width of a legend
     */
    public Integer getWidth() {
        return width;
    }

    /**
     * Sets width of a legend, when position is set to absolute.
     *
     * @param width width of a legend
     */
    public T setWidth(Integer width) {
        this.width = width;
        return (T) this;
    }

    /**
     * @return false if the width of legend item labels adjusts
     */
    public Boolean getForceWidth() {
        return forceWidth;
    }

    /**
     * Set forceWidth to true if you want the width of legend item labels won't be adjusted. Useful when you have
     * more than one chart and want to align all the legends. If not set the default value is false.
     *
     * @param forceWidth force width option
     */
    public T setForceWidth(Boolean forceWidth) {
        this.forceWidth = forceWidth;
        return (T) this;
    }

    /**
     * @return gradient rotation of the legend
     */
    public Integer getGradientRotation() {
        return gradientRotation;
    }

    /**
     * Sets gradient rotation of the legend. Can be used if legend uses custom data. Set it to 0, 90, 180 or 270.
     *
     * @param gradientRotation gradient rotation of the legend
     */
    public T setGradientRotation(Integer gradientRotation) {
        this.gradientRotation = gradientRotation;
        return (T) this;
    }

    /**
     * @return tab index
     */
    public Integer getTabIndex() {
        return tabIndex;
    }

    /**
     * Sets tab index to the legend. Chart will set focus on legend entry when user clicks tab key. When a focus is
     * set, screen readers like NVDA Screen reader will read label which is set using accessibleLabel property of
     * Legend. If legend has switchable set to true, pressing Enter (Return) key will show/hide the graph. Note, not
     * all browsers and readers support this.
     *
     * @param tabIndex tab index
     */
    public T setTabIndex(Integer tabIndex) {
        this.tabIndex = tabIndex;
        return (T) this;
    }

    /**
     * @return text which screen readers will read if user rolls-over the element or sets focus on it using tab key
     */
    public String getAccessibleLabel() {
        return accessibleLabel;
    }

    /**
     * Sets the text which screen readers will read if user rolls-over the element or sets focus on it using tab key
     * (this is possible only if tabIndex property is set to some number). Text is added as aria-label tag. Note, not
     * all screen readers and browsers support this. If not set the default value is "[[title]]".
     *
     * @param accessibleLabel accessible label text
     */
    public T setAccessibleLabel(String accessibleLabel) {
        this.accessibleLabel = accessibleLabel;
        return (T) this;
    }

    /**
     * @return true if displayed legend data and graph’s entries
     */
    public Boolean getCombineLegend() {
        return combineLegend;
    }

    /**
     * Set combineLegend to true if you want to display both legend data and graph’s entries. If not set the default
     * value is false.
     *
     * @param combineLegend combineLegend option
     */
    public void setCombineLegend(Boolean combineLegend) {
        this.combineLegend = combineLegend;
    }
}