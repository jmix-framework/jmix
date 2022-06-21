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

package io.jmix.flowui.component.delegate;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.HasDataView;
import io.jmix.flowui.data.SupportsDataProvider;
import io.jmix.flowui.data.binding.DataViewBinding;
import io.jmix.flowui.data.binding.impl.DataViewBindingImpl;

import javax.annotation.Nullable;

public abstract class AbstractDataViewDelegate<C extends Component & HasDataView<V, ?, ?> & SupportsDataProvider<V>, V>
        extends AbstractComponentDelegate<C> {

    protected DataViewBinding<C, V> binding;

    public AbstractDataViewDelegate(C component) {
        super(component);
    }

    public void bind(@Nullable DataProvider<V, ?> dataProvider) {
        if (this.binding != null) {
            this.binding.unbind();
            this.binding = null;
        }

        if (dataProvider != null) {
            this.binding = new DataViewBindingImpl<>(component, dataProvider);
            this.binding.bind();
        }
    }
}
