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

package io.jmix.charts.model.chart;

import io.jmix.charts.model.*;
import io.jmix.charts.model.GaugeArrow;
import io.jmix.charts.model.GaugeAxis;
import io.jmix.charts.model.animation.HasStartEffect;
import io.jmix.charts.model.graph.Pattern;

import java.util.List;

public interface AngularGaugeChartModel<T extends AngularGaugeChartModel>
        extends ChartModel<T>, HasMargins<T>, HasStartEffect<T> {
    /**
     * @return true if adjust size is enabled
     */
    Boolean getAdjustSize();

    /**
     * Sets using the whole space of the canvas to draw the gauge. If not set the default value is true.
     *
     * @param adjustSize adjust size option
     */
    T setAdjustSize(Boolean adjustSize);

    /**
     * @return list of GaugeArrow
     */
    List<GaugeArrow> getArrows();

    /**
     * Sets list of GaugeArrow.
     *
     * @param arrows the arrows
     */
    T setArrows(List<GaugeArrow> arrows);

    /**
     * Adds GaugeArrows.
     *
     * @param arrows the arrows
     */
    T addArrows(GaugeArrow... arrows);

    /**
     * @return list of GaugeAxis
     */
    List<GaugeAxis> getAxes();

    /**
     * Sets list of GaugeAxis.
     *
     * @param axes the axes
     */
    T setAxes(List<GaugeAxis> axes);

    /**
     * Adds GaugeAxis.
     *
     * @param axes the axes
     */
    T addAxes(GaugeAxis... axes);

    /**
     * @return true if clock wise only is enabled
     */
    Boolean getClockWiseOnly();

    /**
     * Set clockWiseOnly to true if you use gauge to create a clock. If not set the default value is false.
     *
     * @param clockWiseOnly clock wise only option
     */
    T setClockWiseOnly(Boolean clockWiseOnly);

    /**
     * @return gauge face opacity
     */
    Double getFaceAlpha();

    /**
     * Sets gauge face opacity. If not set the default value is 0.
     *
     * @param faceAlpha the face alpha
     */
    T setFaceAlpha(Double faceAlpha);

    /**
     * @return gauge face border opacity.
     */
    Double getFaceBorderAlpha();

    /**
     * Sets gauge face border opacity. If not set the default value is 0.
     *
     * @param faceBorderAlpha the face border alpha
     */
    T setFaceBorderAlpha(Double faceBorderAlpha);

    /**
     * @return gauge face border color.
     */
    Color getFaceBorderColor();

    /**
     * Sets gauge face border color. If not set the default value is #555555.
     *
     * @param faceBorderColor the face border color
     */
    T setFaceBorderColor(Color faceBorderColor);

    /**
     * @return gauge face border width.
     */
    Integer getFaceBorderWidth();

    /**
     * Sets gauge face border width. If not set the default value is 1.
     *
     * @param faceBorderWidth the face border width
     */
    T setFaceBorderWidth(Integer faceBorderWidth);

    /**
     * @return gauge face color
     */
    Color getFaceColor();

    /**
     * Sets gauge face color, requires faceAlpha greater than 0 value. If not set the default value is #FAFAFA.
     *
     * @param faceColor the face color
     */
    T setFaceColor(Color faceColor);

    /**
     * @return gauge face image-pattern
     */
    Pattern getFacePattern();

    /**
     * Sets gauge face image-pattern.
     *
     * @param facePattern the face pattern
     */
    T setFacePattern(Pattern facePattern);

    /**
     * @return gauge's horizontal position in pixel
     */
    String getGaugeX();

    /**
     * Sets gauge's horizontal position in pixel, origin is the center. Centered by default.
     *
     * @param gaugeX horizontal position in pixel
     */
    T setGaugeX(String gaugeX);

    /**
     * @return gauge's vertical position in pixel
     */
    String getGaugeY();

    /**
     * Sets gauge's vertical position in pixel, origin is the center. Centered by default.
     *
     * @param gaugeY vertical position in pixel
     */
    T setGaugeY(String gaugeY);

    /**
     * @return minimum radius of a gauge
     */
    Integer getMinRadius();

    /**
     * Sets minimum radius of a gauge. If not set the default value is 10.
     *
     * @param minRadius minimum radius
     */
    T setMinRadius(Integer minRadius);
}