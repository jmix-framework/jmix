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

package io.jmix.dashboards.utils;

import io.jmix.dashboards.model.visualmodel.*;

import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DashboardLayoutUtils {

    @Nullable
    public static DashboardLayout findParentLayout(DashboardLayout root, DashboardLayout child) {
        return findParentLayout(root, child.getId());
    }

    @Nullable
    public static DashboardLayout findParentLayout(DashboardLayout root, UUID childId) {
        if (root instanceof GridLayout) {
            GridLayout gridLayout = (GridLayout) root;
            for (GridArea gridArea : gridLayout.getAreas()) {
                if (gridArea.getComponent().getId().equals(childId)) {
                    return root;
                } else {
                    DashboardLayout tmp = findParentLayout(gridArea.getComponent(), childId);
                    if (tmp == null) {
                        continue;
                    }
                    return findParentLayout(gridArea.getComponent(), childId);
                }
            }
        } else if (root instanceof ResponsiveLayout) {
            ResponsiveLayout responsiveLayout = (ResponsiveLayout) root;
            for (ResponsiveArea responsiveArea : responsiveLayout.getAreas()) {
                if (responsiveArea.getComponent().getId().equals(childId)) {
                    return root;
                } else {
                    DashboardLayout tmp = findParentLayout(responsiveArea.getComponent(), childId);
                    if (tmp == null) {
                        continue;
                    }
                    return findParentLayout(responsiveArea.getComponent(), childId);
                }
            }
        } else {
            for (DashboardLayout dashboardLayout : root.getChildren()) {
                if (dashboardLayout.getId().equals(childId)) {
                    return root;
                } else {
                    DashboardLayout tmp = findParentLayout(dashboardLayout, childId);
                    if (tmp == null) {
                        continue;
                    }
                    return findParentLayout(dashboardLayout, childId);
                }
            }
        }
        return null;
    }

    @Nullable
    public static DashboardLayout findLayout(DashboardLayout root, UUID uuid) {
        try {
            if (root.getId().equals(uuid)) {
                return root;
            }
            if (root instanceof GridLayout) {
                GridLayout gridLayout = (GridLayout) root;
                for (GridArea gridArea : gridLayout.getAreas()) {
                    if (gridArea.getComponent().getId().equals(uuid)) {
                        return gridArea.getComponent();
                    } else {
                        DashboardLayout tmp = findLayout(gridArea.getComponent(), uuid);
                        if (tmp == null) {
                            continue;
                        }
                        return findLayout(gridArea.getComponent(), uuid);
                    }
                }
            } else if (root instanceof ResponsiveLayout) {
                ResponsiveLayout responsiveLayout = (ResponsiveLayout) root;
                for (ResponsiveArea responsiveArea : responsiveLayout.getAreas()) {
                    if (responsiveArea.getComponent().getId().equals(uuid)) {
                        return responsiveArea.getComponent();
                    } else {
                        DashboardLayout tmp = findLayout(responsiveArea.getComponent(), uuid);
                        if (tmp == null) {
                            continue;
                        }
                        return findLayout(responsiveArea.getComponent(), uuid);
                    }
                }
            } else {
                for (DashboardLayout dashboardLayout : root.getChildren()) {
                    if (dashboardLayout.getId().equals(uuid)) {
                        return dashboardLayout;
                    } else {
                        DashboardLayout tmp = findLayout(dashboardLayout, uuid);
                        if (tmp == null) {
                            continue;
                        }
                        return findLayout(dashboardLayout, uuid);
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public static boolean isLinearLayout(DashboardLayout layout) {
        return layout instanceof HorizontalLayout || layout instanceof VerticalLayout || layout instanceof GridCellLayout;
    }

    public static boolean isParentCssLayout(DashboardLayout layout) {
        return layout.getParent() instanceof CssLayout;
    }

    public static boolean isParentHasExpand(DashboardLayout layout) {
        DashboardLayout parent = layout.getParent();
        if (isLinearLayout(parent) && parent.getExpand() != null) {
            return parent.getChildren().stream()
                    .anyMatch(e -> e.getId().equals(parent.getExpand()));
        }
        return false;
    }

    public static boolean isGridCellLayout(DashboardLayout layout) {
        return (layout instanceof GridCellLayout);
    }

    public static boolean isRootLayout(DashboardLayout layout) {
        return (layout instanceof RootLayout);
    }

    public static List<DashboardLayout> findParentsLayout(DashboardLayout root, UUID child) {
        List<DashboardLayout> result = new ArrayList<>();
        DashboardLayout layout;
        UUID ch = child;
        while ((layout = findParentLayout(root, ch)) != null) {
            result.add(layout);
            ch = layout.getId();
        }
        return result;
    }

    public static int availableColumns(GridLayout gridLayout, GridCellLayout gridCell) {
        int availableColumns = 0;
        for (int column = gridCell.getColumn(); column < gridLayout.getColumns(); column++) {
            GridArea gridArea = gridLayout.getGridArea(column, gridCell.getRow());
            if (!gridCell.equals(gridArea.getComponent())) {
                if (!isEmptyGridArea(gridArea) || isGridExpanded(gridArea)) {
                    break;
                }
            }
            availableColumns++;
        }
        return availableColumns;
    }

    public static int availableRows(GridLayout gridLayout, GridCellLayout gridCell) {
        int availableRows = 0;
        for (int row = gridCell.getRow(); row < gridLayout.getRows(); row++) {
            GridArea gridArea = gridLayout.getGridArea(gridCell.getColumn(), row);
            if (!gridCell.equals(gridArea.getComponent())) {
                if (!isEmptyGridArea(gridArea) || isGridExpanded(gridArea)) {
                    break;
                }
            }
            availableRows++;
        }
        return availableRows;
    }

    public static boolean isEmptyGridArea(GridArea gridArea) {
        return gridArea.getComponent().getChildren().isEmpty();
    }

    public static boolean isGridExpanded(GridArea gridArea) {
        Integer col1 = gridArea.getCol();
        Integer col2 = gridArea.getCol2() != null ? gridArea.getCol2() : gridArea.getCol();
        Integer row1 = gridArea.getRow();
        Integer row2 = gridArea.getRow2() != null ? gridArea.getRow2() : gridArea.getRow();
        return !(col1.equals(col2) && row1.equals(row2));
    }

    public static boolean isParentResponsiveLayout(DashboardLayout layout) {
        return layout.getParent() instanceof ResponsiveLayout;
    }


}
