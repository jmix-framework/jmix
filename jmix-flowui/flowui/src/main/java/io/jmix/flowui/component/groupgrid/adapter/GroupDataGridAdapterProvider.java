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

package io.jmix.flowui.component.groupgrid.adapter;

import io.jmix.flowui.component.groupgrid.GroupListDataComponent;
import org.springframework.lang.Nullable;

/**
 * Provides an adapter for a group grid component.
 *
 * @see DefaultGroupDataGridAdapterFactory
 */
public interface GroupDataGridAdapterProvider {

    /**
     * @param groupGrid the group grid to check
     * @return {@code true} if the adapter supports the given group grid
     */
    boolean isSupported(GroupListDataComponent<?> groupGrid);

    /**
     * Returns an adapter for the given group grid component.
     *
     * @param groupGrid the group grid to get an adapter for
     * @param <E>       the item type
     * @return an adapter instance or {@code null} if no adapter is available
     */
    @Nullable
    <E> AbstractGroupDataGridAdapter<E> getAdapter(GroupListDataComponent<E> groupGrid);
}
