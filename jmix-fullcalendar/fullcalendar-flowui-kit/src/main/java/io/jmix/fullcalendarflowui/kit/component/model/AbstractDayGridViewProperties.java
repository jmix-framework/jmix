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

/**
 * Base class for configuring properties of day-grid views.
 */
public abstract class AbstractDayGridViewProperties extends AbstractCalendarViewProperties {

    protected String dayPopoverFormat;

    protected String dayHeaderFormat;

    protected String weekNumberFormat;

    protected String eventTimeFormat;

    protected boolean displayEventEnd = false;

    public AbstractDayGridViewProperties(String name) {
        super(name);
    }

    /**
     * @return the day header format or {@code null} if not set
     */
    @Nullable
    public String getDayHeaderFormat() {
        return dayHeaderFormat;
    }

    /**
     * Sets the format of the text that will be displayed on the calendar’s column headings. By default,
     * component sets localized format from messages when is created.
     * <p>
     * The {@code null} value makes component to use value from {@link JmixFullCalendar#getDefaultDayHeaderFormat()}.
     * <p>
     * As component uses <a href="https://fullcalendar.io/docs/moment-plugin">moment plugin</a> for FullCalendar,
     * we should follow the moment.js formatting rules:
     * <a href="https://momentjs.com/docs/#/displaying/format/">Moment.js Documentation</a>
     * <p>
     * For instance, the {@code "dd"} produces {@code Mo}.
     *
     * @param format format to set
     */
    public void setDayHeaderFormat(@Nullable String format) {
        this.dayHeaderFormat = format;

        markAsDirty();
    }

    /**
     * @return the format of the week number or {@code null} if not set
     */
    @Nullable
    public String getWeekNumberFormat() {
        return weekNumberFormat;
    }

    /**
     * Sets the format of the week number that will be displayed when {@link JmixFullCalendar#isWeekNumbersVisible()}
     * is {@code true}.By default, component sets localized format from messages when is created.
     * <p>
     * The {@code null} value makes component to use value from {@link JmixFullCalendar#getDefaultWeekNumberFormat()}.
     * <p>
     * As component uses <a href="https://fullcalendar.io/docs/moment-plugin">moment plugin</a> for FullCalendar,
     * we should follow the moment.js formatting rules:
     * <a href="https://momentjs.com/docs/#/displaying/format/">Moment.js Documentation</a>
     * <p>
     * For instance, the {@code "[Week] w"} produces {@code Week 1} (1, 2, ... 52, 53).
     *
     * @param format format to set
     */
    public void setWeekNumberFormat(@Nullable String format) {
        this.weekNumberFormat = format;

        markAsDirty();
    }

    /**
     * @return the event time format or {@code null} if not set
     */
    @Nullable
    public String getEventTimeFormat() {
        return eventTimeFormat;
    }

    /**
     * Sets the format of the time-text that will be displayed on each event. By default, component sets
     * localized format from messages when is created.
     * <p>
     * The {@code null} value makes component to use value from {@link JmixFullCalendar#getDefaultEventTimeFormat()}.
     * <p>
     * As component uses <a href="https://fullcalendar.io/docs/moment-plugin">moment plugin</a> for FullCalendar,
     * we should follow the moment.js formatting rules:
     * <a href="https://momentjs.com/docs/#/displaying/format/">Moment.js Documentation</a>
     * <p>
     * For instance, the {@code "HH:mm"} produces {@code 00:00} (01, 2, ... 24 : 01, 02 ... 59).
     *
     * @param format format to set
     */
    public void setEventTimeFormat(@Nullable String format) {
        this.eventTimeFormat = format;

        markAsDirty();
    }

    /**
     * @return the day popover format or {@code null} if not set
     */
    @Nullable
    public String getDayPopoverFormat() {
        return dayPopoverFormat;
    }

    /**
     * Sets the date format of title of the popover that is shown when "more" link is clicked. By default,
     * component sets localized format from messages when is created.
     * <p>
     * The {@code null} value makes component to use value from {@link JmixFullCalendar#getDefaultDayPopoverFormat()}.
     * <p>
     * As component uses <a href="https://fullcalendar.io/docs/moment-plugin">moment plugin</a> for FullCalendar,
     * we should follow the moment.js formatting rules:
     * <a href="https://momentjs.com/docs/#/displaying/format/">Moment.js Documentation</a>
     * <p>
     * For instance, the {@code "MMM D, YY"} produces {@code Sep 9, 24}.
     *
     * @param format format to set
     */
    public void setDayPopoverFormat(@Nullable String format) {
        this.dayPopoverFormat = format;

        markAsDirty();
    }

    /**
     * @return {@code true} if an event's end time is visible
     */
    public boolean isDisplayEventEnd() {
        return displayEventEnd;
    }

    /**
     * Determines an event's end time visibility.
     * <p>
     * By default {@code true} for {@link CalendarViewType#DAY_GRID_DAY} and {@code false} for other day-grid views.
     *
     * @param displayEventEnd whether to display end time
     */
    public void setDisplayEventEnd(boolean displayEventEnd) {
        this.displayEventEnd = displayEventEnd;

        markAsDirty();
    }
}
