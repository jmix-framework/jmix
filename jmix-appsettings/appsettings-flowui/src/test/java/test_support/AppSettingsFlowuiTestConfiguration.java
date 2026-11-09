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
import io.jmix.appsettingsflowui.AppSettingsFlowUiConfiguration;
import io.jmix.core.CoreConfiguration;
import io.jmix.core.annotation.JmixModule;
import io.jmix.data.DataConfiguration;
import io.jmix.eclipselink.EclipselinkConfiguration;
import io.jmix.flowui.FlowuiConfiguration;
import io.jmix.flowui.testassist.FlowuiServletTestBeans;
import io.jmix.testsupport.config.CommonCoreTestConfiguration;
import io.jmix.testsupport.config.CoreSecurityTestConfiguration;
import io.jmix.testsupport.config.HsqlMemDataSourceTestConfiguration;
import io.jmix.testsupport.config.JpaMainStoreTestConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Import({CoreConfiguration.class, DataConfiguration.class, EclipselinkConfiguration.class,
        FlowuiConfiguration.class, AppSettingsConfiguration.class, AppSettingsFlowUiConfiguration.class,
        CommonCoreTestConfiguration.class, CoreSecurityTestConfiguration.class,
        HsqlMemDataSourceTestConfiguration.class, JpaMainStoreTestConfiguration.class,
        FlowuiServletTestBeans.class})
@PropertySource("classpath:/test_support/test-app.properties")
@JmixModule(dependsOn = {AppSettingsFlowUiConfiguration.class, EclipselinkConfiguration.class})
public class AppSettingsFlowuiTestConfiguration {
}
