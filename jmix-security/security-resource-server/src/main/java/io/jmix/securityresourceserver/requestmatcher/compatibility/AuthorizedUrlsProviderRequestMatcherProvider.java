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

package io.jmix.securityresourceserver.requestmatcher.compatibility;

import io.jmix.core.security.AuthorizedUrlsProvider;
import io.jmix.securityresourceserver.requestmatcher.ResourceServerRequestMatcherProvider;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.List;
import java.util.stream.Collectors;

/**
 * An implementation of {@link ResourceServerRequestMatcherProvider} that collects URL patterns from multiple
 * {@link AuthorizedUrlsProvider}s and builds the {@link RequestMatcher} from all collected URL patterns using the
 * {@link OrRequestMatcher}. The purpose of the class is to support the legacy {@link AuthorizedUrlsProvider} mechanism.
 *
 * @see io.jmix.core.security.AuthorizedUrlsProvider
 */
public class AuthorizedUrlsProviderRequestMatcherProvider implements ResourceServerRequestMatcherProvider {

    private final List<AuthorizedUrlsProvider> authorizedUrlsProviders;

    public AuthorizedUrlsProviderRequestMatcherProvider(List<AuthorizedUrlsProvider> authorizedUrlsProviders) {
        this.authorizedUrlsProviders = authorizedUrlsProviders;
    }

    @Override
    public RequestMatcher getRequestMatcher() {
        List<RequestMatcher> requestMatchers = authorizedUrlsProviders.stream()
                .flatMap(provider -> provider.getAuthenticatedUrlPatterns().stream())
                .map(AntPathRequestMatcher::new)
                .collect(Collectors.toList());
        return new OrRequestMatcher(requestMatchers);
    }
}
