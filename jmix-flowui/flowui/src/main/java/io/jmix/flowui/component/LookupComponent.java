/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.component;

import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;

/**
 * A component which can be set as lookup component for a view.
 */
public interface LookupComponent<T> {

    /**
     * @param selectHandler handler that should be executed when a user
     *                      select an item in a lookup screen, {@code null} to remove
     */
    void setLookupSelectHandler(@Nullable Consumer<Collection<T>> selectHandler);

    /**
     * @return items selected in this lookup component
     */
    Set<T> getSelectedItems();

    interface MultiSelectLookupComponent<T> extends LookupComponent<T> {

        default void enableMultiSelect() {
            setMultiSelect(true);
        }

        void setMultiSelect(boolean multiSelect);
    }
}
