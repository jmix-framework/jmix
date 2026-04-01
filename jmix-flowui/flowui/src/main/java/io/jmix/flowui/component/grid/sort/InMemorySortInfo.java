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

import org.jspecify.annotations.Nullable;

import java.util.Comparator;

/**
 * Represents sorting information for in-memory sorting operations.
 */
public interface InMemorySortInfo extends SortInfo {

    /**
     * @return comparator to be used for in-memory sorting, or {@code null} if not set
     */
    @Nullable Comparator<?> getComparator();

    /**
     * Sets the comparator to be used for in-memory sorting.
     *
     * @param comparator the comparator to set, or {@code null} to clear the comparator
     */
    void setComparator(@Nullable Comparator<?> comparator);
}
