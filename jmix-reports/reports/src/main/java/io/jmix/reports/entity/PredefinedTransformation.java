/*
 * Copyright 2021 Haulmont.
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

package io.jmix.reports.entity;

import io.jmix.core.metamodel.datatype.EnumClass;

/**
 * Convenient built-in transformations for input parameter values entered by user.
 * Transformed value is passed to the data set loading stage, instead of original value.
 */
public enum PredefinedTransformation implements EnumClass<Integer> {
    /**
     * Convert text value into a mask working as "starts with" condition.
     * Meant to be used in "like" expression in SQL or JPQL query.
     */
    STARTS_WITH(0),

    /**
     * Convert text value into a mask working as "contains" condition.
     * Meant to be used in "like" expression in SQL or JPQL query.
     */
    CONTAINS(1),

    /**
     * Convert text value into a mask working as "ends with" condition.
     * Meant to be used in "like" expression in SQL or JPQL query.
     */
    ENDS_WITH(2);

    private Integer id;

    PredefinedTransformation(Integer id) {
        this.id = id;
    }

    @Override
    public Integer getId() {
        return id;
    }

    public static PredefinedTransformation fromId(Integer id) {
        for (PredefinedTransformation type : PredefinedTransformation.values()) {
            if (type.getId().equals(id)) {
                return type;
            }
        }
        return null;
    }
}
