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
import io.jmix.core.entity.EntityValues;
import io.jmix.flowui.data.BindingState;
import io.jmix.flowui.model.CollectionContainer;

import javax.annotation.Nullable;
import java.util.stream.Stream;

public class ContainerTreeDataGridItems<E> extends ContainerDataGridItems<E>
        implements TreeDataGridItems<E>, HierarchicalDataProvider<E, Void> {

    protected final String hierarchyProperty;
    protected final boolean showOrphans;

    public ContainerTreeDataGridItems(CollectionContainer<E> container,
                                      String hierarchyProperty) {
        this(container, hierarchyProperty, true);
    }

    public ContainerTreeDataGridItems(CollectionContainer<E> container,
                                      String hierarchyProperty,
                                      boolean showOrphans) {
        super(container);

        this.hierarchyProperty = hierarchyProperty;
        this.showOrphans = showOrphans;
    }

    @Override
    public int getChildCount(HierarchicalQuery<E, Void> query) {
        if (getState() == BindingState.INACTIVE) {
            return 0;
        }

        return Math.toIntExact(fetchChildren(query).count());

    }

    @Override
    public Stream<E> fetchChildren(HierarchicalQuery<E, Void> query) {
        if (getState() == BindingState.INACTIVE) {
            return Stream.empty();
        }

        return getChildren(query.getParent())
                .skip(query.getOffset())
                .limit(query.getLimit());
    }

    protected Stream<E> getChildren(@Nullable E item) {
        if (item == null) {
            // root items
            return container.getItems().stream()
                    .filter(it -> {
                        E parentItem = EntityValues.getValue(it, hierarchyProperty);
                        return parentItem == null
                                || (showOrphans && container.getItemOrNull(EntityValues.getId(parentItem)) == null);
                    });
        } else {
            return container.getItems().stream()
                    .filter(it -> {
                        E parentItem = EntityValues.getValue(it, hierarchyProperty);
                        return parentItem != null && parentItem.equals(item);
                    });
        }
    }

    @Override
    public boolean hasChildren(E item) {
        return container.getItems().stream()
                .anyMatch(it -> {
                    E parentItem = EntityValues.getValue(it, hierarchyProperty);
                    return parentItem != null && parentItem.equals(item);
                });
    }
}
