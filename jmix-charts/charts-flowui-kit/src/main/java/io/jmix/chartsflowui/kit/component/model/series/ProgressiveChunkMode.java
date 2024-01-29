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
 * Chunk approach, optional values:
 * <ul>
 *     <li>
 *         {@code 'sequential'}: slice data by data index.
 *     </li>
 *     <li>
 *         {@code 'mod'}: slice data by mod, which make the data items of each chunk coming from all over the data,
 *         bringing better visual effect while progressive rendering.
 *     </li>
 * </ul>
 */
public enum ProgressiveChunkMode implements HasEnumId {
    SEQUENTIAL("sequential"),
    MOD("mod");

    private final String id;

    ProgressiveChunkMode(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Nullable
    public static ProgressiveChunkMode fromId(String id) {
        for (ProgressiveChunkMode at : ProgressiveChunkMode.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}
