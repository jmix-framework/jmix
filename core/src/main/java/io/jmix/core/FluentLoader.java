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
import io.jmix.core.commons.util.Preconditions;
import io.jmix.core.Entity;
import io.jmix.core.queryconditions.Condition;

import javax.persistence.TemporalType;
import java.util.*;
import java.util.function.Consumer;

public class FluentLoader<E extends Entity<K>, K> {

    private Class<E> entityClass;

    private DataManager dataManager;

    private boolean joinTransaction = true;
    private FetchPlan fetchPlan;
    private String fetchPlanName;
    private FetchPlanBuilder fetchPlanBuilder;
    private boolean softDeletion = true;
    private boolean dynamicAttributes;

    private Metadata metadata;
    private FetchPlanRepository fetchPlanRepository;

    public FluentLoader(Class<E> entityClass, DataManager dataManager) {
        this.entityClass = entityClass;
        this.dataManager = dataManager;

        metadata = AppBeans.get(Metadata.class);
        fetchPlanRepository = AppBeans.get(FetchPlanRepository.class);
    }

    LoadContext<E> createLoadContext() {
        LoadContext<E> loadContext = new LoadContext<>(entityClass);
        initCommonLoadContextParameters(loadContext);

        String entityName = metadata.getClass(entityClass).getName();
        String queryString = String.format("select e from %s e", entityName);
        loadContext.setQuery(new LoadContext.Query(queryString));

        return loadContext;
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

        loadContext.setSoftDeletion(softDeletion);
        loadContext.setLoadDynamicAttributes(dynamicAttributes);
    }

    private void createFetchPlanBuilder() {
        if (fetchPlanBuilder == null) {
            fetchPlanBuilder = AppBeans.getPrototype(FetchPlanBuilder.NAME, entityClass);
        }
    }

    /**
     * Loads a list of entities.
     */
    public List<E> list() {
        LoadContext<E> loadContext = createLoadContext();
        return dataManager.loadList(loadContext);
    }

    /**
     * Loads a single instance and wraps it in Optional.
     */
    public Optional<E> optional() {
        LoadContext<E> loadContext = createLoadContext();
        loadContext.getQuery().setMaxResults(1);
        return Optional.ofNullable(dataManager.load(loadContext));
    }

    /**
     * Loads a single instance.
     *
     * @throws IllegalStateException if nothing was loaded
     */
    public E one() {
        LoadContext<E> loadContext = createLoadContext();
        loadContext.getQuery().setMaxResults(1);
        E entity = dataManager.load(loadContext);
        if (entity != null)
            return entity;
        else
            throw new IllegalStateException("No results");
    }

    public FluentLoader<E, K> joinTransaction(boolean join) {
        this.joinTransaction = join;
        return this;
    }

    /**
     * Sets a view.
     * @deprecated replaced by {@link FluentLoader#fetchPlan(FetchPlan)}
     */
    @Deprecated
    public FluentLoader<E, K> view(FetchPlan view) {
        return fetchPlan(view);
    }

    /**
     * Sets a fetch plan.
     */
    public FluentLoader<E, K> fetchPlan(FetchPlan fetchPlan) {
        this.fetchPlan = fetchPlan;
        return this;
    }

    /**
     * Sets a view by name.
     * @deprecated replaced by {@link FluentLoader#fetchPlan(String)}
     */
    @Deprecated
    public FluentLoader<E, K> view(String viewName) {
        return fetchPlan(viewName);
    }

    /**
     * Sets a fetch plan by name.
     */
    public FluentLoader<E, K> fetchPlan(String fetchPlanName) {
        this.fetchPlanName = fetchPlanName;
        return this;
    }

    /**
     *
     * @deprecated replaced by {@link FluentLoader#fetchPlan(Consumer)}
     */
    @Deprecated
    public FluentLoader<E, K> view(Consumer<FetchPlanBuilder> fetchPlanBuilderConfigurer) {
        return fetchPlan(fetchPlanBuilderConfigurer);
    }

    public FluentLoader<E, K> fetchPlan(Consumer<FetchPlanBuilder> fetchPlanBuilderConfigurer) {
        createFetchPlanBuilder();
        fetchPlanBuilderConfigurer.accept(fetchPlanBuilder);
        return this;
    }

    /**
     *
     * @deprecated replaced by {@link FluentLoader#fetchPlanProperties(String...)}
     */
    @Deprecated
    public FluentLoader<E, K> viewProperties(String... properties) {
        return fetchPlanProperties(properties);
    }

    public FluentLoader<E, K> fetchPlanProperties(String... properties) {
        createFetchPlanBuilder();
        fetchPlanBuilder.addAll(properties);
        return this;
    }

    /**
     * Sets soft deletion. The soft deletion is true by default.
     */
    public FluentLoader<E, K> softDeletion(boolean softDeletion) {
        this.softDeletion = softDeletion;
        return this;
    }

    /**
     * Sets loading of dynamic attributes. It is false by default.
     */
    public FluentLoader<E, K> dynamicAttributes(boolean dynamicAttributes) {
        this.dynamicAttributes = dynamicAttributes;
        return this;
    }

    /**
     * Sets the entity identifier.
     */
    public ById<E, K> id(K id) {
        return new ById<>(this, id);
    }

    /**
     * Sets array of entity identifiers.
     */
    @SafeVarargs
    public final ByIds<E, K> ids(K... ids) {
        return new ByIds<>(this, Arrays.asList(ids));
    }

    /**
     * Sets collection of entity identifiers.
     */
    public ByIds<E, K> ids(Collection<K> ids) {
        return new ByIds<>(this, ids);
    }

    /**
     * Sets the query text.
     */
    public ByQuery<E, K> query(String queryString) {
        return new ByQuery<>(this, queryString);
    }

    /**
     * Sets the query with positional parameters (e.g. {@code "e.name = ?1 and e.status = ?2"}).
     */
    public ByQuery<E, K> query(String queryString, Object... parameters) {
        return new ByQuery<>(this, queryString, parameters);
    }

    public static class ById<E extends Entity<K>, K> {

        private FluentLoader<E, K> loader;
        private K id;

        ById(FluentLoader<E, K> loader, K id) {
            this.loader = loader;
            this.id = id;
        }

        LoadContext<E> createLoadContext() {
            LoadContext<E> loadContext = new LoadContext(loader.entityClass).setId(id);
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
         * Sets a view.
         * @deprecated replaced by {@link ById#fetchPlan(FetchPlan)}
         */
        @Deprecated
        public ById<E, K> view(FetchPlan view) {
            return fetchPlan(view);
        }

        /**
         * Sets a fetch plan.
         */
        public ById<E, K> fetchPlan(FetchPlan fetchPlan) {
            loader.fetchPlan = fetchPlan;
            return this;
        }

        /**
         * Sets a view by name.
         * @deprecated replaced by {@link ById#fetchPlan(String)}
         */
        @Deprecated
        public ById<E, K> view(String viewName) {
            return fetchPlan(viewName);
        }

        /**
         * Sets a fetch plan by name.
         */
        public ById<E, K> fetchPlan(String viewName) {
            loader.fetchPlanName = viewName;
            return this;
        }

        /**
         * @deprecated replaced by {@link ById#fetchPlan(Consumer)} )}
         */
        @Deprecated
        public ById<E, K> view(Consumer<FetchPlanBuilder> viewBuilderConfigurer) {
            return fetchPlan(viewBuilderConfigurer);
        }

        public ById<E, K> fetchPlan(Consumer<FetchPlanBuilder> fetchPlanBuilderConfigurer) {
            loader.createFetchPlanBuilder();
            fetchPlanBuilderConfigurer.accept(loader.fetchPlanBuilder);
            return this;
        }

        /**
         * @deprecated replaced by {@link ById#fetchPlanProperties(String...)}
         */
        @Deprecated
        public ById<E, K> viewProperties(String... properties) {
            return fetchPlanProperties(properties);
        }

        public ById<E, K> fetchPlanProperties(String... properties) {
            loader.createFetchPlanBuilder();
            loader.fetchPlanBuilder.addAll(properties);
            return this;
        }

        /**
         * Sets soft deletion. The soft deletion is true by default.
         */
        public ById<E, K> softDeletion(boolean softDeletion) {
            loader.softDeletion = softDeletion;
            return this;
        }

        /**
         * Sets loading of dynamic attributes. It is false by default.
         */
        public ById<E, K> dynamicAttributes(boolean dynamicAttributes) {
            loader.dynamicAttributes = dynamicAttributes;
            return this;
        }
    }

    public static class ByIds<E extends Entity<K>, K> {

        private FluentLoader<E, K> loader;
        private Collection<K> ids;

        ByIds(FluentLoader<E, K> loader, Collection<K> ids) {
            this.loader = loader;
            this.ids = ids;
        }

        LoadContext<E> createLoadContext() {
            LoadContext<E> loadContext = new LoadContext(loader.entityClass).setIds(ids);
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
         * Sets a view.
         * @deprecated replaced by {@link ByIds#fetchPlan(FetchPlan)}
         */
        @Deprecated
        public ByIds<E, K> view(FetchPlan view) {
            return fetchPlan(view);
        }

        /**
         * Sets a fetch plan.
         */
        public ByIds<E, K> fetchPlan(FetchPlan fetchPlan) {
            loader.fetchPlan = fetchPlan;
            return this;
        }

        /**
         * Sets a view by name.
         * @deprecated replaced by {@link ByIds#fetchPlan(String)}
         */
        @Deprecated
        public ByIds<E, K> view(String viewName) {
            return fetchPlan(viewName);
        }

        /**
         * Sets a fetch plan by name.
         */
        public ByIds<E, K> fetchPlan(String fetchPlanName) {
            loader.fetchPlanName = fetchPlanName;
            return this;
        }

        /**
         * Sets a view configured by the {@link FetchPlanBuilder}. For example:
         * <pre>
         *     dataManager.load(Pet.class)
         *         .ids(id1, id2)
         *         .view(viewBuilder -&gt; viewBuilder.addAll(
         *                 "name",
         *                 "owner.name"))
         *         .list();
         * </pre>
         * @deprecated replaced by {@link ByIds#fetchPlan(Consumer)} )}
         */
        @Deprecated
        public ByIds<E, K> view(Consumer<FetchPlanBuilder> viewBuilderConfigurer) {
            return fetchPlan(viewBuilderConfigurer);
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
        public ByIds<E, K> fetchPlan(Consumer<FetchPlanBuilder> fetchPlanBuilderConfigurer) {
            loader.createFetchPlanBuilder();
            fetchPlanBuilderConfigurer.accept(loader.fetchPlanBuilder);
            return this;
        }

        /**
         * Sets a view containing the given properties. A property can be designated by a path in the entity graph.
         * For example:
         * <pre>
         *     dataManager.load(Pet.class)
         *         .ids(id1, id2)
         *         .viewProperties(
         *                 "name",
         *                 "owner.name",
         *                 "owner.address.city")
         *         .list();
         * </pre>
         * @deprecated replaced by {@link ByIds#fetchPlanProperties(String...)}
         */
        @Deprecated
        public ByIds<E, K> viewProperties(String... properties) {
            loader.createFetchPlanBuilder();
            loader.fetchPlanBuilder.addAll(properties);
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
        public ByIds<E, K> fetchPlanProperties(String... properties) {
            loader.createFetchPlanBuilder();
            loader.fetchPlanBuilder.addAll(properties);
            return this;
        }

        /**
         * Sets soft deletion. The soft deletion is true by default.
         */
        public ByIds<E, K> softDeletion(boolean softDeletion) {
            loader.softDeletion = softDeletion;
            return this;
        }

        /**
         * Sets loading of dynamic attributes. It is false by default.
         */
        public ByIds<E, K> dynamicAttributes(boolean dynamicAttributes) {
            loader.dynamicAttributes = dynamicAttributes;
            return this;
        }
    }

    public static class ByQuery<E extends Entity<K>, K> {

        private FluentLoader<E, K> loader;

        private String queryString;
        private Map<String, Object> parameters = new HashMap<>();
        private int firstResult;
        private int maxResults;
        private boolean cacheable;
        private Condition condition;

        ByQuery(FluentLoader<E, K> loader, String queryString) {
            Preconditions.checkNotEmptyString(queryString, "queryString is empty");
            this.loader = loader;
            this.queryString = queryString;
        }

        ByQuery(FluentLoader<E, K> loader, String queryString, Object[] positionalParams) {
            this(loader, queryString);
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

            LoadContext<E> loadContext = new LoadContext(loader.entityClass);
            loader.initCommonLoadContextParameters(loadContext);

            String processedQuery = AppBeans.get(QueryStringProcessor.class).process(queryString, loader.entityClass);
            LoadContext.Query query = new LoadContext.Query(processedQuery);
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                query.setParameter(entry.getKey(), entry.getValue());
            }
            loadContext.setQuery(query);

            loadContext.getQuery().setCondition(condition);
            loadContext.getQuery().setFirstResult(firstResult);
            loadContext.getQuery().setMaxResults(maxResults);
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
         * Sets a view.
         * @deprecated replaced by {@link ByQuery#fetchPlan(FetchPlan)}
         */
        @Deprecated
        public ByQuery<E, K> view(FetchPlan view) {
            return fetchPlan(view);
        }

        /**
         * Sets a fetch plan.
         */
        public ByQuery<E, K> fetchPlan(FetchPlan fetchPlan) {
            loader.fetchPlan = fetchPlan;
            return this;
        }

        /**
         * Sets a view by name.
         * @deprecated replaced by {@link ById#fetchPlan(String)}
         */
        @Deprecated
        public ByQuery<E, K> view(String viewName) {
            return fetchPlan(viewName);
        }

        /**
         * Sets a view by name.
         */
        public ByQuery<E, K> fetchPlan(String viewName) {
            loader.fetchPlanName = viewName;
            return this;
        }

        /**
         *
         * @deprecated replaced by {@link ByQuery#fetchPlan(Consumer)} )}
         */
        public ByQuery<E, K> view(Consumer<FetchPlanBuilder> viewBuilderConfigurer) {
            return fetchPlan(viewBuilderConfigurer);
        }

        public ByQuery<E, K> fetchPlan(Consumer<FetchPlanBuilder> fetchPlanBuilderConfigurer) {
            loader.createFetchPlanBuilder();
            fetchPlanBuilderConfigurer.accept(loader.fetchPlanBuilder);
            return this;
        }

        /**
         *
         * @deprecated replaced by {@link ByQuery#fetchPlanProperties(String...)}
         */
        public ByQuery<E, K> viewProperties(String... properties) {
            return fetchPlanProperties(properties);
        }

        public ByQuery<E, K> fetchPlanProperties(String... properties) {
            loader.createFetchPlanBuilder();
            loader.fetchPlanBuilder.addAll(properties);
            return this;
        }

        /**
         * Sets soft deletion. The soft deletion is true by default.
         */
        public ByQuery<E, K> softDeletion(boolean softDeletion) {
            loader.softDeletion = softDeletion;
            return this;
        }

        /**
         * Sets loading of dynamic attributes. It is false by default.
         */
        public ByQuery<E, K> dynamicAttributes(boolean dynamicAttributes) {
            loader.dynamicAttributes = dynamicAttributes;
            return this;
        }

        /**
         * Sets additional query condition.
         */
        public ByQuery condition(Condition condition) {
            this.condition = condition;
            return this;
        }

        /**
         * Sets value for a query parameter.
         *
         * @param name  parameter name
         * @param value parameter value
         */
        public ByQuery<E, K> parameter(String name, Object value) {
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
        public ByQuery<E, K> parameter(String name, Date value, TemporalType temporalType) {
            parameters.put(name, new TemporalValue(value, temporalType));
            return this;
        }

        /**
         * Sets the map of query parameters.
         */
        public ByQuery<E, K> setParameters(Map<String, Object> parameters) {
            this.parameters.putAll(parameters);
            return this;
        }

        /**
         * Sets results offset.
         */
        public ByQuery<E, K> firstResult(int firstResult) {
            this.firstResult = firstResult;
            return this;
        }

        /**
         * Sets results limit.
         */
        public ByQuery<E, K> maxResults(int maxResults) {
            this.maxResults = maxResults;
            return this;
        }

        /**
         * Indicates that the query results should be cached.
         * By default, queries are not cached.
         */
        public ByQuery<E, K> cacheable(boolean cacheable) {
            this.cacheable = cacheable;
            return this;
        }
    }
}
