/*
 * Copyright 2020 Haulmont.
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

package io.jmix.ui.component.pagination.data;

import io.jmix.core.DataManager;
import io.jmix.core.LoadContext;
import io.jmix.core.ValueLoadContext;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.ui.model.*;
import io.jmix.ui.model.impl.WeakCollectionChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.function.Consumer;

public abstract class AbstractPaginationDataBinder implements PaginationDataBinder {

    private static final Logger log = LoggerFactory.getLogger(PaginationLoaderBinder.class);

    protected DataManager dataManager;

    protected CollectionContainer container;
    protected Consumer<CollectionChangeType> refreshListener;

    protected Consumer<CollectionContainer.CollectionChangeEvent> containerCollectionChangeListener;
    protected WeakCollectionChangeListener weakContainerCollectionChangeListener;

    protected BaseCollectionLoader loader;

    protected void attachCollectionChangeListener() {
        containerCollectionChangeListener = e -> {
            if (refreshListener != null) {
                refreshListener.accept(e.getChangeType());
            }
        };

        weakContainerCollectionChangeListener = new WeakCollectionChangeListener(
                container, containerCollectionChangeListener);
    }

    @Autowired
    public void setDataManager(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Override
    public void removeCollectionChangeListener() {
        weakContainerCollectionChangeListener.removeItself();
    }

    @Override
    public int getFirstResult() {
        return loader != null ? loader.getFirstResult() : 0;
    }

    @Override
    public int getMaxResults() {
        return loader != null ? loader.getMaxResults() : Integer.MAX_VALUE;
    }

    @Override
    public void setFirstResult(int startPosition) {
        if (loader != null)
            loader.setFirstResult(startPosition);
    }

    @Override
    public void setMaxResults(int maxResults) {
        if (loader != null)
            loader.setMaxResults(maxResults);
    }

    @SuppressWarnings("unchecked")
    @Override
    public int getCount() {
        if (loader == null) {
            return container.getItems().size();
        }

        if (loader instanceof CollectionLoader) {
            LoadContext context = ((CollectionLoader) loader).createLoadContext();
            return (int) dataManager.getCount(context);
        } else if (loader instanceof KeyValueCollectionLoader) {
            ValueLoadContext context = ((KeyValueCollectionLoader) loader).createLoadContext();
            return (int) dataManager.getCount(context);
        } else {
            log.warn("Unsupported loader type: {}", loader.getClass().getName());
            return 0;
        }
    }

    @Override
    public MetaClass getEntityMetaClass() {
        return container.getEntityMetaClass();
    }

    @Override
    public int size() {
        return container.getItems().size();
    }

    @Override
    public void refresh() {
        if (loader != null)
            loader.load();
    }

    @Override
    public void setCollectionChangeListener(Consumer<CollectionChangeType> listener) {
        this.refreshListener = listener;
    }
}
