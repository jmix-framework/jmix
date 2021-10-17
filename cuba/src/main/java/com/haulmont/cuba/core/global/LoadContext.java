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
package com.haulmont.cuba.core.global;


import io.jmix.core.*;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.querycondition.Condition;
import io.jmix.data.PersistenceHints;
import io.jmix.dynattr.DynAttrQueryHints;

import javax.persistence.TemporalType;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Class that defines parameters for loading entities from the database via {@link DataManager}.
 * <p>Typical usage:
 * <pre>
 * LoadContext&lt;User&gt; context = LoadContext.create(User.class).setQuery(
 * LoadContext.createQuery("select u from sec$User u where u.login like :login")
 * .setParameter("login", "a%")
 * .setMaxResults(10))
 * .setView("user.browse");
 * List&lt;User&gt; users = dataManager.loadList(context);
 * </pre>
 * <p>
 * Instead of using this class directly, consider fluent interface with the entry point in {@link DataManager#load(Class)}.
 *
 * @deprecated use only in legacy CUBA code. In new code, use {@link io.jmix.core.LoadContext}.
 */
@Deprecated
public class LoadContext<E extends Entity> extends io.jmix.core.LoadContext<E> {

    private static final long serialVersionUID = 6022448642011883513L;

    protected List<Query> prevQueries = new ArrayList<>(0);

    /**
     * Factory method to create a LoadContext instance.
     *
     * @param entityClass class of the loaded entities
     */
    public static <E extends Entity> LoadContext<E> create(Class<E> entityClass) {
        return new LoadContext<>(entityClass);
    }

    /**
     * Factory method to create a LoadContext.Query instance for passing into {@link #setQuery(Query)} method.
     *
     * @param queryString JPQL query string. Only named parameters are supported.
     */
    public static LoadContext.Query createQuery(String queryString) {
        return new LoadContext.Query(queryString);
    }

    /**
     * @param metaClass metaclass of the loaded entities
     */
    public LoadContext(MetaClass metaClass) {
        this();
        Preconditions.checkNotNullArgument(metaClass, "metaClass is null");
        this.metaClass = metaClass;
    }

    /**
     * @param javaClass class of the loaded entities
     */
    public LoadContext(Class<E> javaClass) {
        this();
        Preconditions.checkNotNullArgument(javaClass, "javaClass is null");
        this.metaClass = AppBeans.get(Metadata.class).getClassNN(javaClass);
    }

    protected LoadContext() {
        joinTransaction = false;
    }

    /**
     * @return query definition
     */
    public Query getQuery() {
        return (Query) query;
    }

    /**
     * @param query query definition
     * @return this instance for chaining
     */
    public LoadContext<E> setQuery(Query query) {
        this.query = query;
        return this;
    }

    /**
     * @param queryString JPQL query string. Only named parameters are supported.
     * @return query definition object
     */
    @Override
    public Query setQueryString(String queryString) {
        final Query query = new Query(queryString);
        setQuery(query);
        return query;
    }

    /**
     * @return view that is used for loading entities
     */
    public FetchPlan getView() {
        return fetchPlan;
    }

    /**
     * @param fetchPlan view that is used for loading entities
     * @return this instance for chaining
     */
    public LoadContext<E> setView(FetchPlan fetchPlan) {
        this.fetchPlan = fetchPlan;
        return this;
    }

    /**
     * @deprecated replaced by {@link LoadContext#setFetchPlan(String)}
     */
    @Deprecated
    public LoadContext<E> setView(String fetchPlanName) {
        return setFetchPlan(fetchPlanName);
    }

    /**
     * @param fetchPlanName fetch plan that is used for loading entities
     * @return this instance for chaining
     */
    public LoadContext<E> setFetchPlan(String fetchPlanName) {
        FetchPlanRepository fetchPlanRepository = AppBeans.get(FetchPlanRepository.class);
        this.fetchPlan = fetchPlanRepository.getFetchPlan(metaClass, fetchPlanName);
        return this;
    }

    /**
     * Allows to execute query on a previous query result.
     *
     * @return editable list of previous queries
     */
    public List<Query> getPrevQueries() {
        return prevQueries;
    }

    @Override
    public LoadContext<E> setQuery(io.jmix.core.LoadContext.Query query) {
        Query cubaQuery = new Query();
        if (query != null) {
            query.copyStateTo(cubaQuery);
        }
        super.setQuery(cubaQuery);
        return this;
    }

    @Override
    public LoadContext<E> setFetchPlan(FetchPlan fetchPlan) {
        super.setFetchPlan(fetchPlan);
        return this;
    }

    @Override
    public LoadContext<E> setId(Object id) {
        super.setId(id);
        return this;
    }

    @Override
    public LoadContext<E> setIds(Collection<?> ids) {
        super.setIds(ids);
        return this;
    }

    /**
     * @param softDeletion whether to use soft deletion when loading entities
     */
    public LoadContext<E> setSoftDeletion(boolean softDeletion) {
        super.setHint(PersistenceHints.SOFT_DELETION, softDeletion);
        return this;
    }

    /**
     * @return whether to use soft deletion when loading entities
     */
    public boolean isSoftDeletion() {
        Object value = getHints().get(PersistenceHints.SOFT_DELETION);
        return value == null || Boolean.TRUE.equals(value);
    }

    @Override
    public LoadContext<E> setQueryKey(int queryKey) {
        super.setQueryKey(queryKey);
        return this;
    }

    /**
     * Returns true if the entity's dynamic attributes are loaded.
     */
    public boolean isLoadDynamicAttributes() {
        return hints != null && Boolean.TRUE.equals(hints.get(DynAttrQueryHints.LOAD_DYN_ATTR));
    }

    /**
     * Set to true to load the entity's dynamic attributes. Dynamic attributes are not loaded by default.
     */
    public LoadContext<E> setLoadDynamicAttributes(boolean loadDynamicAttributes) {
        super.setHint(DynAttrQueryHints.LOAD_DYN_ATTR, loadDynamicAttributes);
        return this;
    }

    @Override
    public LoadContext<E> setLoadPartialEntities(boolean loadPartialEntities) {
        super.setLoadPartialEntities(loadPartialEntities);
        return this;
    }

    public LoadContext<E> setAuthorizationRequired(boolean authorizationRequired) {
        if (authorizationRequired) {
            setAccessConstraints(AppBeans.get(AccessConstraintsRegistry.class).getConstraints());
        } else {
            setAccessConstraints(Collections.emptyList());
        }
        return this;
    }

    @Override
    public LoadContext<E> setJoinTransaction(boolean joinTransaction) {
        super.setJoinTransaction(joinTransaction);
        return this;
    }

    /**
     * Creates a copy of this LoadContext instance.
     */
    public LoadContext<?> copy() {
        LoadContext<?> ctx;
        try {
            ctx = getClass().newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Error copying LoadContext", e);
        }
        ctx.metaClass = metaClass;
        ctx.setQuery(query != null ? query.copy() : null);
        ctx.fetchPlan = fetchPlan;
        ctx.id = id;
        ctx.prevQueries.addAll(prevQueries.stream().map(Query::copy).collect(Collectors.toList()));
        ctx.queryKey = queryKey;
        if (hints != null) {
            ctx.getHints().putAll(hints);
        }
        ctx.joinTransaction = joinTransaction;
        return ctx;
    }

    @Override
    public String toString() {
        return String.format(
                "LoadContext{metaClass=%s, query=%s, fetchPlan=%s, id=%s, partialEntities=%s}",
                metaClass, query, fetchPlan, id, loadPartialEntities
        );
    }

    /**
     * Class that defines a query to be executed for data loading.
     */
    public static class Query extends io.jmix.core.LoadContext.Query {

        private static final long serialVersionUID = 8430057828711384548L;

        public Query(String queryString) {
            super(queryString);
        }

        protected Query() {
            super();
        }

        @Override
        public Query copy() {
            Query newQuery = new Query();
            copyStateTo(newQuery);
            return newQuery;
        }

        @Override
        public LoadContext.Query setQueryString(String queryString) {
            super.setQueryString(queryString);
            return this;
        }

        @Override
        public LoadContext.Query setParameter(String name, Object value) {
            super.setParameter(name, value);
            return this;
        }

        @Override
        public LoadContext.Query setParameter(String name, Date value, TemporalType temporalType) {
            super.setParameter(name, value, temporalType);
            return this;
        }

        @Override
        public LoadContext.Query setParameters(Map<String, Object> parameters) {
            super.setParameters(parameters);
            return this;
        }

        @Override
        public LoadContext.Query setFirstResult(int firstResult) {
            super.setFirstResult(firstResult);
            return this;
        }

        @Override
        public LoadContext.Query setMaxResults(int maxResults) {
            super.setMaxResults(maxResults);
            return this;
        }

        @Override
        public LoadContext.Query setCondition(Condition condition) {
            super.setCondition(condition);
            return this;
        }

        @Override
        public LoadContext.Query setSort(Sort sort) {
            super.setSort(sort);
            return this;
        }

        @Override
        public LoadContext.Query setCacheable(boolean cacheable) {
            super.setCacheable(cacheable);
            return this;
        }
    }
}
