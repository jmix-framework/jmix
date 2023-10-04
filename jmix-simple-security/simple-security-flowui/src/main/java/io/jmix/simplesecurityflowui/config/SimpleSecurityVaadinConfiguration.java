/*
 * Copyright 2022 Haulmont.
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

package io.jmix.simplesecurityflowui.config;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.internal.AnnotationReader;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.internal.RouteUtil;
import com.vaadin.flow.server.HandlerHelper;
import com.vaadin.flow.server.VaadinServletContext;
import com.vaadin.flow.server.auth.ViewAccessChecker;
import com.vaadin.flow.spring.VaadinConfigurationProperties;
import com.vaadin.flow.spring.security.*;
import com.vaadin.flow.spring.security.stateless.VaadinStatelessSecurityConfigurer;
import io.jmix.flowui.UiProperties;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewRegistry;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.access.DelegatingAccessDeniedHandler;
import org.springframework.security.web.access.RequestMatcherDelegatingAccessDeniedHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.context.WebApplicationContext;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This configuration is mostly a copy of Vaadin {@link VaadinWebSecurity} with Jmix-related modifications.
 */
public class SimpleSecurityVaadinConfiguration {

    @Autowired
    private VaadinDefaultRequestCache vaadinDefaultRequestCache;

    @Autowired
    private RequestUtil requestUtil;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ViewAccessChecker viewAccessChecker;

    @Autowired
    private VaadinConfigurationProperties vaadinConfigurationProperties;

    @Autowired
    private UiProperties uiProperties;

    @Autowired
    private ViewRegistry viewRegistry;

    @Value("#{servletContext.contextPath}")
    private String servletContextPath;

    /**
     * Registers default {@link SecurityFilterChain} bean.
     * <p>
     * Defines a filter chain which is capable of being matched against an {@code HttpServletRequest}. in order to
     * decide whether it applies to that request.
     * <p>
     * {@link HttpSecurity} configuration can be customized by overriding {@link #configure(HttpSecurity)}.
     */
    @Bean(name = "VaadinSecurityFilterChainBean")
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        configure(http);
        return http.build();
    }


    /**
     * Applies Vaadin default configuration to {@link HttpSecurity}.
     * <p>
     * Typically, subclasses should call super to apply default Vaadin configuration in addition to custom rules.
     *
     * @param http the {@link HttpSecurity} to modify
     * @throws Exception if an error occurs
     */
    protected void configure(HttpSecurity http) throws Exception {
        // Respond with 401 Unauthorized HTTP status code for unauthorized
        // requests for protected Hilla endpoints, so that the response could
        // be handled on the client side using e.g. `InvalidSessionMiddleware`.
        http.exceptionHandling(exceptionHandling ->
                exceptionHandling
                        .accessDeniedHandler(createAccessDeniedHandler())
                        .defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                requestUtil::isEndpointRequest)
        );

        // Vaadin has its own CSRF protection.
        // Spring CSRF is not compatible with Vaadin internal requests
        http.csrf(csrf -> csrf.disable());

        // Ensure automated requests to e.g. closing push channels, service
        // workers,
        // endpoints are not counted as valid targets to redirect user to on
        // login
        http.requestCache(requestCache -> requestCache.requestCache(vaadinDefaultRequestCache));

        http.authorizeHttpRequests(httpRequests -> {
            // Vaadin internal requests must always be allowed to allow public Flow
            // pages
            // and/or login page implemented using Flow.
            httpRequests.requestMatchers(requestUtil::isFrameworkInternalRequest)
                    .permitAll();
            // Public endpoints are OK to access
            httpRequests.requestMatchers(requestUtil::isAnonymousEndpoint)
                    .permitAll();
            // Public routes are OK to access
            httpRequests.requestMatchers(requestUtil::isAnonymousRoute).permitAll();
            httpRequests.requestMatchers(getDefaultHttpSecurityPermitMatcher(
                    getUrlMapping())).permitAll();

            // matcher for Vaadin static (public) resources
            httpRequests.requestMatchers(
                            getDefaultWebSecurityIgnoreMatcher(getUrlMapping()))
                    .permitAll();

            // all other requests require authentication
            httpRequests.anyRequest().authenticated();
        });

        // Enable view access control
        viewAccessChecker.enable();

        initLoginView(http);
    }

    protected void initLoginView(HttpSecurity http) throws Exception {
        String loginViewId = uiProperties.getLoginViewId();
        Class<? extends View<?>> controllerClass =
                viewRegistry.getViewInfo(loginViewId).getControllerClass();
        setLoginView(http, controllerClass);
    }

    /**
     * Matcher for framework internal requests, with Vaadin servlet mapped on the given path.
     *
     * @param urlMapping url mapping for the Vaadin servlet.
     * @return default {@link HttpSecurity} bypass matcher
     */
    public static RequestMatcher getDefaultHttpSecurityPermitMatcher(
            String urlMapping) {
        Objects.requireNonNull(urlMapping,
                "Vaadin servlet url mapping is required");
        Stream.Builder<String> paths = Stream.builder();
        Stream.of(HandlerHelper.getPublicResourcesRequiringSecurityContext())
                .map(path -> applyUrlMapping(urlMapping, path))
                .forEach(paths::add);

        return new OrRequestMatcher(paths.build()
                .map(AntPathRequestMatcher::new).collect(Collectors.toList()));
    }

    /**
     * Matcher for Vaadin static (public) resources, with Vaadin servlet mapped on the given path.
     * <p>
     * Assumes Vaadin servlet to be mapped on root path ({@literal /*}).
     *
     * @param urlMapping the url mapping for the Vaadin servlet
     * @return default {@link WebSecurity} ignore matcher
     */
    public static RequestMatcher getDefaultWebSecurityIgnoreMatcher(
            String urlMapping) {
        Objects.requireNonNull(urlMapping,
                "Vaadin servlet url mapping is required");
        Stream<String> mappingRelativePaths = Stream
                .of(HandlerHelper.getPublicResources())
                .map(path -> applyUrlMapping(urlMapping, path));
        Stream<String> rootPaths = Stream
                .of(HandlerHelper.getPublicResourcesRoot());
        return new OrRequestMatcher(Stream
                .concat(mappingRelativePaths, rootPaths)
                .map(AntPathRequestMatcher::new).collect(Collectors.toList()));
    }


    /**
     * Sets up login for the application using the given Flow login view.
     *
     * @param http          the http security from {@link #filterChain(HttpSecurity)}
     * @param flowLoginView the login view to use
     * @throws Exception if something goes wrong
     */
    protected void setLoginView(HttpSecurity http,
                                Class<? extends Component> flowLoginView) throws Exception {
        setLoginView(http, flowLoginView, getDefaultLogoutUrl());
    }

    /**
     * Sets up login for the application using the given Flow login view.
     *
     * @param http             the http security from {@link #filterChain(HttpSecurity)}
     * @param flowLoginView    the login view to use
     * @param logoutSuccessUrl the URL to redirect the user to after logging out
     * @throws Exception if something goes wrong
     */
    protected void setLoginView(HttpSecurity http,
                                Class<? extends Component> flowLoginView, String logoutSuccessUrl)
            throws Exception {
        Optional<Route> route = AnnotationReader.getAnnotationFor(flowLoginView,
                Route.class);

        if (!route.isPresent()) {
            throw new IllegalArgumentException(
                    "Unable find a @Route annotation on the login view "
                            + flowLoginView.getName());
        }

        if (!(applicationContext instanceof WebApplicationContext)) {
            throw new RuntimeException(
                    "VaadinWebSecurity cannot be used without WebApplicationContext.");
        }

        VaadinServletContext vaadinServletContext = new VaadinServletContext(
                ((WebApplicationContext) applicationContext)
                        .getServletContext());
        String loginPath = RouteUtil.getRoutePath(vaadinServletContext,
                flowLoginView);
        if (!loginPath.startsWith("/")) {
            loginPath = "/" + loginPath;
        }
        loginPath = applyUrlMapping(loginPath);

        // Actually set it up
        String finalLoginPath = loginPath;
        http.formLogin(formLogin -> {
            formLogin.loginPage(finalLoginPath).permitAll();
            formLogin.successHandler(
                    getVaadinSavedRequestAwareAuthenticationSuccessHandler(http));
        });
        configureLogout(http, logoutSuccessUrl);
        http.exceptionHandling(exceptionHandling ->
                exceptionHandling.defaultAuthenticationEntryPointFor(
                        new LoginUrlAuthenticationEntryPoint(finalLoginPath),
                        AnyRequestMatcher.INSTANCE)
        );
        viewAccessChecker.setLoginView(flowLoginView);
    }

    /**
     * Helper method to prepend configured servlet path to the given path.
     * <p>
     * Path will always be considered as relative to servlet path, even if it starts with a slash character.
     *
     * @param path path to be prefixed with servlet path
     * @return the input path prepended by servlet path.
     */
    protected String applyUrlMapping(String path) {
        return applyUrlMapping(vaadinConfigurationProperties.getUrlMapping(), path);
    }

    /**
     * Gets the url mapping for the Vaadin servlet.
     *
     * @return the url mapping
     */
    public String getUrlMapping() {
        return vaadinConfigurationProperties.getUrlMapping();
    }

    private void configureLogout(HttpSecurity http, String logoutSuccessUrl)
            throws Exception {
        SimpleUrlLogoutSuccessHandler logoutSuccessHandler = new SimpleUrlLogoutSuccessHandler();
        logoutSuccessHandler.setDefaultTargetUrl(logoutSuccessUrl);
        logoutSuccessHandler.setRedirectStrategy(new UidlRedirectStrategy());
        http.logout(logout -> logout.logoutSuccessHandler(logoutSuccessHandler));
    }

    private String getDefaultLogoutUrl() {
        return servletContextPath.startsWith("/") ? servletContextPath
                : "/" + servletContextPath;
    }

    private VaadinSavedRequestAwareAuthenticationSuccessHandler getVaadinSavedRequestAwareAuthenticationSuccessHandler(
            HttpSecurity http) {
        VaadinSavedRequestAwareAuthenticationSuccessHandler vaadinSavedRequestAwareAuthenticationSuccessHandler = new VaadinSavedRequestAwareAuthenticationSuccessHandler();
        vaadinSavedRequestAwareAuthenticationSuccessHandler
                .setDefaultTargetUrl(applyUrlMapping(""));
        RequestCache requestCache = http.getSharedObject(RequestCache.class);
        if (requestCache != null) {
            vaadinSavedRequestAwareAuthenticationSuccessHandler
                    .setRequestCache(requestCache);
        }
        http.setSharedObject(
                VaadinSavedRequestAwareAuthenticationSuccessHandler.class,
                vaadinSavedRequestAwareAuthenticationSuccessHandler);
        return vaadinSavedRequestAwareAuthenticationSuccessHandler;
    }

    private AccessDeniedHandler createAccessDeniedHandler() {
        final AccessDeniedHandler defaultHandler = new AccessDeniedHandlerImpl();

        final AccessDeniedHandler http401UnauthorizedHandler = new SimpleSecurityVaadinConfiguration.Http401UnauthorizedAccessDeniedHandler();

        final LinkedHashMap<Class<? extends AccessDeniedException>, AccessDeniedHandler> exceptionHandlers = new LinkedHashMap<>();
        exceptionHandlers.put(CsrfException.class, http401UnauthorizedHandler);

        final LinkedHashMap<RequestMatcher, AccessDeniedHandler> matcherHandlers = new LinkedHashMap<>();
        matcherHandlers.put(requestUtil::isEndpointRequest,
                new DelegatingAccessDeniedHandler(exceptionHandlers,
                        new AccessDeniedHandlerImpl()));

        return new RequestMatcherDelegatingAccessDeniedHandler(matcherHandlers,
                defaultHandler);
    }

    private static class Http401UnauthorizedAccessDeniedHandler
            implements AccessDeniedHandler {
        @Override
        public void handle(HttpServletRequest request,
                           HttpServletResponse response,
                           AccessDeniedException accessDeniedException)
                throws IOException, ServletException {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
        }
    }

    /**
     * Prepends to the given {@code path} with the servlet path prefix from input url mapping.
     * <p>
     * A {@literal null} path is treated as empty string; the same applies for url mapping.
     *
     * @return the path with prepended url mapping.
     * @see VaadinConfigurationProperties#getUrlMapping()
     */
    private static String applyUrlMapping(String urlMapping, String path) {
        if (urlMapping == null) {
            urlMapping = "";
        } else {
            // remove potential / or /* at the end of the mapping
            urlMapping = urlMapping.replaceFirst("/\\*?$", "");
        }
        if (path == null) {
            path = "";
        } else if (path.startsWith("/")) {
            path = path.substring(1);
        }
        return urlMapping + "/" + path;
    }
}
