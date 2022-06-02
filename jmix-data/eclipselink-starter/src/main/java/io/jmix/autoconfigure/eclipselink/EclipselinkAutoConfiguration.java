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

package io.jmix.autoconfigure.eclipselink;

import io.jmix.core.CoreConfiguration;
import io.jmix.core.JmixModules;
import io.jmix.core.Resources;
import io.jmix.core.Stores;
import io.jmix.data.DataConfiguration;
import io.jmix.data.impl.JmixEntityManagerFactoryBean;
import io.jmix.eclipselink.impl.entitycache.StandardQueryCache;
import io.jmix.data.persistence.DbmsSpecifics;
import io.jmix.eclipselink.EclipselinkConfiguration;
import io.jmix.eclipselink.impl.JmixEclipselinkTransactionManager;
import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.cache.Cache;
import javax.cache.configuration.MutableConfiguration;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@AutoConfiguration
@Import({CoreConfiguration.class, DataConfiguration.class, EclipselinkConfiguration.class})
public class EclipselinkAutoConfiguration {

    @Bean
    @Primary
    @ConditionalOnMissingBean(name = "entityManagerFactory")
    protected LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource,
                                                                          JpaVendorAdapter jpaVendorAdapter,
                                                                          DbmsSpecifics dbmsSpecifics,
                                                                          JmixModules jmixModules,
                                                                          Resources resources) {
        return new JmixEntityManagerFactoryBean(Stores.MAIN, dataSource, jpaVendorAdapter, dbmsSpecifics, jmixModules, resources);
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean(name = "transactionManager")
    protected PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JmixEclipselinkTransactionManager(Stores.MAIN, entityManagerFactory);
    }

    @Bean
    @SuppressWarnings({"rawtypes", "unchecked"})
    JCacheManagerCustomizer queryCacheCustomizer() {
        return cacheManager -> {
            Cache<Object, Object> cache = cacheManager.getCache(StandardQueryCache.QUERY_CACHE_NAME);
            if (cache == null) {
                MutableConfiguration configuration = new MutableConfiguration();
                cacheManager.createCache(StandardQueryCache.QUERY_CACHE_NAME, configuration);
            }
        };
    }
}
