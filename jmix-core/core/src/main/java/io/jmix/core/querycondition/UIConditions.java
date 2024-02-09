/*
 * Copyright 2024 Haulmont.
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


import io.jmix.core.annotation.Experimental;
import org.springframework.lang.Nullable;

import java.util.Map;

/**
 * Creates "skipping conditions" - conditions which will be skipped in case of absent or null parameter.<p>
 * Such behaviour required by visual filter components.
 */
@Experimental
public class UIConditions {
    /**
     * Creates a new skipping {@link JpqlCondition}.
     */
    public static JpqlCondition jpqlCondition() {
        return new JpqlCondition().skipNullOrEmpty();
    }

    /**
     * Creates skipping {@link JpqlCondition} with specified parameters.
     */
    public static JpqlCondition jpqlCondition(String where, @Nullable String join) {
        return JpqlCondition.create(where, join).skipNullOrEmpty();
    }

    /**
     * Creates skipping {@link JpqlCondition} with specified parameters.
     */
    public static JpqlCondition jpqlCondition(String where, @Nullable String join, Map<String, Object> parameterValuesMap) {
        return JpqlCondition.createWithParameters(where, join, parameterValuesMap).skipNullOrEmpty();
    }
    /**
     * Creates a new skipping {@link PropertyCondition}.
     */
    public static PropertyCondition propertyCondition() {
        return new PropertyCondition().skipNullOrEmpty();
    }

    /**
     * Creates skipping {@link PropertyCondition} with specified {@code parameterName}.
     */
    public static PropertyCondition propertyConditionWithParameterName(String property, String operation, String parameterName){
        return PropertyCondition.createWithParameterName(property, operation, parameterName).skipNullOrEmpty();
    }

    /**
     * Creates skipping {@link PropertyCondition} with specified {@code parameterValue}.
     */
    public static PropertyCondition propertyConditionWithValue(String property, String operation, Object parameterValue){
        return PropertyCondition.createWithValue(property, operation, parameterValue).skipNullOrEmpty();
    }

    /**
     * Creates "=" skipping condition.
     */
    public static PropertyCondition equal(String property, Object value) {
        return PropertyCondition.createWithValue(property, PropertyCondition.Operation.EQUAL, value).skipNullOrEmpty();
    }

    /**
     * Creates "!=" skipping condition.
     */
    public static PropertyCondition notEqual(String property, Object value) {
        return PropertyCondition.createWithValue(property, PropertyCondition.Operation.NOT_EQUAL, value).skipNullOrEmpty();
    }

    /**
     * Creates "&gt;" skipping condition.
     */
    public static PropertyCondition greater(String property, Object value) {
        return PropertyCondition.createWithValue(property, PropertyCondition.Operation.GREATER, value).skipNullOrEmpty();
    }

    /**
     * Creates "&gt;=" skipping condition.
     */
    public static PropertyCondition greaterOrEqual(String property, Object value) {
        return PropertyCondition.createWithValue(property, PropertyCondition.Operation.GREATER_OR_EQUAL, value).skipNullOrEmpty();
    }

    /**
     * Creates "&lt;" skipping condition.
     */
    public static PropertyCondition less(String property, Object value) {
        return PropertyCondition.createWithValue(property, PropertyCondition.Operation.LESS, value).skipNullOrEmpty();
    }

    /**
     * Creates "&lt;=" skipping condition.
     */
    public static PropertyCondition lessOrEqual(String property, Object value) {
        return PropertyCondition.createWithValue(property, PropertyCondition.Operation.LESS_OR_EQUAL, value).skipNullOrEmpty();
    }

    /**
     * Creates a skipping condition that is translated to "like %value%".
     */
    public static PropertyCondition contains(String property, Object value) {
        return PropertyCondition.createWithValue(property, PropertyCondition.Operation.CONTAINS, value).skipNullOrEmpty();
    }

    /**
     * Creates a skipping condition that is translated to "like value%".
     */
    public static PropertyCondition startsWith(String property, Object value) {
        return PropertyCondition.createWithValue(property, PropertyCondition.Operation.STARTS_WITH, value).skipNullOrEmpty();
    }

    /**
     * Creates a skipping condition that is translated to "like %value".
     */
    public static PropertyCondition endsWith(String property, Object value) {
        return PropertyCondition.createWithValue(property, PropertyCondition.Operation.ENDS_WITH, value).skipNullOrEmpty();
    }

    /**
     * Creates a skipping condition that is translated to "is null" or "is not null"
     * depending on the parameter value.
     */
    public static PropertyCondition isSet(String property, Object value) {
        return PropertyCondition.createWithValue(property, PropertyCondition.Operation.IS_SET, value).skipNullOrEmpty();
    }

    /**
     * Creates a skipping condition that is translated to "in".
     */
    public static PropertyCondition inList(String property, Object value) {
        return PropertyCondition.createWithValue(property, PropertyCondition.Operation.IN_LIST, value).skipNullOrEmpty();
    }

    /**
     * Creates a skipping condition that is translated to "not in".
     */
    public static PropertyCondition notInList(String property, Object value) {
        return PropertyCondition.createWithValue(property, PropertyCondition.Operation.NOT_IN_LIST, value).skipNullOrEmpty();
    }

    /**
     * Creates a skipping condition that is translated to "is empty" or "is not empty"
     * depending on the parameter value.
     */
    public static PropertyCondition isCollectionEmpty(String property, Object value) {
        return PropertyCondition.createWithValue(property, PropertyCondition.Operation.IS_COLLECTION_EMPTY, value).skipNullOrEmpty();
    }

    /**
     * Creates a skipping condition that is translated to "member of".
     */
    public static PropertyCondition memberOfCollection(String property, Object value) {
        return PropertyCondition.createWithValue(property, PropertyCondition.Operation.MEMBER_OF_COLLECTION, value).skipNullOrEmpty();
    }

    /**
     * Creates a skipping condition that is translated to "not member of".
     */
    public static PropertyCondition notMemberOfCollection(String property, Object value) {
        return PropertyCondition.createWithValue(property, PropertyCondition.Operation.NOT_MEMBER_OF_COLLECTION, value).skipNullOrEmpty();
    }



}
