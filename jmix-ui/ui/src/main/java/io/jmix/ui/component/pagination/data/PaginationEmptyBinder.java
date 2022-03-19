/*
 * Copyright 2020 Haulmont.
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

package io.jmix.ui.component.pagination.data;

import io.jmix.core.common.util.Preconditions;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.ui.model.CollectionChangeType;

import java.util.function.Consumer;

public class PaginationEmptyBinder implements PaginationDataBinder {

    protected MetaClass metaClass;

    public PaginationEmptyBinder(MetaClass metaClass) {
        Preconditions.checkNotNullArgument(metaClass);

        this.metaClass = metaClass;
    }

    @Override
    public void removeCollectionChangeListener() {
    }

    @Override
    public int getFirstResult() {
        return 0;
    }

    @Override
    public int getMaxResults() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void setFirstResult(int startPosition) {
    }

    @Override
    public void setMaxResults(int maxResults) {
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public MetaClass getEntityMetaClass() {
        return metaClass;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public void refresh() {
    }

    @Override
    public void setCollectionChangeListener(Consumer<CollectionChangeType> listener) {
    }
}
