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

package io.jmix.flowui.view.navigation;

import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.RouteParameters;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.view.DetailViewMode;
import io.jmix.flowui.view.View;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Consumer;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

/**
 * Provides a fluent interface to configure navigation parameters and navigate to an entity detail {@link View}.
 * <p>
 * An instance of this class should be obtained through {@link ViewNavigators#detailView(Class)} and its overloaded
 * variants.
 */
public class DetailViewNavigator<E> extends ViewNavigator {

    protected final Class<E> entityClass;

    protected E editedEntity;
    protected boolean readOnly;

    protected DetailViewMode mode = DetailViewMode.CREATE;

    public DetailViewNavigator(Class<E> entityClass, Consumer<? extends DetailViewNavigator<E>> handler) {
        super(handler);
        checkNotNullArgument(entityClass);

        this.entityClass = entityClass;
    }

    public DetailViewNavigator<E> newEntity() {
        this.mode = DetailViewMode.CREATE;
        return this;
    }

    public DetailViewNavigator<E> editEntity(E entity) {
        checkNotNullArgument(entity);

        this.editedEntity = entity;
        this.mode = DetailViewMode.EDIT;
        return this;
    }

    @Override
    public DetailViewNavigator<E> withViewId(@Nullable String viewId) {
        super.withViewId(viewId);
        return this;
    }

    @Override
    public DetailViewNavigator<E> withViewClass(@Nullable Class<? extends View> viewClass) {
        super.withViewClass(viewClass);
        return this;
    }

    @Override
    public DetailViewNavigator<E> withRouteParameters(@Nullable RouteParameters routeParameters) {
        super.withRouteParameters(routeParameters);
        return this;
    }

    @Override
    public DetailViewNavigator<E> withQueryParameters(@Nullable QueryParameters queryParameters) {
        super.withQueryParameters(queryParameters);
        return this;
    }

    @Override
    public DetailViewNavigator<E> withBackwardNavigation(boolean backwardNavigation) {
        super.withBackwardNavigation(backwardNavigation);
        return this;
    }

    public DetailViewNavigator<E> withReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        return this;
    }

    /**
     * @return entity class of the detail view
     */
    public Class<E> getEntityClass() {
        return entityClass;
    }

    /**
     * @return entity instance shown by the detail view
     */
    public Optional<E> getEditedEntity() {
        return Optional.ofNullable(editedEntity);
    }

    /**
     * @return the detail view purpose (create or edit an entity)
     */
    public DetailViewMode getMode() {
        return mode;
    }

    /**
     * @return whether a view should be opened in read-only mode
     */
    public boolean isReadOnly() {
        return readOnly;
    }
}
