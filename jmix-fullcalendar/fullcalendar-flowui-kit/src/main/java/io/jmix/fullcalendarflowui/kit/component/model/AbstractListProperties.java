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

import org.jspecify.annotations.Nullable;

/**
 * Base class for configuring properties of list display modes.
 */
public abstract class AbstractListProperties extends AbstractCalendarDisplayModeProperties {

    protected String listDayFormat;

    protected boolean listDayVisible = true;

    protected String listDaySideFormat;

    protected boolean listDaySideVisible = true;

    public AbstractListProperties(String name) {
        super(name);
    }

    /**
     * @return the list day format or {@code null} if not set
     */
    @Nullable
    public String getListDayFormat() {
        return listDayFormat;
    }

    /**
     * Sets the format of the text on the left side of the day headings. By default,
     * component sets localized format from messages when is created.
     * <p>
     * The {@code null} value resets day format to FullCalendar's default.
     * <p>
     * As component uses <a href="https://fullcalendar.io/docs/moment-plugin">moment plugin</a> for FullCalendar,
     * we should follow the moment.js formatting rules:
     * <a href="https://momentjs.com/docs/#/displaying/format/">Moment.js Documentation</a>
     * <p>
     * For instance, the {@code "dd"} produces {@code Mo}.
     *
     * @param format format to set
     */
    public void setListDayFormat(@Nullable String format) {
        this.listDayFormat = format;

        markAsDirty();
    }

    /**
     * @return {@code true} if the text on the left side of the day headings is visible
     */
    public boolean isListDayVisible() {
        return listDayVisible;
    }

    /**
     * Sets the visibility of the text on the left side of the day headings.
     * <p>
     * The default value is {@code true}.
     *
     * @param listDayVisible whether to show the list day text
     */
    public void setListDayVisible(boolean listDayVisible) {
        this.listDayVisible = listDayVisible;

        markAsDirty();
    }

    /**
     * @return the list day side format or {@code null} if not set
     */
    @Nullable
    public String getListDaySideFormat() {
        return listDaySideFormat;
    }

    /**
     * Sets the format of the text on the right side of the day headings. By default,
     * component sets localized format from messages when is created.
     * <p>
     * The {@code null} value resets day format to FullCalendar's default.
     * <p>
     * As component uses <a href="https://fullcalendar.io/docs/moment-plugin">moment plugin</a> for FullCalendar,
     * we should follow the moment.js formatting rules:
     * <a href="https://momentjs.com/docs/#/displaying/format/">Moment.js Documentation</a>
     * <p>
     * For instance, the {@code "dd"} produces {@code Mo}.
     *
     * @param format format to set
     */
    public void setListDaySideFormat(@Nullable String format) {
        this.listDaySideFormat = format;

        markAsDirty();
    }

    /**
     * @return {@code true} if the text on the right side of the day headings is visible
     */
    public boolean isListDaySideVisible() {
        return listDaySideVisible;
    }

    /**
     * Sets the visibility of the text on the right side of the day headings.
     * <p>
     * The default value is {@code true}.
     *
     * @param listDaySideVisible whether to show the list day side text
     */
    public void setListDaySideVisible(boolean listDaySideVisible) {
        this.listDaySideVisible = listDaySideVisible;

        markAsDirty();
    }
}
