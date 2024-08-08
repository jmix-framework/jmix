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

package io.jmix.securityresourceserver.requestmatcher.urlprovider.impl;

import io.jmix.core.security.AuthorizedUrlsProvider;
import io.jmix.securityresourceserver.requestmatcher.urlprovider.AnonymousUrlPatternsProvider;
import io.jmix.securityresourceserver.requestmatcher.urlprovider.AuthenticatedUrlPatternsProvider;

import java.util.List;

/**
 * An implementation of {@link AuthenticatedUrlPatternsProvider} and {@link AnonymousUrlPatternsProvider} that
 * collects URL patterns from multiple {@link AuthorizedUrlsProvider}s. The purpose of the class is to
 * support the legacy {@link AuthorizedUrlsProvider} mechanism.
 *
 * @see AuthorizedUrlsProvider
 */
public class LegacyAuthorizedUrlsPatternsProvider
        implements AuthenticatedUrlPatternsProvider, AnonymousUrlPatternsProvider {

    private final List<AuthorizedUrlsProvider> authorizedUrlsProviders;

    public LegacyAuthorizedUrlsPatternsProvider(List<AuthorizedUrlsProvider> authorizedUrlsProviders) {
        this.authorizedUrlsProviders = authorizedUrlsProviders;
    }

    @Override
    public List<String> getAnonymousUrlPatterns() {
        return authorizedUrlsProviders.stream()
                .flatMap(provider -> provider.getAnonymousUrlPatterns().stream())
                .toList();
    }

    @Override
    public List<String> getAuthenticatedUrlPatterns() {
        return authorizedUrlsProviders.stream()
                .flatMap(provider -> provider.getAuthenticatedUrlPatterns().stream())
                .toList();
    }
}
