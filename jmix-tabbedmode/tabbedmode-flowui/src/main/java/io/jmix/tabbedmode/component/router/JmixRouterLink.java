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

package io.jmix.tabbedmode.component.router;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.VaadinSession;
import io.jmix.flowui.sys.event.UiEventsManager;
import io.jmix.tabbedmode.event.LocationChangeEvent;
import org.springframework.context.ApplicationEvent;

import java.util.Collections;
import java.util.Optional;

public class JmixRouterLink extends RouterLink {

    public JmixRouterLink() {
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        uiEventsManager().ifPresent(uiEventsManager -> {
            uiEventsManager.removeApplicationListeners(this);
            uiEventsManager.addApplicationListener(this, this::onApplicationEvent);
        });
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);

        uiEventsManager().ifPresent(uiEventsManager ->
                uiEventsManager.removeApplicationListeners(this));
    }

    protected void onApplicationEvent(ApplicationEvent applicationEvent) {
        if (applicationEvent instanceof LocationChangeEvent locationChangeEvent) {
            onLocationChanged(locationChangeEvent);
        }
    }

    protected void onLocationChanged(LocationChangeEvent locationChangeEvent) {
        getHighlightAction().highlight(this, getHighlightCondition()
                .shouldHighlight(this, convertToAfterNavigationEvent(locationChangeEvent)));
    }

    protected AfterNavigationEvent convertToAfterNavigationEvent(LocationChangeEvent locationChangeEvent) {
        UI ui = locationChangeEvent.getSource();
        Router router = ui.getInternals().getRouter();
        Location location = locationChangeEvent.getLocation();

        com.vaadin.flow.router.LocationChangeEvent vaadinLocationChangeEvent =
                new com.vaadin.flow.router.LocationChangeEvent(router, ui,
                        NavigationTrigger.PROGRAMMATIC,
                        location, Collections.emptyList());

        return new AfterNavigationEvent(vaadinLocationChangeEvent);
    }

    protected Optional<UiEventsManager> uiEventsManager() {
        VaadinSession session = VaadinSession.getCurrent();
        return session != null
                ? Optional.ofNullable(session.getAttribute(UiEventsManager.class))
                : Optional.empty();
    }
}
