/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.component.validation.time;

import io.jmix.core.TimeSource;

import java.time.*;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractTimeValidator<T> implements TimeValidator {

    protected TimeSource timeSource;
    protected T value;

    protected boolean checkSeconds;

    protected static Map<Class<?>, TimeValidator> constraints = new HashMap<>(5);

    public AbstractTimeValidator(TimeSource timeSource) {
        this.timeSource = timeSource;
    }

    @Override
    public boolean isPast() {
        return compareValueWithCurrent() < 0;
    }

    @Override
    public boolean isPastOrPresent() {
        return compareValueWithCurrent() <= 0;
    }

    @Override
    public boolean isFuture() {
        return compareValueWithCurrent() > 0;
    }

    @Override
    public boolean isFutureOrPresent() {
        return compareValueWithCurrent() >= 0;
    }

    @Override
    public void setCheckSeconds(boolean checkSeconds) {
        this.checkSeconds = checkSeconds;
    }

    public abstract int compareValueWithCurrent();

    public static class DateConstraint extends AbstractTimeValidator<Date> {

        public DateConstraint(TimeSource timeSource, Date value) {
            super(timeSource);
            this.value = value;
        }

        @Override
        public int compareValueWithCurrent() {
            if (!checkSeconds) {
                Date current = getWithoutSeconds(timeSource.currentTimestamp());
                Date val = getWithoutSeconds(value);
                return val.compareTo(current);
            }

            Date currentValue = timeSource.currentTimestamp();
            return value.compareTo(currentValue);
        }

        protected Date getWithoutSeconds(Date value) {
            Date date = new Date(value.getTime());
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.clear(Calendar.SECOND);
            calendar.clear(Calendar.MILLISECOND);
            return calendar.getTime();
        }
    }

    public static class LocalDateConstraint extends AbstractTimeValidator<LocalDate> {

        public LocalDateConstraint(TimeSource timeSource, LocalDate value) {
            super(timeSource);
            this.value = value;
        }

        @Override
        public int compareValueWithCurrent() {
            LocalDate currentValue = timeSource.now().toLocalDate();
            return value.compareTo(currentValue);
        }
    }

    public static class LocalDateTimeConstraint extends AbstractTimeValidator<LocalDateTime> {

        public LocalDateTimeConstraint(TimeSource timeSource, LocalDateTime value) {
            super(timeSource);
            this.value = value;
        }

        @Override
        public int compareValueWithCurrent() {
            if (!checkSeconds) {
                LocalDateTime current = getWithoutSeconds(timeSource.now().toLocalDateTime());
                LocalDateTime val = getWithoutSeconds(value);
                return val.compareTo(current);
            }

            LocalDateTime currentValue = timeSource.now().toLocalDateTime();
            return value.compareTo(currentValue);
        }

        protected LocalDateTime getWithoutSeconds(LocalDateTime value) {
            return value.withSecond(0).withNano(0);
        }
    }

    public static class LocalTimeConstraint extends AbstractTimeValidator<LocalTime> {

        public LocalTimeConstraint(TimeSource timeSource, LocalTime value) {
            super(timeSource);
            this.value = value;
        }

        @Override
        public int compareValueWithCurrent() {
            if (!checkSeconds) {
                LocalTime current = getWithoutSeconds(timeSource.now().toLocalTime());
                LocalTime val = getWithoutSeconds(value);
                return val.compareTo(current);
            }

            LocalTime currentValue = timeSource.now().toLocalTime();
            return value.compareTo(currentValue);
        }

        protected LocalTime getWithoutSeconds(LocalTime value) {
            return value.withSecond(0).withNano(0);
        }
    }

    public static class OffsetTimeConstraint extends AbstractTimeValidator<OffsetTime> {

        public OffsetTimeConstraint(TimeSource timeSource, OffsetTime value) {
            super(timeSource);
            this.value = value;
        }

        @Override
        public int compareValueWithCurrent() {
            if (!checkSeconds) {
                OffsetTime current = getWithoutSeconds(timeSource.now().toOffsetDateTime().toOffsetTime());
                OffsetTime val = getWithoutSeconds(value);
                return val.compareTo(current);
            }

            OffsetTime currentValue = timeSource.now().toOffsetDateTime().toOffsetTime();
            return value.compareTo(currentValue);
        }

        protected OffsetTime getWithoutSeconds(OffsetTime value) {
            return value.withSecond(0).withNano(0);
        }
    }

    public static class OffsetDateTimeConstraint extends AbstractTimeValidator<OffsetDateTime> {

        public OffsetDateTimeConstraint(TimeSource timeSource, OffsetDateTime value) {
            super(timeSource);
            this.value = value;
        }

        @Override
        public int compareValueWithCurrent() {
            if (!checkSeconds) {
                OffsetDateTime current = getWithoutSeconds(timeSource.now().toOffsetDateTime());
                OffsetDateTime val = getWithoutSeconds(value);
                return val.compareTo(current);
            }

            OffsetDateTime currentValue = timeSource.now().toOffsetDateTime();
            return value.compareTo(currentValue);
        }

        protected OffsetDateTime getWithoutSeconds(OffsetDateTime value) {
            return value.withSecond(0).withNano(0);
        }
    }
}
