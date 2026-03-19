/*
 * Copyright 2026 Haulmont.
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

package io.jmix.saml.config;

import io.jmix.saml.SamlVaadinWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

/**
 * Interface that allows to perform additional Spring Security configuration within SAML configuration.
 * All beans that implement this interface will be automatically applied
 * during SAML configuration within {@link SamlVaadinWebSecurity}.
 */
public interface SamlHttpSecurityConfigurer {

    void configure(HttpSecurity http) throws Exception;
}
