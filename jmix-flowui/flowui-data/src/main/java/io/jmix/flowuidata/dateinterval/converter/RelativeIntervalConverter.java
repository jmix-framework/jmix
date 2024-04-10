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

package io.jmix.flowuidata.dateinterval.converter;

import com.google.common.base.Strings;
import io.jmix.core.Messages;
import io.jmix.core.annotation.Internal;
import io.jmix.flowui.app.propertyfilter.dateinterval.converter.DateIntervalConverter;
import io.jmix.flowui.app.propertyfilter.dateinterval.model.BaseDateInterval;
import io.jmix.flowuidata.dateinterval.RelativeDateTimeMomentProvider;
import io.jmix.flowuidata.dateinterval.model.RelativeDateInterval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Internal
@Component("flowui_UiDataRelativeIntervalConverter")
public class RelativeIntervalConverter implements DateIntervalConverter {

    public static final Pattern RELATIVE_PATTERN = Pattern.compile("RELATIVE\\s+(=|<>|>|>=|<|<=)\\s+\\w+");

    protected Messages messages;
    protected RelativeDateTimeMomentProvider relativeDateTimeMomentProvider;

    @Autowired
    public void setMessages(Messages messages) {
        this.messages = messages;
    }

    @Autowired
    public void setRelativeDateTimeMomentProvider(RelativeDateTimeMomentProvider relativeDateTimeMomentProvider) {
        this.relativeDateTimeMomentProvider = relativeDateTimeMomentProvider;
    }

    @Nullable
    @Override
    public BaseDateInterval parse(String dateInterval) {
        if (Strings.isNullOrEmpty(dateInterval)) {
            return null;
        }

        if (!matches(dateInterval)) {
            throw new IllegalArgumentException("Wrong filter relative date interval string format");
        }

        String[] parts = dateInterval.split("\\s+");

        return new RelativeDateInterval(RelativeDateInterval.Operation.fromValue(parts[1]), parts[2]);
    }

    @Override
    public String format(BaseDateInterval dateInterval) {
        RelativeDateInterval relativeDateInterval = getTypedDateInterval(dateInterval);

        return "%s %s %s".formatted(
                BaseDateInterval.Type.RELATIVE,
                relativeDateInterval.getOperation().getValue(),
                relativeDateInterval.getRelativeDateTimeMomentName()
        );
    }

    @Nullable
    @Override
    public String getLocalizedValue(@Nullable BaseDateInterval dateInterval) {
        if (dateInterval == null) {
            return null;
        }

        if (relativeDateTimeMomentProvider == null) {
            throw new IllegalStateException("Cannot get localized value due to starter that provides localized messages"
                    + " for relative date and time moments is not added");
        }

        RelativeDateInterval relativeDateInterval = getTypedDateInterval(dateInterval);
        RelativeDateInterval.Operation operation = relativeDateInterval.getOperation();
        Enum<?> relativeMoment = relativeDateTimeMomentProvider.getByName(
                relativeDateInterval.getRelativeDateTimeMomentName()
        );

        return "%s %s".formatted(
                messages.getMessage(operation),
                messages.getMessage(relativeMoment).toLowerCase()
        );
    }

    @Override
    public boolean matches(String dateInterval) {
        return RELATIVE_PATTERN.matcher(dateInterval).matches();
    }

    @Override
    public boolean supports(BaseDateInterval.Type type) {
        return type == BaseDateInterval.Type.RELATIVE;
    }

    protected RelativeDateInterval getTypedDateInterval(BaseDateInterval dateInterval) {
        if (dateInterval instanceof RelativeDateInterval relativeDateInterval) {
            return relativeDateInterval;
        }

        throw new IllegalArgumentException(
                "%s cannot be applied for '%s'".formatted(getClass().getSimpleName(), dateInterval.getType())
        );
    }
}
