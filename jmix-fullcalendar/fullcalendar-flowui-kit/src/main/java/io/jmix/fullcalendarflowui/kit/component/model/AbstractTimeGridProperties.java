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
 * Base class for configuring properties of time-grid display modes.
 */
public abstract class AbstractTimeGridProperties extends AbstractCalendarDisplayModeProperties {

    protected String dayPopoverFormat;

    protected String dayHeaderFormat;

    protected String weekNumberFormat;

    protected String eventTimeFormat;

    protected String slotLabelFormat;

    protected Integer eventMinHeight;

    protected Integer eventShortHeight;

    protected boolean slotEventOverlap = true;

    protected boolean allDaySlot = true;

    protected boolean displayEventEnd = true;

    public AbstractTimeGridProperties(String name) {
        super(name);
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
     * @return the slot label format or {@code null} if not set
     */
    @Nullable
    public String getSlotLabelFormat() {
        return slotLabelFormat;
    }

    /**
     * Sets the format of the text that will be displayed within a time slot. By default, component sets
     * localized format from messages when is created.
     * <p>
     * The {@code null} value makes component to use value from {@link JmixFullCalendar#getDefaultWeekNumberFormat()}.
     * <p>
     * As component uses <a href="https://fullcalendar.io/docs/moment-plugin">moment plugin</a> for FullCalendar,
     * we should follow the moment.js formatting rules:
     * <a href="https://momentjs.com/docs/#/displaying/format/">Moment.js Documentation</a>
     * <p>
     * For instance, the {@code "ha"} produces {@code 1 am} (1, 2, ... 12 am/pm).
     *
     * @param format format to set
     */
    public void setSlotLabelFormat(@Nullable String format) {
        this.slotLabelFormat = format;

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
     * @return the minimum height or {@code null} if not set
     */
    @Nullable
    public Integer getEventMinHeight() {
        return eventMinHeight;
    }

    /**
     * Sets the minimum height that an event can have.
     * <p>
     * The default value is {@code 15}.
     *
     * @param eventMinHeight minimum height
     */
    public void setEventMinHeight(@Nullable Integer eventMinHeight) {
        this.eventMinHeight = eventMinHeight;

        markAsDirty();
    }

    /**
     * @return the event short height or {@code null} if not set
     */
    @Nullable
    public Integer getEventShortHeight() {
        return eventShortHeight;
    }

    /**
     * Sets the height threshold for when an event has a "short" style.
     *
     * @param eventShortHeight the event short height
     * @see <a href="https://fullcalendar.io/docs/eventShortHeight">FullCalendar docs :: eventShortHeight</a>
     */
    public void setEventShortHeight(@Nullable Integer eventShortHeight) {
        this.eventShortHeight = eventShortHeight;

        markAsDirty();
    }

    /**
     * @return {@code true} if events overlap each other
     */
    public boolean isSlotEventOverlap() {
        return slotEventOverlap;
    }

    /**
     * Determines if timed events should visually overlap.
     * <p>
     * The default value is {@code true}.
     *
     * @param slotEventOverlap whether events should overlap each other
     * @see <a href="https://fullcalendar.io/docs/slotEventOverlap">FullCalendar docs :: slotEventOverlap</a>
     */
    public void setSlotEventOverlap(boolean slotEventOverlap) {
        this.slotEventOverlap = slotEventOverlap;

        markAsDirty();
    }

    /**
     * @return {@code true} if all-day slots are displayed
     */
    public boolean isAllDaySlot() {
        return allDaySlot;
    }

    /**
     * Determines if the all-day slots are displayed at the top of the calendar.
     * <p>
     * The default value is {@code true}.
     *
     * @param allDaySlot whether to display all-day slots
     */
    public void setAllDaySlot(boolean allDaySlot) {
        this.allDaySlot = allDaySlot;

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
     * The default value is {@code true}.
     *
     * @param displayEventEnd whether to display end time
     */
    public void setDisplayEventEnd(boolean displayEventEnd) {
        this.displayEventEnd = displayEventEnd;

        markAsDirty();
    }
}
