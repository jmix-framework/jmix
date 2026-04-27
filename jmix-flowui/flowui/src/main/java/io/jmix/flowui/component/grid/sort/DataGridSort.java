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
 * Represents sorting information for a data grid, including both in-memory and persistent sorting.
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
