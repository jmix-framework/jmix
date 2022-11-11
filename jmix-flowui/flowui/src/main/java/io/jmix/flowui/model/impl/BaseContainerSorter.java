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

package io.jmix.flowui.model.impl;

import io.jmix.core.Sort;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.Sorter;
import org.springframework.beans.factory.BeanFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Base implementation of sorting collection containers.
 */
public abstract class BaseContainerSorter implements Sorter {

    protected BeanFactory beanFactory;

    private final CollectionContainer<?> container;

    public BaseContainerSorter(CollectionContainer<?> container, BeanFactory beanFactory) {
        this.container = container;
        this.beanFactory = beanFactory;
    }

    public CollectionContainer<?> getContainer() {
        return container;
    }

    @Override
    public void sort(Sort sort) {
        sortInMemory(sort);
    }

    @SuppressWarnings("unchecked")
    protected void sortInMemory(Sort sort) {
        if (sort.getOrders().isEmpty() || container.getItems().isEmpty()) {
            return;
        }
        List list = new ArrayList<>(container.getItems());
        list.sort(createComparator(sort, container.getEntityMetaClass()));
        setItemsToContainer(list);
    }

    protected abstract void setItemsToContainer(List<?> list);

    protected Comparator<?> createComparator(Sort sort, MetaClass metaClass) {
        if (sort.getOrders().size() > 1) {
            throw new UnsupportedOperationException("Sort by multiple properties is not supported");
        }
        MetaPropertyPath propertyPath = metaClass.getPropertyPath(sort.getOrders().get(0).getProperty());
        if (propertyPath == null) {
            throw new IllegalArgumentException("Property " + sort.getOrders().get(0).getProperty() + " is invalid");
        }
        boolean asc = sort.getOrders().get(0).getDirection() == Sort.Direction.ASC;
        EntityValuesComparator<Object> comparator = new EntityValuesComparator<>(asc, metaClass, beanFactory);
        return Comparator.comparing(e -> EntityValues.getValueEx(e, propertyPath), comparator);
    }
}
