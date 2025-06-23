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
import io.jmix.reports.yarg.loaders.ReportDataLoader;

import java.util.Objects;

/**
 * When running a report, datasets are transformed into lists of rows, where each row contains a map of name-value pairs.
 * Data set type determines source where the data comes from.
 */
public enum DataSetType implements EnumClass<Integer> {

    /**
     * Data is produced by SQL query.
     */
    SQL(10, "sql"),

    /**
     * Data is produced by JPQL query.
     */
    JPQL(20, "jpql"),

    /**
     * Data is produced by Groovy script.
     */
    GROOVY(30, "groovy"),

    /**
     * Data consists of a single row,
     * and is produced using attributes of an input parameter of the {@link ParameterType#ENTITY} type.
     */
    SINGLE(40, "single"),

    /**
     * Data is produced using attributes of an entity list, which is taken from input parameters.
     * Two options are available:
     * <li>input parameter of the {@link ParameterType#ENTITY_LIST} type is used as a source</li>
     * <li>sub-collection attribute of the {@link ParameterType#ENTITY} parameter is used as a source</li>
     */
    MULTI(50, "multi"),

    /**
     * Data is produced by applying a JsonPath query to a JSON input.
     */
    JSON(60, "json"),

    /**
     * Delegation to user-defined {@link ReportDataLoader} object.
     */
    DELEGATE(70, "delegate");

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