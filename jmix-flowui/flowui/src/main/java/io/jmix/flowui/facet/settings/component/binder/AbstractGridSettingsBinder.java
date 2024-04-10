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

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import io.jmix.flowui.data.ContainerDataUnit;
import io.jmix.flowui.data.grid.DataGridItems;
import io.jmix.flowui.facet.settings.component.DataGridSettings;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.HasLoader;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;

public abstract class AbstractGridSettingsBinder<V extends Grid<?>, S extends DataGridSettings>
        implements DataLoadingSettingsBinder<V, S> {

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void applySettings(V component, S settings) {
        if (isEmpty(settings.getColumns())) {
            return;
        }

        List<? extends Grid.Column<?>> componentColumns = getOrderedColumns(component);

        List<String> componentColumnKeys = componentColumns.stream().map(Grid.Column::getKey).toList();
        List<String> settingsColumnKeys = settings.getColumns().stream().map(DataGridSettings.Column::getKey).toList();

        // Checks only size of collections and same elements. It does not consider the order in collections.
        // So settings won't be applied if DataGrid contains columns that are missed in settings.
        if (CollectionUtils.isEqualCollection(componentColumnKeys, settingsColumnKeys)) {
            List<Grid.Column<?>> newColumnsOrder = new ArrayList<>(componentColumnKeys.size());

            for (DataGridSettings.Column sColumn : settings.getColumns()) {
                Grid.Column<?> column = component.getColumnByKey(sColumn.getKey());
                Objects.requireNonNull(column);

                if (sColumn.getWidth() != null) {
                    // Changing the column width manually works only if flexGrow is 0.
                    // If flexGrow is >=1 then the column width is 100px. So we consider that user didn't resize
                    //  the column if its width is 100px. And we keep the flexGrow value in this case.
                    column.setFlexGrow(sColumn.getFlexGrow() > 0 && !"100px".equals(sColumn.getWidth())
                            ? 0
                            : sColumn.getFlexGrow());
                            
                    column.setWidth(sColumn.getWidth());
                }
                if (sColumn.getVisible() != null) {
                    column.setVisible(sColumn.getVisible());
                }
                newColumnsOrder.add(column);
            }
            component.setColumnOrder((List) newColumnsOrder);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void applyDataLoadingSettings(V component, S settings) {
        if (!isDataLoadingSettingsEnabled(component)
                || isEmpty(settings.getSortOrder())) {
            return;
        }

        List sortOrder = settings.getSortOrder().stream()
                .map(sSortOrder -> new GridSortOrder<>(
                        component.getColumnByKey(sSortOrder.getKey()),
                        SortDirection.valueOf(sSortOrder.getSortDirection())))
                .toList();

        DataGridItems<?> gridItems = getGridItems(component);
        if (gridItems instanceof DataGridItems.Sortable) {
            ((DataGridItems.Sortable<?>) gridItems).suppressSorting();
        }
        try {
            component.sort(sortOrder);
        } finally {
            if (gridItems instanceof DataGridItems.Sortable) {
                ((DataGridItems.Sortable<?>) gridItems).enableSorting();
            }
        }
    }

    @Override
    public boolean saveSettings(V component, S settings) {
        boolean changed = false;

        List<? extends GridSortOrder<?>> sortOrder = component.getSortOrder();
        if (isColumnSortOrderChanged(sortOrder, settings.getSortOrder())) {
            setSortOrderToSettings(sortOrder, settings);
            changed = true;
        }
        List<? extends Grid.Column<?>> componentColumns = getOrderedColumns(component);
        if (isColumnSettingsChanged(componentColumns, settings.getColumns())) {
            setColumnsToSettings(componentColumns, settings);
            changed = true;
        }

        return changed;
    }

    @Override
    public S getSettings(V component) {
        S settings = createSettings();
        settings.setId(component.getId().orElse(null));

        setSortOrderToSettings(component.getSortOrder(), settings);
        setColumnsToSettings(getOrderedColumns(component), settings);

        return settings;
    }

    protected abstract S createSettings();

    protected boolean isDataLoadingSettingsEnabled(V grid) {
        DataGridItems<?> items = getGridItems(grid);
        if (items instanceof ContainerDataUnit) {
            CollectionContainer<?> container = ((ContainerDataUnit<?>) items).getContainer();
            return container instanceof HasLoader
                    && ((HasLoader) container).getLoader() instanceof CollectionLoader;
        }
        return false;
    }

    @Nullable
    protected abstract DataGridItems<?> getGridItems(V grid);

    protected boolean isColumnSortOrderChanged(@Nullable List<? extends GridSortOrder<?>> componentSortOrder,
                                               @Nullable List<DataGridSettings.SortOrder> settingsSortOrder) {
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

    protected boolean isColumnSettingsChanged(@Nullable List<? extends Grid.Column<?>> componentColumns,
                                              @Nullable List<DataGridSettings.Column> settingsColumns) {
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

            // Check columns order
            if (!Objects.equals(column.getKey(), sColumn.getKey())) {
                return true;
            }
            if (!Objects.equals(column.getWidth(), sColumn.getWidth())) {
                return true;
            }
            if (!Objects.equals(column.isVisible(), sColumn.getVisible())) {
                return true;
            }
        }
        return false;
    }

    protected void setColumnsToSettings(@Nullable List<? extends Grid.Column<?>> componentColumns,
                                        DataGridSettings settings) {
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
                    sColumn.setFlexGrow(column.getFlexGrow());
                    return sColumn;
                }).toList();

        settings.setColumns(settingsColumns);
    }

    protected List<? extends Grid.Column<?>> getOrderedColumns(V grid) {
        // Gets all (with hidden by security) columns list that has correct order.
        List<? extends Grid.Column<?>> allColumns = getAllColumns(grid);

        // Gets columns that are added to DataGrid, even with visible=false.
        List<? extends Grid.Column<?>> columns = grid.getColumns();

        // We need to save the correct user's order and
        // exclude hidden by security columns.
        return allColumns.stream()
                .filter(columns::contains)
                .toList();
    }

    protected abstract List<? extends Grid.Column<?>> getAllColumns(V grid);
}
