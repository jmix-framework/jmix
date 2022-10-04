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

package test_support;

import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.server.ServiceException;
import com.vaadin.flow.server.VaadinServletService;
import com.vaadin.flow.spring.SpringServlet;
import org.springframework.context.ApplicationContext;

public class TestSpringServlet extends SpringServlet {

    protected ApplicationContext applicationContext;

    public TestSpringServlet(ApplicationContext context, boolean rootMapping) {
        super(context, rootMapping);

        applicationContext = context;
    }

    @Override
    protected VaadinServletService createServletService(DeploymentConfiguration deploymentConfiguration)
            throws ServiceException {
        TestSpringVaadinServletService service =
                new TestSpringVaadinServletService(this, deploymentConfiguration, applicationContext);
        service.init();
        return service;
    }
}
