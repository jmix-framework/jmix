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

import java.util.List;
import java.util.Map;

/**
 * Turns a data set prompt into a JPQL query and executes it. The only boundary between Reports and the
 * AI Tools add-on: an implementation is present in the application context only if that add-on is.
 */
public interface LlmDataQueryService {

    /**
     * Generates a query for the given prompt.
     *
     * @param request prompt together with the parameters the query may reference
     * @return the generated query
     * @throws LlmDataQueryException if the query cannot be generated or is rejected as invalid
     */
    LlmDataQuery generate(LlmQueryGenerationRequest request);

    /**
     * Executes a generated query and returns its rows.
     * <p>
     * Data access constraints of the current user apply, so the rows may be narrower or shorter than the
     * query alone would suggest.
     *
     * @param request query, arguments and row limit
     * @return rows keyed by {@link LlmDataQuery#getResultProperties()}
     * @throws LlmDataQueryException if the query is rejected as invalid or its execution fails
     */
    List<Map<String, Object>> execute(LlmQueryExecutionRequest request);
}
