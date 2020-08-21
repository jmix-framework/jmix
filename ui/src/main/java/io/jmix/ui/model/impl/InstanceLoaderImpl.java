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

package io.jmix.ui.model.impl;

import com.google.common.base.Strings;
import io.jmix.core.*;
import io.jmix.core.common.event.EventHub;
import io.jmix.core.common.event.Subscription;
import io.jmix.core.querycondition.Condition;
import io.jmix.ui.model.DataContext;
import io.jmix.ui.model.HasLoader;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.model.InstanceLoader;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 *
 */
public class InstanceLoaderImpl<E extends JmixEntity> implements InstanceLoader<E> {

    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected FetchPlanRepository fetchPlanRepository;
    @Autowired
    protected QueryStringProcessor queryStringProcessor;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected AccessConstraintsRegistry accessConstraintsRegistry;

    protected DataContext dataContext;
    protected InstanceContainer<E> container;
    protected String query;
    protected Condition condition;
    protected Map<String, Object> parameters = new HashMap<>();
    protected Object entityId;
    protected boolean softDeletion = true;
    protected FetchPlan fetchPlan;
    protected String fetchPlanName;
    protected Map<String, Object> hints;
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
            entity = dataContext.merge(entity);
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
            String queryString = queryStringProcessor.process(this.query, entityClass);
            LoadContext.Query query = loadContext.setQueryString(queryString);
            query.setCondition(condition);
            query.setParameters(parameters);
        }

        loadContext.setFetchPlan(resolveFetchPlan());
        loadContext.setSoftDeletion(softDeletion);
        loadContext.setHints(hints);
        loadContext.setAccessConstraints(accessConstraintsRegistry.getConstraints());

        return loadContext;
    }

    protected FetchPlan resolveFetchPlan() {
        FetchPlan view = this.fetchPlan;
        if (view == null && fetchPlanName != null) {
            view = fetchPlanRepository.getFetchPlan(container.getEntityMetaClass(), fetchPlanName);
        }
        if (view == null) {
            view = container.getFetchPlan();
        }
        return view;
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
    public void setParameter(String name, Object value) {
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
    public boolean isSoftDeletion() {
        return softDeletion;
    }

    @Override
    public void setSoftDeletion(boolean softDeletion) {
        this.softDeletion = softDeletion;
    }

    @Override
    public void setHint(String hintName, Object value) {
        if (hints == null) {
            hints = new HashMap<>();
        }
        hints.put(hintName, value);
    }

    @Override
    public Map<String, Object> getHints() {
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

    @Override
    public Subscription addPreLoadListener(Consumer<PreLoadEvent> listener) {
        return events.subscribe(PreLoadEvent.class, listener);
    }

    @Override
    public Subscription addPostLoadListener(Consumer<PostLoadEvent> listener) {
        return events.subscribe(PostLoadEvent.class, listener);
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
    public void setView(String viewName) {
        if (this.fetchPlan != null)
            throw new IllegalStateException("view is already set");
        this.fetchPlanName = viewName;
    }
}
