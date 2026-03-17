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
import org.jspecify.annotations.Nullable;

/**
 * Configuration properties of day-grid year display mode {@link CalendarDisplayModes#DAY_GRID_YEAR}.
 * <p>
 * The properties can be retrieved from {@link JmixFullCalendar#getCalendarDisplayModeProperties(CalendarDisplayModes)}.
 * For instance:
 * <pre>{@code
 * calendar.getCalendarDisplayModeProperties(CalendarDisplayModes.DAY_GRID_YEAR);
 * }</pre>
 */
public class DayGridYearProperties extends AbstractDayGridProperties {

    protected String monthStartFormat;

    public DayGridYearProperties() {
        super(CalendarDisplayModes.DAY_GRID_YEAR.getId());
    }

    /**
     * @return the text format for the first cell of each month or {@code null} if not set
     */
    @Nullable
    public String getMonthStartFormat() {
        return monthStartFormat;
    }

    /**
     * Sets the format of the text format for the first cell of each month. By default,
     * component sets localized format from messages when is created.
     * <p>
     * The {@code null} value resets day format to FullCalendar's default.
     * <p>
     * As component uses <a href="https://fullcalendar.io/docs/moment-plugin">moment plugin</a> for FullCalendar,
     * we should follow the moment.js formatting rules:
     * <a href="https://momentjs.com/docs/#/displaying/format/">Moment.js Documentation</a>
     * <p>
     * For instance, the {@code "MMMM"} produces {@code September} ({@code November}, {@code December}, etc.).
     *
     * @param format format to set
     */
    public void setMonthStartFormat(@Nullable String format) {
        this.monthStartFormat = format;

        markAsDirty();
    }
}
