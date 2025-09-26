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

import io.jmix.reports.entity.ReportGroup;

/**
 * Bean responsible for parsing, validating annotated report group definition
 * and creating a model object that can be referenced by annotated reports or used otherwise by the application.
 */
public interface AnnotatedGroupBuilder {

    /**
     * Parse, validate given annotated group definition.
     * Construct and return a model object.
     *
     * @param groupDefinition group class annotated with {@link io.jmix.reports.annotation.ReportGroupDef}
     * @return constructed ReportGroup model object
     * @throws InvalidReportDefinitionException if the definition has errors
     */
    ReportGroup createGroupFromDefinition(Object groupDefinition) throws InvalidReportDefinitionException;
}
