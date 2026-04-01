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

public class PersistentSortInfoImpl implements PersistentSortInfo {

    protected final @Nullable MetaPropertyPath metaPropertyPath;
    protected final String property;
    protected final boolean ascending;

    protected List<String> expressions;

    public PersistentSortInfoImpl(@Nullable MetaPropertyPath metaPropertyPath,
                                  String property,
                                  List<String> expressions,
                                  boolean ascending) {
        Preconditions.checkNotNullArgument(property);
        Preconditions.checkNotNullArgument(expressions);

        this.metaPropertyPath = metaPropertyPath;
        this.property = property;
        this.expressions = new ArrayList<>(expressions);
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
