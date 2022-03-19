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

import io.jmix.core.Messages;
import io.jmix.dashboards.model.visualmodel.DashboardLayout;
import io.jmix.dashboards.model.visualmodel.WidgetLayout;
import io.jmix.dashboardsui.DashboardIcon;
import io.jmix.dashboardsui.dashboard.event.CreateWidgetTemplateEvent;
import io.jmix.dashboardsui.dashboard.event.model.*;
import io.jmix.dashboardsui.dashboard.tools.factory.ActionsProvider;
import io.jmix.ui.UiEventPublisher;
import io.jmix.ui.action.AbstractAction;
import io.jmix.ui.action.Action;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static io.jmix.dashboards.utils.DashboardLayoutUtils.*;
import static io.jmix.ui.icon.JmixIcon.*;

@Component("dshbrd_ActionsProvider")
public class ActionsProviderImpl implements ActionsProvider {

    @Autowired
    protected UiEventPublisher uiEventPublisher;

    @Autowired
    private Messages messages;

    @Override
    public List<Action> getLayoutActions(DashboardLayout layout) {
        List<Action> actions = new ArrayList<>();
        if (layout != null) {
            if (!isGridCellLayout(layout) && !isRootLayout(layout)) {
                actions.add(createRemoveAction(layout));
            }
            if (layout instanceof WidgetLayout) {
                WidgetLayout widgetLayout = (WidgetLayout) layout;
                actions.add(createEditAction(widgetLayout));
                actions.add(createTemplateAction(widgetLayout));
            }
            if (!isRootLayout(layout) && !isGridCellLayout(layout) && !isParentCssLayout(layout) && !isParentHasExpand(layout)) {
                actions.add(createWeightAction(layout));
            }
            if (isGridCellLayout(layout)){
                actions.add(createColspanAction(layout));
            }
            if (isLinearLayout(layout)) {
                actions.add(createExpandAction(layout));
            }
            actions.add(createStyleAction(layout));
        }
        return actions;
    }

    private AbstractAction createStyleAction(DashboardLayout layout) {
        AbstractAction styleAction = new AbstractAction("style") {
            @Override
            public void actionPerform(io.jmix.ui.component.Component component) {
                uiEventPublisher.publishEvent(new StyleChangedEvent(layout));
            }
        };
        styleAction.setCaption(messages.getMessage("style"));
        styleAction.setIcon(PAINT_BRUSH.source());
        return styleAction;
    }

    private AbstractAction createExpandAction(DashboardLayout layout) {
        AbstractAction expandAction = new AbstractAction("expand") {
            @Override
            public void actionPerform(io.jmix.ui.component.Component component) {
                uiEventPublisher.publishEvent(new ExpandChangedEvent(layout));
            }
        };

        expandAction.setIcon(EXPAND.source());
        expandAction.setCaption(messages.getMessage("expand"));
        return expandAction;
    }

    private AbstractAction createColspanAction(DashboardLayout layout) {
        AbstractAction weightAction = new AbstractAction("weight") {
            @Override
            public void actionPerform(io.jmix.ui.component.Component component) {
                uiEventPublisher.publishEvent(new ColspanChangedEvent(layout));
            }
        };
        weightAction.setCaption(messages.getMessage("weight"));
        weightAction.setIcon(ARROWS.source());
        return weightAction;
    }

    private AbstractAction createWeightAction(DashboardLayout layout) {
        AbstractAction weightAction = new AbstractAction("weight") {
            @Override
            public void actionPerform(io.jmix.ui.component.Component component) {
                uiEventPublisher.publishEvent(new WeightChangedEvent(layout));
            }
        };
        weightAction.setCaption(messages.getMessage("weight"));
        weightAction.setIcon(ARROWS.source());
        return weightAction;
    }

    private AbstractAction createTemplateAction(WidgetLayout widgetLayout) {
        AbstractAction templateAction = new AbstractAction("template") {
            @Override
            public void actionPerform(io.jmix.ui.component.Component component) {
                uiEventPublisher.publishEvent(new CreateWidgetTemplateEvent(widgetLayout.getWidget()));
            }
        };
        templateAction.setIcon(DATABASE.source());
        templateAction.setCaption(messages.getMessage("template"));
        return templateAction;
    }

    private AbstractAction createEditAction(WidgetLayout widgetLayout) {
        AbstractAction editAction = new AbstractAction("edit") {
            @Override
            public void actionPerform(io.jmix.ui.component.Component component) {
                uiEventPublisher.publishEvent(new WidgetEditEvent(widgetLayout));
            }
        };
        editAction.setIcon(DashboardIcon.GEAR_ICON.source());
        editAction.setCaption(messages.getMessage("edit"));
        return editAction;
    }

    private AbstractAction createRemoveAction(DashboardLayout layout) {
        AbstractAction removeAction = new AbstractAction("remove") {
            @Override
            public void actionPerform(io.jmix.ui.component.Component component) {
                uiEventPublisher.publishEvent(new WidgetRemovedEvent(layout));
            }

        };
        removeAction.setIcon(DashboardIcon.TRASH_ICON.source());
        removeAction.setCaption(messages.getMessage("remove"));
        return removeAction;
    }
}
