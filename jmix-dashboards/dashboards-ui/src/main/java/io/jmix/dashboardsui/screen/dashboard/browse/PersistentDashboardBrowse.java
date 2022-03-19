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

package io.jmix.dashboardsui.screen.dashboard.browse;

import io.jmix.core.AccessManager;
import io.jmix.core.common.util.ParamsMap;
import io.jmix.dashboards.entity.PersistentDashboard;
import io.jmix.dashboardsui.dashboard.tools.AccessConstraintsHelper;
import io.jmix.dashboardsui.role.accesscontext.DashboardGroupBrowseContext;
import io.jmix.dashboardsui.role.accesscontext.PersistentDashboardEditButtonContext;
import io.jmix.dashboardsui.screen.dashboard.view.DashboardViewScreen;
import io.jmix.dashboardsui.screen.dashboardgroup.DashboardGroupBrowse;
import io.jmix.ui.Screens;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.list.EditAction;
import io.jmix.ui.component.Button;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import javax.inject.Named;

@UiController("dshbrd_PersistentDashboard.browse")
@UiDescriptor("persistent-dashboard-browse.xml")
@LookupComponent("persistentDashboardsTable")
public class PersistentDashboardBrowse extends StandardLookup<PersistentDashboard> {

    @Autowired
    protected Screens screens;

    @Autowired
    protected CollectionContainer<PersistentDashboard> persistentDashboardsDc;

    @Autowired
    protected CollectionLoader<PersistentDashboard> persistentDashboardsDl;

    @Autowired
    protected AccessConstraintsHelper accessConstraintsHelper;

    @Autowired
    protected AccessManager accessManager;

    @Autowired
    private Button dashboardGroupsBrowse;

    @Named("persistentDashboardsTable.edit")
    private EditAction<PersistentDashboard> persistentDashboardsTableEdit;

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        persistentDashboardsDl.setParameter("currentUser", accessConstraintsHelper.getCurrentUsername());
        persistentDashboardsDl.load();

        persistentDashboardsTableEdit.setEnabled(isDashboardEditButtonPermitted());
        dashboardGroupsBrowse.setEnabled(isDashboardGroupBrowsePermitted());
    }

    protected boolean isDashboardEditButtonPermitted() {
        PersistentDashboardEditButtonContext dashboardEditButtonContext = new PersistentDashboardEditButtonContext();
        accessManager.applyRegisteredConstraints(dashboardEditButtonContext);
        return dashboardEditButtonContext.isPermitted();
    }

    protected boolean isDashboardGroupBrowsePermitted() {
        DashboardGroupBrowseContext dashboardGroupBrowseContext = new DashboardGroupBrowseContext();
        accessManager.applyRegisteredConstraints(dashboardGroupBrowseContext);
        return dashboardGroupBrowseContext.isPermitted();
    }

    @Subscribe("dashboardGroupsBrowse")
    public void onDashboardGroupsBrowseClick(Button.ClickEvent event) {
        screens.create(DashboardGroupBrowse.class, OpenMode.NEW_TAB).show();
    }

    @Subscribe("persistentDashboardsTable.show")
    public void showDashboard(Action.ActionPerformedEvent event) {
        PersistentDashboard item = persistentDashboardsDc.getItemOrNull();
        if (item != null) {
            screens.create(DashboardViewScreen.class, OpenMode.NEW_TAB, new MapScreenOptions(ParamsMap.of(
                    DashboardViewScreen.CODE, item.getCode(),
                    DashboardViewScreen.DISPLAY_NAME, item.getName())))
                    .show();
        }

    }
}
