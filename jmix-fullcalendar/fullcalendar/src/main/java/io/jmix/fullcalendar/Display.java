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

package io.jmix.fullcalendar;


import io.jmix.core.metamodel.datatype.EnumClass;
import jakarta.annotation.Nullable;

// todo RP messages
public enum Display implements EnumClass<String> {

    AUTO("auto"),
    BLOCK("block"),
    LIST_ITEM("list-item"),
    BACKGROUND("background"),
    INVERSE_BACKGROUND("inverse-background"),
    NONE("none");

    private final String id;

    Display(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static Display fromId(String id) {
        for (Display at : Display.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}