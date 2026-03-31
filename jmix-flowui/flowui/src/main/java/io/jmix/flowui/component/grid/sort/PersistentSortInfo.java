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

import io.jmix.core.entity.KeyValueEntity;
import org.jspecify.annotations.Nullable;

/**
 * Represents sorting information for persistent sorting operations.
 */
public interface PersistentSortInfo extends SortInfo {

    /**
     * @return the expression to be used for persistent sorting, or {@code null} if not set
     */
    @Nullable
    String getExpression();

    /**
     * Sets the expression to be used for persistent sorting.
     * <p>
     * In case of JPQL, the {@code {E}} alias can be used for JPA entities.
     * For instance, {@code "function('calc_total_sum', {E}.id)"}.
     * <p>
     * <strong>Note that for {@link KeyValueEntity}, the {@code {E}} alias is not supported.</strong> Use the concrete
     * alias from the query, e.g. {@code "function('calc_total_sum', e.id)"}.
     *
     * @param expression the expression to set, or {@code null} to clear the expression
     */
    void setExpression(@Nullable String expression);
}
