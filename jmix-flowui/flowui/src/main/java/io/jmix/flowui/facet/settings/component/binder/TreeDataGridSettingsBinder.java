/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.facet.settings.component.binder;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import io.jmix.core.JmixOrder;
import io.jmix.flowui.component.grid.TreeDataGrid;
import io.jmix.flowui.data.grid.DataGridItems;
import io.jmix.flowui.facet.settings.Settings;
import io.jmix.flowui.facet.settings.component.TreeDataGridSettings;
import org.springframework.core.annotation.Order;

import java.util.List;

@Order(JmixOrder.LOWEST_PRECEDENCE)
@org.springframework.stereotype.Component("ui_TreeDataGridSettingsBinder")
public class TreeDataGridSettingsBinder extends AbstractGridSettingsBinder<TreeDataGrid<?>, TreeDataGridSettings> {

    @Override
    public Class<? extends Component> getComponentClass() {
        return TreeDataGrid.class;
    }

    @Override
    public Class<? extends Settings> getSettingsClass() {
        return TreeDataGridSettings.class;
    }

    @Override
    protected TreeDataGridSettings createSettings() {
        return new TreeDataGridSettings();
    }

    @Override
    protected DataGridItems<?> getGridItems(TreeDataGrid<?> grid) {
        return grid.getItems();
    }

    @Override
    protected List<? extends Grid.Column<?>> getAllColumns(TreeDataGrid<?> grid) {
        return grid.getAllColumns();
    }
}
