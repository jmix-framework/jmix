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
import io.jmix.ui.action.Action;

import java.util.List;

/**
 * Provides a list of available actions for dashboard layout.
 * <br>
 * The following actions are available:
 * <ul>
 *     <li>Remove (not available for {@link RootLayout} and {@link GridCellLayout})</li>
 *     <li>Style</li>
 *     <li>Weight (not available for {@link RootLayout}, {@link GridCellLayout}, if parent layout is {@link CssLayout} or is expanded)</li>
 *     <li>Expand (only for {@link VerticalLayout}, {@link HorizontalLayout}, {@link GridCellLayout})</li>
 *     <li>Colspan (only for {@link GridCellLayout})</li>
 *     <li>Edit (only for {@link WidgetLayout})</li>
 *     <li>Template (only for {@link WidgetLayout})</li>
 * </ul>
 *
 */
public interface ActionsProvider {

    /**
     * Provides the actions for specified layout.
     *
     * @param layout dashboard layout
     * @return list of available actions
     */
    List<Action> getLayoutActions(DashboardLayout layout);
}
