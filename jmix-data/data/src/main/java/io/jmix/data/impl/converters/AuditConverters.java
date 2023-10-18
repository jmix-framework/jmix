/*
 * Copyright 2020 Haulmont.
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

package io.jmix.data.impl.converters;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;

import static java.time.ZoneId.systemDefault;

public class AuditConverters {

    public enum DateToLongConverter implements Converter<Date, Long> {
        INSTANCE;

        @Override
        public Long convert(Date source) {
            return source.getTime();
        }
    }

    public enum LongToDateConverter implements Converter<Long, Date> {
        INSTANCE;

        @Override
        public Date convert(Long source) {
            return new Date(source);
        }
    }

    public enum UserToStringConverter implements Converter<UserDetails, String> {
        INSTANCE;

        @Override
        public String convert(UserDetails source) {
            return source.getUsername();
        }
    }

    public enum DateToOffsetDateTimeConverter implements Converter<Date, OffsetDateTime> {
        INSTANCE;

        @Override
        public OffsetDateTime convert(Date source) {
            return OffsetDateTime.ofInstant(source.toInstant(), systemDefault());
        }
    }

    public enum OffsetDateTimeToDateConverter implements Converter<OffsetDateTime, Date> {
        INSTANCE;

        @Override
        public Date convert(OffsetDateTime source) {
            return Date.from(source.toInstant());
        }
    }

    public enum DateToLocalDateConverter implements Converter<Date, LocalDate> {
        INSTANCE;

        @Override
        public LocalDate convert(Date source) {
            return LocalDate.ofInstant(source.toInstant(), ZoneId.systemDefault());
        }
    }

    public enum LocalDateToDateConverter implements Converter<LocalDate, Date> {
        INSTANCE;

        @Override
        public Date convert(LocalDate source) {
            return Date.from(source.atStartOfDay(ZoneId.systemDefault()).toInstant());
        }
    }

    public enum DateToLocalDateTimeConverter implements Converter<Date, LocalDateTime> {
        INSTANCE;

        @Override
        public LocalDateTime convert(Date source) {
            return LocalDateTime.ofInstant(source.toInstant(), ZoneId.systemDefault());
        }
    }

    public enum LocalDateTimeToDateConverter implements Converter<LocalDateTime, Date> {
        INSTANCE;

        @Override
        public Date convert(LocalDateTime source) {
            return Date.from(source.atZone(ZoneId.systemDefault()).toInstant());
        }
    }

    public enum LocalDateTimeToLocalDateConverter implements Converter<LocalDateTime, LocalDate> {
        INSTANCE;

        @Override
        public LocalDate convert(LocalDateTime source) {
            return source.toLocalDate();
        }
    }

    public enum LocalDateToLocalDateTimeConverter implements Converter<LocalDate, LocalDateTime> {
        INSTANCE;

        @Override
        public LocalDateTime convert(LocalDate source) {
            return LocalDateTime.from(source);
        }
    }

    public enum LocalDateToOffsetDateTimeConverter implements Converter<LocalDate, OffsetDateTime> {
        INSTANCE;

        @Override
        public OffsetDateTime convert(LocalDate source) {
            return OffsetDateTime.from(source);
        }
    }

    public enum OffsetDateTimeToLocalDateConverter implements Converter<OffsetDateTime, LocalDate> {
        INSTANCE;

        @Override
        public LocalDate convert(OffsetDateTime source) {
            return source.toLocalDate();
        }
    }

    public enum LocalDateTimeToOffsetDateTimeConverter implements Converter<LocalDateTime, OffsetDateTime> {
        INSTANCE;

        @Override
        public OffsetDateTime convert(LocalDateTime source) {
            return OffsetDateTime.from(source);
        }
    }

    public enum OffsetDateTimeToLocalDateTimeConverter implements Converter<OffsetDateTime, LocalDateTime> {
        INSTANCE;

        @Override
        public LocalDateTime convert(OffsetDateTime source) {
            return source.toLocalDateTime();
        }
    }
}
