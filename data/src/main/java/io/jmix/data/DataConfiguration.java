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

package io.jmix.data;

import io.jmix.core.BeanLocator;
import io.jmix.core.CoreConfiguration;
import io.jmix.core.EntitySystemStateSupport;
import io.jmix.core.PersistentAttributesLoadChecker;
import io.jmix.core.annotation.JmixModule;
import io.jmix.data.impl.DataPersistentAttributesLoadChecker;
import io.jmix.data.impl.converters.AuditConverters;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.data.convert.Jsr310Converters;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@ComponentScan
@ConfigurationPropertiesScan
@JmixModule(dependsOn = CoreConfiguration.class)
@EnableTransactionManagement
public class DataConfiguration {

    @Bean(name = PersistentAttributesLoadChecker.NAME)
    protected PersistentAttributesLoadChecker persistentAttributesLoadChecker(BeanLocator beanLocator) {
        return new DataPersistentAttributesLoadChecker(beanLocator);
    }

    @Bean(name = EntitySystemStateSupport.NAME)
    protected EntitySystemStateSupport entitySystemStateSupport() {
        return new DataEntitySystemStateSupport();
    }

    @Bean
    @Qualifier("jmix_auditConverter")
    protected ConversionService conversionService() {//todo taimanov maybe another architecture: 1) immutable after creation 2) still easily extendable in apps
        GenericConversionService service = new GenericConversionService();

        service.addConverter(Jsr310Converters.DateToLocalDateTimeConverter.INSTANCE);
        service.addConverter(AuditConverters.DateToLongConverter.INSTANCE);

        service.addConverter(AuditConverters.UserToStringConverter.INSTANCE);


        return service;
    }
}
