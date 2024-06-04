/*
 * Copyright 2022 Haulmont.
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

package io.jmix.gridexportui.exporter;

import io.jmix.core.Sort;
import io.jmix.ui.component.DataGrid;
import io.jmix.ui.component.Table;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Helper class for extracting sorting information from Jmix UI components.
 * Provides methods to convert sorting information from {@link Table.SortInfo} and
 * {@link DataGrid.SortOrder} to {@link Sort}.
 */
public class ExporterSortHelper {

    /**
     * Converts the sorting information from a {@link Table.SortInfo} object to a {@link Sort} object.
     *
     * @param sortInfo The sorting information from a Table component. Can be {@code null}.
     * @return A {@link Sort} object representing the sorting information, or {@code null} if {@code sortInfo} is {@code null}.
     */
    @Nullable
    public static Sort getSortOrder(@Nullable Table.SortInfo sortInfo) {
        if (sortInfo == null) {
            return null;
        }

        String sortInfoPropertyId = sortInfo.getPropertyId().toString();
        return Sort.by(sortInfo.getAscending() ? Sort.Order.asc(sortInfoPropertyId) : Sort.Order.desc(sortInfoPropertyId));
    }

    /**
     * Converts the sorting information from a list of {@link DataGrid.SortOrder} objects to a {@link Sort} object.
     *
     * @param sortOrders The list of sorting information from a DataGrid component. Can be {@code null}.
     * @return A {@link Sort} object representing the sorting information, or {@code null} if {@code sortOrders} is {@code null}.
     */
    @Nullable
    public static Sort getSortOrder(@Nullable List<DataGrid.SortOrder> sortOrders) {
        if (sortOrders == null) {
            return null;
        }

        return Sort.by(sortOrders.stream().map(sortOrder -> {
            String sortOrderColumnId = sortOrder.getColumnId();
            return DataGrid.SortDirection.ASCENDING.equals(sortOrder.getDirection())
                    ? Sort.Order.asc(sortOrderColumnId) : Sort.Order.desc(sortOrderColumnId);
        }).collect(Collectors.toList()));
    }
}
