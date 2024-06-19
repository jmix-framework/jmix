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

import com.google.common.base.Splitter;
import io.jmix.core.JmixModules;
import io.jmix.securityresourceserver.requestmatcher.urlprovider.AnonymousUrlPatternsProvider;
import io.jmix.securityresourceserver.requestmatcher.urlprovider.AuthenticatedUrlPatternsProvider;

import java.util.List;

/**
 * An implementation of the {@link AuthenticatedUrlPatternsProvider} and the {@link AnonymousUrlPatternsProvider} that
 * gets URL patterns from application properties:
 * <ul>
 *     <li>For authenticated URL patterns the class uses the {@code jmix.resource-server.authenticated-url-patterns} property</li>
 *     <li>For anonymous URL patterns the class uses the {@code jmix.resource-server.anonymous-url-patterns} property</li>
 * </ul>
 *
 * @see AuthenticatedUrlPatternsProvider
 * @see AnonymousUrlPatternsProvider
 */
public class AppPropertiesUrlPatternsProvider implements AuthenticatedUrlPatternsProvider, AnonymousUrlPatternsProvider {

    private static final String AUTHENTICATED_URL_PATTERNS_PROPERTY = "jmix.resource-server.authenticated-url-patterns";
    private static final String ANONYMOUS_URL_PATTERNS_PROPERTY = "jmix.resource-server.anonymous-url-patterns";

    private final JmixModules jmixModules;

    public AppPropertiesUrlPatternsProvider(JmixModules jmixModules) {
        this.jmixModules = jmixModules;
    }

    @Override
    public List<String> getAuthenticatedUrlPatterns() {
        return getUrlPatternsFromAppProperties(AUTHENTICATED_URL_PATTERNS_PROPERTY);
    }

    @Override
    public List<String> getAnonymousUrlPatterns() {
        return getUrlPatternsFromAppProperties(ANONYMOUS_URL_PATTERNS_PROPERTY);
    }

    protected List<String> getUrlPatternsFromAppProperties(String propertyName) {
        //the value of the property in each module may contain a comma separated list of URL patterns
        List<String> propertyValues = jmixModules.getPropertyValues(propertyName);
        return propertyValues.stream()
                .flatMap(propertyValue ->
                        Splitter.on(",")
                                .omitEmptyStrings()
                                .trimResults()
                                .splitToStream(propertyValue))
                .toList();
    }
}
