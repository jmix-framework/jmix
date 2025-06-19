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

package io.jmix.flowui.data.binding;


import com.vaadin.flow.component.Component;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.HasDataView;

/**
 * Represents a binding between a UI component and a data provider. This interface is used to
 * manage and synchronize data between the UI and the underlying data source.
 *
 * @param <C> the type of the component, which must extend {@link Component} and implement
 *            {@link HasDataView}
 * @param <V> the type of data provided by the {@link DataProvider}
 */
public interface DataViewBinding<C extends Component & HasDataView<V, ?, ?>, V> extends JmixBinding {

    /**
     * Returns the component associated with this binding.
     *
     * @return the component associated with this binding
     */
    C getComponent();

    /**
     * Retrieves the data provider associated with this binding.
     *
     * @return the data provider managing the data, or {@code null} if no data provider is bound
     */
    DataProvider<V, ?> getDataProvider();
}
