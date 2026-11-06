/*
 * Copyright 2026 Haulmont.
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

package io.jmix.flowui.sys;

import com.vaadin.flow.server.VaadinServlet;
import io.jmix.core.annotation.Internal;
import org.atmosphere.cpr.ApplicationConfig;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.web.servlet.ServletRegistrationBean;

/**
 * Stops the server push endpoint from returning permissive CORS headers to cross-origin requests
 * (CVE-2026-110737).
 * <p>
 * Sets the {@value ApplicationConfig#DROP_ACCESS_CONTROL_ALLOW_ORIGIN_HEADER} init parameter to {@code true} on
 * every Vaadin servlet registered through a {@link ServletRegistrationBean} bean: the default Vaadin servlet, the
 * tabbed mode servlet, or a servlet declared with {@code @WebServlet}. A Vaadin servlet registered in another way,
 * for example in {@code web.xml}, does not get the parameter.
 * <p>
 * Push keeps working for the application's own origin. An application that uses push from another origin can
 * restore the previous behavior by setting the same init parameter to {@code false} on the Vaadin servlet
 * registration, for example in its own {@link BeanPostProcessor}. The parameter is not changed if the registration
 * already has it.
 */
@Internal
public class PushCorsServletRegistrationPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        if (bean instanceof ServletRegistrationBean<?> registration
                && registration.getServlet() instanceof VaadinServlet
                && !registration.getInitParameters().containsKey(
                ApplicationConfig.DROP_ACCESS_CONTROL_ALLOW_ORIGIN_HEADER)) {
            registration.addInitParameter(ApplicationConfig.DROP_ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, "true");
        }
        return bean;
    }
}
