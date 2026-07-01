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

package io.jmix.flowui.component.grid.sort.impl;

import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.flowui.component.grid.sort.InMemorySortInfo;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;

/**
 * Implementation of the {@link InMemorySortInfo} interface that encapsulates
 * sorting details for in-memory operations.
 * <p>
 * This class is used to define sorting behaviors that can be applied to data
 * when sorting is performed in memory.
 */
public class InMemorySortInfoImpl implements InMemorySortInfo {

    protected final String property;
    protected final boolean ascending;

    @Nullable
    protected final MetaPropertyPath metaPropertyPath;

    @Nullable
    protected Comparator<?> comparator;

    public InMemorySortInfoImpl(String property,
                                boolean ascending,
                                @Nullable MetaPropertyPath metaPropertyPath,
                                @Nullable Comparator<?> comparator) {
        this.property = property;
        this.ascending = ascending;
        this.metaPropertyPath = metaPropertyPath;
        this.comparator = comparator;
    }

    @Nullable
    @Override
    public MetaPropertyPath getMetaPropertyPath() {
        return metaPropertyPath;
    }

    @Override
    public String getSortKey() {
        return property;
    }

    @Nullable
    @Override
    public Comparator<?> getComparator() {
        return comparator;
    }

    @Override
    public void setComparator(@Nullable Comparator<?> comparator) {
        this.comparator = comparator;
    }

    @Override
    public boolean isAscending() {
        return ascending;
    }
}
