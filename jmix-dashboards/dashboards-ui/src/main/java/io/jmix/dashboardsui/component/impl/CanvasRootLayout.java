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

package io.jmix.dashboardsui.component.impl;

import io.jmix.dashboards.model.visualmodel.RootLayout;
import io.jmix.ui.component.VBoxLayout;

import static io.jmix.dashboardsui.DashboardStyleConstants.DASHBOARD_ROOT_LAYOUT;

public class CanvasRootLayout extends AbstractCanvasLayout {

    public static final String NAME = "canvasRootLayout";

    protected VBoxLayout verticalLayout;

    public CanvasRootLayout init(RootLayout model) {
        init(model, VBoxLayout.class);
        verticalLayout = (VBoxLayout) delegate;
        delegate.setStyleName(DASHBOARD_ROOT_LAYOUT);
        return this;
    }

    @Override
    public VBoxLayout getDelegate() {
        return verticalLayout;
    }
}
