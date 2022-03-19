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

import io.jmix.dashboards.model.visualmodel.*;
import io.jmix.dashboardsui.component.CanvasLayout;
import io.jmix.dashboardsui.component.impl.CanvasGridLayout;
import io.jmix.dashboardsui.dashboard.tools.factory.CanvasComponentsFactory;
import io.jmix.dashboardsui.screen.dashboard.editor.canvas.CanvasFragment;
import io.jmix.ui.component.ComponentContainer;
import io.jmix.ui.component.ExpandingLayout;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class DashboardModelConverter {
    protected CanvasComponentsFactory factory;

    public CanvasComponentsFactory getFactory() {
        return factory;
    }

    public void setFactory(CanvasComponentsFactory factory) {
        this.factory = factory;
    }

    public CanvasLayout modelToContainer(CanvasFragment fragment, DashboardLayout model) {
        CanvasLayout canvasLayout = null;
        if (model instanceof RootLayout) {
            canvasLayout = factory.createCanvasRootLayout((RootLayout) model);
            initChildren(fragment, model, (ComponentContainer) canvasLayout.getDelegate());
        } else if (model instanceof VerticalLayout) {
            canvasLayout = factory.createCanvasVerticalLayout((VerticalLayout) model);
            initChildren(fragment, model, (ComponentContainer) canvasLayout.getDelegate());
        } else if (model instanceof HorizontalLayout) {
            canvasLayout = factory.createCanvasHorizontalLayout((HorizontalLayout) model);
            initChildren(fragment, model, (ComponentContainer) canvasLayout.getDelegate());
        } else if (model instanceof CssLayout) {
            CssLayout cssLayoutModel = (CssLayout) model;
            canvasLayout = factory.createCssLayout(cssLayoutModel);
            initChildren(fragment, model, (ComponentContainer) canvasLayout.getDelegate());
        } else if (model instanceof WidgetLayout) {
            canvasLayout = factory.createCanvasWidgetLayout(fragment, ((WidgetLayout) model));
        } else if (model instanceof GridLayout) {
            GridLayout gridModel = (GridLayout) model;
            canvasLayout = factory.createCanvasGridLayout(gridModel);

            for (GridArea area : gridModel.getAreas()) {
                GridCellLayout cellLayout = area.getComponent();
                CanvasLayout childGridCanvas = modelToContainer(fragment, cellLayout);
                Integer col2 = area.getCol() + cellLayout.getColSpan();
                Integer row2 = area.getRow() + cellLayout.getRowSpan();
                ((CanvasGridLayout) canvasLayout).addComponent(childGridCanvas, area.getCol(), area.getRow(), col2, row2);
            }
        } else if (model instanceof ResponsiveLayout) {
            ResponsiveLayout respLayoutModel = (ResponsiveLayout) model;
            canvasLayout = factory.createCanvasResponsiveLayout(respLayoutModel);

            List<ResponsiveArea> sortedAreas = respLayoutModel.getAreas().stream()
                    .sorted(Comparator.comparing(ResponsiveArea::getOrder))
                    .collect(Collectors.toList());

            for (ResponsiveArea area : sortedAreas) {
                DashboardLayout cellLayout = area.getComponent();
                CanvasLayout childGridCanvas = modelToContainer(fragment, cellLayout);
                childGridCanvas.getModel().setParent(respLayoutModel);
                canvasLayout.addComponent(childGridCanvas);
            }
        }

        if (canvasLayout == null) {
            throw new IllegalStateException("Unknown layout class: " + model.getClass());
        }

        if (model.getStyleName() != null) {
            canvasLayout.addStyleName(model.getStyleName());
        }
        canvasLayout.setWidth(model.getWidthWithUnits());
        canvasLayout.setHeight(model.getHeightWithUnits());

        if (model.getId() != null) {
            canvasLayout.setUuid(model.getId());
        }
        return canvasLayout;
    }

    private void initChildren(CanvasFragment fragment, DashboardLayout model, ComponentContainer delegate) {
        boolean expanded = isExpanded(model);
        if (!model.getChildren().isEmpty()) {

            for (DashboardLayout childModel : model.getChildren()) {
                CanvasLayout childContainer = modelToContainer(fragment, childModel);
                delegate.add(childContainer);

                if (childModel.getId().equals(model.getExpand())) {
                    if (delegate instanceof ExpandingLayout) {
                        ((ExpandingLayout) delegate).expand(childContainer);
                    }
                }

                if (!expanded) {
                    childContainer.setWeight(childModel.getWeight());
                }
            }
        }
    }

    private boolean isExpanded(DashboardLayout model) {
        return model.getExpand() != null && model.getChildren().stream()
                .anyMatch(e -> model.getExpand().equals(e.getId()));
    }
}