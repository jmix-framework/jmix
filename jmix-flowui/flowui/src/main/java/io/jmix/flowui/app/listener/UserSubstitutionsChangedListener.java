/*
 * Copyright 2023 Haulmont.
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

package io.jmix.flowui.app.listener;

import io.jmix.core.usersubstitution.event.UiUserSubstitutionsChangedEvent;
import io.jmix.core.usersubstitution.event.UserSubstitutionsChangedEvent;
import io.jmix.flowui.UiEventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Publishes a {@link UiUserSubstitutionsChangedEvent} for the UI scope.
 * The event should be handled in the components or the views.
 */
@Component("flowui_UserSubstitutionsChangedListener")
public class UserSubstitutionsChangedListener {

    @Autowired
    protected UiEventPublisher uiEventPublisher;

    @EventListener
    public void onUserSubstitutionsChanged(UserSubstitutionsChangedEvent event) {
        uiEventPublisher.publishEvent(new UiUserSubstitutionsChangedEvent(event.getSource()));
    }
}