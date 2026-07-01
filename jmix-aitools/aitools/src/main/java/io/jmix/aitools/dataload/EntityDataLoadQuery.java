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

package io.jmix.aitools.dataload;

import io.jmix.aitools.dataload.execution.GeneratedJpqlParameter;
import io.jmix.aitools.dataload.execution.JpqlExecutionService;
import io.jmix.aitools.dataload.generation.EntityDataLoadGenerationService;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Structured JPQL query draft produced by {@link EntityDataLoadGenerationService} for a
 * natural-language request. Carries everything {@link JpqlExecutionService} needs to validate
 * and run the query, plus the LLM's explanation and warnings for the caller.
 */
public class EntityDataLoadQuery {

    protected String jpql;
    protected List<GeneratedJpqlParameter> parameters;
    protected List<String> resultProperties;
    protected String explanation;
    protected List<String> warnings;
    @Nullable
    protected Integer maxResults;
    @Nullable
    protected Integer firstResult;

    public EntityDataLoadQuery(String jpql,
                               List<GeneratedJpqlParameter> parameters,
                               List<String> resultProperties,
                               String explanation,
                               List<String> warnings,
                               @Nullable Integer maxResults,
                               @Nullable Integer firstResult) {
        this.jpql = jpql;
        this.parameters = parameters;
        this.resultProperties = resultProperties;
        this.explanation = explanation;
        this.warnings = warnings;
        this.maxResults = maxResults;
        this.firstResult = firstResult;
    }

    /**
     * Returns the generated JPQL select query. Every SELECT expression is aliased via {@code AS}.
     *
     * @return the generated JPQL select query
     */
    public String getJpql() {
        return jpql;
    }

    /**
     * Returns named parameters that must be bound when executing {@link #getJpql()}
     *
     * @return named parameters
     */
    public List<GeneratedJpqlParameter> getParameters() {
        return parameters;
    }

    /**
     * Returns result column names in the exact order of the SELECT expressions in
     * {@link #getJpql()}; used as keys for the row maps in the execution result.
     *
     * @return result column names
     */
    public List<String> getResultProperties() {
        return resultProperties;
    }

    /**
     * @return human-readable explanation of what the query does, produced by the LLM
     */
    public String getExplanation() {
        return explanation;
    }

    /**
     * @return warnings emitted by the LLM about the generated query (assumptions made,
     * approximations, applied pagination, etc.)
     */
    public List<String> getWarnings() {
        return warnings;
    }

    /**
     * Returns requested row limit, or {@code null} to apply the default limit on execution.
     *
     * @return requested row limit
     */
    @Nullable
    public Integer getMaxResults() {
        return maxResults;
    }

    /**
     * Rrequested offset of the first row, or {@code null} to start from the first row.
     *
     * @return requested offset of the first row
     */
    @Nullable
    public Integer getFirstResult() {
        return firstResult;
    }
}
