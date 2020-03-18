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

package io.jmix.ui.model;

import io.jmix.core.Entity;
import io.jmix.core.Metadata;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.security.Security;
import io.jmix.ui.model.impl.*;
import io.jmix.core.security.EntityOp;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.Collection;

/**
 * Factory bean for data API components.
 */
@Component("jmix_DataComponents")
public class DataComponents implements ApplicationContextAware {

    @Inject
    protected Metadata metadata;

    @Inject
    protected Security security;

    @Inject
    protected SorterFactory sorterFactory;

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(@Nonnull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * Creates {@code DataContext}.
     */
    public DataContext createDataContext() {
        return new DataContextImpl(applicationContext);
    }

    /**
     * Creates {@code InstanceContainer}.
     */
    public <E extends Entity> InstanceContainer<E> createInstanceContainer(Class<E> entityClass) {
        return new InstanceContainerImpl<>(applicationContext, metadata.getClass(entityClass));
    }

    /**
     * Creates {@code InstancePropertyContainer}.
     */
    @SuppressWarnings("unchecked")
    public <E extends Entity> InstancePropertyContainer<E> createInstanceContainer(Class<E> entityClass,
                                                                                   InstanceContainer<? extends Entity> masterContainer,
                                                                                   String property) {
        InstancePropertyContainerImpl<E> container = new InstancePropertyContainerImpl<>(
                applicationContext, metadata.getClass(entityClass), masterContainer, property);

        if (security.isEntityAttrReadPermitted(masterContainer.getEntityMetaClass(), property)
                && security.isEntityOpPermitted(entityClass, EntityOp.READ)) {
            masterContainer.addItemChangeListener(e -> {
                Entity item = masterContainer.getItemOrNull();
                container.setItem(item != null ? EntityValues.getValue(item, property) : null);
            });

            masterContainer.addItemPropertyChangeListener(e -> {
                if (e.getProperty().equals(property)) {
                    container.setItem((E) e.getValue());
                }
            });
        }

        return container;
    }

    /**
     * Creates {@code CollectionContainer}.
     */
    public <E extends Entity> CollectionContainer<E> createCollectionContainer(Class<E> entityClass) {
        CollectionContainerImpl<E> container = new CollectionContainerImpl<>(
                applicationContext, metadata.getClass(entityClass));
        container.setSorter(sorterFactory.createCollectionContainerSorter(container, null));
        return container;
    }

    /**
     * Creates {@code CollectionPropertyContainer}.
     */
    @SuppressWarnings("unchecked")
    public <E extends Entity> CollectionPropertyContainer<E> createCollectionContainer(Class<E> entityClass,
                                                                                       InstanceContainer<? extends Entity> masterContainer,
                                                                                       String property) {
        CollectionPropertyContainerImpl<E> container = new CollectionPropertyContainerImpl<>(
                applicationContext, metadata.getClass(entityClass), masterContainer, property);
        container.setSorter(sorterFactory.createCollectionPropertyContainerSorter(container));

        if (security.isEntityAttrReadPermitted(masterContainer.getEntityMetaClass(), property)
                && security.isEntityOpPermitted(entityClass, EntityOp.READ)) {
            masterContainer.addItemChangeListener(e -> {
                Entity item = masterContainer.getItemOrNull();
                container.setItems(item != null ? EntityValues.getValue(item, property) : null);
            });

            masterContainer.addItemPropertyChangeListener(e -> {
                if (e.getProperty().equals(property)) {
                    container.setDisconnectedItems((Collection<E>) e.getValue());
                }
            });
        }

        return container;
    }

    /**
     * Creates {@code KeyValueContainer}.
     */
    public KeyValueContainer createKeyValueContainer() {
        return new KeyValueContainerImpl(applicationContext);
    }

    /**
     * Creates {@code KeyValueCollectionContainer}.
     */
    public KeyValueCollectionContainer createKeyValueCollectionContainer() {
        return new KeyValueCollectionContainerImpl(applicationContext);
    }

    /**
     * Creates {@code InstanceLoader}.
     */
    public <E extends Entity> InstanceLoader<E> createInstanceLoader() {
        return new InstanceLoaderImpl<>(applicationContext);
    }

    /**
     * Creates {@code CollectionLoader}.
     */
    public <E extends Entity> CollectionLoader<E> createCollectionLoader() {
        return new CollectionLoaderImpl<>(applicationContext);
    }

    /**
     * Creates {@code KeyValueCollectionLoader}.
     */
    public KeyValueCollectionLoader createKeyValueCollectionLoader() {
        return new KeyValueCollectionLoaderImpl(applicationContext);
    }

    /**
     * Creates {@code KeyValueInstanceLoader}.
     */
    public KeyValueInstanceLoader createKeyValueInstanceLoader() {
        return new KeyValueInstanceLoaderImpl(applicationContext);
    }
}
