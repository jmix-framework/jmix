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

package io.jmix.messagetemplatesflowui.role;

import io.jmix.messagetemplates.entity.MessageTemplate;
import io.jmix.messagetemplates.entity.MessageTemplateBlock;
import io.jmix.messagetemplates.entity.MessageTemplateParameter;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityflowui.role.annotation.MenuPolicy;
import io.jmix.securityflowui.role.annotation.ViewPolicy;

/**
 * Role that grants minimal permissions to view and generate message templates.
 */
@ResourceRole(name = "Message Templates: minimal access", code = MessageTemplatesMinimalAccessRole.CODE,
        scope = SecurityScope.UI)
public interface MessageTemplatesMinimalAccessRole {

    String CODE = "message-templates-minimal-access";

    @MenuPolicy(menuIds = "msgtmp_MessageTemplate.list")
    void menuPolicy();

    @ViewPolicy(viewIds = {
            "msgtmp_MessageTemplate.list", "msgtmp_MessageTemplate.detail",
            "msgtmp_HtmlEditorView", "msgtmp_MessageTemplateParameter.detail",
            "msgtmp_MessageTemplateParametersInputDialogView"
    })
    void viewPolicy();

    @EntityPolicy(entityClass = MessageTemplate.class, actions = EntityPolicyAction.READ)
    @EntityPolicy(entityClass = MessageTemplateBlock.class, actions = EntityPolicyAction.READ)
    @EntityPolicy(entityClass = MessageTemplateParameter.class, actions = EntityPolicyAction.READ)
    void entityPolicy();

    @EntityAttributePolicy(entityClass = MessageTemplate.class,
            attributes = {"id", "name", "code", "type", "content", "parameters"},
            action = EntityAttributePolicyAction.VIEW)
    @EntityAttributePolicy(entityClass = MessageTemplateBlock.class, attributes = "*",
            action = EntityAttributePolicyAction.VIEW)
    @EntityAttributePolicy(entityClass = MessageTemplateParameter.class, attributes = "*",
            action = EntityAttributePolicyAction.VIEW)
    void entityAttributePolicy();
}
