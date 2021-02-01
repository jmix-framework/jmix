/*
 * Copyright 2020 Haulmont.
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

package io.jmix.emailtemplates.role;


import io.jmix.emailtemplates.entity.*;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;


@ResourceRole(code = "email-templates-can-send-templates-core", name = "Can send email templates (Core)")
public interface EmailTemplatesViewCoreRole {

    @EntityPolicy(entityClass = TemplateReport.class, actions = {EntityPolicyAction.READ})
    @EntityPolicy(entityClass = ReportEmailTemplate.class, actions = {EntityPolicyAction.READ})
    @EntityPolicy(entityClass = JsonEmailTemplate.class, actions = {EntityPolicyAction.READ})
    @EntityPolicy(entityClass = EmailTemplate.class, actions = {EntityPolicyAction.READ, EntityPolicyAction.UPDATE})
    @EntityPolicy(entityClass = ParameterValue.class, actions = {EntityPolicyAction.READ})
    @EntityPolicy(entityClass = TemplateGroup.class, actions = {EntityPolicyAction.READ})
    void entities();

    @EntityAttributePolicy(entityClass = TemplateReport.class, action = EntityAttributePolicyAction.VIEW, attributes = "*")
    @EntityAttributePolicy(entityClass = ReportEmailTemplate.class, action = EntityAttributePolicyAction.VIEW, attributes = "*")
    @EntityAttributePolicy(entityClass = JsonEmailTemplate.class, action = EntityAttributePolicyAction.VIEW, attributes = "*")
    @EntityAttributePolicy(entityClass = EmailTemplate.class, action = EntityAttributePolicyAction.VIEW, attributes = "*")
    @EntityAttributePolicy(entityClass = ParameterValue.class, action = EntityAttributePolicyAction.VIEW, attributes = "*")
    @EntityAttributePolicy(entityClass = TemplateGroup.class, action = EntityAttributePolicyAction.VIEW, attributes = "*")
    void entityAttributes();
}
