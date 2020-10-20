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

package test_support;

import com.haulmont.cuba.CubaConfiguration;
import io.jmix.core.CoreConfiguration;
import io.jmix.core.Stores;
import io.jmix.core.security.UserRepository;
import io.jmix.core.security.impl.InMemoryUserRepository;
import io.jmix.data.DataConfiguration;
import io.jmix.data.impl.JmixEntityManagerFactoryBean;
import io.jmix.data.impl.JmixTransactionManager;
import io.jmix.data.impl.PersistenceConfigProcessor;
import io.jmix.dynattr.DynAttrConfiguration;
import io.jmix.dynattrui.DynAttrUiConfiguration;
import io.jmix.email.EmailConfiguration;
import io.jmix.email.Emailer;
import io.jmix.email.JmixMailSender;
import io.jmix.fsfilestorage.FileSystemFileStorageConfiguration;
import io.jmix.security.SecurityConfiguration;
import io.jmix.securityui.SecurityUiConfiguration;
import io.jmix.ui.UiConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.*;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.scripting.ScriptEvaluator;
import org.springframework.scripting.groovy.GroovyScriptEvaluator;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Configuration
@Import({CoreConfiguration.class, DataConfiguration.class,  EmailConfiguration.class, CubaConfiguration.class,
        UiConfiguration.class, FileSystemFileStorageConfiguration.class, SecurityConfiguration.class,
        SecurityUiConfiguration.class, DynAttrConfiguration.class, DynAttrUiConfiguration.class})
@PropertySource("classpath:/test_support/test-app.properties")
public class EmailTestConfiguration {

    @Bean
    @Primary
    DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
                .generateUniqueName(true)
                .setType(EmbeddedDatabaseType.HSQL)
                .build();
    }

    @Bean
    @Primary
    LocalContainerEntityManagerFactoryBean entityManagerFactory(
            DataSource dataSource, PersistenceConfigProcessor processor, JpaVendorAdapter jpaVendorAdapter) {
        return new JmixEntityManagerFactoryBean(Stores.MAIN, dataSource, processor, jpaVendorAdapter);
    }

    @Bean
    @Primary
    PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JmixTransactionManager(Stores.MAIN, entityManagerFactory);
    }

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager();
    }

    @Bean
    @Primary
    public Emailer emailerApi() {
        return new TestEmailerImpl();
    }


    @Bean("mailSendTaskExecutor")
    public TaskExecutor taskExecutor() {
        return new SyncTaskExecutor();
    }

    @Bean
    @Primary
    public JavaMailSender jmixMailSender() {
        return new TestMailSender();
    }

    @Bean
    public ScriptEvaluator scriptEvaluator() {
        return new GroovyScriptEvaluator();
    }

    @Bean
    public UserRepository userRepository() {
        return new InMemoryUserRepository();
    }
}
