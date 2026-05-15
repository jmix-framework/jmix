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

package io.jmix.aitools.dataload.executor;

import java.util.List;

public class SpringAiGeneratedJpqlPayload {

    protected String jpql;
    protected String rootEntityName;
    protected List<SpringAiGeneratedJpqlParameterPayload> parameters;
    protected List<String> usedEntities;
    protected List<String> usedPropertyPaths;
    protected String explanation;
    protected List<String> warnings;
    protected Integer maxResults;
    protected Integer firstResult;

    public String getJpql() {
        return jpql;
    }

    public void setJpql(String jpql) {
        this.jpql = jpql;
    }

    public String getRootEntityName() {
        return rootEntityName;
    }

    public void setRootEntityName(String rootEntityName) {
        this.rootEntityName = rootEntityName;
    }

    public List<SpringAiGeneratedJpqlParameterPayload> getParameters() {
        return parameters;
    }

    public void setParameters(List<SpringAiGeneratedJpqlParameterPayload> parameters) {
        this.parameters = parameters;
    }

    public List<String> getUsedEntities() {
        return usedEntities;
    }

    public void setUsedEntities(List<String> usedEntities) {
        this.usedEntities = usedEntities;
    }

    public List<String> getUsedPropertyPaths() {
        return usedPropertyPaths;
    }

    public void setUsedPropertyPaths(List<String> usedPropertyPaths) {
        this.usedPropertyPaths = usedPropertyPaths;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }

    public Integer getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(Integer maxResults) {
        this.maxResults = maxResults;
    }

    public Integer getFirstResult() {
        return firstResult;
    }

    public void setFirstResult(Integer firstResult) {
        this.firstResult = firstResult;
    }
}
