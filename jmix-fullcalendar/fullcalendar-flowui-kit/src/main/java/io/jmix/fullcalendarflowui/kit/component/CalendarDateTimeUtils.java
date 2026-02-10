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

package io.jmix.fullcalendarflowui.kit.component;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.Nullable;

import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Objects;

/**
 * INTERNAL.
 */
public final class CalendarDateTimeUtils {

    /**
     * Converts a date instance to the passed java type corresponding to one of the date types.
     *
     * @param date     the date object, not {@code null}
     * @param javaType the java type to convert to
     * @param zoneId   the zone ID to use or {@code null} to use default system timezone
     * @return the date object converted to the passed java type, not {@code null}
     */
    public static Object transformToType(Object date, Class javaType, @Nullable ZoneId zoneId) {
        /*
         * CAUTION! Copied from io.jmix.core.DateTimeTransformations#transformToType()
         */
        Objects.requireNonNull(date);
        Objects.requireNonNull(javaType);

        ZonedDateTime zonedDateTime = transformToZDT(date, zoneId);
        return transformFromZdtInternal(zonedDateTime, javaType);
    }

    /**
     * Obtains an instance of {@link ZonedDateTime} from Date or LocalDate or LocalDateTime or OffsetDateTime
     * ZonedDateTime is created for LocalDate, LocalDateTime with default system timezone
     *
     * @param date date object, not null
     * @return the ZonedDateTime, not null
     * @throws IllegalArgumentException if the type of the provided date is not supported
     */
    public static ZonedDateTime transformToZDT(Object date, @Nullable ZoneId fromZoneId) {
        /*
         * CAUTION! Copied from io.jmix.core.DateTimeTransformations#transformToZDT()
         */
        Objects.requireNonNull(date);
        ZoneId zoneId = fromZoneId != null ? fromZoneId : ZoneId.systemDefault();
        if (date instanceof java.sql.Date) {
            return ((java.sql.Date) date).toLocalDate().atStartOfDay(zoneId);
        } else if (date instanceof Date) {
            return ((Date) date).toInstant().atZone(zoneId);
        } else if (date instanceof LocalDate) {
            return ((LocalDate) date).atStartOfDay(zoneId);
        } else if (date instanceof LocalDateTime) {
            return ((LocalDateTime) date).atZone(zoneId);
        } else if (date instanceof OffsetDateTime) {
            return ((OffsetDateTime) date).atZoneSameInstant(zoneId);
        }
        throw newUnsupportedTypeException(date.getClass());
    }

    /**
     * Obtains an instance of LocalTime from Time or Date or LocalTime or OffsetTime
     *
     * @param time the time object to transform
     * @return local time instance
     * @throws IllegalArgumentException if the type of the provided time is not supported
     */
    public static LocalTime transformToLocalTime(Object time) {
        /*
         * CAUTION! Copied from io.jmix.core.DateTimeTransformations#transformToLocalTime()
         */
        Preconditions.checkNotNull(time);
        if (time instanceof java.sql.Time) {
            return ((java.sql.Time) time).toLocalTime();
        } else if (time instanceof Date) {
            return ((Date) time).toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalTime();
        } else if (time instanceof LocalTime) {
            return (LocalTime) time;
        } else if (time instanceof OffsetTime) {
            return ((OffsetTime) time).toLocalTime();
        }
        throw newUnsupportedTypeException(time.getClass());
    }

    /**
     * Parses date from ISO string.
     *
     * @param isoDate date in ISO format
     * @return local date instance
     * @throws DateTimeParseException if the provided date cannot be parsed
     */
    public static LocalDate parseIsoDate(String isoDate) {
        try {
            return LocalDate.parse(isoDate);
        } catch (DateTimeParseException e) {
            throw new IllegalStateException("Cannot parse date: " + isoDate, e);
        }
    }

    /**
     * Parses date-time or date from ISO string.
     *
     * @param isoDateTime date-time in ISO format
     * @param zoneId      zoneId if the provided date-time does contain time zone
     * @return {@link ZonedDateTime} instance
     * @throws DateTimeParseException if the provided date-time cannot be parsed
     */
    public static ZonedDateTime parseIsoDateTime(String isoDateTime, ZoneId zoneId) {
        try {
            return ZonedDateTime.parse(isoDateTime);
        } catch (DateTimeParseException e) {
            // Exception means that offset part is missed
        }
        try {
            return LocalDateTime.parse(isoDateTime).atZone(zoneId);
        } catch (DateTimeParseException e) {
            // Exception means that time part is missed
        }
        try {
            return LocalDate.parse(isoDateTime).atStartOfDay(zoneId);
        } catch (DateTimeParseException e) {
            throw new IllegalStateException("Cannot parse date: " + isoDateTime, e);
        }
    }

    /**
     * Parses raw ISO date time to {@link ZonedDateTime} with the zoneId and then transform this value
     * to {@link LocalDateTime} with system default time zone.
     *
     * @param isoDateTime raw ISO date time
     * @param zoneId      zoneId to transform
     * @return local date time
     */
    public static LocalDateTime parseAndTransform(String isoDateTime, ZoneId zoneId) {
        ZonedDateTime zonedDateTime = parseIsoDateTime(isoDateTime, zoneId);
        return transformAsSystemDefault(zonedDateTime);
    }

    private static LocalDateTime transformAsSystemDefault(ZonedDateTime zonedDateTime) {
        return zonedDateTime.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * Transforms {@link ZonedDateTime} to the provided type.
     *
     * @param zonedDateTime date-time to transform
     * @param javaType      java type to transform
     * @return the transformed date with the provider type
     */
    private static Object transformFromZdtInternal(ZonedDateTime zonedDateTime, Class javaType) {
        /**
         * CAUTION! Copied from io.jmix.core.DateTimeTransformations#transformFromZdtInternal()
         */
        if (java.sql.Date.class.equals(javaType)) {
            return java.sql.Date.valueOf(zonedDateTime.toLocalDate());
        } else if (Date.class.equals(javaType)) {
            return Date.from(zonedDateTime.toInstant());
        } else if (LocalDate.class.equals(javaType)) {
            return zonedDateTime.toLocalDate();
        } else if (LocalDateTime.class.equals(javaType)) {
            return zonedDateTime.toLocalDateTime();
        } else if (OffsetDateTime.class.equals(javaType)) {
            return zonedDateTime.toOffsetDateTime();
        } else if (LocalTime.class.equals(javaType)) {
            return zonedDateTime.toLocalTime();
        } else if (OffsetTime.class.equals(javaType)) {
            return zonedDateTime.toOffsetDateTime().toOffsetTime();
        }
        throw newUnsupportedTypeException(javaType);
    }

    private static RuntimeException newUnsupportedTypeException(Class javaType) {
        throw new IllegalArgumentException(String.format("Unsupported date type %s", javaType));
    }
}
