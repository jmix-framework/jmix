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

package io.jmix.flowui.model.impl;

import io.jmix.core.DataManager;
import io.jmix.core.Sort;
import io.jmix.core.Stores;
import io.jmix.core.ValueLoadContext;
import io.jmix.core.common.event.EventHub;
import io.jmix.core.common.event.Subscription;
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.querycondition.Condition;
import io.jmix.flowui.model.*;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class KeyValueCollectionLoaderImpl implements KeyValueCollectionLoader {

    @Autowired
    protected DataManager dataManager;

    @Autowired
    protected SorterFactory sorterFactory;

    protected DataContext dataContext;
    protected KeyValueCollectionContainer container;
    protected String query;
    protected Condition condition;
    protected Map<String, Object> parameters = new HashMap<>();
    protected int firstResult = 0;
    protected int maxResults = Integer.MAX_VALUE;
    protected Sort sort;
    protected Map<String, Serializable> hints = new HashMap<>();
    protected String storeName = Stores.MAIN;
    protected Function<ValueLoadContext, List<KeyValueEntity>> delegate;
    protected EventHub events = new EventHub();

    @Nullable
    @Override
    public DataContext getDataContext() {
        return dataContext;
    }

    @Override
    public void setDataContext(@Nullable DataContext dataContext) {
        this.dataContext = dataContext;
    }

    @Override
    public void load() {
        if (container == null)
            throw new IllegalStateException("container is null");
        if (query == null && delegate == null)
            throw new IllegalStateException("both query and delegate are null");

        ValueLoadContext loadContext = createLoadContext();

        if (!sendPreLoadEvent(loadContext)) {
            return;
        }

        List<KeyValueEntity> list;
        if (delegate == null) {
            list = dataManager.loadValues(loadContext);
        } else {
            list = delegate.apply(loadContext);
        }

        if (dataContext != null) {
            List<KeyValueEntity> mergedList = new ArrayList<>(list.size());
            for (KeyValueEntity entity : list) {
                mergedList.add(dataContext.merge(entity));
            }
            container.setItems(mergedList);
        } else {
            container.setItems(list);
        }

        sendPostLoadEvent(list);
    }

    @Override
    public ValueLoadContext createLoadContext() {
        ValueLoadContext loadContext = ValueLoadContext.create();
        loadContext.setStoreName(storeName);
        loadContext.setIdName(container.getIdName());
        for (MetaProperty property : container.getEntityMetaClass().getProperties()) {
            loadContext.addProperty(property.getName());
        }

        ValueLoadContext.Query query = loadContext.setQueryString(this.query);

        query.setCondition(condition);
        query.setSort(sort);
        query.setParameters(parameters);

        if (firstResult > 0)
            query.setFirstResult(firstResult);
        if (maxResults < Integer.MAX_VALUE)
            query.setMaxResults(maxResults);

        loadContext.setHints(hints);

        return loadContext;
    }

    protected boolean sendPreLoadEvent(ValueLoadContext loadContext) {
        PreLoadEvent preLoadEvent = new PreLoadEvent(this, loadContext);
        events.publish(PreLoadEvent.class, preLoadEvent);
        return !preLoadEvent.isLoadPrevented();
    }

    protected void sendPostLoadEvent(List<KeyValueEntity> entities) {
        PostLoadEvent postLoadEvent = new PostLoadEvent(this, entities);
        events.publish(PostLoadEvent.class, postLoadEvent);
    }

    @Override
    public KeyValueCollectionContainer getContainer() {
        return container;
    }

    @Override
    public void setContainer(KeyValueCollectionContainer container) {
        this.container = container;
        if (container instanceof HasLoader) {
            ((HasLoader) container).setLoader(this);
        }
        container.setSorter(sorterFactory.createCollectionContainerSorter(container, this));
    }

    @Override
    public String getQuery() {
        return query;
    }

    @Override
    public void setQuery(String query) {
        this.query = query;
    }

    @Nullable
    @Override
    public Condition getCondition() {
        return condition;
    }

    @Override
    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    @Override
    public Map<String, Object> getParameters() {
        return Collections.unmodifiableMap(parameters);
    }

    @Override
    public void setParameters(Map<String, Object> parameters) {
        this.parameters.clear();
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            setParameter(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public Object getParameter(String name) {
        return parameters.get(name);
    }

    @Override
    public void setParameter(String name, @Nullable Object value) {
        parameters.put(name, value);
    }

    @Override
    public void removeParameter(String name) {
        parameters.remove(name);
    }

    @Override
    public int getMaxResults() {
        return maxResults;
    }

    @Override
    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    @Nullable
    @Override
    public Sort getSort() {
        return sort;
    }

    @Override
    public void setSort(@Nullable Sort sort) {
        if (sort == null || sort.getOrders().isEmpty()) {
            this.sort = null;
        } else {
            this.sort = sort;
        }
    }

    @Override
    public void setHint(String hintName, Serializable value) {
        hints.put(hintName, value);
    }

    @Override
    public Map<String, Serializable> getHints() {
        return hints;
    }

    @Override
    public Function<ValueLoadContext, List<KeyValueEntity>> getDelegate() {
        return delegate;
    }

    @Override
    public void setLoadDelegate(Function<ValueLoadContext, List<KeyValueEntity>> delegate) {
        this.delegate = delegate;
    }

    @Override
    public Subscription addPreLoadListener(Consumer<PreLoadEvent> listener) {
        return events.subscribe(PreLoadEvent.class, listener);
    }

    @Override
    public Subscription addPostLoadListener(Consumer<PostLoadEvent> listener) {
        return events.subscribe(PostLoadEvent.class, listener);
    }

    @Override
    public String getStoreName() {
        return storeName;
    }

    @Override
    public void setStoreName(@Nullable String name) {
        storeName = name != null ? name : Stores.MAIN;
    }

    @Override
    public int getFirstResult() {
        return firstResult;
    }

    @Override
    public void setFirstResult(int firstResult) {
        this.firstResult = firstResult;
    }
}
