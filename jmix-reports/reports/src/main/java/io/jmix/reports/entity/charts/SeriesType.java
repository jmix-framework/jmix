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

package io.jmix.reports.entity.charts;

import io.jmix.core.metamodel.datatype.EnumClass;

public enum SeriesType implements EnumClass<String> {
    LINE("line"), COLUMN("column"), STEP("step"), SMOOTHED_LINE("smoothedLine");

    private String id;

    @Override
    public String getId() {
        return id;
    }

    SeriesType(String id) {
        this.id = id;
    }

    public static SeriesType fromId(String id) {
        for (SeriesType type : SeriesType.values()) {
            if (type.getId().equals(id)) {
                return type;
            }
        }
        return null;
    }
}