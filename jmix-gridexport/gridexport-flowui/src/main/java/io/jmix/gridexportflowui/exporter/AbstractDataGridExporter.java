/*
 * Copyright 2023 Haulmont.
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

package io.jmix.gridexportflowui.exporter;

import com.google.common.base.Strings;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasText;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import io.jmix.core.*;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.metamodel.model.Range;
import io.jmix.flowui.UiProperties;
import io.jmix.flowui.component.ListDataComponent;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.grid.EnhancedDataGrid;
import io.jmix.flowui.component.grid.TreeDataGrid;
import io.jmix.flowui.component.grid.headerfilter.DataGridHeaderFilter;
import io.jmix.flowui.component.groupgrid.GroupInfo;
import io.jmix.flowui.component.groupgrid.GroupListDataComponent;
import io.jmix.flowui.component.groupgrid.adapter.AbstractGroupDataGridAdapter;
import io.jmix.flowui.component.groupgrid.adapter.AbstractGroupDataGridColumnAdapter;
import io.jmix.flowui.component.groupgrid.adapter.GroupDataGridAdapterFactory;
import io.jmix.flowui.component.groupgrid.data.GroupDataGridItems;
import io.jmix.flowui.data.DataUnit;
import io.jmix.flowui.data.grid.ContainerTreeDataGridItems;
import io.jmix.flowui.data.grid.EntityDataGridItems;
import io.jmix.flowui.kit.component.grid.JmixGrid;
import io.jmix.flowui.kit.component.grid.JmixTreeGrid;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.gridexportflowui.GridExportProperties;
import io.jmix.gridexportflowui.exporter.entitiesloader.AllEntitiesLoader;
import io.jmix.gridexportflowui.exporter.entitiesloader.AllEntitiesLoader.ExportedEntityVisitor;
import io.jmix.gridexportflowui.exporter.entitiesloader.AllEntitiesLoaderFactory;
import io.jmix.gridexportflowui.exporter.entitiesloader.LimitOffsetAllEntitiesLoader;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class AbstractDataGridExporter<T extends AbstractDataGridExporter<?>> implements DataGridExporter {

    protected MessageTools messageTools;
    protected DatatypeRegistry datatypeRegistry;
    protected Messages messages;
    protected MetadataTools metadataTools;
    protected UiProperties uiProperties;
    protected CoreProperties coreProperties;
    protected GridExportProperties gridExportProperties;
    protected GroupDataGridAdapterFactory groupDataGridAdapterFactory;

    @Nullable
    protected String exportAllPaginationStrategy;
    @Nullable
    protected Integer exportAllBatchSize;

    @Autowired
    public void setMessageTools(MessageTools messageTools) {
        this.messageTools = messageTools;
    }

    @Autowired
    public void setDatatypeRegistry(DatatypeRegistry datatypeRegistry) {
        this.datatypeRegistry = datatypeRegistry;
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.messages = messages;
    }

    @Autowired
    public void setMetadataTools(MetadataTools metadataTools) {
        this.metadataTools = metadataTools;
    }

    @Autowired
    public void setUiProperties(UiProperties uiProperties) {
        this.uiProperties = uiProperties;
    }

    @Autowired
    public void setCoreProperties(CoreProperties coreProperties) {
        this.coreProperties = coreProperties;
    }

    @Autowired
    public void setGridExportProperties(GridExportProperties gridExportProperties) {
        this.gridExportProperties = gridExportProperties;
    }

    @Autowired
    public void setGroupDataGridAdapterFactory(GroupDataGridAdapterFactory groupDataGridAdapterFactory) {
        this.groupDataGridAdapterFactory = groupDataGridAdapterFactory;
    }

    protected String fileName;

    protected Map<String, Function<ColumnValueContext, Object>> columnValueProviders;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @SuppressWarnings("unchecked")
    public T withFileName(String fileName) {
        setFileName(fileName);
        return (T) this;
    }

    /**
     * Returns the pagination strategy used by this exporter to load all rows.
     *
     * @return pagination strategy, or {@code null} if
     * {@link GridExportProperties#getExportAllPaginationStrategy()} is used
     */
    @Nullable
    public String getExportAllPaginationStrategy() {
        return exportAllPaginationStrategy;
    }

    /**
     * Sets the pagination strategy used by this exporter to load all rows, overriding
     * {@link GridExportProperties#getExportAllPaginationStrategy()}.
     *
     * @param exportAllPaginationStrategy pagination strategy, e.g.
     *                                    {@link LimitOffsetAllEntitiesLoader#PAGINATION_STRATEGY}, or {@code null}
     *                                    to use the application-wide one
     */
    public void setExportAllPaginationStrategy(@Nullable String exportAllPaginationStrategy) {
        this.exportAllPaginationStrategy = exportAllPaginationStrategy;
    }

    /**
     * @param exportAllPaginationStrategy pagination strategy, or {@code null} to use the application-wide one
     * @return this instance for chaining
     * @see #setExportAllPaginationStrategy(String)
     */
    @SuppressWarnings("unchecked")
    public T withExportAllPaginationStrategy(@Nullable String exportAllPaginationStrategy) {
        setExportAllPaginationStrategy(exportAllPaginationStrategy);
        return (T) this;
    }

    /**
     * Returns the number of entities loaded in one query when this exporter loads all rows.
     *
     * @return batch size, or {@code null} if {@link GridExportProperties#getExportAllBatchSize()} is used
     */
    @Nullable
    public Integer getExportAllBatchSize() {
        return exportAllBatchSize;
    }

    /**
     * Sets the number of entities loaded in one query when this exporter loads all rows, overriding
     * {@link GridExportProperties#getExportAllBatchSize()}.
     *
     * @param exportAllBatchSize positive batch size, or {@code null} to use the application-wide one
     * @throws IllegalArgumentException if the batch size is not positive
     */
    public void setExportAllBatchSize(@Nullable Integer exportAllBatchSize) {
        if (exportAllBatchSize != null && exportAllBatchSize <= 0) {
            throw new IllegalArgumentException("Export all batch size must be positive: " + exportAllBatchSize);
        }
        this.exportAllBatchSize = exportAllBatchSize;
    }

    /**
     * @param exportAllBatchSize positive batch size, or {@code null} to use the application-wide one
     * @return this instance for chaining
     * @throws IllegalArgumentException if the batch size is not positive
     * @see #setExportAllBatchSize(Integer)
     */
    @SuppressWarnings("unchecked")
    public T withExportAllBatchSize(@Nullable Integer exportAllBatchSize) {
        setExportAllBatchSize(exportAllBatchSize);
        return (T) this;
    }

    @Override
    public void addColumnValueProvider(String columnId, Function<ColumnValueContext, Object> columnValueProvider) {
        if (columnValueProviders == null) {
            columnValueProviders = new HashMap<>();
        }

        columnValueProviders.put(columnId, columnValueProvider);
    }

    @Override
    public void removeColumnValueProvider(String columnId) {
        if (MapUtils.isNotEmpty(columnValueProviders)) {
            columnValueProviders.remove(columnId);
        }
    }

    @Nullable
    @Override
    public Function<ColumnValueContext, Object> getColumnValueProvider(String columnId) {
        return MapUtils.isNotEmpty(columnValueProviders)
                ? columnValueProviders.get(columnId)
                : null;
    }

    /**
     * Reads the rows of a data grid without producing a file. The values are formatted as {@code JsonExporter}
     * formats them: through this exporter's column value providers and its text formatting. Only the rows the grid
     * holds are read; nothing is loaded. Rows come in the order of the grid's items, and for a group data grid in
     * group order; selected rows come in that same order.
     * <p>
     * A group data grid is read as a flat table: its grouping columns take the place of the group column, and its
     * rows are the entities without group rows, group after group as the grid shows them.
     * <p>
     * Columns, headers and items are resolved by the protected grid methods of this exporter, such as
     * {@link #getColumnHeaderText(Grid, Grid.Column)}, so a subclass that overrides them affects the read rows the
     * same way as its export.
     *
     * @param target       data grid to read; a group data grid is read through its adapter
     * @param exportMode   {@link ExportMode#CURRENT_PAGE} or {@link ExportMode#SELECTED_ROWS}
     * @param columnFilter selects the columns to read; grouping columns of a group data grid are always read
     * @return the columns and the formatted rows
     * @throws IllegalArgumentException      if {@code exportMode} is {@link ExportMode#ALL_ROWS}
     * @throws UnsupportedOperationException if the target is not an {@link EnhancedDataGrid}
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public DataGridRows readRows(ListDataComponent<?> target, ExportMode exportMode,
                                 Predicate<Grid.Column<Object>> columnFilter) {
        if (exportMode == ExportMode.ALL_ROWS) {
            throw new IllegalArgumentException("Only the rows held by the grid can be read, not " + exportMode);
        }

        Grid<Object> dataGrid = resolveGrid(target);
        List<Grid.Column<Object>> gridColumns = getColumnsToRead(dataGrid, columnFilter);

        List<DataGridRows.Column> columns = gridColumns.stream()
                .map(column -> new DataGridRows.Column(column.getKey(),
                        getColumnHeaderText(dataGrid, column),
                        ((EnhancedDataGrid) dataGrid).getColumnMetaPropertyPath(column)))
                .toList();

        List<List<String>> rows = getItemsToRead(dataGrid, exportMode).stream()
                .map(item -> gridColumns.stream()
                        .map(column -> formatCellValue(dataGrid, column, item))
                        .toList())
                .toList();

        return new DataGridRows(columns, rows);
    }

    protected void loadAllEntities(AllEntitiesLoaderFactory allEntitiesLoaderFactory, DataUnit dataUnit,
                                   ExportedEntityVisitor exportedEntityVisitor) {
        AllEntitiesLoader loader = getAllEntitiesLoaderInternal(allEntitiesLoaderFactory);
        int loadBatchSize = exportAllBatchSize != null
                ? exportAllBatchSize
                : gridExportProperties.getExportAllBatchSize();
        loader.loadAll(dataUnit, exportedEntityVisitor, loadBatchSize);
    }

    protected AllEntitiesLoader getAllEntitiesLoaderInternal(AllEntitiesLoaderFactory allEntitiesLoaderFactory) {
        return exportAllPaginationStrategy != null
                ? allEntitiesLoaderFactory.getEntitiesLoader(exportAllPaginationStrategy)
                : allEntitiesLoaderFactory.getEntitiesLoader();
    }

    protected String getMetaClassName(MetaClass metaClass) {
        return messageTools.getEntityCaption(metaClass);
    }

    protected String getFileName(Grid<Object> dataGrid) {
        if (fileName != null) {
            return fileName;
        }

        ListDataComponent<?> listDataComponent = (ListDataComponent<?>) dataGrid;
        EntityDataGridItems<?> items = (EntityDataGridItems<?>) listDataComponent.getItems();
        if (items == null) {
            throw new IllegalStateException("DataGrid is not bound to data");
        }
        return getMetaClassName(items.getEntityMetaClass());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Nullable
    protected Object getColumnValue(Grid<?> dataGrid, Grid.Column<?> column, Object instance) {
        Function<ColumnValueContext, Object> columnValueProvider = MapUtils.isNotEmpty(columnValueProviders)
                ? columnValueProviders.get(column.getKey())
                : null;

        if (columnValueProvider != null) {
            return columnValueProvider.apply(new ColumnValueContext((ListDataComponent<?>) dataGrid, column, instance));
        }

        Object cellValue = null;
        MetaPropertyPath metaPropertyPath = ((EnhancedDataGrid) dataGrid).getColumnMetaPropertyPath(column);

        if (metaPropertyPath != null) {
            cellValue = EntityValues.getValueEx(instance, metaPropertyPath.getPath());
        } else {
            Renderer<?> renderer = column.getRenderer();
            //if a component renderer is assigned, try to retrieve export value from rendered component
            if (renderer instanceof ComponentRenderer componentRenderer) {
                Component cellComponent = componentRenderer.createComponent(instance);
                if (cellComponent instanceof HasText hasText) {
                    cellValue = hasText.getText();
                } else if (cellComponent instanceof HasValue<?, ?> hasValue) {
                    cellValue = UiComponentUtils.getValue(hasValue);
                } else if (renderer instanceof TextRenderer<?>) {
                    cellValue = cellComponent.getElement().getText();
                }
            }
        }

        return cellValue;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected Grid<Object> resolveGrid(ListDataComponent<?> target) {
        if (target instanceof GroupListDataComponent groupGrid) {
            AbstractGroupDataGridAdapter<?> adapter = groupDataGridAdapterFactory.getAdapter(groupGrid);
            if (adapter != null) {
                return (Grid) adapter;
            }
        }

        if (target instanceof Grid grid && grid instanceof EnhancedDataGrid) {
            return grid;
        }

        throw new UnsupportedOperationException("Unsupported component: " + target.getClass().getName());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected String formatCellValue(Grid<Object> dataGrid, Grid.Column<Object> column, Object item) {
        Object value = getColumnValue(dataGrid, column, item);
        if (value == null) {
            return "";
        }

        MetaPropertyPath metaPropertyPath = ((EnhancedDataGrid) dataGrid).getColumnMetaPropertyPath(column);
        return metaPropertyPath != null
                ? formatValue(value, metaPropertyPath)
                : formatValue(value);
    }

    /**
     * Returns the items the grid holds. For a tree data grid these are all items of its container, collapsed
     * ones included.
     *
     * @param dataGrid data grid
     * @return items of the grid
     */
    protected Collection<Object> getDataGridItems(Grid<Object> dataGrid) {
        if (dataGrid instanceof TreeDataGrid<Object> treeDataGrid) {
            ContainerTreeDataGridItems<Object> items = (ContainerTreeDataGridItems<Object>) treeDataGrid.getItems();
            return items == null
                    ? Collections.emptyList()
                    : new ArrayList<>(items.getContainer().getItems());
        }

        return dataGrid.getGenericDataView().getItems().collect(Collectors.toList());
    }

    /**
     * Returns the items to read in the order the grid shows them. Selected items are taken in that order too, not in
     * the order they were selected.
     *
     * @param dataGrid   data grid or group data grid adapter
     * @param exportMode {@link ExportMode#CURRENT_PAGE} or {@link ExportMode#SELECTED_ROWS}
     * @return items to read
     */
    protected List<Object> getItemsToRead(Grid<Object> dataGrid, ExportMode exportMode) {
        List<Object> displayedItems = getDisplayedItems(dataGrid);
        if (exportMode == ExportMode.CURRENT_PAGE) {
            return displayedItems;
        }

        Set<Object> selectedItems = dataGrid.getSelectedItems();
        List<Object> items = new ArrayList<>(selectedItems.size());
        for (Object item : displayedItems) {
            if (selectedItems.contains(item)) {
                items.add(item);
            }
        }

        // A selected item the grid no longer shows (e.g. the selection survived a reload) is read after the shown ones.
        if (items.size() < selectedItems.size()) {
            Set<Object> displayedSelectedItems = new HashSet<>(items);
            for (Object item : selectedItems) {
                if (!displayedSelectedItems.contains(item)) {
                    items.add(item);
                }
            }
        }
        return items;
    }

    /**
     * Returns the items of the grid in the order the grid shows them; for a group data grid, its entities without
     * group rows.
     *
     * @param dataGrid data grid or group data grid adapter
     * @return items of the grid in display order
     */
    protected List<Object> getDisplayedItems(Grid<Object> dataGrid) {
        return dataGrid instanceof AbstractGroupDataGridAdapter<Object> groupDataGrid
                ? getGroupDataGridItems(groupDataGrid)
                : new ArrayList<>(getDataGridItems(dataGrid));
    }

    /**
     * Returns the entities of a group data grid without group rows, in the order the grid shows them: depth-first
     * over the group tree, each group's sub-groups before its own items. Without groups, the items keep their
     * order in the grid's items.
     *
     * @param groupDataGrid group data grid adapter
     * @return the entities of the group data grid
     */
    protected List<Object> getGroupDataGridItems(AbstractGroupDataGridAdapter<Object> groupDataGrid) {
        GroupDataGridItems<Object> groupDataGridItems = groupDataGrid.getItems();
        if (groupDataGridItems == null) {
            return new ArrayList<>();
        }

        if (!groupDataGrid.hasGroups()) {
            return new ArrayList<>(groupDataGridItems.getItems());
        }

        List<Object> items = new ArrayList<>();
        for (GroupInfo rootGroup : groupDataGrid.getRootGroups()) {
            collectGroupItems(groupDataGrid, groupDataGridItems, rootGroup, items);
        }
        return items;
    }

    protected void collectGroupItems(AbstractGroupDataGridAdapter<Object> groupDataGrid,
                                     GroupDataGridItems<Object> groupDataGridItems,
                                     GroupInfo group, List<Object> items) {
        for (GroupInfo childGroup : groupDataGrid.getChildren(group)) {
            collectGroupItems(groupDataGrid, groupDataGridItems, childGroup, items);
        }
        items.addAll(groupDataGridItems.getOwnChildItems(group));
    }

    /**
     * Returns the columns to read. For a group data grid the grouping columns replace the group column and are not
     * subject to {@code columnFilter}.
     *
     * @param dataGrid     data grid or group data grid adapter
     * @param columnFilter selects the columns to read
     * @return columns to read
     */
    protected List<Grid.Column<Object>> getColumnsToRead(Grid<Object> dataGrid,
                                                         Predicate<Grid.Column<Object>> columnFilter) {
        List<Grid.Column<Object>> columns = dataGrid.getColumns().stream()
                .filter(columnFilter)
                .toList();

        return dataGrid instanceof AbstractGroupDataGridAdapter<Object> groupDataGrid
                ? getColumnsIncludingGrouping(groupDataGrid, getOrderedColumns(dataGrid), columns)
                : columns;
    }

    /**
     * Returns the given columns of a group data grid in display order, with the grouping columns in place of the
     * group column.
     *
     * @param groupDataGrid group data grid adapter
     * @param allColumns    all columns of the grid in display order
     * @param columns       columns to keep
     * @return unmodifiable list of the columns to export
     */
    protected List<Grid.Column<Object>> getColumnsIncludingGrouping(AbstractGroupDataGridAdapter<Object> groupDataGrid,
                                                                    List<Grid.Column<Object>> allColumns,
                                                                    List<Grid.Column<Object>> columns) {
        List<Grid.Column<Object>> columnsToExport = allColumns.stream()
                .filter(columns::contains)
                .collect(Collectors.toCollection(ArrayList::new));

        // Include grouping columns instead of group column
        int groupIndex = 0;
        Grid.Column<?> groupColumn = null;
        for (int i = 0; i < columnsToExport.size(); i++) {
            Grid.Column<Object> column = columnsToExport.get(i);
            if (groupDataGrid.isGroupColumn(column)) {
                groupIndex = i;
                groupColumn = column;
                break;
            }
        }
        Collection<Grid.Column<Object>> groupingColumns = groupDataGrid.getGroupingColumns();
        columnsToExport.addAll(groupIndex, groupingColumns);
        columnsToExport.remove(groupColumn);
        return Collections.unmodifiableList(columnsToExport);
    }

    /**
     * Returns a list of all columns (including those hidden by security) in the correct order for Jmix extensions
     * of {@link Grid}. Otherwise, it returns {@link Grid#getColumns()}.
     *
     * @param grid grid from which to get all columns
     * @return all (with hidden by security) columns list that has correct order
     */
    protected List<Grid.Column<Object>> getOrderedColumns(Grid<Object> grid) {
        if (grid instanceof DataGrid<Object> dataGrid) {
            return dataGrid.getAllColumns();
        } else if (grid instanceof TreeDataGrid<Object> treeDataGrid) {
            return treeDataGrid.getAllColumns();
        }
        return grid.getColumns();
    }

    /**
     * Returns the header text of a column as the grid shows it. A grouping column of a group data grid is not
     * attached to the grid, so its last specified header is used.
     *
     * @param dataGrid data grid or group data grid adapter
     * @param column   column of the grid
     * @return header text, empty if the header has no text
     */
    protected String getColumnHeaderText(Grid<Object> dataGrid, Grid.Column<?> column) {
        if (dataGrid instanceof AbstractGroupDataGridAdapter<Object> gridAdapter) {
            // Grouping column is not attached to the Grid,
            // so we should retrieve the last specified header.
            if (gridAdapter.getGroupingColumns().contains(column)
                    && column instanceof AbstractGroupDataGridColumnAdapter<?> columnAdapter) {
                String headerText = columnAdapter.getStoredHeaderText();
                if (!Strings.isNullOrEmpty(headerText)) {
                    return headerText;
                }

                Component headerComponent = columnAdapter.getStoredHeaderComponent();

                if (headerComponent instanceof HasText hasText) {
                    headerText = hasText.getText();
                } else if (headerComponent instanceof DataGridHeaderFilter dataGridHeaderFilter
                        && dataGridHeaderFilter.getHeader() instanceof HasText hasText) {
                    headerText = hasText.getText();
                }
                return Strings.nullToEmpty(headerText);
            }
        }
        return getColumnHeaderText(column);
    }

    /**
     * Returns the header text of a column attached to a grid, taken from the default header row when other header
     * rows are appended.
     *
     * @param column column attached to a grid
     * @return header text, empty if the header has no text
     */
    protected String getColumnHeaderText(Grid.Column<?> column) {
        String headerText = !isHeaderRowAppended(column)
                ? column.getHeaderText()
                : getDefaultHeaderText(column);

        if (!Strings.isNullOrEmpty(headerText)) {
            return headerText;
        }

        Component headerComponent = getDefaultHeaderComponent(column);

        if (headerComponent instanceof HasText hasText) {
            headerText = hasText.getText();
        } else if (headerComponent instanceof DataGridHeaderFilter dataGridHeaderFilter
                && dataGridHeaderFilter.getHeader() instanceof HasText hasText) {
            headerText = hasText.getText();
        }
        return Strings.nullToEmpty(headerText);
    }

    /**
     * Checks whether header rows were appended to the column's grid, so the column is not a direct child of the grid
     * any more and its own header text is not the one the grid shows.
     *
     * @param column column attached to a grid
     * @return {@code true} if header rows were appended to the column's grid
     */
    protected boolean isHeaderRowAppended(Grid.Column<?> column) {
        return column.getParent().isPresent() && !(column.getParent().get() instanceof DataGrid<?>);
    }

    /**
     * Returns the text of the column's cell in the grid's default header row.
     *
     * @param column column attached to a grid
     * @return text of the column's cell in the default header row, or {@code null} if the grid has no default
     * header row
     */
    @Nullable
    protected String getDefaultHeaderText(Grid.Column<?> column) {
        HeaderRow defaultHeaderRow = getDefaultHeaderRow(column);

        return defaultHeaderRow == null
                ? null
                : defaultHeaderRow.getCell(column).getText();
    }

    /**
     * Returns the component of the column's cell in the grid's default header row.
     *
     * @param column column attached to a grid
     * @return component of the column's cell in the default header row, or {@code null} if the grid has no default
     * header row or the cell has no component
     */
    @Nullable
    protected Component getDefaultHeaderComponent(Grid.Column<?> column) {
        HeaderRow defaultHeaderRow = getDefaultHeaderRow(column);

        return defaultHeaderRow == null
                ? null
                : defaultHeaderRow.getCell(column).getComponent();
    }

    @Nullable
    protected HeaderRow getDefaultHeaderRow(Grid.Column<?> column) {
        if (column.getGrid() instanceof JmixGrid<?> jmixGrid) {
            return jmixGrid.getDefaultHeaderRow();
        } else if (column.getGrid() instanceof JmixTreeGrid<?> jmixTreeGrid) {
            return jmixTreeGrid.getDefaultHeaderRow();
        }
        return null;
    }

    protected Function<Object, InstanceContainer<Object>> createInstanceContainerProvider(
            Grid<?> dataGrid, Object item) {
        return entity -> {
            throw new UnsupportedOperationException("ExcelExporter doesn't provider instance container");
        };
    }

    protected String formatValue(@Nullable Object cellValue, MetaPropertyPath metaPropertyPath) {
        if (cellValue == null) {
            if (metaPropertyPath.getRange().isDatatype()) {
                Class<?> javaClass = metaPropertyPath.getRange().asDatatype().getJavaClass();
                if (Boolean.class.equals(javaClass)) {
                    cellValue = false;
                }

            } else {
                return "";
            }
        }

        if (cellValue instanceof Number) {
            Number n = (Number) cellValue;
            Datatype<?> datatype = null;

            Range range = metaPropertyPath.getMetaProperty().getRange();
            if (range.isDatatype()) {
                datatype = range.asDatatype();
            }

            datatype = datatype == null ? datatypeRegistry.get(n.getClass()) : datatype;
            return datatype.format(n);
        } else if (cellValue instanceof Date) {
            Class<?> javaClass = null;
            MetaProperty metaProperty = metaPropertyPath.getMetaProperty();

            if (metaProperty.getRange().isDatatype()) {
                javaClass = metaProperty.getRange().asDatatype().getJavaClass();
            }
            Date date = ((Date) cellValue);

            if (Objects.equals(java.sql.Time.class, javaClass)) {
                return datatypeRegistry.get(java.sql.Time.class).format(date);
            } else if (Objects.equals(java.sql.Date.class, javaClass)) {
                return datatypeRegistry.get(java.sql.Date.class).format(date);
            } else {
                return datatypeRegistry.get(Date.class).format(date);
            }
        } else {
            return formatValue(cellValue);
        }
    }

    protected String formatValue(@Nullable Object cellValue) {
        if (cellValue == null) {
            return "";
        }

        if (cellValue instanceof Number) {
            Number n = (Number) cellValue;
            Datatype<?> datatype = datatypeRegistry.get(n.getClass());
            return datatype.format(n);
        } else if (cellValue instanceof java.sql.Time) {
            return datatypeRegistry.get(java.sql.Time.class).format(cellValue);
        } else if (cellValue instanceof java.sql.Date) {
            return datatypeRegistry.get(java.sql.Date.class).format(cellValue);
        } else if (cellValue instanceof Date) {
            return datatypeRegistry.get(Date.class).format(cellValue);
        } else if (cellValue instanceof Boolean) {
            return String.valueOf(cellValue);
        } else if (cellValue instanceof Enum) {
            return messages.getMessage((Enum<?>) cellValue);
        } else if (cellValue instanceof Entity) {
            return metadataTools.getInstanceName(cellValue);
        } else {
            return cellValue.toString();
        }
    }
}
