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

import io.jmix.core.FetchPlan;
import io.jmix.core.FetchPlanBuilder;
import io.jmix.core.JmixEntity;
import org.springframework.beans.factory.BeanFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Consumer;

public class FluentLoader<E extends JmixEntity> extends io.jmix.core.FluentLoader<E> {

    public static final String NAME = "core_FluentLoader";

    public FluentLoader(Class<E> entityClass) {
        super(entityClass);
    }

    /**
     * Sets a view.
     *
     * @deprecated replaced by {@link FluentLoader#fetchPlan(FetchPlan)}
     */
    @Deprecated
    public FluentLoader<E> view(FetchPlan fetchPlan) {
        return (FluentLoader<E>) fetchPlan(fetchPlan);
    }

    /**
     * Sets a fetchPlan by name.
     *
     * @deprecated replaced by {@link FluentLoader#fetchPlan(String)}
     */
    @Deprecated
    public FluentLoader<E> view(String fetchPlanName) {
        return (FluentLoader<E>) fetchPlan(fetchPlanName);
    }

    /**
     * @deprecated replaced by {@link FluentLoader#fetchPlan(Consumer)}
     */
    @Deprecated
    public FluentLoader<E> view(Consumer<FetchPlanBuilder> fetchPlanBuilderConfigurer) {
        return (FluentLoader<E>) fetchPlan(fetchPlanBuilderConfigurer);
    }

    /**
     * @deprecated replaced by {@link FluentLoader#fetchPlanProperties(String...)}
     */
    @Deprecated
    public FluentLoader<E> viewProperties(String... properties) {
        return (FluentLoader<E>) fetchPlanProperties(properties);
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
        return new ByQuery<>(this, queryString, beanFactory);
    }

    /**
     * Sets the query with positional parameters (e.g. {@code "e.name = ?1 and e.status = ?2"}).
     */
    public ByQuery<E> query(String queryString, Object... parameters) {
        return new ByQuery<>(this, queryString, parameters, beanFactory);
    }

    public static class ById<E extends JmixEntity> extends io.jmix.core.FluentLoader.ById<E> {

        protected ById(io.jmix.core.FluentLoader<E> loader, Object id) {
            super(loader, id);
        }

        /**
         * Sets a fetchPlan.
         *
         * @deprecated replaced by {@link ById#fetchPlan(FetchPlan)}
         */
        @Deprecated
        public ById<E> view(FetchPlan fetchPlan) {
            return (ById<E>) fetchPlan(fetchPlan);
        }

        /**
         * Sets a fetchPlan by name.
         *
         * @deprecated replaced by {@link ById#fetchPlan(String)}
         */
        @Deprecated
        public ById<E> view(String fetchPlanName) {
            return (ById<E>) fetchPlan(fetchPlanName);
        }

        /**
         * @deprecated replaced by {@link ById#fetchPlan(Consumer)} )}
         */
        @Deprecated
        public ById<E> view(Consumer<FetchPlanBuilder> fetchPlanBuilderConfigurer) {
            return (ById<E>) fetchPlan(fetchPlanBuilderConfigurer);
        }

        /**
         * @deprecated replaced by {@link ById#fetchPlanProperties(String...)}
         */
        @Deprecated
        public ById<E> viewProperties(String... properties) {
            return (ById<E>) fetchPlanProperties(properties);
        }
    }

    public static class ByIds<E extends JmixEntity> extends io.jmix.core.FluentLoader.ByIds<E> {

        protected ByIds(io.jmix.core.FluentLoader<E> loader, Collection ids) {
            super(loader, ids);
        }

        /**
         * Sets a fetchPlan.
         *
         * @deprecated replaced by {@link ByIds#fetchPlan(FetchPlan)}
         */
        @Deprecated
        public ByIds<E> view(FetchPlan fetchPlan) {
            return (ByIds<E>) fetchPlan(fetchPlan);
        }

        /**
         * Sets a fetchPlan by name.
         *
         * @deprecated replaced by {@link ByIds#fetchPlan(String)}
         */
        @Deprecated
        public ByIds<E> view(String fetchPlanName) {
            return (ByIds<E>) fetchPlan(fetchPlanName);
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
            return (ByIds<E>) fetchPlan(fetchPlanBuilderConfigurer);
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

    public static class ByQuery<E extends JmixEntity> extends io.jmix.core.FluentLoader.ByQuery<E> {

        protected ByQuery(io.jmix.core.FluentLoader<E> loader, String queryString, BeanFactory beanFactory) {
            super(loader, queryString, beanFactory);
        }

        protected ByQuery(io.jmix.core.FluentLoader<E> loader, String queryString, Object[] positionalParams, BeanFactory beanFactory) {
            super(loader, queryString, positionalParams, beanFactory);
        }

        /**
         * Sets a fetchPlan.
         *
         * @deprecated replaced by {@link ByQuery#fetchPlan(FetchPlan)}
         */
        @Deprecated
        public ByQuery<E> view(FetchPlan fetchPlan) {
            return (ByQuery<E>) fetchPlan(fetchPlan);
        }

        /**
         * Sets a fetchPlan by name.
         *
         * @deprecated replaced by {@link ById#fetchPlan(String)}
         */
        @Deprecated
        public ByQuery<E> view(String fetchPlanName) {
            return (ByQuery<E>) fetchPlan(fetchPlanName);
        }

        /**
         * @deprecated replaced by {@link ByQuery#fetchPlan(Consumer)} )}
         */
        public ByQuery<E> view(Consumer<FetchPlanBuilder> fetchPlanBuilderConfigurer) {
            return (ByQuery<E>) fetchPlan(fetchPlanBuilderConfigurer);
        }

        /**
         * @deprecated replaced by {@link ByQuery#fetchPlanProperties(String...)}
         */
        public ByQuery<E> viewProperties(String... properties) {
            return (ByQuery<E>) fetchPlanProperties(properties);
        }
    }
}
