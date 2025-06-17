/*
 * Copyright 2025 Haulmont.
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

package io.jmix.reports.impl.repository;

import io.jmix.core.Metadata;
import io.jmix.core.Sort;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.reports.entity.ReportGroupInfo;
import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.collections.comparators.ComparatorChain;
import org.apache.commons.collections.comparators.NullComparator;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Comparator;

/**
 * Common methods for implementing data loading operations in in-memory repositories.
 */
@Component("report_InMemoryRepositoryUtils")
public class InMemoryRepositoryUtils {

    protected final Metadata metadata;

    public InMemoryRepositoryUtils(Metadata metadata) {
        this.metadata = metadata;
    }

    /**
     * Check filter condition: contains ignoring case.
     */
    public boolean containsIgnoreCase(@Nullable String propertyValue, @Nullable String filterValue) {
        if (filterValue != null && !filterValue.isEmpty()) {
            return propertyValue != null
                   && propertyValue.toLowerCase().contains(filterValue.toLowerCase());
        }
        return true;
    }

    /**
     * Return comparator that can sort entity lists by {@link Sort.Order} objects referencing entity's properties.
     * Treats null values lower than any value.
     *
     * @param sort sort clauses
     * @param entityClass entity class
     * @return comparator
     * @param <T> entity type
     */
    @SuppressWarnings("unchecked")
    public <T> Comparator<T> createBeanComparator(Sort sort, Class<T> entityClass) {
        ComparatorChain chain = new ComparatorChain();

        MetaClass metaClass = metadata.getClass(entityClass);

        for (Sort.Order sortOrder : sort.getOrders()) {
            if (metaClass.findProperty(sortOrder.getProperty()) == null) {
                throw new IllegalArgumentException(String.format("Invalid sort %s: entity %s doesn't have property %s",
                        sort, metaClass.getName(), sortOrder.getProperty()
                ));
            }

            NullComparator nullsAreLowPropertyComparator = new NullComparator(false);
            BeanComparator<ReportGroupInfo> comparator = new BeanComparator<>(sortOrder.getProperty(), nullsAreLowPropertyComparator);
            chain.addComparator(comparator, sortOrder.getDirection() == Sort.Direction.DESC);
        }
        return chain;
    }
}
