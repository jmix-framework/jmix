/*
 * Copyright 2020 Haulmont.
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

package io.jmix.ui.settings.component.binder;

import com.vaadin.data.provider.GridSortOrder;
import com.vaadin.ui.Grid;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.DataGrid;
import io.jmix.ui.component.data.DataGridItems;
import io.jmix.ui.component.data.meta.ContainerDataUnit;
import io.jmix.ui.component.impl.DataGridSettingsUtils;
import io.jmix.ui.component.impl.DataGridImpl;
import io.jmix.ui.component.impl.WrapperUtils;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.model.HasLoader;
import io.jmix.ui.settings.component.ComponentSettings;
import io.jmix.ui.settings.component.DataGridSettings;
import io.jmix.ui.settings.component.SettingsWrapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("rawtypes")
public abstract class AbstractDataGridSettingsBinder implements DataLoadingSettingsBinder<DataGrid, DataGridSettings> {

    @Override
    public Class<? extends Component> getComponentClass() {
        return DataGridImpl.class;
    }

    @Override
    public Class<? extends ComponentSettings> getSettingsClass() {
        return DataGridSettings.class;
    }

    @Override
    public void applySettings(DataGrid dataGrid, SettingsWrapper wrapper) {
        DataGridSettings settings = wrapper.getSettings();

        List<DataGridSettings.ColumnSettings> columns = settings.getColumns();
        if (columns != null) {
            List<DataGrid.Column> modelColumns = dataGrid.getVisibleColumns();

            List<String> modelIds = modelColumns.stream()
                    .map(String::valueOf)
                    .collect(Collectors.toList());

            List<String> loadedIds = columns.stream()
                    .map(DataGridSettings.ColumnSettings::getId)
                    .collect(Collectors.toList());

            if (CollectionUtils.isEqualCollection(modelIds, loadedIds)) {
                applyColumnSettings(dataGrid, settings, modelColumns);
            }
        }
    }

    @Override
    public void applyDataLoadingSettings(DataGrid dataGrid, SettingsWrapper wrapper) {
        DataGridSettings dataGridSettings = wrapper.getSettings();

        if (dataGrid.isSortable() && isApplyDataLoadingSettings(dataGrid)) {
            if (dataGridSettings.getColumns() == null) {
                return;
            }

            String sortColumnId = dataGridSettings.getSortColumnId();
            if (StringUtils.isNotEmpty(sortColumnId)) {
                Grid grid = getGrid(dataGrid);
                Grid.Column column = grid.getColumn(sortColumnId);
                if (column != null) {
                    if (dataGrid.getItems() instanceof DataGridItems.Sortable) {
                        ((DataGridItems.Sortable) dataGrid.getItems()).suppressSorting();
                    }
                    try {
                        grid.clearSortOrder();
                        DataGrid.SortDirection sortDirection = dataGridSettings.getSortDirection();
                        if (sortDirection != null) {
                            List<GridSortOrder> sortOrders = Collections.singletonList(
                                    new GridSortOrder<>(column, WrapperUtils.convertToGridSortDirection(sortDirection)));
                            grid.setSortOrder(sortOrders);
                        }
                    } finally {
                        if (dataGrid.getItems() instanceof DataGridItems.Sortable) {
                            ((DataGridItems.Sortable) dataGrid.getItems()).enableSorting();
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean saveSettings(DataGrid dataGrid, SettingsWrapper wrapper) {
        DataGridSettings settings = wrapper.getSettings();

        String sortColumnId = null;
        DataGrid.SortDirection sortDirection = null;

        if (settings.getColumns() != null) {
            sortColumnId = settings.getSortColumnId();
            sortDirection = settings.getSortDirection();
        }

        boolean commonSettingsChanged = isCommonDataGridSettingsChanged(dataGrid, settings);
        boolean sortChanged = isSortPropertySettingsChanged(dataGrid, sortColumnId, sortDirection);

        boolean settingsChanged = commonSettingsChanged || sortChanged;

        if (settingsChanged) {
            settings.setColumns(getColumnsSettings(dataGrid));

            List<GridSortOrder> sortOrders = getGrid(dataGrid).getSortOrder();
            // DataGrid does not allow to reset sorting if once it were set,
            // so we don't save null sorting
            if (!sortOrders.isEmpty()) {
                GridSortOrder sortOrder = sortOrders.get(0);

                settings.setSortColumnId(sortOrder.getSorted().getId());
                settings.setSortDirection(WrapperUtils.convertToDataGridSortDirection(sortOrder.getDirection()));
            }
        }

        return settingsChanged;
    }

    @Override
    public DataGridSettings getSettings(DataGrid dataGrid) {
        DataGridSettings settings = createSettings();
        settings.setId(dataGrid.getId());

        settings.setColumns(getColumnsSettings(dataGrid));

        List<GridSortOrder> sortOrders = getGrid(dataGrid).getSortOrder();
        if (!sortOrders.isEmpty()) {
            GridSortOrder sortOrder = sortOrders.get(0);

            settings.setSortColumnId(sortOrder.getSorted().getId());
            settings.setSortDirection(WrapperUtils.convertToDataGridSortDirection(sortOrder.getDirection()));
        }

        return settings;
    }

    protected List<DataGridSettings.ColumnSettings> getColumnsSettings(DataGrid dataGrid) {
        List<DataGrid.Column> visibleColumns = dataGrid.getVisibleColumns();
        List<DataGridSettings.ColumnSettings> columnsSettings = new ArrayList<>(visibleColumns.size());

        for (DataGrid.Column column : visibleColumns) {
            DataGridSettings.ColumnSettings columnSettings = new DataGridSettings.ColumnSettings();
            columnSettings.setId(column.toString());

            double width = column.getWidth();
            if (width > -1) {
                columnSettings.setWidth(width);
            }

            columnSettings.setCollapsed(column.isCollapsed());

            columnsSettings.add(columnSettings);
        }

        return columnsSettings;
    }

    protected boolean isApplyDataLoadingSettings(DataGrid dataGrid) {
        DataGridItems tableItems = dataGrid.getItems();
        if (tableItems instanceof ContainerDataUnit) {
            CollectionContainer container = ((ContainerDataUnit) tableItems).getContainer();
            return container instanceof HasLoader && ((HasLoader) container).getLoader() instanceof CollectionLoader;
        }
        return false;
    }

    protected void applyColumnSettings(DataGrid dataGrid, DataGridSettings settings, Collection<DataGrid.Column> oldColumns) {
        Grid grid = getGrid(dataGrid);

        List<DataGridSettings.ColumnSettings> columnsSettings = CollectionUtils.isEmpty(settings.getColumns())
                ? Collections.emptyList()
                : settings.getColumns();

        List<DataGrid.Column> newColumns = new ArrayList<>();

        // add columns from saved settings
        for (DataGridSettings.ColumnSettings columnSettings : columnsSettings) {
            for (DataGrid.Column column : oldColumns) {
                if (column.getId().equals(columnSettings.getId())) {
                    newColumns.add(column);

                    Double width = columnSettings.getWidth();
                    if (width != null) {
                        column.setWidth(width);
                    } else {
                        column.setWidthAuto();
                    }

                    Boolean collapsed = columnSettings.getCollapsed();
                    if (collapsed != null && grid.isColumnReorderingAllowed()) {
                        column.setCollapsed(collapsed);
                    }

                    break;
                }
            }
        }

        // add columns not saved in settings (perhaps new)
        for (DataGrid.Column column : oldColumns) {
            if (!newColumns.contains(column)) {
                newColumns.add(column);
            }
        }

        // if the data grid contains only one column, always show it
        if (newColumns.size() == 1) {
            newColumns.get(0).setCollapsed(false);
        }

        // We don't save settings for columns hidden by security permissions,
        // so we need to return them back to they initial positions
        DataGridSettingsUtils.restoreColumnsOrder(dataGrid, newColumns);
        grid.setColumnOrder(newColumns.stream()
                .map(DataGrid.Column::getId)
                .toArray(String[]::new));

        if (dataGrid.isSortable() && !isApplyDataLoadingSettings(dataGrid)) {
            // apply sorting
            grid.clearSortOrder();
            String sortColumnId = settings.getSortColumnId();
            if (StringUtils.isNotEmpty(sortColumnId)) {
                Grid.Column column = grid.getColumn(sortColumnId);
                if (column != null) {
                    DataGrid.SortDirection sortDirection = settings.getSortDirection();
                    if (sortDirection != null) {
                        List<GridSortOrder> sortOrders = Collections.singletonList(
                                new GridSortOrder(column, WrapperUtils.convertToGridSortDirection(sortDirection))
                        );
                        grid.setSortOrder(sortOrders);
                    }
                }
            }
        }
    }

    protected boolean isCommonDataGridSettingsChanged(DataGrid dataGrid, DataGridSettings settings) {
        List<DataGridSettings.ColumnSettings> settingsColumnList = settings.getColumns();

        // if columns null consider settings changed, because we cannot track changes
        // without previous "state"
        if (settingsColumnList == null) {
            return true;
        }

        List<DataGrid.Column> visibleColumns = dataGrid.getVisibleColumns();
        if (settingsColumnList.size() != visibleColumns.size()) {
            return true;
        }

        for (int i = 0; i < visibleColumns.size(); i++) {
            Object columnId = visibleColumns.get(i).getId();

            DataGridSettings.ColumnSettings settingsColumn = settingsColumnList.get(i);
            String settingsColumnId = settingsColumn.getId();

            if (columnId.toString().equals(settingsColumnId)) {
                double columnWidth = visibleColumns.get(i).getWidth();

                Double settingsColumnWidth = settingsColumn.getWidth();
                if (settingsColumnWidth == null) {
                    settingsColumnWidth = -1d;
                }

                if (columnWidth != settingsColumnWidth) {
                    return true;
                }

                if (visibleColumns.get(i).isCollapsed() != BooleanUtils.toBoolean(settingsColumn.getCollapsed())) {
                    return true;
                }
            } else {
                return true;
            }
        }

        return false;
    }

    protected boolean isSortPropertySettingsChanged(DataGrid dataGrid, @Nullable String settingsSortColumnId, @Nullable DataGrid.SortDirection settingsSort) {
        List<GridSortOrder> sortOrders = getGrid(dataGrid).getSortOrder();

        String columnId = null;
        com.vaadin.shared.data.sort.SortDirection sort = null;

        if (!sortOrders.isEmpty()) {
            GridSortOrder sortOrder = sortOrders.get(0);

            columnId = sortOrder.getSorted().getId();
            sort = sortOrder.getDirection();
        }

        com.vaadin.shared.data.sort.SortDirection settingsGridSort =
                settingsSort != null ? WrapperUtils.convertToGridSortDirection(settingsSort) : null;

        return !Objects.equals(columnId, settingsSortColumnId)
                || !Objects.equals(sort, settingsGridSort);
    }

    protected DataGridSettings createSettings() {
        return new DataGridSettings();
    }

    protected Grid getGrid(DataGrid dataGrid) {
        return dataGrid.unwrap(Grid.class);
    }
}
