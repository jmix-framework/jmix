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

package database_initialization;

import io.jmix.autoconfigure.data.JmixLiquibaseAutoConfiguration;
import io.jmix.autoconfigure.eclipselink.EclipselinkAutoConfiguration;
import io.jmix.core.cluster.ClusterApplicationEventChannelSupplier;
import io.jmix.core.cluster.LocalApplicationEventChannelSupplier;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.cache.autoconfigure.CacheAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.JdbcTemplateAutoConfiguration;
import org.springframework.boot.liquibase.autoconfigure.LiquibaseAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks that the beans reading the database through JPA are created after the Liquibase migrations,
 * the way Spring Boot orders JDBC beans after them.
 */
public class DatabaseInitializationOrderTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withPropertyValues(
                    "spring.datasource.url=jdbc:hsqldb:mem:dbInitOrderTest-" + System.nanoTime(),
                    "spring.datasource.username=sa",
                    "spring.cache.type=simple",
                    "main.liquibase.change-log=classpath:database_initialization/test-changelog.xml")
            .withUserConfiguration(TestConfiguration.class);

    /**
     * The auto-configurations of a Jmix application that touch the database, including those of Spring Boot.
     */
    private final ApplicationContextRunner applicationRunner = contextRunner
            .withConfiguration(AutoConfigurations.of(
                    DataSourceAutoConfiguration.class,
                    JdbcTemplateAutoConfiguration.class,
                    LiquibaseAutoConfiguration.class,
                    JmixLiquibaseAutoConfiguration.class,
                    EclipselinkAutoConfiguration.class,
                    CacheAutoConfiguration.class));

    /**
     * Only the Jmix auto-configurations: nothing of Spring Boot registers the database initialization ordering.
     */
    private final ApplicationContextRunner jmixOnlyRunner = contextRunner
            .withConfiguration(AutoConfigurations.of(
                    DataSourceAutoConfiguration.class,
                    JmixLiquibaseAutoConfiguration.class,
                    EclipselinkAutoConfiguration.class,
                    CacheAutoConfiguration.class));

    @Test
    void entityManagerFactory_dependsOnLiquibase() {
        applicationRunner.run(context -> {
            BeanDefinition emf = context.getBeanFactory().getBeanDefinition("entityManagerFactory");
            BeanDefinition jdbcTemplate = context.getBeanFactory().getBeanDefinition("jdbcTemplate");
            assertThat(jdbcTemplate.getDependsOn()).contains("jmix_Liquibase");
            assertThat(emf.getDependsOn()).contains("jmix_Liquibase");
        });
    }

    @Test
    void entityManagerFactory_withoutBootDatabaseAutoConfigurations_dependsOnLiquibase() {
        jmixOnlyRunner.run(context -> {
            BeanDefinition emf = context.getBeanFactory().getBeanDefinition("entityManagerFactory");
            assertThat(emf.getDependsOn()).contains("jmix_Liquibase");
        });
    }

    @Test
    void jpaQueryInPostConstruct_freshDatabase_runsAfterMigrations() {
        applicationRunner.withUserConfiguration(EarlyReaderConfiguration.class).run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context.getBean(EarlyReader.class).count).isEqualTo(0L);
        });
    }

    @Configuration(proxyBeanMethods = false)
    static class TestConfiguration {
        @Bean
        ClusterApplicationEventChannelSupplier clusterApplicationEventChannelSupplier() {
            return new LocalApplicationEventChannelSupplier();
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class EarlyReaderConfiguration {
        @Bean
        EarlyReader earlyReader() {
            return new EarlyReader();
        }
    }

    /**
     * Reads a table created by the migrations while the context starts, the way a {@code @PostConstruct}
     * of an application bean would.
     */
    static class EarlyReader {

        @Autowired
        EntityManagerFactory entityManagerFactory;

        long count;

        @PostConstruct
        void readSettings() {
            try (EntityManager em = entityManagerFactory.createEntityManager()) {
                count = ((Number) em.createNativeQuery("select count(*) from TEST_SETTING").getSingleResult()).longValue();
            }
        }
    }
}
