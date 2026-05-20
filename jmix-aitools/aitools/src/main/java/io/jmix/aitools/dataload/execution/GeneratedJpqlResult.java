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

import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Structured JPQL draft returned by the generation layer.
 * <p>
 * This type represents what the LLM produced after generation or repair. It keeps the
 * generated JPQL text together with structured metadata such as parameters, explanation,
 * warnings, and optional execution hints.
 */
public class GeneratedJpqlResult {

    protected String jpql;
    protected List<GeneratedJpqlParameter> parameters;
    protected String explanation;
    protected List<String> warnings;
    protected Integer maxResults;
    protected Integer firstResult;

    public GeneratedJpqlResult(String jpql,
                               List<GeneratedJpqlParameter> parameters,
                               String explanation,
                               List<String> warnings) {
        this(jpql, parameters, explanation, warnings, null, null);
    }

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

    public String getJpql() {
        return jpql;
    }

    /**
     * Returns the structured parameters produced together with the JPQL draft.
     */
    public List<GeneratedJpqlParameter> getParameters() {
        return parameters;
    }

    /**
     * Returns a short human-readable explanation of the generated query intent.
     */
    public String getExplanation() {
        return explanation;
    }

    /**
     * Returns warnings produced during generation, for example ambiguity notes.
     */
    public List<String> getWarnings() {
        return warnings;
    }

    /**
     * Returns the requested maximum number of rows, if the generation layer provided it.
     * <p>
     * This value is still part of the generation-stage result and may later be normalized
     * together with the JPQL text by post-processing.
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
