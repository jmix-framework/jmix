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
import io.jmix.flowui.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;

public abstract class AbstractGridSettingsBinder<V extends Grid<?>, S extends DataGridSettings>
        implements DataLoadingSettingsBinder<V, S> {

    private static final Logger log = LoggerFactory.getLogger(AbstractGridSettingsBinder.class);

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void applySettings(V component, S settings) {
        if (isEmpty(settings.getColumns())) {
            return;
        }

        List<? extends Grid.Column<?>> componentColumns = getOrderedColumns(component);
        List<Grid.Column<?>> newColumnsOrder = new ArrayList<>();

        // apply existing settings if possible
        for (DataGridSettings.Column sColumn : settings.getColumns()) {
            Grid.Column<?> column = component.getColumnByKey(sColumn.getKey());
            if (column == null) {
                log.warn("Column with key '{}' not found in {}. The settings will not be applied.",
                        sColumn.getKey(), Grid.class.getSimpleName());
                continue;
            }

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

        if (newColumnsOrder.size() != componentColumns.size()) {
            List<? extends Grid.Column<?>> notProcessedColumns = componentColumns.stream()
                    .filter(column -> !newColumnsOrder.contains(column))
                    .toList();

            for (Grid.Column<?> notProcessedColumn : notProcessedColumns) {
                int index = findFirstLeftNeighborIndex(componentColumns, newColumnsOrder, notProcessedColumn);

                // add right after left neighbor, otherwise add to zero-index
                newColumnsOrder.add(index + 1, notProcessedColumn);
            }
        }

        component.setColumnOrder((List) newColumnsOrder);

        // Collection property container sorting cannot be applied in '#applyDataLoadingSettings()',
        // because the items are not loaded at that moment and Grid will attempt to sort an empty container.
        // The next sorting operation won't be applied, as the Grid does not re-sort if the sorting is the same.
        // However, it is safe to sort collection property container here, because it is in-memory items
        // collection.
        if (isCollectionPropertyContainer(getGridItems(component))) {
            applySorting(component, settings);
        }
    }

    protected int findFirstLeftNeighborIndex(List<? extends Grid.Column<?>> componentColumns,
                                             List<Grid.Column<?>> newColumnsOrder,
                                             Grid.Column<?> notIncludedColumn) {
        int startIndex = componentColumns.indexOf(notIncludedColumn);
        while (startIndex > 0) {
            Grid.Column<?> neighborCandidate = componentColumns.get(--startIndex);

            if (newColumnsOrder.contains(neighborCandidate)) {
                return newColumnsOrder.indexOf(neighborCandidate);
            }
        }

        // left neighbor is not found, should be 0 index
        return -1;
    }

    @Override
    public void applyDataLoadingSettings(V component, S settings) {
        if (!isDataLoadingSettingsEnabled(component)) {
            return;
        }

        applySorting(component, settings);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void applySorting(V component, S settings) {
        if (isEmpty(settings.getSortOrder())) {
            return;
        }
        List sortOrder = settings.getSortOrder().stream()
                .filter(o -> o.getKey() != null)
                .map(sSortOrder -> new GridSortOrder<>(
                        component.getColumnByKey(sSortOrder.getKey()),
                        SortDirection.valueOf(sSortOrder.getSortDirection())))
                .toList();

        // Do not sort if columns in sort order do not exist in Grid anymore
        for (GridSortOrder sortOrderItem : (List<GridSortOrder>) sortOrder) {
            if (sortOrderItem.getSorted() == null) {
                log.warn("{} does not contain column(s) saved in sort-order settings. The sort will not be applied.",
                        Grid.class.getSimpleName());
                return;
            }
        }

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

    protected List<? extends Grid.Column<?>> getApplicableColumns(V component) {
        List<? extends Grid.Column<?>> componentColumns = getOrderedColumns(component);
        if (componentColumns.stream().anyMatch(c -> c.getKey() == null)) {
            log.warn("{} contains one or more columns without key specified, settings for them will not be stored",
                    component.getClass().getSimpleName());
            componentColumns = componentColumns.stream()
                    .filter(c -> c.getKey() != null).toList();
        }
        return componentColumns;
    }

    @Override
    public boolean saveSettings(V component, S settings) {
        boolean changed = false;

        List<? extends GridSortOrder<?>> sortOrder = component.getSortOrder();
        if (isColumnSortOrderChanged(sortOrder, settings.getSortOrder())) {
            setSortOrderToSettings(sortOrder, settings);
            changed = true;
        }
        List<? extends Grid.Column<?>> applicableColumns = getApplicableColumns(component);
        if (isColumnSettingsChanged(applicableColumns, settings.getColumns())) {
            setColumnsToSettings(applicableColumns, settings);
            changed = true;
        }

        return changed;
    }

    @Override
    public S getSettings(V component) {
        S settings = createSettings();
        settings.setId(component.getId().orElse(null));

        setSortOrderToSettings(component.getSortOrder(), settings);
        setColumnsToSettings(getApplicableColumns(component), settings);

        return settings;
    }

    protected abstract S createSettings();

    protected boolean isDataLoadingSettingsEnabled(V grid) {
        DataGridItems<?> items = getGridItems(grid);
        if (items instanceof ContainerDataUnit) {
            CollectionContainer<?> container = ((ContainerDataUnit<?>) items).getContainer();
            return container instanceof HasLoader
                    && ((HasLoader) container).getLoader() instanceof BaseCollectionLoader;
        }
        return false;
    }

    protected boolean isCollectionPropertyContainer(@Nullable DataGridItems<?> items) {
        if (!(items instanceof ContainerDataUnit<?>)) {
            return false;
        }
        return ((ContainerDataUnit<?>) items).getContainer() instanceof CollectionPropertyContainer<?>;
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

            if (key != null && !key.equals(sSortOrder.getKey())) {
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
