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

import io.jmix.core.common.util.Preconditions;
import io.jmix.flowui.component.ListDataComponent;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.grid.EnhancedDataGrid;

public abstract class AbstractGroupDataGridAdapter<E> extends DataGrid<E> implements ListDataComponent<E>,
        EnhancedDataGrid<E> {

    protected GroupGrid<E> groupGrid;

    public AbstractGroupDataGridAdapter(GroupGrid<E> groupGrid) {
        Preconditions.checkNotNullArgument(groupGrid);
        this.groupGrid = groupGrid;
    }

    public GroupGrid<E> getAdaptee() {
        return groupGrid;
    }
}
