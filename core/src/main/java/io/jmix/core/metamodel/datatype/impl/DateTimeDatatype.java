/*
 * Copyright 2019 Haulmont.
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

package io.jmix.core.metamodel.datatype.impl;

import io.jmix.core.common.util.ParamsMap;
import io.jmix.core.metamodel.annotation.DatatypeDef;
import io.jmix.core.metamodel.annotation.DateTimeFormat;
import io.jmix.core.metamodel.datatype.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

@DatatypeDef(id = "dateTime", javaClass = Date.class, defaultForClass = true, value = "jmix_DateTimeDatatype")
@DateTimeFormat("yyyy-MM-dd HH:mm:ss.SSS")
public class DateTimeDatatype implements Datatype<Date>, ParameterizedDatatype, TimeZoneAwareDatatype {

    private String formatPattern;

    @Autowired
    protected FormatStringsRegistry formatStringsRegistry;

    public DateTimeDatatype() {
        DateTimeFormat dateTimeFormat = getClass().getAnnotation(DateTimeFormat.class);
        if (dateTimeFormat != null) {
            formatPattern = dateTimeFormat.value();
        }
    }

    @Override
    public String format(@Nullable Object value) {
        if (value == null) {
            return "";
        } else {
            DateFormat format;
            if (formatPattern != null) {
                format = new SimpleDateFormat(formatPattern);
            } else {
                format = DateFormat.getDateInstance();
            }
            return format.format((value));
        }
    }

    @Override
    public String format(@Nullable Object value, Locale locale) {
        return format(value, locale, null);
    }

    @Override
    public String format(@Nullable Object value, Locale locale, @Nullable TimeZone timeZone) {
        if (value == null) {
            return "";
        }

        FormatStrings formatStrings = formatStringsRegistry.getFormatStringsOrNull(locale);
        if (formatStrings == null) {
            return format(value);
        }

        DateFormat format = new SimpleDateFormat(formatStrings.getDateTimeFormat());
        if (timeZone != null) {
            format.setTimeZone(timeZone);
        }

        return format.format(value);
    }

    @Override
    public Date parse(@Nullable String value) throws ParseException {
        if (StringUtils.isBlank(value)) {
            return null;
        }

        DateFormat format;
        if (formatPattern != null) {
            format = new SimpleDateFormat(formatPattern);
        } else {
            format = DateFormat.getDateInstance();
        }
        return format.parse(value.trim());
    }

    @Override
    public Date parse(@Nullable String value, Locale locale) throws ParseException {
        return parse(value, locale, null);
    }

    @Nullable
    public Date parse(@Nullable String value, Locale locale, @Nullable TimeZone timeZone) throws ParseException {
        if (StringUtils.isBlank(value)) {
            return null;
        }

        FormatStrings formatStrings = formatStringsRegistry.getFormatStringsOrNull(locale);
        if (formatStrings == null) {
            return parse(value);
        }

        DateFormat format = new SimpleDateFormat(formatStrings.getDateTimeFormat());

        if (timeZone != null) {
            format.setTimeZone(timeZone);
        }

        return format.parse(value.trim());
    }

    @Override
    public Map<String, Object> getParameters() {
        return ParamsMap.of("format", formatPattern);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}