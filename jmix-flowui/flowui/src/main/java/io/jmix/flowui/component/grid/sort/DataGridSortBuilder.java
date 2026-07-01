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

import io.jmix.core.common.util.Preconditions;
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.flowui.component.grid.ColumnSortInfo;
import io.jmix.flowui.component.grid.DataGridSortContext;
import io.jmix.flowui.component.grid.EnhancedDataGrid;
import io.jmix.flowui.component.grid.sort.impl.InMemorySortInfoImpl;
import io.jmix.flowui.component.grid.sort.impl.PersistentSortInfoImpl;
import org.jspecify.annotations.Nullable;

import java.util.*;

/**
 * Builder for {@link DataGridSort}. It builds {@link DataGridSort} object using information from
 * {@link DataGridSortContext}.
 * <p>
 * The builder provides methods for replacing existing sort information. For instance:
 * <pre>
 * DataGridSortBuilder.create(context)
 *         .replaceSort("fullName", List.of("{E}.firstName", "{E}.lastName"),
 *                 Comparator.comparing(Customer::getFirstName).thenComparing(Customer::getLastName))
 *         .build();
 * </pre>
 *
 * @param <E> entity type
 * @see DataGridSortContext
 * @see EnhancedDataGrid
 */
public class DataGridSortBuilder<E> {

    protected final Map<String, InMemorySortInfo> inMemorySortInfos;
    protected final Map<String, PersistentSortInfo> persistentSortInfos;

    protected DataGridSortBuilder(DataGridSortContext<E> context) {
        inMemorySortInfos = collectInMemorySortInfos(context.getSortInfos());
        persistentSortInfos = collectPersistentSortInfos(context.getSortInfos());
    }

    /**
     * Creates a new instance of {@code DataGridSortBuilder} with the specified context.
     *
     * @param context the {@code DataGridSortContext} to be used
     * @return a new instance of {@code DataGridSortBuilder} initialized with the given context
     */
    public static <E> DataGridSortBuilder<E> create(DataGridSortContext<E> context) {
        Preconditions.checkNotNullArgument(context);

        return new DataGridSortBuilder<>(context);
    }

    /**
     * Replaces existing sort information for the specified column key in both in-memory and persistent sort information.
     * <p>
     * In expression parameter, in case of JPQL, the {@code {E}} alias can be used for JPA entities.
     * For instance, {@code "function('calc_total_sum', {E}.id)"}.
     * <p>
     * <strong>Note that for {@link KeyValueEntity}, the {@code {E}} alias is not supported.</strong> Use the concrete
     * alias from the query, e.g. {@code "function('calc_total_sum', e.id)"}.
     *
     * @param key        column key
     * @param comparator comparator to be used for in-memory sorting
     * @param expression expression to be used for persistent sorting
     * @return current instance
     */
    public DataGridSortBuilder<E> replaceSort(String key, @Nullable String expression, @Nullable Comparator<E> comparator) {
        Preconditions.checkNotNullArgument(key);

        replaceSort(key, comparator);
        replaceSort(key, expression);

        return this;
    }

    /**
     * Replaces existing sort information for the specified column key in both in-memory and persistent sort information.
     * <p>
     * In expression parameter, in case of JPQL, the {@code {E}} alias can be used for JPA entities.
     * For instance, {@code "function('calc_total_sum', {E}.id)"}.
     * <p>
     * <strong>Note that for {@link KeyValueEntity}, the {@code {E}} alias is not supported.</strong> Use the concrete
     * alias from the query, e.g. {@code "function('calc_total_sum', e.id)"}.
     *
     * @param key         column key
     * @param comparator  comparator to be used for in-memory sorting
     * @param expressions list of expressions to be used for persistent sorting
     * @return current instance
     */
    public DataGridSortBuilder<E> replaceSort(String key, List<String> expressions, @Nullable Comparator<E> comparator) {
        Preconditions.checkNotNullArgument(key);

        replaceSort(key, comparator);
        replaceSort(key, expressions);

        return this;
    }

    /**
     * Replaces existing sort information for the specified column key in the in-memory sort information.
     *
     * @param key        column key
     * @param comparator comparator to be used for in-memory sorting
     * @return current instance
     */
    public DataGridSortBuilder<E> replaceSort(String key, @Nullable Comparator<E> comparator) {
        Preconditions.checkNotNullArgument(key);

        InMemorySortInfo inMemorySortInfo = inMemorySortInfos.get(key);
        if (inMemorySortInfo != null) {
            inMemorySortInfo.setComparator(comparator);
        }

        return this;
    }

    /**
     * Replaces existing sort information for the specified column key in the persistent sort information.
     * <p>
     * In expression parameter, in case of JPQL, the {@code {E}} alias can be used for JPA entities.
     * For instance, {@code "function('calc_total_sum', {E}.id)"}.
     * <p>
     * <strong>Note that for {@link KeyValueEntity}, the {@code {E}} alias is not supported.</strong> Use the concrete
     * alias from the query, e.g. {@code "e.id"}.
     *
     * @param key        column key
     * @param expression expression to be used for persistent sorting
     * @return current instance
     */
    public DataGridSortBuilder<E> replaceSort(String key, @Nullable String expression) {
        Preconditions.checkNotNullArgument(key);

        PersistentSortInfo persistentSortInfo = persistentSortInfos.get(key);
        if (persistentSortInfo != null) {
            persistentSortInfo.setExpressions(expression == null ? Collections.emptyList() : List.of(expression));
        }

        return this;
    }

    /**
     * Replaces existing sort information for the specified column key in the persistent sort information.
     * <p>
     * In expression parameter, in case of JPQL, the {@code {E}} alias can be used for JPA entities.
     * For instance, {@code "function('calc_total_sum', {E}.id)"}.
     * <p>
     * <strong>Note that for {@link KeyValueEntity}, the {@code {E}} alias is not supported.</strong> Use the concrete
     * alias from the query, e.g. {@code "e.id"}.
     *
     * @param key         column key
     * @param expressions list of expressions to be used for persistent sorting
     * @return current instance
     */
    public DataGridSortBuilder<E> replaceSort(String key, List<String> expressions) {
        Preconditions.checkNotNullArgument(key);
        Preconditions.checkNotNullArgument(expressions);

        PersistentSortInfo persistentSortInfo = persistentSortInfos.get(key);
        if (persistentSortInfo != null) {
            persistentSortInfo.setExpressions(expressions);
        }

        return this;
    }

    /**
     * @return a list of {@code InMemorySortInfo} objects representing the in-memory sort configuration
     */
    public List<InMemorySortInfo> getInMemorySortInfos() {
        return new ArrayList<>(inMemorySortInfos.values());
    }

    /**
     * @return a list of {@code PersistentSortInfo} objects representing the persistent sort configuration
     */
    public List<PersistentSortInfo> getPersistentSortInfos() {
        return new ArrayList<>(persistentSortInfos.values());
    }

    /**
     * @return a new instance of {@code DataGridSort} built from the current configuration
     */
    public DataGridSort build() {
        return DataGridSort.by(getPersistentSortInfos(), getInMemorySortInfos());
    }

    protected Map<String, InMemorySortInfo> collectInMemorySortInfos(List<ColumnSortInfo<E>> columnSortInfos) {
        Map<String, InMemorySortInfo> inMemorySortInfos = new LinkedHashMap<>(columnSortInfos.size());
        for (ColumnSortInfo<E> sortInfo : columnSortInfos) {
            MetaPropertyPath mpp = sortInfo.getMetaPropertyPath();
            String columnKey = sortInfo.getColumn().getKey();
            Comparator<E> comparator = sortInfo.getColumn().getComparatorOrNull();

            if (mpp != null) {
                inMemorySortInfos.put(columnKey,
                        createInMemorySortInfo(mpp.toPathString(), sortInfo.isAscending(), mpp, comparator));
            } else {
                inMemorySortInfos.put(columnKey,
                        createInMemorySortInfo(columnKey, sortInfo.isAscending(), null, comparator));
            }
        }
        return inMemorySortInfos;
    }

    protected Map<String, PersistentSortInfo> collectPersistentSortInfos(List<ColumnSortInfo<E>> columnSortInfos) {
        Map<String, PersistentSortInfo> persistentSortInfos = new LinkedHashMap<>(columnSortInfos.size());
        for (ColumnSortInfo<E> sortInfo : columnSortInfos) {
            MetaPropertyPath mpp = sortInfo.getMetaPropertyPath();
            String columnKey = sortInfo.getColumn().getKey();
            if (mpp != null) {
                persistentSortInfos.put(columnKey,
                        createPersistentSortInfo(mpp.toPathString(), sortInfo.isAscending(), mpp, List.of()));
            } else {
                persistentSortInfos.put(columnKey,
                        createPersistentSortInfo(columnKey, sortInfo.isAscending(), null, List.of()));
            }
        }
        return persistentSortInfos;
    }

    protected InMemorySortInfo createInMemorySortInfo(String property,
                                                      boolean ascending,
                                                      @Nullable MetaPropertyPath mpp,
                                                      @Nullable Comparator<?> comparator) {
        return new InMemorySortInfoImpl(property, ascending, mpp, comparator);
    }

    protected PersistentSortInfo createPersistentSortInfo(String property,
                                                          boolean ascending,
                                                          @Nullable MetaPropertyPath mpp,
                                                          List<String> expressions) {
        return new PersistentSortInfoImpl(property, ascending, mpp,  expressions);
    }
}
