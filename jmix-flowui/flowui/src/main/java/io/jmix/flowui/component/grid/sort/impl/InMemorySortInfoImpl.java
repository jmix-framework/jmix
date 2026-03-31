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

public class InMemorySortInfoImpl implements InMemorySortInfo {

    protected MetaPropertyPath metaPropertyPath;
    protected String property;
    protected Comparator<?> comparator;
    protected boolean ascending;

    public InMemorySortInfoImpl(@Nullable MetaPropertyPath metaPropertyPath,
                                String property,
                                @Nullable Comparator<?> comparator,
                                boolean ascending) {
        this.metaPropertyPath = metaPropertyPath;
        this.property = property;
        this.comparator = comparator;
        this.ascending = ascending;
    }

    @Override
    public @Nullable MetaPropertyPath getMetaPropertyPath() {
        return metaPropertyPath;
    }

    @Override
    public String getProperty() {
        return property;
    }

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
