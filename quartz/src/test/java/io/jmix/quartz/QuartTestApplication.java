package io.jmix.quartz;

import io.jmix.core.JmixModules;
import io.jmix.core.Resources;
import io.jmix.core.Stores;
import io.jmix.core.annotation.JmixModule;
import io.jmix.data.impl.JmixEntityManagerFactoryBean;
import io.jmix.data.impl.JmixTransactionManager;
import io.jmix.data.impl.liquibase.JmixLiquibase;
import io.jmix.data.impl.liquibase.LiquibaseChangeLogProcessor;
import io.jmix.data.persistence.DbmsSpecifics;
import liquibase.integration.spring.SpringLiquibase;
import org.quartz.*;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@SpringBootApplication
@JmixModule(id = "quartz-tests")
public class QuartTestApplication {

    @Bean
    @Primary
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager();
    }

    @Bean
    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
                .generateUniqueName(true)
                .setType(EmbeddedDatabaseType.HSQL)
                .build();
    }

    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource,
                                                                       JpaVendorAdapter jpaVendorAdapter,
                                                                       DbmsSpecifics dbmsSpecifics,
                                                                       JmixModules jmixModules,
                                                                       Resources resources) {
        return new JmixEntityManagerFactoryBean(Stores.MAIN, dataSource, jpaVendorAdapter, dbmsSpecifics, jmixModules, resources);
    }

    @Bean
    @Primary
    PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JmixTransactionManager(Stores.MAIN, entityManagerFactory);
    }

    @Bean
    @Primary
    public SpringLiquibase liquibase(DataSource dataSource, LiquibaseChangeLogProcessor processor) {
        JmixLiquibase liquibase = new JmixLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLogContent(processor.createMasterChangeLog(Stores.MAIN));
        return liquibase;
    }

    static class MyQuartzJob implements Job {

        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            //do nothing
        }
    }

    @Bean
    public JobDetail testJob() {
        return JobBuilder.newJob()
                .withIdentity("testJob")
                .ofType(MyQuartzJob.class)
                .storeDurably()
                .build();
    }
}
