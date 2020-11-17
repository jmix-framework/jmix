/*
 * Copyright 2020 Haulmont.
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

package io.jmix.autoconfigure.rest;

import io.jmix.core.CoreConfiguration;
import io.jmix.data.DataConfiguration;
import io.jmix.rest.RestConfiguration;
import io.jmix.rest.api.auth.UniqueAuthenticationKeyGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.provider.token.AuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore;

@Configuration
@Import({CoreConfiguration.class, DataConfiguration.class, RestConfiguration.class})
public class RestAutoConfiguration {

    @Bean(name = "rest_tokenStore")
    @ConditionalOnMissingBean(TokenStore.class)
    protected TokenStore tokenStore() {
        InMemoryTokenStore tokenStore = new InMemoryTokenStore();
        tokenStore.setAuthenticationKeyGenerator(authenticationKeyGenerator());
        return tokenStore;
    }

    @Bean
    protected UniqueAuthenticationKeyGenerator authenticationKeyGenerator() {
        return new UniqueAuthenticationKeyGenerator();
    }
}
