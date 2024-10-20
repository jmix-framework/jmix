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

import io.jmix.core.common.util.Preconditions;
import io.jmix.fullcalendarflowui.component.model.DaysOfWeek;
import io.jmix.fullcalendarflowui.component.model.Display;
import org.springframework.lang.Nullable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Non JPA implementation of calendar event.
 */
public class SimpleCalendarEvent implements CalendarEvent {

    protected final Object id;
    protected Object groupId;
    protected Object constraint;

    protected Boolean allDay;

    protected LocalDateTime startDateTime;
    protected LocalDateTime endDateTime;

    protected String title;
    protected String description;
    protected Boolean interactive;
    protected String classNames;
    protected Boolean startEditable;
    protected Boolean durationEditable;
    protected Display display;
    protected Boolean overlap;
    protected String backgroundColor;
    protected String borderColor;
    protected String textColor;

    protected Map<String, Object> additionalProperties;

    protected DaysOfWeek recurringDaysOfWeek;
    protected LocalDate recurringStartDate;
    protected LocalDate recurringEndDate;
    protected LocalTime recurringStartTime;
    protected LocalTime recurringEndTime;

    public SimpleCalendarEvent() {
        this(UUID.randomUUID());
    }

    public SimpleCalendarEvent(Object id) {
        Preconditions.checkNotNullArgument(id);
        this.id = id;
    }

    @Override
    public Object getId() {
        return id;
    }

    @Override
    public Object getGroupId() {
        return groupId;
    }

    /**
     * Sets group ID.
     * <p>
     * See full description here: {@link CalendarEvent#getGroupId()}.
     *
     * @param groupId group ID to set.
     */
    public void setGroupId(Object groupId) {
        this.groupId = groupId;
    }

    @Nullable
    @Override
    public Boolean getAllDay() {
        return allDay;
    }

    @Override
    public void setAllDay(@Nullable Boolean allDay) {
        this.allDay = allDay;
    }

    @Override
    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    @Override
    public void setStartDateTime(@Nullable LocalDateTime start) {
        this.startDateTime = start;
    }

    @Override
    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    @Override
    public void setEndDateTime(@Nullable LocalDateTime end) {
        this.endDateTime = end;
    }

    @Override
    public String getTitle() {
        return title;
    }

    /**
     * Sets event title.
     * <p>
     * See full description here: {@link CalendarEvent#getTitle()}.
     *
     * @param title title to set
     */
    public void setTitle(@Nullable String title) {
        this.title = title;
    }

    @Override
    public String getDescription() {
        return description;
    }

    /**
     * Sets event description.
     * <p>
     * See full description here: {@link CalendarEvent#getDescription()}.
     *
     * @param description description to set
     */
    public void setDescription(@Nullable String description) {
        this.description = description;
    }

    @Override
    public Boolean getInteractive() {
        return interactive;
    }

    /**
     * Sets event interactivity.
     * <p>
     * See full description here: {@link CalendarEvent#getInteractive()}.
     *
     * @param interactive whether events can be navigated by TAB key
     */
    public void setInteractive(@Nullable Boolean interactive) {
        this.interactive = interactive;
    }

    @Override
    public String getClassNames() {
        return classNames;
    }

    /**
     * Sets event class names.
     * <p>
     * See full description here: {@link CalendarEvent#getClassNames()}.
     *
     * @param classNames class names to set
     */
    public void setClassNames(@Nullable String classNames) {
        this.classNames = classNames;
    }

    @Nullable
    @Override
    public Boolean getStartEditable() {
        return startEditable;
    }

    /**
     * Sets whether an event can be dragged in the calendar component.
     * <p>
     * See full description here: {@link CalendarEvent#getStartEditable()}.
     *
     * @param startEditable startEditable option
     */
    public void setStartEditable(@Nullable Boolean startEditable) {
        this.startEditable = startEditable;
    }

    @Nullable
    @Override
    public Boolean getDurationEditable() {
        return durationEditable;
    }

    /**
     * Sets whether an event can be resized in the calendar component.
     * <p>
     * See full description here: {@link CalendarEvent#getDurationEditable()}.
     *
     * @param durationEditable durationEditable option
     */
    public void setDurationEditable(@Nullable Boolean durationEditable) {
        this.durationEditable = durationEditable;
    }

    @Override
    public Display getDisplay() {
        return display;
    }

    /**
     * Sets the type of event rendering.
     * <p>
     * See full description here: {@link CalendarEvent#getDisplay()}.
     *
     * @param display the type of event rendering
     */
    public void setDisplay(@Nullable Display display) {
        this.display = display;
    }

    @Override
    public Boolean getOverlap() {
        return overlap;
    }

    /**
     * Sets whether the event can be dragged/ resized over other events
     * and prevents other events from being dragged/ resized over this event
     * <p>
     * See full description here: {@link CalendarEvent#getOverlap()}.
     *
     * @param overlap overlap option
     */
    public void setOverlap(@Nullable Boolean overlap) {
        this.overlap = overlap;
    }

    @Nullable
    @Override
    public Object getConstraint() {
        return constraint;
    }

    /**
     * Sets an event constraint.
     * <p>
     * See full description here: {@link CalendarEvent#getConstraint()}.
     *
     * @param constraint constraint to set
     */
    public void setConstraint(@Nullable Object constraint) {
        this.constraint = constraint;
    }

    @Override
    public String getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Sets the background color for event.
     * <p>
     * See full description here: {@link CalendarEvent#getBackgroundColor()}.
     *
     * @param backgroundColor background color
     */
    public void setBackgroundColor(@Nullable String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    @Override
    public String getBorderColor() {
        return borderColor;
    }

    /**
     * Sets an event border color.
     * <p>
     * See full description here: {@link CalendarEvent#getBorderColor()}.
     *
     * @param borderColor border color
     */
    public void setBorderColor(@Nullable String borderColor) {
        this.borderColor = borderColor;
    }

    @Override
    public String getTextColor() {
        return textColor;
    }

    /**
     * Sets an event text color.
     * <p>
     * See full description here: {@link CalendarEvent#getTextColor()}.
     *
     * @param textColor text color
     */
    public void setTextColor(@Nullable String textColor) {
        this.textColor = textColor;
    }

    @Nullable
    @Override
    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }

    /**
     * Sets additional properties and their values.
     * <p>
     * See full description here: {@link CalendarEvent#getAdditionalProperties()}.
     *
     * @param additionalProperties additional properties
     */
    public void setAdditionalProperties(@Nullable Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    @Override
    public DaysOfWeek getRecurringDaysOfWeek() {
        return recurringDaysOfWeek;
    }

    /**
     * Sets recurring days of week.
     * <p>
     * See full description here: {@link CalendarEvent#getRecurringDaysOfWeek()}.
     *
     * @param recurringDaysOfWeek recurring days of week
     */
    public void setRecurringDaysOfWeek(@Nullable DaysOfWeek recurringDaysOfWeek) {
        this.recurringDaysOfWeek = recurringDaysOfWeek;
    }

    @Override
    public LocalDate getRecurringStartDate() {
        return recurringStartDate;
    }

    /**
     * Sets recurring start date.
     * <p>
     * See full description here: {@link CalendarEvent#getRecurringStartDate()}.
     *
     * @param recurringStartDate recurring start date
     */
    public void setRecurringStartDate(@Nullable LocalDate recurringStartDate) {
        this.recurringStartDate = recurringStartDate;
    }

    @Override
    public LocalDate getRecurringEndDate() {
        return recurringEndDate;
    }

    /**
     * Sets recurring end date.
     * <p>
     * See full description here: {@link CalendarEvent#getRecurringEndDate()}.
     *
     * @param recurringEndDate recurring end date
     */
    public void setRecurringEndDate(@Nullable LocalDate recurringEndDate) {
        this.recurringEndDate = recurringEndDate;
    }

    @Override
    public LocalTime getRecurringStartTime() {
        return recurringStartTime;
    }

    /**
     * Sets recurring start time.
     * <p>
     * See full description here: {@link CalendarEvent#getRecurringStartDate()}.
     *
     * @param recurringStartTime recurring start time
     */
    public void setRecurringStartTime(@Nullable LocalTime recurringStartTime) {
        this.recurringStartTime = recurringStartTime;
    }

    @Override
    public LocalTime getRecurringEndTime() {
        return recurringEndTime;
    }

    /**
     * Sets recurring end time.
     * <p>
     * See full description here: {@link CalendarEvent#getRecurringEndTime()}.
     *
     * @param recurringEndTime recurring end time
     */
    public void setRecurringEndTime(@Nullable LocalTime recurringEndTime) {
        this.recurringEndTime = recurringEndTime;
    }

    /**
     * Creates a builder for convenient event creation. Generates {@link UUID} as an event ID.
     *
     * @return a builder
     */
    public static Builder create() {
        return new Builder();
    }

    /**
     * Creates a builder for convenient event creation.
     *
     * @param id event ID
     * @return a builder
     */
    public static Builder create(Object id) {
        return new Builder(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof SimpleCalendarEvent sObj) {
            return Objects.equals(id, sObj.id);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Builder for creating {@link SimpleCalendarEvent}.
     */
    public static class Builder {

        protected SimpleCalendarEvent event;

        /**
         * Creates builder with {@link UUID} as an event ID.
         */
        public Builder() {
            event = new SimpleCalendarEvent();
        }

        /**
         * Creates builder.
         *
         * @param id event ID
         */
        public Builder(Object id) {
            event = new SimpleCalendarEvent(id);
        }

        /**
         * See full description here: {@link CalendarEvent#getGroupId()}.
         *
         * @return group ID or {@code null} if not set
         */
        @Nullable
        public Object getGroupId() {
            return event.getGroupId();
        }

        /**
         * Sets group ID.
         * <p>
         * See full description here: {@link CalendarEvent#getGroupId()}.
         *
         * @param groupId group ID
         * @return current instance of builder
         */
        public Builder withGroupId(Object groupId) {
            event.groupId = groupId;
            return this;
        }

        /**
         * See full description here: {@link CalendarEvent#getAllDay()}.
         *
         * @return all-day option or {@code null} if not set
         */
        @Nullable
        public Boolean getAllDay() {
            return event.getAllDay();
        }

        /**
         * Sets all-day option.
         * <p>
         * See full description here: {@link CalendarEvent#setAllDay(Boolean)}.
         *
         * @param allDay all-day option
         * @return current instance of builder
         */
        public Builder withAllDay(@Nullable Boolean allDay) {
            event.allDay = allDay;
            return this;
        }

        /**
         * See full description here: {@link CalendarEvent#getStartDateTime()}.
         *
         * @return start date-time or {@code null} if not set
         */
        @Nullable
        public LocalDateTime getStartDateTime() {
            return event.getStartDateTime();
        }

        /**
         * Sets start date-time.
         * <p>
         * See full description here: {@link CalendarEvent#setStartDateTime}.
         *
         * @param start start date-time
         * @return current instance of builder
         */
        public Builder withStartDateTime(@Nullable LocalDateTime start) {
            event.startDateTime = start;
            return this;
        }

        /**
         * Sets start date-time.
         * <p>
         * See full description here: {@link CalendarEvent#setStartDateTime}.
         *
         * @param date start date
         * @param time start time
         * @return current instance of builder
         */
        public Builder withStartDateTime(LocalDate date, LocalTime time) {
            Preconditions.checkNotNullArgument(date);
            Preconditions.checkNotNullArgument(time);
            event.startDateTime = LocalDateTime.of(date, time);
            return this;
        }

        /**
         * See full description here: {@link CalendarEvent#getEndDateTime()}.
         *
         * @return end date-time or {@code null} if not set
         */
        @Nullable
        public LocalDateTime getEndDateTime() {
            return event.getEndDateTime();
        }

        /**
         * Sets end date-time.
         * <p>
         * See full description here: {@link CalendarEvent#setEndDateTime(LocalDateTime)}.
         *
         * @param end end date-time
         * @return current instance of builder
         */
        public Builder withEndDateTime(@Nullable LocalDateTime end) {
            event.endDateTime = end;
            return this;
        }

        /**
         * Sets end date-time.
         * <p>
         * See full description here: {@link CalendarEvent#setEndDateTime(LocalDateTime)}.
         *
         * @param date start date
         * @param time start time
         * @return current instance of builder
         */
        public Builder withEndDateTime(LocalDate date, LocalTime time) {
            Preconditions.checkNotNullArgument(date);
            Preconditions.checkNotNullArgument(time);
            event.endDateTime = LocalDateTime.of(date, time);
            return this;
        }

        /**
         * See full description here: {@link CalendarEvent#getTitle()}.
         *
         * @return event title or {@code null} if not set
         */
        @Nullable
        public String getTitle() {
            return event.getTitle();
        }

        /**
         * Sets event title.
         * <p>
         * See full description here: {@link CalendarEvent#getTitle()}.
         *
         * @param title event title
         * @return current instance of builder
         */
        public Builder withTitle(@Nullable String title) {
            event.title = title;
            return this;
        }

        /**
         * See full description here: {@link CalendarEvent#getDescription()}.
         *
         * @return description or {@code null} if not set
         */
        @Nullable
        public String getDescription() {
            return event.getDescription();
        }

        /**
         * Sets event description.
         * <p>
         * See full description here: {@link CalendarEvent#getDescription()}.
         *
         * @param description event description
         * @return current instance of builder
         */
        public Builder withDescription(@Nullable String description) {
            event.description = description;
            return this;
        }

        /**
         * See full description here: {@link CalendarEvent#getInteractive()}.
         *
         * @return whether events can be navigated by TAB key or {@code null} if not set
         */
        @Nullable
        public Boolean getInteractive() {
            return event.getInteractive();
        }

        /**
         * Sets event interactivity.
         * <p>
         * See full description here: {@link CalendarEvent#getInteractive()}.
         *
         * @param interactive interactive option
         * @return current instance of builder
         */
        public Builder withInteractive(@Nullable Boolean interactive) {
            event.interactive = interactive;
            return this;
        }

        /**
         * See full description here: {@link CalendarEvent#getClassNames()}.
         *
         * @return event class names or {@code null} if not set
         */
        @Nullable
        public String getClassNames() {
            return event.getClassNames();
        }

        /**
         * Sets event class names.
         * <p>
         * See full description here: {@link CalendarEvent#getClassNames()}.
         *
         * @param classNames event class names
         * @return current instance of builder
         */
        public Builder withClassNames(@Nullable String classNames) {
            event.classNames = classNames;
            return this;
        }

        /**
         * See full description here: {@link CalendarEvent#getStartEditable()}.
         *
         * @return whether an event can be dragged in the calendar component or {@code null} if not set
         */
        @Nullable
        public Boolean getStartEditable() {
            return event.getStartEditable();
        }

        /**
         * Sets whether an event can be dragged in the calendar component.
         * <p>
         * See full description here: {@link CalendarEvent#getStartEditable()}.
         *
         * @param startEditable startEditable option
         * @return current instance of builder
         */
        public Builder withStartEditable(@Nullable Boolean startEditable) {
            event.startEditable = startEditable;
            return this;
        }

        /**
         * See full description here: {@link CalendarEvent#getDurationEditable()}.
         *
         * @return whether an event can be resized in the calendar component or {@code null} if not set
         */
        @Nullable
        public Boolean getDurationEditable() {
            return event.getDurationEditable();
        }

        /**
         * Sets whether an event can be resized in the calendar component
         * <p>
         * See full description here: {@link CalendarEvent#getDurationEditable()}.
         *
         * @param durationEditable durationEditable option
         * @return current instance of builder
         */
        public Builder withDurationEditable(@Nullable Boolean durationEditable) {
            event.durationEditable = durationEditable;
            return this;
        }

        /**
         * See full description here: {@link CalendarEvent#getDisplay()}.
         *
         * @return the type of event rendering or {@code null} if not set
         */
        @Nullable
        public Display getDisplay() {
            return event.getDisplay();
        }

        /**
         * Sets the type of event rendering.
         * <p>
         * See full description here: {@link CalendarEvent#getDisplay()}.
         *
         * @param display the type of rendering
         * @return current instance of builder
         */
        public Builder withDisplay(@Nullable Display display) {
            event.display = display;
            return this;
        }

        /**
         * See full description here: {@link CalendarEvent#getOverlap()}.
         *
         * @return whether the event can be dragged/ resized over other events or {@code null} if not set
         */
        @Nullable
        public Boolean getOverlap() {
            return event.getOverlap();
        }

        /**
         * Sets whether the event can be dragged/resized over other events and prevents other
         * events from being dragged/resized over this event.
         * <p>
         * See full description here: {@link CalendarEvent#getOverlap()}.
         *
         * @param overlap overlap option
         * @return current instance of builder
         */
        public Builder withOverlap(@Nullable Boolean overlap) {
            event.overlap = overlap;
            return this;
        }

        /**
         * See full description here: {@link CalendarEvent#getConstraint()}.
         *
         * @return event constraint or {@code null} if not set
         */
        @Nullable
        public Object getConstraint() {
            return event.getConstraint();
        }

        /**
         * Sets an event constraint.
         * <p>
         * See full description here: {@link CalendarEvent#getConstraint()}.
         *
         * @param constraint event constraint
         * @return current instance of builder
         */
        public Builder withConstraint(@Nullable Object constraint) {
            event.constraint = constraint;
            return this;
        }

        /**
         * See full description here: {@link CalendarEvent#getBackgroundColor()}.
         *
         * @return event background color or {@code null} if not set
         */
        @Nullable
        public String getBackgroundColor() {
            return event.getBackgroundColor();
        }

        /**
         * Sets an event background color.
         * <p>
         * See full description here: {@link CalendarEvent#getBackgroundColor()}.
         *
         * @param backgroundColor event background color
         * @return current instance of builder
         */
        public Builder withBackgroundColor(@Nullable String backgroundColor) {
            event.backgroundColor = backgroundColor;
            return this;
        }

        /**
         * See full description here: {@link CalendarEvent#getBorderColor()}.
         *
         * @return event border color or {@code null} if not set
         */
        @Nullable
        public String getBorderColor() {
            return event.getBorderColor();
        }

        /**
         * Sets an event border color.
         * <p>
         * See full description here: {@link CalendarEvent#getBorderColor()}.
         *
         * @param borderColor event border color
         * @return current instance of builder
         */
        public Builder withBorderColor(@Nullable String borderColor) {
            event.borderColor = borderColor;
            return this;
        }

        /**
         * See full description here: {@link CalendarEvent#getTextColor()}.
         *
         * @return event text color or {@code null} if not set
         */
        @Nullable
        public String getTextColor() {
            return event.getTextColor();
        }

        /**
         * Sets an event text color.
         * <p>
         * See full description here: {@link CalendarEvent#getTextColor()}.
         *
         * @param textColor event text color
         * @return current instance of builder
         */
        public Builder withTextColor(@Nullable String textColor) {
            event.textColor = textColor;
            return this;
        }

        /**
         * See full description here: {@link CalendarEvent#getAdditionalProperties()}.
         *
         * @return additional properties or {@code null} if not set
         */
        @Nullable
        public Map<String, Object> getAdditionalProperties() {
            return event.getAdditionalProperties();
        }

        /**
         * Sets additional properties and their values.
         * <p>
         * See full description here: {@link CalendarEvent#getAdditionalProperties()}.
         *
         * @param additionalProperties additional properties
         * @return current instance of builder
         */
        public Builder withAdditionalProperties(@Nullable Map<String, Object> additionalProperties) {
            event.additionalProperties = additionalProperties;
            return this;
        }

        /**
         * See full description here: {@link CalendarEvent#getRecurringDaysOfWeek()}.
         *
         * @return recurring days of week or {@code null} if not set
         */
        @Nullable
        public DaysOfWeek getRecurringDaysOfWeek() {
            return event.getRecurringDaysOfWeek();
        }

        /**
         * Sets recurring days of week.
         * <p>
         * See full description here: {@link CalendarEvent#getRecurringDaysOfWeek()}.
         *
         * @param daysOfWeek fays of week
         * @return current instance of builder
         */
        public Builder withRecurringDaysOfWeek(@Nullable DaysOfWeek daysOfWeek) {
            event.recurringDaysOfWeek = daysOfWeek;
            return this;
        }

        /**
         * See full description here: {@link CalendarEvent#getRecurringStartDate()}.
         *
         * @return recurring start date or {@code null} if not set
         */
        @Nullable
        public LocalDate getRecurringStartDate() {
            return event.getRecurringStartDate();
        }

        /**
         * Sets recurring start date.
         * <p>
         * See full description here: {@link CalendarEvent#getRecurringStartDate()}.
         *
         * @param recurringStartDate recurring start date
         * @return current instance of builder
         */
        public Builder withRecurringStartDate(@Nullable LocalDate recurringStartDate) {
            event.recurringStartDate = recurringStartDate;
            return this;
        }

        /**
         * See full description here: {@link CalendarEvent#getRecurringEndDate()}.
         *
         * @return recurring end date or {@code null} if not set
         */
        @Nullable
        public LocalDate getRecurringEndDate() {
            return event.getRecurringEndDate();
        }

        /**
         * Sets recurring end date.
         * <p>
         * See full description here: {@link CalendarEvent#getRecurringEndDate()}.
         *
         * @param recurringEndDate recurring end date
         * @return current instance of builder
         */
        public Builder withRecurringEndDate(@Nullable LocalDate recurringEndDate) {
            event.recurringEndDate = recurringEndDate;
            return this;
        }

        /**
         * See full description here: {@link CalendarEvent#getRecurringStartDate()}.
         *
         * @return recurring start time or {@code null} if not set
         */
        @Nullable
        public LocalTime getRecurringStartTime() {
            return event.getRecurringStartTime();
        }

        /**
         * Sets recurring start time.
         * <p>
         * See full description here: {@link CalendarEvent#getRecurringStartTime()}.
         *
         * @param recurringStartTime recurring start time
         * @return current instance of builder
         */
        public Builder withRecurringStartTime(@Nullable LocalTime recurringStartTime) {
            event.recurringStartTime = recurringStartTime;
            return this;
        }

        /**
         * See full description here: {@link CalendarEvent#getRecurringEndTime()}.
         *
         * @return recurring end time or {@code null} if not set
         */
        @Nullable
        public LocalTime getRecurringEndTime() {
            return event.getRecurringEndTime();
        }

        /**
         * Sets recurring end time.
         * <p>
         * See full description here: {@link CalendarEvent#getRecurringEndTime()}.
         *
         * @param recurringEndTime recurring end time
         * @return current instance of builder
         */
        public Builder withRecurringEndTime(@Nullable LocalTime recurringEndTime) {
            event.recurringEndTime = recurringEndTime;
            return this;
        }

        /**
         * Builds an event.
         *
         * @return instance of event
         */
        public SimpleCalendarEvent build() {
            return event;
        }
    }
}
