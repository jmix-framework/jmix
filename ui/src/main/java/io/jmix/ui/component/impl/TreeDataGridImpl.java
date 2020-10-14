/*
 * Copyright 2019 Haulmont.
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

package io.jmix.ui.component.impl;

import com.vaadin.data.TreeData;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.TreeDataProvider;
import com.vaadin.shared.Registration;
import com.vaadin.ui.Grid;
import io.jmix.core.common.event.Subscription;
import io.jmix.core.common.util.Preconditions;
import io.jmix.ui.component.TreeDataGrid;
import io.jmix.ui.component.data.BindingState;
import io.jmix.ui.component.data.DataGridItems;
import io.jmix.ui.component.data.TreeDataGridItems;
import io.jmix.ui.component.datagrid.DataGridDataProvider;
import io.jmix.ui.component.datagrid.HierarchicalDataGridDataProvider;
import io.jmix.ui.widget.JmixTreeGrid;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

public class TreeDataGridImpl<E> extends AbstractDataGrid<JmixTreeGrid<E>, E>
        implements TreeDataGrid<E> {

    protected Predicate<E> itemCollapseAllowedProvider = t -> true;

    protected Registration expandListener;
    protected Registration collapseListener;

    protected Column<E> hierarchyColumn;

    @Override
    protected JmixTreeGrid<E> createComponent() {
        return new JmixTreeGrid<>();
    }

    @Override
    protected void onColumnReorder(Grid.ColumnReorderEvent e) {
        super.onColumnReorder(e);

        String[] columnOrder = getColumnOrder();
        // if the hierarchy column isn't set explicitly,
        // we set the first column as the hierarchy column
        if (getHierarchyColumn() == null
                && columnOrder.length > 0) {
            String columnId = columnOrder[0];

            Grid.Column<E, ?> newHierarchyColumn = component.getColumn(columnId);
            setHierarchyColumnInternal(newHierarchyColumn);
        }
    }

    @Override
    public int getLevel(E item) {
        return component.getLevel(item);
    }

    @Nullable
    @Override
    public TreeDataGridItems<E> getItems() {
        return (TreeDataGridItems<E>) super.getItems();
    }

    protected TreeDataGridItems<E> getTreeDataGridItemsNN() {
        TreeDataGridItems<E> dataGridItems = getItems();
        if (dataGridItems == null
                || dataGridItems.getState() == BindingState.INACTIVE) {
            throw new IllegalStateException("DataGridItems is not active");
        }
        return dataGridItems;
    }

    @Override
    public void setItems(@Nullable DataGridItems<E> dataGridItems) {
        if (dataGridItems != null
                && !(dataGridItems instanceof TreeDataGridItems)) {
            throw new IllegalArgumentException("TreeDataGrid supports only TreeDataGridItems data binding");
        }

        super.setItems(dataGridItems);
    }

    @Override
    protected DataProvider<E, ?> createEmptyDataProvider() {
        return new TreeDataProvider<>(new TreeData<>());
    }

    @Override
    protected void initComponent(Grid<E> component) {
        super.initComponent(component);

        JmixTreeGrid<E> treeGrid = (JmixTreeGrid<E>) component;
        treeGrid.setItemCollapseAllowedProvider(itemCollapseAllowedProvider::test);
    }

    @Override
    protected DataGridDataProvider<E> createDataGridDataProvider(DataGridItems<E> dataGridItems) {
        return new HierarchicalDataGridDataProvider<>((TreeDataGridItems<E>) dataGridItems, this);
    }

    @Override
    protected void editItemInternal(E itemToEdit) {
        if (!isItemVisible(itemToEdit)) {
            component.expandItemWithParents(itemToEdit);
        }

        int rowIndex = getVisibleItemsConsideringHierarchy().indexOf(itemToEdit);
        component.getEditor().editRow(rowIndex);
    }

    protected List<E> getVisibleItemsConsideringHierarchy() {
        return component.getDataCommunicator()
                .fetchItemsWithRange(0, getTreeDataGridItemsNN().size());
    }

    protected boolean isItemVisible(E item) {
        E parent = getTreeDataGridItemsNN().getParent(item);
        return parent == null || isExpanded(parent) && isItemVisible(parent);
    }

    @Override
    public Predicate<E> getItemCollapseAllowedProvider() {
        return itemCollapseAllowedProvider;
    }

    @Override
    public void setItemCollapseAllowedProvider(Predicate<E> provider) {
        checkNotNullArgument(provider);

        this.itemCollapseAllowedProvider = provider;
        // We reset a provider to the component in order to trigger the data update
        component.setItemCollapseAllowedProvider(provider::test);
    }

    @Nullable
    @Override
    public Column<E> getHierarchyColumn() {
        return hierarchyColumn;
    }

    @Override
    public void setHierarchyColumn(String id) {
        setHierarchyColumn(getColumnNN(id));
    }

    @Override
    public void setHierarchyColumn(Column<E> column) {
        checkNotNullArgument(column);

        this.hierarchyColumn = column;

        Grid.Column<E, ?> newHierarchyColumn = ((ColumnImpl<E>) column).getGridColumn();
        setHierarchyColumnInternal(newHierarchyColumn);
    }

    protected void setHierarchyColumnInternal(Grid.Column<E, ?> newHierarchyColumn) {
        Grid.Column<E, ?> prevHierarchyColumn = component.getHierarchyColumn();
        component.setHierarchyColumn(newHierarchyColumn);

        // Due to Vaadin bug, we need to reset column's
        // collapsible state after changing the hierarchy column
        if (prevHierarchyColumn != null
                && !newHierarchyColumn.equals(prevHierarchyColumn)) {
            updateColumnCollapsible(prevHierarchyColumn);
        }
    }

    protected void updateColumnCollapsible(Grid.Column<E, ?> vColumn) {
        ColumnImpl<E> column = getColumnByGridColumn(vColumn);
        if (column != null) {
            column.updateCollapsible();
        }
    }

    @Override
    public void expand(Collection<E> items) {
        component.expand(items);
    }

    @Override
    public void expandRecursively(Stream<E> items, int depth) {
        component.expandRecursively(items, depth);
    }

    @Override
    public void expandAll() {
        if (getItems() != null) {
            expandRecursively(getItems().getChildren(null), Integer.MAX_VALUE);
        }
    }

    @Override
    public void collapse(Collection<E> items) {
        component.collapse(items);
    }

    @Override
    public void collapseRecursively(Stream<E> items, int depth) {
        component.collapseRecursively(items, depth);
    }

    @Override
    public void collapseAll() {
        if (getItems() != null) {
            collapseRecursively(getItems().getChildren(null), Integer.MAX_VALUE);
        }
    }

    @Override
    public boolean isExpanded(E item) {
        return component.isExpanded(item);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Subscription addExpandListener(Consumer<ExpandEvent<E>> listener) {
        if (expandListener == null) {
            expandListener = component.addExpandListener(this::onItemExpand);
        }

        return getEventHub().subscribe(ExpandEvent.class, (Consumer) listener);
    }

    protected void onItemExpand(com.vaadin.event.ExpandEvent<E> e) {
        ExpandEvent<E> event = new ExpandEvent<>(TreeDataGridImpl.this,
                e.getExpandedItem(), e.isUserOriginated());
        publish(ExpandEvent.class, event);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Subscription addCollapseListener(Consumer<CollapseEvent<E>> listener) {
        if (collapseListener == null) {
            collapseListener = component.addCollapseListener(this::onItemCollapse);
        }

        return getEventHub().subscribe(CollapseEvent.class, (Consumer) listener);
    }

    protected void onItemCollapse(com.vaadin.event.CollapseEvent<E> e) {
        CollapseEvent<E> event = new CollapseEvent<>(TreeDataGridImpl.this,
                e.getCollapsedItem(), e.isUserOriginated());
        publish(CollapseEvent.class, event);
    }

    @Override
    public void scrollTo(E item, ScrollDestination destination) {
        Preconditions.checkNotNullArgument(item);
        Preconditions.checkNotNullArgument(destination);

        int rowIndex = getVisibleItemsConsideringHierarchy().indexOf(item);
        if (rowIndex == -1) {
            return;
        }

        component.scrollTo(rowIndex, WrapperUtils.convertToGridScrollDestination(destination));
    }
}
