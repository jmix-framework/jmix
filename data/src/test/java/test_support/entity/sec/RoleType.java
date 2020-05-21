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

package test_support.entity.sec;

import io.jmix.core.metamodel.datatype.impl.EnumClass;

import java.util.Objects;

/**
 * Role type.
 */
public enum RoleType implements EnumClass<Integer> {

    STANDARD(0),
    SUPER(10),
    READONLY(20),
    DENYING(30);

    private int id;

    RoleType(int id) {
        this.id = id;
    }

    @Override
    public Integer getId() {
        return id;
    }

    public static RoleType fromId(Integer id) {
        if (id == null)
            return STANDARD; // for backward compatibility, just in case
        for (RoleType type : RoleType.values()) {
            if (Objects.equals(id, type.getId()))
                return type;
        }
        return null; // unknown id
    }
}
