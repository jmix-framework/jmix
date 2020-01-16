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

package io.jmix.ui.sys.vaadin;

import com.vaadin.server.DefaultUIProvider;
import com.vaadin.server.UIProvider;
import com.vaadin.server.VaadinServletService;
import com.vaadin.server.VaadinSession;
import com.vaadin.spring.internal.UIScopeImpl;
import com.vaadin.spring.internal.VaadinSessionScope;
import com.vaadin.spring.server.SpringVaadinServlet;

import java.util.ArrayList;
import java.util.List;

// Exposes JmixUIProvider with customized widgetset lookup
public class JmixVaadinServlet extends SpringVaadinServlet {
    @Override
    protected void servletInitialized() {
        VaadinServletService service = getService();
        service.addSessionInitListener(sessionInitEvent -> {
            // remove DefaultUIProvider instances to avoid mapping
            // extraneous UIs if e.g. a servlet is declared as a nested class in a UI class
            VaadinSession session = sessionInitEvent.getSession();
            List<UIProvider> uiProviders = new ArrayList<>(session.getUIProviders());
            for (UIProvider provider : uiProviders) {
                // use canonical names as these may have been loaded with
                // different classloaders
                if (DefaultUIProvider.class.getCanonicalName().equals(
                        provider.getClass().getCanonicalName())) {
                    session.removeUIProvider(provider);
                }
            }

            // add JMix UI provider
            UIProvider uiProvider = new JmixUIProvider(session);
            session.addUIProvider(uiProvider);
        });
        service.addSessionDestroyListener(event -> {
            VaadinSession session = event.getSession();

            UIScopeImpl.cleanupSession(session);
            VaadinSessionScope.cleanupSession(session);
        });
    }
}