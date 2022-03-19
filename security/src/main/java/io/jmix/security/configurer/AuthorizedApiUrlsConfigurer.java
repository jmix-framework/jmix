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
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;

import java.util.Collection;
import java.util.stream.Collectors;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.toArray;

public class AuthorizedApiUrlsConfigurer extends AbstractHttpConfigurer<AuthorizedApiUrlsConfigurer, HttpSecurity> {
    @Override
    public void setBuilder(HttpSecurity http) {
        super.setBuilder(http);
        initAuthorizedUrls(http);
    }

    private void initAuthorizedUrls(HttpSecurity http) {
        ApplicationContext applicationContext = http.getSharedObject(ApplicationContext.class);

        Collection<String> anonymousUrlPatterns = getAnonymousUrlPatterns(applicationContext);
        Collection<String> authenticatedUrlPatterns = getAuthenticatedUrlPatterns(applicationContext);

        if (!anonymousUrlPatterns.isEmpty() || !authenticatedUrlPatterns.isEmpty()) {
            try {
                String[] urlPatterns = toArray(concat(anonymousUrlPatterns, authenticatedUrlPatterns), String.class);

                ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry urlRegistry = http.requestMatchers()
                        .antMatchers(urlPatterns)
                        .and()
                        .authorizeRequests();

                if (!anonymousUrlPatterns.isEmpty()) {
                    urlRegistry.antMatchers(toArray(anonymousUrlPatterns, String.class)).permitAll();
                }

                if (!authenticatedUrlPatterns.isEmpty()) {
                    urlRegistry.antMatchers(toArray(authenticatedUrlPatterns, String.class)).authenticated();
                }

            } catch (Exception e) {
                throw new RuntimeException("Error while init security", e);
            }
        }
    }

    private Collection<String> getAnonymousUrlPatterns(ApplicationContext applicationContext) {
        return applicationContext.getBeansOfType(AuthorizedUrlsProvider.class).values().stream()
                .flatMap(p -> p.getAnonymousUrlPatterns().stream())
                .collect(Collectors.toList());
    }

    private Collection<String> getAuthenticatedUrlPatterns(ApplicationContext applicationContext) {
        return applicationContext.getBeansOfType(AuthorizedUrlsProvider.class).values().stream()
                .flatMap(p -> p.getAuthenticatedUrlPatterns().stream())
                .collect(Collectors.toList());
    }
}
