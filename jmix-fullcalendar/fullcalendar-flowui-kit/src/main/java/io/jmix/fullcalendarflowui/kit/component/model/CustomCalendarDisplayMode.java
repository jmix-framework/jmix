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
 * Custom calendar display mode properties. It enables to create new display mode with custom date range and use it
 * along with other display modes. If a date range is not specified it can be managed by
 * {@link JmixFullCalendar#setVisibleRange(LocalDate, LocalDate)}.
 */
public class CustomCalendarDisplayMode extends AbstractCalendarDisplayModeProperties {

    protected CalendarDisplayMode displayMode;

    protected CalendarDisplayMode baseDisplayMode;

    protected CalendarDuration duration;

    protected Integer dayCount;

    /**
     * Creates new instance of custom display mode with the specified ID. The created custom display mode will be
     * based on {@link GenericCalendarDisplayModes#DAY_GRID}.
     *
     * @param id ID to pass
     */
    public CustomCalendarDisplayMode(String id) {
        this(id, null);
    }

    /**
     * Creates new instance of custom display mode with the specified ID and base display mode:
     * {@link GenericCalendarDisplayModes}.
     *
     * @param id   ID to pass
     * @param baseDisplayMode the display mode to base on
     */
    public CustomCalendarDisplayMode(String id, @Nullable CalendarDisplayMode baseDisplayMode) {
        super(id);

        this.displayMode = () -> id;
        this.baseDisplayMode = baseDisplayMode;
    }

    /**
     * @return calendar display mode object from the ID
     */
    public CalendarDisplayMode getDisplayMode() {
        return displayMode;
    }

    /**
     * @return the type of custom calendar display mode
     */
    public CalendarDisplayMode getBaseDisplayMode() {
        return baseDisplayMode == null ? GenericCalendarDisplayModes.DAY_GRID : baseDisplayMode;
    }

    @Nullable
    public CalendarDuration getDuration() {
        return duration;
    }

    /**
     * Sets the exact duration of a custom display mode.
     * <p>
     * Takes precedence over the {@link #setDayCount(Integer)}.
     *
     * @param duration the duration to set
     */
    public void setDuration(@Nullable CalendarDuration duration) {
        this.duration = duration;
    }

    /**
     * Sets the exact duration of a custom display mode. See {@link #setDuration(CalendarDuration)}.
     *
     * @param duration the duration to set
     * @return current instance of custom display mode
     */
    public CustomCalendarDisplayMode withDuration(@Nullable CalendarDuration duration) {
        setDuration(duration);
        return this;
    }

    @Nullable
    public Integer getDayCount() {
        return dayCount;
    }

    /**
     * Sets the exact number of days displayed in a custom display mode, regardless of
     * {@link JmixFullCalendar#isWeekendsVisible()} or hidden days.
     *
     * @param dayCount day count to set
     */
    public void setDayCount(@Nullable Integer dayCount) {
        this.dayCount = dayCount;
    }

    /**
     * Sets the exact number of days displayed in a custom display mode, regardless of
     * {@link JmixFullCalendar#isWeekendsVisible()} or hidden days. See {@link #setDayCount(Integer)}.
     *
     * @param dayCount day count to set
     * @return current instance of custom display mode
     */
    public CustomCalendarDisplayMode withDayCount(@Nullable Integer dayCount) {
        this.dayCount = dayCount;
        return this;
    }
}
