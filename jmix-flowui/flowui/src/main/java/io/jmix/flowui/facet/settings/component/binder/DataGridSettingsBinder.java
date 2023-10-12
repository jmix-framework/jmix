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
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.data.grid.DataGridItems;
import io.jmix.flowui.facet.settings.Settings;
import io.jmix.flowui.facet.settings.component.DataGridSettings;
import org.springframework.core.annotation.Order;
import org.springframework.lang.Nullable;

import java.util.List;

@Order(JmixOrder.LOWEST_PRECEDENCE)
@org.springframework.stereotype.Component("flowui_DataGridSettingsBinder")
public class DataGridSettingsBinder extends AbstractGridSettingsBinder<DataGrid<?>, DataGridSettings> {

    @Override
    public Class<? extends Component> getComponentClass() {
        return DataGrid.class;
    }

    @Override
    public Class<? extends Settings> getSettingsClass() {
        return DataGridSettings.class;
    }

    @Override
    protected DataGridSettings createSettings() {
        return new DataGridSettings();
    }

    @Nullable
    @Override
    protected DataGridItems<?> getGridItems(DataGrid<?> grid) {
        return grid.getItems();
    }

    @Override
    protected List<? extends Grid.Column<?>> getAllColumns(DataGrid<?> grid) {
        return grid.getAllColumns();
    }
}
