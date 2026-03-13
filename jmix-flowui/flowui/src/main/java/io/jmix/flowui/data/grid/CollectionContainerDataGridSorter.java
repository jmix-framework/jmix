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

package io.jmix.flowui.data.grid;

import io.jmix.core.Sort;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.model.BaseCollectionLoader;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.impl.CollectionContainerSorter;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.lang.Nullable;

import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

public class CollectionContainerDataGridSorter extends CollectionContainerSorter {

    private Map<String, Comparator<?>> propertyComparators = Collections.emptyMap();

    public CollectionContainerDataGridSorter(CollectionContainer<?> container,
                                             @Nullable BaseCollectionLoader loader,
                                             BeanFactory beanFactory) {
        super(container, loader, beanFactory);
    }

    /**
     * Sets property comparators. Comparators are used in in-memory sorting.
     * <p>
     * The key of the map is a property name and value is comparator.
     *
     * @param propertyComparators the map of property comparators
     */
    public void setPropertyComparators(@Nullable Map<String, Comparator<?>> propertyComparators) {
        this.propertyComparators = propertyComparators != null ? propertyComparators : Collections.emptyMap();
    }

    @Override
    protected Comparator<?> createComparator(Sort.Order sortOrder, MetaClass metaClass) {
        Comparator<?> comparator = propertyComparators.get(sortOrder.getProperty());
        if (comparator != null) {
            return comparator;
        }

        return super.createComparator(sortOrder, metaClass);
    }
}
