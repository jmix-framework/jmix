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

package com.haulmont.cuba.web.gui.components;

import com.haulmont.cuba.core.global.View;
import com.haulmont.cuba.gui.components.LookupComponent;
import com.haulmont.cuba.gui.components.RowsCount;
import com.haulmont.cuba.gui.components.TreeTable;
import com.haulmont.cuba.gui.components.columnmanager.ColumnManager;
import com.haulmont.cuba.gui.components.data.meta.DatasourceDataUnit;
import com.haulmont.cuba.gui.components.data.table.AggregatableTableItems;
import com.haulmont.cuba.gui.components.data.table.DatasourceTableItems;
import com.haulmont.cuba.gui.components.table.CubaLinkCellClickListener;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.DsBuilder;
import com.haulmont.cuba.gui.data.impl.DatasourceImplementation;
import com.haulmont.cuba.gui.presentation.LegacyPresentationsDelegate;
import com.haulmont.cuba.gui.presentation.Presentations;
import com.haulmont.cuba.settings.component.LegacySettingsDelegate;
import com.haulmont.cuba.settings.converter.LegacyTreeTableSettingsConverter;
import com.haulmont.cuba.web.gui.components.table.CubaTableFieldFactoryImpl;
import com.haulmont.cuba.web.gui.components.table.TableDelegate;
import io.jmix.core.Entity;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.datatoolsui.accesscontext.UiShowEntityInfoContext;
import io.jmix.datatoolsui.action.ShowEntityInfoAction;
import io.jmix.ui.component.AggregationInfo;
import io.jmix.ui.component.Table;
import io.jmix.ui.component.data.TableItems;
import io.jmix.ui.component.data.meta.EntityTableItems;
import io.jmix.ui.component.impl.AbstractTable;
import io.jmix.ui.component.impl.TreeTableImpl;
import io.jmix.ui.component.impl.WrapperUtils;
import io.jmix.ui.component.presentation.TablePresentationsLayout;
import io.jmix.ui.component.table.TableFieldFactoryImpl;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.presentation.TablePresentations;
import io.jmix.ui.presentation.model.TablePresentation;
import io.jmix.ui.settings.component.binder.ComponentSettingsBinder;
import io.jmix.ui.widget.JmixEnhancedTable;
import io.jmix.ui.widget.data.AggregationContainer;
import org.dom4j.Element;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Consumer;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

@Deprecated
public class WebTreeTable<E extends Entity> extends TreeTableImpl<E>
        implements TreeTable<E>, LookupComponent.LookupSelectionChangeNotifier<E>, ColumnManager {

    protected LegacySettingsDelegate settingsDelegate;
    protected LegacyPresentationsDelegate presentationsDelegate;
    protected TableDelegate tableDelegate;

    protected boolean usePresentations;

    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();

        settingsDelegate = createSettingsDelegate();
        tableDelegate = createTableDelegate();
    }

    @Override
    public void applyDataLoadingSettings(Element element) {
        settingsDelegate.applyDataLoadingSettings(element);
    }

    @Override
    public void applySettings(Element element) {
        settingsDelegate.applySettings(element);
    }

    @Override
    public boolean saveSettings(Element element) {
        return settingsDelegate.saveSettings(element);
    }

    @Override
    public boolean isSettingsEnabled() {
        return settingsDelegate.isSettingsEnabled();
    }

    @Override
    public void setSettingsEnabled(boolean settingsEnabled) {
        settingsDelegate.setSettingsEnabled(settingsEnabled);
    }

    @Override
    protected ComponentSettingsBinder getSettingsBinder() {
        return settingsRegistry.getSettingsBinder(this.getClass());
    }

    protected LegacySettingsDelegate createSettingsDelegate() {
        return (LegacySettingsDelegate) applicationContext.getBean(LegacySettingsDelegate.NAME,
                this, new LegacyTreeTableSettingsConverter(), getSettingsBinder());
    }

    @Override
    protected TablePresentations createTablePresentations() {
        Presentations presentations = applicationContext.getBean(Presentations.class, this);

        presentationsDelegate = applicationContext.getBean(LegacyPresentationsDelegate.class,
                this, presentations, getSettingsBinder());

        return presentations;
    }

    @Override
    protected TablePresentationsLayout createTablePresentationsLayout() {
        TablePresentationsLayout layout = super.createTablePresentationsLayout();
        return presentationsDelegate.createTablePresentationsLayout(layout);
    }

    @Override
    protected void updatePresentationSettings(TablePresentations p) {
        if (settingsDelegate.isLegacySettings(getFrame())) {
            presentationsDelegate.updatePresentationSettings((Presentations) p);
        } else {
            super.updatePresentationSettings(p);
        }
    }

    @Override
    protected void applyPresentationSettings(TablePresentation p) {
        if (settingsDelegate.isLegacySettings(getFrame())) {
            presentationsDelegate.applyPresentationSettings(p);
        } else {
            super.applyPresentationSettings(p);
        }
    }

    @Override
    public void resetPresentation() {
        if (settingsDelegate.isLegacySettings(getFrame())) {
            presentationsDelegate.resetPresentations(settingsDelegate.getDefaultSettings());
        } else {
            super.resetPresentation();
        }
    }

    @Override
    public void usePresentations(boolean b) {
        this.usePresentations = b;
    }

    @Override
    public boolean isUsePresentations() {
        return usePresentations;
    }

    @Override
    public void loadPresentations() {
        if (isUsePresentations()) {
            super.loadPresentations();
        } else {
            throw new UnsupportedOperationException("Component doesn't use presentations");
        }
    }

    @Nullable
    @Override
    public TablePresentations getPresentations() {
        if (isUsePresentations()) {
            return super.getPresentations();
        } else {
            throw new UnsupportedOperationException("Component doesn't use presentations");
        }
    }

    @Override
    public void applyPresentation(Object id) {
        if (isUsePresentations()) {
            super.applyPresentation(id);
        } else {
            throw new UnsupportedOperationException("Component doesn't use presentations");
        }
    }

    @Override
    public void applyPresentationAsDefault(Object id) {
        if (isUsePresentations()) {
            super.applyPresentationAsDefault(id);
        } else {
            throw new UnsupportedOperationException("Component doesn't use presentations");
        }
    }

    @Override
    protected void handlePresentationVariables(Map<String, Object> variables) {
        if (isUsePresentations()) {
            super.handlePresentationVariables(variables);
        }
    }

    @Override
    public void setItems(@Nullable TableItems<E> tableItems) {
        super.setItems(tableItems);

        if (tableItems != null) {
            if (getRowsCount() != null) {
                getRowsCount().setRowsCountTarget(this);
            }
        }

        initShowEntityInfoAction();
    }

    protected void initShowEntityInfoAction() {
        UiShowEntityInfoContext showInfoContext = new UiShowEntityInfoContext();
        accessManager.applyRegisteredConstraints(showInfoContext);

        if (showInfoContext.isPermitted()) {
            if (getAction(ShowEntityInfoAction.ID) == null) {
                addAction(actions.create(ShowEntityInfoAction.ID));
            }
        }
    }

    @Nullable
    @Override
    public RowsCount getRowsCount() {
        return tableDelegate.getRowsCount();
    }

    @Override
    public void setRowsCount(@Nullable RowsCount rowsCount) {
        tableDelegate.setRowsCount(rowsCount, topPanel, this::createTopPanel, componentComposition,
                this::updateCompositionStylesTopPanelVisible);
    }

    protected TableDelegate createTableDelegate() {
        return (TableDelegate) applicationContext.getBean(TableDelegate.NAME);
    }

    @Override
    protected Map<Object, Object> __aggregateValues(AggregationContainer container, AggregationContainer.Context context) {
        if (getItems() instanceof AggregatableTableItems) {
            List<AggregationInfo> aggregationInfos = getAggregationInfos(container);

            Map<AggregationInfo, Object> results = ((AggregatableTableItems<E>) getItems()).aggregateValues(
                    aggregationInfos.toArray(new AggregationInfo[0]),
                    context.getItemIds()
            );

            return convertAggregationKeyMapToColumnIdKeyMap(container, results);
        } else {
            return super.__aggregateValues(container, context);
        }
    }

    @Override
    protected Map<Object, Object> __aggregate(AggregationContainer container, AggregationContainer.Context context) {
        if (getItems() instanceof AggregatableTableItems) {
            List<AggregationInfo> aggregationInfos = getAggregationInfos(container);

            Map<AggregationInfo, String> results = ((AggregatableTableItems<E>) getItems()).aggregate(
                    aggregationInfos.toArray(new AggregationInfo[0]),
                    context.getItemIds()
            );

            Map<Object, Object> resultsByColumns = convertAggregationKeyMapToColumnIdKeyMap(container, results);

            if (aggregationCells != null) {
                resultsByColumns = __handleAggregationResults(context, resultsByColumns);
            }
            return resultsByColumns;
        } else {
            return super.__aggregate(container, context);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void removeLookupValueChangeListener(Consumer<LookupSelectionChangeEvent<E>> listener) {
        unsubscribe(LookupSelectionChangeEvent.class, (Consumer) listener);
    }

    @Override
    public void refresh() {
        TableItems<E> tableItems = getItems();
        if (tableItems instanceof DatasourceTableItems) {
            ((DatasourceTableItems) tableItems).getDatasource().refresh();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Datasource getItemDatasource(Entity item) {
        if (fieldDatasources == null) {
            fieldDatasources = new WeakHashMap<>();
        }

        Object fieldDatasource = fieldDatasources.get(item);
        if (fieldDatasource instanceof Datasource) {
            return (Datasource) fieldDatasource;
        }

        EntityTableItems containerTableItems = (EntityTableItems) getItems();
        Datasource datasource = DsBuilder.create()
                .setAllowCommit(false)
                .setMetaClass(containerTableItems.getEntityMetaClass())
                .setRefreshMode(CollectionDatasource.RefreshMode.NEVER)
                .setViewName(View.LOCAL)
                .buildDatasource();

        ((DatasourceImplementation) datasource).valid();

        datasource.setItem(item);
        fieldDatasources.put(item, datasource);

        return datasource;
    }

    @Override
    public void sortBy(Object propertyId, boolean ascending) {
        if (isSortable()) {
            component.setSortOptions(propertyId, ascending);
            component.sort();
        }
    }

    @Override
    public void removeColumnCollapseListener(Consumer<ColumnCollapseEvent<E>> listener) {
        internalRemoveColumnCollapseListener(listener);
    }

    @Override
    protected Consumer<Table.Column.ClickEvent> createLinkCellClickListener() {
        return new CubaLinkCellClickListener(this, applicationContext);
    }

    @Override
    public void setCellClickListener(String columnId, Consumer<CellClickEvent<E>> clickListener) {
        io.jmix.ui.component.Table.Column<E> column = getColumn(columnId);
        checkNotNullArgument(column, String.format("column with id '%s' not found", columnId));

        column.addClickListener(clickEvent -> {
            CellClickEvent<E> cellClickEvent = new CellClickEvent<>(this, clickEvent.getItem(), columnId);
            clickListener.accept(cellClickEvent);
        });
    }

    @Override
    public void removeClickListener(String columnId) {
        removeCellClickListener(columnId);
    }

    @Override
    public void setColumnCaption(String columnId, @Nullable String caption) {
        io.jmix.ui.component.Table.Column<E> column = getColumn(columnId);
        if (column == null) {
            throw new IllegalStateException(String.format("Column with id '%s' not found", columnId));
        }
        setColumnCaption(column, caption);
    }

    @Override
    public void setColumnCaption(io.jmix.ui.component.Table.Column column, @Nullable String caption) {
        checkNotNullArgument(column, "column must be non null");
        column.setCaption(caption);
    }

    @Override
    public void setColumnDescription(String columnId, @Nullable String description) {
        io.jmix.ui.component.Table.Column<E> column = getColumn(columnId);
        if (column == null) {
            throw new IllegalStateException(String.format("Column with id '%s' not found", columnId));
        }
        setColumnDescription(column, description);
    }

    @Override
    public void setColumnDescription(io.jmix.ui.component.Table.Column column, @Nullable String description) {
        checkNotNullArgument(column, "column must be non null");
        column.setDescription(description);
    }

    @Override
    public void setColumnAlignment(String columnId, ColumnAlignment alignment) {
        io.jmix.ui.component.Table.Column<E> column = getColumn(columnId);
        if (column == null) {
            throw new IllegalStateException(String.format("Column with id '%s' not found", columnId));
        }
        setColumnAlignment(column, alignment);
    }

    @Override
    public void setColumnAlignment(io.jmix.ui.component.Table.Column column, ColumnAlignment alignment) {
        checkNotNullArgument(column, "column must be non null");
        column.setAlignment(alignment);
    }

    @Override
    public void setColumnCollapsed(String columnId, boolean collapsed) {
        io.jmix.ui.component.Table.Column<E> column = getColumn(columnId);
        if (column == null) {
            throw new IllegalStateException(String.format("Column with id '%s' not found", columnId));
        }
        setColumnCollapsed(column, collapsed);
    }

    @Override
    public void setColumnCollapsed(io.jmix.ui.component.Table.Column column, boolean collapsed) {
        checkNotNullArgument(column, "column must be non null");
        column.setCollapsed(collapsed);
    }

    @Override
    public void setColumnWidth(String columnId, int width) {
        io.jmix.ui.component.Table.Column<E> column = getColumn(columnId);
        if (column == null) {
            throw new IllegalStateException(String.format("Column with id '%s' not found", columnId));
        }
        setColumnWidth(column, width);
    }

    @Override
    public void setColumnWidth(io.jmix.ui.component.Table.Column column, int width) {
        checkNotNullArgument(column, "column must be non null");
        column.setWidth(width);
    }

    @Override
    public void addAggregationProperty(String columnId, AggregationInfo.Type type) {
        addAggregationProperty(getColumn(columnId), type);
    }

    @Override
    public void addAggregationProperty(io.jmix.ui.component.Table.Column column, AggregationInfo.Type type) {
        checkNotNullArgument(column, "column must be non null");
        checkNotNullArgument(column.getAggregation(), "column aggregation must be non null");

        checkAggregation(column.getAggregation());

        component.addContainerPropertyAggregation(column.getId(), WrapperUtils.convertAggregationType(type));

        if (column.getAggregation() != null) {
            addAggregationCell(column);
        }
    }

    @Override
    public void removeAggregationProperty(String columnId) {
        io.jmix.ui.component.Table.Column<E> column = getColumn(columnId);
        if (column == null) {
            throw new IllegalStateException(String.format("Column with id '%s' not found", columnId));
        }

        component.removeContainerPropertyAggregation(column.getId());
        removeAggregationCell(column);
    }

    @Override
    public void setColumnSortable(String columnId, boolean sortable) {
        io.jmix.ui.component.Table.Column<E> column = getColumn(columnId);
        if (column == null) {
            throw new IllegalStateException(String.format("Column with id '%s' not found", columnId));
        }
        setColumnSortable(column, sortable);
    }

    @Override
    public void setColumnSortable(io.jmix.ui.component.Table.Column column, boolean sortable) {
        checkNotNullArgument(column, "column must be non null");

        column.setSortable(sortable);
    }

    @Override
    public void setColumnCaptionAsHtml(String columnId, boolean captionAsHtml) {
        io.jmix.ui.component.Table.Column<E> column = getColumn(columnId);
        if (column == null) {
            throw new IllegalStateException(String.format("Column with id '%s' not found", columnId));
        }
        setColumnCaptionAsHtml(column, captionAsHtml);
    }

    @Override
    public void setColumnCaptionAsHtml(io.jmix.ui.component.Table.Column column, boolean captionAsHtml) {
        checkNotNullArgument(column, "Column must be non null");
        column.setCaptionAsHtml(captionAsHtml);
    }

    @Override
    public void setColumnExpandRatio(io.jmix.ui.component.Table.Column column, float ratio) {
        checkNotNullArgument(column, "Column must be non null");
        column.setExpandRatio(ratio);
    }

    @Override
    public float getColumnExpandRatio(io.jmix.ui.component.Table.Column column) {
        checkNotNullArgument(column, "Column must be non null");
        return column.getExpandRatio();
    }

    @Override
    public void addCellClickListener(String columnId) {
        io.jmix.ui.component.Table.Column column = getColumn(columnId);
        checkNotNullArgument(column, String.format("column with id '%s' not found", columnId));
        component.addTableCellClickListener(columnId, this::onCellClick);
    }

    @Override
    public void removeCellClickListener(String columnId) {
        io.jmix.ui.component.Table.Column column = getColumn(columnId);
        checkNotNullArgument(column, String.format("column with id '%s' not found", columnId));
        component.removeTableCellClickListener(column.getId());
    }

    protected void onCellClick(JmixEnhancedTable.TableCellClickEvent event) {
        TableItems<E> tableItems = getItems();
        if (tableItems == null) {
            return;
        }

        E item = tableItems.getItem(event.getItemId());
        if (item == null) {
            return;
        }

        if (!(getColumn(String.valueOf(event.getColumnId())) instanceof com.haulmont.cuba.gui.components.Table.Column)) {
            return;
        }

        com.haulmont.cuba.gui.components.Table.Column<E> column =
                (com.haulmont.cuba.gui.components.Table.Column<E>) getColumn(String.valueOf(event.getColumnId()));
        io.jmix.ui.component.Table.Column.ClickEvent<E> clickEvent =
                new io.jmix.ui.component.Table.Column.ClickEvent<>(column, item, event.isText());
        column.fireClickEvent(clickEvent);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    protected ColumnImpl<E> createColumn(Object id, AbstractTable<?, E> owner) {
        com.haulmont.cuba.gui.components.Table.Column column = new com.haulmont.cuba.gui.components.Table.Column<>(id);
        column.setOwner(owner);
        return column;
    }

    @Override
    protected void setupAutowiredColumns(EntityTableItems<E> entityTableSource) {
        Collection<MetaPropertyPath> paths = getAutowiredProperties(entityTableSource);

        for (MetaPropertyPath metaPropertyPath : paths) {
            MetaProperty property = metaPropertyPath.getMetaProperty();
            if (!property.getRange().getCardinality().isMany()
                    && !metadataTools.isSystem(property)) {
                io.jmix.ui.component.Table.Column<E> column = addColumn(metaPropertyPath);

                String propertyName = property.getName();
                MetaClass propertyMetaClass = metadataTools.getPropertyEnclosingMetaClass(metaPropertyPath);

                column.setCaption(messageTools.getPropertyCaption(propertyMetaClass, propertyName));

                if (column instanceof com.haulmont.cuba.gui.components.Table.Column) {
                    ((com.haulmont.cuba.gui.components.Table.Column<E>) column)
                            .setType(metaPropertyPath.getRangeJavaClass());
                }
            }
        }
    }

    @Override
    protected Collection<MetaPropertyPath> getAutowiredProperties(EntityTableItems<E> entityTableSource) {
        if (entityTableSource instanceof DatasourceDataUnit) {
            CollectionDatasource datasource = ((DatasourceDataUnit) entityTableSource).getDatasource();

            return datasource.getView() != null ?
                    // if a view is specified - use view properties
                    metadataTools.getFetchPlanPropertyPaths(datasource.getView(), datasource.getMetaClass()) :
                    // otherwise use all properties from meta-class
                    metadataTools.getPropertyPaths(datasource.getMetaClass());
        }

        return super.getAutowiredProperties(entityTableSource);
    }

    @Override
    protected void clearFieldDatasources() {
        if (fieldDatasources == null) {
            return;
        }

        // detach instance containers from entities explicitly
        for (Map.Entry<Object, Object> entry : fieldDatasources.entrySet()) {
            if (entry.getValue() instanceof InstanceContainer) {
                InstanceContainer container = (InstanceContainer) entry.getValue();
                container.setItem(null);
            } else if (entry.getValue() instanceof Datasource) {
                Datasource datasource = (Datasource) entry.getValue();
                datasource.setItem(null);
            }
        }

        fieldDatasources.clear();
    }

    @Nullable
    @Override
    protected Class getColumnType(io.jmix.ui.component.Table.Column<E> column) {
        return column instanceof com.haulmont.cuba.gui.components.Table.Column
                ? ((com.haulmont.cuba.gui.components.Table.Column<E>) column).getType()
                : super.getColumnType(column);
    }

    @Override
    protected TableFieldFactoryImpl createFieldFactory() {
        return new CubaTableFieldFactoryImpl<>(this, accessManager, metadataTools, uiComponentsGenerator);
    }
}
