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

package io.jmix.flowui.app.propertyfilter.dateinterval.model;

import io.jmix.flowui.app.propertyfilter.dateinterval.model.predefined.PredefinedDateInterval;

import java.util.function.Function;

/**
 * Base interface for date intervals. Extending {@link Function} interface it enables to format interval to JPQL
 * string operation with property.
 */
public interface BaseDateInterval extends Function<String, String> {

    /**
     * @return type of date interval
     */
    Type getType();

    /**
     * @param property entity property
     * @return formatted JPQL string operation
     */
    @Override
    default String apply(String property) {
        return property;
    }

    /**
     * Type of date interval.
     */
    enum Type {

        /**
         * Interval the preceding to the current.
         *
         * @see DateInterval
         */
        LAST,

        /**
         * Interval from the current to future.
         *
         * @see DateInterval
         */
        NEXT,

        /**
         * Predefined date interval.
         *
         * @see PredefinedDateInterval
         */
        PREDEFINED,

        /**
         * Interval that uses date and time constants (e.g. first day of the current week).
         */
        RELATIVE
    }
}
