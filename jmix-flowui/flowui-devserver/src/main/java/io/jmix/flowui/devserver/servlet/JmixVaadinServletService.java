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

import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.server.RequestHandler;
import com.vaadin.flow.server.ServiceException;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinServlet;
import com.vaadin.flow.server.VaadinServletService;
import com.vaadin.flow.server.communication.IndexHtmlRequestHandler;
import com.vaadin.flow.server.communication.JavaScriptBootstrapHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class JmixVaadinServletService extends VaadinServletService {

    private static final Logger log = LoggerFactory.getLogger(JmixVaadinServletService.class);

    public JmixVaadinServletService(VaadinServlet servlet, DeploymentConfiguration deploymentConfiguration) {
        super(servlet, deploymentConfiguration);
    }

    @Override
    protected List<RequestHandler> createRequestHandlers() throws ServiceException {
        List<RequestHandler> handlers = super.createRequestHandlers();
        handlers = handlers.stream()
                .filter(requestHandler ->
                        !(requestHandler instanceof JavaScriptBootstrapHandler)
                                || requestHandler instanceof IndexHtmlRequestHandler
                ).collect(Collectors.toList());

        handlers.add(new JmixJavaScriptBootstrapHandler());
        return handlers;
    }

    @Override
    public void handleRequest(VaadinRequest request, VaadinResponse response) {
        try {
            super.handleRequest(request, response);
        } catch (Exception e) {
            log.debug("Exception when handle request", e);
            // FrontendUtils.console("Exception when handle request:\n" + e);
        }
    }
}
