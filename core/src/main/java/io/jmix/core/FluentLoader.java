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
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.persistence.TemporalType;
import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;

@Component(FluentLoader.NAME)
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class FluentLoader<E extends JmixEntity> {

    public static final String NAME = "core_FluentLoader";

    private Class<E> entityClass;
    private MetaClass metaClass;

    private DataManager dataManager;

    private boolean joinTransaction = true;
    private FetchPlan fetchPlan;
    private String fetchPlanName;
    private FetchPlanBuilder fetchPlanBuilder;
    private boolean softDeletion = true;
    private Map<String, Serializable> hints;
    private List<AccessConstraint<?>> accessConstraints;

    @Autowired
    private Metadata metadata;

    @Autowired
    private FetchPlanRepository fetchPlanRepository;

    @Autowired
    private BeanFactory beanFactory;

    @Autowired
    private ObjectProvider<FetchPlanBuilder> fetchPlanBuilderProvider;

    @Autowired
    public void setDataManager(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public FluentLoader(Class<E> entityClass) {
        this.entityClass = entityClass;
    }

    @PostConstruct
    private void init() {
        this.metaClass = metadata.getClass(entityClass);
    }

    LoadContext<E> createLoadContext() {
        MetaClass metaClass = metadata.getClass(entityClass);
        LoadContext<E> loadContext = new LoadContext<>(metaClass);
        initCommonLoadContextParameters(loadContext);

        String entityName = metaClass.getName();
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
        loadContext.setHints(hints);
        loadContext.setAccessConstraints(accessConstraints);
    }

    private void createFetchPlanBuilder() {
        if (fetchPlanBuilder == null) {
            fetchPlanBuilder = fetchPlanBuilderProvider.getObject(entityClass);
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

    public FluentLoader<E> joinTransaction(boolean join) {
        this.joinTransaction = join;
        return this;
    }

    /**
     * Sets a view.
     *
     * @deprecated replaced by {@link FluentLoader#fetchPlan(FetchPlan)}
     */
    @Deprecated
    public FluentLoader<E> view(FetchPlan fetchPlan) {
        return fetchPlan(fetchPlan);
    }

    /**
     * Sets a fetch plan.
     */
    public FluentLoader<E> fetchPlan(FetchPlan fetchPlan) {
        this.fetchPlan = fetchPlan;
        return this;
    }

    /**
     * Sets a fetchPlan by name.
     *
     * @deprecated replaced by {@link FluentLoader#fetchPlan(String)}
     */
    @Deprecated
    public FluentLoader<E> view(String fetchPlanName) {
        return fetchPlan(fetchPlanName);
    }

    /**
     * Sets a fetch plan by name.
     */
    public FluentLoader<E> fetchPlan(String fetchPlanName) {
        this.fetchPlanName = fetchPlanName;
        return this;
    }

    /**
     * @deprecated replaced by {@link FluentLoader#fetchPlan(Consumer)}
     */
    @Deprecated
    public FluentLoader<E> view(Consumer<FetchPlanBuilder> fetchPlanBuilderConfigurer) {
        return fetchPlan(fetchPlanBuilderConfigurer);
    }

    public FluentLoader<E> fetchPlan(Consumer<FetchPlanBuilder> fetchPlanBuilderConfigurer) {
        createFetchPlanBuilder();
        fetchPlanBuilderConfigurer.accept(fetchPlanBuilder);
        return this;
    }

    /**
     * @deprecated replaced by {@link FluentLoader#fetchPlanProperties(String...)}
     */
    @Deprecated
    public FluentLoader<E> viewProperties(String... properties) {
        return fetchPlanProperties(properties);
    }

    public FluentLoader<E> fetchPlanProperties(String... properties) {
        createFetchPlanBuilder();
        fetchPlanBuilder.addAll(properties);
        return this;
    }

    /**
     * Sets soft deletion. The soft deletion is true by default.
     */
    public FluentLoader<E> softDeletion(boolean softDeletion) {
        this.softDeletion = softDeletion;
        return this;
    }

    /**
     * Sets custom hint that should be used by the query.
     */
    public FluentLoader<E> hint(String hintName, Serializable value) {
        if (hints == null) {
            hints = new HashMap<>();
        }
        hints.put(hintName, value);
        return this;
    }

    /**
     * Sets custom hints that should be used by the query.
     */
    public FluentLoader<E> hints(Map<String, Serializable> hints) {
        this.hints = hints;
        return this;
    }

    /**
     * Sets access constraints.
     */
    public FluentLoader<E> accessConstraints(List<AccessConstraint<?>> accessConstraints) {
        this.accessConstraints = accessConstraints;
        return this;
    }

    /**
     * Sets the entity identifier.
     */
    public ById<E> id(Object id) {
        return new ById<>(this, id);
    }

    /**
     * Sets array of entity identifiers.
     */
    @SafeVarargs
    public final ByIds<E> ids(Object... ids) {
        return new ByIds<>(this, Arrays.asList(ids));
    }

    /**
     * Sets collection of entity identifiers.
     */
    public ByIds<E> ids(Collection ids) {
        return new ByIds<>(this, ids);
    }

    /**
     * Sets the query text.
     */
    public ByQuery<E> query(String queryString) {
        return new ByQuery<>(this, queryString, beanFactory);
    }

    /**
     * Sets the query with positional parameters (e.g. {@code "e.name = ?1 and e.status = ?2"}).
     */
    public ByQuery<E> query(String queryString, Object... parameters) {
        return new ByQuery<>(this, queryString, parameters, beanFactory);
    }

    public static class ById<E extends JmixEntity> {

        private FluentLoader<E> loader;
        private Object id;

        ById(FluentLoader<E> loader, Object id) {
            this.loader = loader;
            this.id = id;
        }

        LoadContext<E> createLoadContext() {
            LoadContext<E> loadContext = new LoadContext(loader.metaClass).setId(id);
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
         * Sets a fetchPlan.
         *
         * @deprecated replaced by {@link ById#fetchPlan(FetchPlan)}
         */
        @Deprecated
        public ById<E> view(FetchPlan fetchPlan) {
            return fetchPlan(fetchPlan);
        }

        /**
         * Sets a fetch plan.
         */
        public ById<E> fetchPlan(FetchPlan fetchPlan) {
            loader.fetchPlan = fetchPlan;
            return this;
        }

        /**
         * Sets a fetchPlan by name.
         *
         * @deprecated replaced by {@link ById#fetchPlan(String)}
         */
        @Deprecated
        public ById<E> view(String fetchPlanName) {
            return fetchPlan(fetchPlanName);
        }

        /**
         * Sets a fetch plan by name.
         */
        public ById<E> fetchPlan(String fetchPlanName) {
            loader.fetchPlanName = fetchPlanName;
            return this;
        }

        /**
         * @deprecated replaced by {@link ById#fetchPlan(Consumer)} )}
         */
        @Deprecated
        public ById<E> view(Consumer<FetchPlanBuilder> fetchPlanBuilderConfigurer) {
            return fetchPlan(fetchPlanBuilderConfigurer);
        }

        public ById<E> fetchPlan(Consumer<FetchPlanBuilder> fetchPlanBuilderConfigurer) {
            loader.createFetchPlanBuilder();
            fetchPlanBuilderConfigurer.accept(loader.fetchPlanBuilder);
            return this;
        }

        /**
         * @deprecated replaced by {@link ById#fetchPlanProperties(String...)}
         */
        @Deprecated
        public ById<E> viewProperties(String... properties) {
            return fetchPlanProperties(properties);
        }

        public ById<E> fetchPlanProperties(String... properties) {
            loader.createFetchPlanBuilder();
            loader.fetchPlanBuilder.addAll(properties);
            return this;
        }

        /**
         * Sets soft deletion. The soft deletion is true by default.
         */
        public ById<E> softDeletion(boolean softDeletion) {
            loader.softDeletion = softDeletion;
            return this;
        }

        /**
         * Sets custom hint that should be used by the query.
         */
        public ById<E> hint(String hintName, Serializable value) {
            if (loader.hints == null) {
                loader.hints = new HashMap<>();
            }
            loader.hints.put(hintName, value);
            return this;
        }

        /**
         * Sets custom hints that should be used by the query.
         */
        public ById<E> hints(Map<String, Serializable> hints) {
            loader.hints = hints;
            return this;
        }

        /**
         * Sets access constraints.
         */
        public ById<E> accessConstraints(List<AccessConstraint<?>> accessConstraints) {
            loader.accessConstraints = accessConstraints;
            return this;
        }
    }

    public static class ByIds<E extends JmixEntity> {

        private FluentLoader<E> loader;
        private Collection ids;

        ByIds(FluentLoader<E> loader, Collection ids) {
            this.loader = loader;
            this.ids = ids;
        }

        LoadContext<E> createLoadContext() {
            LoadContext<E> loadContext = new LoadContext(loader.metaClass).setIds(ids);
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
         * Sets a fetchPlan.
         *
         * @deprecated replaced by {@link ByIds#fetchPlan(FetchPlan)}
         */
        @Deprecated
        public ByIds<E> view(FetchPlan fetchPlan) {
            return fetchPlan(fetchPlan);
        }

        /**
         * Sets a fetch plan.
         */
        public ByIds<E> fetchPlan(FetchPlan fetchPlan) {
            loader.fetchPlan = fetchPlan;
            return this;
        }

        /**
         * Sets a fetchPlan by name.
         *
         * @deprecated replaced by {@link ByIds#fetchPlan(String)}
         */
        @Deprecated
        public ByIds<E> view(String fetchPlanName) {
            return fetchPlan(fetchPlanName);
        }

        /**
         * Sets a fetch plan by name.
         */
        public ByIds<E> fetchPlan(String fetchPlanName) {
            loader.fetchPlanName = fetchPlanName;
            return this;
        }

        /**
         * Sets a fetchPlan configured by the {@link FetchPlanBuilder}. For example:
         * <pre>
         *     dataManager.load(Pet.class)
         *         .ids(id1, id2)
         *         .view(viewBuilder -&gt; viewBuilder.addAll(
         *                 "name",
         *                 "owner.name"))
         *         .list();
         * </pre>
         *
         * @deprecated replaced by {@link ByIds#fetchPlan(Consumer)} )}
         */
        @Deprecated
        public ByIds<E> view(Consumer<FetchPlanBuilder> fetchPlanBuilderConfigurer) {
            return fetchPlan(fetchPlanBuilderConfigurer);
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
         * Sets a fetchPlan containing the given properties. A property can be designated by a path in the entity graph.
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
         *
         * @deprecated replaced by {@link ByIds#fetchPlanProperties(String...)}
         */
        @Deprecated
        public ByIds<E> viewProperties(String... properties) {
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
        public ByIds<E> fetchPlanProperties(String... properties) {
            loader.createFetchPlanBuilder();
            loader.fetchPlanBuilder.addAll(properties);
            return this;
        }

        /**
         * Sets soft deletion. The soft deletion is true by default.
         */
        public ByIds<E> softDeletion(boolean softDeletion) {
            loader.softDeletion = softDeletion;
            return this;
        }

        /**
         * Sets custom hint that should be used by the query.
         */
        public ByIds<E> hint(String hintName, Serializable value) {
            if (loader.hints == null) {
                loader.hints = new HashMap<>();
            }
            loader.hints.put(hintName, value);
            return this;
        }

        /**
         * Sets custom hints that should be used by the query.
         */
        public ByIds<E> hints(Map<String, Serializable> hints) {
            loader.hints = hints;
            return this;
        }

        /**
         * Sets access constraints.
         */
        public ByIds<E> accessConstraints(List<AccessConstraint<?>> accessConstraints) {
            loader.accessConstraints = accessConstraints;
            return this;
        }
    }

    public static class ByQuery<E extends JmixEntity> {

        private FluentLoader<E> loader;

        private String queryString;
        private Map<String, Object> parameters = new HashMap<>();
        private int firstResult;
        private int maxResults;
        private boolean cacheable;
        private Condition condition;
        private BeanFactory beanFactory;

        ByQuery(FluentLoader<E> loader, String queryString, BeanFactory beanFactory) {
            this.beanFactory = beanFactory;
            Preconditions.checkNotEmptyString(queryString, "queryString is empty");
            this.loader = loader;
            this.queryString = queryString;
        }

        ByQuery(FluentLoader<E> loader, String queryString, Object[] positionalParams, BeanFactory beanFactory) {
            this(loader, queryString, beanFactory);
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

            LoadContext<E> loadContext = new LoadContext(loader.metaClass);
            loader.initCommonLoadContextParameters(loadContext);

            String processedQuery = beanFactory.getBean(QueryStringProcessor.class).process(queryString, loader.entityClass);
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
         * Sets a fetchPlan.
         *
         * @deprecated replaced by {@link ByQuery#fetchPlan(FetchPlan)}
         */
        @Deprecated
        public ByQuery<E> view(FetchPlan fetchPlan) {
            return fetchPlan(fetchPlan);
        }

        /**
         * Sets a fetch plan.
         */
        public ByQuery<E> fetchPlan(FetchPlan fetchPlan) {
            loader.fetchPlan = fetchPlan;
            return this;
        }

        /**
         * Sets a fetchPlan by name.
         *
         * @deprecated replaced by {@link ById#fetchPlan(String)}
         */
        @Deprecated
        public ByQuery<E> view(String fetchPlanName) {
            return fetchPlan(fetchPlanName);
        }

        /**
         * Sets a fetchPlan by name.
         */
        public ByQuery<E> fetchPlan(String fetchPlanName) {
            loader.fetchPlanName = fetchPlanName;
            return this;
        }

        /**
         * @deprecated replaced by {@link ByQuery#fetchPlan(Consumer)} )}
         */
        public ByQuery<E> view(Consumer<FetchPlanBuilder> fetchPlanBuilderConfigurer) {
            return fetchPlan(fetchPlanBuilderConfigurer);
        }

        public ByQuery<E> fetchPlan(Consumer<FetchPlanBuilder> fetchPlanBuilderConfigurer) {
            loader.createFetchPlanBuilder();
            fetchPlanBuilderConfigurer.accept(loader.fetchPlanBuilder);
            return this;
        }

        /**
         * @deprecated replaced by {@link ByQuery#fetchPlanProperties(String...)}
         */
        public ByQuery<E> viewProperties(String... properties) {
            return fetchPlanProperties(properties);
        }

        public ByQuery<E> fetchPlanProperties(String... properties) {
            loader.createFetchPlanBuilder();
            loader.fetchPlanBuilder.addAll(properties);
            return this;
        }

        /**
         * Sets soft deletion. The soft deletion is true by default.
         */
        public ByQuery<E> softDeletion(boolean softDeletion) {
            loader.softDeletion = softDeletion;
            return this;
        }

        /**
         * Sets custom hint that should be used by the query.
         */
        public ByQuery hint(String hintName, Serializable value) {
            if (loader.hints == null) {
                loader.hints = new HashMap<>();
            }
            loader.hints.put(hintName, value);
            return this;
        }

        /**
         * Sets custom hints that should be used by the query.
         */
        public ByQuery hints(Map<String, Serializable> hints) {
            loader.hints = hints;
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
        public ByQuery<E> parameter(String name, Object value) {
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
        public ByQuery<E> parameter(String name, Date value, TemporalType temporalType) {
            parameters.put(name, new TemporalValue(value, temporalType));
            return this;
        }

        /**
         * Sets the map of query parameters.
         */
        public ByQuery<E> setParameters(Map<String, Object> parameters) {
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
         * Indicates that the query results should be cached.
         * By default, queries are not cached.
         */
        public ByQuery<E> cacheable(boolean cacheable) {
            this.cacheable = cacheable;
            return this;
        }

        /**
         * Sets access constraints.
         */
        public ByQuery<E> accessConstraints(List<AccessConstraint<?>> accessConstraints) {
            loader.accessConstraints = accessConstraints;
            return this;
        }
    }
}
