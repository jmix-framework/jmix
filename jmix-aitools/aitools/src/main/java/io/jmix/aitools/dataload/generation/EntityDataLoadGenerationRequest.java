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

package io.jmix.aitools.dataload.generation;

import org.jspecify.annotations.Nullable;

import java.util.List;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

/**
 * A generation request carrying, besides the natural-language prompt, the constraints a caller knows as data:
 * the parameters the query may reference and how each is bound, and the result columns it must expose under
 * fixed aliases. All constraints are optional; a request with only a prompt generates exactly as free text does.
 */
public class EntityDataLoadGenerationRequest {

    protected String prompt;
    protected List<EntityDataLoadQueryParameter> availableParameters = List.of();
    protected List<String> requiredResultProperties = List.of();

    public EntityDataLoadGenerationRequest(String prompt) {
        checkNotNullArgument(prompt, "prompt is null");
        this.prompt = prompt;
    }

    /**
     * Sets the parameters the generated query may reference.
     *
     * @param availableParameters the parameters, or {@code null} for none
     * @return this request
     */
    public EntityDataLoadGenerationRequest setAvailableParameters(
            @Nullable List<EntityDataLoadQueryParameter> availableParameters) {
        this.availableParameters = availableParameters == null ? List.of() : List.copyOf(availableParameters);
        return this;
    }

    /**
     * Sets the aliases the generated query must select and expose, verbatim.
     *
     * @param requiredResultProperties the required aliases, or {@code null} for none
     * @return this request
     */
    public EntityDataLoadGenerationRequest setRequiredResultProperties(
            @Nullable List<String> requiredResultProperties) {
        this.requiredResultProperties = requiredResultProperties == null
                ? List.of()
                : List.copyOf(requiredResultProperties);
        return this;
    }

    /**
     * Returns the text the caller asks the model to answer.
     *
     * @return the natural-language prompt
     */
    public String getPrompt() {
        return prompt;
    }

    /**
     * Returns the parameters the generated query may reference.
     *
     * @return the parameters; empty when none were given
     */
    public List<EntityDataLoadQueryParameter> getAvailableParameters() {
        return availableParameters;
    }

    /**
     * Returns the aliases the generated query must select and expose verbatim.
     *
     * @return the required aliases; empty when none were given
     */
    public List<String> getRequiredResultProperties() {
        return requiredResultProperties;
    }
}
