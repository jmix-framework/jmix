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

package io.jmix.security;

import io.jmix.security.configurer.StandardSecurityConfigurer;
import org.springframework.beans.factory.annotation.BeanFactoryAnnotationUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.config.annotation.SecurityConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import java.util.Map;

public class SecurityConfigurers {

    public static StandardSecurityConfigurer empty() {
        return new StandardSecurityConfigurer();
    }

    public static StandardSecurityConfigurer uiSecurity() {
        return new StandardSecurityConfigurer()
                .anonymous()
                .sessionManagement()
                .rememberMe()
                .allowUiUrls();
    }

    public static StandardSecurityConfigurer apiSecurity() {
        return new StandardSecurityConfigurer()
                .anonymous()
                .allowApiUrls();
    }

    /**
     * Method finds SecurityConfigurer beans with the given qualifier and applies them to the HttpSecurity
     */
    public static void applySecurityConfigurersWithQualifier(HttpSecurity http, String qualifier) throws Exception {
        ConfigurableApplicationContext applicationContext = (ConfigurableApplicationContext) http.getSharedObject(ApplicationContext.class);
        Map<String, SecurityConfigurer> beans = BeanFactoryAnnotationUtils.qualifiedBeansOfType(applicationContext.getBeanFactory(), SecurityConfigurer.class, qualifier);
        for (SecurityConfigurer configurer : beans.values()) {
            http.apply(configurer);
        }
    }
}
