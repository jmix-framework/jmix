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

package io.jmix.aitools.dataload.repair.impl;

import io.jmix.aitools.dataload.execution.GeneratedJpqlResult;
import io.jmix.aitools.dataload.json.EmptyObjectTolerantListDeserializer;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.annotation.JsonDeserialize;

import java.util.List;

/**
 * Raw LLM output of a repair attempt, deserialized from the model's JSON response.
 * <p>
 * Scalar fields are optional and may be absent ({@code null}); the payload is mapped to a
 * {@link GeneratedJpqlResult} before use.
 */
@NullMarked
public class GeneratedJpqlPayload {

    @Nullable
    protected String jpql;
    @Nullable
    @JsonDeserialize(using = EmptyObjectTolerantListDeserializer.class)
    protected List<GeneratedJpqlParameterPayload> parameters;
    @Nullable
    protected String explanation;
    @Nullable
    @JsonDeserialize(using = EmptyObjectTolerantListDeserializer.class)
    protected List<String> warnings;
    @Nullable
    protected Integer maxResults;
    @Nullable
    protected Integer firstResult;

    /**
     * Returns the repaired JPQL query text.
     *
     * @return JPQL query, or {@code null} if the model did not provide one
     */
    @Nullable
    public String getJpql() {
        return jpql;
    }

    public void setJpql(@Nullable String jpql) {
        this.jpql = jpql;
    }

    /**
     * Returns the named query parameters.
     *
     * @return query parameters, or {@code null} if absent
     */
    @Nullable
    public List<GeneratedJpqlParameterPayload> getParameters() {
        return parameters;
    }

    public void setParameters(@Nullable List<GeneratedJpqlParameterPayload> parameters) {
        this.parameters = parameters;
    }

    /**
     * Returns the human-readable explanation of the query.
     *
     * @return query explanation, or {@code null} if absent
     */
    @Nullable
    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(@Nullable String explanation) {
        this.explanation = explanation;
    }

    /**
     * Returns the warnings produced about the query.
     *
     * @return warnings, or {@code null} if absent
     */
    @Nullable
    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(@Nullable List<String> warnings) {
        this.warnings = warnings;
    }

    /**
     * Returns the requested maximum number of rows.
     *
     * @return maximum number of rows, or {@code null} if absent
     */
    @Nullable
    public Integer getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(@Nullable Integer maxResults) {
        this.maxResults = maxResults;
    }

    /**
     * Returns the requested offset of the first row.
     *
     * @return row offset, or {@code null} if absent
     */
    @Nullable
    public Integer getFirstResult() {
        return firstResult;
    }

    public void setFirstResult(@Nullable Integer firstResult) {
        this.firstResult = firstResult;
    }
}
