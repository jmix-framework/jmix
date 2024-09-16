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

package io.jmix.fullcalendarflowui.component.event;

import io.jmix.fullcalendarflowui.component.model.DayOfWeek;
import io.jmix.fullcalendarflowui.component.FullCalendar;
import io.jmix.fullcalendarflowui.kit.component.model.CalendarDisplayModes;
import org.springframework.lang.Nullable;

import java.time.LocalDate;

/**
 * The context for generating class names that day header cell will use.
 */
public class DayHeaderClassNamesContext extends AbstractFullCalendarContext {

    protected final LocalDate date;

    protected final DayOfWeek dayOfWeek;

    protected final boolean isDisabled;

    protected final boolean isFuture;

    protected final boolean isOther;

    protected final boolean isPast;

    protected final boolean isToday;

    protected final DisplayModeInfo displayModeInfo;

    public DayHeaderClassNamesContext(FullCalendar fullCalendar,
                                      @Nullable LocalDate date,
                                      DayOfWeek dayOfWeek,
                                      boolean isDisabled,
                                      boolean isFuture,
                                      boolean isOther,
                                      boolean isPast,
                                      boolean isToday,
                                      DisplayModeInfo displayModeInfo) {
        super(fullCalendar);
        this.date = date;
        this.dayOfWeek = dayOfWeek;
        this.isDisabled = isDisabled;
        this.isFuture = isFuture;
        this.isOther = isOther;
        this.isPast = isPast;
        this.isToday = isToday;
        this.displayModeInfo = displayModeInfo;
    }

    /**
     * Note, for {@link CalendarDisplayModes#DAY_GRID_MONTH} and {@link CalendarDisplayModes#DAY_GRID_YEAR} it returns
     * {@code null} value.
     *
     * @return date that corresponds to day header cell
     */
    @Nullable
    public LocalDate getDate() {
        return date;
    }

    /**
     * @return day header's day of week
     */
    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    /**
     * Cell can be disabled, for instance, if it is not in valid date range. See
     * {@link FullCalendar#setValidRange(LocalDate, LocalDate)}.
     * <p>
     * Note, for {@link CalendarDisplayModes#DAY_GRID_MONTH} and {@link CalendarDisplayModes#DAY_GRID_YEAR} it always
     * returns {@code false} value.
     *
     * @return {@code true} if day header cell is disabled
     */
    public boolean isDisabled() {
        return isDisabled;
    }

    /**
     * Note, for {@link CalendarDisplayModes#DAY_GRID_MONTH} and {@link CalendarDisplayModes#DAY_GRID_YEAR} it always
     * returns {@code false} value.
     *
     * @return whether the day header cell's date is in future compared with today's date
     */
    public boolean isFuture() {
        return isFuture;
    }

    /**
     * Note, for {@link CalendarDisplayModes#DAY_GRID_MONTH} and {@link CalendarDisplayModes#DAY_GRID_YEAR} it always
     * returns {@code false} value.
     *
     * @return whether the day header cell's date is in other month
     */
    public boolean isOther() {
        return isOther;
    }

    /**
     * Note, for {@link CalendarDisplayModes#DAY_GRID_MONTH} and {@link CalendarDisplayModes#DAY_GRID_YEAR} it always
     * returns {@code false} value.
     *
     * @return whether the day header cell's date is in past compared with today's date
     */
    public boolean isPast() {
        return isPast;
    }

    /**
     * Note, for {@link CalendarDisplayModes#DAY_GRID_MONTH} and {@link CalendarDisplayModes#DAY_GRID_YEAR} it always
     * returns {@code false} value.
     *
     * @return whether the day header cell's date is today
     */
    public boolean isToday() {
        return isToday;
    }

    /**
     * @return information about current calendar's display mode
     */
    public DisplayModeInfo getDisplayModeInfo() {
        return displayModeInfo;
    }
}
