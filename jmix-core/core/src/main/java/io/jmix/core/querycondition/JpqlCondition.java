/*
 * Copyright 2020 Haulmont.
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

package io.jmix.core.querycondition;

import org.apache.commons.collections4.CollectionUtils;

import jakarta.annotation.Nullable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Condition that represents JPQL query with "where" and optional "join" sections.
 */
public class JpqlCondition implements Condition {

    public static final Pattern PARAMETER_PATTERN = Pattern.compile(":([\\w.$]+)");

    protected String where;
    protected String join;
    protected Map<String, Object> parameterValuesMap = new HashMap<>();

    public JpqlCondition() {
    }

    public static JpqlCondition create(String where, @Nullable String join) {
        JpqlCondition condition = new JpqlCondition();
        condition.setWhere(where);
        condition.setJoin(join);
        return condition;
    }

    public static JpqlCondition createWithParameters(String where,
                                                     @Nullable String join,
                                                     Map<String, Object> parameterValuesMap) {
        JpqlCondition condition = new JpqlCondition();
        condition.setWhere(where);
        condition.setJoin(join);
        condition.setParameterValuesMap(parameterValuesMap);
        return condition;
    }

    public String getWhere() {
        return where;
    }

    public void setWhere(String where) {
        if (!Objects.equals(this.where, where)) {
            if (this.where != null) {
                removeParameters(this.where);
            }

            parseParameters(where);
            this.where = where;
        }
    }

    @Nullable
    public String getJoin() {
        return join;
    }

    public void setJoin(@Nullable String join) {
        if (!Objects.equals(this.join, join)) {
            if (this.join != null) {
                removeParameters(this.join);
            }

            if (join != null) {
                parseParameters(join);
            }

            this.join = join;
        }
    }

    public Map<String, Object> getParameterValuesMap() {
        return parameterValuesMap;
    }

    public void setParameterValuesMap(Map<String, Object> parameterValuesMap) {
        this.parameterValuesMap.putAll(parameterValuesMap);
    }

    @Override
    public Collection<String> getParameters() {
        return new ArrayList<>(parameterValuesMap.keySet());
    }

    @Nullable
    @Override
    public Condition actualize(Set<String> actualParameters) {
        for (Map.Entry<String, Object> parameter : parameterValuesMap.entrySet()) {
            if (!actualParameters.contains(parameter.getKey())
                    && (parameter.getValue() == null
                    || (parameter.getValue() instanceof Collection
                    && CollectionUtils.isEmpty((Collection<?>) parameter.getValue())))) {
                return null;
            }
        }

        return this;
    }

    @Override
    public Condition copy() {
        JpqlCondition copy = new JpqlCondition();
        copy.setWhere(this.where);
        copy.setJoin(this.join);
        copy.setParameterValuesMap(this.parameterValuesMap);
        return copy;
    }

    @Override
    public Set<String> getExcludedParameters(Set<String> actualParameters) {
        Set<String> excludedParameters = new TreeSet<>();
        for (Map.Entry<String, Object> parameter : parameterValuesMap.entrySet()) {
            if (!actualParameters.contains(parameter.getKey())
                    && (parameter.getValue() == null
                    || (parameter.getValue() instanceof Collection
                    && CollectionUtils.isEmpty((Collection<?>) parameter.getValue())))) {
                excludedParameters.addAll(this.getParameters());
                return excludedParameters;
            }
        }
        return excludedParameters;
    }

    protected void parseParameters(String value) {
        Matcher matcher = PARAMETER_PATTERN.matcher(value);
        while (matcher.find()) {
            String parameterName = matcher.group(1);
            parameterValuesMap.put(parameterName, null);
        }
    }

    protected void removeParameters(String value) {
        Matcher matcher = PARAMETER_PATTERN.matcher(value);
        while (matcher.find()) {
            String parameterName = matcher.group(1);
            parameterValuesMap.remove(parameterName);
        }
    }
}
