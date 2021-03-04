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


import io.jmix.emailtemplates.role.EmailTemplatesAdminRole;
import io.jmix.reportsui.role.ReportsRunUiRole;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.security.role.annotation.SpecificPolicy;
import io.jmix.securityui.role.annotation.MenuPolicy;
import io.jmix.securityui.role.annotation.ScreenPolicy;

@ResourceRole(code = EmailTemplatesAdminUiRole.CODE, name = "Email Templates: administration UI")
public interface EmailTemplatesAdminUiRole extends EmailTemplatesAdminRole, ReportsRunUiRole {

    String CODE = "emailtemplates-admin-ui";

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
    void emailTemplatesAdminUi();
}
