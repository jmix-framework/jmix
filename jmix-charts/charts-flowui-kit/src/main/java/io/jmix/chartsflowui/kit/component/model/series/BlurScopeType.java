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

package io.jmix.chartsflowui.kit.component.model.series;

import io.jmix.chartsflowui.kit.component.model.HasEnumId;
import jakarta.annotation.Nullable;

/**
 * The range of fade out when focus is enabled.
 */
public enum BlurScopeType implements HasEnumId {
    COORDINATE_SYSTEM("coordinateSystem"),
    SERIES("series"),
    GLOBAL("global");

    private final String id;

    BlurScopeType(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Nullable
    public static BlurScopeType fromId(String id) {
        for (BlurScopeType at : BlurScopeType.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}
