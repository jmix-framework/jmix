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
package com.haulmont.cuba.gui.data.impl;

import io.jmix.core.Entity;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.GroupDatasource;
import io.jmix.ui.component.data.GroupInfo;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GroupDatasourceImpl<T extends Entity, K>
        extends CollectionDatasourceImpl<T, K>
        implements GroupDatasource<T, K>, GroupDatasource.SupportsGroupSortDelegate {

    protected GroupSortDelegate groupSortDelegate = (groups, sortInfo) -> {
        boolean asc = CollectionDatasource.Sortable.Order.ASC.equals(sortInfo[0].getOrder());
        groups.sort(Comparator.comparing(GroupInfo::getValue, EntityValuesComparator.asc(asc)));
    };

    protected GroupDelegate<T,K> groupDelegate = new GroupDelegate<T, K>(this,
            sortDelegate, groupSortDelegate) {
        @Override
        protected void doSort(SortInfo<MetaPropertyPath>[] sortInfo) {
            GroupDatasourceImpl.super.doSort();
        }
    };

    @Override
    public void groupBy(Object[] properties) {
        groupDelegate.groupBy(properties, sortInfos);
    }

    @Override
    public List<GroupInfo> rootGroups() {
        return groupDelegate.rootGroups();
    }

    @Override
    public boolean hasChildren(GroupInfo groupId) {
        return groupDelegate.hasChildren(groupId);
    }

    @Override
    public List<GroupInfo> getChildren(GroupInfo groupId) {
        return groupDelegate.getChildren(groupId);
    }

    @Override
    public List<T> getOwnChildItems(GroupInfo groupId) {
        return groupDelegate.getOwnChildItems(groupId);
    }

    @Override
    public List<T> getChildItems(GroupInfo groupId) {
        return groupDelegate.getChildItems(groupId);
    }

    @Override
    public GroupInfo getParentGroup(T entity) {
        return groupDelegate.getParentGroup(entity);
    }

    @Override
    public List<GroupInfo> getGroupPath(T entity) {
        return groupDelegate.getGroupPath(entity);
    }

    @Override
    public Object getGroupProperty(GroupInfo groupId) {
        return groupDelegate.getGroupProperty(groupId);
    }

    @Override
    public Object getGroupPropertyValue(GroupInfo groupId) {
        return groupDelegate.getGroupPropertyValue(groupId);
    }

    @Override
    public Collection<K> getGroupItemIds(GroupInfo groupId) {
        return groupDelegate.getGroupItemIds(groupId);
    }

    @Override
    public int getGroupItemsCount(GroupInfo groupId) {
        return groupDelegate.getGroupItemsCount(groupId);
    }

    @Override
    public boolean hasGroups() {
        return groupDelegate.hasGroups();
    }

    @Override
    public Collection<?> getGroupProperties() {
        return groupDelegate.getGroupProperties();
    }

    @Override
    public boolean containsGroup(GroupInfo groupId) {
        return groupDelegate.containsGroup(groupId);
    }

    @Override
    public K nextItemId(K itemId) {
        if (!groupDelegate.rootGroups().isEmpty()) {
            // Works in bounds of the current top-level group
            for (GroupInfo rootGroup : groupDelegate.rootGroups()) {
                List<K> groupItemIds = groupDelegate.getGroupItemIds(rootGroup);
                for (int i = 0; i < groupItemIds.size(); i++) {
                    if (groupItemIds.get(i).equals(itemId) && i < groupItemIds.size() - 1) {
                        return groupItemIds.get(i + 1);
                    }
                }
            }
            return null;
        } else
            return super.nextItemId(itemId);
    }

    @Override
    public K prevItemId(K itemId) {
        if (!groupDelegate.rootGroups().isEmpty()) {
            for (GroupInfo rootGroup : groupDelegate.rootGroups()) {
                // Works in bounds of the current top-level group
                List<K> groupItemIds = groupDelegate.getGroupItemIds(rootGroup);
                for (int i = 0; i < groupItemIds.size(); i++) {
                    if (groupItemIds.get(i).equals(itemId) && i > 0) {
                        return groupItemIds.get(i - 1);
                    }
                }
            }
            return null;
        } else
            return super.prevItemId(itemId);
    }

    @Override
    public K firstItemId() {
        List<GroupInfo> rootGroups = groupDelegate.rootGroups();
        if (!rootGroups.isEmpty()) {
            List<K> groupItemIds = groupDelegate.getGroupItemIds(rootGroups.get(0));
            if (!groupItemIds.isEmpty())
                return groupItemIds.get(0);
            else
                return null;
        } else {
            return super.firstItemId();
        }
    }

    @Override
    public K lastItemId() {
        List<GroupInfo> rootGroups = groupDelegate.rootGroups();
        if (!rootGroups.isEmpty()) {
            List<K> groupItemIds = groupDelegate.getGroupItemIds(rootGroups.get(rootGroups.size() - 1));
            if (!groupItemIds.isEmpty())
                return groupItemIds.get(groupItemIds.size() - 1);
            else
                return null;
        } else {
            return super.lastItemId();
        }
    }

    @Override
    public void setSuspended(boolean suspended) {
        boolean wasSuspended = this.suspended;
        this.suspended = suspended;

        if (wasSuspended && !suspended && refreshOnResumeRequired) {
            refresh();

            if (!getGroupProperties().isEmpty()) {
                groupBy(getGroupProperties().toArray());

                fireCollectionChanged(Operation.REFRESH, Collections.emptyList());
            }
        }
    }

    @Override
    public void setSortDelegate(SortDelegate<T, K> sortDelegate) {
        super.setSortDelegate(sortDelegate);
        groupDelegate.setSortDelegate(sortDelegate);
    }

    @Override
    public void setGroupSortDelegate(GroupSortDelegate sortDelegate) {
        this.groupSortDelegate = sortDelegate;
        groupDelegate.setGroupSortDelegate(sortDelegate);
    }
}
