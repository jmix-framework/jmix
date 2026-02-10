/*
 * Copyright 2025 Haulmont.
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

package io.jmix.flowuidata.dateinterval.converter;

import com.google.common.base.Strings;
import io.jmix.core.DateTimeTransformations;
import io.jmix.core.Messages;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.DatatypeFormatter;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.flowui.app.propertyfilter.dateinterval.converter.DateIntervalConverter;
import io.jmix.flowui.app.propertyfilter.dateinterval.model.BaseDateInterval;
import io.jmix.flowuidata.dateinterval.model.CustomDateInterval;
import org.springframework.beans.factory.annotation.Autowired;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.time.LocalDate;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converter for parsing and formatting {@link CustomDateInterval} in a specific string representation.
 */
@Component("flowui_CustomDateIntervalConverter")
public class CustomDateIntervalConverter implements DateIntervalConverter {

    public static final Pattern CUSTOM_PATTERN =
            Pattern.compile("CUSTOM\\s+TYPE\\s+[\\w|\\-]+\\s+PROP\\s+[\\w|\\-]+\\s+START\\s+[\\w|\\-]+\\s+END\\s+[\\w|\\-]+");

    public static final Pattern TYPE_PATTERN = Pattern.compile("TYPE\\s+(\\w+)");
    public static final Pattern PROP_PATTERN = Pattern.compile("PROP\\s+(\\w+)");
    public static final Pattern START_PATTERN = Pattern.compile("START\\s+([\\w|\\-]+)");
    public static final Pattern END_PATTERN = Pattern.compile("END\\s+([\\w|\\-]+)");

    protected Messages messages;
    protected DatatypeRegistry datatypeRegistry;
    protected DateTimeTransformations dateTimeTransformations;
    protected Datatype<LocalDate> localDateDatatype;
    protected DatatypeFormatter datatypeFormatter;

    @Autowired
    public void setMessages(Messages messages) {
        this.messages = messages;
    }

    @Autowired
    public void setDatatypeRegistry(DatatypeRegistry datatypeRegistry) {
        this.datatypeRegistry = datatypeRegistry;
        localDateDatatype = datatypeRegistry.get(LocalDate.class);
    }

    @Autowired
    public void setDateTimeTransformations(DateTimeTransformations dateTimeTransformations) {
        this.dateTimeTransformations = dateTimeTransformations;
    }

    @Autowired
    public void setDatatypeFormatter(DatatypeFormatter datatypeFormatter) {
        this.datatypeFormatter = datatypeFormatter;
    }

    @Nullable
    @Override
    public BaseDateInterval parse(String dateInterval) {
        if (Strings.isNullOrEmpty(dateInterval)) {
            return null;
        }

        if (!matches(dateInterval)) {
            throw new IllegalArgumentException("Wrong filter custom date interval string format");
        }

        Matcher typeMatcher = TYPE_PATTERN.matcher(dateInterval);
        Matcher propMatcher = PROP_PATTERN.matcher(dateInterval);
        Matcher startMatcher = START_PATTERN.matcher(dateInterval);
        Matcher endMatcher = END_PATTERN.matcher(dateInterval);

        if (!typeMatcher.find() || !propMatcher.find() || !startMatcher.find() || !endMatcher.find()) {
            throw new IllegalArgumentException("Wrong filter custom date interval string format");
        }

        String type = typeMatcher.group(1);
        String prop = propMatcher.group(1);
        String startString = startMatcher.group(1);
        String endString = endMatcher.group(1);

        Object start;
        Object end;
        try {
            start = localDateDatatype.parse(startString);
            end = localDateDatatype.parse(endString);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Wrong filter custom date interval string format");
        }

        Class<?> javaClass = datatypeRegistry.get(type).getJavaClass();
        return new CustomDateInterval(
                prop, type,
                dateTimeTransformations.transformToType(Objects.requireNonNull(start), javaClass, null),
                dateTimeTransformations.transformToType(Objects.requireNonNull(end), javaClass, null)
        );
    }

    @Override
    public String format(BaseDateInterval dateInterval) {
        CustomDateInterval customDateInterval = (CustomDateInterval) dateInterval;

        return "%s TYPE %s PROP %s START %s END %s".formatted(
                BaseDateInterval.Type.CUSTOM, customDateInterval.getDatatype(), customDateInterval.getPropertyPath(),
                dateTimeTransformations.transformToType(customDateInterval.getStart(), LocalDate.class, null),
                dateTimeTransformations.transformToType(customDateInterval.getEnd(), LocalDate.class, null)
        );
    }

    @Nullable
    @Override
    public String getLocalizedValue(@Nullable BaseDateInterval dateInterval) {
        if (dateInterval == null) {
            return null;
        }

        CustomDateInterval customDateInterval = (CustomDateInterval) dateInterval;
        LocalDate start = (LocalDate) dateTimeTransformations.transformToType(
                customDateInterval.getStart(), LocalDate.class, null
        );
        LocalDate end = (LocalDate) dateTimeTransformations.transformToType(
                customDateInterval.getEnd(), LocalDate.class, null
        );

        return messages.formatMessage(getClass(), "customDateIntervalConverter.localizedValue",
                datatypeFormatter.formatLocalDate(start),
                datatypeFormatter.formatLocalDate(end)
        );
    }

    @Override
    public boolean matches(String dateInterval) {
        return CUSTOM_PATTERN.matcher(dateInterval).matches();
    }

    @Override
    public boolean supports(BaseDateInterval.Type type) {
        return type == BaseDateInterval.Type.CUSTOM;
    }
}
