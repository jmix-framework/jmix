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
import jakarta.annotation.Nullable;

import java.time.*;
import java.util.Date;
import java.util.Objects;

/**
 * INTERNAL.
 */
public final class CalendarDateTimeTransformations {

    /**
     * CAUTION! Copied from io.jmix.core.DateTimeTransformations#transformToType()
     *
     * @param date
     * @param javaType
     * @param zoneId
     * @return
     */
    public static Object transformToType(Object date, Class javaType, @Nullable ZoneId zoneId) {
        Objects.requireNonNull(date);
        Objects.requireNonNull(javaType);

        ZonedDateTime zonedDateTime = transformToZDT(date, zoneId);
        return transformFromZdtInternal(zonedDateTime, javaType);
    }

    /**
     * CAUTION! Copied from io.jmix.core.DateTimeTransformations#transformToZDT()
     *
     * @param date
     * @param fromZoneId
     * @return
     */
    public static ZonedDateTime transformToZDT(Object date, @Nullable ZoneId fromZoneId) {
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
     * CAUTION! Copied from io.jmix.core.DateTimeTransformations#transformFromZdtInternal()
     *
     * @param zonedDateTime
     * @param javaType
     * @return
     */
    private static Object transformFromZdtInternal(ZonedDateTime zonedDateTime, Class javaType) {
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

    /**
     * CAUTION! Copied from io.jmix.core.DateTimeTransformations#transformToLocalTime()
     */
    public static LocalTime transformToLocalTime(Object date) {
        Preconditions.checkNotNull(date);
        if (date instanceof java.sql.Time) {
            return ((java.sql.Time) date).toLocalTime();
        } else if (date instanceof Date) {
            return ((Date) date).toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalTime();
        } else if (date instanceof LocalTime) {
            return (LocalTime) date;
        } else if (date instanceof OffsetTime) {
            return ((OffsetTime) date).toLocalTime();
        }
        throw newUnsupportedTypeException(date.getClass());
    }

    private static RuntimeException newUnsupportedTypeException(Class javaType) {
        throw new IllegalArgumentException(String.format("Unsupported date type %s", javaType));
    }
}
