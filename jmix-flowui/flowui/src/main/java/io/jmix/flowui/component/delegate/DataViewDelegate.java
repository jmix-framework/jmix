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
import com.vaadin.flow.data.provider.HasDataView;
import com.vaadin.flow.data.provider.HasListDataView;
import io.jmix.flowui.data.SupportsDataProvider;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;

@org.springframework.stereotype.Component("flowui_DataViewDelegate")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DataViewDelegate<C extends Component
        & HasDataView<V, ?, ?> & HasListDataView<V, ?> & SupportsDataProvider<V>, V>
        extends AbstractDataViewDelegate<C, V> {

    public DataViewDelegate(C component) {
        super(component);
    }
}
