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

import io.jmix.aitools.dataload.EntityDataLoadQuery;

/**
 * Service for generating JPQL from user prompt.
 * <p>
 * Generation step of the natural-language data-load flow: turns a user's free-form request into a
 * structured JPQL query draft (query text, parameters and result properties).
 */
public interface EntityDataLoadGenerationService {

    /**
     * Generates a JPQL query draft from the given natural-language request.
     *
     * @param userText user request in natural language
     * @return generated query draft
     */
    EntityDataLoadQuery generate(String userText);
}
