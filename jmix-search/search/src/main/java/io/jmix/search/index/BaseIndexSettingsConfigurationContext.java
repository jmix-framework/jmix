/*
 * Copyright 2021 Haulmont.
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

package io.jmix.search.index;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Allows to configure index settings (including analysis).
 * <p>
 * Settings can be configured for all search indexes ({@link BaseIndexSettingsConfigurationContext#getCommonSettingsBuilder()})
 * or for index related to specific entity ({@link BaseIndexSettingsConfigurationContext#getEntitySettingsBuilder(Class)}).
 */
public class BaseIndexSettingsConfigurationContext<T> {

    private final T commonSettingsBuilder;
    private final Map<Class<?>, T> specificSettingsBuilders;

    private final Supplier<T> builderGenerator;

    public BaseIndexSettingsConfigurationContext(Supplier<T> builderGenerator) {
        this.commonSettingsBuilder = builderGenerator.get();
        this.specificSettingsBuilders = new HashMap<>();
        this.builderGenerator = builderGenerator;
    }

    /**
     * Provides builder to set settings for all search indexes.
     *
     * @return Index settings builder
     */
    public T getCommonSettingsBuilder() {
        return commonSettingsBuilder;
    }

    /**
     * Provides builder to set settings for index related to provided entity.
     * All necessary settings should be configured explicitly - they will not be merged with the common ones.
     *
     * @param entityClass entity class
     * @return ES index settings builder
     */
    public T getEntitySettingsBuilder(Class<?> entityClass) {
        return specificSettingsBuilders.computeIfAbsent(entityClass, key -> builderGenerator.get());
    }

    public Map<Class<?>, T> getAllSpecificSettingsBuilders() {
        return new ConcurrentHashMap<>(specificSettingsBuilders);
    }
}
