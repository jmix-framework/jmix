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

package io.jmix.reports.llm;

import org.jspecify.annotations.Nullable;

import java.util.List;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

/**
 * Request to generate a query for a data set prompt.
 *
 * @see LlmDataQueryService#generate(LlmQueryGenerationRequest)
 */
public class LlmQueryGenerationRequest {

    protected String prompt;
    protected List<LlmQueryParameter> availableParameters;

    @Nullable
    protected Integer maxResults;

    public LlmQueryGenerationRequest(String prompt,
                                     @Nullable List<LlmQueryParameter> availableParameters,
                                     @Nullable Integer maxResults) {
        checkNotNullArgument(prompt, "prompt is null");

        this.prompt = prompt;
        this.availableParameters = availableParameters == null ? List.of() : List.copyOf(availableParameters);
        this.maxResults = maxResults;
    }

    /**
     * @return the data set prompt in natural language
     */
    public String getPrompt() {
        return prompt;
    }

    /**
     * Returns the parameters the generated query may reference. Their values are irrelevant here: the
     * generated query must reference them by name and receives values only at execution time.
     *
     * @return the parameters the generated query may reference
     */
    public List<LlmQueryParameter> getAvailableParameters() {
        return availableParameters;
    }

    /**
     * @return row limit to aim for, or {@code null} to let generation decide
     */
    @Nullable
    public Integer getMaxResults() {
        return maxResults;
    }
}
