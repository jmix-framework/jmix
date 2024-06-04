/*
 * Copyright 2023 Haulmont.
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

package io.jmix.chartsflowui.kit.component.model.shared;

import io.jmix.chartsflowui.kit.component.model.HasEnumId;
import jakarta.annotation.Nullable;

/**
 * Enum to define how to display text when it's overflow.
 */
public enum Overflow implements HasEnumId {
    NONE("none"),
    TRUNCATE("truncate"),
    BREAK("break"),
    BREAK_ALL("breakAll");

    private final String id;

    Overflow(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Nullable
    public static Overflow fromId(String id) {
        for (Overflow at : Overflow.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}
