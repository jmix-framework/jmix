/*
 * Copyright 2020 Haulmont.
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

package com.haulmont.cuba.core.global.impl;

import com.haulmont.cuba.core.global.Events;
import com.haulmont.cuba.gui.events.UiEvent;
import io.jmix.ui.UiEventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component(Events.NAME)
public class EventsImpl implements Events {

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private UiEventPublisher uiEventPublisher;

    @Override
    public void publish(ApplicationEvent event) {
        // check if we have active UI
        if (event instanceof UiEvent) {
            uiEventPublisher.publishEvent(event);
        } else {
            applicationEventPublisher.publishEvent(event);
        }
    }
}
