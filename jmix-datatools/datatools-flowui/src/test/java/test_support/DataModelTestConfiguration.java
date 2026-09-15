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

import io.jmix.core.annotation.JmixModule;
import io.jmix.datatools.DatatoolsConfiguration;
import io.jmix.eclipselink.EclipselinkConfiguration;
import io.jmix.security.SecurityConfiguration;
import io.jmix.securitydata.SecurityDataConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;

@Configuration
@Import({EntityInspectorPolicyTestConfiguration.class, DatatoolsConfiguration.class})
@JmixModule(id = "test_support.datamodel", dependsOn = {SecurityDataConfiguration.class,
        EclipselinkConfiguration.class, SecurityConfiguration.class, DatatoolsConfiguration.class})
public class DataModelTestConfiguration {

    @Bean("test_ThrowingDataModelContributor")
    @Order(1)
    ThrowingDataModelContributor throwingDataModelContributor() {
        return new ThrowingDataModelContributor();
    }

    @Bean("test_StubDataModelContributor")
    @Order(2)
    StubDataModelContributor stubDataModelContributor() {
        return new StubDataModelContributor();
    }
}
