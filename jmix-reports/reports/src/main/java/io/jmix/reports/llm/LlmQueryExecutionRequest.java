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
 * Request to execute a generated query with concrete arguments.
 *
 * @see LlmDataQueryService#execute(LlmQueryExecutionRequest)
 */
public class LlmQueryExecutionRequest {

    protected String prompt;
    protected LlmDataQuery query;
    protected List<LlmQueryParameter> arguments;

    @Nullable
    protected Integer maxResults;

    public LlmQueryExecutionRequest(String prompt,
                                    LlmDataQuery query,
                                    @Nullable List<LlmQueryParameter> arguments,
                                    @Nullable Integer maxResults) {
        checkNotNullArgument(prompt, "prompt is null");
        checkNotNullArgument(query, "query is null");

        this.prompt = prompt;
        this.query = query;
        this.arguments = arguments == null ? List.of() : List.copyOf(arguments);
        this.maxResults = maxResults;
    }

    /**
     * Returns the data set prompt the query was generated from. Nothing is regenerated here: the add-on's own
     * execution request carries the text a query answers, and Reports passes on the one the data set states
     * rather than inventing another. The text never reaches the model on a run — an invalid query fails the
     * data set instead of being repaired against it.
     *
     * @return the data set prompt
     */
    public String getPrompt() {
        return prompt;
    }

    /**
     * @return the query to execute
     */
    public LlmDataQuery getQuery() {
        return query;
    }

    /**
     * Returns the arguments to bind: one entry per parameter the query references, carrying the value and
     * the Java type it must be bound as. Parameters the query does not reference must not be listed.
     *
     * @return the arguments to bind
     */
    public List<LlmQueryParameter> getArguments() {
        return arguments;
    }

    /**
     * @return row limit for this execution, or {@code null} to fall back to the query's own limit and
     * then to the add-on's default
     */
    @Nullable
    public Integer getMaxResults() {
        return maxResults;
    }
}
