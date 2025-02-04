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
import com.vaadin.flow.server.RequestHandler;
import com.vaadin.flow.server.ServiceException;
import com.vaadin.flow.server.VaadinServlet;
import com.vaadin.flow.server.communication.IndexHtmlRequestHandler;
import com.vaadin.flow.server.communication.JavaScriptBootstrapHandler;
import com.vaadin.flow.spring.SpringVaadinServletService;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.stream.Collectors;

public class TabbedModeVaadinServletService extends SpringVaadinServletService {

    protected ApplicationContext context;

    /**
     * Creates an instance connected to the given servlet and using the given
     * configuration with provided application {@code context}.
     *
     * @param servlet                 the servlet which receives requests
     * @param deploymentConfiguration the configuration to use
     * @param context                 the Spring application context
     */
    public TabbedModeVaadinServletService(VaadinServlet servlet,
                                          DeploymentConfiguration deploymentConfiguration,
                                          ApplicationContext context) {
        super(servlet, deploymentConfiguration, context);
        this.context = context;
    }

    @Override
    protected List<RequestHandler> createRequestHandlers() throws ServiceException {
        List<RequestHandler> handlers = super.createRequestHandlers();
        handlers = handlers.stream()
                .filter(requestHandler ->
                        !(requestHandler instanceof JavaScriptBootstrapHandler)
                                || requestHandler instanceof IndexHtmlRequestHandler
                ).collect(Collectors.toList());

        handlers.add(new TabbedModeJavaScriptBootstrapHandler(context));
        return handlers;
    }
}
