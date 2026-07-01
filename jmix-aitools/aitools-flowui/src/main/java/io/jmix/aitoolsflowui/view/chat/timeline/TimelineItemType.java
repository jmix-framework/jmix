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

package io.jmix.aitoolsflowui.view.chat.timeline;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Type of {@link TimelineItem}: a user message, an assistant message, or a transient assistant
 * "thinking" placeholder shown while a response is being generated.
 */
@NullMarked
public enum TimelineItemType implements EnumClass<Integer> {

    USER(10),
    ASSISTANT(20),
    ASSISTANT_THINKING(30);

    private final Integer id;

    TimelineItemType(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    /**
     * Returns the type with the given stored id.
     *
     * @param id stored enum id
     * @return the matching type, or {@code null} if no type has that id
     */
    @Nullable
    public static TimelineItemType fromId(Integer id) {
        for (TimelineItemType at : TimelineItemType.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}