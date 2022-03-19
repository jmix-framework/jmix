/*
 * Copyright 2019 Haulmont.
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

package io.jmix.eclipselink;

import io.jmix.core.EntitySystemStateSupport;
import io.jmix.core.PersistentAttributesLoadChecker;
import io.jmix.core.annotation.JmixModule;
import io.jmix.data.DataConfiguration;
import io.jmix.eclipselink.impl.DataEntitySystemStateSupport;
import io.jmix.eclipselink.impl.DataPersistentAttributesLoadChecker;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@ComponentScan
@ConfigurationPropertiesScan
@JmixModule(dependsOn = DataConfiguration.class)
@EnableTransactionManagement
public class EclipselinkConfiguration {
    @Bean("data_PersistentAttributesLoadChecker")
    @Primary
    protected PersistentAttributesLoadChecker persistentAttributesLoadChecker(ApplicationContext applicationContext) {
        return new DataPersistentAttributesLoadChecker(applicationContext);
    }

    @Bean("data_EntitySystemStateSupport")
    @Primary
    protected EntitySystemStateSupport entitySystemStateSupport() {
        return new DataEntitySystemStateSupport();
    }
}
