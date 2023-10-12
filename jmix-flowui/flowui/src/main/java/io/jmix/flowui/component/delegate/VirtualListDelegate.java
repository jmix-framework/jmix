/*
 * Copyright 2023 Haulmont.
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

import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.component.virtuallist.JmixVirtualList;
import io.jmix.flowui.data.EntityItems;
import io.jmix.flowui.data.items.InMemoryDataProviderWrapper;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Component("flowui_VirtualListDelegate")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class VirtualListDelegate<V> extends AbstractComponentDelegate<JmixVirtualList<V>> {

    protected DataProvider<V, ?> dataProvider;
    protected Registration itemsChangeRegistration;

    public VirtualListDelegate(JmixVirtualList<V> component) {
        super(component);
    }

    public void bind(@Nullable DataProvider<V, ?> dataProvider) {
        unbind();

        if (dataProvider != null) {
            doBind(dataProvider);
        }
    }

    protected void unbind() {
        if (itemsChangeRegistration != null) {
            itemsChangeRegistration.remove();
            itemsChangeRegistration = null;
        }
        dataProvider = null;
    }

    @SuppressWarnings("unchecked")
    protected void doBind(DataProvider<V, ?> dataProvider) {
        this.dataProvider = dataProvider instanceof InMemoryDataProviderWrapper<?>
                ? ((InMemoryDataProviderWrapper<V>) dataProvider).getDataProvider()
                : dataProvider;
        if (this.dataProvider instanceof EntityItems) {
            itemsChangeRegistration = ((EntityItems<V>) this.dataProvider).addItemsChangeListener(this::onItemsChanged);
        }
    }

    protected void onItemsChanged(EntityItems.ItemsChangeEvent<V> itemsChangeEvent) {
        dataProvider.refreshAll();
    }

    @Nullable
    public DataProvider<V, ?> getDataProvider() {
        return dataProvider;
    }
}
