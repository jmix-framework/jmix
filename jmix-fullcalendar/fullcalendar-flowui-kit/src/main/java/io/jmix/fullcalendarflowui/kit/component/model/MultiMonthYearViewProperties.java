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
 * Configuration properties of multi-month year view {@link CalendarViewType#MULTI_MONTH_YEAR}.
 * <p>
 * The view properties can be retrieved from {@link JmixFullCalendar#getCalendarViewProperties(CalendarViewType)}.
 * For instance:
 * <pre>{@code
 * calendar.getCalendarViewProperties(CalendarViewType.MULTI_MONTH_YEAR);
 * }</pre>
 */
public class MultiMonthYearViewProperties extends AbstractCalendarViewProperties {

    protected Integer multiMonthMaxColumns;

    protected Integer multiMonthMinWidth;

    protected String multiMonthTitleFormat;

    protected boolean fixedWeekCount = true;

    protected boolean showNonCurrentDates = true;

    public MultiMonthYearViewProperties() {
        super(CalendarViewType.MULTI_MONTH_YEAR.getId());
    }

    /**
     * @return the maximum columns of months or {@code null} if not set
     */
    @Nullable
    public Integer getMultiMonthMaxColumns() {
        return multiMonthMaxColumns;
    }

    /**
     * Sets the maximum columns of months that the view will attempt to render.
     * <p>
     * By default, the view will attempt to display 3 columns of mini-months. If there is insufficient
     * space, requiring each month to be smaller than {@link #getMultiMonthMinWidth()}, fewer columns
     * will be displayed.
     * <p>
     * The default value is {@code 3}.
     *
     * @param multiMonthMaxColumns maximum columns of months
     */
    public void setMultiMonthMaxColumns(@Nullable Integer multiMonthMaxColumns) {
        this.multiMonthMaxColumns = multiMonthMaxColumns;

        markAsDirty();
    }

    @Nullable
    public Integer getMultiMonthMinWidth() {
        return multiMonthMinWidth;
    }

    /**
     * Sets thw minimum width of each mini-month. The component will not allow mini-month to be become smaller
     * than this value.
     * <p>
     * If the available width requires each mini-month to become smaller than this pixel value, the mini-months
     * will wrap to subsequent rows instead.
     * <p>
     * The default value is {@code 350}.
     *
     * @param multiMonthMinWidth minimum width in pixels
     */
    public void setMultiMonthMinWidth(@Nullable Integer multiMonthMinWidth) {
        this.multiMonthMinWidth = multiMonthMinWidth;

        markAsDirty();
    }

    @Nullable
    public String getMultiMonthTitleFormat() {
        return multiMonthTitleFormat;
    }

    /**
     * Sets the format of the text above each month. By default, component sets localized format from messages
     * when is created.
     * <p>
     * The {@code null} value resets day format to FullCalendar's default.
     * <p>
     * As component uses <a href="https://fullcalendar.io/docs/moment-plugin">moment plugin</a> for FullCalendar,
     * we should follow the moment.js formatting rules:
     * <a href="https://momentjs.com/docs/#/displaying/format/">Moment.js Documentation</a>
     * <p>
     * For instance, the {@code "MMM"} produces {@code Sep} ({@code Nov}, {@code Dec}, etc.).
     *
     * @param format format to set
     */
    public void setMultiMonthTitleFormat(@Nullable String format) {
        this.multiMonthTitleFormat = format;

        markAsDirty();
    }

    /**
     * @return {@code true} if calendar displays fixed week count
     */
    public boolean isFixedWeekCount() {
        return fixedWeekCount;
    }

    /**
     * Determines the number of displayed weeks.
     * <p>
     * If {@code true}, the calendar will always be {@code 6} weeks tall. If {@code false}, the calendar will have
     * either {@code 4}, {@code 5}, or {@code 6} weeks, depending on the month.
     * <p>
     * The default value is {@code true}.
     *
     * @param fixedWeekCount whether to display fixed week count
     */
    public void setFixedWeekCount(boolean fixedWeekCount) {
        this.fixedWeekCount = fixedWeekCount;

        markAsDirty();
    }

    /**
     * @return {@code true} if calendar shows dates from next/previous months
     */
    public boolean isShowNonCurrentDates() {
        return showNonCurrentDates;
    }

    /**
     * Determines displaying dates from previous and next months.
     * <p>
     * The default value is {@code true}.
     *
     * @param showNonCurrentDates whether to show dates from next/previous months
     */
    public void setShowNonCurrentDates(boolean showNonCurrentDates) {
        this.showNonCurrentDates = showNonCurrentDates;

        markAsDirty();
    }
}
