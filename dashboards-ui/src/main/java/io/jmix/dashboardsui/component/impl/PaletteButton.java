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

import io.jmix.dashboards.model.visualmodel.DashboardLayout;
import io.jmix.dashboardsui.component.Draggable;
import io.jmix.ui.component.impl.ButtonImpl;

public class PaletteButton extends ButtonImpl implements Draggable {

    public static final String NAME = "paletteButton";

    protected DashboardLayout layout;

    public DashboardLayout getLayout() {
        return layout;
    }

    public void setLayout(DashboardLayout layout) {
        this.layout = layout;
    }
}
