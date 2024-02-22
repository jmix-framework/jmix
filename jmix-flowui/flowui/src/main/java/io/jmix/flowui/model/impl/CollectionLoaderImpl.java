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

package io.jmix.flowui.model.impl;

import io.jmix.core.*;
import io.jmix.core.common.event.EventHub;
import io.jmix.core.common.event.Subscription;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.querycondition.Condition;
import io.jmix.core.querycondition.LogicalCondition;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.flowui.model.*;
import io.jmix.flowui.monitoring.DataLoaderLifeCycle;
import io.jmix.flowui.monitoring.DataLoaderMonitoringInfo;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import static io.jmix.flowui.monitoring.UiMonitoring.*;

/**
 *
 */
public class CollectionLoaderImpl<E> implements CollectionLoader<E> {

    @Autowired
    protected DataManager dataManager;

    @Autowired
    protected FetchPlanRepository fetchPlanRepository;
    @Autowired
    protected SorterFactory sorterFactory;
    @Autowired
    protected List<QueryStringProcessor> queryStringProcessors;
    @Autowired
    protected MeterRegistry meterRegistry;

    protected DataContext dataContext;
    protected CollectionContainer<E> container;
    protected String query;
    protected Condition condition;
    protected Map<String, Object> parameters = new HashMap<>();
    protected int firstResult = 0;
    protected int maxResults = Integer.MAX_VALUE;
    protected boolean cacheable;
    protected FetchPlan fetchPlan;
    protected String fetchPlanName;
    protected Sort sort;
    protected Map<String, Serializable> hints = new HashMap<>();
    protected Function<LoadContext<E>, List<E>> delegate;
    protected EventHub events = new EventHub();
    protected Function<DataLoader, DataLoaderMonitoringInfo> monitoringInfoProvider = __ -> DataLoaderMonitoringInfo.empty();

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
    public void setMonitoringInfoProvider(Function<DataLoader, DataLoaderMonitoringInfo> monitoringInfoProvider) {
        Preconditions.checkNotNullArgument(monitoringInfoProvider);
        this.monitoringInfoProvider = monitoringInfoProvider;
    }

    @Override
    public Function<DataLoader, DataLoaderMonitoringInfo> getMonitoringInfoProvider() {
        return monitoringInfoProvider;
    }

    @Override
    public void load() {
        _load();
    }

    protected boolean _load() {
        if (container == null)
            throw new IllegalStateException("container is null");
        if (query == null && delegate == null)
            throw new IllegalStateException("both query and delegate are null");

        LoadContext<E> loadContext = createLoadContext();

        if (!sendPreLoadEvent(loadContext)) {
            return false;
        }

        List<E> list;

        Timer.Sample sample = startTimerSample(meterRegistry);

        if (delegate == null) {
            list = dataManager.loadList(loadContext);
        } else {
            list = delegate.apply(loadContext);
            if (list == null) {
                return false;
            }
        }

        DataLoaderMonitoringInfo info = monitoringInfoProvider.apply(this);
        stopDataLoaderTimerSample(sample, meterRegistry, DataLoaderLifeCycle.LOAD, info);

        if (dataContext != null) {
            List<E> mergedList = new ArrayList<>(list.size());
            for (E entity : list) {
                mergedList.add(dataContext.merge(entity, new MergeOptions().setFresh(true)));
            }
            container.setItems(mergedList);
        } else {
            container.setItems(list);
        }

        sendPostLoadEvent(list);

        return true;
    }

    @Override
    public LoadContext<E> createLoadContext() {
        Class<E> entityClass = container.getEntityMetaClass().getJavaClass();

        LoadContext<E> loadContext = new LoadContext<>(container.getEntityMetaClass());

        String queryString = QueryUtils.applyQueryStringProcessors(queryStringProcessors, this.query, entityClass);

        LoadContext.Query query = loadContext.setQueryString(queryString);

        query.setCondition(condition);
        query.setSort(sort);
        query.setParameters(parameters);

        query.setCacheable(cacheable);
        query.setDistinct(canLeadToDuplicateResultsRecursive(condition));

        if (firstResult > 0)
            query.setFirstResult(firstResult);
        if (maxResults < Integer.MAX_VALUE)
            query.setMaxResults(maxResults);

        loadContext.setFetchPlan(resolveFetchPlan());
        loadContext.setHints(hints);

        return loadContext;
    }

    /**
     * Evaluates recursively if the condition depends on some x-to-many property
     * so the list of loaded entities can contain duplicates.
     *
     * @param condition condition to check
     * @return true if duplicate results are possible, false otherwise
     */
    protected boolean canLeadToDuplicateResultsRecursive(Condition condition) {
        if (condition instanceof LogicalCondition) {
            LogicalCondition logicalCondition = (LogicalCondition) condition;
            for (Condition childCondition : logicalCondition.getConditions()) {
                boolean duplicatesPossible = canLeadToDuplicateResultsRecursive(childCondition);
                if (duplicatesPossible) {
                    return true;
                }
            }
            return false;
        } else if (condition instanceof PropertyCondition) {
            PropertyCondition propertyCondition = (PropertyCondition) condition;
            MetaPropertyPath mpp = container.getEntityMetaClass().getPropertyPath(propertyCondition.getProperty());
            if (mpp == null) {
                return false;
            }
            MetaProperty[] metaProperties = mpp.getMetaProperties();
            //length - 1 because no duplicates will be produced if the only x-to-many property is the last one
            for (int i = 0; i < metaProperties.length - 1; i++) {
                if (metaProperties[i].getRange().getCardinality().isMany()) {
                    return true;
                }
            }
            return false;
        } else {
            return false;
        }
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

        Timer.Sample sample = startTimerSample(meterRegistry);

        events.publish(PreLoadEvent.class, preLoadEvent);

        DataLoaderMonitoringInfo info = monitoringInfoProvider.apply(this);
        stopDataLoaderTimerSample(sample, meterRegistry, DataLoaderLifeCycle.PRE_LOAD, info);

        return !preLoadEvent.isLoadPrevented();
    }

    protected void sendPostLoadEvent(List<E> entities) {
        PostLoadEvent<E> postLoadEvent = new PostLoadEvent<>(this, entities);

        Timer.Sample sample = startTimerSample(meterRegistry);

        events.publish(PostLoadEvent.class, postLoadEvent);

        DataLoaderMonitoringInfo info = monitoringInfoProvider.apply(this);
        stopDataLoaderTimerSample(sample, meterRegistry, DataLoaderLifeCycle.POST_LOAD, info);
    }

    @Override
    public CollectionContainer<E> getContainer() {
        return container;
    }

    @Override
    public void setContainer(CollectionContainer<E> container) {
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
    public int getFirstResult() {
        return firstResult;
    }

    @Override
    public void setFirstResult(int firstResult) {
        this.firstResult = firstResult;
    }

    @Override
    public int getMaxResults() {
        return maxResults;
    }

    @Override
    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
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
    public boolean isCacheable() {
        return cacheable;
    }

    @Override
    public void setCacheable(boolean cacheable) {
        this.cacheable = cacheable;
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
    public Function<LoadContext<E>, List<E>> getLoadDelegate() {
        return delegate;
    }

    @Override
    public void setLoadDelegate(Function<LoadContext<E>, List<E>> delegate) {
        this.delegate = delegate;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Subscription addPreLoadListener(Consumer<PreLoadEvent<E>> listener) {
        return events.subscribe(PreLoadEvent.class, (Consumer) listener);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Subscription addPostLoadListener(Consumer<PostLoadEvent<E>> listener) {
        return events.subscribe(PostLoadEvent.class, (Consumer) listener);
    }
}
