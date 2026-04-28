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

package io.jmix.data.persistence;

import org.jspecify.annotations.Nullable;

/**
 * Interface to be implemented by Spring beans to supply JPQL sort expressions for datatype and LOB properties.
 * <p>
 * Suppliers are applied in order defined by {@link org.springframework.core.Ordered} or
 * {@link org.springframework.core.annotation.Order}. Return {@code null} if the supplier does not support the
 * specified property.
 */
public interface JpqlSortExpressionSupplier {

    /**
     * Returns JPQL order expression for the specified property,
     * e.g. <code>{E}.property</code>, where <code>{E}</code> is a selected entity alias.
     * It's possible to:
     * <ul>
     *     <li>Apply JPQL functions for property, e.g <code>upper({E}.property)</code></li>
     *     <li>Use <code>asc/desc</code> or <code>nulls last/nulls first</code>,
     *     e.g. <code>{E}.property asc nulls first</code></li>
     * </ul>
     *
     * @return JPQL order expression for the specified property.
     */
    @Nullable
    default String getDatatypeSortExpression(SortExpressionContext context) {
        return null;
    }

    /**
     * Returns JPQL order expression for the specified LOB property.
     *
     * @return JPQL order expression for the specified LOB property.
     */
    @Nullable
    default String getLobSortExpression(SortExpressionContext context) {
        return null;
    }
}
