/*
 * Copyright 2025 Haulmont.
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

package io.jmix.flowui.component.groupgrid.adapter;

import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.common.util.Preconditions;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.grid.EnhancedDataGrid;
import io.jmix.flowui.component.groupgrid.GroupColumn;
import io.jmix.flowui.component.groupgrid.GroupInfo;
import io.jmix.flowui.component.groupgrid.GroupListDataComponent;
import io.jmix.flowui.component.groupgrid.data.GroupDataGridItems;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * A base class that adapts an implementation of {@link GroupListDataComponent} to use it as a
 * {@link DataGrid} in other components and modules (e.g., in export actions).
 *
 * @param <E> the type of items
 * @see GroupDataGridAdapterFactory
 * @see GroupDataGridAdapterProvider
 */
public abstract class AbstractGroupDataGridAdapter<E> extends DataGrid<E> implements GroupListDataComponent<E>,
        EnhancedDataGrid<E> {

    protected GroupListDataComponent<E> groupDataGrid;

    public AbstractGroupDataGridAdapter(GroupListDataComponent<E> groupDataGrid) {
        Preconditions.checkNotNullArgument(groupDataGrid);
        this.groupDataGrid = groupDataGrid;
    }

    /**
     * @return the underlying {@link GroupListDataComponent} instance that is adapted
     */
    public GroupListDataComponent<E> getAdaptee() {
        return groupDataGrid;
    }

    /**
     * @return the collection of columns that are used for grouping data in the grid
     */
    public abstract Collection<Column<E>> getGroupingColumns();

    /**
     * Checks whether the given column is an implementation of {@link GroupColumn}.
     *
     * @param column the column to check
     * @return {@code true} if the column is a group column
     */
    public abstract boolean isGroupColumn(Column<E> column);

    @Nullable
    @Override
    public GroupDataGridItems<E> getItems() {
        return getItemsInternal();
    }

    @Override
    public void groupByKeys(String... keys) {
        groupDataGrid.groupByKeys(keys);
    }

    @Override
    public void groupByKeysList(List<String> keys) {
        groupDataGrid.groupByKeysList(keys);
    }

    @Override
    public void ungroup() {
        groupDataGrid.ungroup();
    }

    @Override
    public void ungroupByKeys(String... keys) {
        groupDataGrid.ungroupByKeys(keys);
    }

    @Override
    public void ungroupByKeysList(List<String> keys) {
        groupDataGrid.ungroupByKeysList(keys);
    }

    @Override
    public void expand(GroupInfo group) {
        groupDataGrid.expand(group);
    }

    @Override
    public void expandByPath(E item) {
        groupDataGrid.expandByPath(item);
    }

    @Override
    public void expandAll() {
        groupDataGrid.expandAll();
    }

    @Override
    public void collapse(GroupInfo group) {
        groupDataGrid.collapse(group);
    }

    @Override
    public void collapseByPath(E item) {
        groupDataGrid.collapseByPath(item);
    }

    @Override
    public void collapseAll() {
        groupDataGrid.collapseAll();
    }

    @Override
    public boolean isExpanded(GroupInfo group) {
        return groupDataGrid.isExpanded(group);
    }

    @Override
    public List<GroupInfo> getRootGroups() {
        return groupDataGrid.getRootGroups();
    }

    @Override
    public List<GroupInfo> getChildren(GroupInfo group) {
        return groupDataGrid.getChildren(group);
    }

    @Override
    public boolean hasChildren(GroupInfo groupInfo) {
        return groupDataGrid.hasChildren(groupInfo);
    }

    @Override
    public boolean hasGroups() {
        return groupDataGrid.hasGroups();
    }

    @Override
    public Registration addCollapseListener(Consumer<CollapseEvent<E>> listener) {
        return groupDataGrid.addCollapseListener(listener);
    }

    @Override
    public Registration addExpandListener(Consumer<ExpandEvent<E>> listener) {
        return groupDataGrid.addExpandListener(listener);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected BiFunction<Renderer<E>, String, Column<E>> getDefaultColumnFactory() {
        return (BiFunction) getDefaultColumnFactoryInternal();
    }

    protected abstract BiFunction<Renderer<E>, String, AbstractGroupDataGridColumnAdapter<E>> getDefaultColumnFactoryInternal();

    @Nullable
    protected abstract GroupDataGridItems<E> getItemsInternal();
}
