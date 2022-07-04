/*
 * Copyright 2019 Haulmont.
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

package io.jmix.flowui.model;

import io.jmix.core.AccessManager;
import io.jmix.core.Metadata;
import io.jmix.core.Stores;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.model.impl.*;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * Factory bean for data API components.
 */
@Component("ui_DataComponents")
public class DataComponents {

    // TODO: gg, constructor injection
    @Autowired
    protected AutowireCapableBeanFactory beanFactory;

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected SorterFactory sorterFactory;

    @Autowired
    protected AccessManager accessManager;

    @Autowired
    protected Stores stores;

    protected void autowire(Object instance) {
        beanFactory.autowireBean(instance);

        if (instance instanceof InitializingBean) {
            try {
                ((InitializingBean) instance).afterPropertiesSet();
            } catch (Exception e) {
                throw new RuntimeException(
                        "Unable to initialize UI Component - calling afterPropertiesSet for " +
                                instance.getClass(), e);
            }
        }
    }

    /**
     * Creates {@code DataContext}.
     */
    public DataContext createDataContext() {
        DataContextImpl dataContext = new DataContextImpl();
        autowire(dataContext);
        return dataContext;
    }

    /**
     * Creates {@code InstanceContainer}.
     */
    public <E> InstanceContainer<E> createInstanceContainer(Class<E> entityClass) {
        InstanceContainerImpl<E> container = new InstanceContainerImpl<>(metadata.getClass(entityClass));
        autowire(container);
        return container;
    }

    /**
     * Creates {@code InstancePropertyContainer}.
     */
    @SuppressWarnings("unchecked")
    public <E> InstancePropertyContainer<E> createInstanceContainer(Class<E> entityClass,
                                                                    InstanceContainer<?> masterContainer,
                                                                    String property) {
        InstancePropertyContainerImpl<E> container = new InstancePropertyContainerImpl<>(
                metadata.getClass(entityClass), masterContainer, property);
        autowire(container);

        // TODO: gg, implement
//        UiEntityContext entityContext = new UiEntityContext(masterContainer.getEntityMetaClass());
//        accessManager.applyRegisteredConstraints(entityContext);

//        UiEntityAttributeContext attributeContext = new UiEntityAttributeContext(masterContainer.getEntityMetaClass(), property);
//        accessManager.applyRegisteredConstraints(attributeContext);

//        if (entityContext.isViewPermitted()
//                && attributeContext.canView()) {
        masterContainer.addItemChangeListener(e -> {
            Object item = masterContainer.getItemOrNull();
            container.setItem(item != null ? EntityValues.getValue(item, property) : null);
        });

        masterContainer.addItemPropertyChangeListener(e -> {
            if (e.getProperty().equals(property)) {
                container.setItem((E) e.getValue());
            }
        });
//        }

        return container;
    }

    /**
     * Creates {@code CollectionContainer}.
     */
    public <E> CollectionContainer<E> createCollectionContainer(Class<E> entityClass) {
        CollectionContainerImpl<E> container = new CollectionContainerImpl<>(metadata.getClass(entityClass));
        autowire(container);
        container.setSorter(sorterFactory.createCollectionContainerSorter(container, null));
        return container;
    }

    /**
     * Creates {@code CollectionPropertyContainer}.
     */
    @SuppressWarnings("unchecked")
    public <E> CollectionPropertyContainer<E> createCollectionContainer(Class<E> entityClass,
                                                                        InstanceContainer<?> masterContainer,
                                                                        String property) {
        CollectionPropertyContainerImpl<E> container = new CollectionPropertyContainerImpl<>(
                metadata.getClass(entityClass), masterContainer, property);
        autowire(container);
        container.setSorter(sorterFactory.createCollectionPropertyContainerSorter(container));

        // TODO: gg, implement
//        UiEntityContext entityContext = new UiEntityContext(masterContainer.getEntityMetaClass());
//        accessManager.applyRegisteredConstraints(entityContext);

//        UiEntityAttributeContext attributeContext = new UiEntityAttributeContext(masterContainer.getEntityMetaClass(), property);
//        accessManager.applyRegisteredConstraints(attributeContext);

//        if (attributeContext.canView()
//                && entityContext.isViewPermitted()) {
        masterContainer.addItemChangeListener(e -> {
            Object item = masterContainer.getItemOrNull();
            container.setItems(item != null ? EntityValues.getValue(item, property) : null);
        });

        masterContainer.addItemPropertyChangeListener(e -> {
            if (e.getProperty().equals(property) && e.getItem() == masterContainer.getItem()) {
                container.setDisconnectedItems((Collection<E>) e.getValue());
            }
        });
//        }

        return container;
    }

    /**
     * Creates {@code KeyValueContainer}.
     */
    public KeyValueContainer createKeyValueContainer() {
        KeyValueContainerImpl container = new KeyValueContainerImpl();
        autowire(container);
        container.getEntityMetaClass().setStore(stores.get(Stores.NOOP));
        return container;
    }

    /**
     * Creates {@code KeyValueContainer} for the given MetaClass.
     */
    public KeyValueContainer createKeyValueContainer(MetaClass metaClass) {
        KeyValueContainerImpl container = new KeyValueContainerImpl(metaClass);
        autowire(container);
        container.getEntityMetaClass().setStore(stores.get(Stores.NOOP));
        return container;
    }

    /**
     * Creates {@code KeyValueCollectionContainer}.
     */
    public KeyValueCollectionContainer createKeyValueCollectionContainer() {
        KeyValueCollectionContainerImpl container = new KeyValueCollectionContainerImpl();
        autowire(container);
        container.getEntityMetaClass().setStore(stores.get(Stores.NOOP));
        container.setSorter(sorterFactory.createCollectionContainerSorter(container, null));
        return container;
    }

    /**
     * Creates {@code InstanceLoader}.
     */
    public <E> InstanceLoader<E> createInstanceLoader() {
        InstanceLoaderImpl<E> loader = new InstanceLoaderImpl<>();
        autowire(loader);
        return loader;
    }

    /**
     * Creates {@code CollectionLoader}.
     */
    public <E> CollectionLoader<E> createCollectionLoader() {
        CollectionLoaderImpl<E> loader = new CollectionLoaderImpl<>();
        autowire(loader);
        return loader;
    }

    /**
     * Creates {@code KeyValueCollectionLoader}.
     */
    public KeyValueCollectionLoader createKeyValueCollectionLoader() {
        KeyValueCollectionLoaderImpl loader = new KeyValueCollectionLoaderImpl();
        autowire(loader);
        return loader;
    }

    /**
     * Creates {@code KeyValueInstanceLoader}.
     */
    public KeyValueInstanceLoader createKeyValueInstanceLoader() {
        KeyValueInstanceLoaderImpl loader = new KeyValueInstanceLoaderImpl();
        autowire(loader);
        return loader;
    }
}
