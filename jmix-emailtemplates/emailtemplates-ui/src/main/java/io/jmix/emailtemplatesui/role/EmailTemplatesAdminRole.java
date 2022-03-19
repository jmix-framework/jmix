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

package io.jmix.emailtemplatesui.role;


import io.jmix.emailtemplates.entity.*;
import io.jmix.reports.entity.*;
import io.jmix.reportsui.role.ReportsRunRole;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.security.role.annotation.SpecificPolicy;
import io.jmix.securityui.role.annotation.MenuPolicy;
import io.jmix.securityui.role.annotation.ScreenPolicy;

@ResourceRole(code = EmailTemplatesAdminRole.CODE, name = "Email Templates: administration", scope = SecurityScope.UI)
public interface EmailTemplatesAdminRole extends ReportsRunRole {

    String CODE = "emailtemplates-admin";

    @ScreenPolicy(screenIds = {
            "emltmp_TemplateGroup.edit",
            "emltmp_TemplateGroup.browse",
            "emltmp_TemplateBlock.edit",
            "emltmp_TemplateBlock.browse",
            "emltmp_TemplateBlockGroup.edit",
            "emltmp_TemplateBlockGroup.browse",
            "emltmp_HtmlSourceCodeScreen",
            "emltmp_EmailTemplate.send",
            "emltmp_ReportEmailTemplate.edit",
            "emltmp_EmailTemplate.browse",
            "emltmp_JsonEmailTemplate.edit",
            "emltmp_EmailTemplateAttachment.edit",
            "report_Report.browse",
            "report_InputParameters.lookup",
            "report_InputParametersFragment",
            "report_ReportInputParameter.edit",
            "report_ReportValueFormat.edit"})
    @MenuPolicy(menuIds = {"emltmp_EmailTemplate.browse"})
    @SpecificPolicy(resources = {"groupsButton", "blocksButton"})
    @EntityPolicy(entityClass = Report.class, actions = {EntityPolicyAction.READ})
    @EntityPolicy(entityClass = ReportValueFormat.class, actions = {EntityPolicyAction.ALL})
    @EntityPolicy(entityClass = ReportInputParameter.class, actions = {EntityPolicyAction.ALL})
    @EntityPolicy(entityClass = EmailTemplateAttachment.class, actions = {EntityPolicyAction.ALL})
    @EntityPolicy(entityClass = TemplateReport.class, actions = {EntityPolicyAction.ALL})
    @EntityPolicy(entityClass = TemplateGroup.class, actions = {EntityPolicyAction.ALL})
    @EntityPolicy(entityClass = TemplateBlockGroup.class, actions = {EntityPolicyAction.ALL})
    @EntityPolicy(entityClass = TemplateBlock.class, actions = {EntityPolicyAction.ALL})
    @EntityPolicy(entityClass = ParameterValue.class, actions = {EntityPolicyAction.ALL})
    @EntityPolicy(entityClass = ReportEmailTemplate.class, actions = {EntityPolicyAction.ALL})
    @EntityPolicy(entityClass = EmailTemplate.class, actions = {EntityPolicyAction.ALL})
    @EntityPolicy(entityClass = JsonEmailTemplate.class, actions = {EntityPolicyAction.ALL})
    @EntityAttributePolicy(entityClass = ReportValueFormat.class, action = EntityAttributePolicyAction.MODIFY, attributes = "*")
    @EntityAttributePolicy(entityClass = ReportInputParameter.class, action = EntityAttributePolicyAction.MODIFY, attributes = "*")
    @EntityAttributePolicy(entityClass = Report.class, action = EntityAttributePolicyAction.MODIFY, attributes = "*")
    @EntityAttributePolicy(entityClass = EmailTemplateAttachment.class, action = EntityAttributePolicyAction.MODIFY, attributes = "*")
    @EntityAttributePolicy(entityClass = TemplateReport.class, action = EntityAttributePolicyAction.MODIFY, attributes = "*")
    @EntityAttributePolicy(entityClass = TemplateGroup.class, action = EntityAttributePolicyAction.MODIFY, attributes = "*")
    @EntityAttributePolicy(entityClass = TemplateBlockGroup.class, action = EntityAttributePolicyAction.MODIFY, attributes = "*")
    @EntityAttributePolicy(entityClass = TemplateBlock.class, action = EntityAttributePolicyAction.MODIFY, attributes = "*")
    @EntityAttributePolicy(entityClass = ParameterValue.class, action = EntityAttributePolicyAction.MODIFY, attributes = "*")
    @EntityAttributePolicy(entityClass = ReportEmailTemplate.class, action = EntityAttributePolicyAction.MODIFY, attributes = "*")
    @EntityAttributePolicy(entityClass = EmailTemplate.class, action = EntityAttributePolicyAction.MODIFY, attributes = "*")
    @EntityAttributePolicy(entityClass = JsonEmailTemplate.class, action = EntityAttributePolicyAction.MODIFY, attributes = "*")
    void emailTemplatesAdmin();
}
