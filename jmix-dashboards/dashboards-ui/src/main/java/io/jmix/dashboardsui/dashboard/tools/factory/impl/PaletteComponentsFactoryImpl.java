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
import com.vaadin.ui.dnd.DragSourceExtension;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.dashboards.entity.WidgetTemplate;
import io.jmix.dashboards.model.Widget;
import io.jmix.dashboards.model.visualmodel.*;
import io.jmix.dashboardsui.DashboardIcon;
import io.jmix.dashboardsui.DashboardStyleConstants;
import io.jmix.dashboardsui.component.impl.PaletteButton;
import io.jmix.dashboards.converter.JsonConverter;
import io.jmix.dashboardsui.dashboard.tools.WidgetUtils;
import io.jmix.dashboardsui.dashboard.tools.factory.PaletteComponentsFactory;
import io.jmix.dashboardsui.repository.WidgetRepository;
import io.jmix.ui.UiComponents;
import io.jmix.ui.widget.JmixButton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("dshbrd_PaletteComponentsFactory")
public class PaletteComponentsFactoryImpl implements PaletteComponentsFactory {

    @Autowired
    protected UiComponents factory;

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected Messages messages;

    @Autowired
    protected JsonConverter converter;

    @Autowired
    protected WidgetUtils widgetUtils;

    @Autowired
    protected WidgetRepository widgetRepository;

    public PaletteButton createVerticalLayoutButton() {
        PaletteButton button = createCommonButton();
        button.setCaption(messages.getMessage(PaletteComponentsFactoryImpl.class,"verticalLayout"));
        button.setIconFromSet(DashboardIcon.VERTICAL_LAYOUT_ICON);
        button.setLayout(metadata.create(VerticalLayout.class));
        button.getLayout().setId(null);
        button.setDescription(messages.getMessage(PaletteComponentsFactoryImpl.class, "verticalLayout"));
        return button;
    }

    public PaletteButton createHorizontalLayoutButton() {
        PaletteButton button = createCommonButton();
        button.setCaption(messages.getMessage(PaletteComponentsFactoryImpl.class,"horizontalLayout"));
        button.setIconFromSet(DashboardIcon.HORIZONTAL_LAYOUT_ICON);
        button.setLayout(metadata.create(HorizontalLayout.class));
        button.getLayout().setId(null);
        button.setDescription(messages.getMessage(PaletteComponentsFactoryImpl.class,"horizontalLayout"));
        return button;
    }

    public PaletteButton createGridLayoutButton() {
        PaletteButton button = createCommonButton();
        button.setCaption(messages.getMessage(PaletteComponentsFactoryImpl.class,"gridLayout"));
        button.setIconFromSet(DashboardIcon.GRID_LAYOUT_ICON);
        button.setLayout(metadata.create(GridLayout.class));
        button.getLayout().setId(null);
        button.setDescription(messages.getMessage(PaletteComponentsFactoryImpl.class,"gridLayout"));
        return button;
    }

    @Override
    public PaletteButton createCssLayoutButton() {
        PaletteButton button = createCommonButton();
        button.setCaption(messages.getMessage(PaletteComponentsFactoryImpl.class,"cssLayout"));
        button.setIconFromSet(DashboardIcon.CSS_LAYOUT_ICON);
        button.setLayout(metadata.create(CssLayout.class));
        button.getLayout().setId(null);
        button.setDescription(messages.getMessage(PaletteComponentsFactoryImpl.class,"cssLayout"));
        return button;
    }

    public PaletteButton createWidgetButton(Widget widget) {
        WidgetLayout layout = metadata.create(WidgetLayout.class);
        layout.setWidget(widget);

        PaletteButton button = createCommonButton();

        button.setCaption(widgetRepository.getLocalizedWidgetName(widget));
        button.setDescription(widget.getDescription());
        button.setLayout(layout);
        button.getLayout().setId(null);
        return button;
    }

    public PaletteButton createWidgetTemplateButton(WidgetTemplate wt) {
        Widget widget = converter.widgetFromJson(wt.getWidgetModel());
        widget.setName(widgetUtils.getWidgetType(widget.getFragmentId()));
        WidgetTemplateLayout layout = metadata.create(WidgetTemplateLayout.class);
        layout.setWidget(widget);

        PaletteButton button = createCommonButton();
        button.setCaption(String.format("%s (%s)", wt.getName(), widgetRepository.getLocalizedWidgetName(widget)));
        button.setLayout(layout);
        button.getLayout().setId(null);
        return button;
    }

    protected PaletteButton createCommonButton() {
        PaletteButton button = factory.create(PaletteButton.class);
        button.setWidth("100%");
        button.setHeight("50px");
        button.setStyleName(DashboardStyleConstants.DASHBOARD_BUTTON);
        DragSourceExtension<JmixButton> dragSourceExtension = new DragSourceExtension<>(button.unwrap(JmixButton.class));
        dragSourceExtension.setEffectAllowed(EffectAllowed.COPY);
        dragSourceExtension.addDragStartListener(e -> dragSourceExtension.setDragData(button.getLayout()));
        dragSourceExtension.addDragEndListener(e -> dragSourceExtension.setDragData(null));
        return button;
    }

    @Override
    public PaletteButton createResponsiveLayoutButton() {
        PaletteButton button = createCommonButton();
        button.setCaption(messages.getMessage(PaletteComponentsFactoryImpl.class,"responsiveLayout"));
        button.setIconFromSet(DashboardIcon.CSS_LAYOUT_ICON);
        button.setLayout(metadata.create(ResponsiveLayout.class));
        button.getLayout().setId(null);
        button.setDescription(messages.getMessage(PaletteComponentsFactoryImpl.class,"responsiveLayout"));
        return button;
    }
}
