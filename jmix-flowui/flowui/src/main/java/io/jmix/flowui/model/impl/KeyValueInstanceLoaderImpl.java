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
import io.jmix.core.Stores;
import io.jmix.core.ValueLoadContext;
import io.jmix.core.common.event.EventHub;
import io.jmix.core.common.event.Subscription;
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.querycondition.Condition;
import io.jmix.flowui.model.DataContext;
import io.jmix.flowui.model.HasLoader;
import io.jmix.flowui.model.KeyValueContainer;
import io.jmix.flowui.model.KeyValueInstanceLoader;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class KeyValueInstanceLoaderImpl implements KeyValueInstanceLoader {

    @Autowired
    protected DataManager dataManager;

    protected DataContext dataContext;
    protected KeyValueContainer container;
    protected String query;
    protected Condition condition;
    protected Map<String, Object> parameters = new HashMap<>();
    protected Map<String, Serializable> hints = new HashMap<>();
    protected String storeName = Stores.MAIN;
    protected Function<ValueLoadContext, KeyValueEntity> delegate;
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

        KeyValueEntity result = null;
        if (delegate == null) {
            List<KeyValueEntity> list = dataManager.loadValues(loadContext);
            if (!list.isEmpty()) {
                result = list.get(0);
            }
        } else {
            result = delegate.apply(loadContext);
        }

        container.setItem(result);
        sendPostLoadEvent(result);
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
        query.setParameters(parameters);
        query.setMaxResults(1);

        loadContext.setHints(hints);

        return loadContext;
    }

    protected boolean sendPreLoadEvent(ValueLoadContext loadContext) {
        PreLoadEvent preLoadEvent = new PreLoadEvent(this, loadContext);
        events.publish(PreLoadEvent.class, preLoadEvent);
        return !preLoadEvent.isLoadPrevented();
    }

    protected void sendPostLoadEvent(@Nullable KeyValueEntity entity) {
        PostLoadEvent postLoadEvent = new PostLoadEvent(this, entity);
        events.publish(PostLoadEvent.class, postLoadEvent);
    }

    @Override
    public KeyValueContainer getContainer() {
        return container;
    }

    @Override
    public void setContainer(KeyValueContainer container) {
        this.container = container;
        if (container instanceof HasLoader) {
            ((HasLoader) container).setLoader(this);
        }
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
    public void setHint(String hintName, Serializable value) {
        hints.put(hintName, value);
    }

    @Override
    public Map<String, Serializable> getHints() {
        return hints;
    }

    @Override
    public Function<ValueLoadContext, KeyValueEntity> getLoadDelegate() {
        return delegate;
    }

    @Override
    public void setLoadDelegate(Function<ValueLoadContext, KeyValueEntity> delegate) {
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
    public void setStoreName(String name) {
        storeName = name != null ? name : Stores.MAIN;
    }
}
