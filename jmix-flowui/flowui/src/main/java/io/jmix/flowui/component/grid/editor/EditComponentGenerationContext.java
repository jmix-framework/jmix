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

package io.jmix.flowui.component.grid.editor;

import io.jmix.flowui.component.SupportsStatusChangeHandler;
import io.jmix.flowui.component.SupportsStatusChangeHandler.StatusContext;
import io.jmix.flowui.data.ValueSource;
import io.jmix.flowui.data.ValueSourceProvider;

import jakarta.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Contains information for a function that returns the column editor component
 * used by {@link DataGridEditor}.
 *
 * @param <T> the type of the row/item being edited in {@link DataGridEditor}
 */
public class EditComponentGenerationContext<T> {

    protected final T item;
    protected final ValueSourceProvider valueSourceProvider;
    protected final Consumer<StatusContext<?>> statusHandler;

    public EditComponentGenerationContext(T item,
                                          ValueSourceProvider valueSourceProvider,
                                          @Nullable Consumer<StatusContext<?>> statusHandler) {
        this.item = item;
        this.valueSourceProvider = valueSourceProvider;
        this.statusHandler = statusHandler;
    }

    /**
     * @return an item being edited in {@link DataGridEditor}
     */
    public T getItem() {
        return item;
    }

    /**
     * @return a provider to obtain instances of {@link ValueSource}
     */
    public ValueSourceProvider getValueSourceProvider() {
        return valueSourceProvider;
    }

    /**
     * @return a handler to be set to a component that implements {@link SupportsStatusChangeHandler}
     */
    @Nullable
    public Consumer<StatusContext<?>> getStatusHandler() {
        return statusHandler;
    }
}
