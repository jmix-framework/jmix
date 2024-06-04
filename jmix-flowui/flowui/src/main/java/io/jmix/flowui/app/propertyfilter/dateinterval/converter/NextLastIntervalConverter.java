/*
 * Copyright 2024 Haulmont.
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

package io.jmix.flowui.app.propertyfilter.dateinterval.converter;

import com.google.common.base.Strings;
import io.jmix.core.Messages;
import io.jmix.core.annotation.Internal;
import io.jmix.flowui.app.propertyfilter.dateinterval.model.BaseDateInterval;
import io.jmix.flowui.app.propertyfilter.dateinterval.model.DateInterval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Internal
@Component("flowui_NextLastIntervalConverter")
public class NextLastIntervalConverter implements DateIntervalConverter {

    public static final Pattern NEXT_LAST_PATTERN =
            Pattern.compile("(NEXT|LAST)\\s+\\d+\\s+(DAY|MONTH|MINUTE|HOUR|YEAR)(\\s+including_current)?");
    protected static final String DEFAULT_NEXT_LAST_FORMATTING_PATTERN = "%s %d %s%s";

    protected Messages messages;

    @Autowired
    public void setMessages(Messages messages) {
        this.messages = messages;
    }

    @Nullable
    @Override
    public BaseDateInterval parse(String dateInterval) {
        if (Strings.isNullOrEmpty(dateInterval)) {
            return null;
        }

        if (!matches(dateInterval)) {
            throw new IllegalArgumentException("Wrong filter last or next date interval string format");
        }

        String[] parts = dateInterval.split("\\s+");
        BaseDateInterval.Type type = BaseDateInterval.Type.valueOf(parts[0]);

        Integer number = Integer.valueOf(parts[1]);
        DateInterval.TimeUnit timeUnit = DateInterval.TimeUnit.valueOf(parts[2]);
        Boolean includingCurrent = parts.length == 4 && INCLUDING_CURRENT_DESCRIPTION.equals(parts[3]);


        return new DateInterval(type, number, timeUnit, includingCurrent);
    }

    @Override
    public String format(BaseDateInterval dateInterval) {
        DateInterval interval = (DateInterval) dateInterval;
        BaseDateInterval.Type type = dateInterval.getType();
        Boolean includingCurrent = interval.getIncludingCurrent();

        return DEFAULT_NEXT_LAST_FORMATTING_PATTERN.formatted(
                type.name(),
                interval.getNumber(),
                interval.getTimeUnit(),
                Boolean.TRUE.equals(includingCurrent) ? " " + INCLUDING_CURRENT_DESCRIPTION : ""
        );
    }

    @Nullable
    @Override
    public String getLocalizedValue(@Nullable BaseDateInterval dateInterval) {
        if (dateInterval == null) {
            return null;
        }

        DateInterval interval = (DateInterval) dateInterval;
        BaseDateInterval.Type type = dateInterval.getType();
        Boolean includingCurrent = interval.getIncludingCurrent();

        return DEFAULT_NEXT_LAST_FORMATTING_PATTERN.formatted(
                messages.getMessage(type),
                interval.getNumber(),
                messages.getMessage(interval.getTimeUnit()).toLowerCase(),
                Boolean.TRUE.equals(includingCurrent)
                        ? " " + messages.getMessage(this.getClass(), "dateIntervals.includingCurrent")
                        : ""
        );
    }

    @Override
    public boolean matches(String dateInterval) {
        return NEXT_LAST_PATTERN.matcher(dateInterval).matches();
    }

    @Override
    public boolean supports(BaseDateInterval.Type type) {
        return type == BaseDateInterval.Type.NEXT
                || type == BaseDateInterval.Type.LAST;
    }
}
