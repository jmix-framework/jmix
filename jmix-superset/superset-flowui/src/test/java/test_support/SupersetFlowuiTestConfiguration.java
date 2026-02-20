/*
 * Copyright 2024 Haulmont.
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
import io.jmix.core.annotation.JmixModule;
import io.jmix.flowui.FlowuiConfiguration;
import io.jmix.superset.SupersetConfiguration;
import io.jmix.superset.SupersetTokenManager;
import io.jmix.superset.client.cookie.SupersetCookieManager;
import io.jmix.supersetflowui.SupersetFlowuiConfiguration;
import io.jmix.testsupport.config.CommonCoreTestConfiguration;
import io.jmix.testsupport.config.CoreSecurityTestConfiguration;
import io.jmix.flowui.testassist.FlowuiServletTestBeans;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Import({FlowuiConfiguration.class, CoreConfiguration.class, SupersetFlowuiConfiguration.class,
        SupersetConfiguration.class, CommonCoreTestConfiguration.class,
        FlowuiServletTestBeans.class, CoreSecurityTestConfiguration.class})
@PropertySource("classpath:/test_support/application-test.properties")
@JmixModule(dependsOn = SupersetFlowuiConfiguration.class)
public class SupersetFlowuiTestConfiguration {

    @Bean
    public SupersetCookieManager supersetCookieManager() {
        return new SupersetCookieManager();
    }

    @Bean
    @Primary
    public SupersetTokenManager tokenManager() {
        return new TestSupersetTokenManager();
    }

    @Bean("test_TestDatasetConstraintsProvider")
    public TestDatasetConstraintsProvider testDatasetConstraintsProvider() {
        return new TestDatasetConstraintsProvider();
    }
}
