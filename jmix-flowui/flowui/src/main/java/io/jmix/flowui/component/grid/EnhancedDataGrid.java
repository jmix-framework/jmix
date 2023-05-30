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

package io.jmix.flowui.component.grid;

import com.vaadin.flow.component.grid.Grid;
import io.jmix.core.metamodel.model.MetaPropertyPath;

import org.springframework.lang.Nullable;
import java.util.List;

public interface EnhancedDataGrid<T> {

    @Nullable
    MetaPropertyPath getColumnMetaPropertyPath(Grid.Column<T> column);

    Grid.Column<T> addColumn(MetaPropertyPath metaPropertyPath);

    Grid.Column<T> addColumn(String key, MetaPropertyPath metaPropertyPath);

    boolean isEditorCreated();

    /**
     * @return a copy of columns that are visible and not hidden by security
     * @deprecated use {@link Grid#getColumns()} and filter returned list by visibility property
     */
    @Deprecated
    List<Grid.Column<T>> getVisibleColumns();
}
