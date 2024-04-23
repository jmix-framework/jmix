/*
 * Copyright 2024 Haulmont.
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

import com.vaadin.flow.spring.SpringSecurityAutoConfiguration;
import com.vaadin.flow.spring.security.NavigationAccessControlConfigurer;
import io.jmix.core.CoreConfiguration;
import io.jmix.data.DataConfiguration;
import io.jmix.flowui.sys.UiAccessChecker;
import io.jmix.security.SecurityConfiguration;
import io.jmix.securityflowui.SecurityFlowuiConfiguration;
import io.jmix.securityflowui.access.JmixNavigationAccessChecker;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * Auto-configuration provides a default configurer for Vaadin
 * {@link com.vaadin.flow.server.auth.NavigationAccessControl}. The auto-configuration must be applied before
 * {@link SpringSecurityAutoConfiguration} from Vaadin.
 */
@AutoConfiguration
@Import({CoreConfiguration.class, DataConfiguration.class, SecurityConfiguration.class, SecurityFlowuiConfiguration.class})
@AutoConfigureBefore(SpringSecurityAutoConfiguration.class)
public class NavigationAccessControlAutoConfiguration {

    @Bean("flowui_NavigationAccessControlConfigurer")
    @ConditionalOnMissingBean
    static NavigationAccessControlConfigurer navigationAccessControlConfigurer(UiAccessChecker uiAccessChecker) {
        return new NavigationAccessControlConfigurer()
                .withNavigationAccessChecker(new JmixNavigationAccessChecker(uiAccessChecker));
    }

}
