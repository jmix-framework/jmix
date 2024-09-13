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

package io.jmix.fullcalendarflowui.component.data;

import io.jmix.fullcalendarflowui.kit.component.model.HasEnumId;
import org.springframework.lang.Nullable;

/**
 * Describes possible data operations in data providers.
 *
 * @see ItemsCalendarDataProvider
 */
public enum DataChangeOperation implements HasEnumId<String> {

    REFRESH("refresh"),
    UPDATE("update"),
    ADD("add"),
    REMOVE("remove");

    private final String id;

    DataChangeOperation(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Nullable
    public static DataChangeOperation fromId(String id) {
        for (DataChangeOperation operation : values()) {
            if (operation.getId().equals(id)) {
                return operation;
            }
        }
        return null;
    }
}