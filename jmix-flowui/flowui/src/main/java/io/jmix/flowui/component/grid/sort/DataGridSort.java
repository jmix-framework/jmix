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

package io.jmix.flowui.component.grid.sort;

import com.google.common.base.Strings;
import io.jmix.core.Sort;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Represents the sorting logic for a data grid, supporting both persistent and in-memory sorting operations.
 * This class aggregates sorting information and provides utilities to convert those into sorting configurations.
 */
public class DataGridSort {

    private static final Logger log = LoggerFactory.getLogger(DataGridSort.class);

    protected List<PersistentSortInfo> persistentSortInfos;
    protected List<InMemorySortInfo> inMemorySortInfos;

    protected DataGridSort(List<PersistentSortInfo> persistentSortInfos,
                           List<InMemorySortInfo> inMemorySortInfos) {
        this.persistentSortInfos = Collections.unmodifiableList(persistentSortInfos);
        this.inMemorySortInfos = Collections.unmodifiableList(inMemorySortInfos);
    }

    /**
     * Creates a new instance of {@link DataGridSort} with the specified sorting information.
     *
     * @param persistentSortInfos sort information for persistent sorting
     * @param inMemorySortInfos   sort information for in-memory sorting
     * @return a new instance of {@link DataGridSort}
     */
    public static DataGridSort by(List<PersistentSortInfo> persistentSortInfos,
                                  List<InMemorySortInfo> inMemorySortInfos) {
        Preconditions.checkNotNullArgument(persistentSortInfos);
        Preconditions.checkNotNullArgument(inMemorySortInfos);

        return new DataGridSort(persistentSortInfos, inMemorySortInfos);
    }

    /**
     * Retrieves an unmodifiable list of in-memory sorting information.
     * <p>
     * Note that returned numbers of sorts may be different from the number of sorts in the result {@link Sort} object
     * when {@link #toInMemorySort()} is called.
     *
     * @return a list of {@link InMemorySortInfo}
     */
    public List<InMemorySortInfo> getInMemorySortInfos() {
        return inMemorySortInfos;
    }

    /**
     * Retrieves an unmodifiable list of persistent sorting information.
     * <p>
     * Note that returned numbers of sorts may be different from the number of sorts in the result {@link Sort} object
     * when {@link #toPersistentSort()} is called.
     *
     * @return a list of {@link PersistentSortInfo}
     */
    public List<PersistentSortInfo> getPersistentSortInfos() {
        return persistentSortInfos;
    }

    /**
     * Converts the current persistent sorting information into a {@link Sort} object.
     * <p>
     * This method iterates through a list of {@link PersistentSortInfo} objects and constructs a
     * {@link Sort} object based on the defined sorting attributes. It handles both regular property-based
     * sorting and expression-based sorting. If an expression is missing for a property not bound
     * to an entity, the method skips sorting for that property.
     *
     * @return a {@link Sort} object representing the current persistent sorting criteria
     */
    public Sort toPersistentSort() {
        List<Sort.Order> orders = new ArrayList<>();
        for (PersistentSortInfo sortInfo : persistentSortInfos) {
            String property = sortInfo.getMetaPropertyPath() != null
                    ? sortInfo.getMetaPropertyPath().toPathString()
                    : Objects.requireNonNull(sortInfo.getSortKey());

            if (CollectionUtils.isNotEmpty(sortInfo.getExpressions())) {
                for (String expression : sortInfo.getExpressions()) {
                    if (!Strings.isNullOrEmpty(expression)) {
                        orders.add(sortInfo.isAscending()
                                ? Sort.ExpressionOrder.asc(expression)
                                : Sort.ExpressionOrder.desc(expression));
                    }
                }
            } else if (sortInfo.getMetaPropertyPath() != null) {
                orders.add(sortInfo.isAscending() ? Sort.Order.asc(property) : Sort.Order.desc(property));
            } else {
                log.debug("Columns that are not bound with entity property must contain an expression." +
                        " Skipping sorting by property {} because an expression is not set", property);
            }
        }
        return Sort.by(orders);
    }

    /**
     * Converts the current in-memory sorting information into a {@link Sort} object.
     * <p>
     * This method iterates through a list of {@link InMemorySortInfo} objects and constructs a
     * {@link Sort} object based on the defined sorting attributes. It handles both regular
     * property-based sorting and comparator-based sorting. If a comparator is not set or the
     * property does not have a meta property path, the corresponding sort is skipped.
     *
     * @return a {@link Sort} object representing the current in-memory sorting criteria
     */
    public Sort toInMemorySort() {
        List<Sort.Order> orders = new ArrayList<>();
        for (InMemorySortInfo sortInfo : inMemorySortInfos) {
            String property = sortInfo.getMetaPropertyPath() != null
                    ? sortInfo.getMetaPropertyPath().toPathString()
                    : Objects.requireNonNull(sortInfo.getSortKey());
            if (sortInfo.getMetaPropertyPath() != null || sortInfo.getComparator() != null) {
                orders.add(sortInfo.isAscending() ? Sort.Order.asc(property) : Sort.Order.desc(property));
            } else {
                log.debug("Skipping sorting by property {} because comparator is not set or property does not have {}",
                        property, MetaPropertyPath.class.getSimpleName());
            }
        }
        return Sort.by(orders);
    }
}
