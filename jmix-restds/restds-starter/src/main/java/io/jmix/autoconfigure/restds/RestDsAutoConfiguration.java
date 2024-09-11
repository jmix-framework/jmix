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

package io.jmix.autoconfigure.restds;

import io.jmix.core.CoreConfiguration;
import io.jmix.core.security.AddonAuthenticationManagerSupplier;
import io.jmix.restds.RestDsConfiguration;
import io.jmix.restds.impl.RestAuthenticationManagerSupplier;
import io.jmix.restds.impl.RestPasswordAuthenticator;
import io.jmix.security.authentication.StandardAuthenticationProvidersProducer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.userdetails.UserDetailsService;

@AutoConfiguration
@Import({CoreConfiguration.class, RestDsConfiguration.class})
public class RestDsAutoConfiguration {

    @ConditionalOnProperty("jmix.restds.authentication-provider-store")
    @Bean("restds_RestAuthenticationManagerSupplier")
    @Order(100)
    public AddonAuthenticationManagerSupplier restAuthenticationManagerSupplier(StandardAuthenticationProvidersProducer providersProducer,
                                                                                ApplicationEventPublisher publisher,
                                                                                UserDetailsService userDetailsService,
                                                                                ApplicationContext applicationContext) {

        String storeName = applicationContext.getEnvironment().getProperty("jmix.restds.authentication-provider-store");

        RestPasswordAuthenticator restAuthenticator = applicationContext.getBean(RestPasswordAuthenticator.class);
        restAuthenticator.setDataStoreName(storeName);

        return new RestAuthenticationManagerSupplier(providersProducer, publisher, restAuthenticator, userDetailsService);
    }
}
