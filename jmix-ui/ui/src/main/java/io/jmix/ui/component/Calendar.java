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

package io.jmix.ui.component;

import io.jmix.core.common.event.Subscription;
import io.jmix.ui.component.calendar.CalendarEvent;
import io.jmix.ui.component.calendar.CalendarEventProvider;
import io.jmix.ui.component.calendar.ContainerCalendarEventProvider;
import io.jmix.ui.meta.CanvasBehaviour;
import io.jmix.ui.meta.PropertiesConstraint;
import io.jmix.ui.meta.PropertiesGroup;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioCollection;
import io.jmix.ui.meta.StudioComponent;
import io.jmix.ui.meta.StudioProperties;
import io.jmix.ui.meta.StudioProperty;

import javax.annotation.Nullable;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.time.DayOfWeek;
import java.time.Month;
import java.util.EventObject;
import java.util.Map;
import java.util.TimeZone;
import java.util.function.Consumer;

/**
 * Calendar is used for visualizing events in a calendar using week or month view.
 *
 * @param <V> type of value
 */
@StudioComponent(
        caption = "Calendar",
        category = "Components",
        xmlElement = "calendar",
        icon = "io/jmix/ui/icon/component/calendar.svg",
        canvasBehaviour = CanvasBehaviour.CALENDAR,
        documentationURL = "https://docs.jmix.io/jmix/%VERSION%/ui/vcl/components/calendar.html"
)
@StudioProperties(
        properties = {
                @StudioProperty(name = "dataContainer", type = PropertyType.COLLECTION_DATACONTAINER_REF, typeParameter = "V"),
                @StudioProperty(name = "captionProperty", type = PropertyType.PROPERTY_PATH_REF, options = "string"),
                @StudioProperty(name = "descriptionProperty", type = PropertyType.PROPERTY_PATH_REF, options = "string"),
                @StudioProperty(name = "stylenameProperty", type = PropertyType.PROPERTY_PATH_REF, options = "string"),
                @StudioProperty(name = "isAllDayProperty", type = PropertyType.PROPERTY_PATH_REF, options = "boolean"),
                @StudioProperty(name = "startDateProperty", type = PropertyType.PROPERTY_PATH_REF,
                        options = {"date", "dateTime", "localDate", "localDateTime", "offsetDateTime"},
                        typeParameter = "V"),
                @StudioProperty(name = "endDateProperty", type = PropertyType.PROPERTY_PATH_REF,
                        options = {"date", "dateTime", "localDate", "localDateTime", "offsetDateTime"},
                        typeParameter = "V"),
                @StudioProperty(name = "datatype", type = PropertyType.DATATYPE_ID, options = {"date", "dateTime",
                        "localDate", "localDateTime", "offsetDateTime"}, typeParameter = "V")
        },
        groups = {
                @PropertiesGroup(constraint = PropertiesConstraint.ALL_OR_NOTHING,
                        properties = {"dataContainer", "captionProperty"}),
                @PropertiesGroup(constraint = PropertiesConstraint.ALL_OR_NOTHING,
                        properties = {"dataContainer", "descriptionProperty"}),
                @PropertiesGroup(constraint = PropertiesConstraint.ALL_OR_NOTHING,
                        properties = {"dataContainer", "stylenameProperty"}),
                @PropertiesGroup(constraint = PropertiesConstraint.ALL_OR_NOTHING,
                        properties = {"dataContainer", "isAllDayProperty"}),
                @PropertiesGroup(constraint = PropertiesConstraint.ALL_OR_NOTHING,
                        properties = {"dataContainer", "startDateProperty"}),
                @PropertiesGroup(constraint = PropertiesConstraint.ALL_OR_NOTHING,
                        properties = {"dataContainer", "endDateProperty"})
        }
)
public interface Calendar<V> extends Component.BelongToFrame, Component.HasCaption, Component.HasIcon,
        HasContextHelp, HasHtmlCaption, HasHtmlDescription, HasDatatype<V>, HasHtmlSanitizer {

    String NAME = "calendar";

    /**
     * Set start date for the calendar range.
     */
    @StudioProperty(name = "startDate", type = PropertyType.DATE)
    void setStartDate(@Nullable V date);

    /**
     * @return the start date for the calendar range.
     */
    @Nullable
    V getStartDate();

    /**
     * Set end date for the calendar's range.
     */
    @StudioProperty(name = "endDate", type = PropertyType.DATE)
    void setEndDate(@Nullable V date);

    /**
     * @return the last date for the calendar range.
     */
    @Nullable
    V getEndDate();

    /**
     * Set timezone.
     */
    void setTimeZone(TimeZone zone);

    /**
     * @return timezone.
     */
    TimeZone getTimeZone();

    /**
     * Set format for time. 12H/24H.
     */
    @StudioProperty(name = "timeFormat", type = PropertyType.ENUMERATION, defaultValue = "24H", options = {"12H", "24H"})
    void setTimeFormat(TimeFormat format);

    /**
     * @return enumeration of ite format.
     */
    TimeFormat getTimeFormat();

    /**
     * Set first day of the week to show.
     */
    @StudioProperty(name = "firstVisibleDayOfWeek", defaultValue = "1")
    @Min(value = 1)
    @Max(value = 7)
    void setFirstVisibleDayOfWeek(int firstDay);

    /**
     * @return first showed day of the week.
     */
    int getFirstVisibleDayOfWeek();

    /**
     * Set last day of the week to show.
     */
    @StudioProperty(name = "lastVisibleDayOfWeek", defaultValue = "7")
    @Min(value = 1)
    @Max(value = 7)
    void setLastVisibleDayOfWeek(int lastDay);

    /**
     * @return last showed day of the week.
     */
    int getLastVisibleDayOfWeek();

    /**
     * Set first hour of the day to show.
     */
    @StudioProperty(name = "firstVisibleHourOfDay", defaultValue = "0")
    @Min(value = 0)
    @Max(value = 23)
    void setFirstVisibleHourOfDay(int firstHour);

    /**
     * @return first showed hour of the day.
     */
    int getFirstVisibleHourOfDay();

    /**
     * Set last hour of the day to show.
     */
    @StudioProperty(name = "lastVisibleHourOfDay", defaultValue = "23")
    @Min(value = 0)
    @Max(value = 23)
    void setLastVisibleHourOfDay(int lastHour);

    /**
     * @return last showed hour of the day.
     */
    int getLastVisibleHourOfDay();

    /**
     * Allows setting first day of week independent of Locale.
     * <p>
     * Pass {@code null} to use a day of week defined by current locale.
     * </p>
     *
     * @param dayOfWeek any of java.util.Calendar.SUNDAY ... java.util.Calendar.SATURDAY
     *                  or null to revert to default first day of week by locale
     */
    void setFirstDayOfWeek(Integer dayOfWeek);

    /**
     * Set date caption format for the weekly view.
     */
    @StudioProperty(name = "weeklyCaptionFormat", type = PropertyType.DATE_FORMAT)
    void setWeeklyCaptionFormat(String dateFormatPattern);

    /**
     * @return date pattern of captions.
     */
    String getWeeklyCaptionFormat();

    /**
     * Set the calendar event provider. Provider can contain calendar events.
     *
     * @param calendarEventProvider an event provider with events
     * @see ContainerCalendarEventProvider
     */
    void setEventProvider(@Nullable CalendarEventProvider calendarEventProvider);

    /**
     * @return calendar event provider.
     */
    CalendarEventProvider getEventProvider();

    /**
     * Set visibility for the backward and forward buttons.
     */
    @StudioProperty(name = "navigationButtonsVisible", defaultValue = "false")
    void setNavigationButtonsVisible(boolean value);

    /**
     * @return backward and forward buttons visibility.
     */
    boolean isNavigationButtonsVisible();

    /**
     * @return {@link DayOfWeek} values matched to localized day names
     */
    Map<DayOfWeek, String> getDayNames();

    /**
     * Sets localized Calendar day names.
     *
     * @param dayNames {@link DayOfWeek} values matched to localized day names
     */
    @StudioCollection(xmlElement = "dayNames",
            itemXmlElement = "day",
            itemCaption = "Day Name",
            itemProperties = {
                    @StudioProperty(name = "dayOfWeek", type = PropertyType.ENUMERATION,
                            options = {"@link java.time.DayOfWeek"}, required = true),
                    @StudioProperty(name = "value", type = PropertyType.LOCALIZED_STRING, required = true)
            },
            icon = "io/jmix/ui/icon/element/dayNames.svg",
            itemIcon = "io/jmix/ui/icon/element/dayName.svg"
    )
    void setDayNames(Map<DayOfWeek, String> dayNames);

    /**
     * @return {@link Month} values matched to localized month names
     */
    Map<Month, String> getMonthNames();

    /**
     * Sets localized Calendar month names.
     *
     * @param monthNames {@link Month} values matched to localized month names
     */
    @StudioCollection(xmlElement = "monthNames",
            itemXmlElement = "month",
            itemCaption = "Month Name",
            itemProperties = {
                    @StudioProperty(name = "month", type = PropertyType.ENUMERATION,
                            options = {"@link java.time.Month"}, required = true),
                    @StudioProperty(name = "value", type = PropertyType.LOCALIZED_STRING, required = true)
            },
            icon = "io/jmix/ui/icon/element/monthNames.svg",
            itemIcon = "io/jmix/ui/icon/element/monthName.svg"
    )
    void setMonthNames(Map<Month, String> monthNames);

    /**
     * Adds a listener that is invoked when the user clicks on a day number of the month.
     *
     * @param listener a listener to add
     * @return a registration object for removing an event listener
     */
    Subscription addDateClickListener(Consumer<CalendarDateClickEvent<V>> listener);

    /**
     * Adds a listener that is invoked when the user clicks on an event.
     *
     * @param listener a listener to add
     * @return a registration object for removing an event listener
     */
    Subscription addEventClickListener(Consumer<CalendarEventClickEvent<V>> listener);

    /**
     * Adds a listener that is invoked when the user changes an event duration.
     *
     * @param listener a listener to add
     * @return a registration object for removing an event listener
     */
    Subscription addEventResizeListener(Consumer<CalendarEventResizeEvent<V>> listener);

    /**
     * Adds a listener that is invoked when the user changes an event position.
     *
     * @param listener a listener to add
     * @return a registration object for removing an event listener
     */
    Subscription addEventMoveListener(Consumer<CalendarEventMoveEvent<V>> listener);

    /**
     * Adds a listener that is invoked when the user clicks on a week number.
     *
     * @param listener a listener to add
     * @return a registration object for removing an event listener
     */
    Subscription addWeekClickListener(Consumer<CalendarWeekClickEvent<V>> listener);

    /**
     * Adds a listener that is invoked when the user clicks forward navigation button.
     *
     * @param listener a listener to add
     * @return a registration object for removing an event listener
     */
    Subscription addForwardClickListener(Consumer<CalendarForwardClickEvent<V>> listener);

    /**
     * Adds a listener that is invoked when the user clicks backward navigation button.
     *
     * @param listener a listener to add
     * @return a registration object for removing an event listener
     */
    Subscription addBackwardClickListener(Consumer<CalendarBackwardClickEvent<V>> listener);

    /**
     * Adds a listener that is invoked when the user drag-marks day or time cells using mouse.
     *
     * @param listener a listener to add
     * @return a registration object for removing an event listener
     */
    Subscription addRangeSelectListener(Consumer<CalendarRangeSelectEvent<V>> listener);

    /**
     * Adds a listener that is invoked when the user clicks on an empty space in the day.
     *
     * @param listener a listener to add
     * @return a registration object for removing an event listener
     */
    Subscription addDayClickListener(Consumer<CalendarDayClickEvent<V>> listener);

    enum TimeFormat {
        FORMAT_12H, FORMAT_24H
    }

    /**
     * An event object that is fired when the user changes an event position.
     *
     * @param <V> type of value
     * @see #addEventMoveListener(Consumer)
     */
    class CalendarEventMoveEvent<V> extends EventObject {
        protected CalendarEvent<V> calendarEvent;
        protected V newStart;
        protected V newEnd;
        protected Object entity;

        public CalendarEventMoveEvent(Calendar<V> calendar, CalendarEvent<V> calendarEvent, V newStart, @Nullable Object entity) {
            super(calendar);

            this.calendarEvent = calendarEvent;
            this.newStart = newStart;
            this.entity = entity;
        }

        public CalendarEventMoveEvent(Calendar<V> calendar, CalendarEvent<V> calendarEvent, @Nullable V newStart,
                                      @Nullable V newEnd, @Nullable Object entity) {
            super(calendar);

            this.calendarEvent = calendarEvent;
            this.newStart = newStart;
            this.newEnd = newEnd;
            this.entity = entity;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Calendar<V> getSource() {
            return (Calendar<V>) super.getSource();
        }

        public CalendarEvent<V> getCalendarEvent() {
            return calendarEvent;
        }

        /**
         * @return the event start date
         */
        @Nullable
        public V getNewStart() {
            return newStart;
        }

        /**
         * @return the event end date
         */
        @Nullable
        public V getNewEnd() {
            return newEnd;
        }

        /**
         * @return moved event entity or null if it is not entity based event
         */
        @Nullable
        public Object getEntity() {
            return entity;
        }
    }

    /**
     * An event object that is fired when the user clicks backward navigation button.
     *
     * @param <V> type of value
     * @see #addBackwardClickListener(Consumer)
     */
    class CalendarBackwardClickEvent<V> extends EventObject {

        public CalendarBackwardClickEvent(Calendar<V> calendar) {
            super(calendar);
        }

        @SuppressWarnings("unchecked")
        @Override
        public Calendar<V> getSource() {
            return (Calendar<V>) super.getSource();
        }
    }

    /**
     * An event object that is fired when the user clicks on a day number of the month.
     *
     * @param <V> type of value
     * @see #addDateClickListener(Consumer)
     */
    class CalendarDateClickEvent<V> extends EventObject {
        protected V date;

        public CalendarDateClickEvent(Calendar<V> calendar, @Nullable V date) {
            super(calendar);

            this.date = date;
        }

        /**
         * @return clicked date
         */
        @Nullable
        public V getDate() {
            return date;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Calendar<V> getSource() {
            return (Calendar<V>) super.getSource();
        }
    }

    /**
     * An event object that is fired when the user clicks on an event.
     *
     * @param <V> type of value
     * @see #addEventClickListener(Consumer)
     */
    class CalendarEventClickEvent<V> extends EventObject {
        protected CalendarEvent<V> calendarEvent;
        protected Object entity;

        public CalendarEventClickEvent(Calendar<V> calendar, CalendarEvent<V> calendarEvent, @Nullable Object entity) {
            super(calendar);

            this.calendarEvent = calendarEvent;
            this.entity = entity;
        }

        /**
         * @return event entity that was clicked or null if it is not entity based event
         */
        @Nullable
        public Object getEntity() {
            return entity;
        }

        public CalendarEvent<V> getCalendarEvent() {
            return calendarEvent;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Calendar<V> getSource() {
            return (Calendar<V>) super.getSource();
        }
    }

    /**
     * An event object that is fired when the user clicks forward navigation button.
     *
     * @param <V> type of value
     * @see #addForwardClickListener(Consumer)
     */
    class CalendarForwardClickEvent<V> extends EventObject {
        public CalendarForwardClickEvent(Calendar<V> calendar) {
            super(calendar);
        }

        @SuppressWarnings("unchecked")
        @Override
        public Calendar<V> getSource() {
            return (Calendar<V>) super.getSource();
        }
    }

    /**
     * An event object that is fired when the user changes an event duration.
     *
     * @param <V> type of value
     * @see #addEventResizeListener(Consumer)
     */
    class CalendarEventResizeEvent<V> extends EventObject {
        protected CalendarEvent<V> calendarEvent;
        protected V newStart;
        protected V newEnd;
        protected Object entity;

        public CalendarEventResizeEvent(Calendar<V> calendar, CalendarEvent<V> calendarEvent, @Nullable V newStart,
                                        @Nullable V newEnd, @Nullable Object entity) {
            super(calendar);

            this.calendarEvent = calendarEvent;
            this.newEnd = newEnd;
            this.newStart = newStart;
            this.entity = entity;
        }

        /**
         * @return event entity that was resized or null if it is not entity based event
         */
        @Nullable
        public Object getEntity() {
            return entity;
        }

        public CalendarEvent<V> getCalendarEvent() {
            return calendarEvent;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Calendar<V> getSource() {
            return (Calendar<V>) super.getSource();
        }

        /**
         * @return the event start date
         */
        @Nullable
        public V getNewStart() {
            return newStart;
        }

        /**
         * @return the event end date
         */
        @Nullable
        public V getNewEnd() {
            return newEnd;
        }
    }

    /**
     * An event object that is fired when the user clicks on a week number.
     *
     * @param <V> type of value
     * @see #addWeekClickListener(Consumer)
     */
    class CalendarWeekClickEvent<V> extends EventObject {
        protected int week;
        protected int year;

        public CalendarWeekClickEvent(Calendar<V> calendar, int week, int year) {
            super(calendar);

            this.week = week;
            this.year = year;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Calendar<V> getSource() {
            return (Calendar<V>) super.getSource();
        }

        /**
         * @return a week number
         */
        public int getWeek() {
            return week;
        }

        /**
         * @return a year
         */
        public int getYear() {
            return year;
        }
    }

    /**
     * An event object that is fired when the user drag-marks day or time cells using mouse.
     *
     * @param <V> type of value
     * @see #addRangeSelectListener(Consumer)
     */
    class CalendarRangeSelectEvent<V> extends EventObject {
        protected V start;
        protected V end;

        public CalendarRangeSelectEvent(Calendar<V> calendar, @Nullable V start, @Nullable V end) {
            super(calendar);

            this.start = start;
            this.end = end;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Calendar<V> getSource() {
            return (Calendar<V>) super.getSource();
        }

        @Nullable
        public V getStart() {
            return start;
        }

        @Nullable
        public V getEnd() {
            return end;
        }
    }

    /**
     * An event object that is fired when the user clicks on an empty space in the day.
     *
     * @param <V> type of value
     * @see #addDayClickListener(Consumer)
     */
    class CalendarDayClickEvent<V> extends EventObject {

        protected V date;

        public CalendarDayClickEvent(Calendar<V> source, @Nullable V date) {
            super(source);

            this.date = date;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Calendar<V> getSource() {
            return (Calendar<V>) super.getSource();
        }

        /**
         * If calendar value type supports time (e.g. {@link java.util.Date}) date will contain time value for day and
         * week calendar view. Calendar with month view does not provide time value.
         *
         * @return date which user clicked on
         */
        @Nullable
        public V getDate() {
            return date;
        }
    }
}
