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

package io.jmix.autoconfigure.securityresourceserver;

import io.jmix.core.CoreConfiguration;
import io.jmix.core.JmixModules;
import io.jmix.core.security.AuthorizedUrlsProvider;
import io.jmix.securityresourceserver.requestmatcher.*;
import io.jmix.securityresourceserver.requestmatcher.compatibility.AuthorizedUrlsProviderRequestMatcherProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.util.List;

@AutoConfiguration
@ConditionalOnProperty(value = "jmix.resource-server.use-default-configuration", matchIfMissing = true)
@Import({CoreConfiguration.class})
public class SecurityResourceServerAutoConfiguration {

    @ConditionalOnMissingBean
    @Bean("sec_ResourceServerSecurityMatcherProvider")
    CompositeResourceServerRequestMatcherProvider resourceServerSecurityMatcherProvider(
            List<ResourceServerRequestMatcherProvider> resourceServerRequestMatcherProviders) {
        return new CompositeRequestMatcherProviderImpl(resourceServerRequestMatcherProviders);
    }

    @Bean("sec_AppPropertiesAuthenticatedUrlPatternsProvider")
    AppPropertiesAuthenticatedUrlPatternsProvider appPropertiesAuthenticatedUrlPatternsProvider(JmixModules jmixModules) {
        return new AppPropertiesAuthenticatedUrlPatternsProvider(jmixModules);
    }

    @Bean("sec_AuthenticatedUrlsRequestMatcherProvider")
    AuthenticatedUrlsRequestMatcherProvider authorizedUrlsRequestMatcherProvider(
            List<AuthenticatedUrlPatternsProvider> authenticatedUrlPatternsProviders) {
        return new AuthenticatedUrlsRequestMatcherProvider(authenticatedUrlPatternsProviders);
    }

    @Bean("sec_AuthorizedUrlsProviderRequestMatcherProvider")
    AuthorizedUrlsProviderRequestMatcherProvider authorizedUrlsProviderRequestMatcherProvider(
            List<AuthorizedUrlsProvider> authorizedUrlsProviders) {
        return new AuthorizedUrlsProviderRequestMatcherProvider(authorizedUrlsProviders);
    }
}
