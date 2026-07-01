/*
 * Copyright 2022 Haulmont.
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

package io.jmix.autoconfigure.appsettings;

import io.jmix.appsettings.AppSettingsConfiguration;
import io.jmix.appsettings.AppSettingsTenantProvider;
import io.jmix.multitenancy.core.TenantProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({AppSettingsConfiguration.class})
public class AppSettingsAutoConfiguration {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = "io.jmix.multitenancy.core.TenantProvider")
    static class MultitenancyBridgeConfiguration {

        @Bean
        @ConditionalOnBean(type = "io.jmix.multitenancy.core.TenantProvider")
        @ConditionalOnMissingBean(AppSettingsTenantProvider.class)
        public AppSettingsTenantProvider appSettingsTenantProvider(TenantProvider tenantProvider) {
            return tenantProvider::getCurrentUserTenantId;
        }
    }
}
