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


import io.jmix.charts.model.FunnelValueRepresentation;
import io.jmix.charts.model.label.LabelPosition;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioProperty;

public interface FunnelChartModel<T extends FunnelChartModel> extends SlicedChartModel<T> {
    /**
     * @return balloon text
     */
    String getBalloonText();

    /**
     * Sets balloon text. The following tags can be used: [[value]], [[title]], [[percents]], [[description]]. Also can
     * be used fields in the chart such as titleField, valueField etc. Fields that not used in the chart can be added
     * by using additionalFields. HTML tags can also be used. If not set the default value is
     * "[[title]]:[[value]]\n[[description]]".
     *
     * @param balloonText balloon text string
     * @return funnel chart model
     */
    @StudioProperty(defaultValue = "[[title]]:[[value]]\n[[description]]")
    T setBalloonText(String balloonText);

    /**
     * @return width of a base (first slice) of a chart
     */
    String getBaseWidth();

    /**
     * Sets width of a base (first slice) of a chart. 100% means it will occupy all available space. If not set the
     * default value is 100%.
     *
     * @param baseWidth the base width
     * @return funnel chart model
     */
    @StudioProperty(defaultValue = "100%")
    T setBaseWidth(String baseWidth);

    /**
     * @return label position
     */
    LabelPosition getLabelPosition();

    /**
     * Specifies where labels should be placed. Allowed values are left, center, right. If you set left or right, you
     * should increase left or right margin in order labels to be visible. If not set the default value is CENTER.
     *
     * @param labelPosition the label position
     * @return funnel chart model
     */
    @StudioProperty(type = PropertyType.ENUMERATION, defaultValue = "CENTER")
    T setLabelPosition(LabelPosition labelPosition);

    /**
     * @return label text
     */
    String getLabelText();

    /**
     * Sets label text. The following tags can be used: [[value]], [[title]], [[percents]], [[description]]. Also can
     * be used fields in the chart such as titleField, valueField etc. Fields that not used in the chart can be added
     * by using additionalFields. If not set the default value is "[[title]]: [[value]]".
     *
     * @param labelText label text string
     * @return funnel chart model
     */
    @StudioProperty(defaultValue = "[[title]]: [[value]]")
    T setLabelText(String labelText);

    /**
     * @return height of a funnel neck
     */
    String getNeckHeight();

    /**
     * Sets height of a funnel neck. If default value, zero is used, the funnel won't have neck at all, which will make
     * it look like pyramid. If not set the default value is "0".
     *
     * @param neckHeight the neck height
     * @return funnel chart model
     */
    @StudioProperty(defaultValue = "0")
    T setNeckHeight(String neckHeight);

    /**
     * @return width of a funnel neck
     */
    String getNeckWidth();

    /**
     * Sets width of a funnel neck. If default value, zero is used, the funnel won't have neck at all, which will make
     * it look like pyramid. If not set the default value is "0".
     *
     * @param neckWidth the neck width
     * @return funnel chart model
     */
    @StudioProperty(defaultValue = "0")
    T setNeckWidth(String neckWidth);

    /**
     * @return pull distance
     */
    String getPullDistance();

    /**
     * Sets the distance by which slice should be pulled when user clicks on it. If not set the default value is 30.
     *
     * @param pullDistance the pull distance
     * @return funnel chart model
     */
    @StudioProperty(defaultValue = "30")
    T setPullDistance(String pullDistance);

    /**
     * @return initial X coordinate of slices
     */
    Integer getStartX();

    /**
     * Sets initial X coordinate of slices. They will animate to the final X position from this one. If not set the
     * default value is 0.
     *
     * @param startX the start X
     * @return funnel chart model
     */
    @StudioProperty(defaultValue = "0")
    T setStartX(Integer startX);

    /**
     * @return initial Y coordinate of slices
     */
    Integer getStartY();

    /**
     * Sets initial y coordinate of slices. They will animate to the final y position from this one. If not set the
     * default value is 0.
     *
     * @param startY the start Y
     * @return funnel chart model
     */
    @StudioProperty(defaultValue = "0")
    T setStartY(Integer startY);

    /**
     * @return value represents
     */
    FunnelValueRepresentation getValueRepresents();

    /**
     * Sets the value represents. By default, the height of a slice represents it's value. Set this property to "area"
     * if you want the area of a slice to represent value. If not set the default value is HEIGHT.
     *
     * @param valueRepresents the value represents
     * @return funnel chart model
     */
    @StudioProperty(type = PropertyType.ENUMERATION, defaultValue = "HEIGHT")
    T setValueRepresents(FunnelValueRepresentation valueRepresents);

    /**
     * @return true if rotate is enabled
     */
    Boolean getRotate();

    /**
     * If rotate is set to true, the funnel will be rotated and will became a pyramid. If not set the default value
     * is false.
     *
     * @param rotate rotate option
     * @return funnel chart model
     */
    @StudioProperty(defaultValue = "false")
    T setRotate(Boolean rotate);

    /**
     * @return the angle of the 3D part of the chart
     */
    Integer getAngle();

    /**
     * Sets the angle of the 3D part of the chart. This creates a 3D effect (if the depth3D is greater than 0). If
     * not set the default value is 0.
     *
     * @param angle the angle
     * @return funnel chart model
     */
    @StudioProperty(defaultValue = "0")
    T setAngle(Integer angle);

    /**
     * @return the depth of funnel/pyramid
     */
    Integer getDepth3D();

    /**
     * Sets the depth of funnel/pyramid. Set angle to greater than 0 value in order this to work. Note,
     * neckHeight/neckWidth will become 0 if you set these properties to bigger than 0 values.
     *
     * @param depth3D the depth 3D
     * @return funnel chart model
     */
    @StudioProperty
    T setDepth3D(Integer depth3D);
}