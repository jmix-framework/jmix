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

package io.jmix.dashboardsui.dashboard.tools;

import io.jmix.core.security.CurrentAuthentication;
import io.jmix.dashboards.entity.WidgetTemplate;
import io.jmix.dashboards.model.DashboardModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Component("dshbrd_AccessConstraintsHelper")
public class AccessConstraintsHelper {
    @Autowired
    protected CurrentAuthentication currentAuthentication;

    public boolean isDashboardAllowedCurrentUser(DashboardModel dashboard) {
        Boolean isAvailableForAllUsers = dashboard.getIsAvailableForAllUsers();
        String createdBy = dashboard.getCreatedBy();

        if (isAvailableForAllUsers == null || isBlank(createdBy)) {
            return true;
        }

        return isAvailableForAllUsers || getCurrentUsername().equals(createdBy);
    }

    public boolean isWidgetTemplateAllowedCurrentUser(WidgetTemplate widgetTemplate) {
        Boolean isAvailableForAllUsers = widgetTemplate.getIsAvailableForAllUsers();
        String createdBy = widgetTemplate.getCreatedBy();

        if (isAvailableForAllUsers == null || isBlank(createdBy)) {
            return true;
        }

        return isAvailableForAllUsers || getCurrentUsername().equals(createdBy);
    }

    public String getCurrentUsername() {
        return currentAuthentication.getUser().getUsername();
    }
}
