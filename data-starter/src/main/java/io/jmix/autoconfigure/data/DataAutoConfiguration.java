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

import io.jmix.core.CoreConfiguration;
import io.jmix.core.Stores;
import io.jmix.core.pessimisticlocking.LockManager;
import io.jmix.data.DataConfiguration;
import io.jmix.data.impl.JmixEntityManagerFactoryBean;
import io.jmix.data.impl.JmixTransactionManager;
import io.jmix.data.impl.PersistenceConfigProcessor;
import io.jmix.data.impl.entitycache.StandardQueryCache;
import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.cache.configuration.MutableConfiguration;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Configuration
@Import({CoreConfiguration.class, DataConfiguration.class})
public class DataAutoConfiguration {

    @Bean
    @Primary
    @ConditionalOnMissingBean(name = "entityManagerFactory")
    protected LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource,
                                                                          PersistenceConfigProcessor processor,
                                                                          JpaVendorAdapter jpaVendorAdapter) {
        return new JmixEntityManagerFactoryBean(Stores.MAIN, dataSource, processor, jpaVendorAdapter);
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean(name = "transactionManager")
    protected PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JmixTransactionManager(Stores.MAIN, entityManagerFactory);
    }

    @Bean
    @SuppressWarnings({"rawtypes", "unchecked"})
    JCacheManagerCustomizer queryCacheCustomizer() {
        return cacheManager -> {
            MutableConfiguration configuration = new MutableConfiguration();
            cacheManager.createCache(StandardQueryCache.QUERY_CACHE_NAME, configuration);
        };
    }
}
