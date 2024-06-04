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
import com.vaadin.flow.internal.AnnotationReader;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.internal.RouteUtil;
import com.vaadin.flow.server.VaadinServletContext;
import com.vaadin.flow.spring.security.VaadinWebSecurity;
import io.jmix.flowui.UiProperties;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewRegistry;
import io.jmix.security.configurer.AnonymousConfigurer;
import io.jmix.security.configurer.RememberMeConfigurer;
import io.jmix.security.configurer.SessionManagementConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import java.util.Optional;

/**
 * Provides default Vaadin and Jmix FlowUI security to the project.
 */
public class FlowuiVaadinWebSecurity extends VaadinWebSecurity {

    private static Logger log = LoggerFactory.getLogger(FlowuiVaadinWebSecurity.class);

    protected UiProperties uiProperties;
    protected ViewRegistry viewRegistry;
    protected ApplicationContext applicationContext;
    protected ServerProperties serverProperties;

    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Autowired
    public void setUiProperties(UiProperties uiProperties) {
        this.uiProperties = uiProperties;
    }

    @Autowired
    public void setViewRegistry(ViewRegistry viewRegistry) {
        this.viewRegistry = viewRegistry;
    }

    @Autowired
    public void setServerProperties(ServerProperties serverProperties) {
        this.serverProperties = serverProperties;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //apply Jmix configuration
        configureJmixSpecifics(http);

        //apply Vaadin configuration
        super.configure(http);
    }

    /**
     * Configures the {@link HttpSecurity} by adding Jmix-specific settings.
     */
    protected void configureJmixSpecifics(HttpSecurity http) throws Exception {
        http.with(new AnonymousConfigurer(), Customizer.withDefaults());
        http.with(new SessionManagementConfigurer(), Customizer.withDefaults());
        http.with(new RememberMeConfigurer(), Customizer.withDefaults());

        http.authorizeHttpRequests(urlRegistry -> {
            //We need such request matcher here in order to permit access to login page when a query parameter is passed.
            //For example, in case of using the multi-tenancy add-on we need to pass the query parameter: /login?tenantId=mytenant
            //By default, only access to /login is allowed and access to /login?someParam=someVal is blocked. The request
            //matcher below allows access to login view with any query parameter.
            String loginPath = getLoginPath();
            urlRegistry.requestMatchers(request -> loginPath.equals(request.getRequestURI())).permitAll();

            // Permit default Spring framework error page (/error)
            MvcRequestMatcher.Builder mvcRequestMatcherBuilder = new MvcRequestMatcher.Builder(applicationContext.getBean(HandlerMappingIntrospector.class));
            MvcRequestMatcher errorPageRequestMatcher = mvcRequestMatcherBuilder.pattern(serverProperties.getError().getPath());
            urlRegistry.requestMatchers(errorPageRequestMatcher).permitAll();
        });

        initLoginView(http);
    }

    /**
     * Configures login view by finding login view id in application properties.
     */
    protected void initLoginView(HttpSecurity http) throws Exception {
        String loginViewId = uiProperties.getLoginViewId();
        if (Strings.isNullOrEmpty(loginViewId)) {
            log.debug("Login view Id is not defined");
            return;
        }
        Class<? extends View<?>> controllerClass =
                viewRegistry.getViewInfo(loginViewId).getControllerClass();
        setLoginView(http, controllerClass, "/");
    }

    protected String getLoginPath() {
        String loginViewId = uiProperties.getLoginViewId();
        Class<? extends View<?>> loginViewClass =
                viewRegistry.getViewInfo(loginViewId).getControllerClass();

        Optional<Route> route = AnnotationReader.getAnnotationFor(loginViewClass, Route.class);

        if (route.isEmpty()) {
            throw new IllegalArgumentException(
                    "Unable find a @Route annotation on the login view "
                            + loginViewClass.getName());
        }

        if (!(applicationContext instanceof WebApplicationContext)) {
            throw new RuntimeException(
                    "VaadinWebSecurity cannot be used without WebApplicationContext.");
        }

        VaadinServletContext vaadinServletContext = new VaadinServletContext(
                ((WebApplicationContext) applicationContext).getServletContext());
        String loginPath = RouteUtil.getRoutePath(vaadinServletContext, loginViewClass);
        if (!loginPath.startsWith("/")) {
            loginPath = "/" + loginPath;
        }
        loginPath = applyUrlMapping(loginPath);

        return loginPath;
    }

    /**
     * Temporary workaround until https://github.com/vaadin/flow/issues/19075 is fixed
     */
    @Override
    protected void configure(WebSecurity web) throws Exception {
        super.configure(web);
        web.ignoring().requestMatchers(new AntPathRequestMatcher("/VAADIN/push/**"));
    }
}