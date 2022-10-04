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

package io.jmix.flowui.component.grid;

import io.jmix.core.Sort;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.flowui.data.grid.GridDataItems;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.HasLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class JmixGridDataProvider<T> extends AbstractContainerGridDataProvider<T> implements GridDataItems.Sortable<T> {

    private static final Logger log = LoggerFactory.getLogger(JmixGridDataProvider.class);

    protected boolean suppressSorting;

    public JmixGridDataProvider(CollectionContainer<T> container) {
        super(container);
    }

    @Override
    public void sort(Object[] propertyId, boolean[] ascending) {
        if (container.getSorter() != null) {
            if (suppressSorting && container instanceof HasLoader && ((HasLoader) container).getLoader() instanceof CollectionLoader) {
                ((CollectionLoader) ((HasLoader) container).getLoader()).setSort(createSort(propertyId, ascending));
            } else {
                container.getSorter().sort(createSort(propertyId, ascending));
            }
        } else {
            log.debug("Container {} sorter is null", container);
        }
    }

    protected Sort createSort(Object[] propertyId, boolean[] ascending) {
        List<Sort.Order> orders = new ArrayList<>();
        for (int i = 0; i < propertyId.length; i++) {
            String property;
            if (propertyId[i] instanceof MetaPropertyPath) {
                property = ((MetaPropertyPath) propertyId[i]).toPathString();
            } else {
                property = (String) propertyId[i];
            }
            Sort.Order order = ascending[i] ? Sort.Order.asc(property) : Sort.Order.desc(property);
            orders.add(order);
        }
        return Sort.by(orders);
    }

    @Override
    public void resetSortOrder() {
        if (container.getSorter() != null) {
            if (suppressSorting && container instanceof HasLoader && ((HasLoader) container).getLoader() instanceof CollectionLoader) {
                ((CollectionLoader) ((HasLoader) container).getLoader()).setSort(Sort.UNSORTED);
            } else {
                container.getSorter().sort(Sort.UNSORTED);
            }
        } else {
            log.debug("Container {} sorter is null", container);
        }
    }

    @Override
    public void suppressSorting() {
        suppressSorting = true;
    }

    @Override
    public void enableSorting() {
        suppressSorting = false;
    }
}
