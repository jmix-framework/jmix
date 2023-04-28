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

package io.jmix.dashboards.model.parameter;


import io.jmix.core.metamodel.datatype.impl.EnumClass;

import jakarta.annotation.Nullable;

public enum ParameterType implements EnumClass<String> {

    ENTITY("ENTITY"),
    ENTITY_LIST("ENTITY_LIST"),
    ENUM("ENUM"),
    DATE("DATE"),
    DATETIME("DATETIME"),
    TIME("TIME"),
    UUID("UUID"),
    INTEGER("INTEGER"),
    STRING("STRING"),
    DECIMAL("DECIMAL"),
    BOOLEAN("BOOLEAN"),
    LONG("LONG");

    private String id;

    ParameterType(String value) {
        this.id = value;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static ParameterType fromId(String id) {
        for (ParameterType at : ParameterType.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}