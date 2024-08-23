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
import org.springframework.lang.Nullable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

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
        Objects.requireNonNull(id);
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

    public void setTitle(@Nullable String title) {
        this.title = title;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(@Nullable String description) {
        this.description = description;
    }

    @Override
    public Boolean getInteractive() {
        return interactive;
    }

    public void setInteractive(@Nullable Boolean interactive) {
        this.interactive = interactive;
    }

    @Override
    public String getClassNames() {
        return classNames;
    }

    public void setClassNames(@Nullable String classNames) {
        this.classNames = classNames;
    }

    @Nullable
    @Override
    public Boolean getStartEditable() {
        return startEditable;
    }

    public void setStartEditable(@Nullable Boolean startEditable) {
        this.startEditable = startEditable;
    }

    @Nullable
    @Override
    public Boolean getDurationEditable() {
        return durationEditable;
    }

    public void setDurationEditable(@Nullable Boolean durationEditable) {
        this.durationEditable = durationEditable;
    }

    @Override
    public Display getDisplay() {
        return display;
    }

    public void setDisplay(@Nullable Display display) {
        this.display = display;
    }

    @Override
    public Boolean getOverlap() {
        return overlap;
    }

    public void setOverlap(@Nullable Boolean overlap) {
        this.overlap = overlap;
    }

    @Nullable
    @Override
    public Object getConstraint() {
        return constraint;
    }

    public void setConstraint(@Nullable Object constraint) {
        this.constraint = constraint;
    }

    @Override
    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(@Nullable String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    @Override
    public String getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(@Nullable String borderColor) {
        this.borderColor = borderColor;
    }

    @Override
    public String getTextColor() {
        return textColor;
    }

    public void setTextColor(@Nullable String textColor) {
        this.textColor = textColor;
    }

    @Nullable
    @Override
    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperties(@Nullable Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    @Override
    public DaysOfWeek getRecurringDaysOfWeek() {
        return recurringDaysOfWeek;
    }

    public void setRecurringDaysOfWeek(@Nullable DaysOfWeek recurringDaysOfWeek) {
        this.recurringDaysOfWeek = recurringDaysOfWeek;
    }

    @Override
    public LocalDate getRecurringStarDate() {
        return recurringStartDate;
    }

    public void setRecurringStartDate(@Nullable LocalDate recurringStartDate) {
        this.recurringStartDate = recurringStartDate;
    }

    @Override
    public LocalDate getRecurringEndDate() {
        return recurringEndDate;
    }

    public void setRecurringEndDate(@Nullable LocalDate recurringEndDate) {
        this.recurringEndDate = recurringEndDate;
    }

    @Override
    public LocalTime getRecurringStarTime() {
        return recurringStartTime;
    }

    public void setRecurringStartTime(@Nullable LocalTime recurringStartTime) {
        this.recurringStartTime = recurringStartTime;
    }

    @Override
    public LocalTime getRecurringEndTime() {
        return recurringEndTime;
    }

    public void setRecurringEndTime(@Nullable LocalTime recurringEndTime) {
        this.recurringEndTime = recurringEndTime;
    }

    public static Builder create() {
        return new Builder();
    }

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

    public static class Builder {

        protected SimpleCalendarEvent event;

        public Builder() {
            event = new SimpleCalendarEvent();
        }

        public Builder(Object id) {
            event = new SimpleCalendarEvent(id);
        }

        public Builder withGroupId(Object groupId) {
            event.groupId = groupId;
            return this;
        }

        public Builder withAllDay(@Nullable Boolean allDay) {
            event.allDay = allDay;
            return this;
        }

        public Builder withStartDateTime(@Nullable LocalDateTime start) {
            event.startDateTime = start;
            return this;
        }

        public Builder withEndDateTime(@Nullable LocalDateTime end) {
            event.endDateTime = end;
            return this;
        }

        public Builder withTitle(@Nullable String title) {
            event.title = title;
            return this;
        }

        public Builder withDescription(@Nullable String description) {
            event.description = description;
            return this;
        }

        public Builder withInteractive(@Nullable Boolean interactive) {
            event.interactive = interactive;
            return this;
        }

        public Builder withClassNames(@Nullable String classNames) {
            event.classNames = classNames;
            return this;
        }

        public Builder withStartEditable(@Nullable Boolean startEditable) {
            event.startEditable = startEditable;
            return this;
        }

        public Builder withDurationEditable(@Nullable Boolean durationEditable) {
            event.durationEditable = durationEditable;
            return this;
        }

        public Builder withDisplay(@Nullable Display display) {
            event.display = display;
            return this;
        }

        public Builder withOverlap(@Nullable Boolean overlap) {
            event.overlap = overlap;
            return this;
        }

        public Builder withConstraint(@Nullable Object constraint) {
            event.constraint = constraint;
            return this;
        }

        public Builder withBackgroundColor(@Nullable String backgroundColor) {
            event.backgroundColor = backgroundColor;
            return this;
        }

        public Builder withBorderColor(@Nullable String borderColor) {
            event.borderColor = borderColor;
            return this;
        }

        public Builder withTextColor(@Nullable String textColor) {
            event.textColor = textColor;
            return this;
        }

        public Builder withAdditionalProperties(@Nullable Map<String, Object> additionalProperties) {
            event.additionalProperties = additionalProperties;
            return this;
        }

        public Builder withRecurringDaysOfWeek(@Nullable DaysOfWeek daysOfWeek) {
            event.recurringDaysOfWeek = daysOfWeek;
            return this;
        }

        public Builder withRecurringStartDate(@Nullable LocalDate recurringStartDate) {
            event.recurringStartDate = recurringStartDate;
            return this;
        }

        public Builder withRecurringEndDate(@Nullable LocalDate recurringEndDate) {
            event.recurringEndDate = recurringEndDate;
            return this;
        }

        public Builder withRecurringStartTime(@Nullable LocalTime recurringStartTime) {
            event.recurringStartTime = recurringStartTime;
            return this;
        }

        public Builder withRecurringEndTime(@Nullable LocalTime recurringEndTime) {
            event.recurringEndTime = recurringEndTime;
            return this;
        }

        public SimpleCalendarEvent build() {
            return event;
        }
    }
}
