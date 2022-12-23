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

public interface EnhancedTreeDataGrid<T> extends EnhancedDataGrid<T> {

    /**
     * Adds hierarchy column by the meta property path.
     * see {@link TreeDataGrid#getColumnByKey(String)}.
     *
     * @param metaPropertyPath meta property path to add column
     * @return added column
     */
    Grid.Column<T> addHierarchyColumn(MetaPropertyPath metaPropertyPath);

    /**
     * Adds hierarchy column by the meta property path and specified key. The key is used to identify the column,
     * see {@link TreeDataGrid#getColumnByKey(String)}.
     *
     * @param key              column key
     * @param metaPropertyPath meta property path to add column
     * @return added column
     */
    Grid.Column<T> addHierarchyColumn(String key, MetaPropertyPath metaPropertyPath);
}
