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

package io.jmix.fullcalendarflowui.component.data;

import io.jmix.fullcalendarflowui.component.model.DaysOfWeek;
import io.jmix.fullcalendarflowui.component.model.Display;
import io.jmix.fullcalendarflowui.component.FullCalendar;
import io.jmix.fullcalendarflowui.kit.component.JmixFullCalendar;
import io.jmix.fullcalendarflowui.kit.component.model.CalendarDisplayModes;
import io.jmix.fullcalendarflowui.kit.component.model.JsFunction;
import org.springframework.lang.Nullable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetTime;
import java.util.Map;
import java.util.TimeZone;

/**
 * The base interface of event in {@link FullCalendar}.
 *
 * @see SimpleCalendarEvent
 * @see EntityCalendarEvent
 */
public interface CalendarEvent {

    /**
     * Returns a unique ID of calendar event, that will be used for identification on client-side.
     *
     * @return ID of calendar event or {@code null} if not set
     */
    Object getId();

    /**
     * Returns a group object that is shared between other evens. Events with the same group ID will be
     * dragged and resized together automatically.
     *
     * @return group ID object or {@code null} if not set
     */
    @Nullable
    Object getGroupId();

    /**
     * Determines if the event is shown in the “all-day” section of relevant {@link CalendarDisplayModes}.
     * The default value is {@code false}.
     * <p>
     * Note, the {@code null} value means not all day event.
     *
     * @return {true} if the event is shown in the “all-day” section or {@code null} if not set
     */
    @Nullable
    Boolean getAllDay();

    /**
     * Sets whether the event should be shown in the "all-day" section of relevant {@link CalendarDisplayModes}.
     * In addition, if {@code true} the time text is not displayed with the event. The default value is {@code false}.
     * <p>
     * Note, the {@code null} value means not all-day event.
     *
     * @param allDay allDay option
     */
    void setAllDay(@Nullable Boolean allDay);

    /**
     * Returns the start date time object that corresponds to system default time zone: {@link TimeZone#getDefault()}.
     * <p>
     * For {@link EntityCalendarEvent} value automatically transformed from supported by entity date-time object to
     * {@link LocalDateTime}.
     *
     * @return start date time object that corresponds to system default time zone or {@code null} if not set
     */
    @Nullable
    LocalDateTime getStartDateTime();

    /**
     * Sets start date time value that corresponds to system default time zone: {@link TimeZone#getDefault()}.
     * <p>
     * For {@link EntityCalendarEvent} value automatically transformed from {@link LocalDateTime} to supported
     * by entity date-time object to.
     *
     * @param startDateTime start date-time
     */
    void setStartDateTime(@Nullable LocalDateTime startDateTime);

    /**
     * Returns the end date time object that corresponds to system default time zone: {@link TimeZone#getDefault()}.
     * <p>
     * For {@link EntityCalendarEvent} value automatically transformed from supported by entity date-time object to
     * {@link LocalDateTime}.
     *
     * @return end date time value that corresponds to system default time zone or {@code null} if not set
     */
    @Nullable
    LocalDateTime getEndDateTime();

    /**
     * Sets end date time value that corresponds to system default time zone: {@link TimeZone#getDefault()}.
     * <p>
     * Note that this value is <strong>exclusive</strong>. For instance, ab event with end property -
     * {@code 2024-09-03} will appear to span through {code 2024-09-02} but before the start of {@code 2024-09-03}.
     * <p>
     * For {@link EntityCalendarEvent} value automatically transformed from {@link LocalDateTime} to supported
     * by entity date-time object to.
     *
     * @param endDateTime end date-time
     */
    void setEndDateTime(@Nullable LocalDateTime endDateTime);

    /**
     * @return a text that will appear on an event
     */
    @Nullable
    String getTitle();

    /**
     * @return an event description
     */
    @Nullable
    String getDescription();

    /**
     * Defines whether events can be navigated by TAB key.
     * <p>
     * If value is {@code null}, the event interactivity will be managed by {@link FullCalendar#isEventInteractive()}.
     *
     * @return {@code true} if the event should be navigable by TAB key or {@code null} if not set
     */
    @Nullable
    Boolean getInteractive();

    /**
     * @return a class name or class names separated by space that should be attached to the rendered event
     */
    @Nullable
    String getClassNames();

    /**
     * Determines whether an event can be dragged in the calendar component. This value override the
     * {@link FullCalendar#setEventStartEditable(boolean)} property.
     * <p>
     * Note, {@code null} value means that the ability of editing start position will be managed by component's
     * property.
     * <p>
     * For instance, for calendar {@code eventStartEditable=true}:
     * <ul>
     *     <li>and event's {@code startEditable=true} - event start is editable</li>
     *     <li>and event's {@code startEditable=false} - event start is not editable</li>
     *     <li>and event's {@code startEditable=null} - event start is editable</li>
     * </ul>
     * For calendar {@code eventStartEditable=false}:
     * <ul>
     *     <li>and event's {@code startEditable=true} - event start is editable</li>
     *     <li>and event's {@code startEditable=false} - event start is not editable</li>
     *     <li>and event's {@code startEditable=null} - event start is not editable</li>
     * </ul>
     *
     * @return {@code false} if an event should not provide the ability to edit event start position
     */
    @Nullable
    Boolean getStartEditable();

    /**
     * Determines whether an event can be resized in the calendar component. This value override the
     * {@link JmixFullCalendar#setEventDurationEditable(boolean)} property.
     * <p>
     * Note, {@code null} value means that the ability of event resizing will be managed by component's
     * property.
     * <p>
     * For instance, for calendar {@code eventDurationEditable=true}:
     * <ul>
     *     <li>and event's {@code durationEditable=true} - event duration is editable</li>
     *     <li>and event's {@code durationEditable=false} - event duration is not editable</li>
     *     <li>and event's {@code durationEditable=null} - event duration is editable</li>
     * </ul>
     * For calendar {@code eventStartEditable=false}:
     * <ul>
     *     <li>and event's {@code durationEditable=true} - event duration is editable</li>
     *     <li>and event's {@code durationEditable=false} - event duration is not editable</li>
     *     <li>and event's {@code durationEditable=null} - event duration is not editable</li>
     * </ul>
     *
     * @return {@code false} if an event should not be resized
     */
    @Nullable
    Boolean getDurationEditable();

    /**
     * Defines the type of event rendering. If not specified, the {@link Display#AUTO} will be used by default.
     *
     * @return the type of event rendering or {@code null} if not set
     */
    @Nullable
    Display getDisplay();

    /**
     * Defines whether the event can be dragged/resized over other events and prevents other events from being
     * dragged/resized over this event.
     * <p>
     * Note if value is {@code null}, the ability of overlapping will be managed by
     * {@link FullCalendar#isEventOverlap()} or {@link FullCalendar#getEventOverlapJsFunction()}.
     * <p>
     * For instance, for calendar's {@code eventOverlap=true}:
     * <ul>
     *     <li>and event's {@code overlap=true} - event can be overlapped</li>
     *     <li>and event's {@code overlap=false} - event cannot be overlapped</li>
     *     <li>and event's {@code overlap=null} - event can be overlapped</li>
     * </ul>
     * For calendar {@code eventOverlap=false}:
     * <ul>
     *     <li>and event's {@code overlap=true} - event can be overlapped</li>
     *     <li>and event's {@code overlap=false} - event cannot be overlapped</li>
     *     <li>and event's {@code overlap=null} - event cannot be overlapped</li>
     * </ul>
     *
     * @return {@code true} if event should be overlapped
     */
    @Nullable
    Boolean getOverlap();

    /**
     * A group ID of other events. This property limits dragging and resizing to a certain cells in component.
     * <p>
     * If value is {@code null}, the event constraints will be managed by component's
     * {@link FullCalendar#getEventConstraintGroupId()} and {@link FullCalendar#getEventConstraintBusinessHours()}
     *
     * @return a constraint object or {@code null} if not set
     */
    @Nullable
    Object getConstraint();

    /**
     * Specifies the background color for event. Supported values are:
     * <ul>
     *     <li>
     *         <code>#f00</code>
     *     </li>
     *     <li>
     *         <code>#ff0000</code>
     *     </li>
     *     <li>
     *         <code>rgb(255,0,0)</code>
     *     </li>
     *     <li>
     *         Color name - <code>red</code>
     *     </li>
     * </ul>
     *  Note, if the background color is {@code null}, color will be managed by component's
     *  {@link FullCalendar#getEventBackgroundColor()}.
     *
     * @return background color or {@code null} if not set
     */
    @Nullable
    String getBackgroundColor();

    /**
     * Specifies the border color for event. Supported values are:
     * <ul>
     *     <li>
     *         <code>#f00</code>
     *     </li>
     *     <li>
     *         <code>#ff0000</code>
     *     </li>
     *     <li>
     *         <code>rgb(255,0,0)</code>
     *     </li>
     *     <li>
     *         Color name - <code>red</code>
     *     </li>
     * </ul>
     *  Note, if the border color is {@code null}, color will be managed by component's
     *  {@link FullCalendar#getEventBorderColor()}.
     *
     * @return border color or {@code null} if not set
     */
    @Nullable
    String getBorderColor();

    /**
     * Specifies the text color for event. Supported values are:
     * <ul>
     *     <li>
     *         <code>#f00</code>
     *     </li>
     *     <li>
     *         <code>#ff0000</code>
     *     </li>
     *     <li>
     *         <code>rgb(255,0,0)</code>
     *     </li>
     *     <li>
     *         Color name - <code>red</code>
     *     </li>
     * </ul>
     *  Note, if the text color is {@code null}, color will be managed by component's
     *  {@link FullCalendar#getEventTextColor()}. The color applies in time-grid display modes and for
     *  all-day events in day-grid display modes.
     *
     * @return text color or {@code null} if not set
     */
    @Nullable
    String getTextColor();

    /**
     * Defines additional properties and their values that will be available in various JS functions that takes
     * an event as parameter. For instance, see {@link FullCalendar#setEventOrderJsFunction(JsFunction)}.
     *
     * @return map of additional properties and their values
     */
    @Nullable
    Map<String, Object> getAdditionalProperties();

    /**
     * Specifies the days of the week this event repeats. If omitted, the event is assumed to repeat every day.
     *
     * @return the days of the week this event repeats
     */
    @Nullable
    DaysOfWeek getRecurringDaysOfWeek();

    /**
     * Returns the date when recurrences of this event start. If it is not specified, recurrences will extend
     * infinitely into the past.
     *
     * @return the start date of recurring event
     */
    @Nullable
    LocalDate getRecurringStartDate();

    /**
     * Returns the date when recurrences of this event end. If it is not specified, recurrences will extend
     * infinitely into the future.
     * <p>
     * Note that this value is <strong>exclusive</strong>. For all day recurring events, make end date the day
     * after you want your last recurrence.
     *
     * @return the end date of recurring event
     */
    @Nullable
    LocalDate getRecurringEndDate();

    /**
     * Returns the start time of recurring event. If it is not defined, the event considered as all day event.
     * <p>
     * Note that calendar does not apply TimeZone conversion for this property. If {@link OffsetTime} type is used in
     * entity it will be transformed to system default time zone.
     *
     * @return the start time of recurring event
     */
    @Nullable
    LocalTime getRecurringStartTime();

    /**
     * Returns the end time of recurring event. If it is not defined, the event will appear to have the default
     * duration as configured in {@link FullCalendar} component.
     * <p>
     * Note that calendar does not apply TimeZone conversion for this property. If {@link OffsetTime} type is used in
     * entity it will be transformed to system default time zone.
     *
     * @return the end time of recurring event
     */
    @Nullable
    LocalTime getRecurringEndTime();
}
