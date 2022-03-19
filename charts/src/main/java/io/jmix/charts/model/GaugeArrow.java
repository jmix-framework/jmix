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
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioElement;
import io.jmix.ui.meta.StudioProperty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * Creates an arrow for {@link AngularGaugeChart} chart, multiple can be assigned.
 * <br>
 * See documentation for properties of GaugeArrow JS Object. <br>
 *
 * <a href="http://docs.amcharts.com/3/javascriptcharts/GaugeArrow">http://docs.amcharts.com/3/javascriptcharts/GaugeArrow</a>
 */
@StudioElement(
        caption = "GaugeArrow",
        xmlElement = "arrow",
        xmlns = "http://jmix.io/schema/ui/charts",
        xmlnsAlias = "chart")
public class GaugeArrow extends AbstractChartObject {

    private static final long serialVersionUID = -9054603815401102787L;

    private Double alpha;

    private String axis;

    private Double borderAlpha;

    private Boolean clockWiseOnly;

    private Color color;

    private String id;

    private String innerRadius;

    private Double nailAlpha;

    private Double nailBorderAlpha;

    private Integer nailBorderThickness;

    private Integer nailRadius;

    private String radius;

    private Integer startWidth;

    private Double value;

    /**
     * @return opacity of an arrow
     */
    public Double getAlpha() {
        return alpha;
    }

    /**
     * Sets opacity of an arrow. If not set the default value is 1.
     *
     * @param alpha opacity
     * @return gauge arrow
     */
    @StudioProperty(defaultValue = "1")
    @Max(1)
    @Min(0)
    public GaugeArrow setAlpha(Double alpha) {
        this.alpha = alpha;
        return this;
    }

    /**
     * @return opacity of arrow border
     */
    public Double getBorderAlpha() {
        return borderAlpha;
    }

    /**
     * Sets opacity of arrow border. If not set the default value is 1.
     *
     * @param borderAlpha border opacity
     * @return gauge arrow
     */
    @StudioProperty(defaultValue = "1")
    @Max(1)
    @Min(0)
    public GaugeArrow setBorderAlpha(Double borderAlpha) {
        this.borderAlpha = borderAlpha;
        return this;
    }

    /**
     * @return true if clockWiseOnly is enabled
     */
    public Boolean getClockWiseOnly() {
        return clockWiseOnly;
    }

    /**
     * Set clockWiseOnly to true if you need the arrow to rotate only clock-wise. If not set the default value is false.
     *
     * @param clockWiseOnly clockWiseOnly option
     * @return gauge arrow
     */
    @StudioProperty(defaultValue = "false")
    public GaugeArrow setClockWiseOnly(Boolean clockWiseOnly) {
        this.clockWiseOnly = clockWiseOnly;
        return this;
    }

    /**
     * @return color of an arrow
     */
    public Color getColor() {
        return color;
    }

    /**
     * Sets color of an arrow. If not set the default value is #000000.
     *
     * @param color color
     * @return gauge arrow
     */
    @StudioProperty(type = PropertyType.OPTIONS, defaultValue = "#000000")
    public GaugeArrow setColor(Color color) {
        this.color = color;
        return this;
    }

    /**
     * @return inner radius
     */
    public String getInnerRadius() {
        return innerRadius;
    }

    /**
     * Sets inner radius of an arrow. If not set the default value is 0.
     *
     * @param innerRadius inner radius
     * @return gauge arrow
     */
    @StudioProperty(defaultValue = "0")
    public GaugeArrow setInnerRadius(String innerRadius) {
        this.innerRadius = innerRadius;
        return this;
    }

    /**
     * @return opacity of a nail, holding the arrow
     */
    public Double getNailAlpha() {
        return nailAlpha;
    }

    /**
     * Sets opacity of a nail, holding the arrow. If not set the default value is 1.
     *
     * @param nailAlpha opacity
     * @return gauge arrow
     */
    @StudioProperty(defaultValue = "1")
    @Max(1)
    @Min(0)
    public GaugeArrow setNailAlpha(Double nailAlpha) {
        this.nailAlpha = nailAlpha;
        return this;
    }

    /**
     * @return opacity of nail border
     */
    public Double getNailBorderAlpha() {
        return nailBorderAlpha;
    }

    /**
     * Sets opacity of nail border. If not set the default value is 0.
     *
     * @param nailBorderAlpha opacity
     * @return gauge arrow
     */
    @StudioProperty(defaultValue = "0")
    @Max(1)
    @Min(0)
    public GaugeArrow setNailBorderAlpha(Double nailBorderAlpha) {
        this.nailBorderAlpha = nailBorderAlpha;
        return this;
    }

    /**
     * @return thickness of nail border
     */
    public Integer getNailBorderThickness() {
        return nailBorderThickness;
    }

    /**
     * Sets thickness of nail border. If not set the default value is 1.
     *
     * @param nailBorderThickness thickness
     * @return gauge arrow
     */
    @StudioProperty(defaultValue = "1")
    public GaugeArrow setNailBorderThickness(Integer nailBorderThickness) {
        this.nailBorderThickness = nailBorderThickness;
        return this;
    }

    /**
     * @return radius
     */
    public Integer getNailRadius() {
        return nailRadius;
    }

    /**
     * Sets radius of a nail, holding the arrow. If not set the default value is 8.
     *
     * @param nailRadius radius
     * @return gauge arrow
     */
    @StudioProperty(defaultValue = "8")
    public GaugeArrow setNailRadius(Integer nailRadius) {
        this.nailRadius = nailRadius;
        return this;
    }

    /**
     * @return radius of an arrow
     */
    public String getRadius() {
        return radius;
    }

    /**
     * Sets radius of an arrow. If not set the default value is 90%.
     *
     * @param radius radius
     * @return gauge arrow
     */
    @StudioProperty(defaultValue = "90%")
    public GaugeArrow setRadius(String radius) {
        this.radius = radius;
        return this;
    }

    /**
     * @return width of arrow root
     */
    public Integer getStartWidth() {
        return startWidth;
    }

    /**
     * Sets width of arrow root. If not set the default value is 8.
     *
     * @param startWidth width
     * @return gauge arrow
     */
    @StudioProperty(defaultValue = "8")
    public GaugeArrow setStartWidth(Integer startWidth) {
        this.startWidth = startWidth;
        return this;
    }

    /**
     * @return axis id
     */
    public String getAxis() {
        return axis;
    }

    /**
     * Sets axis of the arrow. If you don't set any axis, the first axis of a chart will be used.
     *
     * @param axis axis id
     * @return gauge arrow
     */
    @StudioProperty
    public GaugeArrow setAxis(String axis) {
        this.axis = axis;
        return this;
    }

    /**
     * @return unique id of an arrow
     */
    public String getId() {
        return id;
    }

    /**
     * Sets unique id of an arrow.
     * @param id unique id of an arrow
     * @return gauge arrow
     */
    @StudioProperty
    public GaugeArrow setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * @return value to which the arrow is point at
     */
    public Double getValue() {
        return value;
    }

    /**
     * Sets value to which the arrow should point at.
     *
     * @param value value
     * @return gauge arrow
     */
    @StudioProperty
    public GaugeArrow setValue(Double value) {
        this.value = value;
        return this;
    }
}