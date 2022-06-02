/*
 * Copyright 2019 Haulmont.
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

package io.jmix.autoconfigure.cuba;

import com.haulmont.cuba.CubaConfiguration;
import com.haulmont.cuba.core.app.ConfigStorage;
import io.jmix.core.CoreConfiguration;
import io.jmix.data.DataConfiguration;
import io.jmix.datatools.DatatoolsConfiguration;
import io.jmix.datatoolsui.DatatoolsUiConfiguration;
import io.jmix.dynattr.DynAttrConfiguration;
import io.jmix.dynattrui.DynAttrUiConfiguration;
import io.jmix.localfs.LocalFileStorageConfiguration;
import io.jmix.security.SecurityConfiguration;
import io.jmix.ui.UiConfiguration;
import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer;
import org.springframework.context.annotation.Bean;
import io.jmix.uidata.UiDataConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

import javax.cache.Cache;
import javax.cache.configuration.MutableConfiguration;

@AutoConfiguration
@Import({
        CoreConfiguration.class,
        DataConfiguration.class,
        SecurityConfiguration.class,
        UiConfiguration.class,
        DynAttrConfiguration.class,
        DynAttrUiConfiguration.class,
        LocalFileStorageConfiguration.class,
        UiDataConfiguration.class,
        DatatoolsConfiguration.class,
        DatatoolsUiConfiguration.class,
        CubaConfiguration.class})
public class CubaAutoConfiguration {
    @Bean
    @SuppressWarnings({"rawtypes", "unchecked"})
    JCacheManagerCustomizer configStorageCacheCustomizer() {
        return cacheManager -> {
            Cache<Object, Object> cache = cacheManager.getCache(ConfigStorage.CONFIG_STORAGE_CACHE_NAME);
            if (cache == null) {
                MutableConfiguration configuration = new MutableConfiguration();
                cacheManager.createCache(ConfigStorage.CONFIG_STORAGE_CACHE_NAME, configuration);
            }
        };
    }
}
