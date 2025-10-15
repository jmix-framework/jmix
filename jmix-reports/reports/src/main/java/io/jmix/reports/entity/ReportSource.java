/*
 * Copyright 2025 Haulmont.
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

package io.jmix.reports.entity;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;

/**
 * Defines where the report or report group comes from.
 */
public enum ReportSource implements EnumClass<String> {

    /**
     * Report or report group was defined in the project source code.
     * @see io.jmix.reports.annotation.ReportDef
     * @see io.jmix.reports.annotation.ReportGroupDef
     */
    ANNOTATED_CLASS("annotatedClass"),

    /**
     * Report or report group is stored as persistent entity in the database.
     * @see Report
     * @see ReportGroup
     */
    DATABASE("database");

    private final String id;

    ReportSource(String id) { this.id = id; }

    @Override
    public String getId() {
        return id;
    }

    @Nullable
    public static ReportSource fromId(String id) {
        for (ReportSource item : ReportSource.values()) {
            if (item.getId().equals(id)) {
                return item;
            }
        }
        return null;
    }
}
