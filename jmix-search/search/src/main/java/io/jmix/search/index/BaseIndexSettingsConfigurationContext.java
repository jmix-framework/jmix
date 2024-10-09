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
 * Class provides logic of index settings & analysis configuration.
 * <p>
 * <ul>
 *  <li>Index settings for all indexes can be configured via {@link BaseIndexSettingsConfigurationContext#getCommonIndexSettingsBuilder()}</li>
 *  <li>Index settings for specific index can be configured via {@link BaseIndexSettingsConfigurationContext#getEntityIndexSettingsBuilder(Class)}</li>
 *  <li>Analysis settings for all indexes can be configured via {@link BaseIndexSettingsConfigurationContext#getCommonAnalysisBuilder()}</li>
 *  <li>Analysis settings for specific index can be configured via {@link BaseIndexSettingsConfigurationContext#getEntityAnalysisBuilder(Class)}</li>
 * </ul>
 * <p>
 * NOTE: do not call .build() method of acquired builders within your configurer.
 */
public class BaseIndexSettingsConfigurationContext<T, A> {

    protected final T commonIndexSettingsBuilder;
    protected final A commonAnalysisBuilder;
    protected final Map<Class<?>, T> specificIndexSettingsBuilders;
    protected final Map<Class<?>, A> specificAnalysisBuilders;

    protected final Supplier<T> indexSettingsBuilderGenerator;
    protected final Supplier<A> analysisBuilderGenerator;

    public BaseIndexSettingsConfigurationContext(Supplier<T> indexSettingsBuilderGenerator,
                                                 Supplier<A> analysisBuilderGenerator) {
        this.commonIndexSettingsBuilder = indexSettingsBuilderGenerator.get();
        this.commonAnalysisBuilder = analysisBuilderGenerator.get();
        this.specificIndexSettingsBuilders = new HashMap<>();
        this.specificAnalysisBuilders = new HashMap<>();
        this.indexSettingsBuilderGenerator = indexSettingsBuilderGenerator;
        this.analysisBuilderGenerator = analysisBuilderGenerator;
    }

    /**
     * Provides builder to configure index settings for all indexes.
     *
     * @return Index settings builder
     */
    public T getCommonIndexSettingsBuilder() {
        return commonIndexSettingsBuilder;
    }

    /**
     * Provides builder to configure analysis settings for all indexes.
     *
     * @return Analysis settings builder
     */
    public A getCommonAnalysisBuilder() {
        return commonAnalysisBuilder;
    }

    /**
     * Provides builder to configure index settings for index related to provided entity.
     *
     * @param entityClass entity class
     * @return Index settings builder
     */
    public T getEntityIndexSettingsBuilder(Class<?> entityClass) {
        return specificIndexSettingsBuilders.computeIfAbsent(entityClass, key -> indexSettingsBuilderGenerator.get());
    }

    /**
     * Provides builder to configure analysis settings for index related to provided entity.
     *
     * @param entityClass entity class
     * @return Analysis settings builder
     */
    public A getEntityAnalysisBuilder(Class<?> entityClass) {
        return specificAnalysisBuilders.computeIfAbsent(entityClass, key -> analysisBuilderGenerator.get());
    }

    public Map<Class<?>, T> getAllSpecificIndexSettingsBuilders() {
        return new ConcurrentHashMap<>(specificIndexSettingsBuilders);
    }

    public Map<Class<?>, A> getAllSpecificAnalysisBuilders() {
        return new ConcurrentHashMap<>(specificAnalysisBuilders);
    }
}
