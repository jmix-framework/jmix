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
import io.jmix.texttodata.validation.JpqlValidationResult;

public class TextToDataResult {

    protected JpqlGenerationRequest generationRequest;
    protected GeneratedJpqlResult generatedJpqlResult;
    protected JpqlValidationResult validationResult;
    protected int repairAttempts;
    protected boolean repaired;

    public TextToDataResult(JpqlGenerationRequest generationRequest,
                            GeneratedJpqlResult generatedJpqlResult,
                            JpqlValidationResult validationResult,
                            int repairAttempts,
                            boolean repaired) {
        this.generationRequest = generationRequest;
        this.generatedJpqlResult = generatedJpqlResult;
        this.validationResult = validationResult;
        this.repairAttempts = repairAttempts;
        this.repaired = repaired;
    }

    public JpqlGenerationRequest getGenerationRequest() {
        return generationRequest;
    }

    public GeneratedJpqlResult getGeneratedJpqlResult() {
        return generatedJpqlResult;
    }

    public JpqlValidationResult getValidationResult() {
        return validationResult;
    }

    public boolean isValid() {
        return validationResult.isValid();
    }

    public int getRepairAttempts() {
        return repairAttempts;
    }

    public boolean isRepaired() {
        return repaired;
    }

    @Override
    public String toString() {
        return "TextToDataResult{" +
                "generationRequest=" + generationRequest +
                ", generatedJpqlResult=" + generatedJpqlResult +
                ", validationResult=" + validationResult +
                ", repairAttempts=" + repairAttempts +
                ", repaired=" + repaired +
                '}';
    }
}
