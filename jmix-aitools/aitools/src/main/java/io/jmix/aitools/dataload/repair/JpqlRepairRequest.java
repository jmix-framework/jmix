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

    public JpqlExecutionRequest getExecutionRequest() {
        return executionRequest;
    }

    public GeneratedJpqlResult getGeneratedJpqlResult() {
        return generatedJpqlResult;
    }

    public JpqlValidationResult getValidationResult() {
        return validationResult;
    }

    public int getAttempt() {
        return attempt;
    }
}
