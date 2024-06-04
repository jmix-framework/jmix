package io.jmix.securityflowui;

import com.google.common.base.Strings;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.internal.AnnotationReader;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.internal.RouteUtil;
import com.vaadin.flow.server.HandlerHelper;
import com.vaadin.flow.server.VaadinServletContext;
import com.vaadin.flow.spring.VaadinConfigurationProperties;
import com.vaadin.flow.spring.security.RequestUtil;
import com.vaadin.flow.spring.security.VaadinDefaultRequestCache;
import com.vaadin.flow.spring.security.VaadinSavedRequestAwareAuthenticationSuccessHandler;
import io.jmix.core.JmixSecurityFilterChainOrder;
import io.jmix.flowui.UiProperties;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewRegistry;
import io.jmix.security.SecurityConfigurers;
import io.jmix.security.configurer.AnonymousConfigurer;
import io.jmix.security.configurer.RememberMeConfigurer;
import io.jmix.security.configurer.SessionManagementConfigurer;
import io.jmix.securityflowui.access.UiViewAccessChecker;
import io.jmix.securityflowui.security.FlowuiVaadinWebSecurity;
import io.jmix.securityflowui.util.PrevVaadinRequestUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.access.DelegatingAccessDeniedHandler;
import org.springframework.security.web.access.RequestMatcherDelegatingAccessDeniedHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

/**
 * @deprecated {@link FlowuiVaadinWebSecurity} is used instead.
 */
//@EnableWebSecurity
//@Configuration
@Deprecated(since = "2.3", forRemoval = true)
public class FlowuiSecurityConfiguration {

    private static final Logger log = LoggerFactory.getLogger(FlowuiSecurityConfiguration.class);

    public static final String LOGOUT_URL = "/logout";
    public static final String LOGOUT_SUCCESS_URL = "/";
    public static final String SECURITY_CONFIGURER_QUALIFIER = "flowui";

    protected ApplicationContext applicationContext;

    protected VaadinDefaultRequestCache vaadinDefaultRequestCache;
    protected VaadinConfigurationProperties configurationProperties;
    protected RequestUtil requestUtil;

    protected UiViewAccessChecker viewAccessChecker;
    protected UiProperties uiProperties;
    protected ViewRegistry viewRegistry;
    protected ServerProperties serverProperties;

    protected PrevVaadinRequestUtil prevVaadinRequestUtil;

    @Autowired
    public void setPrevVaadinRequestUtil(PrevVaadinRequestUtil prevVaadinRequestUtil) {
        this.prevVaadinRequestUtil = prevVaadinRequestUtil;
    }

    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Autowired
    public void setVaadinDefaultRequestCache(VaadinDefaultRequestCache vaadinDefaultRequestCache) {
        this.vaadinDefaultRequestCache = vaadinDefaultRequestCache;
    }

    @Autowired
    public void setConfigurationProperties(VaadinConfigurationProperties configurationProperties) {
        this.configurationProperties = configurationProperties;
    }

    @Autowired
    public void setRequestUtil(RequestUtil requestUtil) {
        this.requestUtil = requestUtil;
    }

    @Autowired
    public void setViewAccessChecker(UiViewAccessChecker viewAccessChecker) {
        this.viewAccessChecker = viewAccessChecker;
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

    /**
     * The paths listed as "ignoring" in this method are handled without any
     * Spring Security involvement. They have no access to any security context
     * etc.
     * <p>
     * {@inheritDoc}
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers(getDefaultWebSecurityIgnoreMatcher(getUrlMapping()));
    }

    @Bean("sec_UiSecurityFilterChain")
    @Order(JmixSecurityFilterChainOrder.FLOWUI)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        configure(http);

        // Apply Jmix SecurityConfigurer beans with the given qualifier to the HttpSecurity
        // Can be used as extension points to adjust security configurations
        SecurityConfigurers.applySecurityConfigurersWithQualifier(http, SECURITY_CONFIGURER_QUALIFIER);

        initLoginView(http);

        return http.build();
    }

    protected void configure(HttpSecurity http) throws Exception {
        // Respond with 401 Unauthorized HTTP status code for unauthorized
        // requests for protected Fusion endpoints, so that the response could
        // be handled on the client side using e.g. `InvalidSessionMiddleware`.
        http.exceptionHandling()
                .accessDeniedHandler(createAccessDeniedHandler())
                .defaultAuthenticationEntryPointFor(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                        requestUtil::isEndpointRequest);

        // Vaadin has its own CSRF protection.
        // Spring CSRF is not compatible with Vaadin internal requests
        http.csrf().ignoringRequestMatchers(
                requestUtil::isFrameworkInternalRequest);

        // Ensure automated requests to e.g. closing push channels, service
        // workers,
        // endpoints are not counted as valid targets to redirect user to on
        // login
        http.requestCache().requestCache(vaadinDefaultRequestCache);

        // Jmix additional configurers
        http.apply(new AnonymousConfigurer());
        http.apply(new SessionManagementConfigurer());
        http.apply(new RememberMeConfigurer());

        AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry urlRegistry = http
                .authorizeHttpRequests();

        // Vaadin internal requests must always be allowed to allow public Flow
        // pages
        // and/or login page implemented using Flow.
        urlRegistry.requestMatchers(requestUtil::isFrameworkInternalRequest)
                .permitAll();
        // Public endpoints are OK to access
        urlRegistry.requestMatchers(requestUtil::isAnonymousEndpoint).permitAll();
        // Public routes are OK to access
        //use RequestUtil::isAnonymousRoute from Vaadin 24.1. See https://github.com/jmix-framework/jmix/issues/2985
        urlRegistry.requestMatchers(prevVaadinRequestUtil::isAnonymousRoute).permitAll();
//        urlRegistry.requestMatchers(requestUtil::isAnonymousRoute).permitAll();
        urlRegistry.requestMatchers(
                getDefaultHttpSecurityPermitMatcher(getUrlMapping())).permitAll();

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

        // all other requests require authentication
        urlRegistry.anyRequest().authenticated();

        http.securityContext(contextConfigurer ->
                contextConfigurer.requireExplicitSave(false));

        // Enable view access control
        viewAccessChecker.enable();
    }

    protected void initLoginView(HttpSecurity http) throws Exception {
        String loginViewId = uiProperties.getLoginViewId();
        if (Strings.isNullOrEmpty(loginViewId)) {
            log.debug("Login view Id is not defined");
            return;
        }

        setLoginView(http, loginViewId);
    }

    /**
     * Matcher for framework internal requests, with Vaadin servlet mapped on
     * the given path.
     *
     * @param urlMapping url mapping for the Vaadin servlet.
     * @return default {@link HttpSecurity} bypass matcher
     */
    public static RequestMatcher getDefaultHttpSecurityPermitMatcher(String urlMapping) {
        checkNotNullArgument(urlMapping, "Vaadin servlet url mapping is required");

        Stream.Builder<String> paths = Stream.builder();
        Stream.of(HandlerHelper.getPublicResourcesRequiringSecurityContext())
                .map(path -> applyUrlMapping(urlMapping, path))
                .forEach(paths::add);

        return new OrRequestMatcher(paths.build()
                .map(AntPathRequestMatcher::new).collect(Collectors.toList()));
    }

    /**
     * Matcher for Vaadin static (public) resources, with Vaadin servlet mapped
     * on the given path.
     * <p>
     * Assumes Vaadin servlet to be mapped on root path ({@literal /*}).
     *
     * @param urlMapping the url mapping for the Vaadin servlet
     * @return default {@link WebSecurity} ignore matcher
     */
    public static RequestMatcher getDefaultWebSecurityIgnoreMatcher(String urlMapping) {
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

    protected void setLoginView(HttpSecurity http, String viewId) throws Exception {
        setLoginView(http, viewId, LOGOUT_SUCCESS_URL);
    }

    protected void setLoginView(HttpSecurity http, String viewId, String logoutUrl) throws Exception {
        Class<? extends View<?>> controllerClass =
                viewRegistry.getViewInfo(viewId).getControllerClass();

        setLoginView(http, controllerClass, logoutUrl);
    }

    protected void setLoginView(HttpSecurity http,
                                Class<? extends Component> viewClass) throws Exception {
        setLoginView(http, viewClass, LOGOUT_SUCCESS_URL);
    }

    protected void setLoginView(HttpSecurity http,
                                Class<? extends Component> loginViewClass, String logoutUrl) throws Exception {
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

        // Actually set it up
        FormLoginConfigurer<HttpSecurity> formLogin = http.formLogin();
        formLogin.loginPage(loginPath).permitAll();
        formLogin.successHandler(createSuccessHandler(http));

        HandlerMappingIntrospector handlerMappingIntrospector = applicationContext.getBean(HandlerMappingIntrospector.class);
        MvcRequestMatcher.Builder mvcRequestMatcherBuilder = new MvcRequestMatcher.Builder(handlerMappingIntrospector);
        MvcRequestMatcher loginPathPattern = mvcRequestMatcherBuilder.pattern(loginPath);
        http.csrf(csrf -> csrf.ignoringRequestMatchers(loginPathPattern));
        // TODO: gg, replace with VaadinWebSecurity.configureLogout?
        http.logout()
                .logoutUrl(LOGOUT_URL)
                .logoutRequestMatcher(createLogoutRequestMatcher(LOGOUT_URL))
                .logoutSuccessUrl(logoutUrl);
        http.exceptionHandling().defaultAuthenticationEntryPointFor(
                new LoginUrlAuthenticationEntryPoint(loginPath), AnyRequestMatcher.INSTANCE);

        viewAccessChecker.setLoginView(loginViewClass);
    }

    protected RequestMatcher createLogoutRequestMatcher(String logoutUrl) {
        RequestMatcher post = createLogoutRequestMatcher(logoutUrl, "POST");
        RequestMatcher get = createLogoutRequestMatcher(logoutUrl, "GET");
        RequestMatcher put = createLogoutRequestMatcher(logoutUrl, "PUT");
        RequestMatcher delete = createLogoutRequestMatcher(logoutUrl, "DELETE");
        return new OrRequestMatcher(get, post, put, delete);
    }

    protected RequestMatcher createLogoutRequestMatcher(String logoutUrl, String httpMethod) {
        return new AntPathRequestMatcher(logoutUrl, httpMethod);
    }

    protected VaadinSavedRequestAwareAuthenticationSuccessHandler createSuccessHandler(HttpSecurity http) {
        VaadinSavedRequestAwareAuthenticationSuccessHandler authenticationSuccessHandler =
                new VaadinSavedRequestAwareAuthenticationSuccessHandler();

        authenticationSuccessHandler.setDefaultTargetUrl(applyUrlMapping(""));

        RequestCache requestCache = http.getSharedObject(RequestCache.class);
        if (requestCache != null) {
            authenticationSuccessHandler.setRequestCache(requestCache);
        }

        return authenticationSuccessHandler;
    }

    protected AccessDeniedHandler createAccessDeniedHandler() {
        AccessDeniedHandler defaultHandler = new AccessDeniedHandlerImpl();
        AccessDeniedHandler http401UnauthorizedHandler = new Http401UnauthorizedAccessDeniedHandler();

        LinkedHashMap<Class<? extends AccessDeniedException>, AccessDeniedHandler> exceptionHandlers =
                new LinkedHashMap<>();
        exceptionHandlers.put(CsrfException.class, http401UnauthorizedHandler);

        LinkedHashMap<RequestMatcher, AccessDeniedHandler> matcherHandlers = new LinkedHashMap<>();
        matcherHandlers.put(requestUtil::isEndpointRequest,
                new DelegatingAccessDeniedHandler(exceptionHandlers, new AccessDeniedHandlerImpl()));

        return new RequestMatcherDelegatingAccessDeniedHandler(matcherHandlers, defaultHandler);
    }

    protected static class Http401UnauthorizedAccessDeniedHandler implements AccessDeniedHandler {

        @Override
        public void handle(HttpServletRequest request,
                           HttpServletResponse response,
                           AccessDeniedException accessDeniedException) throws IOException, ServletException {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
        }
    }

    public String getUrlMapping() {
        return configurationProperties.getUrlMapping();
    }

    /**
     * Prepends to the given {@code path} with the configured url mapping.
     * <p>
     * A {@code null} path is treated as empty string; the same applies for
     * url mapping.
     *
     * @param path a path
     * @return the path with prepended url mapping.
     * @see VaadinConfigurationProperties#getUrlMapping()
     */
    public String applyUrlMapping(String path) {
        return applyUrlMapping(configurationProperties.getUrlMapping(), path);
    }

    /**
     * Prepends to the given {@code path} with the servlet path prefix from
     * input url mapping.
     * <p>
     * A {@code null} path is treated as empty string; the same applies for
     * url mapping.
     *
     * @param path       a path
     * @param urlMapping url mapping
     * @return the path with prepended url mapping.
     * @see VaadinConfigurationProperties#getUrlMapping()
     */
    public static String applyUrlMapping(@Nullable String urlMapping, @Nullable String path) {
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
}
