/*
 * Copyright 2025 Haulmont.
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

package io.jmix.tabbedmode.navigation;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.Location;
import com.vaadin.flow.router.NavigationState;
import io.jmix.core.common.util.Preconditions;
import io.jmix.flowui.view.View;
import io.jmix.tabbedmode.JmixUI;
import io.jmix.tabbedmode.Views;
import io.jmix.tabbedmode.builder.ViewOpeningContext;
import io.jmix.tabbedmode.view.ViewOpenMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A helper class for redirecting to a view represented by a {@link Location}.
 */
public class RedirectHandler {

    private static final Logger log = LoggerFactory.getLogger(RedirectHandler.class);

    protected final JmixUI ui;
    protected final Views views;

    protected Location redirectLocation;

    public RedirectHandler(JmixUI ui, Views views) {
        this.ui = ui;
        this.views = views;
    }

    /**
     * Schedules a redirect to the given location.
     *
     * @param redirectLocation a location to redirect to
     */
    public void schedule(Location redirectLocation) {
        Preconditions.checkNotNullArgument(redirectLocation);

        this.redirectLocation = redirectLocation;
    }

    /**
     * Whether the redirect is scheduled.
     *
     * @return {@code true} if the redirect is scheduled, {@code false} otherwise.
     */
    public boolean scheduled() {
        return redirectLocation != null;
    }

    /**
     * Performs redirect to the previously scheduled location.
     */
    public void redirect() {
        if (redirectLocation == null) {
            log.debug("Unable to redirect. Redirect location not set");
            return;
        }

        openView(redirectLocation);

        redirectLocation = null;
    }

    protected void openView(Location location) {
        NavigationState navigationState = UI.getCurrent().getInternals()
                .getRouter().resolveNavigationTarget(location).orElse(null);

        if (navigationState == null) {
            return;
        }

        Class<? extends Component> navigationTarget = navigationState.getNavigationTarget();
        if (!View.class.isAssignableFrom(navigationTarget)) {
            throw new IllegalArgumentException("'navigationTarget' is not a "
                    + View.class.getSimpleName());
        }

        //noinspection unchecked
        Class<? extends View<?>> viewClass = (Class<? extends View<?>>) navigationTarget;
        View<?> view = views.create(viewClass);

        views.open(ui, ViewOpeningContext.create(view, ViewOpenMode.NEW_TAB)
                .withRouteParameters(navigationState.getRouteParameters())
                .withQueryParameters(location.getQueryParameters()));
    }
}
