/*
 * Copyright 2021 Haulmont.
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

package io.jmix.charts.model.settings;

import io.jmix.charts.model.JsonEnum;

import javax.annotation.Nullable;

public enum CreditsPosition implements JsonEnum {
    TOP_LEFT("top-left"),
    TOP_RIGHT("top-right"),
    BOTTOM_LEFT("bottom-left"),
    BOTTOM_RIGHT("bottom-right");

    private String id;

    CreditsPosition(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static CreditsPosition fromId(String id) {
        for (CreditsPosition position : values()) {
            if (position.getId().equals(id)) {
                return position;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return id;
    }
}
