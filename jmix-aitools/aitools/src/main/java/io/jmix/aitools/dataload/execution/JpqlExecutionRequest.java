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

public class JpqlExecutionRequest {

    protected String userText;

    protected String jpql;
    protected List<JpqlExecutionParameter> parameters = List.of();
    protected List<String> resultProperties = List.of();

    protected Integer maxResults;
    protected Integer firstResult;

    public JpqlExecutionRequest() {
    }

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

    public String getUserText() {
        return userText;
    }

    public void setUserText(String userText) {
        this.userText = userText;
    }

    public String getJpql() {
        return jpql;
    }

    public void setJpql(String jpql) {
        this.jpql = jpql;
    }

    public List<JpqlExecutionParameter> getParameters() {
        return parameters;
    }

    public void setParameters(@Nullable List<JpqlExecutionParameter> parameters) {
        this.parameters = parameters == null ? List.of() : List.copyOf(parameters);
    }

    public List<String> getResultProperties() {
        return resultProperties;
    }

    public void setResultProperties(@Nullable List<String> resultProperties) {
        this.resultProperties = resultProperties == null ? List.of() : List.copyOf(resultProperties);
    }

    @Nullable
    public Integer getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(@Nullable Integer maxResults) {
        this.maxResults = maxResults;
    }

    @Nullable
    public Integer getFirstResult() {
        return firstResult;
    }

    public void setFirstResult(@Nullable Integer firstResult) {
        this.firstResult = firstResult;
    }
}
