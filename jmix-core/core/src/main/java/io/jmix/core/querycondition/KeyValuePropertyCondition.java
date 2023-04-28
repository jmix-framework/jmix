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

/**
 * Condition for filtering by entity property.
 * <p>
 * Use one of the static methods like {@link #equal(String, Object)}, {@link #greater(String, Object)} to create
 * property conditions.
 */
public class KeyValuePropertyCondition extends PropertyCondition {

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
        pc.setProperty(property);
        pc.setOperation(operation);
        pc.setParameterName(parameterName);
        pc.keyValueProperty();
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
        pc.setProperty(property);
        pc.setOperation(operation);
        pc.setParameterValue(parameterValue);
        pc.setParameterName(PropertyConditionUtils.generateParameterName(property));
        pc.keyValueProperty();
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
        return createWithValue(property, operation, value).keyValueProperty();
    }

    /**
     * Creates "=" condition.
     */
    public static PropertyCondition equal(String property, Object value) {
        return createWithValue(property, Operation.EQUAL, value).keyValueProperty();
    }

    /**
     * Creates "!=" condition.
     */
    public static PropertyCondition notEqual(String property, Object value) {
        return createWithValue(property, Operation.NOT_EQUAL, value).keyValueProperty();
    }

    /**
     * Creates "&gt;" condition.
     */
    public static PropertyCondition greater(String property, Object value) {
        return createWithValue(property, Operation.GREATER, value).keyValueProperty();
    }

    /**
     * Creates "&gt;=" condition.
     */
    public static PropertyCondition greaterOrEqual(String property, Object value) {
        return createWithValue(property, Operation.GREATER_OR_EQUAL, value).keyValueProperty();
    }

    /**
     * Creates "&lt;" condition.
     */
    public static PropertyCondition less(String property, Object value) {
        return createWithValue(property, Operation.LESS, value).keyValueProperty();
    }

    /**
     * Creates "&lt;=" condition.
     */
    public static PropertyCondition lessOrEqual(String property, Object value) {
        return createWithValue(property, Operation.LESS_OR_EQUAL, value).keyValueProperty();
    }

    /**
     * Creates a condition that is translated to "like %value%".
     */
    public static PropertyCondition contains(String property, Object value) {
        return createWithValue(property, Operation.CONTAINS, value).keyValueProperty();
    }

    /**
     * Creates a condition that is translated to "like value%".
     */
    public static PropertyCondition startsWith(String property, Object value) {
        return createWithValue(property, Operation.STARTS_WITH, value).keyValueProperty();
    }

    /**
     * Creates a condition that is translated to "like %value".
     */
    public static PropertyCondition endsWith(String property, Object value) {
        return createWithValue(property, Operation.ENDS_WITH, value).keyValueProperty();
    }

    /**
     * Creates a condition that is translated to "is null" or "is not null"
     * depending on the parameter value.
     */
    public static PropertyCondition isSet(String property, Object value) {
        return createWithValue(property, Operation.IS_SET, value).keyValueProperty();
    }

    /**
     * Creates a condition that is translated to "in".
     */
    public static PropertyCondition inList(String property, Object value) {
        return createWithValue(property, Operation.IN_LIST, value).keyValueProperty();
    }

    /**
     * Creates a condition that is translated to "not in".
     */
    public static PropertyCondition notInList(String property, Object value) {
        return createWithValue(property, Operation.NOT_IN_LIST, value).keyValueProperty();
    }

}
