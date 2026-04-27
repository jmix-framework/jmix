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

import io.jmix.core.metamodel.model.MetaPropertyPath;
import org.jspecify.annotations.Nullable;

/**
 * Interface representing sorting information for a property.
 */
public interface SortInfo {

    /**
     * @return the MetaPropertyPath object representing the property path, or {@code null} if not set
     */
    @Nullable
    MetaPropertyPath getMetaPropertyPath();

    /**
     * Returns the sorting key that can be a column key or a property path.
     *
     * @return the sorting key
     */
    String getSortKey();

    /**
     * @return {@code true} if the sort is ascending, {@code false} otherwise
     */
    boolean isAscending();
}
