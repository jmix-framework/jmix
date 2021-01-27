/*
 * Copyright (c) 2008-2020 Haulmont.
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

package io.jmix.reports.role;

import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportGroup;
import io.jmix.reports.entity.ReportTemplate;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.Role;

import static io.jmix.security.model.EntityAttributePolicyAction.MODIFY;

/**
 * System role that grants minimal permissions for run reports required for all users of generic UI client.
 */
@Role(name = ReportsMinimalRoleDefinition.ROLE_NAME, code = ReportsMinimalRoleDefinition.ROLE_NAME)
public interface ReportsMinimalRoleDefinition {

    String ROLE_NAME = "system-reports-minimal";

    @EntityPolicy(entityClass = Report.class, actions = {EntityPolicyAction.READ})
    @EntityPolicy(entityClass = ReportGroup.class, actions = {EntityPolicyAction.READ})
    @EntityPolicy(entityClass = ReportTemplate.class, actions = {EntityPolicyAction.READ})
    @EntityAttributePolicy(entityClass = Report.class, attributes = {"locName", "description", "code", "updateTs", "group"}, action = MODIFY)
    @EntityAttributePolicy(entityClass = ReportGroup.class, attributes = {"title", "localeNames"}, action = MODIFY)
    @EntityAttributePolicy(entityClass = ReportTemplate.class, attributes = {"code", "name", "customDefinition", "custom", "alterable"}, action = MODIFY)
    void access();
}
