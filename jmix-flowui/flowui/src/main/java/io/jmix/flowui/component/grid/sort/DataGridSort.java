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
import com.vaadin.flow.data.provider.SortDirection;
import io.jmix.core.Sort;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.flowui.component.grid.EnhancedDataGrid;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Represents sorting information for a data grid, including both in-memory and persistent sorting.
 */
public class DataGridSort {

    private static final Logger log = LoggerFactory.getLogger(DataGridSort.class);

    protected List<SortInfo> sortInfos;
    protected List<InMemorySortInfo> inMemorySorts;

    protected DataGridSort(List<SortInfo> sortInfos, List<InMemorySortInfo> inMemorySorts) {
        this.sortInfos = new ArrayList<>(sortInfos);
        this.inMemorySorts = new ArrayList<>(inMemorySorts);
    }

    public static DataGridSort by(List<SortInfo> sorts, List<InMemorySortInfo> inMemorySorts) {
        Preconditions.checkNotNullArgument(sorts);
        Preconditions.checkNotNullArgument(inMemorySorts);
        return new DataGridSort(sorts, inMemorySorts);
    }

    /**
     * Retrieves an unmodifiable list of in-memory sorting information.
     *
     * @return a list of {@link InMemorySortInfo}
     */
    public List<InMemorySortInfo> getInMemorySorts() {
        return Collections.unmodifiableList(inMemorySorts);
    }

    /**
     * Retrieves an unmodifiable list of sorting information.
     *
     * @return a list of {@link SortInfo}
     */
    public List<SortInfo> getSortInfos() {
        return Collections.unmodifiableList(sortInfos);
    }

    public Sort convertInMemoryToSort() {
        List<Sort.Order> orders = new ArrayList<>();
        for (InMemorySortInfo sortInfo : inMemorySorts) {
            String property = sortInfo.getMetaPropertyPath() != null
                    ? sortInfo.getMetaPropertyPath().toPathString()
                    : Objects.requireNonNull(sortInfo.getProperty());
            if (sortInfo.getMetaPropertyPath() != null || sortInfo.getComparator() != null) {
                orders.add(sortInfo.isAscending() ? Sort.Order.asc(property) : Sort.Order.desc(property));
            } else {
                log.warn("Skipping sorting by property {} because comparator is not set", property);
            }
        }
        return Sort.by(orders);
    }

    /**
     * Converts the list of {@link SortInfo} objects to a {@link Sort} object.
     * <p>
     * If a {@link SortInfo} contains a JPQL expression, it creates an
     * {@link Sort.ExpressionOrder}, otherwise {@link Sort.Order} is created.
     *
     * @return a {@link Sort} instance.
     */
    public Sort convertToSort() {
        List<Sort.Order> orders = new ArrayList<>();
        for (SortInfo sortInfo : sortInfos) {
            String property = sortInfo.getMetaPropertyPath() != null
                    ? sortInfo.getMetaPropertyPath().toPathString()
                    : Objects.requireNonNull(sortInfo.getProperty());

            // TODO: pinyazhin, check that transient properties are filtered out in result query
            if (!Strings.isNullOrEmpty(sortInfo.getExpression())) {
                orders.add(sortInfo.isAscending() ? Sort.ExpressionOrder.asc(sortInfo.getExpression()) : Sort.ExpressionOrder.desc(sortInfo.getExpression()));
            } else if (sortInfo.getMetaPropertyPath() != null) {
                orders.add(sortInfo.isAscending() ? Sort.Order.asc(property) : Sort.Order.desc(property));
            } else {
                log.warn("Columns that are not bound with entity property must contain JPQL expression." +
                        " Skipping sorting by property {} because JPQL expression is not set", property);
            }
        }
        return Sort.by(orders);
    }

    /**
     * Represents abstract sorting information about property and direction.
     */
    public static class AbstractSortInfo {

        protected MetaPropertyPath metaPropertyPath;
        protected String property;

        protected boolean ascending;

        // TODO: pinyazhin, javadoc
        public AbstractSortInfo(EnhancedDataGrid.DataGridSortContext.ColumnSortInfo columnSortInfo) {
            Preconditions.checkNotNullArgument(columnSortInfo);

            this.metaPropertyPath = columnSortInfo.getMetaPropertyPath();
            this.property = columnSortInfo.getColumn().getKey();
            this.ascending = columnSortInfo.isAscending();
        }

        /**
         * Constructor for sorting by property name or column key.
         *
         * @param metaPropertyPath meta-property path of the property to be sorted
         * @param property         property name or column key to be sorted
         * @param ascending        sorting direction
         */
        public AbstractSortInfo(@Nullable MetaPropertyPath metaPropertyPath, @Nullable String property,
                                boolean ascending) {
            this.metaPropertyPath = metaPropertyPath;
            this.property = property;
            this.ascending = ascending;
        }

        /**
         * @return meta-property path of the property to be sorted, or {@code null} if not set
         */
        @Nullable
        public MetaPropertyPath getMetaPropertyPath() {
            return metaPropertyPath;
        }

        /**
         * @return property name or column key to be sorted, or {@code null} if not set
         */
        @Nullable
        public String getProperty() {
            return property;
        }

        /**
         * @return {@code true} if sorting is ascending, {@code false} otherwise
         */
        public boolean isAscending() {
            return ascending;
        }
    }

    /**
     * Represents sorting information performed in the database.
     */
    public static class SortInfo extends AbstractSortInfo {

        protected String expression;

        public SortInfo(EnhancedDataGrid.DataGridSortContext.ColumnSortInfo columnSortInfo) {
            super(columnSortInfo);
        }

        public SortInfo(MetaPropertyPath metaPropertyPath, boolean ascending) {
            super(metaPropertyPath, null, ascending);
        }

        public SortInfo(String key, boolean ascending) {
            super(null, key, ascending);
        }

        /**
         * Converts a list of {@link EnhancedDataGrid.DataGridSortContext.ColumnSortInfo} objects
         * into a list of {@link SortInfo} objects.
         *
         * @param sorts the list of info to be converted
         * @return a list of {@link SortInfo}
         */
        public static List<SortInfo> of(List<EnhancedDataGrid.DataGridSortContext.ColumnSortInfo> sorts) {
            return sorts.stream().map(SortInfo::new).toList();
        }

        /**
         * @return JPQL expression for sorting, or {@code null} if not set
         */
        @Nullable
        public String getExpression() {
            return expression;
        }

        /**
         * Sets the expression for sorting.
         * <p>
         * For instance, for JPQL it can be {@code "{E}.name"} or {@code "function('calc_total_sum', {E}.id)"}.
         * <p>
         * <strong>Note that for {@link KeyValueEntity} queries the entity alias placeholder {E} is not replaced, so
         * you should use the same alias as it is defined in the query.</strong>
         *
         * @param expression the expression to be used for sorting
         * @return current instance of {@link SortInfo}
         */
        public SortInfo withExpression(@Nullable String expression) {
            this.expression = expression;
            return this;
        }
    }

    /**
     * Represents in-memory sorting information for a property or column key used for in-memory sorting.
     */
    public static class InMemorySortInfo extends AbstractSortInfo {

        protected Comparator<?> comparator;

        // TODO: pinyazhin, javadoc
        public InMemorySortInfo(EnhancedDataGrid.DataGridSortContext.ColumnSortInfo columnSortInfo) {
            super(columnSortInfo.getMetaPropertyPath(),
                    columnSortInfo.getColumn().getKey(),
                    columnSortInfo.isAscending());

            // Retrieve comparator as is
            this.comparator = columnSortInfo.getColumn().getComparator(SortDirection.ASCENDING);
        }

        /**
         * Constructor for sorting by property name or column key.
         *
         * @param property  property name or column key to be sorted
         * @param ascending sorting direction
         */
        public InMemorySortInfo(String property, boolean ascending) {
            super(null, property, ascending);
        }

        /**
         * Constructor for sorting by meta-property path.
         *
         * @param metaPropertyPath meta-property path
         * @param ascending        sorting direction
         */
        public InMemorySortInfo(MetaPropertyPath metaPropertyPath, boolean ascending) {
            super(metaPropertyPath, null, ascending);
        }

        /**
         * Converts a list of {@link EnhancedDataGrid.DataGridSortContext.ColumnSortInfo} objects
         * into a list of {@link SortInfo} objects.
         *
         * @param sorts the list of info to be converted
         * @return list of {@link InMemorySortInfo} objects
         */
        public static List<InMemorySortInfo> of(List<EnhancedDataGrid.DataGridSortContext.ColumnSortInfo> sorts) {
            return sorts.stream().map(InMemorySortInfo::new).toList();
        }

        /**
         * @return comparator to be used for in-memory sorting, or {@code null} if not set
         */
        @Nullable
        public Comparator<?> getComparator() {
            return comparator;
        }

        /**
         * Sets the comparator to be used for in-memory sorting.
         *
         * @param comparator the comparator to set
         * @return current instance of {@link InMemorySortInfo}
         */
        public InMemorySortInfo withComparator(@Nullable Comparator<?> comparator) {
            this.comparator = comparator;
            return this;
        }
    }
}
