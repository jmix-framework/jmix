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

package io.jmix.flowui.component.groupgrid;

import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.component.ListDataComponent;

import java.util.Collection;
import java.util.EventObject;
import java.util.List;
import java.util.function.Consumer;

public interface GroupListDataComponent<E> extends ListDataComponent<E> {

    /**
     * Groups data by the specified column keys.
     * <p>
     * The previous grouping will be replaced by a new one.
     *
     * @param keys the column keys to group by
     */
    void groupByKeys(String... keys);

    /**
     * Groups data by the specified column keys.
     * <p>
     * The previous grouping will be replaced by a new one.
     *
     * @param keys the column keys to group by
     */
    void groupByKeysList(List<String> keys);

    /**
     * Resets any grouping applied in the grid.
     */
    void ungroup();

    /**
     * Ungroups the provided array of column keys.
     * <p>
     * Grouping columns that are not in the provided keys array will still be applied.
     *
     * @param keys the array of column keys used to ungroup
     */
    void ungroupByKeys(String... keys);

    /**
     * Ungroups the provided list of column keys.
     * <p>
     * Grouping columns that are not in the provided keys list will still be applied.
     *
     * @param keys a list of column keys used to ungroup;
     */
    void ungroupByKeysList(List<String> keys);

    /**
     * Expands the specified group.
     *
     * @param group the group to be expanded
     */
    void expand(GroupInfo group);

    /**
     * Expands all groups that are on the path to the provided item.
     *
     * @param item item to expand all groups
     */
    void expandByPath(E item);

    /**
     * Expands all root groups and their children recursively.
     */
    void expandAll();

    /**
     * Collapses the specified group.
     *
     * @param group the group to be collapsed
     */
    void collapse(GroupInfo group);

    /**
     * Collapses all groups that are on the path to the provided item.
     *
     * @param item item to collapse all groups
     */
    void collapseByPath(E item);

    /**
     * Collapses all root groups and their children recursively.
     */
    void collapseAll();

    /**
     * Determines if the specified group is expanded.
     *
     * @param group the group to check
     * @return {@code true} if the group is expanded, false otherwise
     */
    boolean isExpanded(GroupInfo group);

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
     * Indicates that items have groups
     */
    boolean hasGroups();

    /**
     * Adds a listener to be notified when a group is collapsed.
     *
     * @param listener the listener to add
     * @return a registration object for removing the listener
     */
    Registration addCollapseListener(Consumer<CollapseEvent<E>> listener);

    /**
     * Adds a listener to be notified when a group is expanded.
     *
     * @param listener the listener to add
     * @return a registration object for removing the listener
     */
    Registration addExpandListener(Consumer<ExpandEvent<E>> listener);

    /**
     * Event fired when a group collapsed.
     *
     * @param <E> item type
     */
    class CollapseEvent<E> extends EventObject {

        protected boolean isFromClient;
        protected Collection<E> collapsedItems;

        public CollapseEvent(GroupListDataComponent<E> source, boolean isFromClient, Collection<E> collapsedItems) {
            super(source);

            this.isFromClient = isFromClient;
            this.collapsedItems = collapsedItems;
        }

        @Override
        @SuppressWarnings("unchecked")
        public GroupListDataComponent<E> getSource() {
            return (GroupListDataComponent<E>) super.getSource();
        }

        /**
         * Checks if this event originated from the client side.
         *
         * @return {@code true} if this event originated from the client side
         */
        public boolean isFromClient() {
            return isFromClient;
        }

        /**
         * @return a collection of collapsed group items
         */
        public Collection<E> getCollapsedItems() {
            return collapsedItems;
        }
    }

    /**
     * Event fired when a group expanded.
     *
     * @param <E> item type
     */
    class ExpandEvent<E> extends EventObject {

        protected boolean isFromClient;
        protected Collection<E> expandedItems;

        public ExpandEvent(GroupListDataComponent<E> source, boolean isFromClient, Collection<E> expandedItems) {
            super(source);

            this.isFromClient = isFromClient;
            this.expandedItems = expandedItems;
        }

        @Override
        @SuppressWarnings("unchecked")
        public GroupListDataComponent<E> getSource() {
            return (GroupListDataComponent<E>) super.getSource();
        }

        /**
         * Checks if this event originated from the client side.
         *
         * @return {@code true} if this event originated from the client side
         */
        public boolean isFromClient() {
            return isFromClient;
        }

        /**
         * @return a collection of expanded group items
         */
        public Collection<E> getExpandedItems() {
            return expandedItems;
        }
    }
}
