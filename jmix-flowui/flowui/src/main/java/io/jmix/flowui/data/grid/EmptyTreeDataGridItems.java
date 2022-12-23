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

package io.jmix.flowui.data.grid;

import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import io.jmix.core.metamodel.model.MetaClass;

import java.util.stream.Stream;

public class EmptyTreeDataGridItems<T> extends EmptyDataGridItems<T> implements  TreeDataGridItems<T>,
        HierarchicalDataProvider<T, Void> {

    public EmptyTreeDataGridItems(MetaClass metaClass) {
        super(metaClass);
    }

    @Override
    public int getChildCount(HierarchicalQuery<T, Void> query) {
        return 0;
    }

    @Override
    public Stream<T> fetchChildren(HierarchicalQuery<T, Void> query) {
        return Stream.empty();
    }

    @Override
    public boolean hasChildren(T item) {
        return false;
    }
}
