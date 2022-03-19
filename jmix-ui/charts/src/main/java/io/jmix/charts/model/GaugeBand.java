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

import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioElement;
import io.jmix.ui.meta.StudioProperty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.List;

/**
 * Creates a band for a specified value range on the {@link GaugeAxis}. Multiple bands can be assigned to a single
 * {@link GaugeAxis}.
 * <br>
 * See documentation for properties of GaugeBand JS Object. <br>
 *
 * <a href="http://docs.amcharts.com/3/javascriptcharts/GaugeBand">http://docs.amcharts.com/3/javascriptcharts/GaugeBand</a>
 */
@StudioElement(
        caption = "GaugeBand",
        xmlElement = "band",
        xmlns = "http://jmix.io/schema/ui/charts",
        xmlnsAlias = "chart")
public class GaugeBand extends AbstractChartObject {

    private static final long serialVersionUID = 6480625092225201700L;

    private Double alpha;

    private List<Float> gradientRatio;

    private String balloonText;

    private Color color;

    private Double endValue;

    private String id;

    private String innerRadius;

    private String radius;

    private Double startValue;

    private String url;

    public GaugeBand() {
    }

    /**
     * @return opacity of band fill
     */
    public Double getAlpha() {
        return alpha;
    }

    /**
     * Sets opacity of band fill. Will use {@link GaugeAxis#bandAlpha} if not set any.
     *
     * @param alpha opacity
     * @return gauge band
     */
    @StudioProperty
    @Max(1)
    @Min(0)
    public GaugeBand setAlpha(Double alpha) {
        this.alpha = alpha;
        return this;
    }

    /**
     * @return color of a band
     */
    public Color getColor() {
        return color;
    }

    /**
     * Sets color of a band.
     *
     * @param color color
     * @return gauge band
     */
    @StudioProperty(type = PropertyType.OPTIONS)
    public GaugeBand setColor(Color color) {
        this.color = color;
        return this;
    }

    /**
     * @return end value of a fill
     */
    public Double getEndValue() {
        return endValue;
    }

    /**
     * Sets end value of a fill.
     *
     * @param endValue end value
     * @return gauge band
     */
    @StudioProperty
    public GaugeBand setEndValue(Double endValue) {
        this.endValue = endValue;
        return this;
    }

    /**
     * @return inner radius of a band
     */
    public String getInnerRadius() {
        return innerRadius;
    }

    /**
     * Sets inner radius of a band. If not set any, the band will end with the end of minor ticks. Set 0 if you want
     * the band to be drawn to the axis center.
     *
     * @param innerRadius inner radius
     * @return gauge band
     */
    @StudioProperty
    public GaugeBand setInnerRadius(String innerRadius) {
        this.innerRadius = innerRadius;
        return this;
    }

    /**
     * @return band radius
     */
    public String getRadius() {
        return radius;
    }

    /**
     * Sets band radius. If not set any, the band will start with the axis outline.
     *
     * @param radius band radius
     * @return gauge band
     */
    @StudioProperty
    public GaugeBand setRadius(String radius) {
        this.radius = radius;
        return this;
    }

    /**
     * @return start value of a fill
     */
    public Double getStartValue() {
        return startValue;
    }

    /**
     * Sets start value of a fill.
     *
     * @param startValue start value
     * @return gauge band
     */
    @StudioProperty
    public GaugeBand setStartValue(Double startValue) {
        this.startValue = startValue;
        return this;
    }

    /**
     * @return balloon text
     */
    public String getBalloonText() {
        return balloonText;
    }

    /**
     * Sets balloon text. When rolled-over, band will display balloon.
     *
     * @param balloonText balloon text
     * @return gauge band
     */
    @StudioProperty
    public GaugeBand setBalloonText(String balloonText) {
        this.balloonText = balloonText;
        return this;
    }

    /**
     * @return unique id of a band
     */
    public String getId() {
        return id;
    }

    /**
     * Sets unique id of a band.
     *
     * @param id id
     * @return gauge band
     */
    @StudioProperty
    public GaugeBand setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * @return the URL
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets URL for band. Gauge band can be clickable and can lead to some page.
     *
     * @param url the URL
     * @return gauge band
     */
    @StudioProperty
    public GaugeBand setUrl(String url) {
        this.url = url;
        return this;
    }

    /**
     * @return list of gradient ratio
     */
    public List<Float> getGradientRatio() {
        return gradientRatio;
    }

    /**
     * Sets list of gradient ratio. Will make bands to be filled with color gradients. Negative value means the color
     * will be darker than the original, and positive number means the color will be lighter.
     *
     * @param gradientRatio list of gradient ratio
     * @return gauge band
     */
    @StudioProperty(type = PropertyType.STRING)
    public GaugeBand setGradientRatio(List<Float> gradientRatio) {
        this.gradientRatio = gradientRatio;
        return this;
    }
}