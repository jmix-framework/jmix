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

package io.jmix.reports.entity.pivottable;

import io.jmix.core.metamodel.datatype.EnumClass;

public enum AggregationMode implements EnumClass<String> {
    COUNT("count"),
    COUNT_UNIQUE_VALUES("countUniqueValues"),
    LIST_UNIQUE_VALUES("listUniqueValues"),
    SUM("sum"),
    INTEGER_SUM("integerSum"),
    AVERAGE("average"),
    MINIMUM("minimum"),
    MAXIMUM("maximum"),
    SUM_OVER_SUM("sumOverSum"),
    UPPER_BOUND_80("upperBound80"),
    LOWER_BOUND_80("lowerBound80"),
    SUM_AS_FRACTION_OF_TOTAL("sumAsFractionOfTotal"),
    SUM_AS_FRACTION_OF_ROWS("sumAsFractionOfRows"),
    SUM_AS_FRACTION_OF_COLUMNS("sumAsFractionOfColumns"),
    COUNT_AS_FRACTION_OF_TOTAL("countAsFractionOfTotal"),
    COUNT_AS_FRACTION_OF_ROWS("countAsFractionOfRows"),
    COUNT_AS_FRACTION_OF_COLUMNS("countAsFractionOfColumns");

    String id;

    AggregationMode(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    public static AggregationMode fromId(String id) {
        for (AggregationMode type : values()) {
            if (type.getId().equals(id)) {
                return type;
            }
        }
        return null;
    }
}
