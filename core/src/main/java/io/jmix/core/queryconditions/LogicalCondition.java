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

package io.jmix.core.queryconditions;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Logical condition (AND, OR) which contains other conditions.
 * <p>
 * {@link #getParameters()} returns parameters of nested conditions.
 */
public class LogicalCondition implements Condition {

    public enum Type {
        AND, OR
    }

    private List<Condition> conditions = new ArrayList<>();

    private Type type;

    public static LogicalCondition and() {
        return new LogicalCondition(Type.AND);
    }

    public static LogicalCondition or() {
        return new LogicalCondition(Type.OR);
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
    public Condition actualize(Set<String> actualParameters) {
        LogicalCondition copy = new LogicalCondition(type);
        for (Condition condition : conditions) {
            Condition actualized = condition.actualize(actualParameters);
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
    public String toString() {
        return type.toString() + " " + conditions.toString();
    }
}
