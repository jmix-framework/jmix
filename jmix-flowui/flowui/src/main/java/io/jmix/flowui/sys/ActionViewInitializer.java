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

package io.jmix.flowui.sys;

import io.jmix.flowui.action.ViewOpeningAction.QueryParametersProvider;
import io.jmix.flowui.action.ViewOpeningAction.RouteParametersProvider;
import io.jmix.flowui.view.DialogWindow.AfterCloseEvent;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.builder.DetailWindowBuilder;
import io.jmix.flowui.view.builder.LookupWindowBuilder;
import io.jmix.flowui.view.builder.WindowBuilder;
import io.jmix.flowui.view.navigation.DetailViewNavigator;
import io.jmix.flowui.view.navigation.ViewNavigator;
import org.springframework.lang.Nullable;

import java.util.function.Consumer;

/**
 * Initializes and configures various navigators and window builders
 * for views in an application.
 */
public class ActionViewInitializer {

    protected String viewId;
    protected Class<? extends View> viewClass;
    protected RouteParametersProvider routeParametersProvider;
    protected QueryParametersProvider queryParametersProvider;
    protected Consumer<AfterCloseEvent<?>> afterCloseHandler;
    protected Consumer<View<?>> viewConfigurer;

    /**
     * Returns the unique identifier of the view.
     *
     * @return the view identifier or {@code null} if not set
     */
    @Nullable
    public String getViewId() {
        return viewId;
    }

    /**
     * Sets the unique identifier of the view.
     *
     * @param viewId the view identifier to set,
     */
    public void setViewId(@Nullable String viewId) {
        this.viewId = viewId;
    }

    /**
     * Returns the class of the view.
     *
     * @return the class of the view or {@code null} if not set
     */
    @Nullable
    public Class<? extends View> getViewClass() {
        return viewClass;
    }

    /**
     * Sets the class of the view.
     *
     * @param viewClass the class of the view to set
     */
    public void setViewClass(@Nullable Class<? extends View> viewClass) {
        this.viewClass = viewClass;
    }

    /**
     * Returns the {@link RouteParametersProvider} associated with this instance.
     *
     * @return the {@link RouteParametersProvider} instance or {@code null} if not set
     */
    @Nullable
    public RouteParametersProvider getRouteParametersProvider() {
        return routeParametersProvider;
    }

    /**
     * Sets the {@link RouteParametersProvider} instance to be used.
     *
     * @param provider the {@link RouteParametersProvider} instance to set, or {@code null}
     *                 if no provider is to be assigned
     */
    public void setRouteParametersProvider(@Nullable RouteParametersProvider provider) {
        this.routeParametersProvider = provider;
    }

    /**
     * Returns the {@link QueryParametersProvider} associated with this instance.
     *
     * @return the {@link QueryParametersProvider} instance or {@code null} if not set
     */
    @Nullable
    public QueryParametersProvider getQueryParametersProvider() {
        return queryParametersProvider;
    }

    /**
     * Sets the {@link QueryParametersProvider} instance to be used for obtaining query parameters.
     *
     * @param provider the {@link QueryParametersProvider} instance to set,
     *                 or {@code null} if no provider is to be assigned
     */
    public void setQueryParametersProvider(@Nullable QueryParametersProvider provider) {
        this.queryParametersProvider = provider;
    }

    /**
     * Returns the handler to be executed after a view is closed.
     *
     * @param <V> the type of the {@link View} associated with the {@link AfterCloseEvent}
     * @return a {@link Consumer} to handle the {@link AfterCloseEvent}, or {@code null} if not set
     */
    @Nullable
    public <V extends View<?>> Consumer<AfterCloseEvent<V>> getAfterCloseHandler() {
        return (Consumer) afterCloseHandler;
    }

    /**
     * Sets the handler to be executed after a view is closed. This handler is invoked
     * with an {@link AfterCloseEvent} instance associated with the closed view.
     *
     * @param <V>               the type of the {@link View} associated with the {@link AfterCloseEvent}
     * @param afterCloseHandler a {@link Consumer} to handle the {@link AfterCloseEvent}, or {@code null}
     *                          if no handler is to be set
     */
    public <V extends View<?>> void setAfterCloseHandler(@Nullable Consumer<AfterCloseEvent<V>> afterCloseHandler) {
        this.afterCloseHandler = (Consumer) afterCloseHandler;
    }

    /**
     * Returns the view configurer as a {@link Consumer} instance. The configurer can be
     * used to apply specific configurations to a view of the specified type.
     *
     * @param <V> the type of the {@link View} to which the configurer applies
     * @return a {@link Consumer} for configuring the specified {@link View},
     * or {@code null} if no configurer is set
     */
    public <V extends View<?>> Consumer<V> getViewConfigurer() {
        return (Consumer) viewConfigurer;
    }

    /**
     * Sets the view configurer to be used for configuring views of the specified type.
     * The provided configurer is applied during view initialization to further customize
     * the view instance.
     *
     * @param <V>            the type of the {@link View} to which the configurer applies
     * @param viewConfigurer a {@link Consumer} instance to configure the view,
     *                       or {@code null} if no configurer is to be set
     */
    public <V extends View<?>> void setViewConfigurer(@Nullable Consumer<V> viewConfigurer) {
        //noinspection unchecked
        this.viewConfigurer = (Consumer) viewConfigurer;
    }

    /**
     * Initializes the provided {@link ViewNavigator} by applying settings from the current context.
     *
     * @param navigator the {@link ViewNavigator} to be initialized
     * @return the initialized {@link ViewNavigator} instance with the applied configurations
     */
    public ViewNavigator initNavigator(ViewNavigator navigator) {
        if (viewClass != null) {
            navigator = navigator.withViewClass(viewClass);
        }

        if (viewId != null) {
            navigator = navigator.withViewId(viewId);
        }

        if (routeParametersProvider != null) {
            navigator = navigator.withRouteParameters(routeParametersProvider.getRouteParameters());
        }

        if (queryParametersProvider != null) {
            navigator = navigator.withQueryParameters(queryParametersProvider.getQueryParameters());
        }

        return navigator;
    }

    /**
     * Initializes the given {@link DetailViewNavigator} instance by applying settings
     * from the current context.
     *
     * @param <E>       the type of the entity associated with the detail view
     * @param navigator the {@link DetailViewNavigator} to be initialized
     * @return the initialized {@link DetailViewNavigator} with the applied configurations
     */
    public <E> DetailViewNavigator<E> initNavigator(DetailViewNavigator<E> navigator) {
        if (viewClass != null) {
            navigator = navigator.withViewClass(viewClass);
        }

        if (viewId != null) {
            navigator = navigator.withViewId(viewId);
        }

        if (routeParametersProvider != null) {
            navigator = navigator.withRouteParameters(routeParametersProvider.getRouteParameters());
        }

        if (queryParametersProvider != null) {
            navigator = navigator.withQueryParameters(queryParametersProvider.getQueryParameters());
        }

        return navigator;
    }

    /**
     * Initializes the provided {@link DetailWindowBuilder} by applying settings from the current context.
     *
     * @param windowBuilder the {@link DetailWindowBuilder} to be initialized
     * @param <V>           the type of the view associated with the window
     * @param <E>           the type of the entity associated with the window
     * @return the initialized {@link DetailWindowBuilder} with the applied configurations
     */
    public <E, V extends View<?>> DetailWindowBuilder<E, V> initWindowBuilder(DetailWindowBuilder<E, V> windowBuilder) {
        if (viewClass != null) {
            windowBuilder = windowBuilder.withViewClass((Class) viewClass);
        }

        if (viewId != null) {
            windowBuilder = windowBuilder.withViewId(viewId);
        }

        if (afterCloseHandler != null) {
            windowBuilder = windowBuilder.withAfterCloseListener((Consumer) afterCloseHandler);
        }

        if (viewConfigurer != null) {
            windowBuilder = windowBuilder.withViewConfigurer((Consumer) viewConfigurer);
        }

        return windowBuilder;
    }

    /**
     * Initializes the provided {@link LookupWindowBuilder} by applying settings from the current context.
     *
     * @param windowBuilder the {@link LookupWindowBuilder} to be initialized
     * @param <V>           the type of the view associated with the window
     * @param <E>           the type of the entity associated with the window
     * @return the initialized {@link LookupWindowBuilder} with the applied configurations
     */
    public <E, V extends View<?>> LookupWindowBuilder<E, V> initWindowBuilder(LookupWindowBuilder<E, V> windowBuilder) {
        if (viewClass != null) {
            windowBuilder = windowBuilder.withViewClass((Class) viewClass);
        }

        if (viewId != null) {
            windowBuilder = windowBuilder.withViewId(viewId);
        }

        if (afterCloseHandler != null) {
            windowBuilder = windowBuilder.withAfterCloseListener((Consumer) afterCloseHandler);
        }

        if (viewConfigurer != null) {
            windowBuilder = windowBuilder.withViewConfigurer((Consumer) viewConfigurer);
        }

        return windowBuilder;
    }

    /**
     * Initializes the provided {@link WindowBuilder} by applying settings from the current context.
     *
     * @param windowBuilder the {@link WindowBuilder} to be initialized
     * @param <V>           the type of the view associated with the window
     * @return the initialized {@link WindowBuilder} with the applied configurations
     */
    public <V extends View<?>> WindowBuilder<V> initWindowBuilder(WindowBuilder<V> windowBuilder) {
        if (viewClass != null) {
            windowBuilder = windowBuilder.withViewClass((Class) viewClass);
        }

        if (viewId != null) {
            windowBuilder = windowBuilder.withViewId(viewId);
        }

        if (afterCloseHandler != null) {
            windowBuilder = windowBuilder.withAfterCloseListener((Consumer) afterCloseHandler);
        }

        if (viewConfigurer != null) {
            windowBuilder = windowBuilder.withViewConfigurer((Consumer) viewConfigurer);
        }

        return windowBuilder;
    }
}
