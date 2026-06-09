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

package io.jmix.aitools.dataload.repair;

import io.jmix.aitools.dataload.execution.JpqlExecutionRequest;
import io.jmix.aitools.dataload.execution.GeneratedJpqlResult;
import io.jmix.aitools.dataload.validation.JpqlValidationResult;

/**
 * Input for a single {@link JpqlRepairer} attempt: the original execution request, the current query
 * draft, its validation result and the attempt number.
 */
public class JpqlRepairRequest {

    protected JpqlExecutionRequest executionRequest;
    protected GeneratedJpqlResult generatedJpqlResult;
    protected JpqlValidationResult validationResult;
    protected int attempt;

    public JpqlRepairRequest(JpqlExecutionRequest executionRequest,
                             GeneratedJpqlResult generatedJpqlResult,
                             JpqlValidationResult validationResult,
                             int attempt) {
        this.executionRequest = executionRequest;
        this.generatedJpqlResult = generatedJpqlResult;
        this.validationResult = validationResult;
        this.attempt = attempt;
    }

    /**
     * Returns the original execution request that triggered the repair.
     *
     * @return execution request
     */
    public JpqlExecutionRequest getExecutionRequest() {
        return executionRequest;
    }

    /**
     * Returns the current query draft to repair.
     *
     * @return query draft
     */
    public GeneratedJpqlResult getGeneratedJpqlResult() {
        return generatedJpqlResult;
    }

    /**
     * Returns the validation result describing why the current draft is invalid.
     *
     * @return validation result
     */
    public JpqlValidationResult getValidationResult() {
        return validationResult;
    }

    /**
     * Returns the 1-based number of the current repair attempt.
     *
     * @return attempt number
     */
    public int getAttempt() {
        return attempt;
    }
}
