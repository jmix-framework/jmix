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

import io.jmix.authserver.AuthServerConfiguration;
import io.jmix.core.CoreConfiguration;
import io.jmix.oidc.OidcConfiguration;
import io.jmix.rest.RestConfiguration;
import io.jmix.rest.security.impl.RestAsResourceServerBeforeInvocationEventListener;
import io.jmix.rest.security.impl.RestOidcResourceServerBeforeInvocationEventListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({CoreConfiguration.class, RestConfiguration.class})
public class RestAutoConfiguration {
    @Bean("rest_RestAsResourceServerBeforeInvocationEventListener")
    @ConditionalOnClass(AuthServerConfiguration.class)
    protected RestAsResourceServerBeforeInvocationEventListener restAsResourceServerBeforeInvocationEventListener() {
        return new RestAsResourceServerBeforeInvocationEventListener();
    }

    @Bean("rest_RestOidcResourceServerBeforeInvocationEventListener")
    @ConditionalOnClass(OidcConfiguration.class)
    protected RestOidcResourceServerBeforeInvocationEventListener restOidcResourceServerBeforeInvocationEventListener() {
        return new RestOidcResourceServerBeforeInvocationEventListener();
    }
}
