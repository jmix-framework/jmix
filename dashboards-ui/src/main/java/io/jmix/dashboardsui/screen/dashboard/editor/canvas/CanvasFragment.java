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

package io.jmix.dashboardsui.screen.dashboard.editor.canvas;

import io.jmix.dashboards.model.DashboardModel;
import io.jmix.dashboardsui.DashboardException;
import io.jmix.dashboardsui.component.Dashboard;
import io.jmix.dashboardsui.component.impl.CanvasRootLayout;
import io.jmix.dashboardsui.component.impl.CanvasWidgetLayout;
import io.jmix.dashboardsui.dashboard.tools.DashboardModelConverter;
import io.jmix.dashboardsui.widget.LookupWidget;
import io.jmix.dashboardsui.widget.RefreshableWidget;
import io.jmix.ui.WindowParam;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.HasComponents;
import io.jmix.ui.component.OrderedContainer;
import io.jmix.ui.screen.ScreenFragment;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@UiController("dshbrd_Canvas.fragment")
@UiDescriptor("canvas-fragment.xml")
public class CanvasFragment extends ScreenFragment {
    public static final String SCREEN_NAME = "dshbrd_Canvas.fragment";
    public static final String DASHBOARD_MODEL = "dashboardModel";
    public static final String DASHBOARD = "dashboard";

    @Autowired
    protected OrderedContainer canvas;
    @Autowired
    @Qualifier("uiModelConverter")
    protected DashboardModelConverter converter;
    @WindowParam
    protected Dashboard dashboard;
    @WindowParam
    protected DashboardModel dashboardModel;

    protected CanvasRootLayout vLayout;

    @Subscribe
    public void onInit(InitEvent event) {
        updateLayout(dashboardModel);
    }

    public DashboardModel getDashboardModel() {
        return dashboardModel;
    }

    public Dashboard getDashboard() {
        return dashboard;
    }

    protected DashboardModelConverter getConverter() {
        return converter;
    }

    public void updateLayout(@Nullable DashboardModel dashboard) {
        if (dashboard == null) {
            throw new DashboardException("DASHBOARD parameter can not be null");
        }
        this.dashboardModel = dashboard;
        vLayout = (CanvasRootLayout) getConverter().modelToContainer(this, dashboard.getVisualModel());
        canvas.removeAll();
        canvas.add(vLayout);
    }

    public List<RefreshableWidget> getRefreshableWidgets() {
        List<RefreshableWidget> result = new ArrayList<>();
        searchWidgets(vLayout, RefreshableWidget.class, result);
        return result;
    }

    public List<LookupWidget> getLookupWidgets() {
        List<LookupWidget> result = new ArrayList<>();
        searchWidgets(vLayout, LookupWidget.class, result);
        return result;
    }

    protected <T> void searchWidgets(HasComponents layout, Class<T> widgetClass, List<T> wbList) {
        if (layout instanceof CanvasWidgetLayout) {
            ScreenFragment wb = getWidgetFragment((CanvasWidgetLayout) layout);
            if (wb != null && widgetClass.isAssignableFrom(wb.getClass())) {
                wbList.add((T) wb);
            }
        } else {
            for (Component child : layout.getOwnComponents()) {
                if (child instanceof HasComponents) {
                    searchWidgets((HasComponents) child, widgetClass, wbList);
                }
            }
        }
    }

    private ScreenFragment getWidgetFragment(CanvasWidgetLayout layout) {
        return layout.getWidgetComponent();
    }

    public CanvasRootLayout getvLayout() {
        return vLayout;
    }
}
