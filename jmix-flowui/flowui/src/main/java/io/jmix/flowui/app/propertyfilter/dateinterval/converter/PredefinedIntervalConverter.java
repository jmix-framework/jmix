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
import io.jmix.core.annotation.Internal;
import io.jmix.flowui.app.propertyfilter.dateinterval.model.BaseDateInterval;
import io.jmix.flowui.app.propertyfilter.dateinterval.model.predefined.PredefinedDateInterval;
import io.jmix.flowui.app.propertyfilter.dateinterval.model.predefined.PredefinedDateIntervalRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Internal
@Component("flowui_PredefinedIntervalConverter")
public class PredefinedIntervalConverter implements DateIntervalConverter {

    public static final Pattern PREDEFINED_PATTERN = Pattern.compile("PREDEFINED\\s+\\w+");

    protected PredefinedDateIntervalRegistry predefinedDateIntervalFactory;

    @Autowired
    public void setPredefinedDateIntervalFactory(PredefinedDateIntervalRegistry predefinedDateIntervalFactory) {
        this.predefinedDateIntervalFactory = predefinedDateIntervalFactory;
    }

    @Nullable
    @Override
    public BaseDateInterval parse(String dateInterval) {
        if (Strings.isNullOrEmpty(dateInterval)) {
            return null;
        }

        if (!matches(dateInterval)) {
            throw new IllegalArgumentException("Wrong filter predefined date interval string format");
        }

        String[] parts = dateInterval.split("\\s+");

        return predefinedDateIntervalFactory.getIntervalByName(parts[1])
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "There is no predefined date interval with given name: '%s'".formatted(parts[1]))
                );
    }

    @Override
    public String format(BaseDateInterval dateInterval) {
        return "%s %s".formatted(BaseDateInterval.Type.PREDEFINED, ((PredefinedDateInterval) dateInterval).getName());
    }

    @Nullable
    @Override
    public String getLocalizedValue(@Nullable BaseDateInterval dateInterval) {
        if (dateInterval == null) {
            return null;
        }

        return ((PredefinedDateInterval) dateInterval).getLocalizedName();
    }

    @Override
    public boolean matches(String dateInterval) {
        return PREDEFINED_PATTERN.matcher(dateInterval).matches();
    }

    @Override
    public boolean supports(BaseDateInterval.Type type) {
        return type == BaseDateInterval.Type.PREDEFINED;
    }
}
