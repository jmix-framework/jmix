/*
 * Copyright 2024 Haulmont.
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

package io.jmix.fullcalendarflowui.kit.component.model;

import io.jmix.fullcalendarflowui.kit.component.JmixFullCalendar;
import jakarta.annotation.Nullable;

import java.time.LocalDate;

/**
 * Custom calendar view properties. It enables to create new view with custom date range and use it along with other
 * views. If date range is not specified it can be managed by
 * {@link JmixFullCalendar#setVisibleRange(LocalDate, LocalDate)}.
 */
public class CustomCalendarView extends AbstractCalendarViewProperties {

    protected CalendarView calendarView;

    protected CalendarView type;

    protected CalendarDuration duration;

    protected Integer dayCount;

    /**
     * Creates new instance of custom views with the specified view ID. The type of custom view will be
     * {@link GenericCalendarViewType#DAY_GRID}.
     *
     * @param id ID to pass
     */
    public CustomCalendarView(String id) {
        this(id, null);
    }

    /**
     * Creates new instance of custom views with the specified view ID and view type: {@link GenericCalendarViewType}.
     *
     * @param id   ID to pass
     * @param type the type of custom view
     */
    public CustomCalendarView(String id, @Nullable CalendarView type) {
        super(id);

        this.calendarView = () -> id;
        this.type = type;
    }

    /**
     * @return calendar view object from the view ID
     */
    public CalendarView getCalendarView() {
        return calendarView;
    }

    /**
     * @return the type of custom calendar view
     */
    public CalendarView getType() {
        return type == null ? GenericCalendarViewType.DAY_GRID : type;
    }

    @Nullable
    public CalendarDuration getDuration() {
        return duration;
    }

    /**
     * Sets the exact duration of a custom view.
     * <p>
     * Takes precedence over the {@link #setDayCount(Integer)}.
     *
     * @param duration the duration to set
     */
    public void setDuration(@Nullable CalendarDuration duration) {
        this.duration = duration;
    }

    /**
     * Sets the exact duration of a custom view. See {@link #setDuration(CalendarDuration)}.
     *
     * @param duration the duration to set
     * @return current instance of custom view
     */
    public CustomCalendarView withDuration(@Nullable CalendarDuration duration) {
        setDuration(duration);
        return this;
    }

    @Nullable
    public Integer getDayCount() {
        return dayCount;
    }

    /**
     * Sets the exact number of days displayed in a custom view, regardless of
     * {@link JmixFullCalendar#isWeekendsVisible()} or hidden days.
     *
     * @param dayCount day count to set
     */
    public void setDayCount(@Nullable Integer dayCount) {
        this.dayCount = dayCount;
    }

    /**
     * Sets the exact number of days displayed in a custom view, regardless of
     * {@link JmixFullCalendar#isWeekendsVisible()} or hidden days. See {@link #setDayCount(Integer)}.
     *
     * @param dayCount day count to set
     * @return current instance of custom view
     */
    public CustomCalendarView withDayCount(@Nullable Integer dayCount) {
        this.dayCount = dayCount;
        return this;
    }
}
