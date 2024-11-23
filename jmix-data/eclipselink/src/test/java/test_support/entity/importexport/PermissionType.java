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
package test_support.entity.importexport;

import io.jmix.core.metamodel.datatype.EnumClass;

import java.util.Objects;

public enum PermissionType implements EnumClass<Integer> {

    SCREEN(10),

    ENTITY_OP(20),

    ENTITY_ATTR(30),

    SPECIFIC(40),

    UI(50);

    private int id;

    PermissionType(int id) {
        this.id = id;
    }

    /** Returns corresponding database value */
    @Override
    public Integer getId() {
        return id;
    }

    /** Constructs type from corresponding database value */
    public static PermissionType fromId(Integer id) {
        for (PermissionType type : PermissionType.values()) {
            if (Objects.equals(type.getId(), id)) {
                return type;
            }
        }
        return null;
    }
}