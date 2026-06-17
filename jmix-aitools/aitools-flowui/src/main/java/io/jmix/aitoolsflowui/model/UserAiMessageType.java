/*
 * Copyright 2026 Haulmont.
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

package io.jmix.aitoolsflowui.model;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.jspecify.annotations.Nullable;

public enum UserAiMessageType implements EnumClass<Integer> {

    USER(10),
    ASSISTANT(20),
    TOOL(30),
    SYSTEM(40);

    private final Integer id;

    UserAiMessageType(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    @Nullable
    public static UserAiMessageType fromId(Integer id) {
        for (UserAiMessageType at : UserAiMessageType.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}