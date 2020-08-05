/*
 * Copyright 2020 Haulmont.
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

package io.jmix.ui.component;

import javax.annotation.Nullable;

/**
 * Pagination component for using inside of {@link Table} or {@link DataGrid}.
 */
public interface TablePagination extends Pagination {
    String NAME = "tablePagination";

    /**
     * @return target component e.g. {@link DataGrid} or {@link Table}
     */
    @Nullable
    ListComponent getTablePaginationTarget();

    /**
     * Sets target component which data should be shown by pages.
     *
     * @param target {@link DataGrid} or {@link Table} based component
     */
    void setTablePaginationTarget(ListComponent target);
}
