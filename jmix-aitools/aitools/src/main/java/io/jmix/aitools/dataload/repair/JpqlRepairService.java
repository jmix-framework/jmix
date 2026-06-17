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

import io.jmix.aitools.AiToolsDataLoadProperties;
import io.jmix.aitools.dataload.execution.JpqlExecutionRequest;
import io.jmix.aitools.dataload.execution.GeneratedJpqlResult;
import io.jmix.aitools.dataload.validation.JpqlValidationResult;
import io.jmix.aitools.dataload.validation.JpqlValidationService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Repairs an invalid query by running the configured {@link JpqlRepairer}, re-validating after each
 * attempt until the query becomes valid or the attempt limit is reached.
 * <p>
 * If no {@code JpqlRepairer} bean is available, the query is returned unchanged.
 */
@Component("aitls_JpqlRepairService")
public class JpqlRepairService {

    @Autowired
    protected AiToolsDataLoadProperties dataLoadProperties;

    @Autowired
    protected JpqlValidationService jpqlValidationService;

    @Autowired
    protected ObjectProvider<JpqlRepairer> jpqlRepairerProvider;

    /**
     * Repairs the query if its validation failed, using the configured maximum number of attempts.
     *
     * @param executionRequest    original execution request
     * @param generatedJpqlResult current query draft
     * @param validationResult    validation result of the current draft
     * @return repair result; the query is returned unchanged if it is already valid
     * @see AiToolsDataLoadProperties#getMaxRepairAttempts()
     */
    public JpqlRepairResult repairIfNeeded(JpqlExecutionRequest executionRequest,
                                           GeneratedJpqlResult generatedJpqlResult,
                                           JpqlValidationResult validationResult) {
        return repairIfNeeded(executionRequest, generatedJpqlResult, validationResult,
                dataLoadProperties.getMaxRepairAttempts());
    }

    /**
     * Repairs the query if its validation failed, retrying up to {@code maxRepairAttempts} times.
     * <p>
     * The query is returned unchanged (with {@code repaired = false}) if it is already valid, if
     * {@code maxRepairAttempts} is not positive, or if no {@link JpqlRepairer} bean is available.
     *
     * @param executionRequest    original execution request
     * @param generatedJpqlResult current query draft
     * @param validationResult    validation result of the current draft
     * @param maxRepairAttempts   maximum number of repair attempts
     * @return repair result with the final query draft and its validation result
     */
    public JpqlRepairResult repairIfNeeded(JpqlExecutionRequest executionRequest,
                                           GeneratedJpqlResult generatedJpqlResult,
                                           JpqlValidationResult validationResult,
                                           int maxRepairAttempts) {
        if (validationResult.isValid() || maxRepairAttempts <= 0) {
            return new JpqlRepairResult(generatedJpqlResult, validationResult, 0, false);
        }

        JpqlRepairer repairer = jpqlRepairerProvider.getIfAvailable();
        if (repairer == null) {
            return new JpqlRepairResult(generatedJpqlResult, validationResult, 0, false);
        }

        GeneratedJpqlResult currentResult = generatedJpqlResult;
        JpqlValidationResult currentValidation = validationResult;

        for (int attempt = 1; attempt <= maxRepairAttempts; attempt++) {
            GeneratedJpqlResult repairedResult = repairer.repair(
                    new JpqlRepairRequest(executionRequest, currentResult, currentValidation, attempt));
            if (repairedResult == null) {
                return new JpqlRepairResult(currentResult, currentValidation, attempt, true);
            }

            currentResult = repairedResult;
            currentValidation = jpqlValidationService.validate(currentResult);

            if (currentValidation.isValid()) {
                return new JpqlRepairResult(currentResult, currentValidation, attempt, true);
            }
        }

        return new JpqlRepairResult(currentResult, currentValidation, maxRepairAttempts, true);
    }
}
