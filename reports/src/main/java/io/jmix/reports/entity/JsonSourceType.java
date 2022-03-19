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

public enum JsonSourceType implements EnumClass<Integer> {
    GROOVY_SCRIPT(10),
    URL(20),
    PARAMETER(30);

    private Integer id;

    JsonSourceType(Integer id) {
        this.id = id;
    }

    @Override
    public Integer getId() {
        return id;
    }

    public static JsonSourceType fromId(Integer id) {
        for (JsonSourceType type : JsonSourceType.values()) {
            if (type.getId().equals(id)) {
                return type;
            }
        }
        return null;
    }
}
