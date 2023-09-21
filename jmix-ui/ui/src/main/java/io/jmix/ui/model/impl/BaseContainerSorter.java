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

package io.jmix.ui.model.impl;

import io.jmix.core.Sort;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.Sorter;
import org.springframework.beans.factory.BeanFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Base implementation of sorting collection containers.
 */
public abstract class BaseContainerSorter implements Sorter {

    protected BeanFactory beanFactory;

    private final CollectionContainer container;

    public BaseContainerSorter(CollectionContainer container, BeanFactory beanFactory) {
        this.container = container;
        this.beanFactory = beanFactory;
    }

    public CollectionContainer getContainer() {
        return container;
    }

    @Override
    public void sort(Sort sort) {
        sortInMemory(sort);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void sortInMemory(Sort sort) {
        List<Sort.Order> orders = sort.getOrders();

        if (orders.isEmpty() || container.getItems().isEmpty()) {
            return;
        }

        List containerItems = new ArrayList<>(container.getItems());
        MetaClass metaClass = container.getEntityMetaClass();

        Comparator comparator = createComparator(orders.get(0), metaClass);
        for (int i = 1; i < orders.size(); i++) {
            comparator = comparator.thenComparing(createComparator(orders.get(i), metaClass));
        }

        containerItems.sort(comparator);
        setItemsToContainer(containerItems);
    }

    protected abstract void setItemsToContainer(List list);

    protected Comparator<?> createComparator(Sort.Order sortOrder, MetaClass metaClass) {
        MetaPropertyPath propertyPath = metaClass.getPropertyPath(sortOrder.getProperty());
        if (propertyPath == null) {
            throw new IllegalArgumentException("Property " + sortOrder.getProperty() + " is invalid");
        }

        boolean asc = sortOrder.getDirection() == Sort.Direction.ASC;
        EntityValuesComparator<Object> comparator = new EntityValuesComparator<>(asc, metaClass, beanFactory);
        return Comparator.comparing(e -> EntityValues.getValueEx(e, propertyPath), comparator);
    }
}
