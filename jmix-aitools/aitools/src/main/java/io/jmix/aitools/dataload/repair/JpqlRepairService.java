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

import io.jmix.aitools.AiToolsProperties;
import io.jmix.aitools.dataload.generation.GeneratedJpqlResult;
import io.jmix.aitools.dataload.generation.JpqlGenerationRequest;
import io.jmix.aitools.dataload.postprocess.JpqlPostProcessingService;
import io.jmix.aitools.dataload.validation.JpqlValidationResult;
import io.jmix.aitools.dataload.validation.JpqlValidationService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("aitols_JpqlRepairService")
public class JpqlRepairService {

    @Autowired
    protected AiToolsProperties aiToolsProperties;

    @Autowired
    protected JpqlValidationService jpqlValidationService;

    @Autowired
    protected JpqlPostProcessingService jpqlPostProcessingService;

    @Autowired
    protected ObjectProvider<JpqlRepairer> jpqlRepairerProvider;

    public JpqlRepairResult repairIfNeeded(JpqlGenerationRequest generationRequest,
                                           GeneratedJpqlResult generatedJpqlResult,
                                           JpqlValidationResult validationResult) {
        return repairIfNeeded(generationRequest, generatedJpqlResult, validationResult, aiToolsProperties.getMaxRepairAttempts());
    }

    public JpqlRepairResult repairIfNeeded(JpqlGenerationRequest generationRequest,
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
                    new JpqlRepairRequest(generationRequest, currentResult, currentValidation, attempt));
            if (repairedResult == null) {
                return new JpqlRepairResult(currentResult, currentValidation, attempt, true);
            }

            currentResult = jpqlPostProcessingService.process(repairedResult).getGeneratedJpqlResult();
            currentValidation = jpqlValidationService.validate(currentResult);

            if (currentValidation.isValid()) {
                return new JpqlRepairResult(currentResult, currentValidation, attempt, true);
            }
        }

        return new JpqlRepairResult(currentResult, currentValidation, maxRepairAttempts, true);
    }
}
