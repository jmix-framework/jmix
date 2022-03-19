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

import io.jmix.core.Entity;
import io.jmix.core.FetchPlan;
import io.jmix.core.FetchPlanBuilder;
import io.jmix.core.LoadContext;
import io.jmix.core.constraint.AccessConstraint;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.querycondition.Condition;
import io.jmix.data.PersistenceHints;
import org.springframework.context.ApplicationContext;

import javax.persistence.TemporalType;
import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;

public class FluentLoader<E extends Entity> extends io.jmix.core.FluentLoader<E> {

    public static final String NAME = "core_FluentLoader";

    public FluentLoader(Class<E> entityClass) {
        super(entityClass);
    }

    /**
     * Loads a list of entities.
     */
    public List<E> list() {
        return all().list();
    }

    /**
     * Loads a single instance and wraps it in Optional.
     */
    public Optional<E> optional() {
        return all().optional();
    }

    /**
     * Loads a single instance.
     *
     * @throws IllegalStateException if nothing was loaded
     */
    public E one() {
        return all().one();
    }

    public FluentLoader<E> joinTransaction(boolean join) {
        all().joinTransaction(join);
        return this;
    }

    public FluentLoader<E> fetchPlan(FetchPlan fetchPlan) {
        all().fetchPlan(fetchPlan);
        return this;
    }

    public FluentLoader<E> fetchPlan(String fetchPlanName) {
        all().fetchPlan(fetchPlanName);
        return this;
    }

    public FluentLoader<E> fetchPlan(Consumer<FetchPlanBuilder> fetchPlanBuilderConfigurer) {
        all().fetchPlan(fetchPlanBuilderConfigurer);
        return this;
    }

    public FluentLoader<E> fetchPlanProperties(String... properties) {
        all().fetchPlanProperties(properties);
        return this;
    }

    public FluentLoader<E> softDeletion(boolean softDeletion) {
        all().hint(PersistenceHints.SOFT_DELETION, softDeletion);
        return this;
    }

    public FluentLoader<E> hint(String hintName, Serializable value) {
        all().hint(hintName, value);
        return this;
    }

    public FluentLoader<E> hints(Map<String, Serializable> hints) {
        all().hints(hints);
        return this;
    }

    public FluentLoader<E> accessConstraints(List<AccessConstraint<?>> accessConstraints) {
        all().accessConstraints(accessConstraints);
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
     * Sets a fetchPlan by name.
     *
     * @deprecated replaced by {@link FluentLoader#fetchPlan(String)}
     */
    @Deprecated
    public FluentLoader<E> view(String fetchPlanName) {
        return fetchPlan(fetchPlanName);
    }

    /**
     * @deprecated replaced by {@link FluentLoader#fetchPlan(Consumer)}
     */
    @Deprecated
    public FluentLoader<E> view(Consumer<FetchPlanBuilder> fetchPlanBuilderConfigurer) {
        return fetchPlan(fetchPlanBuilderConfigurer);
    }

    /**
     * @deprecated replaced by {@link FluentLoader#fetchPlanProperties(String...)}
     */
    @Deprecated
    public FluentLoader<E> viewProperties(String... properties) {
        return fetchPlanProperties(properties);
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
    public ByIds<E> ids(Object... ids) {
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
        return new ByQuery<>(this, queryString, applicationContext);
    }

    /**
     * Sets the query with positional parameters (e.g. {@code "e.name = ?1 and e.status = ?2"}).
     */
    public ByQuery<E> query(String queryString, Object... parameters) {
        return new ByQuery<>(this, queryString, parameters, applicationContext);
    }

    @Override
    protected LoadContext<E> instantiateLoadContext(MetaClass metaClass) {
        return new com.haulmont.cuba.core.global.LoadContext<>(metaClass);
    }

    public static class ById<E extends Entity> extends io.jmix.core.FluentLoader.ById<E> {

        protected ById(io.jmix.core.FluentLoader<E> loader, Object id) {
            super(loader, id);
        }

        @Override
        public FluentLoader.ById<E> fetchPlan(FetchPlan fetchPlan) {
            return (ById<E>) super.fetchPlan(fetchPlan);
        }

        @Override
        public FluentLoader.ById<E> fetchPlan(String fetchPlanName) {
            return (ById<E>) super.fetchPlan(fetchPlanName);
        }

        @Override
        public FluentLoader.ById<E> fetchPlan(Consumer<FetchPlanBuilder> fetchPlanBuilderConfigurer) {
            return (ById<E>) super.fetchPlan(fetchPlanBuilderConfigurer);
        }

        @Override
        public FluentLoader.ById<E> fetchPlanProperties(String... properties) {
            return (ById<E>) super.fetchPlanProperties(properties);
        }

        public FluentLoader.ById<E> softDeletion(boolean softDeletion) {
            return (ById<E>) super.hint(PersistenceHints.SOFT_DELETION, softDeletion);
        }

        @Override
        public FluentLoader.ById<E> hint(String hintName, Serializable value) {
            return (ById<E>) super.hint(hintName, value);
        }

        @Override
        public FluentLoader.ById<E> hints(Map<String, Serializable> hints) {
            return (ById<E>) super.hints(hints);
        }

        @Override
        public FluentLoader.ById<E> accessConstraints(Collection<AccessConstraint<?>> accessConstraints) {
            return (ById<E>) super.accessConstraints(accessConstraints);
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
         * Sets a fetchPlan by name.
         *
         * @deprecated replaced by {@link ById#fetchPlan(String)}
         */
        @Deprecated
        public ById<E> view(String fetchPlanName) {
            return fetchPlan(fetchPlanName);
        }

        /**
         * @deprecated replaced by {@link ById#fetchPlan(Consumer)} )}
         */
        @Deprecated
        public ById<E> view(Consumer<FetchPlanBuilder> fetchPlanBuilderConfigurer) {
            return fetchPlan(fetchPlanBuilderConfigurer);
        }

        /**
         * @deprecated replaced by {@link ById#fetchPlanProperties(String...)}
         */
        @Deprecated
        public ById<E> viewProperties(String... properties) {
            return fetchPlanProperties(properties);
        }
    }

    public static class ByIds<E extends Entity> extends io.jmix.core.FluentLoader.ByIds<E> {

        protected ByIds(io.jmix.core.FluentLoader<E> loader, Collection ids) {
            super(loader, ids);
        }

        @Override
        public FluentLoader.ByIds<E> fetchPlan(FetchPlan fetchPlan) {
            return (ByIds<E>) super.fetchPlan(fetchPlan);
        }

        @Override
        public FluentLoader.ByIds<E> fetchPlan(String fetchPlanName) {
            return (ByIds<E>) super.fetchPlan(fetchPlanName);
        }

        @Override
        public FluentLoader.ByIds<E> fetchPlan(Consumer<FetchPlanBuilder> fetchPlanBuilderConfigurer) {
            return (ByIds<E>) super.fetchPlan(fetchPlanBuilderConfigurer);
        }

        @Override
        public FluentLoader.ByIds<E> fetchPlanProperties(String... properties) {
            return (ByIds<E>) super.fetchPlanProperties(properties);
        }

        public FluentLoader.ByIds<E> softDeletion(boolean softDeletion) {
            return (ByIds<E>) super.hint(PersistenceHints.SOFT_DELETION, softDeletion);
        }

        @Override
        public FluentLoader.ByIds<E> hint(String hintName, Serializable value) {
            return (ByIds<E>) super.hint(hintName, value);
        }

        @Override
        public FluentLoader.ByIds<E> hints(Map<String, Serializable> hints) {
            return (ByIds<E>) super.hints(hints);
        }

        @Override
        public FluentLoader.ByIds<E> accessConstraints(Collection<AccessConstraint<?>> accessConstraints) {
            return (ByIds<E>) super.accessConstraints(accessConstraints);
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
         * Sets a fetchPlan by name.
         *
         * @deprecated replaced by {@link ByIds#fetchPlan(String)}
         */
        @Deprecated
        public ByIds<E> view(String fetchPlanName) {
            return fetchPlan(fetchPlanName);
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
            fetchPlanProperties(properties);
            return this;
        }
    }

    public static class ByQuery<E extends Entity> extends io.jmix.core.FluentLoader.ByQuery<E> {

        protected ByQuery(io.jmix.core.FluentLoader<E> loader, String queryString, ApplicationContext applicationContext) {
            super(loader, queryString, applicationContext);
        }

        protected ByQuery(io.jmix.core.FluentLoader<E> loader, String queryString, Object[] positionalParams, ApplicationContext applicationContext) {
            super(loader, queryString, positionalParams, applicationContext);
        }

        @Override
        public FluentLoader.ByQuery<E> fetchPlan(FetchPlan fetchPlan) {
            return (ByQuery<E>) super.fetchPlan(fetchPlan);
        }

        @Override
        public FluentLoader.ByQuery<E> fetchPlan(String fetchPlanName) {
            return (ByQuery<E>) super.fetchPlan(fetchPlanName);
        }

        @Override
        public FluentLoader.ByQuery<E> fetchPlan(Consumer<FetchPlanBuilder> fetchPlanBuilderConfigurer) {
            return (ByQuery<E>) super.fetchPlan(fetchPlanBuilderConfigurer);
        }

        @Override
        public FluentLoader.ByQuery<E> fetchPlanProperties(String... properties) {
            return (ByQuery<E>) super.fetchPlanProperties(properties);
        }

        public FluentLoader.ByQuery<E> softDeletion(boolean softDeletion) {
            return (ByQuery<E>) super.hint(PersistenceHints.SOFT_DELETION, softDeletion);
        }

        @Override
        public FluentLoader.ByQuery hint(String hintName, Serializable value) {
            return (ByQuery) super.hint(hintName, value);
        }

        @Override
        public FluentLoader.ByQuery hints(Map<String, Serializable> hints) {
            return (ByQuery) super.hints(hints);
        }

        @Override
        public ByCondition condition(Condition condition) {
            return super.condition(condition);
        }

        @Override
        public FluentLoader.ByQuery<E> parameter(String name, Date value, TemporalType temporalType) {
            return (ByQuery<E>) super.parameter(name, value, temporalType);
        }

        @Override
        public FluentLoader.ByQuery<E> parameters(Map<String, Object> parameters) {
            return (ByQuery<E>) super.parameters(parameters);
        }

        public FluentLoader.ByQuery<E> setParameters(Map<String, Object> parameters) {
            return (ByQuery<E>) super.parameters(parameters);
        }

        @Override
        public FluentLoader.ByQuery<E> firstResult(int firstResult) {
            return (ByQuery<E>) super.firstResult(firstResult);
        }

        @Override
        public FluentLoader.ByQuery<E> maxResults(int maxResults) {
            return (ByQuery<E>) super.maxResults(maxResults);
        }

        @Override
        public FluentLoader.ByQuery<E> cacheable(boolean cacheable) {
            return (ByQuery<E>) super.cacheable(cacheable);
        }

        @Override
        public FluentLoader.ByQuery<E> accessConstraints(Collection<AccessConstraint<?>> accessConstraints) {
            return (ByQuery<E>) super.accessConstraints(accessConstraints);
        }

        @Override
        public FluentLoader.ByQuery<E> parameter(String name, Object value) {
            return (ByQuery<E>) super.parameter(name, value);
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
         * Sets a fetchPlan by name.
         *
         * @deprecated replaced by {@link ById#fetchPlan(String)}
         */
        @Deprecated
        public ByQuery<E> view(String fetchPlanName) {
            return fetchPlan(fetchPlanName);
        }

        /**
         * @deprecated replaced by {@link ByQuery#fetchPlan(Consumer)} )}
         */
        @Deprecated
        public ByQuery<E> view(Consumer<FetchPlanBuilder> fetchPlanBuilderConfigurer) {
            return fetchPlan(fetchPlanBuilderConfigurer);
        }

        /**
         * @deprecated replaced by {@link ByQuery#fetchPlanProperties(String...)}
         */
        @Deprecated
        public ByQuery<E> viewProperties(String... properties) {
            return fetchPlanProperties(properties);
        }
    }
}
