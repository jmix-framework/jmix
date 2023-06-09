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

package io.jmix.autoconfigure.reportsrest;

import io.jmix.authserver.AuthorizationServerConfiguration;
import io.jmix.core.CoreConfiguration;
import io.jmix.data.DataConfiguration;
import io.jmix.oidc.OidcConfiguration;
import io.jmix.reports.ReportsConfiguration;
import io.jmix.reportsrest.ReportsRestConfiguration;
import io.jmix.reportsrest.security.event.ReportAsResourceServerBeforeInvocationEventListener;
import io.jmix.reportsrest.security.event.ReportOidcResourceServerBeforeInvocationEventListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({CoreConfiguration.class, DataConfiguration.class, ReportsConfiguration.class, ReportsRestConfiguration.class})
public class ReportsRestAutoConfiguration {

    @Bean
    @ConditionalOnClass(AuthorizationServerConfiguration.class)
    protected ReportAsResourceServerBeforeInvocationEventListener reportBeforeInvocationEventListener() {
        return new ReportAsResourceServerBeforeInvocationEventListener();
    }

    @Bean
    @ConditionalOnClass(OidcConfiguration.class)
    protected ReportOidcResourceServerBeforeInvocationEventListener reportBeforeResourceServerApiInvocationEventListener() {
        return new ReportOidcResourceServerBeforeInvocationEventListener();
    }
}
