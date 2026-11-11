/*
 * Copyright 2026 Haulmont.
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

package test_support.entity;

import io.jmix.core.entity.annotation.IgnoreUserTimeZone;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.UUID;

/**
 * Calendar event with a date attribute of every supported type, to check how each of them is handled.
 */
@JmixEntity(name = "test_DateTypesEvent")
public class DateTypesEvent {

    @JmixId
    @JmixGeneratedValue
    protected UUID id;

    @JmixProperty
    protected Boolean allDay;

    @JmixProperty
    protected LocalDate localDate;

    @JmixProperty
    protected LocalDateTime localDateTime;

    @Temporal(TemporalType.DATE)
    @JmixProperty
    protected Date date;

    @Temporal(TemporalType.TIMESTAMP)
    @JmixProperty
    protected Date dateTime;

    @IgnoreUserTimeZone
    @Temporal(TemporalType.TIMESTAMP)
    @JmixProperty
    protected Date dateTimeIgnoringUserTimeZone;

    @JmixProperty
    protected OffsetDateTime offsetDateTime;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Boolean getAllDay() {
        return allDay;
    }

    public void setAllDay(Boolean allDay) {
        this.allDay = allDay;
    }

    public LocalDate getLocalDate() {
        return localDate;
    }

    public void setLocalDate(LocalDate localDate) {
        this.localDate = localDate;
    }

    public LocalDateTime getLocalDateTime() {
        return localDateTime;
    }

    public void setLocalDateTime(LocalDateTime localDateTime) {
        this.localDateTime = localDateTime;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    public Date getDateTimeIgnoringUserTimeZone() {
        return dateTimeIgnoringUserTimeZone;
    }

    public void setDateTimeIgnoringUserTimeZone(Date dateTimeIgnoringUserTimeZone) {
        this.dateTimeIgnoringUserTimeZone = dateTimeIgnoringUserTimeZone;
    }

    public OffsetDateTime getOffsetDateTime() {
        return offsetDateTime;
    }

    public void setOffsetDateTime(OffsetDateTime offsetDateTime) {
        this.offsetDateTime = offsetDateTime;
    }
}
