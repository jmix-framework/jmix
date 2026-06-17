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

package io.jmix.securityflowui.security;

import io.jmix.core.H2ConsoleProperties;
import io.jmix.core.JmixSecurityFilterChainOrder;
import io.jmix.security.util.JmixHttpSecurityUtils;
import jakarta.servlet.ServletContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

public abstract class AbstractFlowuiWebSecurity {

    @Autowired
    protected ServletContext servletContext;
    @Autowired(required = false)
    protected H2ConsoleProperties h2ConsoleProperties;
    @Autowired
    protected WebProperties webProperties;

    @Bean("jmixSecurityFilterChain")
    @Order(JmixSecurityFilterChainOrder.FLOWUI)
    public SecurityFilterChain jmixSecurityFilterChain(HttpSecurity http) throws Exception {
        configureJmixSpecifics(http);
        configureVaadinSpecifics(http);
        configureCustomSpecifics(http);
        return http.build();
    }

    protected void configureCustomSpecifics(HttpSecurity http) {
        // Hook to be implemented...
    }

    protected void configureJmixSpecifics(HttpSecurity http) throws Exception {
        JmixHttpSecurityUtils.configureAnonymous(http);
        JmixHttpSecurityUtils.configureSessionManagement(http);
        JmixHttpSecurityUtils.configureFrameOptions(http);

        http.authorizeHttpRequests(registry -> {
            registry.requestMatchers(
                    PathPatternRequestMatcher.pathPattern(webProperties.getError().getPath())
            ).permitAll();
        });

        if (h2ConsoleProperties != null && h2ConsoleProperties.isEnabled()) {
            http.authorizeHttpRequests(registry ->
                    registry.requestMatchers(PathPatternRequestMatcher.pathPattern(h2ConsoleProperties.getPath() + "/**"))
                            .permitAll());
        }
    }

    protected abstract void configureVaadinSpecifics(HttpSecurity http) throws Exception;
}
