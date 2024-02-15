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

package io.jmix.reportsrest.security.role;

import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportExecution;
import io.jmix.reports.entity.ReportGroup;
import io.jmix.reports.entity.ReportTemplate;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.security.role.annotation.SpecificPolicy;

import static io.jmix.security.model.EntityAttributePolicyAction.VIEW;

/**
 * Role that grants minimal permissions to run reports using REST API.
 */
@ResourceRole(name = "Reports: run reports using REST API", code = ReportsRunApiRole.CODE, scope = SecurityScope.API)
public interface ReportsRunApiRole {

    String CODE = "report-run-api";

    @EntityPolicy(entityClass = Report.class, actions = {EntityPolicyAction.READ})
    @EntityPolicy(entityClass = ReportGroup.class, actions = {EntityPolicyAction.READ})
    @EntityPolicy(entityClass = ReportTemplate.class, actions = {EntityPolicyAction.READ})
    @EntityPolicy(entityClass = ReportExecution.class, actions = {EntityPolicyAction.CREATE, EntityPolicyAction.UPDATE})
    void entityPolicy();

    @EntityAttributePolicy(entityClass = Report.class, attributes = {"localeNames", "description", "code", "updateTs", "group"}, action = VIEW)
    @EntityAttributePolicy(entityClass = ReportGroup.class, attributes = {"title", "localeNames"}, action = VIEW)
    @EntityAttributePolicy(entityClass = ReportTemplate.class, attributes = {"code", "name", "customDefinition", "custom", "alterable"}, action = VIEW)
    void entityAttributePolicy();

    @SpecificPolicy(resources = "reports.rest.enabled")
    void specificPolicy();
}
