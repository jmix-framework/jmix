/*
 * Copyright 2024 Haulmont.
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

package io.jmix.flowui.kit.component.twincolumn;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.data.provider.AbstractListDataView;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.function.SerializableBiConsumer;
import com.vaadin.flow.function.SerializableComparator;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.function.SerializableSupplier;

public class TwinColumnListDataView<V> extends AbstractListDataView<V> {
    public TwinColumnListDataView(SerializableSupplier<? extends DataProvider<V, ?>> dataProviderSupplier,
                                  Component component,
                                  SerializableBiConsumer<SerializablePredicate<V>, SerializableComparator<V>> filterOrSortingChangedCallback) {
        super(dataProviderSupplier, component, filterOrSortingChangedCallback);
    }
}
