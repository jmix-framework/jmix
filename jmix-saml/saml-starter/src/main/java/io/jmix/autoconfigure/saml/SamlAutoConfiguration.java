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

package io.jmix.autoconfigure.saml;

import io.jmix.saml.SamlConfiguration;
import io.jmix.saml.converter.SamlResponseAuthenticationConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.saml2.provider.service.authentication.OpenSamlAuthenticationProvider;

@AutoConfiguration
@Import({SamlConfiguration.class})
@ConditionalOnProperty(name = "jmix.saml.use-default-configuration", matchIfMissing = true)
public class SamlAutoConfiguration {

    @EnableWebSecurity
    public static class JmixSamlConfiguration extends WebSecurityConfigurerAdapter {

        //todo protect REST API
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            OpenSamlAuthenticationProvider authenticationProvider = new OpenSamlAuthenticationProvider();
            authenticationProvider.setResponseAuthenticationConverter(new SamlResponseAuthenticationConverter());

            http
                    .authorizeRequests()
                    .anyRequest().authenticated()
                    .and()
                    .saml2Login()
                    .authenticationManager(new ProviderManager(authenticationProvider));
            http.csrf().disable();
        }
    }

}

