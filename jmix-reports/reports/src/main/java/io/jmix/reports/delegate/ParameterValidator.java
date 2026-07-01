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

import io.jmix.reports.exception.ReportParametersValidationException;
import org.jspecify.annotations.NullMarked;

/**
 * Interface for implementing a custom input parameter validator.
 * @param <T> parameter type
 */
@NullMarked
public interface ParameterValidator<T> {

    /**
     * Validate input value.
     * If one is invalid - throw <code>ReportParametersValidationException</code>,
     * specifying a message that will be shown to the user.
     *
     * @param value value to validate
     * @throws ReportParametersValidationException if value is invalid
     */
    void validate(T value) throws ReportParametersValidationException;
}
