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
import io.jmix.ui.app.propertyfilter.dateinterval.RelativeDateTimeMomentProvider;
import io.jmix.ui.app.propertyfilter.dateinterval.model.BaseDateInterval;
import io.jmix.ui.app.propertyfilter.dateinterval.model.RelativeDateInterval;
import io.jmix.ui.app.propertyfilter.dateinterval.model.RelativeDateInterval.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.regex.Pattern;

@Internal
@Component("ui_RelativeIntervalConverter")
public class RelativeIntervalConverter implements DateIntervalConverter {

    public static final Pattern RELATIVE_PATTERN =
            Pattern.compile("RELATIVE\\s+(=|<>|>|>=|<|<=)\\s+\\w+");

    protected Messages messages;
    protected RelativeDateTimeMomentProvider relativeMomentProvider;

    @Autowired
    public RelativeIntervalConverter(Messages messages, @Nullable RelativeDateTimeMomentProvider relativeMomentProvider) {
        this.messages = messages;
        this.relativeMomentProvider = relativeMomentProvider;
    }

    @Nullable
    @Override
    public BaseDateInterval parse(String dateInterval) {
        if (Strings.isNullOrEmpty(dateInterval)) {
            return null;
        }

        if (!RELATIVE_PATTERN.matcher(dateInterval).matches()) {
            throw new IllegalArgumentException("Wrong filter relative date interval string format");
        }

        String[] parts = dateInterval.split("\\s+");

        return new RelativeDateInterval(Operation.fromValue(parts[1]), parts[2]);
    }

    @Override
    public String format(BaseDateInterval dateInterval) {
        checkType(dateInterval);

        RelativeDateInterval relativeDateInterval = (RelativeDateInterval) dateInterval;
        return BaseDateInterval.Type.RELATIVE
                + " " + relativeDateInterval.getOperation().getValue()
                + " " + relativeDateInterval.getRelativeDateTimeMomentName();
    }

    @Nullable
    @Override
    public String getLocalizedValue(@Nullable BaseDateInterval dateInterval) {
        if (dateInterval == null) {
            return null;
        }

        checkType(dateInterval);

        if (relativeMomentProvider == null) {
            throw new IllegalStateException("Cannot get localized value due to starter that provides localized messages"
                    + " for relative date and time moments is not added");
        }

        RelativeDateInterval relativeDateInterval = (RelativeDateInterval) dateInterval;
        Operation operation = relativeDateInterval.getOperation();
        Enum relativeMoment = relativeMomentProvider.getByName(relativeDateInterval.getRelativeDateTimeMomentName());

        return messages.getMessage(operation) + " " + messages.getMessage(relativeMoment).toLowerCase();
    }

    @Override
    public boolean matches(String dateInterval) {
        return RELATIVE_PATTERN.matcher(dateInterval).matches();
    }

    @Override
    public boolean supports(BaseDateInterval.Type type) {
        return type == BaseDateInterval.Type.RELATIVE;
    }

    protected void checkType(BaseDateInterval dateInterval) {
        if (!(dateInterval instanceof RelativeDateInterval)) {
            throw new IllegalArgumentException(
                    String.format("%s cannot be applied for '%s'", this.getClass().getSimpleName(),
                            dateInterval.getType()));
        }
    }
}
