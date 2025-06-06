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
import io.jmix.messagetemplates.entity.MessageTemplateGroup;
import io.jmix.messagetemplates.entity.MessageTemplateParameter;
import io.jmix.messagetemplatesflowui.view.messagetemplateparameter.model.MessageTemplateParameterLocalization;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.security.role.annotation.SpecificPolicy;
import io.jmix.securityflowui.role.annotation.MenuPolicy;
import io.jmix.securityflowui.role.annotation.ViewPolicy;

/**
 * Role that grants full access to working with message templates.
 */
@ResourceRole(name = "Message Templates: full access", code = MessageTemplatesFullAccessRole.CODE,
        scope = SecurityScope.UI)
public interface MessageTemplatesFullAccessRole {

    String CODE = "message-templates-full-access";

    @ViewPolicy(viewIds = {
            "msgtmp_MessageTemplate.list", "msgtmp_MessageTemplate.detail",
            "msgtmp_MessageTemplateGroup.list", "msgtmp_MessageTemplateGroup.detail",
            "msgtmp_MessageTemplateBlock.list", "msgtmp_MessageTemplateBlock.detail",
            "msgtmp_MessageTemplateParameter.detail", "msgtmp_MessageTemplateParametersInputDialogView",
            "msgtmp_HtmlEditorView"
    })
    void viewPolicy();

    @MenuPolicy(menuIds = {
            "msgtmp_MessageTemplate.list", "msgtmp_MessageTemplateGroup.list", "msgtmp_MessageTemplateBlock.list"
    })
    void menuPolicy();

    @EntityPolicy(entityClass = MessageTemplate.class, actions = EntityPolicyAction.ALL)
    @EntityPolicy(entityClass = MessageTemplateGroup.class, actions = EntityPolicyAction.ALL)
    @EntityPolicy(entityClass = MessageTemplateBlock.class, actions = EntityPolicyAction.ALL)
    @EntityPolicy(entityClass = MessageTemplateParameter.class, actions = EntityPolicyAction.ALL)
    @EntityPolicy(entityClass = MessageTemplateParameterLocalization.class, actions = EntityPolicyAction.ALL)
    void entityPolicy();

    @EntityAttributePolicy(entityClass = MessageTemplate.class, attributes = "*",
            action = EntityAttributePolicyAction.MODIFY)
    @EntityAttributePolicy(entityClass = MessageTemplateGroup.class, attributes = "*",
            action = EntityAttributePolicyAction.MODIFY)
    @EntityAttributePolicy(entityClass = MessageTemplateBlock.class, attributes = "*",
            action = EntityAttributePolicyAction.MODIFY)
    @EntityAttributePolicy(entityClass = MessageTemplateParameter.class, attributes = "*",
            action = EntityAttributePolicyAction.MODIFY)
    @EntityAttributePolicy(entityClass = MessageTemplateParameterLocalization.class, attributes = "*",
            action = EntityAttributePolicyAction.MODIFY)
    void entityAttributePolicy();

    @SpecificPolicy(resources = "messagetemplates.importExportMessageTemplate")
    void specific();
}
