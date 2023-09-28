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

package io.jmix.flowui.component.virtuallist;

import com.vaadin.flow.component.virtuallist.VirtualList;
import com.vaadin.flow.data.provider.DataProvider;
import io.jmix.core.MetadataTools;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.metamodel.datatype.EnumClass;
import io.jmix.flowui.component.delegate.VirtualListDelegate;
import io.jmix.flowui.data.SupportsDataProvider;
import io.jmix.flowui.data.SupportsItemsContainer;
import io.jmix.flowui.data.SupportsItemsEnum;
import io.jmix.flowui.data.items.ContainerDataProvider;
import io.jmix.flowui.data.items.EnumDataProvider;
import io.jmix.flowui.model.CollectionContainer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.Nullable;

public class JmixVirtualList<V> extends VirtualList<V> implements SupportsDataProvider<V>,
        SupportsItemsContainer<V>, SupportsItemsEnum<V>, ApplicationContextAware, InitializingBean {

    protected ApplicationContext applicationContext;
    protected MetadataTools metadataTools;

    protected VirtualListDelegate<V> delegate;

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
        delegate = createDelegate();
        setRenderer(v -> metadataTools.format(v));
    }

    @SuppressWarnings("unchecked")
    protected VirtualListDelegate<V> createDelegate() {
        return applicationContext.getBean(VirtualListDelegate.class, this);
    }

    @Override
    public void setItems(CollectionContainer<V> container) {
        Preconditions.checkNotNullArgument(container);

        setDataProvider(new ContainerDataProvider<>(container));
    }

    @Override
    public void setItems(Class<V> itemsEnum) {
        Preconditions.checkNotNullArgument(itemsEnum);

        if (!itemsEnum.isEnum() || !EnumClass.class.isAssignableFrom(itemsEnum)) {
            throw new IllegalArgumentException(
                    String.format("Items class '%s' must be enumeration and implement %s",
                            itemsEnum, EnumClass.class.getSimpleName()));
        }

        setDataProvider(new EnumDataProvider<>(itemsEnum));
    }

    @Override
    public void setDataProvider(DataProvider<V, ?> dataProvider) {
        bindDataProvider(dataProvider);
        super.setDataProvider(dataProvider);
    }

    protected void bindDataProvider(DataProvider<V, ?> dataProvider) {
        delegate.bind(dataProvider);
    }

    @Nullable
    @Override
    public DataProvider<V, ?> getDataProvider() {
        return delegate.getDataProvider();
    }
}
