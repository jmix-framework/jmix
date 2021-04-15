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

package io.jmix.dashboardsui.component;

import io.jmix.dashboards.model.visualmodel.DashboardLayout;
import io.jmix.dashboardsui.dashboard.tools.factory.ActionsProvider;
import io.jmix.ui.component.*;

import java.util.Collection;
import java.util.UUID;

/**
 * UI representation of {@link DashboardLayout} model on the {@link Dashboard} component.
 */
public interface CanvasLayout extends Component, ComponentContainer, LayoutClickNotifier, HasWeight {

    /**
     *
     * @return UI component that contains child components. It can be {@link ComponentContainer} or {@link ResponsiveGridLayout}.
     */
    Component getDelegate();

    /**
     * Add specified component to the layout.
     *
     * @param component component
     */
    void addComponent(Component component);

    /**
     *
     * @return layout components
     */
    Collection<Component> getLayoutComponents();

    /**
     * Creates an empty panel for dashboard layout actions.
     *
     * @see ActionsProvider#getLayoutActions(DashboardLayout)
     *
     * @return empty buttons panel
     */
    HBoxLayout createButtonsPanel();

    /**
     * @return buttons panel
     */
    HBoxLayout getButtonsPanel();

    /**
     * Sets a buttons panel.
     *
     * @param buttonsPanel new buttons panel
     */
    void setButtonsPanel(HBoxLayout buttonsPanel);

    /**
     * @return canvas layout id
     */
    UUID getUuid();

    /**
     * Sets a canvas layout id.
     *
     * @param uuid id
     */
    void setUuid(UUID uuid);

    /**
     * @return dashboard layout model
     */
    DashboardLayout getModel();

}
