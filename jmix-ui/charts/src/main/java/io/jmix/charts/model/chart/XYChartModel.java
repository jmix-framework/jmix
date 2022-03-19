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

import io.jmix.ui.meta.StudioProperty;

public interface XYChartModel<T extends XYChartModel> extends RectangularChartModel<T> {
    /**
     * @return true if scrollbar of X axis (horizontal) is hidden
     */
    Boolean getHideXScrollbar();

    /**
     * Set hideXScrollbar to true if scrollbar of X axis (horizontal) should be hidden. If not set the default value
     * is false.
     *
     * @param hideXScrollbar hide X scrollbar option
     * @return XY chart model
     */
    @StudioProperty(defaultValue = "false")
    T setHideXScrollbar(Boolean hideXScrollbar);

    /**
     * @return true if scrollbar of Y axis (vertical) is hidden
     */
    Boolean getHideYScrollbar();

    /**
     * Set hideYScrollbar to true if scrollbar of Y axis (vertical) should be hidden. If not set the default value is
     * false.
     *
     * @param hideYScrollbar hide Y scrollbar option
     * @return XY chart model
     */
    @StudioProperty(defaultValue = "false")
    T setHideYScrollbar(Boolean hideYScrollbar);

    /**
     * @return data date format
     */
    String getDataDateFormat();

    /**
     * Sets the data date format (date/time). Use it if you have date-based value axis in your XY chart. Note, that
     * two-digit years "YY" as well as literal month names "MMM" are NOT supported in this setting.
     *
     * @param dataDateFormat data date format string
     * @return XY chart model
     */
    @StudioProperty
    T setDataDateFormat(String dataDateFormat);

    /**
     * @return maximum value of the size/scale of bubbles
     */
    Integer getMaxValue();

    /**
     * Sets the size/scale of bubbles. If these properties are not set, the bubble with smallest value will be of
     * minBulletSize and bubble with biggest value will be of maxBulletSize. However, you might want bubble size to
     * change relative to 0 or some other value. In this case you can use minValue and maxValue properties. Note, if
     * you use these two settings, you might also want to set minBulletSize to 0.
     *
     * @param maxValue maximum value of the size/scale of bubbles
     * @return XY chart model
     */
    @StudioProperty
    T setMaxValue(Integer maxValue);

    /**
     * @return minimum value of the size/scale of bubbles
     */
    Integer getMinValue();

    /**
     * Sets the size/scale of bubbles. If these properties are not set, the bubble with smallest value will be of
     * minBulletSize and bubble with biggest value will be of maxBulletSize. However, you might want bubble size to
     * change relative to 0 or some other value. In this case you can use minValue and maxValue properties. Note, if
     * you use these two settings, you might also want to set minBulletSize to 0.
     *
     * @param minValue minimum value of the size/scale of bubbles
     * @return XY chart model
     */
    @StudioProperty
    T setMinValue(Integer minValue);
}