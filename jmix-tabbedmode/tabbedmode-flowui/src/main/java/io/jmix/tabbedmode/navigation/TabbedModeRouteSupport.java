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

import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.Location;
import io.jmix.flowui.UiEventPublisher;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.navigation.RouteSupport;
import io.jmix.flowui.view.navigation.UrlParamSerializer;
import io.jmix.tabbedmode.JmixUI;
import io.jmix.tabbedmode.component.breadcrumbs.ViewBreadcrumbs;
import io.jmix.tabbedmode.event.LocationChangeEvent;
import io.jmix.tabbedmode.view.TabbedModeViewUtils;
import jakarta.servlet.ServletContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component("tabmod_TabbedModeRouteSupport")
public class TabbedModeRouteSupport extends RouteSupport {

    private static final Logger log = LoggerFactory.getLogger(TabbedModeRouteSupport.class);

    protected final UiEventPublisher uiEventPublisher;

    public TabbedModeRouteSupport(UrlParamSerializer urlParamSerializer,
                                  ServletContext servletContext,
                                  UiEventPublisher uiEventPublisher) {
        super(urlParamSerializer, servletContext);

        this.uiEventPublisher = uiEventPublisher;
    }

    @Override
    protected void replaceStateInternal(UI ui, Location location, boolean callback) {
        super.replaceStateInternal(ui, location, callback);

        updateViewResolvedLocation(ui, location);
        fireLocationChangeEvent(ui, location);
    }

    protected void fireLocationChangeEvent(UI ui, Location location) {
        uiEventPublisher.publishEventForCurrentUI(new LocationChangeEvent(ui, location));
    }

    protected void updateViewResolvedLocation(UI ui, Location location) {
        if (!(ui instanceof JmixUI jmixUI)) {
            log.warn("Unable to update view's resolved location. UI is not an instance of {}", JmixUI.class.getName());
            return;
        }

        jmixUI.findCurrentView().ifPresent(view ->
                updateViewResolvedLocation(view, location));
    }

    protected void updateViewResolvedLocation(View<?> view, Location location) {
        if (UiComponentUtils.isComponentAttachedToDialog(view)) {
            log.debug("Unable to update view's resolved location. Dialog windows are opened");
            return;
        }

        TabbedModeViewUtils.findViewContainer(view).ifPresent(viewContainer -> {
            ViewBreadcrumbs breadcrumbs = viewContainer.getBreadcrumbs();
            if (breadcrumbs != null) {
                breadcrumbs.updateViewLocation(view, location);
            }
        });
    }
}
