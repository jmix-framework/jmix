/*
 * Copyright 2020 Haulmont.
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

package io.jmix.autoconfigure.securityflowui;

import com.vaadin.flow.spring.security.VaadinWebSecurity;
import io.jmix.core.CoreConfiguration;
import io.jmix.data.DataConfiguration;
import io.jmix.security.SecurityConfiguration;
import io.jmix.securityflowui.SecurityFlowuiConfiguration;
import io.jmix.securityflowui.security.FlowuiVaadinWebSecurity;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.context.DelegatingSecurityContextRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

@AutoConfiguration
@Import({CoreConfiguration.class, DataConfiguration.class, SecurityConfiguration.class, SecurityFlowuiConfiguration.class})
public class SecurityFlowuiAutoConfiguration {

    @EnableWebSecurity
    @Configuration
    @ConditionalOnProperty(name = "jmix.flowui.use-default-security-configuration", matchIfMissing = true)
    @ConditionalOnMissingBean(VaadinWebSecurity.class)
    public static class DefaultFlowuiVaadinWebSecurity extends FlowuiVaadinWebSecurity {}

    @Bean("flowui_SecurityContextRepository")
    SecurityContextRepository securityContextRepository() {
        return new DelegatingSecurityContextRepository(
                new RequestAttributeSecurityContextRepository(),
                new HttpSessionSecurityContextRepository()
        );
    }
}
