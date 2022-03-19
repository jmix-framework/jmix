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


import io.jmix.charts.model.axis.CategoryAxis;
import io.jmix.charts.model.chart.impl.RadarChartModelImpl;
import io.jmix.charts.model.chart.impl.SerialChartModelImpl;
import io.jmix.charts.model.chart.impl.XYChartModelImpl;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioElement;
import io.jmix.ui.meta.StudioProperty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.Date;

/**
 * Creates a horizontal/vertical guideline-/area for {@link SerialChartModelImpl}, {@link XYChartModelImpl} and {@link RadarChartModelImpl}
 * charts, automatically adapts it's settings from the axes if none has been specified.
 * <p>
 * See documentation for properties of Guide JS Object. <br>
 *
 * <a href="http://docs.amcharts.com/3/javascriptcharts/Guide">http://docs.amcharts.com/3/javascriptcharts/Guide</a>
 */
@StudioElement(
        caption = "Guide",
        xmlElement = "guide",
        xmlns = "http://jmix.io/schema/ui/charts",
        xmlnsAlias = "chart")
public class Guide extends AbstractChartObject {

    private static final long serialVersionUID = -6465377479319328449L;

    private Boolean above;

    private Integer angle;

    private Color balloonColor;

    private String balloonText;

    private Boolean boldLabel;

    private String category;

    private Color color;

    private Integer dashLength;

    private Date date;

    private Boolean expand;

    private Double fillAlpha;

    private Color fillColor;

    private Integer fontSize;

    private String id;

    private Boolean inside;

    private String label;

    private Integer labelRotation;

    private Double lineAlpha;

    private Color lineColor;

    private Integer lineThickness;

    private Position position;

    private Integer tickLength;

    private Integer toAngle;

    private String toCategory;

    private Date toDate;

    private Object toValue;

    private Object value;

    private String valueAxis;

    /**
     * @return true if guide displayed above the graphs
     */
    public Boolean getAbove() {
        return above;
    }

    /**
     * Set above to true if the guide should be displayed above the graphs. If not set the default value is false.
     *
     * @param above above option
     * @return guide
     */
    @StudioProperty(defaultValue = "false")
    public Guide setAbove(Boolean above) {
        this.above = above;
        return this;
    }

    /**
     * @return angle at which guide starts
     */
    public Integer getAngle() {
        return angle;
    }

    /**
     * Sets angle at which guide should start. Affects only fills, not lines. Radar chart only.
     *
     * @param angle angle
     * @return guide
     */
    @StudioProperty
    public Guide setAngle(Integer angle) {
        this.angle = angle;
        return this;
    }

    /**
     * @return balloon fill color
     */
    public Color getBalloonColor() {
        return balloonColor;
    }

    /**
     * Sets balloon fill color.
     *
     * @param balloonColor fill color
     * @return guide
     */
    @StudioProperty(type = PropertyType.OPTIONS)
    public Guide setBalloonColor(Color balloonColor) {
        this.balloonColor = balloonColor;
        return this;
    }

    /**
     * @return balloon text which is displayed if the user rolls-over the guide
     */
    public String getBalloonText() {
        return balloonText;
    }

    /**
     * Sets the text which will be displayed if the user rolls-over the guide.
     *
     * @param balloonText balloon text
     * @return guide
     */
    @StudioProperty
    public Guide setBalloonText(String balloonText) {
        this.balloonText = balloonText;
        return this;
    }

    /**
     * @return category value
     */
    public String getCategory() {
        return category;
    }

    /**
     * Sets category of the guide (in case the guide is for category axis).
     *
     * @param category category value
     * @return guide
     */

    @StudioProperty
    public Guide setCategory(String category) {
        this.category = category;
        return this;
    }

    /**
     * @return dash length.
     */
    public Integer getDashLength() {
        return dashLength;
    }

    /**
     * Sets dash length.
     *
     * @param dashLength dash length
     * @return guide
     */
    @StudioProperty
    public Guide setDashLength(Integer dashLength) {
        this.dashLength = dashLength;
        return this;
    }

    /**
     * @return date of the guide
     */
    public Date getDate() {
        return date;
    }

    /**
     * Sets date of the guide (in case the guide is for category axis and {@link CategoryAxis#parseDates} is set to
     * true).
     *
     * @param date date
     * @return guide
     */
    @StudioProperty
    public Guide setDate(Date date) {
        this.date = date;
        return this;
    }

    /**
     * @return fill opacity
     */
    public Double getFillAlpha() {
        return fillAlpha;
    }

    /**
     * Sets fill opacity. Value range is 0 - 1.
     *
     * @param fillAlpha fill opacity
     * @return guide
     */
    @StudioProperty
    @Max(1)
    @Min(0)
    public Guide setFillAlpha(Double fillAlpha) {
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
     * Sets fill color.
     *
     * @param fillColor fill color
     * @return guide
     */
    @StudioProperty(type = PropertyType.OPTIONS)
    public Guide setFillColor(Color fillColor) {
        this.fillColor = fillColor;
        return this;
    }

    /**
     * @return font size of guide label
     */
    public Integer getFontSize() {
        return fontSize;
    }

    /**
     * Sets font size of guide label.
     *
     * @param fontSize font size
     * @return guide
     */
    @StudioProperty
    public Guide setFontSize(Integer fontSize) {
        this.fontSize = fontSize;
        return this;
    }

    /**
     * @return true if if label is placed inside plot area
     */
    public Boolean getInside() {
        return inside;
    }

    /**
     * Set inside to true if label should be placed inside plot area.
     *
     * @param inside inside option
     * @return guide
     */
    @StudioProperty
    public Guide setInside(Boolean inside) {
        this.inside = inside;
        return this;
    }

    /**
     * @return label which is displayed near the guide
     */
    public String getLabel() {
        return label;
    }

    /**
     * Sets the label which will be displayed near the guide.
     *
     * @param label label string
     * @return guide
     */
    @StudioProperty
    public Guide setLabel(String label) {
        this.label = label;
        return this;
    }

    /**
     * @return rotation angle of a guide label
     */
    public Integer getLabelRotation() {
        return labelRotation;
    }

    /**
     * Sets rotation angle of a guide label.
     *
     * @param labelRotation rotation angle
     * @return guide
     */
    @StudioProperty
    public Guide setLabelRotation(Integer labelRotation) {
        this.labelRotation = labelRotation;
        return this;
    }

    /**
     * @return line opacity
     */
    public Double getLineAlpha() {
        return lineAlpha;
    }

    /**
     * Sets line opacity.
     *
     * @param lineAlpha line opacity
     * @return guide
     */
    @StudioProperty
    @Max(1)
    @Min(0)
    public Guide setLineAlpha(Double lineAlpha) {
        this.lineAlpha = lineAlpha;
        return this;
    }

    /**
     * @return line color
     */
    public Color getLineColor() {
        return lineColor;
    }

    /**
     * Sets line color.
     *
     * @param lineColor line color
     * @return guide
     */
    @StudioProperty(type = PropertyType.OPTIONS)
    public Guide setLineColor(Color lineColor) {
        this.lineColor = lineColor;
        return this;
    }

    /**
     * @return line thickness
     */
    public Integer getLineThickness() {
        return lineThickness;
    }

    /**
     * Sets line thickness.
     *
     * @param lineThickness line thickness
     * @return guide
     */
    @StudioProperty
    public Guide setLineThickness(Integer lineThickness) {
        this.lineThickness = lineThickness;
        return this;
    }

    /**
     * @return position of guide label
     */
    public Position getPosition() {
        return position;
    }

    /**
     * Sets position of guide label. Possible values are "left" or "right" for horizontal axis and "top" or "bottom"
     * for vertical axis.
     *
     * @param position position of guide label
     * @return guide
     */
    @StudioProperty(type = PropertyType.ENUMERATION)
    public Guide setPosition(Position position) {
        this.position = position;
        return this;
    }

    /**
     * @return tick length
     */
    public Integer getTickLength() {
        return tickLength;
    }

    /**
     * Sets tick length.
     *
     * @param tickLength tick length
     * @return guide
     */
    @StudioProperty
    public Guide setTickLength(Integer tickLength) {
        this.tickLength = tickLength;
        return this;
    }

    /**
     * @return angle at which guide ends
     */
    public Integer getToAngle() {
        return toAngle;
    }

    /**
     * Sets the angle at which guide should end. Affects only fills, not lines. Radar chart only.
     *
     * @param toAngle angle
     * @return guide
     */
    @StudioProperty
    public Guide setToAngle(Integer toAngle) {
        this.toAngle = toAngle;
        return this;
    }

    /**
     * @return "to" category of the guide
     */
    public String getToCategory() {
        return toCategory;
    }

    /**
     * Sets "to" category of the guide (in case the guide is for category axis).
     *
     * @param toCategory to category
     * @return guide
     */
    @StudioProperty
    public Guide setToCategory(String toCategory) {
        this.toCategory = toCategory;
        return this;
    }

    /**
     * @return "to" date of the guide
     */
    public Date getToDate() {
        return toDate;
    }

    /**
     * Sets "to" date of the guide (in case the guide is for category axis and {@link CategoryAxis#parseDates} is set
     * to true) If you have both date and toDate, the space between these two dates can be filled with color.
     *
     * @param toDate date
     * @return guide
     */
    @StudioProperty
    public Guide setToDate(Date toDate) {
        this.toDate = toDate;
        return this;
    }

    /**
     * @return "to" value of the guide
     */
    public Object getToValue() {
        return toValue;
    }

    /**
     * Sets "to" value of the guide (in case the guide is for value axis).
     *
     * @param toValue to value
     * @return guide
     */
    @StudioProperty(type = PropertyType.DOUBLE)
    public Guide setToValue(Object toValue) {
        this.toValue = toValue;
        return this;
    }

    /**
     * @return value of the guide
     */
    public Object getValue() {
        return value;
    }

    /**
     * Sets value of the guide (in case the guide is for value axis).
     *
     * @param value value
     * @return guide
     */
    @StudioProperty(type = PropertyType.DOUBLE)
    public Guide setValue(Object value) {
        this.value = value;
        return this;
    }

    /**
     * @return true if label is bold
     */
    public Boolean getBoldLabel() {
        return boldLabel;
    }

    /**
     * Set boldLabel to true if label should be bold. If not set the default value is false.
     *
     * @param boldLabel boldLabel option
     * @return guide
     */
    @StudioProperty(defaultValue = "false")
    public Guide setBoldLabel(Boolean boldLabel) {
        this.boldLabel = boldLabel;
        return this;
    }

    /**
     * @return color of a guide label
     */
    public Color getColor() {
        return color;
    }

    /**
     * Sets color of a guide label.
     *
     * @param color color
     * @return guide
     */
    @StudioProperty(type = PropertyType.OPTIONS)
    public Guide setColor(Color color) {
        this.color = color;
        return this;
    }

    /**
     * @return true if the guide starts on the beginning of the category cell and ends at the end of
     * "toCategory" cell
     */
    public Boolean getExpand() {
        return expand;
    }

    /**
     * Set expand to true if the guide should start (or be placed, if it's not a fill) on the beginning of the
     * category cell and should end at the end of "toCategory" cell. Works if a guide is added to {@link CategoryAxis}
     * and this axis is non-date-based. If not set the default value is false.
     *
     * @param expand expand option
     * @return guide
     */
    @StudioProperty(defaultValue = "false")
    public Guide setExpand(Boolean expand) {
        this.expand = expand;
        return this;
    }

    /**
     * @return unique id of a guide
     */
    public String getId() {
        return id;
    }

    /**
     * Sets unique id of a guide.
     *
     * @param id id
     * @return guide
     */
    @StudioProperty
    public Guide setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * @return value axis id
     */
    public String getValueAxis() {
        return valueAxis;
    }

    /**
     * Sets value axis of a guide. As you can add guides directly to the chart, you might need to specify which value
     * axis should be used.
     *
     * @param valueAxis value axis id
     * @return guide
     */
    @StudioProperty
    public Guide setValueAxis(String valueAxis) {
        this.valueAxis = valueAxis;
        return this;
    }
}