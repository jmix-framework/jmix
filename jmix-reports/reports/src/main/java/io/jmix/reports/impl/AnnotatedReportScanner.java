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

/**
 * Performs the following tasks:
 * <li>scanning and importing annotated report definitions</li>
 * <li>reloading hot-deployed definitions</li>
 */
public interface AnnotatedReportScanner {
    /**
     * Scan and import group definitions from application context.
     * Imported definitions are stored in {@link AnnotatedReportGroupHolder}.
     * @see io.jmix.reports.annotation.ReportGroupDef
     */
    void importGroupDefinitions();

    /**
     * Scan and import report definitions from application context.
     * Imported definitions are stored in {@link AnnotatedReportHolder}.
     * @see io.jmix.reports.annotation.ReportDef
     */
    void importReportDefinitions();

    /**
     * Reloads annotated report group definition for hot-deploy.
     * @param className fully-qualified name of the annotated report group class
     */
    void loadReportGroupClass(String className);

    /**
     * Reloads annotated report definition for hot-deploy.
     * @param className fully-qualified name of the annotated report class
     */
    void loadReportClass(String className);

}
