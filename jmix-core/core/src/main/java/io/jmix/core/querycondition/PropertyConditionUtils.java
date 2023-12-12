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
import org.apache.commons.lang3.RandomStringUtils;

import java.util.function.Function;

public class PropertyConditionUtils {

    /**
     * @param propertyCondition property condition
     * @return true if property condition operation is unary (doesn't require parameter value), e.g "is set"
     */
    public static boolean isUnaryOperation(PropertyCondition propertyCondition) {
        String operation = propertyCondition.getOperation();
        return PropertyCondition.Operation.IS_SET.equals(operation)
                || PropertyCondition.Operation.IS_EMPTY.equals(operation);
    }

    /**
     * @param propertyCondition property condition
     * @return true if property condition operation is collection (doesn't require parameter value), e.g "in list"
     */
    public static boolean isCollectionOperation(PropertyCondition propertyCondition) {
        String operation = propertyCondition.getOperation();
        return PropertyCondition.Operation.IN_LIST.equals(operation)
                || PropertyCondition.Operation.NOT_IN_LIST.equals(operation);
    }

    /**
     * @param propertyCondition property condition
     * @return true if property condition operation is "in interval"
     */
    public static boolean isInIntervalOperation(PropertyCondition propertyCondition) {
        String operation = propertyCondition.getOperation();
        return PropertyCondition.Operation.IN_INTERVAL.equals(operation);
    }

    /**
     * @param property an entity property
     * @return a parameter name
     */
    public static String generateParameterName(String property) {
        return (Strings.nullToEmpty(property) + RandomStringUtils.randomAlphabetic(8))
                .replace(".", "_")
                .replace("+", "");
    }

    /**
     * @param condition property condition
     * @return a JPQL operation
     */
    public static String getJpqlOperation(PropertyCondition condition) {
        switch (condition.getOperation()) {
            case PropertyCondition.Operation.EQUAL:
                return "=";
            case PropertyCondition.Operation.NOT_EQUAL:
                return "<>";
            case PropertyCondition.Operation.GREATER:
                return ">";
            case PropertyCondition.Operation.GREATER_OR_EQUAL:
                return ">=";
            case PropertyCondition.Operation.LESS:
                return "<";
            case PropertyCondition.Operation.LESS_OR_EQUAL:
                return "<=";
            case PropertyCondition.Operation.CONTAINS:
            case PropertyCondition.Operation.STARTS_WITH:
            case PropertyCondition.Operation.ENDS_WITH:
                return "like";
            case PropertyCondition.Operation.NOT_CONTAINS:
                return "not like";
            case PropertyCondition.Operation.IN_LIST:
                return "in";
            case PropertyCondition.Operation.NOT_IN_LIST:
                return "not in";
            case PropertyCondition.Operation.IS_SET:
                return Boolean.TRUE.equals(condition.getParameterValue()) ? "is not null" : "is null";
            case PropertyCondition.Operation.IN_INTERVAL:
                return getInIntervalJpqlOperation(condition);
            case PropertyCondition.Operation.IS_EMPTY:
                return Boolean.TRUE.equals(condition.getParameterValue()) ? "is empty" : "is not empty";
        }
        throw new RuntimeException("Unknown PropertyCondition operation: " + condition.getOperation());
    }

    protected static String getInIntervalJpqlOperation(PropertyCondition condition) {
        Object parameterValue = condition.getParameterValue();
        if (parameterValue == null) {
            throw new RuntimeException("Cannot build operation condition for date interval " +
                    "because parameter value is null");
        }

        //noinspection unchecked
        return ((Function<String, String>) parameterValue).apply(condition.getProperty());
    }
}
