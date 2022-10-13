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
import io.jmix.flowui.view.View;

import javax.annotation.Nullable;
import java.util.function.Consumer;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

/**
 * Provides a fluent interface to configure navigation parameters and navigate to an entity list {@link View}.
 * <p>
 * An instance of this class should be obtained through {@link ViewNavigators#listView(Class)}.
 */
public class ListViewNavigator<E> extends ViewNavigator {

    protected final Class<E> entityClass;

    public ListViewNavigator(Class<E> entityClass,
                             Consumer<? extends ListViewNavigator<E>> handler) {
        super(handler);
        checkNotNullArgument(entityClass);

        this.entityClass = entityClass;
    }

    @Override
    public ListViewNavigator<E> withViewId(@Nullable String viewId) {
        super.withViewId(viewId);
        return this;
    }

    @Override
    public ListViewNavigator<E> withViewClass(@Nullable Class<? extends View> viewClass) {
        super.withViewClass(viewClass);
        return this;
    }

    @Override
    public ListViewNavigator<E> withRouteParameters(@Nullable RouteParameters routeParameters) {
        super.withRouteParameters(routeParameters);
        return this;
    }

    @Override
    public ListViewNavigator<E> withQueryParameters(@Nullable QueryParameters queryParameters) {
        super.withQueryParameters(queryParameters);
        return this;
    }

    @Override
    public ListViewNavigator<E> withBackwardNavigation(boolean backwardNavigation) {
        super.withBackwardNavigation(backwardNavigation);
        return this;
    }

    /**
     * @return entity class of the list view
     */
    public Class<E> getEntityClass() {
        return entityClass;
    }
}
