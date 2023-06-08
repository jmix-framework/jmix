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
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * <code>DateDatatype</code> works with <code>java.<b>sql</b>.Date</code> but is parameterized with
 * <code>java.<b>util</b>.Date</code> to avoid problems with casting.
 */
@Internal
@DatatypeDef(id = "date", javaClass = java.sql.Date.class, defaultForClass = true, value = "core_DateDatatype")
public class DateDatatype implements Datatype<Date>, ParameterizedDatatype {

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
                    .toLocalDate().format(DateTimeFormatter.ISO_DATE);
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

        DateFormat format = new SimpleDateFormat(formatStrings.getDateFormat());
        return format.format(value);
    }

    protected java.sql.Date normalize(Date dateTime) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dateTime);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return new java.sql.Date(cal.getTimeInMillis());
    }

    @Override
    public Date parse(String value) throws ParseException {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        LocalDate localDateTime = DateTimeFormatter.ISO_DATE.parse(value.trim(), LocalDate::from);
        return normalize(Date.from(localDateTime.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
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

        DateFormat format = new SimpleDateFormat(formatStrings.getDateFormat());
        format.setLenient(false);

        return normalize(format.parse(value.trim()));
    }

    @Override
    public Map<String, Object> getParameters() {
        return ParamsMap.of("format", "yyyy-MM-dd");
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}