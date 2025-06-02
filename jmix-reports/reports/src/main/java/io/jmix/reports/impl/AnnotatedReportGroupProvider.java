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

package io.jmix.reports.impl;

import io.jmix.reports.entity.ReportGroup;
import org.springframework.lang.Nullable;

import java.util.Collection;

/**
 * Bean that holds report group definitions parsed from annotated classes.
 */
public interface AnnotatedReportGroupProvider {

    /**
     * @return collection of all annotated report groups, unsorted
     */
    Collection<ReportGroup> getAllGroups();

    /**
     * Find an annotated report group by its unique code.
     *
     * @param code group code
     * @return annotated report group, or null if not found
     */
    @Nullable
    ReportGroup getGroupByCode(String code);

    /**
     * Scan and import group definitions from application context.
     */
    void importGroupDefinitions();
}
