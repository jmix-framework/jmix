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

package test_support;

import io.jmix.core.CoreConfiguration;
import io.jmix.core.JmixOrder;
import io.jmix.core.annotation.JmixModule;
import io.jmix.data.DataConfiguration;
import io.jmix.eclipselink.EclipselinkConfiguration;
import io.jmix.flowui.FlowuiConfiguration;
import io.jmix.flowui.settings.UserSettingsCache;
import io.jmix.flowui.settings.UserSettingsService;
import io.jmix.flowuidata.FlowuiDataConfiguration;
import io.jmix.testsupport.config.CommonCoreTestBeans;
import io.jmix.testsupport.config.CoreSecurityTestConfiguration;
import io.jmix.testsupport.config.HsqlMemDataSourceTestBeans;
import io.jmix.testsupport.config.JpaMainStoreTestBeans;
import io.jmix.testsupport.config.LiquibaseTestBeans;
import io.jmix.flowui.testassist.FlowuiServletTestBeans;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.annotation.Order;
import overridden_settings.test_support.TestJmixDetailsSettingsBinder;

@Configuration
@Import({FlowuiConfiguration.class, EclipselinkConfiguration.class, DataConfiguration.class,
        CoreConfiguration.class, FlowuiDataConfiguration.class,
        CommonCoreTestBeans.class, HsqlMemDataSourceTestBeans.class,
        JpaMainStoreTestBeans.class, LiquibaseTestBeans.class,
        FlowuiServletTestBeans.class, CoreSecurityTestConfiguration.class})
@PropertySource("classpath:/test_support/test-flowui-data-app.properties")
@JmixModule
public class FlowuiDataTestConfiguration {

    @Bean("test_UserSettingsCache")
    @Primary
    UserSettingsCache userSettingsCache(UserSettingsService userSettingsService) {
        return new TestUserSettingsCacheImpl(userSettingsService);
    }

    @Order(JmixOrder.HIGHEST_PRECEDENCE + 100)
    @Bean("test_TestJmixDetailsSettingsBinder")
    TestJmixDetailsSettingsBinder testJmixDetailsSettingsBinder() {
        return new TestJmixDetailsSettingsBinder();
    }
}
