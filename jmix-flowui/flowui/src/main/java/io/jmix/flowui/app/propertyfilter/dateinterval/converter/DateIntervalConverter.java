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

import io.jmix.core.annotation.Internal;
import io.jmix.flowui.app.propertyfilter.dateinterval.model.BaseDateInterval;
import org.springframework.lang.Nullable;

/**
 * Interface provides methods for parsing/formatting date interval values.
 */
@Internal
public interface DateIntervalConverter {

    String INCLUDING_CURRENT_DESCRIPTION = "including_current";

    /**
     * Parses string presentation of date interval to {@link BaseDateInterval}.
     *
     * @param dateInterval string presentation of date interval
     * @return configured date interval or null if input parameters is null or empty.
     */
    @Nullable
    BaseDateInterval parse(String dateInterval);

    /**
     * Formats date interval to string presentation.
     *
     * @param dateInterval date interval instance
     * @return raw presentation of date interval
     */
    String format(BaseDateInterval dateInterval);

    /**
     * Formats date interval and gets localized value.
     *
     * @param dateInterval date interval instance
     * @return localized value
     */
    @Nullable
    String getLocalizedValue(@Nullable BaseDateInterval dateInterval);

    /**
     * Checks that provided date interval matches with converter's value pattern.
     *
     * @param dateInterval string presentation of date interval
     * @return {@code true} if provided data interval matches with the value pattern
     * of the converter, {@code false} otherwise
     */
    boolean matches(String dateInterval);

    /**
     * @param type date interval type
     * @return {@code true} if converter supports the given type, {@code false} otherwise
     */
    boolean supports(BaseDateInterval.Type type);
}
