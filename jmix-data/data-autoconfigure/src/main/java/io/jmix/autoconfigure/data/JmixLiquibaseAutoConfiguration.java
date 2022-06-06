/*
 * Copyright 2020 Haulmont.
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

package io.jmix.autoconfigure.data;

import io.jmix.core.Stores;
import io.jmix.data.impl.liquibase.LiquibaseChangeLogProcessor;
import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.autoconfigure.AutoConfiguration;

import javax.sql.DataSource;

@AutoConfiguration
@ConditionalOnClass({SpringLiquibase.class})
@AutoConfigureAfter({DataSourceAutoConfiguration.class})
public class JmixLiquibaseAutoConfiguration {

    @Bean("jmix_LiquibaseProperties")
    @ConfigurationProperties(prefix = "jmix.liquibase")
    @ConditionalOnClass({SpringLiquibase.class})
    @ConditionalOnMissingBean(name = "jmix_LiquibaseProperties")
    public LiquibaseProperties liquibaseProperties() {
        return new LiquibaseProperties();
    }

    @Bean(name = "jmix_Liquibase")
    @ConditionalOnClass({SpringLiquibase.class})
    @ConditionalOnMissingBean(name = "jmix_Liquibase")
    public SpringLiquibase liquibase(DataSource dataSource,
                                     LiquibaseChangeLogProcessor processor,
                                     @Qualifier("jmix_LiquibaseProperties") LiquibaseProperties properties) {
        return JmixLiquibaseCreator.create(dataSource, properties, processor, Stores.MAIN);
    }
}
