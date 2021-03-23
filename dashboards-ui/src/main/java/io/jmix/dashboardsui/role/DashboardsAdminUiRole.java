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

import io.jmix.dashboards.role.DashboardsAdminRole;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityui.role.annotation.MenuPolicy;
import io.jmix.securityui.role.annotation.ScreenPolicy;

@ResourceRole(code = DashboardsAdminUiRole.CODE, name = "Dashboards: administration UI")
public interface DashboardsAdminUiRole extends DashboardsAdminRole {
    String CODE = "dashboards-admin-ui";

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
    void dashboardsAdminUi();
}
