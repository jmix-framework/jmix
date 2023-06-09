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

package io.jmix.flowui.component.listbox;

import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.listbox.dataview.ListBoxDataView;
import com.vaadin.flow.component.listbox.dataview.ListBoxListDataView;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.InMemoryDataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import io.jmix.core.MetadataTools;
import io.jmix.flowui.component.delegate.DataViewDelegate;
import io.jmix.flowui.data.SupportsDataProvider;
import io.jmix.flowui.data.SupportsItemsContainer;
import io.jmix.flowui.data.SupportsItemsEnum;
import io.jmix.flowui.data.items.InMemoryDataProviderWrapper;
import io.jmix.flowui.model.CollectionContainer;
import org.springframework.lang.Nullable;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class JmixListBox<V> extends ListBox<V> implements SupportsDataProvider<V>,
        SupportsItemsContainer<V>, SupportsItemsEnum<V>, ApplicationContextAware, InitializingBean {

    protected ApplicationContext applicationContext;
    protected MetadataTools metadataTools;

    protected DataViewDelegate<JmixListBox<V>, V> dataViewDelegate;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        autowireDependencies();
        initComponent();
    }

    protected void autowireDependencies() {
        metadataTools = applicationContext.getBean(MetadataTools.class);
    }

    protected void initComponent() {
        dataViewDelegate = createOptionsDelegate();

        setItemLabelGenerator(this::applyDefaultValueFormat);
    }

    protected String applyDefaultValueFormat(V value) {
        return metadataTools.format(value);
    }

    @Override
    public void setItems(CollectionContainer<V> container) {
        dataViewDelegate.setItems(container);
    }

    @Override
    public void setItems(Class<V> itemsEnum) {
        dataViewDelegate.setItems(itemsEnum);
    }

    @Override
    public ListBoxDataView<V> setItems(DataProvider<V, Void> dataProvider) {
        bindDataProvider(dataProvider);
        return super.setItems(dataProvider);
    }

    @Override
    public ListBoxDataView<V> setItems(InMemoryDataProvider<V> inMemoryDataProvider) {
        // Override Vaadin implementation, so we will have access to the original DataProvider
        InMemoryDataProviderWrapper<V> wrapper = new InMemoryDataProviderWrapper<>(inMemoryDataProvider);
        return setItems(wrapper);
    }

    @Override
    public ListBoxListDataView<V> setItems(ListDataProvider<V> listDataProvider) {
        bindDataProvider(listDataProvider);
        return super.setItems(listDataProvider);
    }

    protected void bindDataProvider(DataProvider<V, ?> dataProvider) {
        // One of binding methods is called from a constructor so bean can be null
        if (dataViewDelegate != null) {
            dataViewDelegate.bind(dataProvider);
        }
    }

    @Nullable
    @Override
    public DataProvider<V, ?> getDataProvider() {
        return dataViewDelegate.getDataProvider();
    }

    @SuppressWarnings("unchecked")
    protected DataViewDelegate<JmixListBox<V>, V> createOptionsDelegate() {
        return applicationContext.getBean(DataViewDelegate.class, this);
    }
}
