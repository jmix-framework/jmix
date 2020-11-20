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

import com.google.common.base.Strings;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * Condition for filtering by entity property. Property condition value may be taken from query parameters (the {@link
 * #parameterName} attribute) or taken from the {@link #parameterValue} property.
 */
public class PropertyCondition implements Condition {

    private String property;

    private String operation;

    private String parameterName;

    private Object parameterValue;

    public PropertyCondition() {
    }

    public static PropertyCondition createWithParameterName(String property, String operation, String parameterName) {
        PropertyCondition pc = new PropertyCondition();
        pc.property = property;
        pc.operation = operation;
        pc.parameterName = parameterName;
        return pc;
    }

    public static PropertyCondition createWithValue(String property, String operation, Object parameterValue) {
        PropertyCondition pc = new PropertyCondition();
        pc.property = property;
        pc.operation = operation;
        pc.parameterValue = parameterValue;
        pc.parameterName = PropertyConditionUtils.generateParameterName(property);
        return pc;
    }

    public static PropertyCondition create(String property, String operation, Object value) {
        return createWithValue(property, operation, value);
    }

    public static PropertyCondition equal(String property, Object value) {
        return createWithValue(property, Operation.EQUAL, value);
    }

    public static PropertyCondition notEqual(String property, Object value) {
        return createWithValue(property, Operation.NOT_EQUAL, value);
    }

    public static PropertyCondition greater(String property, Object value) {
        return createWithValue(property, Operation.GREATER, value);
    }

    public static PropertyCondition greaterOrEqual(String property, Object value) {
        return createWithValue(property, Operation.GREATER_OR_EQUAL, value);
    }

    public static PropertyCondition less(String property, Object value) {
        return createWithValue(property, Operation.LESS, value);
    }

    public static PropertyCondition lessOrEqual(String property, Object value) {
        return createWithValue(property, Operation.LESS_OR_EQUAL, value);
    }

    public static PropertyCondition contains(String property, Object value) {
        return createWithValue(property, Operation.CONTAINS, value);
    }

    public static PropertyCondition startsWith(String property, Object value) {
        return createWithValue(property, Operation.STARTS_WITH, value);
    }

    public static PropertyCondition endsWith(String property, Object value) {
        return createWithValue(property, Operation.ENDS_WITH, value);
    }

//    public static PropertyCondition inList(String property, Object value) {
//        return createWithParameterValue(property, Operation.IN_LIST, value);
//    }
//
//    public static PropertyCondition notInList(String property, Object value) {
//        return createWithParameterValue(property, Operation.NOT_IN_LIST, value);
//    }

    public static PropertyCondition isNull(String property, Object value) {
        return createWithValue(property, Operation.IS_NULL, value);
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getParameterName() {
        return parameterName;
    }

    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    @Nullable
    public Object getParameterValue() {
        return parameterValue;
    }

    public void setParameterValue(@Nullable Object parameterValue) {
        this.parameterValue = parameterValue;
    }

    @Override
    public Collection<String> getParameters() {
        return Collections.singletonList(parameterName);
    }

    @Nullable
    @Override
    public Condition actualize(Set<String> actualParameters) {
        if (actualParameters.containsAll(getParameters())) {
            return this;
        }

        if (parameterValue != null) {
            if (parameterValue instanceof String) {
                if (!Strings.isNullOrEmpty((String) parameterValue)) {
                    return this;
                }
            } else {
                return this;
            }
        }
        return null;
    }

    @Override
    public PropertyCondition copy() {
        PropertyCondition pc = new PropertyCondition();
        pc.setProperty(this.property);
        pc.setOperation(this.operation);
        pc.setParameterName(this.parameterName);
        pc.setParameterValue(this.parameterValue);
        return pc;
    }

    public static class Operation {
        public static final String EQUAL = "equal";
        public static final String NOT_EQUAL = "not_equal";
        public static final String GREATER = "greater";
        public static final String GREATER_OR_EQUAL = "greater_or_equal";
        public static final String LESS = "less";
        public static final String LESS_OR_EQUAL = "less_or_equal";
        public static final String CONTAINS = "contains";
        public static final String NOT_CONTAINS = "not_contains";
        public static final String IS_NULL = "is_null";
        public static final String IS_NOT_NULL = "is_not_null";
        public static final String STARTS_WITH = "starts_with";
        public static final String ENDS_WITH = "ends_with";
//        public static final String IN_LIST = "in_list";
//        public static final String NOT_IN_LIST = "not_in_list";
    }
}
