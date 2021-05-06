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

package io.jmix.ui.app.propertyfilter.dateinterval;

import com.google.common.base.Strings;
import io.jmix.core.Messages;
import io.jmix.core.annotation.Internal;
import io.jmix.ui.app.propertyfilter.dateinterval.predefined.PredefinedDateInterval;
import io.jmix.ui.app.propertyfilter.dateinterval.predefined.PredefinedDateIntervalRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.regex.Pattern;

/**
 * Utility class for date intervals.
 *
 * @see PredefinedDateInterval
 * @see DateInterval
 */
@Internal
@Component("ui_DateIntervalUtils")
public class DateIntervalUtils {

    protected PredefinedDateIntervalRegistry predefinedIntervalFactory;
    protected Messages messages;

    protected static final String INCLUDING_CURRENT_DESCR = "including_current";

    protected static final Pattern NEXT_LAST_PATTERN =
            Pattern.compile("(NEXT|LAST)\\s+\\d+\\s+(DAY|MONTH|MINUTE|HOUR)(\\s+including_current)?");

    protected static final Pattern PREDEFINED_PATTERN =
            Pattern.compile("PREDEFINED\\s+\\w+");

    @Autowired
    public void setPredefinedIntervalFactory(PredefinedDateIntervalRegistry predefinedIntervalFactory) {
        this.predefinedIntervalFactory = predefinedIntervalFactory;
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.messages = messages;
    }

    /**
     * Parses string presentation of date interval to {@link BaseDateInterval}.
     *
     * @param dateInterval string presentation of date interval
     * @return configured date interval or {@code null} if input parameter is null or empty.
     * @see DateInterval
     * @see PredefinedDateInterval
     */
    @Nullable
    public BaseDateInterval parseDateInterval(String dateInterval) {
        if (Strings.isNullOrEmpty(dateInterval)) {
            return null;
        }

        if (!NEXT_LAST_PATTERN.matcher(dateInterval).matches()
                && !PREDEFINED_PATTERN.matcher(dateInterval).matches()) {
            throw new IllegalArgumentException("Wrong filter date interval string format");
        }

        String[] parts = dateInterval.split("\\s+");
        BaseDateInterval.Type type = BaseDateInterval.Type.valueOf(parts[0]);

        if (type == BaseDateInterval.Type.PREDEFINED) {
            return predefinedIntervalFactory.getIntervalByName(parts[1])
                    .orElseThrow(() ->
                            new IllegalArgumentException("There is no predefined date interval with given name: '"
                                    + parts[1] + "'"));
        } else {
            Integer number = Integer.valueOf(parts[1]);
            DateInterval.TimeUnit timeUnit = DateInterval.TimeUnit.valueOf(parts[2]);
            Boolean includeCurrent = parts.length == 4 && INCLUDING_CURRENT_DESCR.equals(parts[3]);

            return new DateInterval(type, number, timeUnit, includeCurrent);
        }
    }

    /**
     * Formats date interval to string presentation.
     *
     * @param dateInterval date interval instance
     * @return raw presentation of date interval
     * @see DateInterval
     * @see PredefinedDateInterval
     */
    public String formatDateInterval(BaseDateInterval dateInterval) {
        BaseDateInterval.Type type = dateInterval.getType();

        if (type == BaseDateInterval.Type.PREDEFINED) {
            return type.name() + " " + ((PredefinedDateInterval) dateInterval).getName();
        } else {
            DateInterval interval = (DateInterval) dateInterval;
            return type.name() + " " + interval.getNumber() + " " + interval.getTimeUnit()
                    + (Boolean.TRUE.equals(interval.getIncludingCurrent()) ? " " + INCLUDING_CURRENT_DESCR : "");
        }
    }

    /**
     * Formats date interval and gets localized value.
     *
     * @param dateInterval date interval instance
     * @return localized value
     * @see DateInterval
     * @see PredefinedDateInterval
     */
    @Nullable
    public String formatDateIntervalToLocalizedValue(@Nullable BaseDateInterval dateInterval) {
        if (dateInterval == null) {
            return null;
        }

        BaseDateInterval.Type type = dateInterval.getType();

        if (type == DateInterval.Type.PREDEFINED) {
            return ((PredefinedDateInterval) dateInterval).getLocalizedCaption();
        } else {
            DateInterval interval = (DateInterval) dateInterval;
            Boolean include = interval.getIncludingCurrent();
            return messages.getMessage(this.getClass(), type.name().toLowerCase())
                    + " "
                    + interval.getNumber()
                    + " "
                    + messages.getMessage(this.getClass(), interval.getTimeUnit().name().toLowerCase()).toLowerCase()
                    + (Boolean.TRUE.equals(include)
                    ? " " + messages.getMessage(this.getClass(), "dateIntervals.includingCurrent")
                    : "");
        }
    }
}
