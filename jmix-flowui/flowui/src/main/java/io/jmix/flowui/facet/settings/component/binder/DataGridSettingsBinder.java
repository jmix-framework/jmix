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
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import io.jmix.core.JmixOrder;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.data.ContainerDataUnit;
import io.jmix.flowui.data.grid.DataGridItems;
import io.jmix.flowui.facet.settings.Settings;
import io.jmix.flowui.facet.settings.component.DataGridSettings;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.HasLoader;
import org.springframework.core.annotation.Order;

import java.util.List;
import java.util.Objects;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;

@Order(JmixOrder.LOWEST_PRECEDENCE)
@org.springframework.stereotype.Component("flowui_DataGridSettingsBinder")
public class DataGridSettingsBinder implements DataLoadingSettingsBinder<DataGrid<?>, DataGridSettings> {

    @Override
    public Class<? extends Component> getComponentClass() {
        return DataGrid.class;
    }

    @Override
    public Class<? extends Settings> getSettingsClass() {
        return DataGridSettings.class;
    }

    @Override
    public void applySettings(DataGrid<?> component, DataGridSettings settings) {
        if (isEmpty(settings.getColumns())) {
            return;
        }

        // todo rp set columns order and consider hidden columns by security
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void applyDataLoadingSettings(DataGrid<?> component, DataGridSettings settings) {
        if (!isDataLoadingSettingsEnabled(component)
                || isEmpty(settings.getSortOrder())) {
            return;
        }

        List sortOrder = settings.getSortOrder().stream()
                .map(sSortOrder -> new GridSortOrder<>(
                        component.getColumnByKey(sSortOrder.getKey()),
                        SortDirection.valueOf(sSortOrder.getSortDirection())))
                .toList();

        if (component.getItems() instanceof DataGridItems.Sortable) {
            ((DataGridItems.Sortable<?>) component.getItems()).suppressSorting();
        }
        try {
            component.sort(sortOrder);
        } finally {
            if (component.getItems() instanceof DataGridItems.Sortable) {
                ((DataGridItems.Sortable<?>) component.getItems()).enableSorting();
            }
        }
    }

    @Override
    public boolean saveSettings(DataGrid<?> component, DataGridSettings settings) {
        boolean changed = false;

        List<? extends GridSortOrder<?>> sortOrder = component.getSortOrder();
        if (isColumnSortOrderChanged(sortOrder, settings.getSortOrder())) {
            setSortOrderToSettings(sortOrder, settings);
            changed = true;
        }
        List<? extends Grid.Column<?>> componentColumns = component.getAllColumns();
        if (isColumnSettingsChanged(componentColumns, settings.getColumns())) {
            setColumnsToSettings(componentColumns, settings);
            changed = true;
        }

        return changed;
    }

    @Override
    public DataGridSettings getSettings(DataGrid<?> component) {
        DataGridSettings settings = createSettings();
        settings.setId(component.getId().orElse(null));

        setSortOrderToSettings(component.getSortOrder(), settings);
        setColumnsToSettings(component.getAllColumns(), settings);

        return settings;
    }

    protected boolean isDataLoadingSettingsEnabled(DataGrid<?> dataGrid) {
        DataGridItems<?> items = dataGrid.getItems();
        if (items instanceof ContainerDataUnit) {
            CollectionContainer<?> container = ((ContainerDataUnit<?>) items).getContainer();
            return container instanceof HasLoader
                    && ((HasLoader) container).getLoader() instanceof CollectionLoader;
        }
        return false;
    }

    protected boolean isColumnSortOrderChanged(List<? extends GridSortOrder<?>> componentSortOrder,
                                               List<DataGridSettings.SortOrder> settingsSortOrder) {
        if (isEmpty(componentSortOrder) && isEmpty(settingsSortOrder)) {
            return false;
        }
        if (isEmpty(componentSortOrder) || isEmpty(settingsSortOrder)) {
            return true;
        }

        if (componentSortOrder.size() != settingsSortOrder.size()) {
            return true;
        }
        for (int i = 0; i < componentSortOrder.size(); i++) {
            GridSortOrder<?> sortOrder = componentSortOrder.get(i);
            String key = sortOrder.getSorted().getKey();

            DataGridSettings.SortOrder sSortOrder = settingsSortOrder.get(i);

            if (!key.equals(sSortOrder.getKey())) {
                return true;
            }
            if (!sortOrder.getDirection().name().equals(sSortOrder.getSortDirection())) {
                return true;
            }
        }
        return false;
    }

    protected void setSortOrderToSettings(List<? extends GridSortOrder<?>> sortOrder, DataGridSettings settings) {
        if (isEmpty(sortOrder)) {
            settings.setSortOrder(null);
            return;
        }

        List<DataGridSettings.SortOrder> settingsSortOrder = sortOrder.stream()
                .map(cSortOrder -> {
                    DataGridSettings.SortOrder sSortOrder = new DataGridSettings.SortOrder();
                    sSortOrder.setKey(cSortOrder.getSorted().getKey());
                    sSortOrder.setSortDirection(cSortOrder.getDirection().name());
                    return sSortOrder;
                }).toList();

        settings.setSortOrder(settingsSortOrder);
    }

    protected boolean isColumnSettingsChanged(List<? extends Grid.Column<?>> componentColumns,
                                              List<DataGridSettings.Column> settingsColumns) {
        if (isEmpty(componentColumns) && isEmpty(settingsColumns)) {
            return false;
        }
        if (isEmpty(componentColumns) || isEmpty(settingsColumns)) {
            return true;
        }

        if (componentColumns.size() != settingsColumns.size()) {
            return true;
        }
        for (int i = 0; i < componentColumns.size(); i++) {
            Grid.Column<?> column = componentColumns.get(i);
            DataGridSettings.Column sColumn = settingsColumns.get(i);

            if (!Objects.equals(column.getKey(), sColumn.getKey())) {
                return true;
            }
            if (!Objects.equals(column.getWidth(), sColumn.getWidth())) {
                return true;
            }
            if (!Objects.equals(column.isVisible(), sColumn.isVisible())) {
                return true;
            }
        }
        return false;
    }

    protected void setColumnsToSettings(List<? extends Grid.Column<?>> componentColumns, DataGridSettings settings) {
        if (isEmpty(componentColumns)) {
            settings.setColumns(null);
            return;
        }

        List<DataGridSettings.Column> settingsColumns = componentColumns.stream().
                map(column -> {
                    DataGridSettings.Column sColumn = new DataGridSettings.Column();
                    sColumn.setKey(column.getKey());
                    sColumn.setWidth(column.getWidth());
                    sColumn.setVisible(column.isVisible());
                    return sColumn;
                }).toList();

        settings.setColumns(settingsColumns);
    }

    protected DataGridSettings createSettings() {
        return new DataGridSettings();
    }
}
