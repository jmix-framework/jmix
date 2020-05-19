/*
 * Copyright (c) 2008-2019 Haulmont.
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

package io.jmix.rest.api.auth;


import io.jmix.rest.api.common.RestParseUtils;
import io.jmix.rest.api.config.RestQueriesConfiguration;
import io.jmix.rest.api.config.RestServicesConfiguration;
import io.jmix.rest.api.sys.CachingHttpServletRequestWrapper;
import io.jmix.rest.property.RestProperties;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This filter is used for anonymous access to CUBA REST API. If no Authorization header presents in the request and if
 * {@link RestProperties#isAnonymousEnabled()} is true, then the anonymous user session will be set to the {@link
 * SecurityContext} and the request will be authenticated. This filter must be invoked after the {@link
 * org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationProcessingFilter}
 */
//todo remove class
public class JmixAnonymousAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JmixAnonymousAuthenticationFilter.class);

    protected static final String GET = "GET";
    protected static final String POST = "POST";

    protected static final String QUERIES = "queries";
    protected static final String SERVICES = "services";

    protected static final String SERVICE_URL_REGEX = "/rest/v2/(services)/([a-zA-Z_][a-zA-Z\\d_$]*)/([a-zA-Z_][a-zA-Z\\d_]*)";
    protected static final String QUERY_URL_REGEX = "/rest/v2/(queries)/([a-zA-Z_][a-zA-Z\\d_$]*)/([a-zA-Z_][a-zA-Z\\d_]*)(/count)?";
    protected static final Pattern REGEX_PATTERN = Pattern.compile("^" + SERVICE_URL_REGEX + "|" + QUERY_URL_REGEX + "$");

    protected RestProperties restProperties;
    protected RestServicesConfiguration restServicesConfiguration;
    protected RestQueriesConfiguration restQueriesConfiguration;
    protected AuthenticationManager authenticationManager;
    protected RestParseUtils restParseUtils;

    public JmixAnonymousAuthenticationFilter(RestProperties restProperties, RestServicesConfiguration restServicesConfiguration, RestQueriesConfiguration restQueriesConfiguration, AuthenticationManager authenticationManager, RestParseUtils restParseUtils) {
        this.restProperties = restProperties;
        this.restServicesConfiguration = restServicesConfiguration;
        this.restQueriesConfiguration = restQueriesConfiguration;
        this.authenticationManager = authenticationManager;
        this.restParseUtils = restParseUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        ServletRequest nextRequest = request;
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            if (restProperties.isAnonymousEnabled()) {
                populateSecurityContextWithAnonymousSession();
            } else {
                //anonymous service method or query may be invoked
                String requestURI = request.getRequestURI();
                String methodType = request.getMethod();
                Matcher matcher = REGEX_PATTERN.matcher(requestURI);
                if (matcher.matches()) {
                    if (SERVICES.equals(matcher.group(1))) {
                        List<String> methodParamNames;
                        String serviceName = matcher.group(2);
                        String methodName = matcher.group(3);
                        if (GET.equals(methodType)) {
                            methodParamNames = Collections.list(request.getParameterNames());
                        } else if (POST.equals(methodType)) {
                            //wrap the request using content caching request wrapper because we need to access the
                            //request body
                            nextRequest = new CachingHttpServletRequestWrapper(request);
                            try {
                                String json = IOUtils.toString(nextRequest.getReader());
                                Map<String, String> paramsMap = restParseUtils.parseParamsJson(json);
                                methodParamNames = new ArrayList<>(paramsMap.keySet());
                            } catch (IOException e) {
                                log.error("Error on reading request body", e);
                                throw e;
                            }
                        } else {
                            filterChain.doFilter(nextRequest, response);
                            return;
                        }
                        RestServicesConfiguration.RestMethodInfo restMethodInfo = restServicesConfiguration
                                .getRestMethodInfo(serviceName, methodName, methodParamNames);
                        if (restMethodInfo != null && restMethodInfo.isAnonymousAllowed()) {
                            populateSecurityContextWithAnonymousSession();
                        }
                    } else if (QUERIES.equals(matcher.group(4))) {
                        String entityName = matcher.group(5);
                        String queryName = matcher.group(6);
                        if (GET.equals(methodType) || POST.equals(methodType)) {
                            RestQueriesConfiguration.QueryInfo restQueryInfo = restQueriesConfiguration.getQuery(entityName, queryName);
                            if (restQueryInfo != null && restQueryInfo.isAnonymousAllowed()) {
                                populateSecurityContextWithAnonymousSession();
                            }
                        }
                    }
                }
            }
        } else {
            log.debug("SecurityContextHolder not populated with cuba anonymous token, as it already contained: '{}'",
                    SecurityContextHolder.getContext().getAuthentication());
        }
        filterChain.doFilter(nextRequest, response);
    }

    protected void populateSecurityContextWithAnonymousSession() {
//        UserSession anonymousSession;
//        try {
//            anonymousSession = trustedClientService.getAnonymousSession(restApiConfig.getTrustedClientPassword(),
//                    restApiConfig.getSecurityScope());
//        } catch (LoginException e) {
//            throw new RuntimeException("Unable to obtain anonymous session for REST", e);
//        }

//        CubaAnonymousAuthenticationToken anonymousAuthenticationToken =
//                new CubaAnonymousAuthenticationToken("anonymous", AuthorityUtils.createAuthorityList("ROLE_CUBA_ANONYMOUS"));

//        AnonymousUserCredentials credentials = new AnonymousUserCredentials();
//        Authentication authentication = authenticationManager.authenticate(credentials);
//        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
