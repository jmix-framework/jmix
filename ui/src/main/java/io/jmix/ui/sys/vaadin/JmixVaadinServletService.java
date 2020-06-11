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

package io.jmix.ui.sys.vaadin;

import com.vaadin.server.*;
import com.vaadin.server.communication.*;
import com.vaadin.spring.server.SpringVaadinServletService;
import io.jmix.core.AppBeans;
import io.jmix.core.Events;
import io.jmix.core.MessageTools;
import io.jmix.core.Messages;
import io.jmix.ui.App;
import io.jmix.ui.UiProperties;
import io.jmix.ui.sys.event.WebSessionDestroyedEvent;
import io.jmix.ui.sys.event.WebSessionInitializedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class JmixVaadinServletService extends SpringVaadinServletService
        implements AtmospherePushConnection.UidlWriterFactory {

    private static final Logger log = LoggerFactory.getLogger(JmixVaadinServletService.class);

    protected String serviceUrl;

    protected UiProperties uiProperties;

    protected boolean testMode;
    protected boolean performanceTestMode;

    protected Events events;
    protected Messages messages;


    public JmixVaadinServletService(VaadinServlet servlet,
                                    DeploymentConfiguration deploymentConfiguration, String serviceUrl)
            throws ServiceException {
        super(servlet, deploymentConfiguration, serviceUrl);

        this.events = AppBeans.get(Events.NAME);
        this.serviceUrl = serviceUrl;

        uiProperties = AppBeans.get(UiProperties.class);
        testMode = uiProperties.isTestMode();
        performanceTestMode = uiProperties.isPerformanceTestMode();

        this.messages = AppBeans.get(Messages.NAME);
        MessageTools messageTools = AppBeans.get(MessageTools.NAME);

        addSessionInitListener(event -> {
            WrappedSession wrappedSession = event.getSession().getSession();
            wrappedSession.setMaxInactiveInterval(uiProperties.getHttpSessionExpirationTimeoutSec());

            HttpSession httpSession = wrappedSession instanceof WrappedHttpSession ?
                    ((WrappedHttpSession) wrappedSession).getHttpSession() : null;

            log.debug("HttpSession {} initialized, timeout={}sec",
                    httpSession, wrappedSession.getMaxInactiveInterval());

            events.publish(new WebSessionInitializedEvent(event.getSession()));
        });

        addSessionDestroyListener(event -> {
            WrappedSession wrappedSession = event.getSession().getSession();
            HttpSession httpSession = wrappedSession instanceof WrappedHttpSession ?
                    ((WrappedHttpSession) wrappedSession).getHttpSession() : null;

            log.debug("HttpSession destroyed: {}", httpSession);
            App app = event.getSession().getAttribute(App.class);
            if (app != null) {
                app.cleanupBackgroundTasks();
            }

            events.publish(new WebSessionDestroyedEvent(event.getSession()));
        });

    }

    @Override
    public String getConfiguredTheme(VaadinRequest request) {
        return uiProperties.getTheme();
    }

    @Override
    protected List<RequestHandler> createRequestHandlers() throws ServiceException {
        List<RequestHandler> requestHandlers = super.createRequestHandlers();

        List<RequestHandler> jmixRequestHandlers = new ArrayList<>();

        ServletContext servletContext = getServlet().getServletContext();

        for (RequestHandler handler : requestHandlers) {
            if (handler instanceof UidlRequestHandler) {
                jmixRequestHandlers.add(new JmixUidlRequestHandler(servletContext));
            } else if (handler instanceof ServletBootstrapHandler) {
                // replace ServletBootstrapHandler with JmixServletBootstrapHandler
                jmixRequestHandlers.add(new JmixServletBootstrapHandler());
            } else if (handler instanceof HeartbeatHandler) {
                // replace HeartbeatHandler with JmixHeartbeatHandler
                jmixRequestHandlers.add(new JmixHeartbeatHandler());
            } else if (handler instanceof ServletUIInitHandler) {
                jmixRequestHandlers.add(new JmixServletUIInitHandler(servletContext));
            } else {
                jmixRequestHandlers.add(handler);
            }
        }

        jmixRequestHandlers.add(new JmixWebJarsHandler(servletContext));


        // replace bootstrap handler with a custom one if service URL set
        if (serviceUrl != null) {
            // need to keep the position of the handler on the list
            for (int i = 0; i < jmixRequestHandlers.size(); ++i) {
                if (jmixRequestHandlers.get(i) instanceof ServletBootstrapHandler) {
                    jmixRequestHandlers.set(i, new ServletBootstrapHandler() {
                        @Override
                        protected String getServiceUrl(BootstrapContext context) {
                            return context.getRequest().getContextPath()
                                    + serviceUrl;
                        }
                    });
                }
            }
        }

        return jmixRequestHandlers;
    }

    @Override
    public UidlWriter createUidlWriter() {
        return new JmixUidlWriter(getServlet().getServletContext());
    }

     /**
     * Add ability to redirect to base application URL if we have unparsable path tail
     */
    protected static class JmixServletBootstrapHandler extends ServletBootstrapHandler {
        @Override
        public boolean handleRequest(VaadinSession session, VaadinRequest request, VaadinResponse response)
                throws IOException {
            String requestPath = request.getPathInfo();

            // redirect to base URL if we have unparsable path tail
            if (!Objects.equals("/", requestPath)) {
                response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
                response.setHeader("Location", request.getContextPath());

                return true;
            }

            return super.handleRequest(session, request, response);
        }
    }

    // Add ability to handle heartbeats in App
    protected static class JmixHeartbeatHandler extends HeartbeatHandler {
        private final Logger log = LoggerFactory.getLogger(JmixHeartbeatHandler.class);

        @Override
        public boolean synchronizedHandleRequest(VaadinSession session, VaadinRequest request, VaadinResponse response)
                throws IOException {
            boolean result = super.synchronizedHandleRequest(session, request, response);

            if (log.isTraceEnabled()) {
                log.trace("Handle heartbeat {} {}", request.getRemoteHost(), request.getRemoteAddr());
            }

            if (result && App.isBound()) {
                App.getInstance().onHeartbeat();
            }

            return result;
        }
    }


    /*
     * Uses JmixUidlWriter instead of default UidlWriter to support reloading screens that contain components
     * that use web resources from WebJars
     */
    protected static class JmixServletUIInitHandler extends ServletUIInitHandler {
        protected final ServletContext servletContext;

        public JmixServletUIInitHandler(ServletContext servletContext) {
            this.servletContext = servletContext;
        }

        @Override
        protected UidlWriter createUidlWriter() {
            return new JmixUidlWriter(servletContext);
        }
    }

    /*
     * Uses JmixUidlWriter instead of default UidlWriter to support reloading screens that contain components
     * that use web resources from WebJars
     */
    protected static class JmixUidlRequestHandler extends UidlRequestHandler {
        protected final ServletContext servletContext;

        public JmixUidlRequestHandler(ServletContext servletContext) {
            this.servletContext = servletContext;
        }

        @Override
        protected UidlWriter createUidlWriter() {
            return new JmixUidlWriter(servletContext);
        }
    }

}