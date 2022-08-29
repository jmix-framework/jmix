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

import com.google.common.base.Strings;
import io.jmix.core.UuidProvider;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.Table;
import io.jmix.ui.component.data.TableItems;
import io.jmix.ui.component.data.meta.ContainerDataUnit;
import io.jmix.ui.component.data.meta.EntityTableItems;
import io.jmix.ui.component.impl.TableImpl;
import io.jmix.ui.component.presentation.TablePresentationsLayout;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.model.HasLoader;
import io.jmix.ui.settings.component.ComponentSettings;
import io.jmix.ui.settings.component.SettingsWrapper;
import io.jmix.ui.settings.component.TableSettings;
import io.jmix.ui.widget.JmixEnhancedTable;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.*;

@SuppressWarnings("rawtypes")
public abstract class AbstractTableSettingsBinder implements DataLoadingSettingsBinder<Table, TableSettings> {

    @Override
    public Class<? extends Component> getComponentClass() {
        return TableImpl.class;
    }

    @Override
    public Class<? extends ComponentSettings> getSettingsClass() {
        return TableSettings.class;
    }

    @Override
    public void applySettings(Table table, SettingsWrapper wrapper) {
        TableSettings tableSettings = wrapper.getSettings();

        if (isPresentationsEnabled(table) && tableSettings.getTextSelection() != null) {
            table.setTextSelectionEnabled(tableSettings.getTextSelection());

            com.vaadin.ui.Component presentationsLayout = getEnhancedTable(table).getPresentationsLayout();
            if (presentationsLayout != null) {
                ((TablePresentationsLayout) presentationsLayout).updateTextSelection();
            }
        }

        List<TableSettings.ColumnSettings> columnSettings = tableSettings.getColumns();
        if (columnSettings != null) {
            boolean refreshWasEnabled = getEnhancedTable(table).disableContentBufferRefreshing();

            Collection<String> modelIds = new ArrayList<>();
            for (Object column : getVTable(table).getVisibleColumns()) {
                modelIds.add(String.valueOf(column));
            }

            Collection<String> loadedIds = new ArrayList<>();
            for (TableSettings.ColumnSettings column : columnSettings) {
                loadedIds.add(column.getId());
            }

            if (CollectionUtils.isEqualCollection(modelIds, loadedIds)) {
                applyColumnSettings(tableSettings, table);
            }

            getEnhancedTable(table).enableContentBufferRefreshing(refreshWasEnabled);
        }
    }

    @Override
    public void applyDataLoadingSettings(Table table, SettingsWrapper wrapper) {
        EntityTableItems entityTableSource = (EntityTableItems) table.getItems();
        if (table.isSortable()
                && isApplyDataLoadingSettings(table)
                && entityTableSource != null) {

            TableSettings tableSettings = wrapper.getSettings();
            List<TableSettings.ColumnSettings> columns = tableSettings.getColumns();

            if (columns != null) {
                String sortProp = tableSettings.getSortProperty();
                if (sortProp != null) {
                    List visibleColumns = Arrays.asList(getVTable(table).getVisibleColumns());

                    MetaPropertyPath sortProperty = entityTableSource.getEntityMetaClass().getPropertyPath(sortProp);
                    if (visibleColumns.contains(sortProperty)) {

                        if (table.getItems() instanceof TableItems.Sortable) {
                            ((TableItems.Sortable) table.getItems()).suppressSorting();
                        }
                        try {
                            com.vaadin.v7.ui.Table vTable = getVTable(table);
                            vTable.setSortContainerPropertyId(null);
                            vTable.setSortAscending(Boolean.TRUE.equals(tableSettings.getSortAscending()));
                            vTable.setSortContainerPropertyId(sortProperty);
                        } finally {
                            if (table.getItems() instanceof TableItems.Sortable) {
                                ((TableItems.Sortable) table.getItems()).enableSorting();
                            }
                        }
                    }
                } else if (table.getSortInfo() != null) {
                    TableItems tableItems = table.getItems();
                    if (tableItems instanceof TableItems.Sortable) {
                        ((TableItems.Sortable) tableItems).resetSortOrder();
                    }
                }
            }
        }
    }

    @Override
    public boolean saveSettings(Table table, SettingsWrapper wrapper) {
        TableSettings tableSettings = wrapper.getSettings();

        boolean settingsChanged = false;

        if (isPresentationsEnabled(table)) {
            Boolean textSelection = tableSettings.getTextSelection();
            if (textSelection == null
                    || BooleanUtils.toBoolean(textSelection) != table.isTextSelectionEnabled()) {
                tableSettings.setTextSelection(table.isTextSelectionEnabled());

                settingsChanged = true;
            }
        }

        String settingsSortProperty = null;
        Boolean settingsSortAscending = null;

        if (tableSettings.getSortProperty() != null) {
            settingsSortProperty = tableSettings.getSortProperty();
            settingsSortAscending = tableSettings.getSortAscending();
        }

        boolean commonSettingsChanged = isCommonTableSettingsChanged(tableSettings, table);
        boolean sortChanged = isSettingsSortPropertyChanged(settingsSortProperty, settingsSortAscending, table.getSortInfo());

        if (commonSettingsChanged || sortChanged) {
            // save column settings
            tableSettings.setColumns(getTableColumnSettings(table));

            Table.SortInfo sortInfo = table.getSortInfo();
            if (sortInfo == null) {
                tableSettings.setSortProperty(null);
                tableSettings.setSortAscending(null);
            } else {
                MetaPropertyPath mpp = (MetaPropertyPath) sortInfo.getPropertyId();
                tableSettings.setSortProperty(mpp.toString());
                tableSettings.setSortAscending(sortInfo.getAscending());
            }

            settingsChanged = true;
        }

        return settingsChanged;
    }

    @Override
    public TableSettings getSettings(Table table) {
        TableSettings tableSettings = createTableSettings();
        tableSettings.setId(table.getId());

        if (isPresentationsEnabled(table)) {
            tableSettings.setTextSelection(table.isTextSelectionEnabled());
        }

        // get column settings
        tableSettings.setColumns(getTableColumnSettings(table));

        // get sort
        Table.SortInfo sortInfo = table.getSortInfo();
        if (sortInfo != null) {
            MetaPropertyPath sortProperty = (MetaPropertyPath) sortInfo.getPropertyId();
            if (sortProperty != null) {
                tableSettings.setSortProperty(sortProperty.toString());
                tableSettings.setSortAscending(sortInfo.getAscending());
            }
        }

        return tableSettings;
    }

    protected TableSettings createTableSettings() {
        return new TableSettings();
    }

    protected List<TableSettings.ColumnSettings> getTableColumnSettings(Table table) {
        com.vaadin.v7.ui.Table vTable = getVTable(table);

        Object[] visibleColumns = vTable.getVisibleColumns();
        List<TableSettings.ColumnSettings> columnsSettings = new ArrayList<>(visibleColumns.length);

        for (Object columnId : visibleColumns) {
            TableSettings.ColumnSettings columnSettings = new TableSettings.ColumnSettings();

            columnSettings.setId(columnId.toString());

            int width = vTable.getColumnWidth(columnId);
            if (width > -1)
                columnSettings.setWidth(width);

            boolean visible = !vTable.isColumnCollapsed(columnId);
            columnSettings.setVisible(visible);

            columnsSettings.add(columnSettings);
        }

        return columnsSettings;
    }

    protected boolean isCommonTableSettingsChanged(TableSettings tableSettings, Table table) {
        com.vaadin.v7.ui.Table vTable = getVTable(table);

        // if columns null consider settings changed, because we cannot track changes
        // without previous "state"
        if (tableSettings.getColumns() == null) {
            return true;
        }

        Object[] visibleColumns = vTable.getVisibleColumns();
        List<TableSettings.ColumnSettings> columnSettings = tableSettings.getColumns();
        if (columnSettings.size() != visibleColumns.length) {
            return true;
        }

        for (int i = 0; i < visibleColumns.length; i++) {
            Object columnId = visibleColumns[i];
            TableSettings.ColumnSettings settingsColumn = columnSettings.get(i);

            // if columns order is changed
            if (!Objects.equals(columnId.toString(), settingsColumn.getId())) {
                return true;
            }

            int settingsWidth = settingsColumn.getWidth() == null ? -1 : settingsColumn.getWidth();
            if (vTable.getColumnWidth(columnId) != settingsWidth) {
                return true;
            }

            boolean visible = !vTable.isColumnCollapsed(columnId);
            boolean settingsVisible = !Boolean.FALSE.equals(settingsColumn.getVisible());

            if (visible != settingsVisible) {
                return true;
            }
        }

        return false;
    }

    protected boolean isSettingsSortPropertyChanged(@Nullable String settingsSortProperty,
                                                    @Nullable Boolean settingsSortAscending,
                                                    @Nullable Table.SortInfo sortInfo) {
        if (sortInfo == null) {
            return !Strings.isNullOrEmpty(settingsSortProperty);
        }

        MetaPropertyPath mpp = (MetaPropertyPath) sortInfo.getPropertyId();
        if (settingsSortProperty == null
                || !mpp.toString().equals(settingsSortProperty)) {
            return true;
        }

        if (settingsSortAscending == null) {
            settingsSortAscending = true;
        }

        return sortInfo.getAscending() != settingsSortAscending;
    }

    protected void applyColumnSettings(TableSettings tableSettings, Table table) {
        com.vaadin.v7.ui.Table vTable = getVTable(table);

        List<TableSettings.ColumnSettings> settingsColumns = CollectionUtils.isEmpty(tableSettings.getColumns())
                ? Collections.emptyList()
                : tableSettings.getColumns();

        Object[] oldColumns = vTable.getVisibleColumns();
        List<Object> newColumns = new ArrayList<>();

        // add columns from saved settings
        for (TableSettings.ColumnSettings columnSetting : settingsColumns) {
            for (Object column : oldColumns) {
                if (column.toString().equals(columnSetting.getId())) {
                    newColumns.add(column);

                    Integer width = columnSetting.getWidth();
                    vTable.setColumnWidth(column, width == null ? -1 : width);

                    Boolean visible = columnSetting.getVisible();
                    if (visible != null) {
                        if (vTable.isColumnCollapsingAllowed()) { // throws exception if not
                            vTable.setColumnCollapsed(column, !visible);
                        }
                    }
                    break;
                }
            }
        }
        // add columns not saved in settings (perhaps new)
        for (Object column : oldColumns) {
            if (!newColumns.contains(column)) {
                newColumns.add(column);
            }
        }
        // if the table contains only one column, always show it
        if (newColumns.size() == 1) {
            if (vTable.isColumnCollapsingAllowed()) { // throws exception if not
                vTable.setColumnCollapsed(newColumns.get(0), false);
            }
        }

        vTable.setVisibleColumns(newColumns.toArray());

        EntityTableItems entityTableSource = (EntityTableItems) table.getItems();
        if (table.isSortable()
                && !isApplyDataLoadingSettings(table)
                && entityTableSource != null) {
            String sortProp = tableSettings.getSortProperty();
            if (!StringUtils.isEmpty(sortProp)) {
                MetaPropertyPath sortProperty = entityTableSource.getEntityMetaClass().getPropertyPath(sortProp);
                if (newColumns.contains(sortProperty)) {
                    vTable.setSortContainerPropertyId(null);
                    vTable.setSortAscending(Boolean.TRUE.equals(tableSettings.getSortAscending()));
                    vTable.setSortContainerPropertyId(sortProperty);
                }
            } else {
                vTable.setSortContainerPropertyId(null);
            }
        }
    }

    protected boolean isApplyDataLoadingSettings(Table table) {
        TableItems tableItems = table.getItems();

        if (tableItems instanceof ContainerDataUnit) {
            CollectionContainer container = ((ContainerDataUnit) tableItems).getContainer();
            return container instanceof HasLoader && ((HasLoader) container).getLoader() instanceof CollectionLoader;
        }

        return false;
    }

    protected boolean isPresentationsEnabled(Table table) {
        return table.getPresentations() != null;
    }

    protected JmixEnhancedTable getEnhancedTable(Table table) {
        return table.unwrap(JmixEnhancedTable.class);
    }

    protected com.vaadin.v7.ui.Table getVTable(Table table) {
        return table.unwrap(com.vaadin.v7.ui.Table.class);
    }
}
