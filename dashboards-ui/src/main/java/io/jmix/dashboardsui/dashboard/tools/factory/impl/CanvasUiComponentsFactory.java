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
import io.jmix.core.common.util.ParamsMap;
import io.jmix.dashboards.model.Widget;
import io.jmix.dashboards.model.visualmodel.*;
import io.jmix.dashboardsui.component.CanvasLayout;
import io.jmix.dashboardsui.component.Dashboard;
import io.jmix.dashboardsui.component.impl.*;
import io.jmix.dashboardsui.dashboard.tools.factory.CanvasComponentsFactory;
import io.jmix.dashboardsui.repository.WidgetRepository;
import io.jmix.dashboardsui.repository.WidgetTypeInfo;
import io.jmix.dashboardsui.screen.dashboard.editor.canvas.CanvasFragment;
import io.jmix.ui.AppUI;
import io.jmix.ui.UiComponents;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.Fragment;
import io.jmix.ui.component.Label;
import io.jmix.ui.component.VBoxLayout;
import io.jmix.ui.screen.MapScreenOptions;
import io.jmix.ui.screen.ScreenFragment;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Creates a non-editable {@link CanvasLayout} to be added to the {@link Dashboard} component.
 */
@org.springframework.stereotype.Component("dshbrd_CanvasUiComponentsFactory")
public class CanvasUiComponentsFactory implements CanvasComponentsFactory {

    public static final String WIDGET = "widget";
    public static final String DASHBOARD_MODEL = "dashboardModel";
    public static final String DASHBOARD = "dashboard";

    private static Logger log = LoggerFactory.getLogger(CanvasUiComponentsFactory.class);

    @Autowired
    protected WidgetRepository widgetRepository;

    @Autowired
    protected UiComponents components;

    @Autowired
    protected Messages messages;

    @Override
    public CanvasVerticalLayout createCanvasVerticalLayout(VerticalLayout verticalLayout) {
        CanvasVerticalLayout layout = components.create(CanvasVerticalLayout.class).init(verticalLayout);
        layout.setUuid(UUID.randomUUID());
        layout.setSizeFull();
        return layout;
    }

    @Override
    public CanvasHorizontalLayout createCanvasHorizontalLayout(HorizontalLayout horizontalLayout) {
        CanvasHorizontalLayout layout = components.create(CanvasHorizontalLayout.class).init(horizontalLayout);
        layout.setUuid(UUID.randomUUID());
        layout.setSizeFull();
        return layout;
    }

    @Override
    public CanvasCssLayout createCssLayout(CssLayout cssLayoutModel) {
        CanvasCssLayout layout = components.create(CanvasCssLayout.class).init(cssLayoutModel);
        layout.setUuid(UUID.randomUUID());
        layout.setSizeFull();
        return layout;
    }

    @Override
    public CanvasGridLayout createCanvasGridLayout(GridLayout gridLayout) {
        CanvasGridLayout layout = components.create(CanvasGridLayout.class).init(gridLayout);
        layout.setUuid(UUID.randomUUID());
        layout.setSizeFull();
        return layout;
    }

    @Override
    public CanvasWidgetLayout createCanvasWidgetLayout(CanvasFragment canvasFragment, WidgetLayout widgetLayout) {
        Widget widget = widgetLayout.getWidget();
        Optional<WidgetTypeInfo> widgetTypeOpt = widgetRepository.getWidgetTypesInfo().stream()
                .filter(widgetType -> StringUtils.equals(widget.getFragmentId(), widgetType.getFragmentId()))
                .findFirst();

        if (!widgetTypeOpt.isPresent()) {
            CanvasWidgetLayout layout = components.create(CanvasWidgetLayout.class).init(widgetLayout);
            Label<String> label = components.create(Label.class);
            String message = messages.formatMessage(CanvasUiComponentsFactory.class, "widgetNotFound", widget.getCaption(), widget.getName());
            label.setValue(message);
            layout.addComponent(label);
            log.error(message);
            return layout;
        }

        widget.setDashboard(canvasFragment.getDashboardModel());

        String fragmentId = widgetTypeOpt.get().getFragmentId();
        Map<String, Object> params = new HashMap<>(ParamsMap.of(
                WIDGET, widget,
                DASHBOARD_MODEL, canvasFragment.getDashboardModel(),
                DASHBOARD, canvasFragment.getDashboard()
        ));
        params.putAll(widgetRepository.getWidgetParams(widget));

        ScreenFragment screenFragment = AppUI.getCurrent().getFragments()
                .create(canvasFragment, fragmentId, new MapScreenOptions(params))
                .init();
        Fragment fragment = screenFragment
                .getFragment();

        fragment.setSizeFull();

        Component widgetComponent = fragment;

        if (BooleanUtils.isTrue(widget.getShowWidgetCaption())) {
            VBoxLayout vBoxLayout = components.create(VBoxLayout.class);
            vBoxLayout.setSpacing(true);
            vBoxLayout.setMargin(true);
            vBoxLayout.setSizeFull();

            Label<String> label = components.create(Label.class);
            label.setValue(widget.getCaption());
            label.setStyleName("h2");
            vBoxLayout.add(label);

            vBoxLayout.add(fragment);
            vBoxLayout.expand(fragment);
            widgetComponent = vBoxLayout;
        } else {
            fragment.setMargin(true);
        }

        CanvasWidgetLayout layout = components.create(CanvasWidgetLayout.class).init(widgetLayout);
        layout.setUuid(UUID.randomUUID());
        layout.addComponent(widgetComponent);
        layout.setWidgetComponent(screenFragment);
        layout.setInnerLayout(widgetComponent);
        layout.setWidget(widget);
        layout.getDelegate().expand(widgetComponent);
        layout.setSizeFull();
        return layout;
    }

    @Override
    public CanvasRootLayout createCanvasRootLayout(RootLayout rootLayout) {
        CanvasRootLayout layout = components.create(CanvasRootLayout.class).init(rootLayout);
        layout.setUuid(UUID.randomUUID());
        return layout;
    }


    @Override
    public CanvasResponsiveLayout createCanvasResponsiveLayout(ResponsiveLayout responsiveLayout) {
        CanvasResponsiveLayout layout = components.create(CanvasResponsiveLayout.class).init(responsiveLayout);
        layout.setUuid(UUID.randomUUID());
        layout.setSizeFull();
        return layout;
    }
}
