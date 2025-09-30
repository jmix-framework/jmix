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

import io.jmix.flowui.component.groupgrid.GroupProperty;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Describes a property that can be used to group items.
 * <p>
 * For instance, the group data grid contains a column not bound with a property of the entity. To enable grouping
 * by this column, a custom {@link GroupPropertyDescriptor} must be provided. If the column defines the "fullName" key,
 * the following code can be used:
 * <pre>
 * customersDataGrid.getItems().addGroupPropertyDescriptor(
 *         new BaseGroupPropertyDescriptor&lt;Customer&gt;("fullName", context -> {
 *             Customer item = context.getItem();
 *             return item.getFirstName() + " " + item.getLastName();
 *         }).withSortProperties(List.of("firstName", "lastName")));
 * </pre>
 * Note, the {@code BaseGroupPropertyDescriptor} is available in the group data grid addon.
 *
 * @param <E> item type
 */
public interface GroupPropertyDescriptor<E> {

    /**
     * @return the property to be used for grouping
     */
    GroupProperty getProperty();

    /**
     * @return a function that provides the value of the custom property for the given item
     */
    Function<GroupValueContext<E>, Object> getValueProvider();

    /**
     * @return a list of properties that should be used for sorting the group property
     */
    default List<String> getSortProperties() {
        return Collections.emptyList();
    }

    /**
     * The context provides information about an item and its associated grouping property.
     *
     * @param <E> the item type
     */
    class GroupValueContext<E> {

        protected final E item;

        protected final GroupProperty property;

        public GroupValueContext(E item, GroupProperty property) {
            this.item = item;
            this.property = property;
        }

        /**
         * @return the item
         */
        public E getItem() {
            return item;
        }

        /**
         * @return group property
         */
        public GroupProperty getProperty() {
            return property;
        }
    }
}
