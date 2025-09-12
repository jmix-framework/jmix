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
import io.jmix.core.comparator.EntityValuesComparator;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import org.apache.commons.collections.comparators.ComparatorChain;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;

/**
 * Common methods for implementing data loading operations in in-memory repositories.
 */
@Component("report_RepositoryUtil")
public class RepositoryUtil {

    protected final Metadata metadata;
    protected final BeanFactory beanFactory;

    public RepositoryUtil(Metadata metadata, BeanFactory beanFactory) {
        this.metadata = metadata;
        this.beanFactory = beanFactory;
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
     * Check filter condition: boolean equals.
     */
    public boolean equalsTo(@Nullable Boolean propertyValue, @Nullable Boolean filterValue) {
        if (filterValue != null) {
            return filterValue ? BooleanUtils.isTrue(propertyValue) : BooleanUtils.isNotTrue(propertyValue);
        }
        return true;
    }

    /**
     * Check filter condition: Date is after or equals.
     */
    public boolean dateAfterOrEquals(@Nullable Date propertyValue, @Nullable Date filterValue) {
        if (filterValue != null) {
            return propertyValue != null && !propertyValue.before(filterValue);
        }
        return true;
    }

    /**
     * Check filter condition: entity property equals (by id).
     */
    public boolean entityEquals(@Nullable Object propertyValue, @Nullable Object filterValue) {
        if (filterValue != null) {
            return propertyValue != null && propertyValue.equals(filterValue); // compares by id
        }
        return true;
    }

    /**
     * Return comparator that can sort entity lists by {@link Sort.Order} objects referencing entity's properties.
     *
     * @param entityClass entity class
     * @return builder object
     * @param <T> entity type
     */
    public <T> ComparatorBuilder<T> comparatorBuilder(Class<T> entityClass) {
        return new ComparatorBuilder<>(entityClass);
    }

    public class ComparatorBuilder<T> {
        private final MetaClass metaClass;
        private final Map<String, Function<T, ?>> customProperties;

        private ComparatorBuilder(Class<T> entityClass) {
            this.metaClass = metadata.getClass(entityClass);
            this.customProperties = new HashMap<>();
        }

        /**
         * Build comparator.
         * Checks that sort keys are valid.
         *
         * @param sort sort clauses
         * @return prepared comparator
         */
        public Comparator<T> build(Sort sort) {
            ComparatorChain chain = new ComparatorChain();
            for (Sort.Order sortOrder : sort.getOrders()) {
                String sortKey = sortOrder.getProperty();
                Comparator<T> comparator;

                Function<T, ?> valueExtractor;
                if (customProperties.containsKey(sortKey)) {
                    valueExtractor = customProperties.get(sortKey);
                } else {
                    MetaPropertyPath propertyPath = metaClass.getPropertyPath(sortKey);
                    if (propertyPath == null) {
                        throw new IllegalArgumentException(String.format(
                                "Invalid sort %s: entity %s doesn't have property %s, and no custom property registered",
                                sort, metaClass.getName(), sortKey
                        ));
                    }

                    valueExtractor = e -> EntityValues.getValueEx(e, propertyPath);
                }
                EntityValuesComparator<Object> valueComparator = new EntityValuesComparator<>(
                        sortOrder.getDirection() == Sort.Direction.ASC, metaClass, beanFactory);
                comparator = Comparator.comparing(valueExtractor, valueComparator);

                chain.addComparator(comparator);
            }

            return chain;
        }

        /**
         * Register additional custom comparator for a sort key which isn't a property of the entity,
         *   e.g. for a calculatable property.
         * A value extractor function must be provided.
         * Extracted values are cached, to avoid repeated computations of the same value.
         *
         * @param sortKey sort key, expected to be used by repository clients
         * @param valueExtractor function to extract comparable value from the entity, similar to {@link Comparator#comparing(Function)}
         * @return builder object
         * @param <U> type of comparable value
         */
        public <U extends Comparable<? super U>> ComparatorBuilder<T> customProperty(
                String sortKey, Function<T, ? extends U> valueExtractor) {

            // cache to avoid repeated computations of the same value
            IdentityHashMap<T, U> valueCache = new IdentityHashMap<>();

            Function<T, ? extends U> cachingExtractor = entity -> {
                if (valueCache.containsKey(entity)) {
                    return valueCache.get(entity);
                }
                U value = valueExtractor.apply(entity);
                valueCache.put(entity, value);
                return value;
            };

            customProperties.put(sortKey, cachingExtractor);
            return this;
        }
    }
}
