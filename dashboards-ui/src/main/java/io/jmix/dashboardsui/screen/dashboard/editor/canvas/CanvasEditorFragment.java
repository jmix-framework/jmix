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
import io.jmix.dashboards.model.visualmodel.RootLayout;
import io.jmix.dashboardsui.DashboardStyleConstants;
import io.jmix.dashboardsui.dashboard.event.CanvasLayoutElementClickedEvent;
import io.jmix.dashboardsui.dashboard.event.DashboardRefreshEvent;
import io.jmix.dashboardsui.dashboard.event.WidgetSelectedEvent;
import io.jmix.dashboardsui.component.CanvasLayout;
import io.jmix.dashboardsui.dashboard.tools.DashboardModelConverter;
import io.jmix.dashboardsui.screen.dashboard.editor.DashboardLayoutHolderComponent;
import io.jmix.ui.UiEventPublisher;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.ComponentContainer;
import io.jmix.ui.component.MouseEventDetails;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;

import java.util.UUID;

@UiController("dshbrd_CanvasEditor.fragment")
@UiDescriptor("canvas-editor-fragment.xml")
public class CanvasEditorFragment extends CanvasFragment implements DashboardLayoutHolderComponent {

    private MouseEventDetails mouseEventDetails;

    @Autowired
    @Qualifier("dropModelConverter")
    protected DashboardModelConverter converter;

    @Autowired
    protected UiEventPublisher uiEventPublisher;

    @Override
    public void updateLayout(DashboardModel dashboard) {
        super.updateLayout(dashboard);
        vLayout.addStyleName("dashboard-main-shadow-border");
    }

    @Override
    protected DashboardModelConverter getConverter() {
        return converter;
    }

    protected void selectLayout(ComponentContainer layout, UUID layoutUuid, boolean needSelect) {
        if (layout instanceof CanvasLayout) {
            if (((CanvasLayout) layout).getUuid().equals(layoutUuid) && needSelect) {
                layout.addStyleName(DashboardStyleConstants.DASHBOARD_TREE_SELECTED);
            } else {
                layout.removeStyleName(DashboardStyleConstants.DASHBOARD_TREE_SELECTED);
            }
        }

        for (Component child : layout.getOwnComponents()) {
            if (child instanceof ComponentContainer) {
                selectLayout((ComponentContainer) child, layoutUuid, needSelect);
            }
        }
    }

    @EventListener
    public void onWidgetSelectedEvent(WidgetSelectedEvent event) {
        UUID layoutUuid = event.getSource();
        selectLayout(vLayout, layoutUuid, true);
    }

    @EventListener
    public void onLayoutRefreshedEvent(DashboardRefreshEvent event) {
        RootLayout dashboardModel = (RootLayout) event.getSource();
        this.dashboardModel.setVisualModel(dashboardModel);
        updateLayout(this.dashboardModel);
    }

    @EventListener
    public void canvasLayoutElementClickedEventListener(CanvasLayoutElementClickedEvent event) {
        UUID layoutUuid = event.getSource();
        MouseEventDetails md = event.getMouseEventDetails();
        //TODO: refactor, problem with addLayoutClickListener in every layout prod a lot of events, in root can't get clicked comp
        if (mouseEventDetails == null) {
            mouseEventDetails = md;
        } else {
            if (mouseEventDetails.getClientX() == md.getClientX() && mouseEventDetails.getClientY() == md.getClientY()) {
                return;
            } else {
                mouseEventDetails = md;
            }
        }

        uiEventPublisher.publishEvent(new WidgetSelectedEvent(layoutUuid, WidgetSelectedEvent.Target.CANVAS));
    }
}
