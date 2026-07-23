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

import io.jmix.aitools.dataload.json.EmptyObjectTolerantListDeserializer;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.annotation.JsonDeserialize;

import java.util.List;

/**
 * Input for {@link JpqlExecutionService}: a JPQL query together with everything needed to run it.
 */
public class JpqlExecutionRequest {

    protected String userText;

    protected String jpql;
    @JsonDeserialize(using = EmptyObjectTolerantListDeserializer.class)
    protected List<JpqlExecutionParameter> parameters = List.of();
    @JsonDeserialize(using = EmptyObjectTolerantListDeserializer.class)
    protected List<String> resultProperties = List.of();

    protected Integer maxResults;
    protected Integer firstResult;

    public JpqlExecutionRequest() {
    }

    /**
     * @param userText         original user request in natural language
     * @param jpql             JPQL query to execute
     * @param parameters       query parameters
     * @param resultProperties properties to select, or {@code null} for none
     * @param maxResults       maximum number of rows, or {@code null} for the configured default
     * @param firstResult      row offset, or {@code null} for no offset
     */
    public JpqlExecutionRequest(String userText,
                                String jpql,
                                List<JpqlExecutionParameter> parameters,
                                @Nullable List<String> resultProperties,
                                @Nullable Integer maxResults,
                                @Nullable Integer firstResult) {
        this.userText = userText;
        this.jpql = jpql;
        setParameters(parameters);
        setResultProperties(resultProperties);
        this.maxResults = maxResults;
        this.firstResult = firstResult;
    }

    /**
     * Returns the original user request in natural language.
     *
     * @return user request text
     */
    public String getUserText() {
        return userText;
    }

    public void setUserText(String userText) {
        this.userText = userText;
    }

    /**
     * Returns the JPQL query to execute.
     *
     * @return JPQL query
     */
    public String getJpql() {
        return jpql;
    }

    public void setJpql(String jpql) {
        this.jpql = jpql;
    }

    /**
     * Returns the query parameters.
     *
     * @return query parameters, never {@code null}
     */
    public List<JpqlExecutionParameter> getParameters() {
        return parameters;
    }

    public void setParameters(@Nullable List<JpqlExecutionParameter> parameters) {
        this.parameters = parameters == null ? List.of() : List.copyOf(parameters);
    }

    /**
     * Returns the properties to select for each result row.
     *
     * @return result properties, never {@code null}
     */
    public List<String> getResultProperties() {
        return resultProperties;
    }

    public void setResultProperties(@Nullable List<String> resultProperties) {
        this.resultProperties = resultProperties == null ? List.of() : List.copyOf(resultProperties);
    }

    /**
     * Returns the requested maximum number of rows.
     *
     * @return maximum number of rows, or {@code null} for the configured default
     */
    @Nullable
    public Integer getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(@Nullable Integer maxResults) {
        this.maxResults = maxResults;
    }

    /**
     * Returns the requested row offset.
     *
     * @return row offset, or {@code null} for no offset
     */
    @Nullable
    public Integer getFirstResult() {
        return firstResult;
    }

    public void setFirstResult(@Nullable Integer firstResult) {
        this.firstResult = firstResult;
    }
}
