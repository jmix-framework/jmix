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

import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.Collection;
import java.util.List;

/**
 * A default implementation of {@link CompositeResourceServerRequestMatcherProvider} that combines multiple
 * {@link ResourceServerRequestMatcherProvider}s using an OR condition.
 *
 * @see ResourceServerRequestMatcherProvider
 */
public class CompositeRequestMatcherProviderImpl implements CompositeResourceServerRequestMatcherProvider {

    private final Collection<ResourceServerRequestMatcherProvider> requestMatcherProviders;

    public CompositeRequestMatcherProviderImpl(Collection<ResourceServerRequestMatcherProvider> requestMatcherProviders) {
        this.requestMatcherProviders = requestMatcherProviders;
    }

    @Override
    public RequestMatcher getSecurityMatcher() {
        return getCompositeRequestMatcher();
    }

    protected RequestMatcher getCompositeRequestMatcher() {
        List<RequestMatcher> matchers = requestMatcherProviders.stream()
                .map(ResourceServerRequestMatcherProvider::getRequestMatcher)
                .toList();
        return new OrRequestMatcher(matchers);
    }
}
