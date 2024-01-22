/*
 * Copyright 2023 Haulmont.
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

package io.jmix.flowui.testassist;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * DevModeServletContextListener starts node tasks using CompletableFuture. But sometimes
 * the thread of {@link CompletableFuture} seems is not started. It leads to hanging tests in the application.
 * Since Vaadin 24.1 it is not enough do disable DevModeServletContextListener using:
 * <pre>
 *     servletContext.setInitParameter(InitParameters.SERVLET_PARAMETER_PRODUCTION_MODE, "true");
 * </pre>
 * So we register context initializer to add {@code vaadin.productionMode=true} property.
 */
public class UiTestContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    protected static final String PRODUCTION_MODE_ATTRIBUTE = "vaadin.productionMode";
    protected static final String WEBSOCKET_REQUEST_SECURITY_CONTEXT_PROVIDED_ATTRIBUTE
            = "jmix.ui.websocket-request-security-context-provided";

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        Map<String, String> properties = new HashMap<>(2);

        // Do not override value of production mode if it's added to environment.
        if (!applicationContext.getEnvironment().containsProperty(PRODUCTION_MODE_ATTRIBUTE)) {
            properties.put(PRODUCTION_MODE_ATTRIBUTE, "true");
        }

        // Disable SecurityContextHolderAtmosphereInterceptor since it
        // produces NPE and moreover cannot be used in integration tests.
        properties.put(WEBSOCKET_REQUEST_SECURITY_CONTEXT_PROVIDED_ATTRIBUTE, "false");

        TestPropertyValues.of(properties)
                .applyTo(applicationContext.getEnvironment());
    }
}
