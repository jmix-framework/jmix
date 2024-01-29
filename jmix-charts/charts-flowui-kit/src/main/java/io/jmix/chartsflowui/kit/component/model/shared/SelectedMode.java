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
 * Selected mode of legend or series, which controls whether series can be toggled displaying by clicking legends.
 * It is enabled by default, and you may set it to be false to disable it.
 */
public enum SelectedMode implements HasEnumId {
    DISABLED("false"),
    SINGLE("single"),
    MULTIPLE("multiple");

    private final String id;

    SelectedMode(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Nullable
    public static SelectedMode fromId(String id) {
        for (SelectedMode at : SelectedMode.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}
