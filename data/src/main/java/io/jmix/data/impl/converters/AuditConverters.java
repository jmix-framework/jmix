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

import java.time.OffsetDateTime;
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

}
