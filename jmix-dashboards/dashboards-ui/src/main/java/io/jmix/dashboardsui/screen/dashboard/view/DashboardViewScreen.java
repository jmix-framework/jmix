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
package io.jmix.dashboardsui.screen.dashboard.view;

import io.jmix.dashboardsui.component.Dashboard;
import io.jmix.dashboardsui.component.impl.DashboardImpl;
import io.jmix.ui.UiComponents;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

@UiController("dshbrd_DashboardView.screen")
@UiDescriptor("dashboard-view-screen.xml")
public class DashboardViewScreen extends Screen {
    public static final String CODE = "CODE";
    public static final String DISPLAY_NAME = "DISPLAY_NAME";

    @Autowired
    protected UiComponents uiComponents;

    protected Dashboard dashboard;

    @Subscribe
    public void onInit(InitEvent event) {
        ScreenOptions options = event.getOptions();
        Map<String, Object> params = new HashMap<>();
        if (options instanceof MapScreenOptions) {
            params = ((MapScreenOptions) options).getParams();
        }
        if (params.containsKey(DISPLAY_NAME)) {
            getWindow().setCaption((String) params.get(DISPLAY_NAME));
        }
        Dashboard dashboardComponent = uiComponents.create(Dashboard.NAME);
        dashboardComponent.setFrame(getWindow().getFrame());
        dashboardComponent.init(params);
        getWindow().add(dashboardComponent);
        this.dashboard = dashboardComponent;
    }

    public Dashboard getDashboard() {
        return dashboard;
    }

}