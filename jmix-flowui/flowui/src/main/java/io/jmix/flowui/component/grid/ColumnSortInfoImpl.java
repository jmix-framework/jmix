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
import io.jmix.core.annotation.Internal;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import org.jspecify.annotations.Nullable;

@Experimental
@Internal
public class ColumnSortInfoImpl<T> implements ColumnSortInfo<T> {

    protected final DataGridColumn<T> column;

    @Nullable
    protected final MetaPropertyPath metaPropertyPath;

    protected final boolean ascending;

    public ColumnSortInfoImpl(DataGridColumn<T> column, boolean ascending, @Nullable MetaPropertyPath metaPropertyPath) {
        Preconditions.checkNotNullArgument(column);

        this.column = column;
        this.ascending = ascending;
        this.metaPropertyPath = metaPropertyPath;
    }

    @Nullable
    @Override
    public MetaPropertyPath getMetaPropertyPath() {
        return metaPropertyPath;
    }

    @Override
    public DataGridColumn<T> getColumn() {
        return column;
    }

    @Override
    public boolean isAscending() {
        return ascending;
    }
}
