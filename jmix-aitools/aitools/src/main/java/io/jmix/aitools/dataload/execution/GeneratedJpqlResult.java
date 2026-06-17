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

package io.jmix.aitools.dataload.execution;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Structured JPQL draft returned by the generation layer.
 * <p>
 * This type represents what the LLM produced after generation or repair.
 */
@NullMarked
public class GeneratedJpqlResult {

    protected String jpql;
    protected List<GeneratedJpqlParameter> parameters;
    protected String explanation;
    protected List<String> warnings;

    @Nullable
    protected Integer maxResults;
    @Nullable
    protected Integer firstResult;

    /**
     * Creates a result without execution hints ({@code maxResults} / {@code firstResult} are unset).
     */
    public GeneratedJpqlResult(String jpql,
                               List<GeneratedJpqlParameter> parameters,
                               String explanation,
                               List<String> warnings) {
        this(jpql, parameters, explanation, warnings, null, null);
    }

    /**
     * @param jpql        generated JPQL text
     * @param parameters  structured query parameters
     * @param explanation short human-readable explanation of the query intent
     * @param warnings    warnings produced during generation
     * @param maxResults  requested maximum number of rows
     * @param firstResult requested row offset
     */
    public GeneratedJpqlResult(String jpql,
                               List<GeneratedJpqlParameter> parameters,
                               String explanation,
                               List<String> warnings,
                               @Nullable Integer maxResults,
                               @Nullable Integer firstResult) {
        this.jpql = jpql;
        this.parameters = parameters;
        this.explanation = explanation;
        this.warnings = warnings;
        this.maxResults = maxResults;
        this.firstResult = firstResult;
    }

    /**
     * Returns the generated JPQL text.
     *
     * @return JPQL text
     */
    public String getJpql() {
        return jpql;
    }

    /**
     * Returns the structured parameters produced together with the JPQL draft.
     *
     * @return query parameters
     */
    public List<GeneratedJpqlParameter> getParameters() {
        return parameters;
    }

    /**
     * Returns a short human-readable explanation of the generated query intent.
     *
     * @return query explanation
     */
    public String getExplanation() {
        return explanation;
    }

    /**
     * Returns warnings produced during generation, for example ambiguity notes.
     *
     * @return generation warnings
     */
    public List<String> getWarnings() {
        return warnings;
    }

    /**
     * Returns the requested maximum number of rows, if the generation layer provided it.
     * <p>
     * This value is still part of the generation-stage result and may later be normalized
     * together with the JPQL text by post-processing.
     *
     * @return maximum number of rows, or {@code null} if not specified
     */
    @Nullable
    public Integer getMaxResults() {
        return maxResults;
    }

    /**
     * Returns the requested row offset, if the generation layer provided it.
     * <p>
     * This value is still part of the generation-stage result and may later be normalized
     * together with the JPQL text by post-processing.
     *
     * @return row offset, or {@code null} if not specified
     */
    @Nullable
    public Integer getFirstResult() {
        return firstResult;
    }

    @Override
    public String toString() {
        return "GeneratedJpqlResult{" +
                "jpql='" + jpql + '\'' +
                ", parameters=" + parameters +
                ", explanation='" + explanation + '\'' +
                ", warnings=" + warnings +
                ", maxResults=" + maxResults +
                ", firstResult=" + firstResult +
                '}';
    }
}
