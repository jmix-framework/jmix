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
 * Data type of the report's input parameter.
 * Determines type of the input value that will be passed to the data sets and further.
 * Also determines the UI component which will be used to enter the value when launching report in UI.
 *
 * @see ReportInputParameter
 * @see io.jmix.reports.annotation.InputParameterDef
 */
public enum ParameterType implements EnumClass<Integer> {
    /**
     * Date, without time.
     * Value class: {@link java.util.Date}
     */
    DATE(10),
    /**
     * Text string.
     * Value class: {@link String}
     */
    TEXT(20),
    /**
     * A single entity instance (persistent or DTO).
     */
    ENTITY(30),
    /**
     * {@link Boolean} value.
     */
    BOOLEAN(40),
    /**
     * Number with an optional decimal part.
     * Value class: {@link Double}
     */
    NUMERIC(50),
    /**
     * A {@link java.util.Collection} of entity instances (persistent or DTO).
     */
    ENTITY_LIST(60),
    /**
     * Enumeration value, descendant of {@link Enum}.
     */
    ENUMERATION(70),
    /**
     * Date with time.
     * Value class: {@link java.util.Date}
     */
    DATETIME(80),
    /**
     * Time.
     * Value class: {@link java.util.Date}
     */
    TIME(90);

    private Integer id;

    @Override
    public Integer getId() {
        return id;
    }

    ParameterType(Integer id) {
        this.id = id;
    }

    public static ParameterType fromId(Integer id) {
        for (ParameterType type : ParameterType.values()) {
            if (type.getId().equals(id)) {
                return type;
            }
        }
        return null;
    }
}