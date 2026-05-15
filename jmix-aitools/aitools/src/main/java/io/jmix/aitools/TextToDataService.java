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

package io.jmix.aitools;

import io.jmix.aitools.dataload.generation.GeneratedJpqlResult;
import io.jmix.aitools.dataload.generation.JpqlGenerationRequest;
import io.jmix.aitools.dataload.generation.JpqlGenerationService;
import io.jmix.aitools.dataload.postprocess.JpqlPostProcessingService;
import io.jmix.aitools.dataload.postprocess.PostProcessedResult;
import io.jmix.aitools.dataload.repair.JpqlRepairResult;
import io.jmix.aitools.dataload.repair.JpqlRepairService;
import io.jmix.aitools.dataload.validation.JpqlValidationResult;
import io.jmix.aitools.dataload.validation.JpqlValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("textdt_TextToDataService")
public class TextToDataService {

    @Autowired
    protected JpqlGenerationService jpqlGenerationService;

    @Autowired
    protected JpqlValidationService jpqlValidationService;

    @Autowired
    protected JpqlPostProcessingService jpqlPostProcessingService;

    @Autowired
    protected JpqlRepairService jpqlRepairService;

    public TextToDataResult generateJpql(String userText) {
        // Generate request and call LLM
        JpqlGenerationRequest generationRequest = jpqlGenerationService.prepareRequest(userText);
        GeneratedJpqlResult generatedJpqlResult = jpqlGenerationService.generate(generationRequest);

        // Postprocess generated JPQL (parameters, pagination, etc.)
        PostProcessedResult postProcessedResult = jpqlPostProcessingService.process(generatedJpqlResult);

        // Validate result
        JpqlValidationResult validationResult = jpqlValidationService.validate(postProcessedResult.getGeneratedJpqlResult());

        // Try to "repair" JPQL if needed (requires LLM call)
        JpqlRepairResult repairResult = jpqlRepairService.repairIfNeeded(
                generationRequest, postProcessedResult.getGeneratedJpqlResult(), validationResult);

        // Postprocess generated JPQL (parameters, pagination, etc.)
        PostProcessedResult repairedPostProcessedResult = jpqlPostProcessingService.process(repairResult.getGeneratedJpqlResult());

        return new TextToDataResult(
                generationRequest,
                repairedPostProcessedResult.getGeneratedJpqlResult(),
                repairedPostProcessedResult,
                repairResult.getValidationResult(),
                repairResult.getRepairAttempts(),
                repairResult.isRepaired()
        );
    }
}
