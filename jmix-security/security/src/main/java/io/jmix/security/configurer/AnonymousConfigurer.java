/*
 * Copyright 2021 Haulmont.
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

package io.jmix.security.configurer;

import io.jmix.core.CoreProperties;
import io.jmix.core.security.ServiceUserProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;

public class AnonymousConfigurer extends AbstractHttpConfigurer<AnonymousConfigurer, HttpSecurity> {
    @Override
    public void setBuilder(HttpSecurity http) {
        super.setBuilder(http);
        initAnonymous(http);
    }

    protected void initAnonymous(HttpSecurity http) {
        try {
            ApplicationContext applicationContext = http.getSharedObject(ApplicationContext.class);

            CoreProperties coreProperties = applicationContext.getBean(CoreProperties.class);
            ServiceUserProvider serviceUserProvider = applicationContext.getBean(ServiceUserProvider.class);

            http.anonymous(anonymousConfigurer -> {
                anonymousConfigurer.key(coreProperties.getAnonymousAuthenticationTokenKey());
                anonymousConfigurer.principal(serviceUserProvider.getAnonymousUser());
                Collection<? extends GrantedAuthority> anonymousAuthorities = serviceUserProvider.getAnonymousUser().getAuthorities();
                if (!anonymousAuthorities.isEmpty()) {
                    anonymousConfigurer.authorities(new ArrayList<>(serviceUserProvider.getAnonymousUser().getAuthorities()));
                }
            });
        } catch (Exception e) {
            throw new RuntimeException("Error while init security", e);
        }
    }
}
