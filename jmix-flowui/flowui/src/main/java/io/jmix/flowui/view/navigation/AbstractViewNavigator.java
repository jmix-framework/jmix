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
import java.util.Optional;
import java.util.function.Consumer;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

/**
 * Provides a fluent interface to configure navigation parameters and navigate to a {@link View}.
 * <p>
 * An instance of this class should be obtained through {@link ViewNavigators#view(String)}.
 */
public abstract class AbstractViewNavigator {

    protected final Consumer<? extends AbstractViewNavigator> handler;

    protected String viewId;

    protected RouteParameters routeParameters;
    protected QueryParameters queryParameters;

    protected boolean backwardNavigation;

    protected AbstractViewNavigator(Consumer<? extends AbstractViewNavigator> handler) {
        checkNotNullArgument(handler);

        this.handler = handler;
    }

    protected AbstractViewNavigator(AbstractViewNavigator viewNavigator) {
        this.handler = viewNavigator.handler;
        this.viewId = viewNavigator.viewId;
        this.routeParameters = viewNavigator.routeParameters;
        this.queryParameters = viewNavigator.queryParameters;
        this.backwardNavigation = viewNavigator.backwardNavigation;
    }

    /**
     * Sets the opened view by id.
     *
     * @param viewId identifier of the view as specified in the {@code ViewController} annotation
     * @return this instance for chaining
     */
    public AbstractViewNavigator withViewId(@Nullable String viewId) {
        this.viewId = viewId;
        return this;
    }

    /**
     * Sets URL route parameters.
     *
     * @param routeParameters route parameters
     * @return this instance for chaining
     */
    public AbstractViewNavigator withRouteParameters(@Nullable RouteParameters routeParameters) {
        this.routeParameters = routeParameters;
        return this;
    }

    /**
     * Sets URL query parameters.
     *
     * @param queryParameters query parameters
     * @return this instance for chaining
     */
    public AbstractViewNavigator withQueryParameters(@Nullable QueryParameters queryParameters) {
        this.queryParameters = queryParameters;
        return this;
    }

    /**
     * Sets whether the current URL should be navigated to when the opened view is closed.
     *
     * @param backwardNavigation whether the current URL should be registered for backward navigation
     * @return this instance for chaining
     */
    public AbstractViewNavigator withBackwardNavigation(boolean backwardNavigation) {
        this.backwardNavigation = backwardNavigation;
        return this;
    }

    /**
     * @return identifier of the opened view as specified in the {@code ViewController} annotation
     */
    public Optional<String> getViewId() {
        return Optional.ofNullable(viewId);
    }

    /**
     * @return opened view class
     */
    public Optional<Class<? extends View>> getViewClass() {
        return Optional.empty();
    }

    /**
     * @return URL route parameters
     */
    public Optional<RouteParameters> getRouteParameters() {
        return Optional.ofNullable(routeParameters);
    }

    /**
     * @return URL query parameters
     */
    public Optional<QueryParameters> getQueryParameters() {
        return Optional.ofNullable(queryParameters);
    }

    /**
     * @return whether the current URL should be navigated to when the opened view is closed
     */
    public boolean isBackwardNavigation() {
        return backwardNavigation;
    }

    /**
     * Perform navigation to the view configured using {@link #withViewId(String)} or {@link #withViewClass(Class)}.
     */
    public void navigate() {
        ((Consumer<AbstractViewNavigator>) handler).accept(this);
    }
}
