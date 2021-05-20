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

package io.jmix.dashboardsui.dashboard.tools.factory.impl;

import com.vaadin.shared.ui.dnd.EffectAllowed;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.dnd.DragSourceExtension;
import com.vaadin.ui.dnd.DropTargetExtension;
import io.jmix.core.Metadata;
import io.jmix.dashboards.model.visualmodel.*;
import io.jmix.dashboards.model.visualmodel.CssLayout;
import io.jmix.dashboards.model.visualmodel.GridLayout;
import io.jmix.dashboardsui.DashboardStyleConstants;
import io.jmix.dashboardsui.component.CanvasLayout;
import io.jmix.dashboardsui.component.impl.*;
import io.jmix.dashboardsui.dashboard.event.WidgetAddedEvent;
import io.jmix.dashboardsui.dashboard.event.WidgetDropLocation;
import io.jmix.dashboardsui.dashboard.event.WidgetMovedEvent;
import io.jmix.dashboardsui.dashboard.event.WidgetSelectedEvent;
import io.jmix.dashboardsui.dashboard.tools.factory.ActionsProvider;
import io.jmix.dashboardsui.screen.dashboard.editor.canvas.CanvasFragment;
import io.jmix.ui.UiComponents;
import io.jmix.ui.UiEventPublisher;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.*;
import io.jmix.ui.widget.JmixCssActionsLayout;
import org.springframework.beans.factory.annotation.Autowired;;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * Creates a layout to use in the dashboard editor. <br>
 * Additional fields are set for the layout:
 * <ul>
 *     <li>Description;</li>
 *     <li>Actions; </li>
 *     <li>Click listener (only for {@link CanvasRootLayout}.</li>
 * </ul>
 */
@org.springframework.stereotype.Component("dshbrd_CanvasDropComponentsFactory")
public class CanvasDropComponentsFactory extends CanvasUiComponentsFactory {

    @Autowired
    protected UiComponents factory;

    @Autowired
    protected UiEventPublisher uiEventPublisher;

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected ActionsProvider actionsProvider;

    @Override
    public CanvasVerticalLayout createCanvasVerticalLayout(VerticalLayout verticalLayout) {
        CanvasVerticalLayout layout = super.createCanvasVerticalLayout(verticalLayout);
        initLayout(verticalLayout, layout);
        return layout;
    }

    private void initLayout(DashboardLayout layoutModel, AbstractCanvasLayout layout) {
        layout.addStyleName(DashboardStyleConstants.DASHBOARD_SHADOW_BORDER);
        layout.setDescription(layoutModel.getCaption());
        createBaseLayoutActions(layout, layoutModel);
        initDragExtension(layoutModel, layout);
        initDropExtension(layout);
    }

    private void initDragExtension(DashboardLayout layoutModel, AbstractCanvasLayout layout) {
        DragSourceExtension<com.vaadin.ui.CssLayout> dragSourceExtension = new DragSourceExtension<>(layout.unwrap(com.vaadin.ui.CssLayout.class));
        dragSourceExtension.setEffectAllowed(EffectAllowed.MOVE);
        dragSourceExtension.addDragStartListener(e -> dragSourceExtension.setDragData(layoutModel));
        dragSourceExtension.addDragEndListener(e -> dragSourceExtension.setDragData(null));
    }

    private void initDropExtension(AbstractCanvasLayout layout) {
        DropTargetExtension<JmixCssActionsLayout> dropTarget = new DropTargetExtension(layout.unwrap(JmixCssActionsLayout.class));
        dropTarget.addDropListener(this::onDrop);
    }

    private void onDrop(com.vaadin.ui.dnd.event.DropEvent<JmixCssActionsLayout> e) {
        if (e.getDragData().isPresent()) {
            DashboardLayout source = (DashboardLayout) e.getDragData().get();
            AbstractComponent targetComponent = e.getComponent();
            UUID targetLayoutId = targetComponent.getId() != null ? UUID.fromString(targetComponent.getId()) : null;
            if (targetLayoutId != null) {
                if (source.getId() == null) {
                    uiEventPublisher.publishEvent(new WidgetAddedEvent(source, targetLayoutId, WidgetDropLocation.MIDDLE));
                } else {
                    uiEventPublisher.publishEvent(new WidgetMovedEvent(source, targetLayoutId, (WidgetDropLocation) null));
                }
            }
        }
    }

    protected Button createButton(Action action) {
        Button removeButton = factory.create(Button.class);
        removeButton.setAction(action);
        removeButton.addStyleName(DashboardStyleConstants.DASHBOARD_EDIT_BUTTON);
        removeButton.setIcon(action.getIcon());
        removeButton.setCaption("");
        removeButton.setDescription(action.getCaption());
        return removeButton;
    }

    @Override
    public CanvasHorizontalLayout createCanvasHorizontalLayout(HorizontalLayout horizontalLayout) {
        CanvasHorizontalLayout layout = super.createCanvasHorizontalLayout(horizontalLayout);
        initLayout(horizontalLayout, layout);
        return layout;
    }

    @Override
    public CanvasCssLayout createCssLayout(CssLayout cssLayoutModel) {
        CanvasCssLayout layout = super.createCssLayout(cssLayoutModel);
        initLayout(cssLayoutModel, layout);
        return layout;
    }

    @Override
    public CanvasGridLayout createCanvasGridLayout(GridLayout gridLayout) {
        CanvasGridLayout layout = super.createCanvasGridLayout(gridLayout);
        initLayout(gridLayout, layout);
        return layout;
    }

    private void createBaseLayoutActions(CanvasLayout canvasLayout, DashboardLayout layout) {
        HBoxLayout buttonsPanel = canvasLayout.createButtonsPanel();
        buttonsPanel.addStyleName(DashboardStyleConstants.DASHBOARD_LAYOUT_CONTROLS);

        List<Action> actions = actionsProvider.getLayoutActions(layout);
        for (Action action : actions) {
            Button button = createButton(action);
            buttonsPanel.add(button);
        }
        Button captionButton = createCaptionButton(layout);
        buttonsPanel.add(captionButton);
    }

    @Override
    public CanvasWidgetLayout createCanvasWidgetLayout(CanvasFragment canvasFragment, WidgetLayout widgetLayout) {
        CanvasWidgetLayout layout = super.createCanvasWidgetLayout(canvasFragment, widgetLayout);
        initLayout(widgetLayout, layout);
        return layout;

    }

    @Override
    public CanvasRootLayout createCanvasRootLayout(RootLayout rootLayout) {
        CanvasRootLayout layout = super.createCanvasRootLayout(rootLayout);
        initLayout(rootLayout, layout);
        addLayoutClickListener(layout);
        return layout;
    }

    protected Button createCaptionButton(DashboardLayout layout) {
        Button captionButton = factory.create(Button.class);
        captionButton.addStyleName(DashboardStyleConstants.DASHBOARD_EDIT_BUTTON);
        captionButton.setCaption(layout.getCaption());
        return captionButton;
    }

    protected void addLayoutClickListener(CanvasLayout layout) {
        layout.addLayoutClickListener(e -> {
            CanvasLayout selectedLayout = null;
            Component clickedComponent = e.getClickedComponent();
            if (clickedComponent == null) {
                HasComponents source = e.getSource();
                if (source instanceof ComponentContainer) {
                    Component parent = ((ComponentContainer) source).getParent();
                    if (parent instanceof CanvasLayout) {
                        selectedLayout = (CanvasLayout) parent;
                    }
                }
            } else {
                selectedLayout = findCanvasLayout(clickedComponent);
            }

            if (selectedLayout != null) {
                uiEventPublisher.publishEvent(new WidgetSelectedEvent(selectedLayout.getUuid(), WidgetSelectedEvent.Target.CANVAS));
            }
        });
    }

    @Nullable
    protected CanvasLayout findCanvasLayout(Component component) {
        if (component instanceof CanvasLayout) {
            return (CanvasLayout) component;
        } else {
            Component parent = component.getParent();
            return parent != null ? findCanvasLayout(parent) : null;
        }
    }

    @Override
    public CanvasResponsiveLayout createCanvasResponsiveLayout(ResponsiveLayout responsiveLayout) {
        CanvasResponsiveLayout layout = super.createCanvasResponsiveLayout(responsiveLayout);
        initLayout(responsiveLayout, layout);
        return layout;
    }
}
