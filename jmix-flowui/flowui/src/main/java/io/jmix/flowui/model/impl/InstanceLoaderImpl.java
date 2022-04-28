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

import com.google.common.base.Strings;
import io.jmix.core.*;
import io.jmix.core.common.event.EventHub;
import io.jmix.core.common.event.Subscription;
import io.jmix.core.querycondition.Condition;
import io.jmix.flowui.RequiresChanges;
import io.jmix.flowui.SameAsUi;
import io.jmix.flowui.model.*;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 *
 */
@SameAsUi
@RequiresChanges
public class InstanceLoaderImpl<E> implements InstanceLoader<E> {

    @Autowired
    protected DataManager dataManager;

    @Autowired
    protected FetchPlanRepository fetchPlanRepository;
    @Autowired
    protected List<QueryStringProcessor> queryStringProcessors;
    @Autowired
    protected Metadata metadata;

    protected DataContext dataContext;
    protected InstanceContainer<E> container;
    protected String query;
    protected Condition condition;
    protected Map<String, Object> parameters = new HashMap<>();
    protected Object entityId;
    protected FetchPlan fetchPlan;
    protected String fetchPlanName;
    protected Map<String, Serializable> hints = new HashMap<>();
    protected Function<LoadContext<E>, E> delegate;
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

        E entity;

        LoadContext<E> loadContext = createLoadContext();

        if (delegate == null) {
            if (!needLoad())
                return;

            if (!sendPreLoadEvent(loadContext)) {
                return;
            }

            entity = dataManager.load(loadContext);

            if (entity == null) {
                throw new EntityAccessException(container.getEntityMetaClass(), entityId);
            }
        } else {
            if (!sendPreLoadEvent(loadContext)) {
                return;
            }
            entity = delegate.apply(createLoadContext());
        }

        if (dataContext != null) {
            entity = dataContext.merge(entity, new MergeOptions().setFresh(true));
        }
        container.setItem(entity);

        sendPostLoadEvent(entity);
    }

    protected boolean needLoad() {
        return entityId != null || !Strings.isNullOrEmpty(query);
    }

    public LoadContext<E> createLoadContext() {
        Class<E> entityClass = container.getEntityMetaClass().getJavaClass();

        LoadContext<E> loadContext = new LoadContext(metadata.getClass(entityClass));

        if (entityId != null) {
            loadContext.setId(entityId);
        } else {
            String queryString = QueryUtils.applyQueryStringProcessors(queryStringProcessors, this.query, entityClass);
            LoadContext.Query query = loadContext.setQueryString(queryString);
            query.setCondition(condition);
            query.setParameters(parameters);
        }

        loadContext.setFetchPlan(resolveFetchPlan());
        loadContext.setHints(hints);

        return loadContext;
    }

    protected FetchPlan resolveFetchPlan() {
        FetchPlan fp = this.fetchPlan;
        if (fp == null && fetchPlanName != null) {
            fp = fetchPlanRepository.getFetchPlan(container.getEntityMetaClass(), fetchPlanName);
        }
        if (fp == null) {
            fp = container.getFetchPlan();
        }
        return fp;
    }

    protected boolean sendPreLoadEvent(LoadContext<E> loadContext) {
        PreLoadEvent<E> preLoadEvent = new PreLoadEvent<>(this, loadContext);
        events.publish(PreLoadEvent.class, preLoadEvent);
        return !preLoadEvent.isLoadPrevented();
    }

    protected void sendPostLoadEvent(E entity) {
        PostLoadEvent<E> postLoadEvent = new PostLoadEvent<>(this, entity);
        events.publish(PostLoadEvent.class, postLoadEvent);
    }

    @Override
    public InstanceContainer<E> getContainer() {
        return container;
    }

    @Override
    public void setContainer(InstanceContainer<E> container) {
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
    public Object getEntityId() {
        return entityId;
    }

    @Override
    public void setEntityId(Object entityId) {
        this.entityId = entityId;
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
    public Function<LoadContext<E>, E> getLoadDelegate() {
        return delegate;
    }

    @Override
    public void setLoadDelegate(Function<LoadContext<E>, E> delegate) {
        this.delegate = delegate;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Subscription addPreLoadListener(Consumer<PreLoadEvent<E>> listener) {
        return events.subscribe(PreLoadEvent.class, (Consumer) listener);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Subscription addPostLoadListener(Consumer<PostLoadEvent<E>> listener) {
        return events.subscribe(PostLoadEvent.class, (Consumer) listener);
    }

    @Override
    public FetchPlan getFetchPlan() {
        return fetchPlan;
    }

    @Override
    public void setFetchPlan(FetchPlan fetchPlan) {
        this.fetchPlan = fetchPlan;
    }

    @Override
    public void setFetchPlan(String fetchPlanName) {
        if (this.fetchPlan != null)
            throw new IllegalStateException("fetch plan is already set");
        this.fetchPlanName = fetchPlanName;
    }
}
