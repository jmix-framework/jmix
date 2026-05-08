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

package io.jmix.texttodata.generation;

import java.util.List;

public class GeneratedJpqlResult {

    protected String jpql;
    protected String rootEntityName;
    protected List<GeneratedJpqlParameter> parameters;
    protected List<String> usedEntities;
    protected List<String> usedPropertyPaths;
    protected String explanation;
    protected List<String> warnings;

    public GeneratedJpqlResult(String jpql,
                               String rootEntityName,
                               List<GeneratedJpqlParameter> parameters,
                               List<String> usedEntities,
                               List<String> usedPropertyPaths,
                               String explanation,
                               List<String> warnings) {
        this.jpql = jpql;
        this.rootEntityName = rootEntityName;
        this.parameters = parameters;
        this.usedEntities = usedEntities;
        this.usedPropertyPaths = usedPropertyPaths;
        this.explanation = explanation;
        this.warnings = warnings;
    }

    public String getJpql() {
        return jpql;
    }

    public String getRootEntityName() {
        return rootEntityName;
    }

    public List<GeneratedJpqlParameter> getParameters() {
        return parameters;
    }

    public List<String> getUsedEntities() {
        return usedEntities;
    }

    public List<String> getUsedPropertyPaths() {
        return usedPropertyPaths;
    }

    public String getExplanation() {
        return explanation;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    @Override
    public String toString() {
        return "GeneratedJpqlResult{" +
                "jpql='" + jpql + '\'' +
                ", rootEntityName='" + rootEntityName + '\'' +
                ", parameters=" + parameters +
                ", usedEntities=" + usedEntities +
                ", usedPropertyPaths=" + usedPropertyPaths +
                ", explanation='" + explanation + '\'' +
                ", warnings=" + warnings +
                '}';
    }
}
