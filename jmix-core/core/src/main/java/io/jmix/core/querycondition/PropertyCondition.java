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
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/**
 * Condition for filtering by entity property.
 * <p>
 * Use one of the static methods like {@link #equal(String, Object)}, {@link #greater(String, Object)} to create
 * property conditions.
 */
public class PropertyCondition extends SkippableCondition<PropertyCondition> {

    private String property;

    private String operation;

    private String parameterName;

    private Object parameterValue;

    public PropertyCondition() {
    }

    /**
     * Creates property condition with the specified parameter name. The parameter value must be provided by
     * calling {@link #setParameterValue(Object)} method.
     *
     * @param property      entity attribute name
     * @param operation     comparison operation, see {@link Operation} constants
     * @param parameterName parameter name
     */
    public static PropertyCondition createWithParameterName(String property, String operation, String parameterName) {
        PropertyCondition pc = new PropertyCondition();
        pc.property = property;
        pc.operation = operation;
        pc.parameterName = parameterName;
        return pc;
    }

    /**
     * Creates a condition to compare the property with the provided value. A parameter name is generated based
     * on the property name.
     *
     * @param property       entity attribute name
     * @param operation      comparison operation, see {@link Operation} constants.
     * @param parameterValue value to compare with
     */
    public static PropertyCondition createWithValue(String property, String operation, Object parameterValue) {
        PropertyCondition pc = new PropertyCondition();
        pc.property = property;
        pc.operation = operation;
        pc.parameterValue = parameterValue;
        pc.parameterName = PropertyConditionUtils.generateParameterName(property);
        return pc;
    }

    /**
     * Creates a condition to compare the property with the given value.
     *
     * @param property  entity attribute name
     * @param operation comparison operation, see constants in {@link Operation}
     * @param value     value to compare with
     */
    public static PropertyCondition create(String property, String operation, Object value) {
        return createWithValue(property, operation, value);
    }

    /**
     * Creates "=" condition.
     */
    public static PropertyCondition equal(String property, Object value) {
        return createWithValue(property, Operation.EQUAL, value);
    }

    /**
     * Creates "!=" condition.
     */
    public static PropertyCondition notEqual(String property, Object value) {
        return createWithValue(property, Operation.NOT_EQUAL, value);
    }

    /**
     * Creates "&gt;" condition.
     */
    public static PropertyCondition greater(String property, Object value) {
        return createWithValue(property, Operation.GREATER, value);
    }

    /**
     * Creates "&gt;=" condition.
     */
    public static PropertyCondition greaterOrEqual(String property, Object value) {
        return createWithValue(property, Operation.GREATER_OR_EQUAL, value);
    }

    /**
     * Creates "&lt;" condition.
     */
    public static PropertyCondition less(String property, Object value) {
        return createWithValue(property, Operation.LESS, value);
    }

    /**
     * Creates "&lt;=" condition.
     */
    public static PropertyCondition lessOrEqual(String property, Object value) {
        return createWithValue(property, Operation.LESS_OR_EQUAL, value);
    }

    /**
     * Creates a condition that is translated to "like %value%".
     */
    public static PropertyCondition contains(String property, Object value) {
        return createWithValue(property, Operation.CONTAINS, value);
    }

    /**
     * Creates a condition that is translated to "like value%".
     */
    public static PropertyCondition startsWith(String property, Object value) {
        return createWithValue(property, Operation.STARTS_WITH, value);
    }

    /**
     * Creates a condition that is translated to "like %value".
     */
    public static PropertyCondition endsWith(String property, Object value) {
        return createWithValue(property, Operation.ENDS_WITH, value);
    }

    /**
     * Creates a condition that is translated to "is null" or "is not null"
     * depending on the parameter value.
     */
    public static PropertyCondition isSet(String property, Object value) {
        return createWithValue(property, Operation.IS_SET, value);
    }

    /**
     * Creates a condition that is translated to "in".
     */
    public static PropertyCondition inList(String property, Object value) {
        return createWithValue(property, Operation.IN_LIST, value);
    }

    /**
     * Creates a condition that is translated to "not in".
     */
    public static PropertyCondition notInList(String property, Object value) {
        return createWithValue(property, Operation.NOT_IN_LIST, value);
    }

    /**
     * Creates a condition that is translated to "is empty" or "is not empty"
     * depending on the parameter value.
     */
    public static PropertyCondition isCollectionEmpty(String property, Object value) {
        return createWithValue(property, Operation.IS_COLLECTION_EMPTY, value);
    }

    /**
     * Creates a condition that is translated to "member of".
     */
    public static PropertyCondition memberOfCollection(String property, Object value) {
        return createWithValue(property, Operation.MEMBER_OF_COLLECTION, value);
    }

    /**
     * Creates a condition that is translated to "not member of".
     */
    public static PropertyCondition notMemberOfCollection(String property, Object value) {
        return createWithValue(property, Operation.NOT_MEMBER_OF_COLLECTION, value);
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
    public Condition actualize(Set<String> actualParameters, boolean defaultSkipNullOrEmpty) {
        applyDefaultSkipNullOrEmpty(defaultSkipNullOrEmpty);
        if (!skipNullOrEmpty || actualParameters.containsAll(getParameters())) {
            return this;
        }

        if (parameterValue != null) {
            if (parameterValue instanceof String) {
                if (!Strings.isNullOrEmpty((String) parameterValue)) {
                    return this;
                }
            } else if (parameterValue instanceof Collection) {
                if (CollectionUtils.isNotEmpty((Collection<?>) parameterValue)) {
                    return this;
                }
            } else {
                return this;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "[" + property + " " + operation + " " + (parameterName != null ? (":" + parameterName) : parameterValue) + "]";
    }

    @Override
    public PropertyCondition copy() {
        PropertyCondition pc = new PropertyCondition();
        pc.setProperty(this.property);
        pc.setOperation(this.operation);
        pc.setParameterName(this.parameterName);
        pc.setParameterValue(this.parameterValue);
        pc.setSkipNullOrEmpty(this.skipNullOrEmpty);
        return pc;
    }

    @Override
    public Set<String> getExcludedParameters(Set<String> actualParameters) {
        Set<String> excludedParameters = new TreeSet<>();
        if (!skipNullOrEmpty || actualParameters.containsAll(getParameters())) {
            return excludedParameters;
        }

        if (parameterValue != null) {
            if (parameterValue instanceof String) {
                if (!Strings.isNullOrEmpty((String) parameterValue)) {
                    return excludedParameters;
                }
            } else if (parameterValue instanceof Collection) {
                if (CollectionUtils.isNotEmpty((Collection<?>) parameterValue)) {
                    return excludedParameters;
                }
            } else {
                return excludedParameters;
            }
        }
        excludedParameters.add(parameterName);
        return excludedParameters;
    }

    /**
     * String constants defining comparison operations.
     */
    public static class Operation {
        public static final String EQUAL = "equal";
        public static final String NOT_EQUAL = "not_equal";
        public static final String GREATER = "greater";
        public static final String GREATER_OR_EQUAL = "greater_or_equal";
        public static final String LESS = "less";
        public static final String LESS_OR_EQUAL = "less_or_equal";
        public static final String CONTAINS = "contains";
        public static final String NOT_CONTAINS = "not_contains";
        public static final String IS_SET = "is_set";
        public static final String STARTS_WITH = "starts_with";
        public static final String ENDS_WITH = "ends_with";
        public static final String IN_LIST = "in_list";
        public static final String NOT_IN_LIST = "not_in_list";
        public static final String IN_INTERVAL = "in_interval";
        public static final String IS_COLLECTION_EMPTY = "is_collection_empty";
        public static final String MEMBER_OF_COLLECTION = "member_of_collection";
        public static final String NOT_MEMBER_OF_COLLECTION = "not_member_of_collection";
    }
}
