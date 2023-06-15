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

package io.jmix.flowui.devserver;

import com.vaadin.flow.server.startup.ServletContextListeners;
import io.jmix.flowui.devserver.servlet.JmixErrorHandler;
import io.jmix.flowui.devserver.servlet.JmixServletContextListener;
import io.jmix.flowui.devserver.servlet.JmixSystemPropertiesLifeCycleListener;
import io.jmix.flowui.devserver.servlet.JmixVaadinServlet;
import org.eclipse.jetty.annotations.AnnotationConfiguration;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ShutdownHandler;
import org.eclipse.jetty.webapp.Configurations;
import org.eclipse.jetty.webapp.JettyWebXmlConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.websocket.jakarta.server.config.JakartaWebSocketServletContainerInitializer;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

/**
 * Used in Studio.
 */
@SuppressWarnings("unused")
public class FlowJettyServer extends Server {

    private final Map<String, Object> params;

    public FlowJettyServer(int port, Map<String, Object> params) {
        super(port);
        this.params = params;
    }

    public void initAndStart() throws Exception {
        init();
        start();
    }

    private void init() throws IOException {
        WebAppContext context = createContext();
        this.setHandler(
                new HandlerList(
                        context,
                        new ShutdownHandler("studio")
                )
        );
        this.addEventListener(
                new JmixSystemPropertiesLifeCycleListener(
                        (String) params.get("ProjectBaseDir"),
                        (String) params.get("IsPnpmEnabled"),
                        (Properties) params.get("Properties")
                )
        );
        Configurations
                .setServerDefault(this)
                .add(JettyWebXmlConfiguration.class.getName(), AnnotationConfiguration.class.getName());
    }

    private WebAppContext createContext() throws IOException {
        WebAppContext context = new WebAppContext();
        context.setContextPath("/");
        context.setClassLoader(FlowJettyServer.class.getClassLoader());
        context.setExtraClasspath((String) params.get("ExtraClassPath"));
        context.setResourceBase((String) params.get("ResourceBaseDir"));
        context.addServlet(JmixVaadinServlet.class, "/*");
        context.setConfigurationDiscovered(true);
        context.getServletContext().setExtendedListenerTypes(true);
        context.addEventListener(new ServletContextListeners());
        JakartaWebSocketServletContainerInitializer.configure(context, null);
        context.setErrorHandler(new JmixErrorHandler());
        context.addEventListener(new JmixServletContextListener(params));
        return context;
    }

    private FlowJettyServer() {
        this.params = Collections.emptyMap();
    }

}
