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

package io.jmix.flowui.kit.component.gridlayout;

import com.vaadin.flow.data.provider.AbstractListDataView;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.function.SerializableBiConsumer;
import com.vaadin.flow.function.SerializableComparator;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.function.SerializableSupplier;

/**
 * Data view implementation for {@link JmixGridLayout} with in-memory list data. Provides information on the data and
 * allows operations on it.
 *
 * @param <T> data type
 */
public class GridLayoutListDataView<T> extends AbstractListDataView<T> {

    /**
     * Creates a new in-memory data view for {@link JmixGridLayout} and verifies the passed data provider is
     * compatible with this data view implementation.
     *
     * @param dataProviderSupplier           data provider supplier
     * @param gridLayout                     grid layout instance for this DataView
     * @param filterOrSortingChangedCallback callback, which is being invoked when the GridLayout's filtering
     *                                       or sorting changes, not <code>null</code>
     */
    public GridLayoutListDataView(
            SerializableSupplier<? extends DataProvider<T, ?>> dataProviderSupplier,
            JmixGridLayout<?> gridLayout,
            SerializableBiConsumer<SerializablePredicate<T>, SerializableComparator<T>> filterOrSortingChangedCallback) {
        super(dataProviderSupplier, gridLayout, filterOrSortingChangedCallback);
    }
}
