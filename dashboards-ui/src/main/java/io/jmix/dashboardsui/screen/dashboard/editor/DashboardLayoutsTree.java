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

package io.jmix.dashboardsui.screen.dashboard.editor;

import io.jmix.dashboards.model.visualmodel.*;

import java.util.*;
import java.util.stream.Collectors;

import static io.jmix.dashboards.utils.DashboardLayoutUtils.findLayout;

public class DashboardLayoutsTree {
    private DashboardLayout visualModel;

    public void setVisualModel(DashboardLayout visualModel) {
        this.visualModel = visualModel;
    }

    protected Collection<DashboardLayout> getChildren(DashboardLayout dashboardLayout) {
        DashboardLayout item = findLayout(visualModel, dashboardLayout.getId());
        List<DashboardLayout> al = new ArrayList<>();
        if (item != null) {
            if (item instanceof GridLayout) {
                GridLayout gridLayout = (GridLayout) item;
                al.addAll(gridLayout.getAreas().stream()
                        .sorted(Comparator.comparingInt(GridArea::getRow)
                                .thenComparingInt(GridArea::getCol))
                        .map(GridArea::getComponent)
                        .collect(Collectors.toList()));
            } else if (item instanceof ResponsiveLayout) {
                ResponsiveLayout dashboardResponsiveLayout = (ResponsiveLayout) item;
                al.addAll(dashboardResponsiveLayout.getAreas().stream()
                        .sorted(Comparator.comparingInt(ResponsiveArea::getOrder))
                        .map(ResponsiveArea::getComponent)
                        .collect(Collectors.toList()));
            } else {
                al.addAll(new ArrayList<>(item.getChildren()));
            }
        }
        return al;
    }

    public List<DashboardLayout> getLayouts() {
        if (visualModel == null) {
            return Collections.emptyList();
        }
        return getChildrenRecursively(visualModel);
    }

    private List<DashboardLayout> getChildrenRecursively(DashboardLayout parent) {
        List<DashboardLayout> result = new ArrayList<>();
        result.add(parent);
        result.addAll(getChildren(parent).stream()
                .flatMap(dashboardLayout -> getChildrenRecursively(dashboardLayout).stream())
                .collect(Collectors.toList()));
        return result;
    }

    public DashboardLayout getVisualModel() {
        return visualModel;
    }
}
