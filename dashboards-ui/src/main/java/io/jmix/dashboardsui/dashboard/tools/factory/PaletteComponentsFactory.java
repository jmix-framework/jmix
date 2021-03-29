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

package io.jmix.dashboardsui.dashboard.tools.factory;

import io.jmix.dashboards.entity.WidgetTemplate;
import io.jmix.dashboards.model.Widget;
import io.jmix.dashboards.model.visualmodel.*;
import io.jmix.dashboardsui.component.impl.PaletteButton;

/**
 * Contains the methods to create a {@link PaletteButton} related to {@link DashboardLayout}.
 */
public interface PaletteComponentsFactory {

    /**
     * @return palette button for {@link VerticalLayout}
     */
    PaletteButton createVerticalLayoutButton();

    /**
     * @return palette button for {@link HorizontalLayout}
     */
    PaletteButton createHorizontalLayoutButton();

    /**
     * @return palette button for {@link GridLayout}
     */
    PaletteButton createGridLayoutButton();

    /**
     * @return palette button for {@link CssLayout}
     */
    PaletteButton createCssLayoutButton();

    /**
     * Creates a palette button for specified widget.
     *
     * @param widget widget
     * @return created palette button
     */
    PaletteButton createWidgetButton(Widget widget);

    /**
     * Creates a palette button for specified widget template.
     *
     * @param widgetTemplate widget template.
     * @return created palette button
     */
    PaletteButton createWidgetTemplateButton(WidgetTemplate widgetTemplate);

    /**
     * @return palette button for {@link ResponsiveLayout}
     */
    PaletteButton createResponsiveLayoutButton();
}
