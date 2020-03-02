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

package persistence_beans;

import io.jmix.autoconfigure.data.JmixDataAutoConfiguration;
import io.jmix.core.Stores;
import io.jmix.data.impl.JmixTransactionManager;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;

import static org.assertj.core.api.Assertions.assertThat;

public class DataAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(DataSourceAutoConfiguration.class, JmixDataAutoConfiguration.class))
            .withPropertyValues("jmix.dbmsType=hsql", "spring.datasource.url=jdbc:hsqldb:mem:testdb", "spring.datasource.username=sa");

    @Test
    public void testCustomBeans() {
        this.contextRunner.withUserConfiguration(UserConfiguration.class).run((context) -> {
            assertThat(context).hasSingleBean(PlatformTransactionManager.class);
            assertThat(context).getBean("transactionManager").isInstanceOf(TestTransactionManager.class);
        });
    }

    static class TestTransactionManager extends JmixTransactionManager {
        public TestTransactionManager(String storeName, EntityManagerFactory entityManagerFactory) {
            super(storeName, entityManagerFactory);
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class UserConfiguration {
        @Bean
        @Primary
        PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
            return new TestTransactionManager(Stores.MAIN, entityManagerFactory);
        }
    }
}
