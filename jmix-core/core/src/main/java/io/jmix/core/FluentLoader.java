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

import com.google.common.base.Strings;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.constraint.AccessConstraint;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.querycondition.Condition;
import io.jmix.core.querycondition.LogicalCondition;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.persistence.LockModeType;
import javax.persistence.TemporalType;
import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;

@Component("core_FluentLoader")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class FluentLoader<E> {

    private Class<E> entityClass;
    private MetaClass metaClass;

    private UnconstrainedDataManager dataManager;

    private boolean joinTransaction = true;
    private FetchPlan fetchPlan;
    private String fetchPlanName;
    private FetchPlanBuilder fetchPlanBuilder;
    private Map<String, Serializable> hints = new HashMap<>();
    private Collection<AccessConstraint<?>> accessConstraints = new ArrayList<>(0);
    private LockModeType lockMode;

    @Autowired
    private Metadata metadata;

    @Autowired
    private FetchPlanRepository fetchPlanRepository;

    @Autowired
    protected ApplicationContext applicationContext;

    @Autowired
    private ObjectProvider<FetchPlanBuilder> fetchPlanBuilderProvider;

    @Autowired
    private AccessConstraintsRegistry accessConstraintsRegistry;

    public void setDataManager(UnconstrainedDataManager dataManager) {
        this.dataManager = dataManager;
    }

    public FluentLoader(Class<E> entityClass) {
        this.entityClass = entityClass;
    }

    @PostConstruct
    private void init() {
        this.metaClass = metadata.getClass(entityClass);
    }

    LoadContext<E> createLoadContext() {
        MetaClass metaClass = metadata.getClass(entityClass);
        LoadContext<E> loadContext = instantiateLoadContext(metaClass);
        initCommonLoadContextParameters(loadContext);

        String entityName = metaClass.getName();
        String queryString = String.format("select e from %s e", entityName);
        loadContext.setQuery(new LoadContext.Query(queryString));

        return loadContext;
    }

    protected LoadContext<E> instantiateLoadContext(MetaClass metaClass) {
        return new LoadContext<>(metaClass);
    }

    private void initCommonLoadContextParameters(LoadContext<E> loadContext) {
        loadContext.setJoinTransaction(joinTransaction);

        if (fetchPlan != null)
            loadContext.setFetchPlan(fetchPlan);
        else if (!Strings.isNullOrEmpty(fetchPlanName))
            loadContext.setFetchPlan(fetchPlanRepository.getFetchPlan(metadata.getClass(entityClass), fetchPlanName));

        if (fetchPlanBuilder != null) {
            if (fetchPlan != null)
                fetchPlanBuilder.addFetchPlan(fetchPlan);
            else if (!Strings.isNullOrEmpty(fetchPlanName))
                fetchPlanBuilder.addFetchPlan(fetchPlanName);
            loadContext.setFetchPlan(fetchPlanBuilder.build());
        }

        loadContext.setHints(hints);
        loadContext.setAccessConstraints(accessConstraints);
        loadContext.setLockMode(lockMode);
    }

    protected void createFetchPlanBuilder() {
        if (fetchPlanBuilder == null) {
            fetchPlanBuilder = fetchPlanBuilderProvider.getObject(entityClass);
        }
    }

    /**
     * Load by entity identifier.
     */
    public ById<E> id(Object id) {
        return new ById<>(this, id);
    }

    /**
     * Load by array of entity identifiers.
     */
    public ByIds<E> ids(Object... ids) {
        return new ByIds<>(this, Arrays.asList(ids));
    }

    /**
     * Load by collection of entity identifiers.
     */
    public ByIds<E> ids(Collection ids) {
        return new ByIds<>(this, ids);
    }

    /**
     * Load by query.
     */
    public ByQuery<E> query(String queryString) {
        return new ByQuery<>(this, queryString, applicationContext);
    }

    /**
     * Load by query with positional parameters (e.g. {@code "e.name = ?1 and e.status = ?2"}).
     */
    public ByQuery<E> query(String queryString, Object... parameters) {
        return new ByQuery<>(this, queryString, parameters, applicationContext);
    }

    /**
     * Load by condition.
     */
    public ByCondition<E> condition(Condition condition) {
        MetaClass metaClass = metadata.getClass(entityClass);
        return new ByCondition<>(this, metaClass.getName(), condition);
    }

    /**
     * Load all instances.
     */
    public ByCondition<E> all() {
        return condition(LogicalCondition.and());
    }

    public static class ById<E> {

        private FluentLoader<E> loader;
        private Object id;

        protected ById(FluentLoader<E> loader, Object id) {
            this.loader = loader;
            this.id = id;
        }

        LoadContext<E> createLoadContext() {
            LoadContext<E> loadContext = loader.instantiateLoadContext(loader.metaClass).setId(id);
            loader.initCommonLoadContextParameters(loadContext);
            return loadContext;
        }

        /**
         * Loads a single instance and wraps it in Optional.
         */
        public Optional<E> optional() {
            if (id != null) {
                LoadContext<E> loadContext = createLoadContext();
                return Optional.ofNullable(loader.dataManager.load(loadContext));
            } else {
                return Optional.empty();
            }
        }

        /**
         * Loads a single instance.
         *
         * @throws IllegalStateException if nothing was loaded
         */
        public E one() {
            if (id != null) {
                LoadContext<E> loadContext = createLoadContext();
                E entity = loader.dataManager.load(loadContext);
                if (entity != null) {
                    return entity;
                }
            }
            throw new IllegalStateException("No results");
        }

        /**
         * Sets a fetch plan.
         */
        public ById<E> fetchPlan(@Nullable FetchPlan fetchPlan) {
            loader.fetchPlan = fetchPlan;
            return this;
        }

        /**
         * Sets a fetch plan by name.
         */
        public ById<E> fetchPlan(@Nullable String fetchPlanName) {
            loader.fetchPlanName = fetchPlanName;
            return this;
        }

        /**
         * Configure the fetch plan.
         */
        public ById<E> fetchPlan(Consumer<FetchPlanBuilder> fetchPlanBuilderConfigurer) {
            loader.createFetchPlanBuilder();
            fetchPlanBuilderConfigurer.accept(loader.fetchPlanBuilder);
            return this;
        }

        /**
         * Sets a fetch plan containing the given properties. A property can be designated by a path in the entity graph.
         * For example:
         * <pre>
         *     dataManager.load(Pet.class)
         *         .id(petId)
         *         .fetchPlanProperties(
         *                 "name",
         *                 "owner.name",
         *                 "owner.address.city")
         *         .list();
         * </pre>
         */
        public ById<E> fetchPlanProperties(String... properties) {
            loader.createFetchPlanBuilder();
            loader.fetchPlanBuilder.addAll(properties);
            return this;
        }

        /**
         * Adds a custom hint that should be used by the query.
         */
        public ById<E> hint(String hintName, Serializable value) {
            loader.hints.put(hintName, value);
            return this;
        }

        /**
         * Adds custom hints that should be used by the query.
         */
        public ById<E> hints(Map<String, Serializable> hints) {
            loader.hints.putAll(hints);
            return this;
        }

        /**
         * Adds access constraints.
         */
        public ById<E> accessConstraints(Collection<AccessConstraint<?>> accessConstraints) {
            loader.accessConstraints.addAll(accessConstraints);
            return this;
        }

        /**
         * Adds registered access constraints that are subclasses of the given class.
         */
        public ById<E> accessConstraints(Class<? extends AccessConstraint> accessConstraintsClass) {
            loader.accessConstraints.addAll(loader.accessConstraintsRegistry.getConstraintsOfType(accessConstraintsClass));
            return this;
        }

        /**
         * Indicates that the operation must be performed in an existing transaction if it exists. True by default.
         */
        public ById<E> joinTransaction(boolean join) {
            loader.joinTransaction = join;
            return this;
        }

        /**
         * Sets a lock mode to be used when executing query.
         */
        public ById<E> lockMode(LockModeType lockMode) {
            loader.lockMode = lockMode;
            return this;
        }
    }

    public static class ByIds<E> {

        private FluentLoader<E> loader;
        private Collection ids;

        protected ByIds(FluentLoader<E> loader, Collection ids) {
            this.loader = loader;
            this.ids = ids;
        }

        LoadContext<E> createLoadContext() {
            LoadContext<E> loadContext = loader.instantiateLoadContext(loader.metaClass).setIds(ids);
            loader.initCommonLoadContextParameters(loadContext);
            return loadContext;
        }

        /**
         * Loads a list of entities.
         */
        public List<E> list() {
            if (ids != null && !ids.isEmpty()) {
                LoadContext<E> loadContext = createLoadContext();
                return loader.dataManager.loadList(loadContext);
            }
            return Collections.emptyList();
        }

        /**
         * Sets a fetch plan.
         */
        public ByIds<E> fetchPlan(@Nullable FetchPlan fetchPlan) {
            loader.fetchPlan = fetchPlan;
            return this;
        }

        /**
         * Sets a fetch plan by name.
         */
        public ByIds<E> fetchPlan(@Nullable String fetchPlanName) {
            loader.fetchPlanName = fetchPlanName;
            return this;
        }

        /**
         * Sets a fetch plan configured by the {@link FetchPlanBuilder}. For example:
         * <pre>
         *     dataManager.load(Pet.class)
         *         .ids(id1, id2)
         *         .fetchPlan(fetchPlanBuilder -&gt; fetchPlanBuilder.addAll(
         *                 "name",
         *                 "owner.name"))
         *         .list();
         * </pre>
         */
        public ByIds<E> fetchPlan(Consumer<FetchPlanBuilder> fetchPlanBuilderConfigurer) {
            loader.createFetchPlanBuilder();
            fetchPlanBuilderConfigurer.accept(loader.fetchPlanBuilder);
            return this;
        }

        /**
         * Sets a fetch plan containing the given properties. A property can be designated by a path in the entity graph.
         * For example:
         * <pre>
         *     dataManager.load(Pet.class)
         *         .ids(id1, id2)
         *         .fetchPlanProperties(
         *                 "name",
         *                 "owner.name",
         *                 "owner.address.city")
         *         .list();
         * </pre>
         */
        public ByIds<E> fetchPlanProperties(String... properties) {
            loader.createFetchPlanBuilder();
            loader.fetchPlanBuilder.addAll(properties);
            return this;
        }

        /**
         * Adds custom hint that should be used by the query.
         */
        public ByIds<E> hint(String hintName, Serializable value) {
            loader.hints.put(hintName, value);
            return this;
        }

        /**
         * Adds custom hints that should be used by the query.
         */
        public ByIds<E> hints(Map<String, Serializable> hints) {
            loader.hints.putAll(hints);
            return this;
        }

        /**
         * Adds access constraints.
         */
        public ByIds<E> accessConstraints(Collection<AccessConstraint<?>> accessConstraints) {
            loader.accessConstraints.addAll(accessConstraints);
            return this;
        }

        /**
         * Adds registered access constraints that are subclasses of the given class.
         */
        public ByIds<E> accessConstraints(Class<? extends AccessConstraint> accessConstraintsClass) {
            loader.accessConstraints.addAll(loader.accessConstraintsRegistry.getConstraintsOfType(accessConstraintsClass));
            return this;
        }

        /**
         * Indicates that the operation must be performed in an existing transaction if it exists. True by default.
         */
        public ByIds<E> joinTransaction(boolean join) {
            loader.joinTransaction = join;
            return this;
        }

        /**
         * Sets a lock mode to be used when executing query.
         */
        public ByIds<E> lockMode(LockModeType lockMode) {
            loader.lockMode = lockMode;
            return this;
        }
    }

    public static class ByQuery<E> {

        private FluentLoader<E> loader;

        private String queryString;
        private Map<String, Object> parameters = new HashMap<>();
        private int firstResult;
        private int maxResults;
        private Sort sort;
        private boolean cacheable;

        protected ByQuery(FluentLoader<E> loader, String queryString, ApplicationContext applicationContext) {
            Preconditions.checkNotEmptyString(queryString, "queryString is empty");
            this.loader = loader;
            this.queryString = queryString;
        }

        protected ByQuery(FluentLoader<E> loader, String queryString, Object[] positionalParams, ApplicationContext applicationContext) {
            this(loader, queryString, applicationContext);
            processPositionalParams(positionalParams);
        }

        private void processPositionalParams(Object[] positionalParams) {
            if (positionalParams == null) {
                return;
            }
            for (int i = 1; i <= positionalParams.length; i++) {
                String paramName = "_p" + i;
                parameters.put(paramName, positionalParams[i - 1]);
                queryString = queryString.replace("?" + i, ":" + paramName);
            }
        }

        LoadContext<E> createLoadContext() {
            Preconditions.checkNotEmptyString(queryString, "query is empty");

            LoadContext<E> loadContext = loader.instantiateLoadContext(loader.metaClass);
            loader.initCommonLoadContextParameters(loadContext);

            Collection<QueryStringProcessor> processors = loader.applicationContext.getBeansOfType(QueryStringProcessor.class).values();
            String processedQuery = QueryUtils.applyQueryStringProcessors(processors, queryString, loader.entityClass);

            LoadContext.Query query = new LoadContext.Query(processedQuery);
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                query.setParameter(entry.getKey(), entry.getValue());
            }
            loadContext.setQuery(query);

            assert loadContext.getQuery() != null;
            loadContext.getQuery().setFirstResult(firstResult);
            loadContext.getQuery().setMaxResults(maxResults);
            loadContext.getQuery().setSort(sort);
            loadContext.getQuery().setCacheable(cacheable);

            return loadContext;
        }

        /**
         * Loads a list of entities.
         */
        public List<E> list() {
            LoadContext<E> loadContext = createLoadContext();
            return loader.dataManager.loadList(loadContext);
        }

        /**
         * Loads a single instance and wraps it in Optional.
         */
        public Optional<E> optional() {
            LoadContext<E> loadContext = createLoadContext();
            return Optional.ofNullable(loader.dataManager.load(loadContext));
        }

        /**
         * Loads a single instance.
         *
         * @throws IllegalStateException if nothing was loaded
         */
        public E one() {
            LoadContext<E> loadContext = createLoadContext();
            E entity = loader.dataManager.load(loadContext);
            if (entity != null)
                return entity;
            else
                throw new IllegalStateException("No results");
        }

        /**
         * Sets a fetch plan.
         */
        public ByQuery<E> fetchPlan(@Nullable FetchPlan fetchPlan) {
            loader.fetchPlan = fetchPlan;
            return this;
        }

        /**
         * Sets a fetchPlan by name.
         */
        public ByQuery<E> fetchPlan(@Nullable String fetchPlanName) {
            loader.fetchPlanName = fetchPlanName;
            return this;
        }

        /**
         * Configure the fetch plan.
         */
        public ByQuery<E> fetchPlan(Consumer<FetchPlanBuilder> fetchPlanBuilderConfigurer) {
            loader.createFetchPlanBuilder();
            fetchPlanBuilderConfigurer.accept(loader.fetchPlanBuilder);
            return this;
        }

        /**
         * Sets a fetch plan containing the given properties. A property can be designated by a path in the entity graph.
         * For example:
         * <pre>
         *     dataManager.load(Pet.class)
         *         .query("...")
         *         .fetchPlanProperties(
         *                 "name",
         *                 "owner.name",
         *                 "owner.address.city")
         *         .list();
         * </pre>
         */
        public ByQuery<E> fetchPlanProperties(String... properties) {
            loader.createFetchPlanBuilder();
            loader.fetchPlanBuilder.addAll(properties);
            return this;
        }


        /**
         * Adds custom hint that should be used by the query.
         */
        public ByQuery<E> hint(String hintName, Serializable value) {
            loader.hints.put(hintName, value);
            return this;
        }

        /**
         * Adds custom hints that should be used by the query.
         */
        public ByQuery<E> hints(Map<String, Serializable> hints) {
            loader.hints.putAll(hints);
            return this;
        }

        /**
         * Sets additional query condition.
         */
        public ByCondition<E> condition(Condition condition) {
            return new ByCondition<>(this, condition);
        }

        /**
         * Sets value for a query parameter.
         *
         * @param name  parameter name
         * @param value parameter value
         */
        public ByQuery<E> parameter(String name, @Nullable Object value) {
            parameters.put(name, value);
            return this;
        }

        /**
         * Sets value for a parameter of {@code java.util.Date} type.
         *
         * @param name         parameter name
         * @param value        parameter value
         * @param temporalType how to interpret the value
         */
        public ByQuery<E> parameter(String name, @Nullable Date value, TemporalType temporalType) {
            parameters.put(name, value != null ? new TemporalValue(value, temporalType) : null);
            return this;
        }

        /**
         * Adds the given parameters to the map of query parameters.
         */
        public ByQuery<E> parameters(Map<String, Object> parameters) {
            this.parameters.putAll(parameters);
            return this;
        }

        /**
         * Sets results offset.
         */
        public ByQuery<E> firstResult(int firstResult) {
            this.firstResult = firstResult;
            return this;
        }

        /**
         * Sets results limit.
         */
        public ByQuery<E> maxResults(int maxResults) {
            this.maxResults = maxResults;
            return this;
        }

        /**
         * Sets sorting, for example {@code sort(Sort.by("name"))}
         */
        public ByQuery<E> sort(Sort sort) {
            this.sort = sort;
            return this;
        }

        /**
         * Indicates that the query results should be cached.
         * By default, queries are not cached.
         */
        public ByQuery<E> cacheable(boolean cacheable) {
            this.cacheable = cacheable;
            return this;
        }

        /**
         * Adds access constraints.
         */
        public ByQuery<E> accessConstraints(Collection<AccessConstraint<?>> accessConstraints) {
            loader.accessConstraints.addAll(accessConstraints);
            return this;
        }

        /**
         * Adds registered access constraints that are subclasses of the given class.
         */
        public ByQuery<E> accessConstraints(Class<? extends AccessConstraint> accessConstraintsClass) {
            loader.accessConstraints.addAll(loader.accessConstraintsRegistry.getConstraintsOfType(accessConstraintsClass));
            return this;
        }

        /**
         * Indicates that the operation must be performed in an existing transaction if it exists. True by default.
         */
        public ByQuery<E> joinTransaction(boolean join) {
            loader.joinTransaction = join;
            return this;
        }

        /**
         * Sets a lock mode to be used when executing query.
         */
        public ByQuery<E> lockMode(LockModeType lockMode) {
            loader.lockMode = lockMode;
            return this;
        }
    }

    public static class ByCondition<E> {

        private FluentLoader<E> loader;

        private String queryString;
        private Map<String, Object> parameters = new HashMap<>();
        private int firstResult;
        private int maxResults;
        private Sort sort;
        private boolean cacheable;
        private Condition condition;

        protected ByCondition(FluentLoader<E> loader, String entityName, Condition condition) {
            this.loader = loader;
            this.queryString = String.format("select e from %s e", entityName);
            this.condition = condition;
        }

        protected ByCondition(ByQuery<E> byQuery, Condition condition) {
            this.loader = byQuery.loader;
            this.condition = condition;
            this.queryString = byQuery.queryString;
            this.parameters = byQuery.parameters;
            this.firstResult = byQuery.firstResult;
            this.maxResults = byQuery.maxResults;
            this.sort = byQuery.sort;
            this.cacheable = byQuery.cacheable;
        }

        LoadContext<E> createLoadContext() {
            Preconditions.checkNotEmptyString(queryString, "query is empty");

            LoadContext<E> loadContext = loader.instantiateLoadContext(loader.metaClass);
            loader.initCommonLoadContextParameters(loadContext);

            Collection<QueryStringProcessor> processors = loader.applicationContext.getBeansOfType(QueryStringProcessor.class).values();
            String processedQuery = QueryUtils.applyQueryStringProcessors(processors, queryString, loader.entityClass);

            LoadContext.Query query = new LoadContext.Query(processedQuery);
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                query.setParameter(entry.getKey(), entry.getValue());
            }
            loadContext.setQuery(query);

            assert loadContext.getQuery() != null;
            loadContext.getQuery().setCondition(condition);
            loadContext.getQuery().setFirstResult(firstResult);
            loadContext.getQuery().setMaxResults(maxResults);
            loadContext.getQuery().setSort(sort);
            loadContext.getQuery().setCacheable(cacheable);

            return loadContext;
        }

        /**
         * Loads a list of entities.
         */
        public List<E> list() {
            LoadContext<E> loadContext = createLoadContext();
            return loader.dataManager.loadList(loadContext);
        }

        /**
         * Loads a single instance and wraps it in Optional.
         */
        public Optional<E> optional() {
            LoadContext<E> loadContext = createLoadContext();
            return Optional.ofNullable(loader.dataManager.load(loadContext));
        }

        /**
         * Loads a single instance.
         *
         * @throws IllegalStateException if nothing was loaded
         */
        public E one() {
            LoadContext<E> loadContext = createLoadContext();
            E entity = loader.dataManager.load(loadContext);
            if (entity != null)
                return entity;
            else
                throw new IllegalStateException("No results");
        }

        /**
         * Sets a fetch plan.
         */
        public ByCondition<E> fetchPlan(@Nullable FetchPlan fetchPlan) {
            loader.fetchPlan = fetchPlan;
            return this;
        }

        /**
         * Sets a fetchPlan by name.
         */
        public ByCondition<E> fetchPlan(@Nullable String fetchPlanName) {
            loader.fetchPlanName = fetchPlanName;
            return this;
        }

        /**
         * Configure the fetch plan.
         */
        public ByCondition<E> fetchPlan(Consumer<FetchPlanBuilder> fetchPlanBuilderConfigurer) {
            loader.createFetchPlanBuilder();
            fetchPlanBuilderConfigurer.accept(loader.fetchPlanBuilder);
            return this;
        }

        /**
         * Sets a fetch plan containing the given properties. A property can be designated by a path in the entity graph.
         * For example:
         * <pre>
         *     dataManager.load(Pet.class)
         *         .condition(...)
         *         .fetchPlanProperties(
         *                 "name",
         *                 "owner.name",
         *                 "owner.address.city")
         *         .list();
         * </pre>
         */
        public ByCondition<E> fetchPlanProperties(String... properties) {
            loader.createFetchPlanBuilder();
            loader.fetchPlanBuilder.addAll(properties);
            return this;
        }

        /**
         * Adds custom hint that should be used by the query.
         */
        public ByCondition<E> hint(String hintName, Serializable value) {
            loader.hints.put(hintName, value);
            return this;
        }

        /**
         * Adds custom hints that should be used by the query.
         */
        public ByCondition<E> hints(Map<String, Serializable> hints) {
            loader.hints.putAll(hints);
            return this;
        }

        /**
         * Sets value for a query parameter.
         *
         * @param name  parameter name
         * @param value parameter value
         */
        public ByCondition<E> parameter(String name, @Nullable Object value) {
            parameters.put(name, value);
            return this;
        }

        /**
         * Sets value for a parameter of {@code java.util.Date} type.
         *
         * @param name         parameter name
         * @param value        parameter value
         * @param temporalType how to interpret the value
         */
        public ByCondition<E> parameter(String name, @Nullable Date value, TemporalType temporalType) {
            parameters.put(name, value != null ? new TemporalValue(value, temporalType) : null);
            return this;
        }

        /**
         * Adds the given parameters to the map of query parameters.
         */
        public ByCondition<E> parameters(Map<String, Object> parameters) {
            this.parameters.putAll(parameters);
            return this;
        }

        /**
         * Sets results offset.
         */
        public ByCondition<E> firstResult(int firstResult) {
            this.firstResult = firstResult;
            return this;
        }

        /**
         * Sets results limit.
         */
        public ByCondition<E> maxResults(int maxResults) {
            this.maxResults = maxResults;
            return this;
        }

        /**
         * Sets sorting, for example {@code sort(Sort.by("name"))}
         */
        public ByCondition<E> sort(Sort sort) {
            this.sort = sort;
            return this;
        }

        /**
         * Indicates that the query results should be cached.
         * By default, queries are not cached.
         */
        public ByCondition<E> cacheable(boolean cacheable) {
            this.cacheable = cacheable;
            return this;
        }

        /**
         * Adds access constraints.
         */
        public ByCondition<E> accessConstraints(Collection<AccessConstraint<?>> accessConstraints) {
            loader.accessConstraints.addAll(accessConstraints);
            return this;
        }

        /**
         * Adds registered access constraints that are subclasses of the given class.
         */
        public ByCondition<E> accessConstraints(Class<? extends AccessConstraint> accessConstraintsClass) {
            loader.accessConstraints.addAll(loader.accessConstraintsRegistry.getConstraintsOfType(accessConstraintsClass));
            return this;
        }

        /**
         * Indicates that the operation must be performed in an existing transaction if it exists. True by default.
         */
        public ByCondition<E> joinTransaction(boolean join) {
            loader.joinTransaction = join;
            return this;
        }

        /**
         * Sets a lock mode to be used when executing query.
         */
        public ByCondition<E> lockMode(LockModeType lockMode) {
            loader.lockMode = lockMode;
            return this;
        }
    }
}
