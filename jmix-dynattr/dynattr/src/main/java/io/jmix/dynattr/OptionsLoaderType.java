/*
 * Copyright 2020 Haulmont.
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

package io.jmix.dynattr;


import io.jmix.core.metamodel.datatype.impl.EnumClass;

import jakarta.annotation.Nullable;
import java.util.Objects;

public enum OptionsLoaderType implements EnumClass<String> {
    JPQL,
    SQL,
    GROOVY;

    @Override
    public String getId() {
        return name();
    }

    @Nullable
    public static OptionsLoaderType fromId(String id) {
        for (OptionsLoaderType type : OptionsLoaderType.values()) {
            if (Objects.equals(type.getId(), id)) {
                return type;
            }
        }
        return null;
    }
}
