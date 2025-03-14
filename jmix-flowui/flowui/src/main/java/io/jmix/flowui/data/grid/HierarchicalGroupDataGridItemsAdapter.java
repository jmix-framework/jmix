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

package io.jmix.flowui.data.grid;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.vaadin.flow.data.provider.AbstractDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.Metadata;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.flowui.data.BindingState;
import org.springframework.lang.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class HierarchicalGroupDataGridItemsAdapter<T> extends AbstractDataProvider<T, Void>
        implements HierarchicalGroupDataGridItems<T> {

    protected final Metadata metadata;
    protected final GroupDataGridItems<T> dataGridItems;

    protected BiMap<T, GroupInfo> childGroupRows = HashBiMap.create();
    protected BiMap<T, GroupInfo> rootGroupRows = HashBiMap.create();

    public HierarchicalGroupDataGridItemsAdapter(GroupDataGridItems<T> dataGridItems, Metadata metadata) {
        this.dataGridItems = dataGridItems;
        this.metadata = metadata;

        dataGridItems.addItemSetChangeListener(this::onItemSetChange);
    }

    protected void onItemSetChange(ItemSetChangeEvent<T> event) {
        updateGroupRows();
    }

    protected void updateGroupRows() {
        childGroupRows.clear();
        rootGroupRows.clear();

        List<GroupInfo> rootGroups = dataGridItems.getRootGroups();
        for (GroupInfo rootGroup : rootGroups) {
            T groupRowItem = createGroupRow(rootGroup);
            rootGroupRows.put(groupRowItem, rootGroup);

            collectGroupRowsRecursively(rootGroup, childGroupRows);
        }
    }

    protected void collectGroupRowsRecursively(GroupInfo group, BiMap<T, GroupInfo> childGroupRows) {
        if (!dataGridItems.hasChildren(group)) {
            return;
        }
        List<GroupInfo> children = dataGridItems.getChildren(group);
        for (GroupInfo childGroup : children) {
            T groupRowItem = createGroupRow(childGroup);
            childGroupRows.put(groupRowItem, childGroup);

            collectGroupRowsRecursively(childGroup, childGroupRows);
        }
    }

    @Override
    public int size(Query<T, Void> query) {
        if (dataGridItems.getState() == BindingState.INACTIVE) {
            return 0;
        }
        return Math.toIntExact(fetch(query).count());
    }

    @Override
    public Stream<T> fetch(Query<T, Void> query) {
        if (dataGridItems.getState() == BindingState.INACTIVE) {
            return Stream.empty();
        }

        return dataGridItems.getItems().stream()
                .skip(query.getOffset())
                .limit(query.getLimit());
    }

    @Override
    public int getChildCount(HierarchicalQuery<T, Void> query) {
        if (getState() == BindingState.INACTIVE) {
            return 0;
        }
        return Math.toIntExact(fetchChildren(query).count());
    }

    @Override
    public Stream<T> fetchChildren(HierarchicalQuery<T, Void> query) {
        if (getState() == BindingState.INACTIVE) {
            return Stream.empty();
        }
        T parent = query.getParent();
        return collectOwnChildren(parent);
    }

    @Override
    public boolean hasChildren(T item) {
        return isGroupRow(item);
    }

    @Override
    public boolean isInMemory() {
        return true;
    }

    @Override
    public void groupBy(MetaPropertyPath[] properties) {
        dataGridItems.groupBy(properties);
    }

    @Override
    public List<GroupInfo> getRootGroups() {
        return dataGridItems.getRootGroups();
    }

    @Override
    public List<GroupInfo> getChildren(GroupInfo groupId) {
        return dataGridItems.getChildren(groupId);
    }

    @Override
    public boolean hasChildren(GroupInfo groupId) {
        return dataGridItems.hasChildren(groupId);
    }

    @Override
    public List<T> getOwnChildItems(GroupInfo groupId) {
        return dataGridItems.getOwnChildItems(groupId);
    }

    @Override
    public List<T> getChildItems(GroupInfo groupId) {
        return dataGridItems.getChildItems(groupId);
    }

    @Nullable
    @Override
    public GroupInfo getParentGroup(T item) {
        return dataGridItems.getParentGroup(item);
    }

    @Override
    public List<GroupInfo> getGroupPath(T item) {
        return dataGridItems.getGroupPath(item);
    }

    @Override
    public Collection<T> getGroupItems(GroupInfo groupId) {
        return dataGridItems.getGroupItems(groupId);
    }

    @Override
    public int getGroupItemsCount(GroupInfo groupId) {
        return dataGridItems.getGroupItemsCount(groupId);
    }

    @Override
    public boolean hasGroups() {
        return dataGridItems.hasGroups();
    }

    @Override
    public Collection<MetaPropertyPath> getGroupProperties() {
        return dataGridItems.getGroupProperties();
    }

    @Override
    public boolean containsGroup(GroupInfo groupId) {
        return dataGridItems.containsGroup(groupId);
    }

    @Override
    public Collection<T> getItems() {
        return dataGridItems.getItems();
    }

    @Override
    public T getItem(Object itemId) {
        return dataGridItems.getItem(itemId);
    }

    @Override
    public Object getItemValue(Object itemId, MetaPropertyPath propertyId) {
        return dataGridItems.getItemValue(itemId, propertyId);
    }

    @Override
    public T getSelectedItem() {
        return dataGridItems.getSelectedItem();
    }

    @Override
    public void setSelectedItem(T item) {
        dataGridItems.setSelectedItem(item);
    }

    @Override
    public Registration addValueChangeListener(Consumer<ValueChangeEvent<T>> listener) {
        return dataGridItems.addValueChangeListener(listener);
    }

    @Override
    public Registration addItemSetChangeListener(Consumer<ItemSetChangeEvent<T>> listener) {
        return dataGridItems.addItemSetChangeListener(listener);
    }

    @Override
    public Registration addSelectedItemChangeListener(Consumer<SelectedItemChangeEvent<T>> listener) {
        return dataGridItems.addSelectedItemChangeListener(listener);
    }

    @Override
    public boolean containsItem(T item) {
        return dataGridItems.containsItem(item);
    }

    @Override
    public BindingState getState() {
        return dataGridItems.getState();
    }

    @Override
    public Registration addStateChangeListener(Consumer<StateChangeEvent> listener) {
        return dataGridItems.addStateChangeListener(listener);
    }

    @Override
    public Class<T> getType() {
        return dataGridItems.getType();
    }

    @Nullable
    @Override
    public GroupInfo getGroupByItem(T groupItem) {
        if (rootGroupRows.containsKey(groupItem)) {
            return rootGroupRows.get(groupItem);
        }
        if (childGroupRows.containsKey(groupItem)) {
            return childGroupRows.get(groupItem);
        }
        return null;
    }

    @Nullable
    @Override
    public T getItemByGroup(GroupInfo group) {
        if (rootGroupRows.inverse().containsKey(group)) {
            return rootGroupRows.inverse().get(group);
        }
        if (childGroupRows.inverse().containsKey(group)) {
            return childGroupRows.inverse().get(group);
        }
        return null;
    }

    public GroupDataGridItems<T> getDataGridItems() {
        return dataGridItems;
    }

    public boolean isGroupRow(T item) {
        if (rootGroupRows.containsKey(item)) {
            return true;
        }
        return childGroupRows.containsKey(item);
    }

    @SuppressWarnings("unchecked")
    protected T createGroupRow(GroupInfo group) {
        T groupRowItem = (T) metadata.create(group.getProperty().getMetaClass());
        EntityValues.setValueEx(groupRowItem, group.getProperty(), group.getValue());
        return groupRowItem;
    }

    protected Stream<T> collectOwnChildren(@Nullable T parent) {
        if (parent == null) {
            return rootGroupRows.keySet().stream();
        }
        GroupInfo groupInfo = getGroupByItem(parent);
        if (groupInfo == null) {
            return Stream.empty();
        }

        List<GroupInfo> childGroups = dataGridItems.getChildren(groupInfo);
        if (childGroups.isEmpty()) {
            return getChildItems(groupInfo).stream();
        }

        List<T> childItems = new ArrayList<>();
        for (GroupInfo group : childGroups) {
            T item = getItemByGroup(group);
            if (item != null) {
                childItems.add(item);
            }
        }
        return childItems.stream();
    }
}
