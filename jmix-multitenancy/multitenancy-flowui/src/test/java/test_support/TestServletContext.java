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

package test_support;

import org.springframework.mock.web.MockServletContext;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.EventListener;

/**
 * Mocked servlet context. Provides ability to add {@link ServletContextListener} and invoke them.
 *
 * @see com.vaadin.flow.spring.VaadinServletContextInitializer
 */
public class TestServletContext extends MockServletContext {

    protected EventListener eventListener;

    @Override
    public <T extends EventListener> void addListener(T listener) {
        eventListener = listener;
    }

    public void fireServletContextInitialized() {
        if (eventListener instanceof ServletContextListener) {
            ((ServletContextListener) eventListener).contextInitialized(new ServletContextEvent(this));
        }
    }
}
