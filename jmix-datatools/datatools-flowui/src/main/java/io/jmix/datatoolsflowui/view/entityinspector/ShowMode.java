/*
 * Copyright 2022 Haulmont.
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

package io.jmix.datatoolsflowui.view.entityinspector;

import io.jmix.core.metamodel.datatype.impl.EnumClass;

import jakarta.annotation.Nullable;

public enum ShowMode implements EnumClass<String> {

    NON_REMOVED("non_removed"),
    REMOVED("removed"),
    ALL("all");

    private final String id;

    ShowMode(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Nullable
    public static ShowMode fromId(String id) {
        for (ShowMode at : ShowMode.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}
