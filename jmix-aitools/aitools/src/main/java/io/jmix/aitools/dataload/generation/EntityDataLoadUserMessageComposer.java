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

/**
 * Composes the user message a generation request is sent to the model as: the natural-language prompt followed
 * by the constraints of the request — the available parameters and how each is bound, the required result
 * properties, the row-count hint — phrased for the model. Keeps that phrasing, and the add-on rules it implies,
 * inside the add-on rather than at each caller.
 * <p>
 * An implementation supplies only the constraints wording ({@link #composeConstraints}); how the message is
 * assembled from it is the default of {@link #compose}.
 */
public interface EntityDataLoadUserMessageComposer {

    /**
     * Phrases the constraints of a request for the model.
     *
     * @param request the generation request
     * @return the constraints part of the user message, or {@code ""} when the request carries none
     */
    String composeConstraints(EntityDataLoadGenerationRequest request);

    /**
     * Composes the whole user message for a request.
     *
     * @param request the generation request
     * @return the prompt followed by its {@link #composeConstraints constraints}, or the prompt alone when the
     *         request carries none
     */
    default String compose(EntityDataLoadGenerationRequest request) {
        return request.getPrompt() + composeConstraints(request);
    }
}
