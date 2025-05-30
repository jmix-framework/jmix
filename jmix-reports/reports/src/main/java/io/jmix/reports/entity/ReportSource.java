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

/**
 * Defines where the report comes from.
 */
public enum ReportSource {

    /**
     * Report was defined in the project source code.
     * @see io.jmix.reports.annotation.ReportDef
     */
    ANNOTATED_CLASS,

    /**
     * Report is stored as persistent entity in the database.
     * @see Report
     */
    DATABASE
}
