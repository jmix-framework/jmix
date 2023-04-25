/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.jmix.securityoauth2.config;

import io.jmix.core.JmixOrder;
import org.springframework.aop.framework.Advised;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AnonymousAuthenticationProvider;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerEndpointsConfiguration;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.endpoint.FrameworkEndpointHandlerMapping;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.ReflectionUtils;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A copy of {@link org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfiguration}
 * that doesn't extend {@code WebSecurityConfigurerAdapter}.
 */
@Configuration
public class ResourceServerConfiguration implements Ordered {

    private int order = 3;

    @Autowired(required = false)
    private TokenStore tokenStore;

    @Autowired(required = false)
    private AuthenticationEventPublisher eventPublisher;

    @Autowired(required = false)
    private Map<String, ResourceServerTokenServices> tokenServices;

    @Autowired
    private ApplicationContext context;

    private List<ResourceServerConfigurer> configurers = Collections.emptyList();

    @Autowired(required = false)
    private AuthorizationServerEndpointsConfiguration endpoints;

    @Override
    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    /**
     * @param configurers the configurers to set
     */
    @Autowired(required = false)
    public void setConfigurers(List<ResourceServerConfigurer> configurers) {
        this.configurers = configurers;
    }

    private static class NotOAuthRequestMatcher implements RequestMatcher {

        private FrameworkEndpointHandlerMapping mapping;

        public NotOAuthRequestMatcher(FrameworkEndpointHandlerMapping mapping) {
            this.mapping = mapping;
        }

        @Override
        public boolean matches(HttpServletRequest request) {
            String requestPath = getRequestPath(request);
            for (String path : mapping.getPaths()) {
                if (requestPath.startsWith(mapping.getPath(path))) {
                    return false;
                }
            }
            return true;
        }

        private String getRequestPath(HttpServletRequest request) {
            String url = request.getServletPath();

            if (request.getPathInfo() != null) {
                url += request.getPathInfo();
            }

            return url;
        }

    }

    @Bean("sec_OAuthResourceServerSecurityFilterChain")
    @Order(JmixOrder.HIGHEST_PRECEDENCE + 150)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        ResourceServerSecurityConfigurer resources = new ResourceServerSecurityConfigurer();
        ResourceServerTokenServices services = resolveTokenServices();
        if (services != null) {
            resources.tokenServices(services);
        } else {
            if (tokenStore != null) {
                resources.tokenStore(tokenStore);
            } else if (endpoints != null) {
                resources.tokenStore(endpoints.getEndpointsConfigurer().getTokenStore());
            }
        }
        if (eventPublisher != null) {
            resources.eventPublisher(eventPublisher);
        }
        for (ResourceServerConfigurer configurer : configurers) {
            configurer.configure(resources);
        }
        // @formatter:off
        http.authenticationProvider(new AnonymousAuthenticationProvider("default"))
                // N.B. exceptionHandling is duplicated in resources.configure() so that
                // it works
                .exceptionHandling()
                .accessDeniedHandler(resources.getAccessDeniedHandler()).and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .csrf().disable();
        // @formatter:on
        http.apply(resources);
        if (endpoints != null) {
            // Assume we are in an Authorization Server
            http.securityMatcher(new NotOAuthRequestMatcher(endpoints.oauth2EndpointHandlerMapping()));
        }
        for (ResourceServerConfigurer configurer : configurers) {
            // Delegates can add authorizeRequests() here
            configurer.configure(http);
        }
        if (configurers.isEmpty()) {
            // Add anyRequest() last as a fall back. Spring Security would
            // replace an existing anyRequest() matcher with this one, so to
            // avoid that we only add it if the user hasn't configured anything.
            http.authorizeRequests().anyRequest().authenticated();
        }
        return http.build();
    }

    private ResourceServerTokenServices resolveTokenServices() {
        if (tokenServices == null || tokenServices.size() == 0) {
            return null;
        }
        if (tokenServices.size() == 1) {
            return tokenServices.values().iterator().next();
        }
        if (tokenServices.size() == 2) {
            // Maybe they are the ones provided natively
            Iterator<ResourceServerTokenServices> iter = tokenServices.values().iterator();
            ResourceServerTokenServices one = iter.next();
            ResourceServerTokenServices two = iter.next();
            if (elementsEqual(one, two)) {
                return one;
            }
        }
        return context.getBean(ResourceServerTokenServices.class);
    }

    private boolean elementsEqual(Object one, Object two) {
        // They might just be equal
        if (one == two) {
            return true;
        }
        Object targetOne = findTarget(one);
        Object targetTwo = findTarget(two);
        return targetOne == targetTwo;
    }

    private Object findTarget(Object item) {
        Object current = item;
        while (current instanceof Advised) {
            try {
                current = ((Advised) current).getTargetSource().getTarget();
            } catch (Exception e) {
                ReflectionUtils.rethrowRuntimeException(e);
            }
        }
        return current;
    }

}
