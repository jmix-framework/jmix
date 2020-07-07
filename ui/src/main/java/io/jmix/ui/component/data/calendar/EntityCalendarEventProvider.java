/*
 * Copyright 2019 Haulmont.
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

package io.jmix.ui.component.data.calendar;

import javax.annotation.Nullable;

/**
 * An event provider that contains entities.
 */
public interface EntityCalendarEventProvider {

    /**
     * Sets the name of the property that represents start date of event.
     *
     * @param startDateProperty the name of the property that represents start date of event
     */
    void setStartDateProperty(String startDateProperty);

    /**
     * @return the name of the property that represents start date of event
     */
    @Nullable
    String getStartDateProperty();

    /**
     * Sets the name of the property that represents end date of event.
     *
     * @param endDateProperty the name of the property that represents end date of event
     */
    void setEndDateProperty(String endDateProperty);

    /**
     * @return the name of the property that represents end date of event
     */
    @Nullable
    String getEndDateProperty();

    /**
     * Sets the name of the property that represents caption of event.
     *
     * @param captionProperty the name of the property that represents caption of event
     */
    void setCaptionProperty(String captionProperty);

    /**
     * @return the name of the property that represents caption of event
     */
    @Nullable
    String getCaptionProperty();

    /**
     * Sets the name of the property that represents description of event.
     *
     * @param descriptionProperty the name of the property that represents description of event
     */
    void setDescriptionProperty(String descriptionProperty);

    /**
     * @return the name of the property that represents description of event
     */
    @Nullable
    String getDescriptionProperty();

    /**
     * Sets the name of the property that represents style name of event.
     *
     * @param styleNameProperty the name of the property that represents style name of event
     */
    void setStyleNameProperty(String styleNameProperty);

    /**
     * @return the name of the property that represents style name of event
     */
    @Nullable
    String getStyleNameProperty();

    /**
     * Sets the name of the property that represents whether event is an all-day event.
     *
     * @param allDayProperty {@code true} if this event is an all-day event, {@code false} otherwise
     */
    void setAllDayProperty(String allDayProperty);

    /**
     * @return {@code true} if this event is an all-day event, {@code false} otherwise
     */
    @Nullable
    String getIsAllDayProperty();

    void unbind();
}
