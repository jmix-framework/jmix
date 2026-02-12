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
import io.jmix.securityresourceserver.authentication.ForceApiSecurityScopePropertiesProvider;
import io.jmix.security.util.RequestLocaleProvider;
import io.jmix.securityresourceserver.authentication.ResourceServerFilterChainCustomizer;
import io.jmix.securityresourceserver.requestmatcher.AnonymousRequestMatcherProvider;
import io.jmix.securityresourceserver.requestmatcher.AuthenticatedRequestMatcherProvider;
import io.jmix.securityresourceserver.requestmatcher.CompositeResourceServerRequestMatcherProvider;
import io.jmix.securityresourceserver.requestmatcher.impl.AnonymousUrlPatternsRequestMatcherProvider;
import io.jmix.securityresourceserver.requestmatcher.impl.AuthenticatedUrlPatternsRequestMatcherProvider;
import io.jmix.securityresourceserver.requestmatcher.impl.CompositeResourceServerRequestMatcherProviderImpl;
import io.jmix.securityresourceserver.requestmatcher.urlprovider.AnonymousUrlPatternsProvider;
import io.jmix.securityresourceserver.requestmatcher.urlprovider.AuthenticatedUrlPatternsProvider;
import io.jmix.securityresourceserver.requestmatcher.urlprovider.impl.AppPropertiesUrlPatternsProvider;
import io.jmix.securityresourceserver.requestmatcher.urlprovider.impl.LegacyAuthorizedUrlsPatternsProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;
import java.util.Map;

@AutoConfiguration
@ConditionalOnProperty(value = "jmix.resource-server.use-default-configuration", matchIfMissing = true)
@Import({CoreConfiguration.class})
public class SecurityResourceServerAutoConfiguration {

    @ConditionalOnMissingBean
    @Bean("sec_ResourceServerSecurityMatcherProvider")
    CompositeResourceServerRequestMatcherProvider resourceServerSecurityMatcherProvider(
            List<AuthenticatedRequestMatcherProvider> authenticatedRequestMatcherProviders,
            List<AnonymousRequestMatcherProvider> anonymousRequestMatcherProviders) {
        return new CompositeResourceServerRequestMatcherProviderImpl(authenticatedRequestMatcherProviders, anonymousRequestMatcherProviders);
    }

    @Bean("sec_AppPropertiesUrlPatternsProvider")
    AppPropertiesUrlPatternsProvider appPropertiesUrlPatternsProvider(JmixModules jmixModules) {
        return new AppPropertiesUrlPatternsProvider(jmixModules);
    }

    @Bean("sec_AuthenticatedUrlsRequestMatcherProvider")
    AuthenticatedUrlPatternsRequestMatcherProvider authenticatedUrlsRequestMatcherProvider(
            List<AuthenticatedUrlPatternsProvider> authenticatedUrlPatternsProviders) {
        return new AuthenticatedUrlPatternsRequestMatcherProvider(authenticatedUrlPatternsProviders);
    }

    @Bean("sec_AnonymousUrlsRequestMatcherProvider")
    AnonymousUrlPatternsRequestMatcherProvider anonymousUrlsRequestMatcherProvider(
            List<AnonymousUrlPatternsProvider> anonymousUrlPatternsProviders) {
        return new AnonymousUrlPatternsRequestMatcherProvider(anonymousUrlPatternsProviders);
    }

    @Bean("sec_LegacyAuthorizedUrlsPatternsProvider")
    LegacyAuthorizedUrlsPatternsProvider legacyAuthorizedUrlsPatternsProvider(
            List<AuthorizedUrlsProvider> authorizedUrlsProviders) {
        return new LegacyAuthorizedUrlsPatternsProvider(authorizedUrlsProviders);
    }

    @Bean("sec_ForceApiSecurityScopePropertiesProvider")
    ForceApiSecurityScopePropertiesProvider authDetailsCustomizationSettingsProvider(JmixModules jmixModules, Environment environment) {
        return new ForceApiSecurityScopePropertiesProvider(jmixModules, environment);
    }

    @Bean("sec_ResourceServerFilterChainCustomizer")
    ResourceServerFilterChainCustomizer resourceServerFilterChainCustomizer(ForceApiSecurityScopePropertiesProvider forceApiSecurityScopePropertiesProvider,
                                                                            RequestLocaleProvider requestLocaleProvider,
                                                                            Map<String, SecurityFilterChain> chains) {
        return new ResourceServerFilterChainCustomizer(forceApiSecurityScopePropertiesProvider, requestLocaleProvider, chains);
    }
}
