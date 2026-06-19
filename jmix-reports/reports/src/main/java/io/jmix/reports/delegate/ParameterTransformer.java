/*
 * Copyright 2026 Haulmont.
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

package io.jmix.reports.delegate;

import org.jspecify.annotations.NullMarked;

import java.util.Map;

/**
 * Transformer can process input parameter values before they are passed to the reporting engine.
 * Examples of possible transformations: ordering a collection, rounding date-time to the nearest day.
 * @param <T> class of the input parameter
 */
@FunctionalInterface
@NullMarked
public interface ParameterTransformer<T> {

    /**
     * Transform input parameter's value, possibly even changing its type.
     * @param value source value
     * @param parameterValues map of all input parameters
     * @return transformed value
     */
    Object transform(T value, Map<String, Object> parameterValues);
}
