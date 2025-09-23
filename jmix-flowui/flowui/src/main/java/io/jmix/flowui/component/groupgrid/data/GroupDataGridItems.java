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

package io.jmix.flowui.component.groupgrid.data;

import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.component.groupgrid.GroupInfo;
import io.jmix.flowui.component.groupgrid.GroupProperty;
import io.jmix.flowui.data.grid.DataGridItems;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.EventObject;
import java.util.List;
import java.util.function.Consumer;

/**
 * Represents a group data grid source of items. Provides methods for grouping data by properties,
 * getting group infos.
 *
 * @param <T> the type of items contained within the group data grid
 */
public interface GroupDataGridItems<T> extends DataGridItems<T> {

    /**
     * Perform grouping by the list of properties. The available values for properties are:
     * <ul>
     *     <li>
     *         MetaPropertyPath can be a property of current or a reference entity.
     *     </li>
     *     <li>
     *         Column key (String) of a generated value.
     *     </li>
     * </ul>
     */
    void groupBy(List<GroupProperty> properties);

    /**
     * @return the list of root groups
     */
    List<GroupInfo> getRootGroups();

    /**
     * @return the list of nested groups
     */
    List<GroupInfo> getChildren(GroupInfo group);

    /**
     * Indicates that the group has nested groups
     *
     * @param groupInfo group info
     */
    boolean hasChildren(GroupInfo groupInfo);

    /**
     * @return the list of nested items
     */
    List<T> getOwnChildItems(GroupInfo group);

    /**
     * @return the list of items from all nested group levels
     */
    List<T> getChildItems(GroupInfo group);

    /**
     * @return a group that the passed item belongs to
     */
    @Nullable
    GroupInfo getGroup(T item);

    /**
     * @param item item (leaf) to get all parent groups for
     * @return the path through all parent groups
     */
    List<GroupInfo> getGroupPath(T item);

    /**
     * @return items that are contained in the selected group
     */
    Collection<T> getGroupItems(GroupInfo group);

    /**
     * @return a count of items that are contained in the selected group
     */
    int getGroupItemsCount(GroupInfo group);

    /**
     * Indicates that items have groups
     */
    boolean hasGroups();

    /**
     * @return group properties
     */
    Collection<GroupProperty> getGroupProperties();

    /**
     * Indicates that a group is contained in the group tree
     */
    boolean containsGroup(GroupInfo group);

    /**
     * Adds a group property descriptor for a custom grouping property. This method enables defining
     * custom logic for computing property values during data grouping.
     * <p>
     * For instance:
     * <pre>
     * customersDataGrid.getItems().addGroupPropertyDescriptor(
     *         new BaseGroupPropertyDescriptor&lt;Customer&gt;("fullName", context -> {
     *             Customer item = context.getItem();
     *             return item.getFirstName() + " " + item.getLastName();
     *         }).withSortProperties(List.of("firstName", "lastName")));
     * </pre>
     *
     * @param groupPropertyDescriptor implementation of {@link GroupPropertyDescriptor}
     */
    void addGroupPropertyDescriptor(GroupPropertyDescriptor<T> groupPropertyDescriptor);

    /**
     * Removes a group property descriptor for a custom property that was previously added for grouping.
     *
     * @param property the custom property
     */
    void removeGroupPropertyDescriptor(GroupProperty property);

    /**
     * @param property the custom property
     * @return a group property descriptor for a custom property
     */
    @Nullable
    GroupPropertyDescriptor<T> getGroupPropertyDescriptor(GroupProperty property);

    /**
     * Removes all group property descriptors previously added for grouping.
     */
    void removeAllGroupPropertyProviders();

    /**
     * Adds a listener to be notified when the group property descriptors are changed.
     *
     * @param listener the listener to add
     * @return a registration object for removing the listener
     */
    Registration addGroupPropertyDescriptorsChangedListener(Consumer<GroupPropertyDescriptorsChangedEvent<T>> listener);

    /**
     * Event is fired when the group property descriptors count is changed (removed/ added).
     *
     * @param <T> item type
     */
    class GroupPropertyDescriptorsChangedEvent<T> extends EventObject {

        protected final Collection<GroupPropertyDescriptor<T>> groupPropertyDescriptors;

        public GroupPropertyDescriptorsChangedEvent(Object source,
                                                    Collection<GroupPropertyDescriptor<T>> groupPropertyDescriptors) {
            super(source);

            this.groupPropertyDescriptors = groupPropertyDescriptors;
        }

        /**
         * @return the collection of group property descriptors currently used in the group data grid items
         */
        public Collection<GroupPropertyDescriptor<T>> getGroupPropertyDescriptors() {
            return groupPropertyDescriptors;
        }
    }
}
