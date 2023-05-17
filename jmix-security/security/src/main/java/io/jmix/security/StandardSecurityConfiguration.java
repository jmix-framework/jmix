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

package io.jmix.security;

import io.jmix.core.JmixSecurityFilterChainOrder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static io.jmix.security.SecurityConfigurers.uiSecurity;

public class StandardSecurityConfiguration {

    public static final String SECURITY_CONFIGURER_QUALIFIER = "standard-security";

    //todo MG do we still need this SecurityFilterChain here?
    @Bean("sec_StandardSecurityFilterChain")
    @Order(JmixSecurityFilterChainOrder.STANDARD_SECURITY)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.apply(uiSecurity());
        http.logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/"));
        SecurityConfigurers.applySecurityConfigurersWithQualifier(http, SECURITY_CONFIGURER_QUALIFIER);
        return http.build();
    }
}
