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

package io.jmix.autoconfigure.security;

import io.jmix.core.CoreConfiguration;
import io.jmix.security.SecurityConfiguration;
import io.jmix.security.role.ResourceRoleRepository;
import io.jmix.security.role.RowLevelRoleRepository;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import javax.cache.Cache;
import javax.cache.configuration.MutableConfiguration;

@AutoConfiguration
@Import({CoreConfiguration.class, SecurityConfiguration.class})
public class SecurityAutoConfiguration {

    @Bean
    @SuppressWarnings({"rawtypes", "unchecked"})
    JCacheManagerCustomizer resourceRolesCacheCustomizer() {
        return cacheManager -> {
            Cache<Object, Object> cache = cacheManager.getCache(ResourceRoleRepository.RESOURCE_ROLES_CACHE_NAME);
            if (cache == null) {
                MutableConfiguration configuration = new MutableConfiguration();
                cacheManager.createCache(ResourceRoleRepository.RESOURCE_ROLES_CACHE_NAME, configuration);
            }
        };
    }

    @Bean
    @SuppressWarnings({"rawtypes", "unchecked"})
    JCacheManagerCustomizer rowLevelRolesCacheCustomizer() {
        return cacheManager -> {
            Cache<Object, Object> cache = cacheManager.getCache(RowLevelRoleRepository.ROW_LEVEL_ROLES_CACHE_NAME);
            if (cache == null) {
                MutableConfiguration configuration = new MutableConfiguration();
                cacheManager.createCache(RowLevelRoleRepository.ROW_LEVEL_ROLES_CACHE_NAME, configuration);
            }
        };
    }

}
