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

package test_support;

import io.jmix.appsettings.AppSettingsConfiguration;
import io.jmix.appsettings.test_support.TestAppSettingsTenantProvider;
import io.jmix.core.CoreConfiguration;
import io.jmix.core.annotation.JmixModule;
import io.jmix.data.DataConfiguration;
import io.jmix.eclipselink.EclipselinkConfiguration;
import io.jmix.testsupport.config.CommonCoreTestConfiguration;
import io.jmix.testsupport.config.HsqlEmbeddedDataSourceTestConfiguration;
import io.jmix.testsupport.config.JpaMainStoreTestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Import({CoreConfiguration.class, DataConfiguration.class, EclipselinkConfiguration.class,
        AppSettingsConfiguration.class, CommonCoreTestConfiguration.class,
        HsqlEmbeddedDataSourceTestConfiguration.class, JpaMainStoreTestConfiguration.class})
@PropertySource("classpath:/test_support/test-app.properties")
@JmixModule(id = "io.jmix.appsettings.tenant-provider.test",
        dependsOn = {AppSettingsConfiguration.class, EclipselinkConfiguration.class})
public class AppSettingsTenantProviderOnlyTestConfiguration {

    @Bean
    @Primary
    public TestAppSettingsTenantProvider testAppSettingsTenantProvider() {
        return new TestAppSettingsTenantProvider();
    }
}
