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

package io.jmix.reports.impl.builder;

import io.jmix.reports.annotation.AvailableForRoles;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportRole;

import java.util.Set;

/**
 * Part of the {@link AnnotatedReportBuilder}.
 * Performs extracting roles from the report definition.
 */
public interface AnnotatedReportRoleExtractor {

    /**
     * Extract roles from the report definition, analyzing {@link AvailableForRoles} annotation.
     * @param definitionInstance annotated definition object
     * @param report report model object being constructed
     * @return set of report roles
     */
    Set<ReportRole> extractRoles(Object definitionInstance, Report report);
}
