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

package test_support;

import io.jmix.core.annotation.JmixModule;
import io.jmix.core.security.UserRepository;
import io.jmix.core.security.InMemoryUserRepository;
import io.jmix.rest.RestConfiguration;
import test_support.transformer.RepairJsonTransformerFromVersion;
import test_support.transformer.RepairJsonTransformerToVersion;
import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import javax.sql.DataSource;

@Configuration
@JmixModule(dependsOn = RestConfiguration.class)
@PropertySource("classpath:/application.properties")
public class JmixRestTestConfiguration {

    @Bean(name = "jmix_RepairJsonTransformerToVersion")
    public RepairJsonTransformerToVersion repairJsonTransformerToVersion() {
        return new RepairJsonTransformerToVersion();
    }

    @Bean(name = "jmix_RepairJsonTransformerFromVersion")
    public RepairJsonTransformerFromVersion repairJsonTransformerFromVersion() {
        return new RepairJsonTransformerFromVersion();
    }

    @Bean
    protected DataSource dataSource() {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setUrl("jdbc:hsqldb:mem:testdb");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        return dataSource;
    }

    @Bean
    public UserRepository userRepository() {
        return new InMemoryUserRepository();
    }
}
