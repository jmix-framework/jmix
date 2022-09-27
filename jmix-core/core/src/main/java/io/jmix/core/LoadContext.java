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
package io.jmix.core;


import io.jmix.core.common.util.Preconditions;
import io.jmix.core.common.util.StringHelper;
import io.jmix.core.constraint.AccessConstraint;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.querycondition.Condition;

import javax.annotation.Nullable;
import javax.persistence.LockModeType;
import javax.persistence.TemporalType;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Class that defines parameters for loading entities from the database via {@link DataManager}.
 * <p>Typical usage:
 * <pre>
 * LoadContext&lt;User&gt; context = new LoadContext(userMetaClass).setQuery(
 * new LoadContext.Query("select u from sec$User u where u.login like :login")
 * .setParameter("login", "a%")
 * .setMaxResults(10))
 * .setFetchPlan("user.browse");
 * List&lt;User&gt; users = dataManager.loadList(context);
 * </pre>
 * <p>
 * Instead of using this class directly, consider fluent interface with the entry point in {@link DataManager#load(Class)}.
 */
public class LoadContext<E> implements DataLoadContext, Serializable {

    private static final long serialVersionUID = -3406772812465222907L;

    protected MetaClass metaClass;
    protected Query query;
    protected FetchPlan fetchPlan;
    protected Object id;
    protected List<Object> idList = new ArrayList<>(0);
    protected List<Query> prevQueries = new ArrayList<>(0);
    protected List<AccessConstraint<?>> accessConstraints = new ArrayList<>();
    protected int queryKey;

    protected boolean loadPartialEntities = true;
    protected boolean joinTransaction = true;

    protected Map<String, Serializable> hints; // lazy initialized map
    protected LockModeType lockMode;

    /**
     * @param metaClass metaclass of the loaded entities
     */
    public LoadContext(MetaClass metaClass) {
        Preconditions.checkNotNullArgument(metaClass, "metaClass is null");
        this.metaClass = metaClass;
    }

    protected LoadContext() {
    }

    /**
     * @return metaclass of the loaded entities
     */
    public MetaClass getEntityMetaClass() {
        return metaClass;
    }

    /**
     * @return query definition
     */
    @Nullable
    public Query getQuery() {
        return query;
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
     * @param queryString query string. Only named parameters are supported.
     * @return query definition object
     */
    @Override
    public Query setQueryString(String queryString) {
        final Query query = new Query(queryString);
        setQuery(query);
        return query;
    }

    /**
     * @param lockMode lock mode to be used when executing query
     */
    @Override
    public void setLockMode(LockModeType lockMode) {
        this.lockMode = lockMode;
    }

    /**
     * @return lock mode to be used when executing query
     */
    @Override
    @Nullable
    public LockModeType getLockMode() {
        return lockMode;
    }

    /**
     * @return fetchPlan that is used for loading entities
     */
    @Nullable
    public FetchPlan getFetchPlan() {
        return fetchPlan;
    }

    /**
     * @param fetchPlan fetchPlan that is used for loading entities
     * @return this instance for chaining
     */
    public LoadContext<E> setFetchPlan(FetchPlan fetchPlan) {
        this.fetchPlan = fetchPlan;
        return this;
    }

    /**
     * @return id of an entity to be loaded
     */
    @Nullable
    public Object getId() {
        return id;
    }

    /**
     * @param id id of an entity to be loaded
     * @return this instance for chaining
     */
    public LoadContext<E> setId(Object id) {
        this.id = id instanceof Entity ? EntityValues.getId(id) : id; // for compatibility with legacy code relying on implicit conversions
        return this;
    }

    /**
     * @return identifiers of entities to be loaded
     */
    public List<?> getIds() {
        return idList;
    }

    /**
     * @param ids identifiers of entities to be loaded
     * @return this instance for chaining
     */
    public LoadContext<E> setIds(Collection<?> ids) {
        this.idList.addAll(ids);
        return this;
    }

    /**
     * Allows to execute query on a previous query result.
     *
     * @return editable list of previous queries
     */
    public List<Query> getPreviousQueries() {
        return prevQueries;
    }

    /**
     * @return key of the current stack of sequential queries, which is unique for the current user session
     */
    public int getQueryKey() {
        return queryKey;
    }

    /**
     * @param queryKey key of the current stack of sequential queries, which is unique for the current user session
     */
    public LoadContext<E> setQueryKey(int queryKey) {
        this.queryKey = queryKey;
        return this;
    }

    /**
     * @return custom hints which are used by the query
     */
    public Map<String, Object> getHints() {
        return hints == null ? Collections.emptyMap() : Collections.unmodifiableMap(hints);
    }

    /**
     * Sets custom hint that should be used by the query.
     */
    public LoadContext<E> setHint(String hintName, Serializable value) {
        if (hints == null) {
            hints = new HashMap<>();
        }
        hints.put(hintName, value);
        return this;
    }

    /**
     * Sets custom hints that should be used by the query.
     */
    public LoadContext<E> setHints(Map<String, Serializable> hints) {
        this.hints = hints;
        return this;
    }

    /**
     * @return whether to load partial entities. When true (which is by default), some local attributes can be unfetched
     * according to {@link #setFetchPlan(FetchPlan)}.
     * <p>The state of {@link FetchPlan#loadPartialEntities()} is ignored when the fetchPlan is passed to {@link DataManager}.
     */
    public boolean isLoadPartialEntities() {
        return loadPartialEntities;
    }

    /**
     * Whether to load partial entities. When true (which is by default), some local attributes can be unfetched
     * according to {@link #setFetchPlan(FetchPlan)}.
     * <p>The state of {@link FetchPlan#loadPartialEntities()} is ignored when the fetchPlan is passed to {@link DataManager}.
     */
    public LoadContext<E> setLoadPartialEntities(boolean loadPartialEntities) {
        this.loadPartialEntities = loadPartialEntities;
        return this;
    }

    /**
     * Returns directly accessible list of access constraints.
     */
    public List<AccessConstraint<?>> getAccessConstraints() {
        return accessConstraints;
    }

    /**
     * Sets list of access constraints.
     */
    public LoadContext<E> setAccessConstraints(@Nullable Collection<AccessConstraint<?>> accessConstraints) {
        this.accessConstraints.clear();
        if (accessConstraints != null) {
            this.accessConstraints.addAll(accessConstraints);
        }
        return this;
    }

    /**
     * @return whether to join existing transaction or always start a new one
     */
    public boolean isJoinTransaction() {
        return joinTransaction;
    }

    /**
     * Sets whether to join existing transaction or always start a new one.
     */
    public LoadContext<E> setJoinTransaction(boolean joinTransaction) {
        this.joinTransaction = joinTransaction;
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
        ctx.query = query != null ? query.copy() : null;
        ctx.fetchPlan = fetchPlan;
        ctx.id = id;
        ctx.prevQueries.addAll(prevQueries.stream().map(Query::copy).collect(Collectors.toList()));
        ctx.queryKey = queryKey;
        if (hints != null) {
            ctx.setHints(new HashMap<>(hints));
        }
        ctx.accessConstraints.addAll(accessConstraints);
        ctx.joinTransaction = joinTransaction;
        ctx.lockMode = lockMode;
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
    public static class Query implements DataLoadContextQuery, Serializable {

        private static final long serialVersionUID = -5803374131930436223L;

        private Map<String, Object> parameters = new HashMap<>();
        private String queryString;
        private int firstResult;
        private int maxResults;
        private boolean cacheable;
        private Condition condition;
        private Sort sort;

        protected Query() {
        }

        /**
         * @param queryString JPQL query string. Only named parameters are supported.
         */
        public Query(String queryString) {
            this.queryString = queryString;
        }

        /**
         * @return JPQL query string
         */
        public String getQueryString() {
            return queryString;
        }

        /**
         * @param queryString JPQL query string. Only named parameters are supported.
         */
        public Query setQueryString(String queryString) {
            this.queryString = queryString;
            return this;
        }

        /**
         * Set value for a query parameter.
         *
         * @param name  parameter name
         * @param value parameter value
         * @return this query instance for chaining
         */
        public Query setParameter(String name, Object value) {
            parameters.put(name, value);
            return this;
        }

        /**
         * Set value for a parameter of java.util.Date type.
         *
         * @param name         parameter name
         * @param value        date value
         * @param temporalType temporal type
         * @return this query instance for chaining
         */
        public Query setParameter(String name, Date value, TemporalType temporalType) {
            parameters.put(name, new TemporalValue(value, temporalType));
            return this;
        }

        /**
         * @return editable map of the query parameters
         */
        public Map<String, Object> getParameters() {
            return parameters;
        }

        /**
         * @param parameters map of the query parameters
         */
        public Query setParameters(Map<String, Object> parameters) {
            this.parameters.putAll(parameters);
            return this;
        }

        /**
         * @param firstResult results offset
         * @return this query instance for chaining
         */
        public Query setFirstResult(int firstResult) {
            this.firstResult = firstResult;
            return this;
        }

        /**
         * @param maxResults results limit
         * @return this query instance for chaining
         */
        public Query setMaxResults(int maxResults) {
            this.maxResults = maxResults;
            return this;
        }

        /**
         * @return root query condition
         */
        public Condition getCondition() {
            return condition;
        }

        /**
         * @param condition root query condition
         * @return this query instance for chaining
         */
        public Query setCondition(Condition condition) {
            this.condition = condition;
            return this;
        }

        /**
         * @return query sort
         */
        public Sort getSort() {
            return sort;
        }

        /**
         * @param sort query sort
         * @return this query instance for chaining
         */
        public Query setSort(Sort sort) {
            this.sort = sort;
            return this;
        }

        /**
         * Indicates that the query results should be cached.
         *
         * @return the same query instance
         */
        public Query setCacheable(boolean cacheable) {
            this.cacheable = cacheable;
            return this;
        }

        /**
         * @return results offset
         */
        public int getFirstResult() {
            return firstResult;
        }

        /**
         * @return results limit
         */
        public int getMaxResults() {
            return maxResults;
        }


        public boolean isCacheable() {
            return cacheable;
        }

        /**
         * Creates a copy of this Query instance.
         */
        public Query copy() {
            Query newQuery = new Query();
            copyStateTo(newQuery);
            return newQuery;
        }

        public void copyStateTo(Query query) {
            query.queryString = this.queryString;
            query.parameters.putAll(this.parameters);
            query.firstResult = this.firstResult;
            query.maxResults = this.maxResults;
            query.cacheable = this.cacheable;
            query.condition = this.condition == null ? null : this.condition.copy();
            query.sort = this.sort;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Query query = (Query) o;

            if (firstResult != query.firstResult) return false;
            if (maxResults != query.maxResults) return false;
            if (!parameters.equals(query.parameters)) return false;
            return queryString.equals(query.queryString);
        }

        @Override
        public int hashCode() {
            int result = parameters.hashCode();
            result = 31 * result + queryString.hashCode();
            result = 31 * result + firstResult;
            result = 31 * result + maxResults;
            return result;
        }

        @Override
        public String toString() {
            String stringResult = "{" +
                    "'" + queryString + '\'' +
                    ", condition=" + condition +
                    ", sort=" + sort +
                    ", firstResult=" + firstResult +
                    ", maxResults=" + maxResults +
                    "}";
            return StringHelper.removeExtraSpaces(stringResult.replace('\n', ' '));
        }
    }
}
