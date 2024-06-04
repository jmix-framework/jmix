/*
 * Copyright 2023 Haulmont.
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

package io.jmix.autoconfigure.pessimisticlock;

import io.jmix.core.CoreConfiguration;
import io.jmix.pessimisticlock.LockManager;
import io.jmix.pessimisticlock.PessimisticLockConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import javax.cache.Cache;
import javax.cache.configuration.MutableConfiguration;

@AutoConfiguration
@Import({CoreConfiguration.class, PessimisticLockConfiguration.class})
public class PessimisticLockAutoConfiguration {

    @Bean
    @SuppressWarnings({"rawtypes", "unchecked"})
    JCacheManagerCustomizer lockCacheCustomizer() {
        return cacheManager -> {
            Cache<Object, Object> cache = cacheManager.getCache(LockManager.LOCKS_CACHE_NAME);
            if (cache == null) {
                MutableConfiguration configuration = new MutableConfiguration();
                cacheManager.createCache(LockManager.LOCKS_CACHE_NAME, configuration);
            }
        };
    }
}
