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

package io.jmix.tabbedmode.sys.vaadin;

import com.vaadin.flow.component.PushConfiguration;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.internal.BootstrapHandlerHelper;
import com.vaadin.flow.server.AppShellRegistry;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.communication.JavaScriptBootstrapHandler;
import com.vaadin.flow.shared.communication.PushMode;
import elemental.json.JsonObject;
import io.jmix.tabbedmode.JmixUI;
import org.springframework.context.ApplicationContext;

import java.util.Optional;

public class TabbedModeJavaScriptBootstrapHandler extends JavaScriptBootstrapHandler {

    protected ApplicationContext context;

    public TabbedModeJavaScriptBootstrapHandler(ApplicationContext context) {
        this.context = context;
    }

    @Override
    protected BootstrapContext createAndInitUI(Class<? extends UI> uiClass,
                                               VaadinRequest request,
                                               VaadinResponse response,
                                               VaadinSession session) {
        BootstrapContext context = createAndInitUIBootstrapHandler(JmixUI.class, request, response, session);
        context = initBootstrapContext(context, request, response, session);

        return context;
    }

    protected BootstrapContext createAndInitUIBootstrapHandler(Class<? extends UI> uiClass,
                                               VaadinRequest request, VaadinResponse response,
                                               VaadinSession session) {
        if (!JmixUI.class.isAssignableFrom(uiClass)) {
            throw new IllegalArgumentException("Provided UI class '%s' must extend '%s'"
                    .formatted(uiClass.getName(), JmixUI.class.getName()));
        }

        UI ui = context.getBean(uiClass);
        ui.getInternals().setContextRoot(
                request.getService().getContextRootRelativePath(request));

        PushConfiguration pushConfiguration = ui.getPushConfiguration();

        ui.getInternals().setSession(session);
        ui.setLocale(session.getLocale());

        BootstrapContext context = createBootstrapContext(request, response, ui,
                request.getService()::getContextRootRelativePath);

        Optional<Push> push = context
                .getPageConfigurationAnnotation(Push.class);

        DeploymentConfiguration deploymentConfiguration = context.getSession()
                .getService().getDeploymentConfiguration();
        PushMode pushMode = push.map(Push::value)
                .orElseGet(deploymentConfiguration::getPushMode);
        setupPushConnectionFactory(pushConfiguration, context);
        pushConfiguration.setPushMode(pushMode);
        pushConfiguration.setPushServletMapping(
                BootstrapHandlerHelper.determinePushServletMapping(session));

        push.map(Push::transport).ifPresent(pushConfiguration::setTransport);

        // Set thread local here so it is available in init
        UI.setCurrent(ui);
        ui.doInit(request, session.getNextUIid(), context.getAppId());
        session.addUI(ui);

        // After init and adding UI to session fire init listeners.
        session.getService().fireUIInitListeners(ui);

        initializeUIWithRouter(context, ui);

        return context;
    }

    protected BootstrapContext initBootstrapContext(BootstrapContext context,
                                                    VaadinRequest request,
                                                    VaadinResponse response,
                                                    VaadinSession session) {
        JsonObject config = context.getApplicationParameters();

        String requestURL = getRequestUrl(request);

        PushConfiguration pushConfiguration = context.getUI()
                .getPushConfiguration();
        pushConfiguration.setPushServletMapping(
                BootstrapHandlerHelper.determinePushServletMapping(session));

        AppShellRegistry registry = AppShellRegistry
                .getInstance(session.getService().getContext());
        registry.modifyPushConfiguration(pushConfiguration);

        config.put("requestURL", requestURL);

        return context;
    }
}
