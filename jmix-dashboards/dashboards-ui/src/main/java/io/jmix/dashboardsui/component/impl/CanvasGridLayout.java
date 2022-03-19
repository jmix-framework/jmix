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


import io.jmix.dashboards.model.visualmodel.GridLayout;
import io.jmix.ui.component.Component;

public class CanvasGridLayout extends AbstractCanvasLayout {

    public static final String NAME = "canvasGridLayout";

    protected io.jmix.ui.component.GridLayout gridLayout;

    public CanvasGridLayout init(GridLayout model) {
        init(model, io.jmix.ui.component.GridLayout.class);
        gridLayout = (io.jmix.ui.component.GridLayout) delegate;

        gridLayout.setColumns(model.getColumns());
        gridLayout.setRows(model.getRows());
        return this;
    }

    @Override
    public io.jmix.ui.component.GridLayout getDelegate() {
        return gridLayout;
    }

    public void addComponent(Component component, int col, int row, int col2, int row2) {
        gridLayout.add(component, col, row, col2, row2);
    }
}
