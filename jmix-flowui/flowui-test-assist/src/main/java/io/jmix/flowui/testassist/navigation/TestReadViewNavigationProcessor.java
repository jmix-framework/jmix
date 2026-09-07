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

package io.jmix.flowui.testassist.navigation;

import com.vaadin.flow.function.SerializableConsumer;
import io.jmix.flowui.sys.ViewSupport;
import io.jmix.flowui.view.StandardReadView;
import io.jmix.flowui.view.ViewRegistry;
import io.jmix.flowui.view.navigation.*;

import java.net.URL;

/**
 * The main goal of this class is supporting backward navigation in UI integration tests.
 * <p>
 * The {@link ReadViewNavigationProcessor} is used for preparing and performing navigation to the
 * inheritor of {@link StandardReadView}.
 * <p>
 * In UI integration tests there is no client-side, so backward navigation URL should be got by another way.
 * This is why {@link TestReadViewNavigationProcessor} replaces {@link ReadViewNavigationProcessor} and
 * delegates building backward navigation URL to {@link ViewNavigationDelegate}.
 */
public class TestReadViewNavigationProcessor extends ReadViewNavigationProcessor {

    protected ViewNavigationDelegate<ReadViewNavigator<?>> navigationDelegate;

    public TestReadViewNavigationProcessor(ViewSupport viewSupport, ViewRegistry viewRegistry,
                                           ViewNavigationSupport navigationSupport, RouteSupport routeSupport,
                                           ViewNavigationDelegate<ReadViewNavigator<?>> navigationDelegate) {
        super(viewSupport, viewRegistry, navigationSupport, routeSupport);

        this.navigationDelegate = navigationDelegate;
    }

    @Override
    public void processNavigation(ReadViewNavigator<?> navigator) {
        super.processNavigation(navigator);

        navigationDelegate.saveCurrentNavigation(
                getViewClass(navigator),
                getRouteParameters(navigator),
                getQueryParameters(navigator));
    }

    @Override
    protected void fetchCurrentURL(SerializableConsumer<URL> callback) {
        callback.accept(navigationDelegate.fetchCurrentUrl());
    }
}
