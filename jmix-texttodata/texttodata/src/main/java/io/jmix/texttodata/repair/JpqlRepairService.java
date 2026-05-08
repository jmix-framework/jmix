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

package io.jmix.texttodata.repair;

import io.jmix.texttodata.TextToDataProperties;
import io.jmix.texttodata.generation.GeneratedJpqlResult;
import io.jmix.texttodata.generation.JpqlGenerationRequest;
import io.jmix.texttodata.validation.JpqlValidationResult;
import io.jmix.texttodata.validation.JpqlValidationService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("textdt_JpqlRepairService")
public class JpqlRepairService {

    @Autowired
    protected TextToDataProperties textToDataProperties;

    @Autowired
    protected JpqlValidationService jpqlValidationService;

    @Autowired
    protected ObjectProvider<JpqlRepairer> textToJpqlRepairerProvider;

    public JpqlRepairResult repairIfNeeded(JpqlGenerationRequest generationRequest,
                                           GeneratedJpqlResult generatedJpqlResult,
                                           JpqlValidationResult validationResult) {
        return repairIfNeeded(generationRequest, generatedJpqlResult, validationResult, textToDataProperties.getMaxRepairAttempts());
    }

    public JpqlRepairResult repairIfNeeded(JpqlGenerationRequest generationRequest,
                                           GeneratedJpqlResult generatedJpqlResult,
                                           JpqlValidationResult validationResult,
                                           int maxRepairAttempts) {
        if (validationResult.isValid() || maxRepairAttempts <= 0) {
            return new JpqlRepairResult(generatedJpqlResult, validationResult, 0, false);
        }

        JpqlRepairer repairer = textToJpqlRepairerProvider.getIfAvailable();
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

            currentResult = repairedResult;
            currentValidation = jpqlValidationService.validate(currentResult);

            if (currentValidation.isValid()) {
                return new JpqlRepairResult(currentResult, currentValidation, attempt, true);
            }
        }

        return new JpqlRepairResult(currentResult, currentValidation, maxRepairAttempts, true);
    }
}
