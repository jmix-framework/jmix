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
import io.jmix.flowui.component.grid.sort.PersistentSortInfo;
import org.jspecify.annotations.Nullable;

public class PersistentSortInfoImpl implements PersistentSortInfo {

    protected MetaPropertyPath metaPropertyPath;
    protected String property;
    protected String expression;
    protected boolean ascending;

    public PersistentSortInfoImpl(@Nullable MetaPropertyPath metaPropertyPath,
                                  String property,
                                  @Nullable String expression,
                                  boolean ascending) {
        this.metaPropertyPath = metaPropertyPath;
        this.property = property;
        this.expression = expression;
        this.ascending = ascending;
    }

    @Nullable
    @Override
    public MetaPropertyPath getMetaPropertyPath() {
        return metaPropertyPath;
    }

    @Override
    public String getProperty() {
        return property;
    }

    @Nullable
    @Override
    public String getExpression() {
        return expression;
    }


    @Override
    public void setExpression(@Nullable String expression) {
        this.expression = expression;
    }

    @Override
    public boolean isAscending() {
        return ascending;
    }
}
