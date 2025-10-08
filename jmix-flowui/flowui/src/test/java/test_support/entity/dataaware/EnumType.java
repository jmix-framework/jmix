/*
 * Copyright 2025 Haulmont.
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

package test_support.entity.dataaware;

import io.jmix.core.metamodel.datatype.EnumClass;

import java.util.Objects;

public enum EnumType implements EnumClass<String> {

    ENUM_1("enum1"),
    ENUM_2("enum2"),
    ENUM_3("enum3");

    private final String id;

    EnumType(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    public static EnumType fromId(String id) {
        if (id == null)
            return ENUM_1; // for backward compatibility, just in case
        for (EnumType type : EnumType.values()) {
            if (Objects.equals(id, type.getId()))
                return type;
        }
        return null; // unknown id
    }
}
