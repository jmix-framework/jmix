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

package io.jmix.core.querycondition;

import org.springframework.lang.Nullable;

import java.util.*;

/**
 * Logical condition (AND, OR) which contains other conditions.
 * <p>
 * {@link #getParameters()} returns parameters of nested conditions.
 * <p>
 * Use {@link #and(Condition...)} and {@link #or(Condition...)} static methods to create logical conditions.
 */
public class LogicalCondition implements Condition {

    public enum Type {
        AND, OR
    }

    private List<Condition> conditions = new ArrayList<>();

    private Type type;

    /**
     * Creates empty AND condition.
     * Use {@link #add(Condition)} method to add nested conditions.
     */
    public static LogicalCondition and() {
        return new LogicalCondition(Type.AND);
    }

    /**
     * Creates AND condition with the given nested conditions.
     */
    public static LogicalCondition and(Condition... conditions) {
        LogicalCondition andCondition = new LogicalCondition(Type.AND);
        for (Condition condition : conditions) {
            andCondition.add(condition);
        }
        return andCondition;
    }

    /**
     * Creates empty OR condition.
     * Use {@link #add(Condition)} method to add nested conditions.
     */
    public static LogicalCondition or() {
        return new LogicalCondition(Type.OR);
    }

    /**
     * Creates OR condition with the given nested conditions.
     */
    public static LogicalCondition or(Condition... conditions) {
        LogicalCondition orCondition = new LogicalCondition(Type.OR);
        for (Condition condition : conditions) {
            orCondition.add(condition);
        }
        return orCondition;
    }

    public LogicalCondition(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public List<Condition> getConditions() {
        return conditions;
    }

    public void setConditions(List<Condition> conditions) {
        this.conditions = conditions;
    }

    public LogicalCondition add(Condition condition) {
        conditions.add(condition);
        return this;
    }

    @Override
    public Collection<String> getParameters() {
        Set<String> parameters = new HashSet<>();
        for (Condition nestedCondition : conditions) {
            parameters.addAll(nestedCondition.getParameters());
        }
        return parameters;
    }

    @Nullable
    @Override
    public Condition actualize(Set<String> actualParameters, boolean defaultSkipNullOrEmpty ) {
        LogicalCondition copy = new LogicalCondition(type);
        for (Condition condition : conditions) {
            Condition actualized = condition.actualize(actualParameters, defaultSkipNullOrEmpty);
            if (actualized != null) {
                copy.add(actualized);
            }
        }
        if (copy.getConditions().isEmpty()) {
            return null;
        } else if (copy.getConditions().size() == 1) {
            return copy.getConditions().get(0);
        } else {
            return copy;
        }
    }

    @Override
    public Condition copy() {
        LogicalCondition copy = new LogicalCondition(type);
        copy.conditions = new ArrayList<>(conditions.size());
        for (Condition nestedCondition : conditions) {
            copy.add(nestedCondition.copy());
        }
        return copy;
    }

    @Override
    public Set<String> getExcludedParameters(Set<String> actualParameters) {
        Set<String> excludedParameters = new TreeSet<>();
        for (Condition condition : conditions) {
            excludedParameters.addAll(condition.getExcludedParameters(actualParameters));
        }
        return excludedParameters;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("(");
        for (Condition condition : conditions) {
            result.append(condition);
            if (conditions.indexOf(condition) != conditions.size() - 1) {
                result.append(" ").append(type).append(" ");
            }
        }
        result.append(")");
        return result.toString();
    }
}
