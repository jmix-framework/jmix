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

import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import io.jmix.flowui.devserver.servlet.JmixErrorHandler;
import io.jmix.flowui.devserver.servlet.JmixServletContextListener;
import io.jmix.flowui.devserver.servlet.JmixSystemPropertiesLifeCycleListener;
import org.eclipse.jetty.ee10.annotations.AnnotationConfiguration;
import org.eclipse.jetty.ee10.webapp.Configurations;
import org.eclipse.jetty.ee10.webapp.JettyWebXmlConfiguration;
import org.eclipse.jetty.ee10.webapp.WebAppContext;
import org.eclipse.jetty.ee10.websocket.jakarta.server.config.JakartaWebSocketServletContainerInitializer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ShutdownHandler;

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
        // uncomment to debug server state
        // FrontendUtils.console(FrontendUtils.BRIGHT_BLUE, dump(), false);
        start();
    }

    private void init() {
        WebAppContext context = createContext();
        this.setHandler(
                new Sequence(
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
                .add(new AnnotationConfiguration(), new JettyWebXmlConfiguration());
    }

    private WebAppContext createContext() {
        WebAppContext context = new WebAppContext();
        context.setContextPath("/");
        context.setConfigurationDiscovered(true);
        context.getContext().setExtendedListenerTypes(true);
        context.setClassLoader(FlowJettyServer.class.getClassLoader());
        context.setParentLoaderPriority(true);
        context.setExtraClasspath((String) params.get("ExtraClassPath"));
        context.setBaseResourceAsString((String) params.get("ResourceBaseDir"));

        JakartaWebSocketServletContainerInitializer.configure(context, null);

        context.addEventListener(new JmixServletContextListener(params));
        context.setErrorHandler(new JmixErrorHandler());

        return context;
    }

    private FlowJettyServer() {
        this.params = Collections.emptyMap();
    }
}
