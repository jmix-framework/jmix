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

package jmix_ui_test_extension.test_support;

import com.vaadin.flow.internal.DevModeHandlerManager;
import com.vaadin.flow.server.startup.ApplicationConfigurationFactory;
import com.vaadin.flow.spring.SpringApplicationConfigurationFactory;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Lets a UI test run Vaadin in development mode the way an application does, with a mock
 * {@link DevModeHandlerManager} in place of the real dev server, so the test can check how the dev-mode handler is
 * started and stopped.
 */
@Configuration
public class TestDevModeConfiguration {

    // Registered by Vaadin auto-configuration in an application; it makes the 'vaadin.productionMode' property
    // override the production mode that JmixUiTestExtension sets on the servlet context.
    @Bean
    public ApplicationConfigurationFactory applicationConfigurationFactory() {
        return new SpringApplicationConfigurationFactory();
    }

    @Bean
    public DevModeHandlerManager devModeHandlerManager() {
        DevModeHandlerManager manager = Mockito.mock(DevModeHandlerManager.class);
        Mockito.when(manager.getHandlesTypes()).thenReturn(new Class<?>[0]);
        return manager;
    }
}
