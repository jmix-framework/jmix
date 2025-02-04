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

import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.server.ServiceException;
import com.vaadin.flow.server.VaadinServletService;
import com.vaadin.flow.spring.RootMappedCondition;
import com.vaadin.flow.spring.SpringServlet;
import com.vaadin.flow.spring.VaadinServletConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.mvc.ServletForwardingController;

//@WebServlet(urlPatterns = "/*", name = "JmixVaadinServlet", asyncSupported = true)
public class TabbedModeVaadinServlet extends SpringServlet {

    protected final ApplicationContext applicationContext;

    /**
     * Creates a new Vaadin servlet instance with the application
     * {@code context} provided.
     * <p>
     * Use {@code true} as a value for {@code forwardingEnforced} parameter if
     * your servlet is mapped to the root ({@code "/*"}). In the case of root
     * mapping a {@link RootMappedCondition} is checked and
     * {@link VaadinServletConfiguration} is applied conditionally. This
     * configuration provide a {@link ServletForwardingController} so that other
     * Spring endpoints may co-exist with Vaadin application (it's required
     * since root mapping handles any request to the context). This is not
     * needed if you are using non-root mapping since are you free to use the
     * mapping which doesn't overlap with any endpoint mapping. In this case use
     * {@code false} for the {@code forwardingEnforced} parameter.
     *
     * @param context     the Spring application context
     * @param rootMapping the incoming HttpServletRequest is wrapped in
     *                    ForwardingRequestWrapper if {@code true}
     */
    public TabbedModeVaadinServlet(ApplicationContext context, boolean rootMapping) {
        super(context, rootMapping);

        this.applicationContext = context;
    }

    @Override
    protected VaadinServletService createServletService(
            DeploymentConfiguration deploymentConfiguration) throws ServiceException {
        TabbedModeVaadinServletService servletService = new TabbedModeVaadinServletService(this,
                deploymentConfiguration, applicationContext);
        servletService.init();

        return servletService;
    }
}
