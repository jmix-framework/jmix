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

import java.util.Map;

/**
 * Validator invoked as part of input parameters validation on the UI.
 * Checks that selected values are mutually valid
 * (e.g. it can check that at least one of several parameters must be specified).
 */
@FunctionalInterface
@NullMarked
public interface ParametersCrossValidator {

    /**
     * Validate input parameter values.
     * If values are invalid - throw <code>ReportParametersValidationException</code>,
     * specifying a message that will be shown to the user.
     *
     * @param parameterValues parameter values
     * @throws ReportParametersValidationException if values are invalid
     */
    void validateParameters(Map<String, Object> parameterValues) throws ReportParametersValidationException;
}
