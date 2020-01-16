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

package io.jmix.core.metamodel.datatypes.impl;

import io.jmix.core.commons.util.ParamsMap;
import io.jmix.core.metamodel.annotations.DatatypeDef;
import io.jmix.core.metamodel.annotations.DateTimeFormat;
import io.jmix.core.metamodel.datatypes.Datatype;
import io.jmix.core.metamodel.datatypes.FormatStrings;
import io.jmix.core.metamodel.datatypes.FormatStringsRegistry;
import io.jmix.core.metamodel.datatypes.ParameterizedDatatype;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * <code>TimeDatatype</code> works with <code>java.sql.Time</code> but is parametrized with <code>java.util.Date</code>
 * to avoid problems with casting.
 */
@DatatypeDef(id = "time", javaClass = java.sql.Time.class, defaultForClass = true, value = "jmix_TimeDatatype")
@DateTimeFormat("HH:mm:ss")
public class TimeDatatype implements Datatype<Date>, ParameterizedDatatype {

    @Inject
    protected FormatStringsRegistry formatStringsRegistry;

    private String formatPattern;

    public TimeDatatype() {
        DateTimeFormat dateTimeFormat = getClass().getAnnotation(DateTimeFormat.class);
        if (dateTimeFormat != null) {
            formatPattern = dateTimeFormat.value();
        }
    }

    @Override
    public String format(Object value) {
        if (value == null) {
            return "";
        } else {
            DateFormat format;
            if (formatPattern != null) {
                format = new SimpleDateFormat(formatPattern);
            } else {
                format = DateFormat.getTimeInstance();
            }
            format.setLenient(false);
            return format.format(value);
        }
    }

    @Override
    public String format(Object value, Locale locale) {
        if (value == null) {
            return "";
        }

        FormatStrings formatStrings = formatStringsRegistry.getFormatStrings(locale);
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
        DateFormat format;
        if (formatPattern != null) {
            format = new SimpleDateFormat(formatPattern);
        } else {
            format = DateFormat.getTimeInstance();
        }

        return format.parse(value.trim());
    }

    @Override
    public Date parse(String value, Locale locale) throws ParseException {
        if (StringUtils.isBlank(value)) {
            return null;
        }

        FormatStrings formatStrings = formatStringsRegistry.getFormatStrings(locale);
        if (formatStrings == null) {
            return parse(value);
        }

        DateFormat format = new SimpleDateFormat(formatStrings.getTimeFormat());
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