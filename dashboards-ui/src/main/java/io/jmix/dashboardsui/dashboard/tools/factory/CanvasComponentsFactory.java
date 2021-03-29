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

import io.jmix.dashboards.model.visualmodel.*;
import io.jmix.dashboardsui.component.CanvasLayout;
import io.jmix.dashboardsui.component.impl.*;
import io.jmix.dashboardsui.dashboard.tools.factory.impl.CanvasDropComponentsFactory;
import io.jmix.dashboardsui.dashboard.tools.factory.impl.CanvasUiComponentsFactory;
import io.jmix.dashboardsui.screen.dashboard.editor.canvas.CanvasFragment;

/**
 * Contains the methods to create {@link CanvasLayout} components.
 *
 * @see CanvasUiComponentsFactory
 * @see CanvasDropComponentsFactory
 */
public interface CanvasComponentsFactory {

    /**
     * Creates a {@link CanvasVerticalLayout} component with specified {@link VerticalLayout} model.
     *
     * @param verticalLayout vertical layout model
     * @return created layout
     */
    CanvasVerticalLayout createCanvasVerticalLayout(VerticalLayout verticalLayout);

    /**
     * Creates a {@link CanvasHorizontalLayout} component with specified {@link HorizontalLayout} model.
     *
     * @param horizontalLayout horizontal layout model
     * @return created layout
     */
    CanvasHorizontalLayout createCanvasHorizontalLayout(HorizontalLayout horizontalLayout);

    /**
     * Creates a {@link CanvasCssLayout} component with specified {@link CssLayout} model.
     *
     * @param cssLayoutModel CSS layout model
     * @return created layout
     */
    CanvasCssLayout createCssLayout(CssLayout cssLayoutModel);

    /**
     * Creates a {@link CanvasGridLayout} component with specified {@link GridLayout} model.
     *
     * @param gridLayout grid layout model
     * @return created layout
     */
    CanvasGridLayout createCanvasGridLayout(GridLayout gridLayout);

    /**
     * Creates a {@link CanvasWidgetLayout} with specified {@link WidgetLayout} model.
     *
     * @param fragment canvas to add a widget fragment
     * @param widgetLayout widget layout model
     * @return created layout
     */
    CanvasWidgetLayout createCanvasWidgetLayout(CanvasFragment fragment, WidgetLayout widgetLayout);

    /**
     * Creates a {@link CanvasRootLayout} with specified {@link RootLayout} model.
     *
     * @param rootLayout root layout model
     * @return created layout
     */
    CanvasRootLayout createCanvasRootLayout(RootLayout rootLayout);

    /**
     * Creates a {@link CanvasResponsiveLayout} with specified {@link ResponsiveLayout} model.
     *
     * @param responsiveLayout responsive layout model
     * @return created layout
     */
    CanvasResponsiveLayout createCanvasResponsiveLayout(ResponsiveLayout responsiveLayout);
}
