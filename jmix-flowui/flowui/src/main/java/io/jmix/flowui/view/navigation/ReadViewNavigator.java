/*
 * Copyright 2026 Haulmont.
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

package io.jmix.flowui.view.navigation;

import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.RouteParameters;
import io.jmix.flowui.view.View;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

/**
 * Provides a fluent interface for navigating to a read view of an entity.
 *
 * @param <E> the type of the entity shown by the view
 */
public class ReadViewNavigator<E> extends AbstractViewNavigator {

    protected final Class<E> entityClass;

    @Nullable
    protected E entity;

    public ReadViewNavigator(View<?> origin,
                             Class<E> entityClass,
                             Consumer<? extends ReadViewNavigator<E>> handler) {
        super(origin, handler);
        checkNotNullArgument(entityClass);

        this.entityClass = entityClass;
    }

    protected ReadViewNavigator(ReadViewNavigator<E> viewNavigator) {
        super(viewNavigator);

        this.entityClass = viewNavigator.entityClass;
        this.entity = viewNavigator.entity;
    }

    /**
     * Sets the entity instance to show.
     *
     * @param entity entity instance to show
     * @return this instance for chaining
     */
    public ReadViewNavigator<E> readEntity(E entity) {
        checkNotNullArgument(entity);

        this.entity = entity;
        return this;
    }

    @Override
    public ReadViewNavigator<E> withViewId(@Nullable String viewId) {
        super.withViewId(viewId);
        return this;
    }

    /**
     * Sets the opened view by its class.
     *
     * @param viewClass view class
     * @return this instance for chaining
     */
    public <V extends View<?>> ReadViewClassNavigator<E, V> withViewClass(Class<V> viewClass) {
        return new ReadViewClassNavigator<>(this, viewClass);
    }

    @Override
    public ReadViewNavigator<E> withRouteParameters(@Nullable RouteParameters routeParameters) {
        super.withRouteParameters(routeParameters);
        return this;
    }

    @Override
    public ReadViewNavigator<E> withQueryParameters(@Nullable QueryParameters queryParameters) {
        super.withQueryParameters(queryParameters);
        return this;
    }

    @Override
    public ReadViewNavigator<E> withBackwardNavigation(boolean backwardNavigation) {
        super.withBackwardNavigation(backwardNavigation);
        return this;
    }

    /**
     * @return the class of the shown entity
     */
    public Class<E> getEntityClass() {
        return entityClass;
    }

    /**
     * @return the entity instance to show, or an empty {@link Optional} if it is not set
     */
    public Optional<E> getEntity() {
        return Optional.ofNullable(entity);
    }
}
