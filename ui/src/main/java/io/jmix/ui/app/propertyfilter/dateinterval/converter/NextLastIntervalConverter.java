/*
 * Copyright 2021 Haulmont.
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

package io.jmix.ui.app.propertyfilter.dateinterval.converter;

import com.google.common.base.Strings;
import io.jmix.core.Messages;
import io.jmix.core.annotation.Internal;
import io.jmix.ui.app.propertyfilter.dateinterval.model.BaseDateInterval;
import io.jmix.ui.app.propertyfilter.dateinterval.model.DateInterval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.regex.Pattern;

@Internal
@Component("ui_NextLastIntervalConverter")
public class NextLastIntervalConverter implements DateIntervalConverter {

    public static final Pattern NEXT_LAST_PATTERN =
            Pattern.compile("(NEXT|LAST)\\s+\\d+\\s+(DAY|MONTH|MINUTE|HOUR)(\\s+including_current)?");

    protected Messages messages;

    @Autowired
    public NextLastIntervalConverter(Messages messages) {
        this.messages = messages;
    }

    @Nullable
    @Override
    public BaseDateInterval parse(String dateInterval) {
        if (Strings.isNullOrEmpty(dateInterval)) {
            return null;
        }

        if (!NEXT_LAST_PATTERN.matcher(dateInterval).matches()) {
            throw new IllegalArgumentException("Wrong filter last/next date interval string format");
        }

        String[] parts = dateInterval.split("\\s+");
        BaseDateInterval.Type type = BaseDateInterval.Type.valueOf(parts[0]);

        Integer number = Integer.valueOf(parts[1]);
        DateInterval.TimeUnit timeUnit = DateInterval.TimeUnit.valueOf(parts[2]);
        Boolean includeCurrent = parts.length == 4 && INCLUDING_CURRENT_DESCR.equals(parts[3]);

        return new DateInterval(type, number, timeUnit, includeCurrent);
    }

    @Override
    public String format(BaseDateInterval dateInterval) {
        DateInterval interval = (DateInterval) dateInterval;
        BaseDateInterval.Type type = dateInterval.getType();

        return type.name()
                + " " + interval.getNumber()
                + " " + interval.getTimeUnit()
                + (Boolean.TRUE.equals(interval.getIncludingCurrent()) ? " " + INCLUDING_CURRENT_DESCR : "");
    }

    @Nullable
    @Override
    public String getLocalizedValue(@Nullable BaseDateInterval dateInterval) {
        if (dateInterval == null) {
            return null;
        }

        DateInterval interval = (DateInterval) dateInterval;
        BaseDateInterval.Type type = dateInterval.getType();
        Boolean include = interval.getIncludingCurrent();

        return messages.getMessage(type)
                + " "
                + interval.getNumber()
                + " "
                + messages.getMessage(interval.getTimeUnit()).toLowerCase()
                + (Boolean.TRUE.equals(include)
                ? " " + messages.getMessage(this.getClass(), "dateIntervals.includingCurrent")
                : "");
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
