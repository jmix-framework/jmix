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
package com.haulmont.cuba.gui.data;

import io.jmix.core.Entity;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.ui.component.data.GroupInfo;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

/**
 * CollectionDatasource which supports a grouping of items by the list of properties
 *
 * @param <T> type of entity
 * @param <K> type of entity ID
 *
 * @deprecated Use {@link io.jmix.ui.model.CollectionContainer} APIs instead.
 */
@Deprecated
public interface GroupDatasource<T extends Entity, K> extends CollectionDatasource<T, K> {
    /**
     * Perform grouping by the list of properties
     */
    void groupBy(Object[] properties);

    /**
     * @return the list of root groups
     */
    List<GroupInfo> rootGroups();

    /**
     * Indicates that group has nested groups
     */
    boolean hasChildren(GroupInfo groupId);

    /**
     * @return the list of nested groups
     */
    List<GroupInfo> getChildren(GroupInfo groupId);

    /**
     * @return the list of nested items
     */
    List<T> getOwnChildItems(GroupInfo groupId);

    /**
     * @return the list of items from all nested group levels
     */
    List<T> getChildItems(GroupInfo groupId);

    /**
     * @return the parent group of passed item
     */
    @Nullable
    GroupInfo getParentGroup(T entity);

    /**
     * @return the path through all parent groups
     */
    List<GroupInfo> getGroupPath(T entity);

    /**
     * @return a group property
     */
    Object getGroupProperty(GroupInfo groupId);

    /**
     * @return a group property value
     */
    Object getGroupPropertyValue(GroupInfo groupId);

    /**
     * @return item ids that are contained in the selected group
     */
    Collection<K> getGroupItemIds(GroupInfo groupId);

    /**
     * @return a count of items that are contained in the selected group
     */
    int getGroupItemsCount(GroupInfo groupId);

    /**
     * Indicated that a datasource has groups
     */
    boolean hasGroups();

    /**
     * @return group properties
     */
    Collection<?> getGroupProperties();

    /**
     * Indicates that a group is contained in the groups tree
     */
    boolean containsGroup(GroupInfo groupId);

    /**
     * Sorts groups and items in-memory after DB sorting
     */
    interface GroupSortDelegate {
        void sortGroups(List<GroupInfo> groups, Sortable.SortInfo<MetaPropertyPath>[] sortInfo);
    }

    /**
     * Set ability to override in-memory sorting of groups in a GroupCollectionDatasource
     */
    interface SupportsGroupSortDelegate {
        void setGroupSortDelegate(GroupSortDelegate sortDelegate);
    }
}
