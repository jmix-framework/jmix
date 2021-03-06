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

import io.jmix.core.entity.BaseUser;
import org.springframework.core.convert.converter.Converter;

import java.util.Date;

public class AuditConverters {

    public enum DateToLongConverter implements Converter<Date, Long> {
        INSTANCE;

        public Long convert(Date source) {
            return source.getTime();
        }
    }

    public enum LongToDateConverter implements Converter<Long, Date> {
        INSTANCE;

        public Date convert(Long source) {
            return new Date(source);
        }
    }

    public enum UserToStringConverter implements Converter<BaseUser, String> {
        INSTANCE;

        public String convert(BaseUser source) {
            return source.getUsername();
        }
    }


}
