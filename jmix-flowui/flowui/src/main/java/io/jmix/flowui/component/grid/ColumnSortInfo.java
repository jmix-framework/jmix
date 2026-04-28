/*
 * Copyright 2026 Haulmont.
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

package io.jmix.flowui.component.grid;

import io.jmix.core.annotation.Experimental;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import org.jspecify.annotations.Nullable;

/**
 * Represents sorting information for a specific column.
 *
 * @param <E> entity type
 */
@Experimental
public interface ColumnSortInfo<E> {

    /**
     * Retrieves the column associated with this sorting information.
     *
     * @return the {@link DataGridColumn} instance
     */
    DataGridColumn<E> getColumn();

    /**
     * Retrieves the {@link MetaPropertyPath} associated with this sort info, or {@code null} if the column
     * is not bound to a meta property path.
     *
     * @return the meta property path associated with this sort info
     */
    @Nullable
    MetaPropertyPath getMetaPropertyPath();

    /**
     * Indicates whether the sorting order for the column is ascending.
     *
     * @return {@code true} if the column is sorted in ascending order, {@code false} otherwise
     */
    boolean isAscending();
}
