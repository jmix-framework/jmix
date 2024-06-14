/*
 * Copyright 2021 Haulmont.
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

package io.jmix.security.configurer;

import io.jmix.core.security.AuthorizedUrlsProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.collect.Iterables.toArray;

/**
 * @deprecated use {@link io.jmix.security.util.JmixHttpSecurityUtils#configureAuthorizedUrls(HttpSecurity)}
 */
@Deprecated(since = "2.3", forRemoval = true)
public class AuthorizedApiUrlsConfigurer extends AbstractHttpConfigurer<AuthorizedApiUrlsConfigurer, HttpSecurity> {
    @Override
    public void setBuilder(HttpSecurity http) {
        super.setBuilder(http);
        initAuthorizedUrls(http);
    }

    private void initAuthorizedUrls(HttpSecurity http) {
        ApplicationContext applicationContext = http.getSharedObject(ApplicationContext.class);
        Collection<AuthorizedUrlsProvider> authorizedUrlsProviders = applicationContext.getBeansOfType(AuthorizedUrlsProvider.class).values();

        Collection<String> anonymousUrlPatterns = authorizedUrlsProviders.stream()
                .flatMap(p -> p.getAnonymousUrlPatterns().stream())
                .toList();

        Collection<String> authenticatedUrlPatterns = authorizedUrlsProviders.stream()
                .flatMap(p -> p.getAuthenticatedUrlPatterns().stream())
                .toList();

        if (!anonymousUrlPatterns.isEmpty() || !authenticatedUrlPatterns.isEmpty()) {
            try {
                http.securityMatcher(createSecurityMatcher(authenticatedUrlPatterns, anonymousUrlPatterns));

                if (!anonymousUrlPatterns.isEmpty()) {
                    RequestMatcher[] anonymousRequestMatchers = createAntPathRequestMatchers(anonymousUrlPatterns);
                    http.authorizeHttpRequests(authorize ->
                            authorize.requestMatchers(anonymousRequestMatchers).permitAll()
                    );
                }
                if (!authenticatedUrlPatterns.isEmpty()) {
                    RequestMatcher[] authenticatedRequestMatchers = createAntPathRequestMatchers(authenticatedUrlPatterns);
                    http.authorizeHttpRequests(authorize ->
                            authorize.requestMatchers(authenticatedRequestMatchers).authenticated()
                    );
                }
            } catch (Exception e) {
                throw new RuntimeException("Error while init security", e);
            }
        }
    }

    private OrRequestMatcher createSecurityMatcher(Collection<String> anonymousUrlPatterns,
                                                   Collection<String> authenticatedUrlPatterns) {
        List<RequestMatcher> antPathMatchers = Stream.concat(anonymousUrlPatterns.stream(), authenticatedUrlPatterns.stream())
                .map(AntPathRequestMatcher::new)
                .collect(Collectors.toList());
        return new OrRequestMatcher(antPathMatchers);
    }

    private AntPathRequestMatcher[] createAntPathRequestMatchers(Collection<String> urlPatterns) {
        return Arrays.stream(toArray(urlPatterns, String.class))
                .map(AntPathRequestMatcher::new)
                .toArray(AntPathRequestMatcher[]::new);
    }
}
