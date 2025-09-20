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

package io.jmix.reportsflowui.helper;

import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import io.jmix.core.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component("report_GridSortHelper")
public class GridSortHelper {

    /**
     * Convert DataGrid sort settings to a {@link Sort} object.
     * Optionally, replace some custom column keys to persistent property names.
     *
     * @param gridSortOrders      DataGrid sort settings
     * @param sortKeyReplacements sort key replacement map (DataGrid column id -> Sort order property)
     * @return Sort object suitable to pass to DataManager or similar APIs
     */
    public <T> Sort convertSortOrders(List<GridSortOrder<T>> gridSortOrders, Map<String, String> sortKeyReplacements) {
        if (gridSortOrders.isEmpty()) {
            return Sort.UNSORTED;
        }

        List<Sort.Order> orders = gridSortOrders.stream()
                .map(sortOrder -> {
                    String sortKey = sortOrder.getSorted().getKey();
                    if (sortKeyReplacements.containsKey(sortKey)) {
                        sortKey = sortKeyReplacements.get(sortKey);
                    }
                    return SortDirection.ASCENDING == sortOrder.getDirection()
                            ? Sort.Order.asc(sortKey)
                            : Sort.Order.desc(sortKey);
                })
                .toList();
        return Sort.by(orders);
    }
}
