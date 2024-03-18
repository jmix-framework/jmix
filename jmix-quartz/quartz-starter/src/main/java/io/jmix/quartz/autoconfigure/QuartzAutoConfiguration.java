/*
 * Copyright 2022 Haulmont.
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

package io.jmix.quartz.autoconfigure;

import io.jmix.quartz.QuartzConfiguration;
import io.jmix.quartz.service.RunningJobsCache;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import javax.cache.Cache;
import javax.cache.configuration.MutableConfiguration;

@AutoConfiguration
@Import({QuartzConfiguration.class})
public class QuartzAutoConfiguration {
    @Bean
    @SuppressWarnings({"rawtypes", "unchecked"})
    JCacheManagerCustomizer runningJobsCacheCustomizer() {
        return cacheManager -> {
            Cache<Object, Object> cache = cacheManager.getCache(RunningJobsCache.CACHE_NAME);
            if (cache == null) {
                MutableConfiguration configuration = new MutableConfiguration();
                cacheManager.createCache(RunningJobsCache.CACHE_NAME, configuration);
            }
        };
    }
}

