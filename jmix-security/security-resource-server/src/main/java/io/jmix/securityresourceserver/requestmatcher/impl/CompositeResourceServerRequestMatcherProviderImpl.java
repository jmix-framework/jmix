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
import io.jmix.securityresourceserver.requestmatcher.AuthenticatedRequestMatcherProvider;
import io.jmix.securityresourceserver.requestmatcher.CompositeResourceServerRequestMatcherProvider;
import io.jmix.securityresourceserver.requestmatcher.util.RequestMatcherUtils;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.Collection;
import java.util.List;

/**
 * A default implementation of {@link CompositeResourceServerRequestMatcherProvider} that combines multiple
 * {@link AuthenticatedRequestMatcherProvider}s and {@link AnonymousRequestMatcherProvider}s using an OR condition.
 *
 * @see AuthenticatedRequestMatcherProvider
 */
public class CompositeResourceServerRequestMatcherProviderImpl implements CompositeResourceServerRequestMatcherProvider {

    private final Collection<AuthenticatedRequestMatcherProvider> authenticatedRequestMatcherProviders;
    private final Collection<AnonymousRequestMatcherProvider> anonymousRequestMatcherProviders;

    public CompositeResourceServerRequestMatcherProviderImpl(
            Collection<AuthenticatedRequestMatcherProvider> authenticatedRequestMatcherProviders,
            Collection<AnonymousRequestMatcherProvider> anonymousRequestMatcherProviders) {
        this.authenticatedRequestMatcherProviders = authenticatedRequestMatcherProviders;
        this.anonymousRequestMatcherProviders = anonymousRequestMatcherProviders;
    }

    @Override
    public RequestMatcher getAuthenticatedRequestMatcher() {
        return getCompositeAuthenticatedRequestMatcher();
    }

    @Override
    public RequestMatcher getAnonymousRequestMatcher() {
        return getCompositeAnonymousRequestMatcher();
    }

    protected RequestMatcher getCompositeAuthenticatedRequestMatcher() {
        List<RequestMatcher> requestMatcher = authenticatedRequestMatcherProviders.stream()
                .map(AuthenticatedRequestMatcherProvider::getAuthenticatedRequestMatcher)
                .toList();
        return RequestMatcherUtils.createCombinedRequestMatcher(requestMatcher);
    }

    protected RequestMatcher getCompositeAnonymousRequestMatcher() {
        List<RequestMatcher> requestMatcher = anonymousRequestMatcherProviders.stream()
                .map(AnonymousRequestMatcherProvider::getAnonymousRequestMatcher)
                .toList();
        return RequestMatcherUtils.createCombinedRequestMatcher(requestMatcher);
    }
}
