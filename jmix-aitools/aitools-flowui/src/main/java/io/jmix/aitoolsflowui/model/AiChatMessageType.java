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

/**
 * Origin of an {@link AiChatMessage}: a user prompt, an assistant reply, a tool result
 * or a system message.
 */
public enum AiChatMessageType implements EnumClass<Integer> {

    USER(10),
    ASSISTANT(20),
    TOOL(30),
    SYSTEM(40);

    private final Integer id;

    AiChatMessageType(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    @Nullable
    public static AiChatMessageType fromId(Integer id) {
        for (AiChatMessageType at : AiChatMessageType.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}