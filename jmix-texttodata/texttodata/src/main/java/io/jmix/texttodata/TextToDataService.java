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

package io.jmix.texttodata;

import io.jmix.texttodata.generation.GeneratedJpqlResult;
import io.jmix.texttodata.generation.JpqlGenerationRequest;
import io.jmix.texttodata.generation.JpqlGenerationService;
import io.jmix.texttodata.repair.JpqlRepairResult;
import io.jmix.texttodata.repair.JpqlRepairService;
import io.jmix.texttodata.validation.JpqlValidationResult;
import io.jmix.texttodata.validation.JpqlValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("textdt_TextToDataService")
public class TextToDataService {

    @Autowired
    protected JpqlGenerationService jpqlGenerationService;

    @Autowired
    protected JpqlValidationService jpqlValidationService;

    @Autowired
    protected JpqlRepairService jpqlRepairService;

    public TextToDataResult generateJpql(String userText) {
        JpqlGenerationRequest generationRequest = jpqlGenerationService.prepareRequest(userText);

        GeneratedJpqlResult generatedJpqlResult = jpqlGenerationService.generate(generationRequest);

        JpqlValidationResult validationResult = jpqlValidationService.validate(generatedJpqlResult);

        JpqlRepairResult repairResult = jpqlRepairService.repairIfNeeded(
                generationRequest, generatedJpqlResult, validationResult);

        return new TextToDataResult(
                generationRequest,
                repairResult.getGeneratedJpqlResult(),
                repairResult.getValidationResult(),
                repairResult.getRepairAttempts(),
                repairResult.isRepaired()
        );
    }
}
