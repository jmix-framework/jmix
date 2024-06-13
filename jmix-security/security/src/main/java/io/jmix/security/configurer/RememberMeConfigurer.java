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

import org.springframework.context.ApplicationContext;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.RememberMeServices;

/**
 * @deprecated use {@link io.jmix.security.util.JmixHttpSecurityUtils#configureRememberMe(HttpSecurity)}
 */
@Deprecated(since = "2.3", forRemoval = true)
public class RememberMeConfigurer extends AbstractHttpConfigurer<RememberMeConfigurer, HttpSecurity> {

    @Override
    public void setBuilder(HttpSecurity http) {
        super.setBuilder(http);
        initRememberMe(http);
    }

    private void initRememberMe(HttpSecurity http) {
        try {
            ApplicationContext applicationContext = http.getSharedObject(ApplicationContext.class);

            RememberMeServices rememberMeServices = applicationContext.getBean(RememberMeServices.class);

            http.rememberMe().rememberMeServices(rememberMeServices);
        } catch (Exception e) {
            throw new RuntimeException("Error while init security", e);
        }
    }
}
