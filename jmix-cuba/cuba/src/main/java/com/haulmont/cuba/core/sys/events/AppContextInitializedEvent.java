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

package com.haulmont.cuba.core.sys.events;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.sys.AppContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ApplicationContextEvent;

/**
 * Application lifecycle event.
 * <p>
 * Published right after initialization of Spring context. {@link AppContext} and {@link AppBeans} can be used
 * in the handlers of this event.
 *
 * @see AppContextStartedEvent
 */
public class AppContextInitializedEvent extends ApplicationContextEvent {

    public AppContextInitializedEvent(ApplicationContext source) {
        super(source);
    }
}