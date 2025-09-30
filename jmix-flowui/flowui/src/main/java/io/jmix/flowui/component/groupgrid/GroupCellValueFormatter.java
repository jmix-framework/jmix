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

import java.io.Serializable;
import java.util.Collection;

/**
 * Interface for formatting group cell values. The formatting is applied only for group data grid columns.
 * <p>
 * If a group data grid column does not have a custom renderer, the formatter is used to format the cell value.
 * The formatted value will also be exported to Excel using export actions.
 * <p>
 * If a group data grid column has a custom group component renderer, the formatter is not used.
 * The formatter will only be applied during export to Excel using export actions.
 * <p>
 * For instance, setting formatter for group column cell value:
 * <pre>
 * &#064;Install(to = "customersDataGrid.group", subject = "groupCellValueFormatter")
 * public String groupColumnCellFormatter(GroupCellValueFormatter.GroupCellContext&lt;Customer&gt; context) {
 *     GroupInfo groupInfo = context.getGroupInfo();
 *     GroupProperty property = groupInfo.getProperty();
 *
 *     if (property.is("grade")) {
 *         return "Customer grade: " + groupInfo.getValue();
 *     } else {
 *         return "Country: " + metadataTools.getInstanceName(groupInfo.getValue());
 *     }
 * }
 * </pre>
 *
 * @param <E> item type
 */
@FunctionalInterface
public interface GroupCellValueFormatter<E> extends Serializable {

    /**
     * Formats the group cell value.
     *
     * @param context group cell context
     * @return formatted group cell value
     */
    String format(GroupCellContext<E> context);

    /**
     * Represents the context for a group cell in a grouped data grid.
     *
     * @param <E> item type
     */
    class GroupCellContext<E> {

        protected GroupInfo groupInfo;
        protected Collection<E> items;

        public GroupCellContext(GroupInfo groupInfo, Collection<E> items) {
            this.groupInfo = groupInfo;
            this.items = items;
        }

        /**
         * Retrieves the group info of the group cell.
         *
         * @return the group info
         */
        public GroupInfo getGroupInfo() {
            return groupInfo;
        }

        /**
         * Retrieves the items belonging to the group info of the group cell.
         *
         * @return the collection of items
         */
        public Collection<E> getItems() {
            return items;
        }
    }
}
