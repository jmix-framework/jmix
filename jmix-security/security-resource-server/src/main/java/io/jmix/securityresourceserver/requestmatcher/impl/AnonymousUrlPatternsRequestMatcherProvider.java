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

package io.jmix.securityresourceserver.requestmatcher.impl;

import io.jmix.securityresourceserver.requestmatcher.AnonymousRequestMatcherProvider;
import io.jmix.securityresourceserver.requestmatcher.urlprovider.AnonymousUrlPatternsProvider;
import io.jmix.securityresourceserver.requestmatcher.util.RequestMatcherUtils;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.List;
import java.util.stream.Collectors;

/**
 * An implementation of {@link AnonymousRequestMatcherProvider} that collects anonymous URL patterns from multiple
 * {@link AnonymousUrlPatternsProvider}s and builds the {@link RequestMatcher} from all collected URL patterns.
 *
 * @see AnonymousUrlPatternsProvider
 */
public class AnonymousUrlPatternsRequestMatcherProvider implements AnonymousRequestMatcherProvider {

    private final List<AnonymousUrlPatternsProvider> anonymousUrlPatternsProviders;

    public AnonymousUrlPatternsRequestMatcherProvider(List<AnonymousUrlPatternsProvider> anonymousUrlPatternsProviders) {
        this.anonymousUrlPatternsProviders = anonymousUrlPatternsProviders;
    }

    @Override
    public RequestMatcher getAnonymousRequestMatcher() {
        List<RequestMatcher> requestMatchers = anonymousUrlPatternsProviders.stream()
                .flatMap(urlPatternProvider -> urlPatternProvider.getAnonymousUrlPatterns().stream())
                .map(PathPatternRequestMatcher::pathPattern)
                .collect(Collectors.toList());
        return RequestMatcherUtils.createCombinedRequestMatcher(requestMatchers);
    }
}
