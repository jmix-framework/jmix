/*
 * Copyright 2019 Haulmont.
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

package io.jmix.ui.widget.data;

import javax.annotation.Nullable;

/**
 * Interface defining methods for enhancing {@link com.vaadin.data.provider.HierarchicalDataProvider} behavior.
 *
 * @param <T> data type
 */
public interface EnhancedHierarchicalDataProvider<T> {

    /**
     * Returns the hierarchy level of an item.
     *
     * @param item the item to get level
     * @return the level of the given item
     */
    int getLevel(T item);

    /**
     * Returns the parent of given item.
     *
     * @param item the item to get parent
     * @return the parent of given item or {@code null} if no parent
     */
    @Nullable
    T getParent(T item);
}
