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

package io.jmix.reportsui.screen.report.edit.tabs;

import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportRole;
import io.jmix.reports.entity.ReportScreen;
import io.jmix.security.constraint.PolicyStore;
import io.jmix.security.constraint.SecureOperations;
import io.jmix.security.model.BaseRole;
import io.jmix.security.role.ResourceRoleRepository;
import io.jmix.ui.WindowConfig;
import io.jmix.ui.WindowInfo;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.ComboBox;
import io.jmix.ui.component.Table;
import io.jmix.ui.model.CollectionPropertyContainer;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.*;
import io.jmix.ui.sys.ScreensHelper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@UiController("report_ReportEditSecurity.fragment")
@UiDescriptor("report-edit-security-fragment.xml")
public class ReportEditSecurityFragment extends ScreenFragment {

    @Autowired
    protected InstanceContainer<Report> reportDc;

    @Autowired
    protected CollectionPropertyContainer<ReportRole> reportRolesDc;

    @Autowired
    protected CollectionPropertyContainer<ReportScreen> reportScreensDc;

    @Autowired
    protected Table<ReportRole> rolesTable;

    @Autowired
    protected Table<ReportScreen> screenTable;

    @Autowired
    protected ComboBox<BaseRole> rolesField;

    @Autowired
    protected ComboBox<String> screenIdField;

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected SecureOperations secureOperations;

    @Autowired
    protected PolicyStore policyStore;

    @Autowired
    protected ScreensHelper screensHelper;

    @Autowired
    protected WindowConfig windowConfig;

    @Autowired
    protected Messages messages;

    @Autowired
    protected ResourceRoleRepository resourceRoleRepository;

    @Subscribe
    protected void onInit(InitEvent event) {
        List<WindowInfo> windowInfoCollection = new ArrayList<>(windowConfig.getWindows());
        // sort by screenId
        screensHelper.sortWindowInfos(windowInfoCollection);

        initScreenIdField(windowInfoCollection);
        initRoleField();
    }

    protected void initScreenIdField(List<WindowInfo> windowInfoCollection) {
        Map<String, String> screens = new LinkedHashMap<>();
        for (WindowInfo windowInfo : windowInfoCollection) {
            String id = windowInfo.getId();
            String menuId = "menu-config." + id;
            String localeMsg = messages.getMessage(menuId);
            String title = menuId.equals(localeMsg) ? id : id + " ( " + localeMsg + " )";
            screens.put(title, id);
        }
        screenIdField.setOptionsMap(screens);
    }

    protected void initRoleField() {
        Map<String, BaseRole> roles = new LinkedHashMap<>();
        for (BaseRole baseRole : resourceRoleRepository.getAllRoles()) {
            roles.put(baseRole.getName(), baseRole);
        }
        rolesField.setOptionsMap(roles);
    }

    @Install(to = "rolesTable.exclude", subject = "enabledRule")
    protected boolean rolesTableExcludeEnabledRule() {
        return isUpdatePermitted();
    }

    @Install(to = "rolesTable.add", subject = "enabledRule")
    protected boolean rolesTableAddEnabledRule() {
        return isUpdatePermitted();
    }

    @Subscribe("rolesTable.add")
    protected void onRolesTableAdd(Action.ActionPerformedEvent event) {
        if (rolesField.getValue() != null) {
            BaseRole role = rolesField.getValue();

            boolean exists = reportRolesDc.getItems()
                    .stream()
                    .anyMatch(reportRole -> role.getCode().equalsIgnoreCase(reportRole.getRoleCode()));

            if (!exists) {
                ReportRole reportRole = metadata.create(ReportRole.class);
                reportRole.setRoleName(role.getName());
                reportRole.setRoleCode(role.getCode());
                reportRolesDc.getMutableItems().add(reportRole);
            }
        }
    }

    @Subscribe("screenTable.add")
    public void onScreenTableAdd(Action.ActionPerformedEvent event) {
        if (screenIdField.getValue() != null) {
            String screenId = screenIdField.getValue();

            boolean exists = reportScreensDc.getItems()
                    .stream()
                    .anyMatch(reportScreen -> screenId.equalsIgnoreCase(reportScreen.getScreenId()));

            if (!exists) {
                ReportScreen reportScreen = metadata.create(ReportScreen.class);
                reportScreen.setScreenId(screenId);
                reportScreensDc.getMutableItems().add(reportScreen);
            }
        }
    }

    protected boolean isUpdatePermitted() {
        return secureOperations.isEntityUpdatePermitted(metadata.getClass(Report.class), policyStore);
    }
}
