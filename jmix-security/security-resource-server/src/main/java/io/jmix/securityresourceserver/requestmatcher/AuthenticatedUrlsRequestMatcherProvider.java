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

package io.jmix.securityresourceserver.requestmatcher;

import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.List;
import java.util.stream.Collectors;

/**
 * An implementation of {@link ResourceServerRequestMatcherProvider} that collects URL patterns from multiple
 * {@link AuthenticatedUrlPatternsProvider}s and builds the {@link RequestMatcher} from all collected URL patterns.
 *
 * @see AuthenticatedUrlPatternsProvider
 */
public class AuthenticatedUrlsRequestMatcherProvider implements ResourceServerRequestMatcherProvider {

    private final List<AuthenticatedUrlPatternsProvider> authenticatedUrlPatternsProviders;

    public AuthenticatedUrlsRequestMatcherProvider(List<AuthenticatedUrlPatternsProvider> authenticatedUrlPatternsProviders) {
        this.authenticatedUrlPatternsProviders = authenticatedUrlPatternsProviders;
    }

    @Override
    public RequestMatcher getRequestMatcher() {
        List<RequestMatcher> requestMatchers = authenticatedUrlPatternsProviders.stream()
                .flatMap(urlPatternProvider -> urlPatternProvider.getAuthenticatedUrlPatterns().stream())
                .map(AntPathRequestMatcher::new)
                .collect(Collectors.toList());
        return new OrRequestMatcher(requestMatchers);
    }
}
