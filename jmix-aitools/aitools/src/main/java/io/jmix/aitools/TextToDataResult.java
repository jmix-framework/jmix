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
import io.jmix.aitools.dataload.postprocess.PostProcessedResult;
import io.jmix.aitools.dataload.validation.JpqlValidationResult;

/**
 * Final facade result returned by the end-to-end text-to-data pipeline.
 * <p>
 * This type combines:
 * <ul>
 *     <li>the original generation request context,</li>
 *     <li>the generated JPQL draft,</li>
 *     <li>the prepared post-processed result,</li>
 *     <li>the validation outcome,</li>
 *     <li>and repair metadata.</li>
 * </ul>
 * It is the main result type for callers that use the full pipeline through
 * {@link TextToDataService}.
 * <p>
 * The difference between the nested result types is:
 * <ul>
 *     <li>{@link GeneratedJpqlResult} describes what generation produced,</li>
 *     <li>{@link PostProcessedResult} describes the post-processed execution-oriented form,</li>
 *     <li>{@code TextToDataResult} describes the complete pipeline outcome.</li>
 * </ul>
 */
public class TextToDataResult {

    protected JpqlGenerationRequest generationRequest;
    protected GeneratedJpqlResult generatedJpqlResult;
    protected PostProcessedResult postProcessedResult;
    protected JpqlValidationResult validationResult;
    protected int repairAttempts;
    protected boolean repaired;

    public TextToDataResult(JpqlGenerationRequest generationRequest,
                            GeneratedJpqlResult generatedJpqlResult,
                            PostProcessedResult postProcessedResult,
                            JpqlValidationResult validationResult,
                            int repairAttempts,
                            boolean repaired) {
        this.generationRequest = generationRequest;
        this.generatedJpqlResult = generatedJpqlResult;
        this.postProcessedResult = postProcessedResult;
        this.validationResult = validationResult;
        this.repairAttempts = repairAttempts;
        this.repaired = repaired;
    }

    public JpqlGenerationRequest getGenerationRequest() {
        return generationRequest;
    }

    /**
     * Returns the generated JPQL draft as it stands after the full pipeline.
     * <p>
     * In practice this is usually the normalized result that survived repair and
     * post-processing, but it still represents the generation-oriented DTO.
     */
    public GeneratedJpqlResult getGeneratedJpqlResult() {
        return generatedJpqlResult;
    }

    /**
     * Returns the validation result for the final generated JPQL draft.
     */
    public JpqlValidationResult getValidationResult() {
        return validationResult;
    }

    /**
     * Returns the prepared execution-oriented result after post-processing.
     */
    public PostProcessedResult getPreparedJpqlResult() {
        return postProcessedResult;
    }

    /**
     * Returns the effective maximum number of rows prepared for execution.
     */
    public Integer getMaxResults() {
        return postProcessedResult.getMaxResults();
    }

    /**
     * Returns the effective row offset prepared for execution.
     */
    public Integer getFirstResult() {
        return postProcessedResult.getFirstResult();
    }

    /**
     * Returns whether the final result passed validation.
     */
    public boolean isValid() {
        return validationResult.isValid();
    }

    /**
     * Returns how many repair attempts were made after initial validation failed.
     */
    public int getRepairAttempts() {
        return repairAttempts;
    }

    /**
     * Returns whether the repair stage was invoked at least once.
     */
    public boolean isRepaired() {
        return repaired;
    }

    @Override
    public String toString() {
        return "TextToDataResult{" +
                "generationRequest=" + generationRequest +
                ", generatedJpqlResult=" + generatedJpqlResult +
                ", preparedJpqlResult=" + postProcessedResult +
                ", validationResult=" + validationResult +
                ", repairAttempts=" + repairAttempts +
                ", repaired=" + repaired +
                '}';
    }
}
