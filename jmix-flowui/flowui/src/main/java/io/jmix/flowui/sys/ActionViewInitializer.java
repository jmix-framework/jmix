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
import io.jmix.flowui.view.navigation.DetailViewNavigator;
import io.jmix.flowui.view.navigation.ViewNavigator;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class ActionViewInitializer {

    protected String viewId;
    protected Class<? extends View> viewClass;
    protected RouteParametersProvider routeParametersProvider;
    protected QueryParametersProvider queryParametersProvider;
    protected Consumer<AfterCloseEvent<?>> afterCloseHandler;

    @Nullable
    public String getViewId() {
        return viewId;
    }

    public void setViewId(@Nullable String viewId) {
        this.viewId = viewId;
    }

    @Nullable
    public Class<? extends View> getViewClass() {
        return viewClass;
    }

    public void setViewClass(@Nullable Class<? extends View> viewClass) {
        this.viewClass = viewClass;
    }

    @Nullable
    public RouteParametersProvider getRouteParametersProvider() {
        return routeParametersProvider;
    }

    public void setRouteParametersProvider(@Nullable RouteParametersProvider provider) {
        this.routeParametersProvider = provider;
    }

    @Nullable
    public QueryParametersProvider getQueryParametersProvider() {
        return queryParametersProvider;
    }

    public void setQueryParametersProvider(@Nullable QueryParametersProvider provider) {
        this.queryParametersProvider = provider;
    }

    @Nullable
    public <V extends View<?>> Consumer<AfterCloseEvent<V>> getAfterCloseHandler() {
        return (Consumer) afterCloseHandler;
    }

    public <V extends View<?>> void setAfterCloseHandler(@Nullable Consumer<AfterCloseEvent<V>> afterCloseHandler) {
        this.afterCloseHandler = (Consumer) afterCloseHandler;
    }

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

        return windowBuilder;
    }

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

        return windowBuilder;
    }
}
