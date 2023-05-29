/*
 * Copyright 2019 Haulmont.
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
package com.haulmont.cuba.core.global.filter;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Clause extends Condition {
    protected String content;

    protected String join;

    protected Set<ParameterInfo> inputParameters;

    protected Set<ParameterInfo> queryParameters;

    protected Op operator;

    protected String type;

    public Clause(String name, String content, @Nullable String join, @Nullable String operator, @Nullable String type) {
        super(name);

        this.content = content;
        this.join = join;
        this.queryParameters = ParametersHelper.parseQuery(content);
        if (operator != null) {
            this.operator = Op.valueOf(operator);
        }
        this.type = type;
    }

    public Clause(String fieldName, Op operator, String param) {
        super(fieldName);

        this.content = fieldName + operator.forJpql() + param;
        this.queryParameters = ParametersHelper.parseQuery(content);
        this.operator = operator;
    }

    @Override
    public List<Condition> getConditions() {
        return Collections.emptyList();
    }

    @Override
    public void setConditions(List<Condition> conditions) {
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public Set<ParameterInfo> getCompiledParameters() {
        Set<ParameterInfo> set = new HashSet<>(queryParameters);
        if (inputParameters != null) {
            set.addAll(
                    inputParameters.stream()
                            .filter(e -> !queryParameters.contains(e))
                            .collect(Collectors.toSet())
            );
        }
        return set;
    }

    @Override
    public Set<ParameterInfo> getQueryParameters() {
        return queryParameters;
    }

    @Override
    public Set<ParameterInfo> getInputParameters() {
        return inputParameters == null ? Collections.emptySet() : inputParameters;
    }

    public void setInputParameters(Set<ParameterInfo> inputParameters) {
        this.inputParameters = inputParameters;
        for (ParameterInfo parameterInfo : queryParameters) {
            if (inputParameters.contains(parameterInfo)) {
                inputParameters.stream()
                        .filter(e -> e.equals(parameterInfo))
                        .findFirst()
                        .ifPresent(e -> {
                            parameterInfo.setJavaClass(e.getJavaClass());
                            parameterInfo.setConditionName(e.getConditionName());
                            parameterInfo.setValue(e.getValue());
                        });
            }
        }
    }

    @Override
    public Set<String> getJoins() {
        return join == null ? Collections.EMPTY_SET : Collections.singleton(join);
    }

    public Op getOperator() {
        return operator;
    }

    @Nullable
    public ConditionType getType() {
        try {
            return type == null ? null : ConditionType.valueOf(type);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
