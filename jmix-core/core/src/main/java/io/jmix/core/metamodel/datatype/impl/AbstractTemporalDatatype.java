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

import io.jmix.core.metamodel.annotation.DateTimeFormat;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.FormatStrings;
import io.jmix.core.metamodel.datatype.FormatStringsRegistry;
import io.jmix.core.metamodel.datatype.ParameterizedDatatype;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.text.ParseException;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQuery;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

public abstract class AbstractTemporalDatatype<T extends TemporalAccessor> implements Datatype<T>, ParameterizedDatatype {

    @Autowired
    protected FormatStringsRegistry formatStringsRegistry;

    protected final DateTimeFormatter formatter;

    public AbstractTemporalDatatype(DateTimeFormatter formatter) {
        this.formatter = formatter;
    }

    public AbstractTemporalDatatype() {
        DateTimeFormat dateTimeFormat = getClass().getAnnotation(DateTimeFormat.class);
        if (dateTimeFormat != null) {
            formatter = DateTimeFormatter.ofPattern(dateTimeFormat.value());
        } else {
            formatter = getDateTimeFormatter();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public String format(Object value) {
        if (value == null) {
            return "";
        } else {
            return formatter.format((T) value);
        }
    }

    @Override
    public String format(@Nullable Object value, Locale locale) {
        if (value == null) {
            return "";
        }

        FormatStrings formatStrings = formatStringsRegistry.getFormatStringsOrNull(locale);
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
        return formatter.parse(value.trim(), newInstance());
    }

    @Nullable
    @Override
    public T parse(@Nullable String value, Locale locale) throws ParseException {
        if (StringUtils.isBlank(value)) {
            return null;
        }

        FormatStrings formatStrings = formatStringsRegistry.getFormatStringsOrNull(locale);
        if (formatStrings == null) {
            return parse(value);
        }

        DateTimeFormatter formatter = getDateTimeFormatter(formatStrings, locale);
        return formatter.parse(value.trim(), newInstance());
    }

    @Override
    public Map<String, Object> getParameters() {
        return Collections.emptyMap();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    protected abstract DateTimeFormatter getDateTimeFormatter();

    protected abstract DateTimeFormatter getDateTimeFormatter(FormatStrings formatStrings, Locale locale);

    protected abstract TemporalQuery<T> newInstance();
}
