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
import com.vaadin.flow.spring.security.VaadinDefaultRequestCache;
import com.vaadin.flow.spring.security.VaadinSecurityConfigurer;
import io.jmix.core.H2ConsoleProperties;
import io.jmix.core.JmixSecurityFilterChainOrder;
import io.jmix.flowui.UiProperties;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewRegistry;
import io.jmix.security.configurer.JmixRequestCacheRequestMatcher;
import io.jmix.security.util.JmixHttpSecurityUtils;
import jakarta.servlet.ServletContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.Collections;
import java.util.List;

/**
 * Provides default Vaadin and Jmix FlowUI security to the project.
 */
public class FlowuiVaadinWebSecurity {

    private static final Logger log = LoggerFactory.getLogger(FlowuiVaadinWebSecurity.class);

    @Autowired
    protected UiProperties uiProperties;
    @Autowired
    protected ViewRegistry viewRegistry;
    @Autowired
    protected ServletContext servletContext;
    @Autowired(required = false)
    protected H2ConsoleProperties h2ConsoleProperties;

    protected List<JmixRequestCacheRequestMatcher> requestCacheRequestMatchers;

    @Autowired
    public void setVaadinDefaultRequestCache(VaadinDefaultRequestCache vaadinDefaultRequestCache,
                                             ObjectProvider<List<JmixRequestCacheRequestMatcher>> requestCacheRequestMatchersProvider) {
        // Configure request cache to do not save resource
        // requests as they are not valid redirect routes.
        this.requestCacheRequestMatchers = requestCacheRequestMatchersProvider.getIfAvailable(Collections::emptyList);
        vaadinDefaultRequestCache.setDelegateRequestCache(getDelegateRequestCache());
    }

    @Bean
    @Order(JmixSecurityFilterChainOrder.FLOWUI)
    SecurityFilterChain jmixSecurityFilterChain(HttpSecurity http) throws Exception {
        configureJmixSpecifics(http);
        configureVaadinSpecifics(http);

        return http.build();
    }

    protected void configureVaadinSpecifics(HttpSecurity http) throws Exception {
        http.with(VaadinSecurityConfigurer.vaadin(), this::initLoginView);
    }

    /**
     * Configures the {@link HttpSecurity} by adding Jmix-specific settings.
     */
    protected void configureJmixSpecifics(HttpSecurity http) throws Exception {
        // TODO: gg, convert to Configurer like Vaadin?
        JmixHttpSecurityUtils.configureAnonymous(http);
        JmixHttpSecurityUtils.configureSessionManagement(http);
        JmixHttpSecurityUtils.configureRememberMe(http);
        JmixHttpSecurityUtils.configureFrameOptions(http);

        /*http.authorizeHttpRequests(urlRegistry -> {
            //TODO [IVGA][SB4] SB4 should permit both request with and without params - check login with parameters
            String loginPath = getLoginPath();
            urlRegistry.requestMatchers(loginPath).permitAll();

            // Permit default Spring framework error page (/error)
            urlRegistry.dispatcherTypeMatchers(DispatcherType.ERROR).permitAll(); //TODO [IVGA][SB4] check
        });*/

        if (h2ConsoleProperties != null && h2ConsoleProperties.isEnabled()) {
            http.authorizeHttpRequests(registry ->
                    registry.requestMatchers(PathPatternRequestMatcher.pathPattern(h2ConsoleProperties.getPath() + "/**")));
        }
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

    protected RequestCache getDelegateRequestCache() {
        HttpSessionRequestCache cache = new HttpSessionRequestCache();
        cache.setRequestMatcher(createViewPathRequestMatcher(viewRegistry));
        return cache;
    }

    protected RequestMatcher createViewPathRequestMatcher(ViewRegistry viewRegistry) {
        return new JmixViewPathRequestMatcher(viewRegistry, requestCacheRequestMatchers);
    }
}