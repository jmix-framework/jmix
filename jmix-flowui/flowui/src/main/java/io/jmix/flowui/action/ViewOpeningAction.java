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

package io.jmix.flowui.action;

import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.view.DialogWindow;
import io.jmix.flowui.view.Install;
import io.jmix.flowui.view.OpenMode;
import io.jmix.flowui.view.View;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Interface to be implemented by actions that open a view.
 */
public interface ViewOpeningAction extends Action {

    /**
     * Returns the view open mode if it was set by {@link #setOpenMode(OpenMode)}
     * or in the view XML, otherwise returns {@code null}.
     */
    @Nullable
    OpenMode getOpenMode();

    /**
     * Sets the view open mode.
     *
     * @param openMode the open mode to set
     */
    void setOpenMode(@Nullable OpenMode openMode);

    /**
     * Returns the view id if it was set by {@link #setViewId(String)}
     * or in the view XML, otherwise returns {@code null}.
     */
    @Nullable
    String getViewId();

    /**
     * Sets the view id.
     *
     * @param viewId the view id to set
     */
    void setViewId(@Nullable String viewId);

    /**
     * Returns the view class if it was set by {@link #setViewClass(Class)}
     * or in the view XML, otherwise returns {@code null}.
     */
    @Nullable
    Class<? extends View> getViewClass();

    /**
     * Sets the view class.
     *
     * @param viewClass the view class to set
     */
    void setViewClass(@Nullable Class<? extends View> viewClass);

    /**
     * @return route parameters or {@code null} if not set
     */
    @Nullable
    RouteParametersProvider getRouteParametersProvider();

    /**
     * Sets route parameters provider that returns route parameters
     * that should be used in the route template.
     * <p>
     * Note that route parameters provider is set if the detail is
     * opened in {@link OpenMode#NAVIGATION}.
     *
     * @param routeParameters route parameters provider to set
     * @see Route
     */
    void setRouteParametersProvider(@Nullable RouteParametersProvider routeParameters);

    /**
     * @return query parameters provider or {@code null} if not set
     */
    @Nullable
    QueryParametersProvider getQueryParametersProvider();

    /**
     * Sets query parameters provider that returns query parameters
     * that should be used in the URL.
     * <p>
     * Note that query parameters provider is set if the detail is
     * opened in {@link OpenMode#NAVIGATION}.
     *
     * @param queryParameters query parameters provider to set
     */
    void setQueryParametersProvider(@Nullable QueryParametersProvider queryParameters);

    /**
     * Sets the handler to be invoked when the detail view closes.
     * <p>
     * Note that handler is invoked if the detail is opened in {@link OpenMode#DIALOG} mode.
     * <p>
     * The preferred way to set the handler is using a controller method
     * annotated with {@link Install}, e.g.:
     * <pre>
     * &#64;Install(to = "petsTable.view", subject = "afterCloseHandler")
     * protected void petsTableViewAfterCloseHandler(AfterCloseEvent event) {
     *     if (event.closedWith(StandardOutcome.SAVE)) {
     *         System.out.println("Saved");
     *     }
     * }
     * </pre>
     *
     * @param afterCloseHandler handler to set
     * @param <V>               view type
     */
    <V extends View<?>> void setAfterCloseHandler(@Nullable Consumer<DialogWindow.AfterCloseEvent<V>> afterCloseHandler);

    @FunctionalInterface
    interface RouteParametersProvider {

        /**
         * @return {@link RouteParameters} instance or {@code null}
         */
        @Nullable
        RouteParameters getRouteParameters();
    }

    @FunctionalInterface
    interface QueryParametersProvider {

        /**
         * @return {@link QueryParameters} instance or {@code null}
         */
        @Nullable
        QueryParameters getQueryParameters();
    }
}
