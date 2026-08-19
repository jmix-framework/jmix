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

import io.jmix.reports.entity.DataSet;
import org.jspecify.annotations.Nullable;

import java.util.List;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

/**
 * A JPQL query generated from a natural-language prompt, together with everything needed to execute it
 * and to show it to a report designer.
 * <p>
 * An instance is stored in the data set (see {@link DataSet#getLlmGeneratedQuery()})
 * and reused by subsequent report runs, so it carries no parameter values.
 */
public class LlmDataQuery {

    protected String jpql;
    protected List<String> resultProperties;
    protected List<LlmQueryParameter> parameters;

    @Nullable
    protected String explanation;

    protected List<String> warnings;

    @Nullable
    protected Integer maxResults;

    public LlmDataQuery(String jpql,
                        @Nullable List<String> resultProperties,
                        @Nullable List<LlmQueryParameter> parameters,
                        @Nullable String explanation,
                        @Nullable List<String> warnings,
                        @Nullable Integer maxResults) {
        checkNotNullArgument(jpql, "jpql is null");

        this.jpql = jpql;
        this.resultProperties = resultProperties == null ? List.of() : List.copyOf(resultProperties);
        this.parameters = parameters == null ? List.of() : List.copyOf(parameters);
        this.explanation = explanation;
        this.warnings = warnings == null ? List.of() : List.copyOf(warnings);
        this.maxResults = maxResults != null && maxResults > 0 ? maxResults : null;
    }

    /**
     * @return the JPQL select query; every select expression is aliased via {@code AS}
     */
    public String getJpql() {
        return jpql;
    }

    /**
     * Returns the select aliases in select-clause order. They are the keys of the produced band rows, so
     * a report template refers to band fields by these names.
     *
     * @return the select aliases in select-clause order
     */
    public List<String> getResultProperties() {
        return resultProperties;
    }

    /**
     * @return named parameters the query references, with their types but without values
     */
    public List<LlmQueryParameter> getParameters() {
        return parameters;
    }

    /**
     * @return human-readable description of what the query does, or {@code null} if none was produced
     */
    @Nullable
    public String getExplanation() {
        return explanation;
    }

    /**
     * @return assumptions and approximations reported when the query was generated
     */
    public List<String> getWarnings() {
        return warnings;
    }

    /**
     * @return row limit proposed when the query was generated, or {@code null} if none was proposed — which is
     * also what a stored zero or a negative number reads as, since a limit is a number of rows
     */
    @Nullable
    public Integer getMaxResults() {
        return maxResults;
    }
}
