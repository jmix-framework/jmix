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

import io.jmix.fullcalendar.DaysOfWeek;
import io.jmix.fullcalendar.Display;
import io.jmix.fullcalendarflowui.component.FullCalendar;
import io.jmix.fullcalendarflowui.kit.component.JmixFullCalendar;
import io.jmix.fullcalendarflowui.kit.component.model.CalendarViewType;
import org.springframework.lang.Nullable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetTime;
import java.util.Map;
import java.util.TimeZone;

public interface CalendarEvent {

    /**
     * Returns a unique ID of calendar event, that will be used for identification on client-side.
     *
     * @return ID of calendar event or {@code null} if not set
     */
    Object getId();

    @Nullable
    Object getGroupId();

    /**
     * Determines if the event is shown in the “all-day” section of relevant {@link CalendarViewType}.
     * In addition, if {@code true} the time text is not displayed with the event. The default value is {@code false}.
     * <p>
     * Note, the {@code null} value means not all day event.
     *
     * @return {true} if the event is shown in the “all-day” section
     */
    @Nullable
    Boolean getAllDay();

    void setAllDay(@Nullable Boolean allDay);


    @Nullable
    LocalDateTime getStartDateTime();

    /**
     * Sets start date time value that corresponds to system default time zone: {@link TimeZone#getDefault()}.
     *
     * @param startDateTime start date time
     */
    void setStartDateTime(@Nullable LocalDateTime startDateTime);

    @Nullable
    LocalDateTime getEndDateTime();

    void setEndDateTime(@Nullable LocalDateTime endDateTime);

    @Nullable
    String getTitle();

    @Nullable
    String getDescription();

    @Nullable
    Boolean getInteractive();

    @Nullable
    String getClassNames();

    /**
     * Determines whether an event can be dragged in the calendar component. This value override the
     * {@link JmixFullCalendar#setEventStartEditable(boolean)} property.
     * <p>
     * Note, {@code null} value means that the ability of editing start position will be managed by component's
     * property.
     * <p>
     * For instance, for calendar {@code eventStartEditable=true}:
     * <ul>
     *     <li>and {@code startEditable=true} - event start is editable</li>
     *     <li>and {@code startEditable=false} - event start is not editable</li>
     *     <li>and {@code startEditable=null} - event start is editable</li>
     * </ul>
     * For calendar {@code eventStartEditable=false}:
     * <ul>
     *     <li>and {@code startEditable=true} - event start is editable</li>
     *     <li>and {@code startEditable=false} - event start is not editable</li>
     *     <li>and {@code startEditable=null} - event start is not editable</li>
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
     *     <li>and {@code durationEditable=true} - event duration is editable</li>
     *     <li>and {@code durationEditable=false} - event duration is not editable</li>
     *     <li>and {@code durationEditable=null} - event duration is editable</li>
     * </ul>
     * For calendar {@code eventStartEditable=false}:
     * <ul>
     *     <li>and {@code durationEditable=true} - event duration is editable</li>
     *     <li>and {@code durationEditable=false} - event duration is not editable</li>
     *     <li>and {@code durationEditable=null} - event duration is not editable</li>
     * </ul>
     *
     * @return {@code false} if an event should not be resized
     */
    @Nullable
    Boolean getDurationEditable();

    @Nullable
    Display getDisplay();

    @Nullable
    Boolean getOverlap();

    @Nullable
    Object getConstraint();

    @Nullable
    String getBackgroundColor();

    @Nullable
    String getBorderColor();

    @Nullable
    String getTextColor();

    @Nullable
    Map<String, Object> getAdditionalProperties();

    @Nullable
    DaysOfWeek getRecurringDaysOfWeek();

    /**
     * Returns the date when recurrences of this event start. If it is not specified, recurrences will extend
     * infinitely into the past.
     *
     * @return the start date of recurring event
     */
    @Nullable
    LocalDate getRecurringStarDate();

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
    LocalTime getRecurringStarTime();

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
