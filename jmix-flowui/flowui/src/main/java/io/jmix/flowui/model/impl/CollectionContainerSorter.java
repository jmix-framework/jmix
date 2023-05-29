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
import io.jmix.flowui.model.BaseCollectionLoader;
import io.jmix.flowui.model.CollectionContainer;
import org.springframework.beans.factory.BeanFactory;

import jakarta.annotation.Nullable;
import java.util.List;

/**
 * Standard implementation of sorting {@link CollectionContainer}s.
 */
public class CollectionContainerSorter extends BaseContainerSorter {

    private final BaseCollectionLoader loader;

    public CollectionContainerSorter(CollectionContainer<?> container,
                                     @Nullable BaseCollectionLoader loader,
                                     BeanFactory beanFactory) {
        super(container, beanFactory);
        this.loader = loader;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    protected void setItemsToContainer(List<?> list) {
        getContainer().setItems(((List) list));
    }

    @Override
    public void sort(Sort sort) {
        if (loader == null) {
            sortInMemory(sort);
        } else {
            loader.setSort(sort);
            if (loader.getFirstResult() == 0
                    && getContainer().getItems().size() < loader.getMaxResults()) {
                sortInMemory(sort);
            } else {
                loader.load();
            }
        }
    }
}
