/*
 * Copyright 2019 Haulmont.
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
package com.haulmont.cuba.core.model.common;

import io.jmix.core.metamodel.datatype.impl.EnumClass;

import java.util.Objects;

/**
 * Type of constraint.
 */
public enum ConstraintCheckType implements EnumClass<String> {
    DATABASE_AND_MEMORY("db_and_memory", true, true),
    DATABASE("db", true, false),
    MEMORY("memory", false, true);

    private String id;
    private boolean memory;
    private boolean database;

    ConstraintCheckType(String id, boolean database, boolean memory) {
        this.id = id;
        this.database = database;
        this.memory = memory;
    }

    @Override
    public String getId() {
        return id;
    }

    public static ConstraintCheckType fromId(String id) {
        for (ConstraintCheckType type : ConstraintCheckType.values()) {
            if (Objects.equals(id, type.getId()))
                return type;
        }
        return null; // unknown id
    }

    public boolean database() {
        return database;
    }

    public boolean memory() {
        return memory;
    }
}