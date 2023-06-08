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

import io.jmix.core.annotation.Internal;
import io.jmix.core.common.util.ParamsMap;
import io.jmix.core.metamodel.annotation.DatatypeDef;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.FormatStrings;
import io.jmix.core.metamodel.datatype.FormatStringsRegistry;
import io.jmix.core.metamodel.datatype.ParameterizedDatatype;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * <code>TimeDatatype</code> works with <code>java.sql.Time</code> but is parametrized with <code>java.util.Date</code>
 * to avoid problems with casting.
 */
@Internal
@DatatypeDef(id = "time", javaClass = java.sql.Time.class, defaultForClass = true, value = "core_TimeDatatype")
public class TimeDatatype implements Datatype<Date>, ParameterizedDatatype {

    @Autowired
    protected FormatStringsRegistry formatStringsRegistry;

    @Override
    public String format(Object value) {
        if (!(value instanceof Date)) {
            return "";
        } else {
            Date date = (Date) value;
            return Instant.ofEpochMilli(date.getTime())
                    .atZone(ZoneId.systemDefault())
                    .toLocalTime().format(DateTimeFormatter.ISO_TIME);
        }
    }

    @Override
    public String format(Object value, Locale locale) {
        if (value == null) {
            return "";
        }

        FormatStrings formatStrings = formatStringsRegistry.getFormatStringsOrNull(locale);
        if (formatStrings == null) {
            return format(value);
        }

        DateFormat format = new SimpleDateFormat(formatStrings.getTimeFormat());
        format.setLenient(false);

        return format.format(value);
    }

    @Override
    public Date parse(String value) throws ParseException {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        return Date.from(DateTimeFormatter.ISO_TIME.parse(value.trim(), LocalTime::from).atDate(Instant
                .ofEpochMilli(0L)
                .atOffset(ZoneOffset.UTC)
                .toLocalDate()).
                atZone(ZoneId.systemDefault()).toInstant());
    }

    @Override
    public Date parse(String value, Locale locale) throws ParseException {
        if (StringUtils.isBlank(value)) {
            return null;
        }

        FormatStrings formatStrings = formatStringsRegistry.getFormatStringsOrNull(locale);
        if (formatStrings == null) {
            return parse(value);
        }

        DateFormat format = new SimpleDateFormat(formatStrings.getTimeFormat());
        return format.parse(value.trim());
    }

    @Override
    public Map<String, Object> getParameters() {
        return ParamsMap.of("format", "HH:mm:ss");
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}