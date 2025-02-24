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

public class RedirectHandler {

    private static final Logger log = LoggerFactory.getLogger(RedirectHandler.class);

    protected final JmixUI ui;
    protected final Views views;

    protected Location redirect;

    public RedirectHandler(JmixUI ui, Views views) {
        this.ui = ui;
        this.views = views;
    }

    public void schedule(Location redirect) {
        Preconditions.checkNotNullArgument(redirect);

        this.redirect = redirect;
    }

    public boolean scheduled() {
        return redirect != null;
    }

    public void redirect() {
        if (redirect == null) {
            log.debug("Unable to redirect. Redirect location not set");
            return;
        }

        openView(redirect);

        redirect = null;
    }

    // TODO: gg, move to util
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
