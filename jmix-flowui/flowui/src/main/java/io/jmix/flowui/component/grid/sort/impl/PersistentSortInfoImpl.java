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

import io.jmix.core.common.util.Preconditions;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.flowui.component.grid.sort.PersistentSortInfo;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of the {@link PersistentSortInfo} interface that represents
 * detailed information for persistent sorting operations.
 * <p>
 * This class is used to define sorting behaviors that can be applied to data
 * when interacting with persistent storage.
 */
public class PersistentSortInfoImpl implements PersistentSortInfo {

    protected final String property;
    protected final boolean ascending;

    @Nullable
    protected final MetaPropertyPath metaPropertyPath;

    protected List<String> expressions;

    public PersistentSortInfoImpl(String property,
                                  boolean ascending,
                                  @Nullable MetaPropertyPath metaPropertyPath,
                                  List<String> expressions) {
        Preconditions.checkNotNullArgument(property);
        Preconditions.checkNotNullArgument(expressions);

        this.property = property;
        this.ascending = ascending;
        this.metaPropertyPath = metaPropertyPath;
        this.expressions = new ArrayList<>(expressions);
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

    @Override
    public List<String> getExpressions() {
        return Collections.unmodifiableList(expressions);
    }

    @Override
    public void setExpressions(List<String> expressions) {
        Preconditions.checkNotNullArgument(expressions);
        this.expressions = new ArrayList<>(expressions);
    }

    @Override
    public boolean isAscending() {
        return ascending;
    }
}
