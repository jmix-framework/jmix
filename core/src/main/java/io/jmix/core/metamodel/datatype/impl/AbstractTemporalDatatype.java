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
import io.jmix.core.metamodel.annotation.DateTimeFormat;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.FormatStrings;
import io.jmix.core.metamodel.datatype.FormatStringsRegistry;
import io.jmix.core.metamodel.datatype.ParameterizedDatatype;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.text.ParseException;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQuery;
import java.util.Locale;
import java.util.Map;

public abstract class AbstractTemporalDatatype<T extends Temporal> implements Datatype<T>, ParameterizedDatatype {

    @Inject
    protected FormatStringsRegistry formatStringsRegistry;

    protected String formatPattern;

    public AbstractTemporalDatatype() {
        DateTimeFormat dateTimeFormat = getClass().getAnnotation(DateTimeFormat.class);
        if (dateTimeFormat != null) {
            formatPattern = dateTimeFormat.value();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public String format(Object value) {
        if (value == null) {
            return "";
        } else {
            DateTimeFormatter formatter;
            if (formatPattern != null) {
                formatter = DateTimeFormatter.ofPattern(formatPattern);
            } else {
                formatter = getDateTimeFormatter();
            }
            return formatter.format((T) value);
        }
    }

    @Override
    public String format(@Nullable Object value, Locale locale) {
        if (value == null) {
            return "";
        }

        FormatStrings formatStrings = formatStringsRegistry.getFormatStrings(locale);
        if (formatStrings == null) {
            return format(value);
        }

        DateTimeFormatter formatter = getDateTimeFormatter(formatStrings, locale);
        return formatter.format((TemporalAccessor) value);
    }

    @Nullable
    @Override
    public T parse(@Nullable String value) throws ParseException {
        if (StringUtils.isBlank(value)) {
            return null;
        }

        DateTimeFormatter formatter;
        if (formatPattern != null) {
            formatter = DateTimeFormatter.ofPattern(formatPattern);
        } else {
            formatter = getDateTimeFormatter();
        }
        return formatter.parse(value.trim(), newInstance());
    }

    @Nullable
    @Override
    public T parse(@Nullable String value, Locale locale) throws ParseException {
        if (StringUtils.isBlank(value)) {
            return null;
        }

        FormatStrings formatStrings = formatStringsRegistry.getFormatStrings(locale);
        if (formatStrings == null) {
            return parse(value);
        }

        DateTimeFormatter formatter = getDateTimeFormatter(formatStrings, locale);
        return formatter.parse(value.trim(), newInstance());
    }

    @Override
    public Map<String, Object> getParameters() {
        return ParamsMap.of("format", formatPattern);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    protected abstract DateTimeFormatter getDateTimeFormatter();

    protected abstract DateTimeFormatter getDateTimeFormatter(FormatStrings formatStrings, Locale locale);

    protected abstract TemporalQuery<T> newInstance();
}
