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

import io.jmix.core.Metadata;
import io.jmix.dashboards.model.DashboardModel;
import io.jmix.dashboards.model.Widget;
import io.jmix.dashboards.model.visualmodel.*;
import io.jmix.dashboards.utils.DashboardLayoutManager;
import io.jmix.dashboardsui.dashboard.event.DashboardRefreshEvent;
import io.jmix.dashboardsui.dashboard.event.WidgetDropLocation;
import io.jmix.dashboardsui.dashboard.event.WidgetSelectedEvent;
import io.jmix.dashboardsui.screen.dashboard.editor.PersistentDashboardEdit;
import io.jmix.dashboardsui.screen.dashboard.editor.css.CssLayoutCreationDialog;
import io.jmix.dashboardsui.screen.dashboard.editor.grid.GridCreationDialog;
import io.jmix.dashboardsui.screen.dashboard.editor.responsive.ResponsiveCreationDialog;
import io.jmix.dashboardsui.screen.widget.WidgetEdit;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.UiEventPublisher;
import io.jmix.ui.component.Frame;
import io.jmix.ui.component.Window;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.OpenMode;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.StandardCloseAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

import static io.jmix.dashboards.utils.DashboardLayoutUtils.*;

@Component("dshbrd_DropLayoutTools")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DropLayoutTools {
    protected PersistentDashboardEdit dashboardEdit;

    protected InstanceContainer<DashboardModel> dashboardDc;
    @Autowired
    private Metadata metadata;
    @Autowired
    private UiEventPublisher uiEventPublisher;
    @Autowired
    private ScreenBuilders screenBuilders;

    @Autowired
    private DashboardLayoutManager layoutManager;

    protected DropLayoutTools(PersistentDashboardEdit dashboardEditor, InstanceContainer<DashboardModel> dashboardDc) {
        this.dashboardEdit = dashboardEditor;
        this.dashboardDc = dashboardDc;
    }

    public DashboardModel getDashboard() {
        return dashboardEdit.getDashboardModel();
    }

    public void addComponent(DashboardLayout layout, UUID targetLayoutUuid, WidgetDropLocation location) {
        DashboardLayout targetLayout = findLayout(getDashboard().getVisualModel(), targetLayoutUuid);
        if (layout instanceof CssLayout) {
            Screen screen = createScreen(CssLayoutCreationDialog.class);
            screen.addAfterCloseListener(e -> {
                StandardCloseAction closeAction = (StandardCloseAction) e.getCloseAction();
                if (Window.COMMIT_ACTION_ID.equals(closeAction.getActionId())) {
                    CssLayoutCreationDialog dialog = (CssLayoutCreationDialog) e.getSource();
                    CssLayout cssLayout = layoutManager.createCssLayout(dialog.getResponsive(), dialog.getCssStyleName());
                    reorderWidgetsAndPushEvents(cssLayout, targetLayout, location);
                }
            });
        }
        if (layout instanceof GridLayout) {
            Screen screen = createScreen(GridCreationDialog.class);
            screen.addAfterCloseListener(e -> {
                StandardCloseAction closeAction = (StandardCloseAction) e.getCloseAction();
                if (Window.COMMIT_ACTION_ID.equals(closeAction.getActionId())) {
                    GridCreationDialog dialog = (GridCreationDialog) e.getSource();
                    GridLayout gridLayout = layoutManager.createGridLayout(dialog.getCols(), dialog.getRows());
                    reorderWidgetsAndPushEvents(gridLayout, targetLayout, location);
                }
            });
        } else if (layout instanceof WidgetTemplateLayout) {
            WidgetLayout widgetLayout = layoutManager.createWidgetLayout(((WidgetTemplateLayout) layout).getWidget(), true);
            reorderWidgetsAndPushEvents(widgetLayout, targetLayout, location);
        } else if (layout instanceof WidgetLayout) {
            WidgetLayout widgetLayout = layoutManager.createWidgetLayout(((WidgetLayout) layout).getWidget(), false);
            screenBuilders.editor(Widget.class, dashboardEdit)
                    .newEntity(widgetLayout.getWidget())
                    .withOpenMode(OpenMode.DIALOG)
                    .build()
                    .show()
                    .addAfterCloseListener(e -> {
                        StandardCloseAction closeAction = (StandardCloseAction) e.getCloseAction();
                        if (Window.COMMIT_ACTION_ID.equals(closeAction.getActionId())) {
                            widgetLayout.setWidget(((WidgetEdit) e.getSource()).getEditedEntity());
                            reorderWidgetsAndPushEvents(widgetLayout, targetLayout, location);
                        }
                    });
        } else if (layout instanceof VerticalLayout) {
            reorderWidgetsAndPushEvents(metadata.create(VerticalLayout.class), targetLayout, location);
        } else if (layout instanceof HorizontalLayout) {
            reorderWidgetsAndPushEvents(metadata.create(HorizontalLayout.class), targetLayout, location);
        } else if (layout instanceof ResponsiveLayout) {
            Screen screen = createScreen(ResponsiveCreationDialog.class);
            screen.addAfterCloseListener(e -> {
                StandardCloseAction closeAction = (StandardCloseAction) e.getCloseAction();
                ResponsiveCreationDialog dialog = (ResponsiveCreationDialog) e.getSource();
                if (Window.COMMIT_ACTION_ID.equals(closeAction.getActionId())) {
                    ResponsiveLayout responsiveLayout = layoutManager.createResponsiveLayout(dialog.getXs(), dialog.getSm(), dialog.getMd(), dialog.getLg());
                    reorderWidgetsAndPushEvents(responsiveLayout, targetLayout, location);
                }
            });
        }
    }

    private Screen createScreen(Class<? extends Screen> screenClass) {
        return screenBuilders.screen(dashboardEdit)
                .withScreenClass(screenClass)
                .withOpenMode(OpenMode.DIALOG)
                .build()
                .show();
    }

    private void reorderWidgetsAndPushEvents(DashboardLayout layout, DashboardLayout targetLayout, WidgetDropLocation location) {
        DashboardLayout parentLayout = targetLayout instanceof WidgetLayout ?
                findParentLayout(getDashboard().getVisualModel(), targetLayout) : targetLayout;
        addChild(parentLayout, layout);
        layout.setParent(parentLayout);
        moveComponent(layout, targetLayout.getId(), location);
        uiEventPublisher.publishEvent(new DashboardRefreshEvent(getDashboard().getVisualModel()));
        uiEventPublisher.publishEvent(new WidgetSelectedEvent(layout.getId(), WidgetSelectedEvent.Target.CANVAS));
    }

    public void moveComponent(DashboardLayout layout, UUID targetLayoutId, WidgetDropLocation location) {
        RootLayout dashboardModel = getDashboard().getVisualModel();
        DashboardLayout target = findLayout(dashboardModel, targetLayoutId);
        DashboardLayout parent = findParentLayout(dashboardModel, layout);

        if (!applyMoveAction(layout, target, dashboardModel)) {
            return;
        }

        if (location == null) {
            if (target.equals(parent)) {
                return;
            }
            location = WidgetDropLocation.MIDDLE;
            if (parent.equals(target.getParent()) && target instanceof WidgetLayout) {
                Integer targetIndex = parent.getChildren().indexOf(target);
                Integer sourceIndex = parent.getChildren().indexOf(layout);
                if (sourceIndex - targetIndex == 1) {
                    location = WidgetDropLocation.LEFT;
                }
            }
        }

        parent.removeOwnChild(layout);

        if (target instanceof ContainerLayout) {
            switch (location) {
                case MIDDLE:
                case CENTER:
                    addChild(target, layout);
                    break;
                case BOTTOM:
                case RIGHT:
                    List<DashboardLayout> newChildren = new ArrayList<>();
                    newChildren.add(layout);
                    newChildren.addAll(parent.getChildren());
                    setChildren(parent, newChildren);
                    break;
                case TOP:
                case LEFT:
                    newChildren = new ArrayList<>(parent.getChildren());
                    newChildren.add(layout);
                    setChildren(parent, newChildren);
                    break;
            }
        }
        if (target instanceof WidgetLayout) {
            List<DashboardLayout> newChildren = new ArrayList<>();
            DashboardLayout targetParent = findParentLayout(dashboardModel, target);
            for (DashboardLayout childLayout : targetParent.getChildren()) {
                if (childLayout.getId().equals(target.getId())) {
                    switch (location) {
                        case TOP:
                        case LEFT:
                            newChildren.add(layout);
                            newChildren.add(childLayout);
                            break;
                        case MIDDLE:
                        case CENTER:
                        case BOTTOM:
                        case RIGHT:
                            newChildren.add(childLayout);
                            newChildren.add(layout);
                            break;
                    }
                } else {
                    newChildren.add(childLayout);
                }
            }
            setChildren(targetParent, newChildren);
        }
    }

    public Frame getFrame() {
        return dashboardEdit.getWindow();
    }

    private boolean applyMoveAction(DashboardLayout layout, DashboardLayout target, DashboardLayout dashboardModel) {
        // if was dropped to itself
        if (target.getId().equals(layout.getId())) {
            return false;
        }

        // if parent was dropped to its children
        List<DashboardLayout> targetParents = findParentsLayout(dashboardModel, target.getId());
        if (targetParents.contains(layout)) {
            return false;
        }

        if (layout instanceof GridCellLayout) {
            return false;
        }
        return true;
    }

    private void addChild(DashboardLayout parent, DashboardLayout child) {
        if (parent instanceof ResponsiveLayout) {
            ((ResponsiveLayout) parent).addArea(layoutManager.createResponsiveArea(child));
        } else {
            parent.addChild(child);
        }
    }

    private void setChildren(DashboardLayout parent, List<DashboardLayout> newChildren) {
        if (parent instanceof ResponsiveLayout) {
            int order = 1;
            Set<ResponsiveArea> newAreas = new HashSet<>();
            for (DashboardLayout layout : newChildren) {
                ResponsiveArea area = ((ResponsiveLayout) parent).findArea(layout);
                if (area != null) {
                    area.setOrder(order);
                } else {
                    area = layoutManager.createResponsiveArea(layout);
                    area.setOrder(order);
                }
                newAreas.add(area);
                order++;
            }
            ((ResponsiveLayout) parent).setAreas(newAreas);
        } else {
            parent.setChildren(newChildren);
        }
    }
}
