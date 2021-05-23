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

import io.jmix.core.metamodel.datatype.impl.EnumClass;

import java.util.Objects;

public enum DataSetType implements EnumClass<Integer> {

    /**
     * SQL query
     */
    SQL(10, "sql"),

    /**
     * JPQL query
     */
    JPQL(20, "jpql"),

    /**
     * Groovy script
     */
    GROOVY(30, "groovy"),

    /**
     * Entity
     */
    SINGLE(40, "single"),

    /**
     * Entities list
     */
    MULTI(50, "multi"),

    /**
     * json
     */
    JSON(60, "json");

    private Integer id;

    private String code;

    @Override
    public Integer getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    DataSetType(Integer id, String code) {
        this.id = id;
        this.code = code;
    }

    public static DataSetType fromId(Integer id) {
        for (DataSetType type : DataSetType.values()) {
            if (type.getId().equals(id)) {
                return type;
            }
        }
        return null;
    }

    public static DataSetType fromCode(String code) {
        for (DataSetType type : DataSetType.values()) {
            if (Objects.equals(type.getCode(), code)) {
                return type;
            }
        }
        return null;
    }
}