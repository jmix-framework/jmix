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
import io.jmix.data.DataConfiguration;
import io.jmix.eclipselink.EclipselinkConfiguration;
import io.jmix.messagetemplates.MessageTemplateProperties;
import io.jmix.messagetemplates.MessageTemplatesConfiguration;
import io.jmix.testsupport.config.CommonCoreTestConfiguration;
import io.jmix.testsupport.config.CoreSecurityTestConfiguration;
import io.jmix.testsupport.config.HsqlMemDataSourceTestConfiguration;
import io.jmix.testsupport.config.JpaMainStoreTestConfiguration;
import io.jmix.testsupport.config.LiquibaseTestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.Locale;

@Configuration
@Import({CoreConfiguration.class, DataConfiguration.class, EclipselinkConfiguration.class,
        MessageTemplatesConfiguration.class, CommonCoreTestConfiguration.class, CoreSecurityTestConfiguration.class,
        HsqlMemDataSourceTestConfiguration.class, JpaMainStoreTestConfiguration.class, LiquibaseTestConfiguration.class})
@JmixModule
public class MessageTemplatesTestConfiguration {

    @Bean
    freemarker.template.Configuration configuration(MessageTemplateProperties messageTemplateProperties) {
        freemarker.template.Configuration configuration =
                new freemarker.template.Configuration(messageTemplateProperties.getFreemarkerVersion());
        configuration.setDefaultEncoding("UTF-8");
        configuration.setLocale(Locale.US);
        return configuration;
    }
}
