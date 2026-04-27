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

package io.jmix.securityflowui.security;

import com.google.common.base.Strings;
import com.vaadin.flow.spring.security.VaadinSecurityConfigurer;
import io.jmix.flowui.UiProperties;
import io.jmix.flowui.view.View;
import io.jmix.security.util.JmixHttpSecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

/**
 * Provides default Vaadin and Jmix FlowUI security to the project.
 */
public class FlowuiVaadinWebSecurity extends AbstractFlowuiWebSecurity {

    private static final Logger log = LoggerFactory.getLogger(FlowuiVaadinWebSecurity.class);

    @Autowired
    protected UiProperties uiProperties;

    @Override
    protected void configureVaadinSpecifics(HttpSecurity http) {
        http.with(VaadinSecurityConfigurer.vaadin(), this::initLoginView);
    }

    /**
     * Configures the {@link HttpSecurity} by adding Jmix-specific settings.
     */
    @Override
    protected void configureJmixSpecifics(HttpSecurity http) throws Exception {
        super.configureJmixSpecifics(http);
        JmixHttpSecurityUtils.configureRememberMe(http);
    }

    /**
     * Configures a login view by finding login view id in application properties.
     */
    protected void initLoginView(VaadinSecurityConfigurer configurer) {
        String loginViewId = uiProperties.getLoginViewId();
        if (Strings.isNullOrEmpty(loginViewId)) {
            log.debug("Login view Id is not defined");
            return;
        }
        Class<? extends View<?>> controllerClass =
                viewRegistry.getViewInfo(loginViewId).getControllerClass();
        configurer.loginView(controllerClass, getLogoutSuccessUrl());
    }

    protected String getLogoutSuccessUrl() {
        String contextPath = servletContext.getContextPath();
        return contextPath.startsWith("/") ? contextPath : "/" + contextPath;
    }
}
