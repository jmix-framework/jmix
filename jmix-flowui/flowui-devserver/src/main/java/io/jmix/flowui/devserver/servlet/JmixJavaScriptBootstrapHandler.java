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

import com.vaadin.flow.component.PushConfiguration;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.internal.BootstrapHandlerHelper;
import com.vaadin.flow.server.AppShellRegistry;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.communication.JavaScriptBootstrapHandler;
import com.vaadin.flow.shared.communication.PushMode;
import elemental.json.JsonObject;

import java.util.Optional;

public class JmixJavaScriptBootstrapHandler extends JavaScriptBootstrapHandler {

    public JmixJavaScriptBootstrapHandler() {
        super();
    }

    @Override
    protected BootstrapContext createAndInitUI(Class<? extends UI> uiClass,
                                               VaadinRequest request,
                                               VaadinResponse response,
                                               VaadinSession session) {
        UI ui = new JmixUI();
        ui.getInternals().setContextRoot(request.getService().getContextRootRelativePath(request));

        PushConfiguration pushConfiguration = ui.getPushConfiguration();

        ui.getInternals().setSession(session);
        ui.setLocale(session.getLocale());

        BootstrapContext context = createBootstrapContext(
                request,
                response,
                ui,
                vaadinRequest -> vaadinRequest.getService().getContextRootRelativePath(request)
        );

        Optional<Push> push = context.getPageConfigurationAnnotation(Push.class);

        setupPushConnectionFactory(pushConfiguration, context);
        pushConfiguration.setPushMode(PushMode.MANUAL);
        push.stream()
                .map(Push::transport)
                .forEach(pushConfiguration::setTransport);

        // Set thread local here so it is available in init
        UI.setCurrent(ui);
        ui.doInit(request, session.getNextUIid(), context.getAppId());
        session.addUI(ui);

        // After init and adding UI to session fire init listeners.
        session.getService().fireUIInitListeners(ui);

        initializeUIWithRouter(context, ui);

        JsonObject config = context.getApplicationParameters();

        String requestURL = getRequestUrl(request);

        context.getUI()
                .getPushConfiguration()
                .setPushServletMapping(BootstrapHandlerHelper.determinePushServletMapping(session));

        AppShellRegistry registry = AppShellRegistry.getInstance(session.getService().getContext());
        registry.modifyPushConfiguration(pushConfiguration);

        config.put("requestURL", requestURL);

        return context;
    }
}
