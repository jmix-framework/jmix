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

package io.jmix.flowui.component.gridlayout;

import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.InMemoryDataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import io.jmix.core.MetadataTools;
import io.jmix.flowui.component.delegate.DataViewDelegate;
import io.jmix.flowui.data.SupportsDataProvider;
import io.jmix.flowui.data.SupportsItemsContainer;
import io.jmix.flowui.data.SupportsItemsEnum;
import io.jmix.flowui.data.items.InMemoryDataProviderWrapper;
import io.jmix.flowui.kit.component.gridlayout.GridLayoutDataView;
import io.jmix.flowui.kit.component.gridlayout.GridLayoutListDataView;
import io.jmix.flowui.kit.component.gridlayout.JmixGridLayout;
import io.jmix.flowui.model.CollectionContainer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.Nullable;

/**
 * Represents a customizable grid layout component for displaying data in a grid-based format.
 * Provides integration with data sources to support data binding, items enumeration, and other features.
 *
 * @param <T> the type of the items displayed in this layout
 */
public class GridLayout<T> extends JmixGridLayout<T> implements SupportsDataProvider<T>,
        SupportsItemsContainer<T>, SupportsItemsEnum<T>, ApplicationContextAware, InitializingBean {

    protected ApplicationContext applicationContext;
    protected MetadataTools metadataTools;

    protected DataViewDelegate<GridLayout<T>, T> dataViewDelegate;

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
        dataViewDelegate = createItemsDelegate();

        setItemLabelGenerator(this::applyDefaultValueFormat);
    }

    protected String applyDefaultValueFormat(T item) {
        return metadataTools.format(item);
    }

    @Override
    public void setItems(CollectionContainer<T> container) {
        dataViewDelegate.setItems(container);
    }

    @Override
    public void setItems(Class<T> itemsEnum) {
        dataViewDelegate.setItems(itemsEnum);
    }

    @Override
    public GridLayoutDataView<T> setItems(DataProvider<T, Void> dataProvider) {
        bindDataProvider(dataProvider);
        return super.setItems(dataProvider);
    }

    @Override
    public GridLayoutDataView<T> setItems(InMemoryDataProvider<T> inMemoryDataProvider) {
        // Override Vaadin implementation, so we will have access to the original DataProvider
        InMemoryDataProviderWrapper<T> wrapper = new InMemoryDataProviderWrapper<>(inMemoryDataProvider);
        return setItems(wrapper);
    }

    @Override
    public GridLayoutListDataView<T> setItems(ListDataProvider<T> listDataProvider) {
        bindDataProvider(listDataProvider);
        return super.setItems(listDataProvider);
    }

    protected void bindDataProvider(DataProvider<T, ?> dataProvider) {
        dataViewDelegate.bind(dataProvider);
    }

    @Nullable
    @Override
    public DataProvider<T, ?> getDataProvider() {
        return dataViewDelegate.getDataProvider();
    }

    protected DataViewDelegate<GridLayout<T>, T> createItemsDelegate() {
        //noinspection unchecked
        return applicationContext.getBean(DataViewDelegate.class, this);
    }
}
