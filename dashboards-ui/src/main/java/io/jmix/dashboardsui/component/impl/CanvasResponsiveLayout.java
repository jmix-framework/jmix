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

package io.jmix.dashboardsui.component.impl;

import io.jmix.dashboards.model.visualmodel.ResponsiveArea;
import io.jmix.dashboards.model.visualmodel.ResponsiveLayout;
import io.jmix.dashboardsui.DashboardStyleConstants;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.ResponsiveGridLayout;
import io.jmix.ui.widget.JmixCssActionsLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class CanvasResponsiveLayout extends AbstractCanvasLayout {
    private static Logger log = LoggerFactory.getLogger(DashboardImpl.class);

    public static final String NAME = "canvasResponsiveLayout";

    protected DashboardResponsiveLayout responsiveLayout;

    public CanvasResponsiveLayout init(ResponsiveLayout model) {
        responsiveLayout = components.create(DashboardResponsiveLayout.class);
        responsiveLayout.setStyleName("dashboard-layout");
        responsiveLayout.setHeightFull();
        ResponsiveGridLayout.Row wrr = responsiveLayout.addRow();
        wrr.setHeightFull();

        this.model = model;

        super.add(this.responsiveLayout);
        super.addStyleName(DashboardStyleConstants.DASHBOARD_WIDGET);
        this.unwrap(JmixCssActionsLayout.class).setId(model.getId().toString());

        return this;
    }

    @Override
    public DashboardResponsiveLayout getDelegate() {
        return responsiveLayout;
    }

    public void addComponent(Component component) {
        Iterator it = responsiveLayout.getRows().iterator();
        ResponsiveGridLayout.Row wrr = (ResponsiveGridLayout.Row) it.next();

        ResponsiveLayout rl = (ResponsiveLayout) model;

        ResponsiveLayout responsiveLayout = (ResponsiveLayout) (((AbstractCanvasLayout) component).getModel()).getParent();
        UUID componentUuid = ((AbstractCanvasLayout) component).getUuid();
        ResponsiveArea responsiveArea = responsiveLayout.getAreas().stream().
                filter(ra -> componentUuid.equals(ra.getComponent().getId())).
                findFirst().orElseThrow(() -> new RuntimeException("Can't find layout with uuid " + componentUuid));

        ResponsiveGridLayout.Column wrc = wrr.addColumn();

        Map<ResponsiveGridLayout.Breakpoint, ResponsiveGridLayout.ColumnsValue> map = new HashMap<>();
        map.put(ResponsiveGridLayout.Breakpoint.XS, createColValue(responsiveArea.getXs() == null ? rl.getXs() : responsiveArea.getXs()));
        map.put(ResponsiveGridLayout.Breakpoint.SM, createColValue(responsiveArea.getSm() == null ? rl.getSm() : responsiveArea.getSm()));
        map.put(ResponsiveGridLayout.Breakpoint.MD, createColValue(responsiveArea.getMd() == null ? rl.getMd() : responsiveArea.getMd()));
        map.put(ResponsiveGridLayout.Breakpoint.LG, createColValue(responsiveArea.getLg() == null ? rl.getLg() : responsiveArea.getLg()));

        wrc.setColumns(map);

        wrc.setComponent(component);
    }

    private ResponsiveGridLayout.ColumnsValue createColValue(Integer cols) {
        return ResponsiveGridLayout.ColumnsValue.columns(cols);
    }

    @Override
    public Collection<Component> getLayoutComponents() {
        return responsiveLayout.getOwnComponents();
    }
}
