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

import com.vaadin.flow.data.provider.AbstractDataView;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.function.SerializableSupplier;

/**
 * Implementation of generic data view for {@link JmixGridLayout}.
 *
 * @param <T> the item type
 */
public class GridLayoutDataView<T> extends AbstractDataView<T> {

    /**
     * Constructs a new generic data view for {@link JmixGridLayout} and verifies the passed data provider is
     * compatible with this data view implementation.
     *
     * @param dataProviderSupplier data provider supplier
     * @param gridLayout           grid layout instance for this DataView
     */
    public GridLayoutDataView(SerializableSupplier<? extends DataProvider<T, ?>> dataProviderSupplier,
                              JmixGridLayout<T> gridLayout) {
        super(dataProviderSupplier, gridLayout);
    }

    @Override
    protected Class<?> getSupportedDataProviderType() {
        return DataProvider.class;
    }

    @Override
    public T getItem(int index) {
        final int dataSize = dataProviderSupplier.get().size(new Query<>());
        if (dataSize == 0) {
            throw new IndexOutOfBoundsException("Requested index %d on empty data".formatted(index));
        }

        if (index < 0 || index >= dataSize) {
            throw new IndexOutOfBoundsException(
                    "Given index %d is outside of the accepted range '0 - %d'".formatted(index, dataSize - 1)
            );
        }

        return getItems().skip(index).findFirst().orElse(null);
    }
}
