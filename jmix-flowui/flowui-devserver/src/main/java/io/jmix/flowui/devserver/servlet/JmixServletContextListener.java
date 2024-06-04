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

package io.jmix.flowui.devserver.servlet;

import java.util.HashMap;
import java.util.Map;

import com.vaadin.flow.server.startup.ServletContextListeners;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

public class JmixServletContextListener implements ServletContextListener {

    private final Map<String, Object> params = new HashMap<>();
    private final ServletContextListeners listeners = new ServletContextListeners();

    // do not delete
    public JmixServletContextListener() {
    }

    public JmixServletContextListener(Map<String, Object> params) {
        this.params.putAll(params);
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        params.forEach((key, value) -> sce.getServletContext().setAttribute(key, value));
        listeners.contextInitialized(sce);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        listeners.contextDestroyed(sce);
    }
}
