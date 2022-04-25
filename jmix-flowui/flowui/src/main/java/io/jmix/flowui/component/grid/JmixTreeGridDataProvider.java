/*
 * Copyright 2019 Haulmont.
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

import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.entity.EntityValues;
import io.jmix.flowui.data.BindingState;
import io.jmix.flowui.data.grid.EntityTreeGridDataItems;
import io.jmix.flowui.model.CollectionContainer;

import javax.annotation.Nullable;
import java.util.stream.Stream;

public class JmixTreeGridDataProvider<T> extends JmixGridDataProvider<T>
        implements HierarchicalDataProvider<T, Void>, EntityTreeGridDataItems<T> {

    protected String hierarchyProperty;
    private final boolean showOrphans;

    public JmixTreeGridDataProvider(CollectionContainer<T> container, String hierarchyProperty) {
        this(container, hierarchyProperty, true);
    }

    public JmixTreeGridDataProvider(CollectionContainer<T> container, String hierarchyProperty, boolean showOrphans) {
        super(container);

        this.hierarchyProperty = hierarchyProperty;
        this.showOrphans = showOrphans;
    }

    @Override
    public int getChildCount(HierarchicalQuery<T, Void> query) {
        if (getState() == BindingState.INACTIVE) {
            return 0;
        }

        return getChildCount(query.getParent());
    }

    @Override
    public Stream<T> fetchChildren(HierarchicalQuery<T, Void> query) {
        if (getState() == BindingState.INACTIVE) {
            return Stream.empty();
        }

        return getChildren(query.getParent())
                .skip(query.getOffset())
                .limit(query.getLimit());
    }

    @Override
    public boolean hasChildren(T item) {
        return container.getItems().stream().anyMatch(it -> {
            T parentItem = EntityValues.getValue(it, hierarchyProperty);
            return parentItem != null && parentItem.equals(item);
        });
    }

    @Override
    public int getLevel(T item) {
        if (!container.containsItem(item)) {
            throw new IllegalArgumentException("Data provider doesn't contain the item passed to the method");
        }

        int level = 0;
        T currentItem = item;
        while ((currentItem = getParent(currentItem)) != null) {
            ++level;
        }

        return level;
    }

    @Override
    public int getChildCount(T parent) {
        return Math.toIntExact(getChildren(parent).count());
    }

    @Override
    public Stream<T> getChildren(@Nullable T item) {
        if (item == null) {
            // root items
            return container.getItems().stream()
                    .filter(it -> {
                        T parentItem = EntityValues.getValue(it, hierarchyProperty);
                        return parentItem == null || (showOrphans && container.getItemOrNull(EntityValues.getId(parentItem)) == null);
                    });
        } else {
            return container.getItems().stream()
                    .filter(it -> {
                        T parentItem = EntityValues.getValue(it, hierarchyProperty);
                        return parentItem != null && parentItem.equals(item);
                    });
        }
    }

    @Nullable
    @Override
    public T getParent(T item) {
        Preconditions.checkNotNullArgument(item);
        return EntityValues.getValue(item, hierarchyProperty);
    }

    @Override
    public String getHierarchyPropertyName() {
        return hierarchyProperty;
    }
}
