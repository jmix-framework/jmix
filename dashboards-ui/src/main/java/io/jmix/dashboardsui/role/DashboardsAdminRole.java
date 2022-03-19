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

package io.jmix.dashboardsui.role;

import io.jmix.dashboards.entity.DashboardGroup;
import io.jmix.dashboards.entity.PersistentDashboard;
import io.jmix.dashboards.entity.WidgetTemplate;
import io.jmix.dashboards.entity.WidgetTemplateGroup;
import io.jmix.dashboards.model.DashboardModel;
import io.jmix.dashboards.model.Widget;
import io.jmix.dashboards.model.parameter.Parameter;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.security.role.annotation.SpecificPolicy;
import io.jmix.securityui.role.annotation.MenuPolicy;
import io.jmix.securityui.role.annotation.ScreenPolicy;

@ResourceRole(code = DashboardsAdminRole.CODE, name = "Dashboards: administration", scope = SecurityScope.UI)
public interface DashboardsAdminRole {
    String CODE = "dashboards-admin";

    @ScreenPolicy(screenIds = {
            "dshbrd_PersistentDashboard.browse",
            "dshbrd_WidgetTemplate.browse",
            "dshbrd_PersistentDashboard.edit",
            "dshbrd_Colspan.dialog",
            "dshbrd_CssLayoutCreation.dialog",
            "dshbrd_Expand.dialog",
            "dshbrd_GridCreation.dialog",
            "dshbrd_ResponsiveCreation.dialog",
            "dshbrd_Style.dialog",
            "dshbrd_Weight.dialog",
            "dshbrd_DashboardView.screen",
            "dshbrd_Parameter.edit",
            "dshbrd_DashboardGroup.browse",
            "dshbrd_DashboardGroup.edit",
            "dshbrd_Widget.edit",
            "dshbrd_WidgetTemplate.edit",
            "dshbrd_WidgetTemplateGroup.edit",
            "dshbrd_WidgetTemplateGroup.browse"
    })
    @MenuPolicy(menuIds = {
            "dashboard",
            "dshbrd_PersistentDashboard.browse",
            "dshbrd_WidgetTemplate.browse"
    })
    @SpecificPolicy(resources = "dashboardGroupsBrowse")
    @SpecificPolicy(resources = "dashboardEditButton")
    @EntityPolicy(entityClass = Parameter.class, actions = {EntityPolicyAction.ALL})
    @EntityPolicy(entityClass = WidgetTemplateGroup.class, actions = {EntityPolicyAction.ALL})
    @EntityPolicy(entityClass = WidgetTemplate.class, actions = {EntityPolicyAction.ALL})
    @EntityPolicy(entityClass = DashboardGroup.class, actions = {EntityPolicyAction.ALL})
    @EntityPolicy(entityClass = PersistentDashboard.class, actions = {EntityPolicyAction.ALL})
    @EntityPolicy(entityClass = Widget.class, actions = {EntityPolicyAction.ALL})
    @EntityPolicy(entityClass = DashboardModel.class, actions = {EntityPolicyAction.ALL})
    @EntityAttributePolicy(entityClass = Parameter.class, action = EntityAttributePolicyAction.MODIFY, attributes = "*")
    @EntityAttributePolicy(entityClass = WidgetTemplateGroup.class, action = EntityAttributePolicyAction.MODIFY, attributes = "*")
    @EntityAttributePolicy(entityClass = WidgetTemplate.class, action = EntityAttributePolicyAction.MODIFY, attributes = "*")
    @EntityAttributePolicy(entityClass = DashboardGroup.class, action = EntityAttributePolicyAction.MODIFY, attributes = "*")
    @EntityAttributePolicy(entityClass = PersistentDashboard.class, action = EntityAttributePolicyAction.MODIFY, attributes = "*")
    @EntityAttributePolicy(entityClass = Widget.class, action = EntityAttributePolicyAction.MODIFY, attributes = "*")
    @EntityAttributePolicy(entityClass = DashboardModel.class, action = EntityAttributePolicyAction.MODIFY, attributes = "*")
    void dashboardsAdmin();
}
