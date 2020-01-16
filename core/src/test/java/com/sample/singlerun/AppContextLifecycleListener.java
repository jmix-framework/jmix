/*
 * Copyright 2019 Haulmont.
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

package com.sample.singlerun;

import io.jmix.core.event.AppContextInitializedEvent;
import io.jmix.core.event.AppContextStartedEvent;
import io.jmix.core.security.SystemUserSession;
import io.jmix.core.security.UserSessionSource;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@Component
public class AppContextLifecycleListener {

    List<ApplicationContextEvent> events = new ArrayList<>();

    @Inject
    private UserSessionSource userSessionSource;

    public List<ApplicationContextEvent> getEvents() {
        return events;
    }

    @EventListener
    void onRefreshed(ContextRefreshedEvent event) {
        events.add(event);
    }

    @EventListener
    void onInitialized(AppContextInitializedEvent event) {
        events.add(event);
        assert userSessionSource.getUserSession() instanceof SystemUserSession;
    }

    @EventListener
    void onStarted(AppContextStartedEvent event) {
        events.add(event);
        assert userSessionSource.getUserSession() instanceof SystemUserSession;
    }
}
