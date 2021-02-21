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

package io.jmix.uicharts.model.trendline;


import io.jmix.uicharts.model.AbstractChartObject;
import io.jmix.uicharts.model.Color;
import io.jmix.uicharts.model.chart.impl.SerialChart;
import io.jmix.uicharts.model.chart.impl.XYChart;

import java.util.Date;

/**
 * Creates a trendline for {@link SerialChart} and {@link XYChart} charts which indicates the trend of your data or
 * covers some different purposes. Multiple can be assigned.
 * <p>
 * See documentation for properties of TrendLine JS Object. <br>
 *
 * <a href="http://docs.amcharts.com/3/javascriptcharts/TrendLine">http://docs.amcharts.com/3/javascriptcharts/TrendLine</a>
 */
public class TrendLine extends AbstractChartObject {

    private static final long serialVersionUID = -6846712713867338160L;

    private String balloonText;

    private Integer dashLength;

    private String finalCategory;

    private Date finalDate;

    private Image finalImage;

    private Double finalValue;

    private Double finalXValue;

    private String id;

    private String initialCategory;

    private Date initialDate;

    private Image initialImage;

    private Double initialValue;

    private Double initialXValue;

    private Boolean isProtected;

    private Double lineAlpha;

    private Color lineColor;

    private Integer lineThickness;

    private String valueAxis;

    private String valueAxisX;

    /**
     * @return balloon text
     */
    public String getBalloonText() {
        return balloonText;
    }

    /**
     * Sets balloon text. When set, enables displaying a roll-over balloon.
     *
     * @param balloonText balloon text
     */
    public TrendLine setBalloonText(String balloonText) {
        this.balloonText = balloonText;
        return this;
    }

    /**
     * @return dash length
     */
    public Integer getDashLength() {
        return dashLength;
    }

    /**
     * Sets dash length. If not set the default value is 0.
     *
     * @param dashLength dash length
     */
    public TrendLine setDashLength(Integer dashLength) {
        this.dashLength = dashLength;
        return this;
    }

    /**
     * @return final category string
     */
    public String getFinalCategory() {
        return finalCategory;
    }

    /**
     * Sets string, equal to category value to which trend line should be drawn. It should be used if chart doesn't
     * parse dates.
     *
     * @param finalCategory final category string
     */
    public TrendLine setFinalCategory(String finalCategory) {
        this.finalCategory = finalCategory;
        return this;
    }

    /**
     * @return date to which trend line should be drawn
     */
    public Date getFinalDate() {
        return finalDate;
    }

    /**
     * Sets date to which trend line should be drawn.
     *
     * @param finalDate final date
     */
    public TrendLine setFinalDate(Date finalDate) {
        this.finalDate = finalDate;
        return this;
    }

    /**
     * @return value at which trend line is end
     */
    public Double getFinalValue() {
        return finalValue;
    }

    /**
     * Sets value at which trend line should end.
     *
     * @param finalValue final value
     */
    public TrendLine setFinalValue(Double finalValue) {
        this.finalValue = finalValue;
        return this;
    }

    /**
     * @return X value at which trend line is end
     */
    public Double getFinalXValue() {
        return finalXValue;
    }

    /**
     * Sets X value at which trend line should end. Used by XY chart only.
     *
     * @param finalXValue final X value
     */
    public TrendLine setFinalXValue(Double finalXValue) {
        this.finalXValue = finalXValue;
        return this;
    }

    /**
     * @return string equal to category value from which trend line should start
     */
    public String getInitialCategory() {
        return initialCategory;
    }

    /**
     * Sets string equal to category value from which trend line should start. It should be used if chart doesn't
     * parse dates.
     *
     * @param initialCategory initial category
     */
    public TrendLine setInitialCategory(String initialCategory) {
        this.initialCategory = initialCategory;
        return this;
    }

    /**
     * @return date from which trend line should start
     */
    public Date getInitialDate() {
        return initialDate;
    }

    /**
     * Sets date from which trend line should start.
     *
     * @param initialDate initial date
     */
    public TrendLine setInitialDate(Date initialDate) {
        this.initialDate = initialDate;
        return this;
    }

    /**
     * @return value from which trend line should start
     */
    public Double getInitialValue() {
        return initialValue;
    }

    /**
     * Sets value from which trend line should start.
     *
     * @param initialValue initial value
     */
    public TrendLine setInitialValue(Double initialValue) {
        this.initialValue = initialValue;
        return this;
    }

    /**
     * @return X value from which trend line should start
     */
    public Double getInitialXValue() {
        return initialXValue;
    }

    /**
     * Sets X value from which trend line should start. Used by XY chart only
     *
     * @param initialXValue initial X value
     */
    public TrendLine setInitialXValue(Double initialXValue) {
        this.initialXValue = initialXValue;
        return this;
    }

    /**
     * @return true if isProtected is enabled
     */
    public Boolean getProtected() {
        return isProtected;
    }

    /**
     * Set isProtected to true if trend line shouldn't be removed when clicked on eraser tool. Used by stock chart.
     * If not set the default value is false.
     *
     * @param aProtected isProtected option
     */
    public TrendLine setProtected(Boolean aProtected) {
        isProtected = aProtected;
        return this;
    }

    /**
     * @return line opacity
     */
    public Double getLineAlpha() {
        return lineAlpha;
    }

    /**
     * Sets line opacity. If not set the default value is 1.
     *
     * @param lineAlpha opacity
     */
    public TrendLine setLineAlpha(Double lineAlpha) {
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
     * Sets line color. If not set the default value is #00CC00.
     *
     * @param lineColor color
     */
    public TrendLine setLineColor(Color lineColor) {
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
     * Sets line thickness. If not set the default value is 1.
     *
     * @param lineThickness thickness
     */
    public TrendLine setLineThickness(Integer lineThickness) {
        this.lineThickness = lineThickness;
        return this;
    }

    /**
     * @return value axis id
     */
    public String getValueAxis() {
        return valueAxis;
    }

    /**
     * Sets value axis of the trend line. Will use first value axis of the chart if not set any.
     *
     * @param valueAxis value axis id
     */
    public TrendLine setValueAxis(String valueAxis) {
        this.valueAxis = valueAxis;
        return this;
    }

    /**
     * @return value axis X id
     */
    public String getValueAxisX() {
        return valueAxisX;
    }

    /**
     * Sets X axis of the trend line. Will use first X axis of the chart if not set any. Used by XY chart only.
     *
     * @param valueAxisX value axis X id
     */
    public TrendLine setValueAxisX(String valueAxisX) {
        this.valueAxisX = valueAxisX;
        return this;
    }

    /**
     * @return id of a trend line
     */
    public String getId() {
        return id;
    }

    /**
     * Sets unique id of a trend line.
     *
     * @param id id
     */
    public TrendLine setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * @return final image
     */
    public Image getFinalImage() {
        return finalImage;
    }

    /**
     * Sets an image to the end of a trend line.
     *
     * @param finalImage final image
     */
    public TrendLine setFinalImage(Image finalImage) {
        this.finalImage = finalImage;
        return this;
    }

    /**
     * @return initial image
     */
    public Image getInitialImage() {
        return initialImage;
    }

    /**
     * Sets an image to the beginning of a trend line.
     *
     * @param initialImage initial image
     */
    public TrendLine setInitialImage(Image initialImage) {
        this.initialImage = initialImage;
        return this;
    }
}