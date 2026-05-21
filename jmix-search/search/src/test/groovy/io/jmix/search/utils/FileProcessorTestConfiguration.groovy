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

package io.jmix.search.utils

import io.jmix.core.FileStorage
import io.jmix.core.FileStorageLocator
import io.jmix.core.annotation.JmixModule
import io.jmix.core.cluster.ClusterApplicationEventChannelSupplier
import io.jmix.core.cluster.LocalApplicationEventChannelSupplier
import io.jmix.localfs.LocalFileStorage
import io.jmix.localfs.LocalFileStorageConfiguration
import org.springframework.cache.CacheManager
import org.springframework.cache.concurrent.ConcurrentMapCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
@JmixModule(dependsOn = LocalFileStorageConfiguration)
public class FileProcessorTestConfiguration {

    @Bean
    public FileProcessor fileProcessor(FileStorageLocator fileStorageLocator) {
        new FileProcessor(fileStorageLocator)
    }

    @Bean
    @Primary
    public FileStorage fileStorage() {
        new LocalFileStorage('testFs')
    }

    @Bean
    public CacheManager cacheManager() {
        new ConcurrentMapCacheManager()
    }

    @Bean
    public ClusterApplicationEventChannelSupplier clusterApplicationEventChannelSupplier() {
        return new LocalApplicationEventChannelSupplier();
    }
}
