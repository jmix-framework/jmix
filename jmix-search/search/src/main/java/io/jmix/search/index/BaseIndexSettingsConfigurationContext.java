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
 * Class provides logic of index settings and analysis configuration.
 * <p>
 * <ul>
 *  <li>Index settings for all indexes can be configured via {@link #getCommonIndexSettingsBuilder()}</li>
 *  <li>Index settings for specific index can be configured via {@link #getEntityIndexSettingsBuilder(Class)}</li>
 *  <li>Analysis settings for all indexes can be configured via {@link #getCommonAnalysisBuilder()}</li>
 *  <li>Analysis settings for specific index can be configured via {@link #getEntityAnalysisBuilder(Class)}</li>
 * </ul>
 * <p>
 * NOTE: Usage of deprecated api ({@link #getCommonSettingsBuilder()} and {@link #getEntitySettingsBuilder(Class)})
 * will be ignored if any of the actual API above is used or {@link io.jmix.search.index.annotation.ExtendedSearch} is applied
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

    protected final T commonSettingsBuilder;
    protected final Map<Class<?>, T> specificSettingsBuilders;

    public BaseIndexSettingsConfigurationContext(Supplier<T> indexSettingsBuilderGenerator,
                                                 Supplier<A> analysisBuilderGenerator) {
        this.commonIndexSettingsBuilder = indexSettingsBuilderGenerator.get();
        this.commonAnalysisBuilder = analysisBuilderGenerator.get();
        this.specificIndexSettingsBuilders = new HashMap<>();
        this.specificAnalysisBuilders = new HashMap<>();
        this.indexSettingsBuilderGenerator = indexSettingsBuilderGenerator;
        this.analysisBuilderGenerator = analysisBuilderGenerator;

        this.commonSettingsBuilder = indexSettingsBuilderGenerator.get();
        this.specificSettingsBuilders = new HashMap<>();
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

    /**
     * Provides builder to set settings for all search indexes.
     *
     * @return Index settings builder
     * @deprecated This settings will not work correctly with {@link io.jmix.search.index.annotation.ExtendedSearch}.
     * Use {@link #getCommonIndexSettingsBuilder()} to configure index settings
     * and {@link #getCommonAnalysisBuilder()} to configure analysis settings.
     */
    @Deprecated(since = "2.4", forRemoval = true)
    public T getCommonSettingsBuilder() {
        return commonSettingsBuilder;
    }

    /**
     * Provides builder to set settings for index related to provided entity.
     * All necessary settings should be configured explicitly - they will not be merged with the common ones.
     *
     * @param entityClass entity class
     * @return Index settings builder
     * @deprecated This settings will not work correctly with {@link io.jmix.search.index.annotation.ExtendedSearch}.
     * Use {@link #getCommonIndexSettingsBuilder()} to configure index settings
     * and {@link #getCommonAnalysisBuilder()} to configure analysis settings.
     */
    @Deprecated(since = "2.4", forRemoval = true)
    public T getEntitySettingsBuilder(Class<?> entityClass) {
        return specificSettingsBuilders.computeIfAbsent(entityClass, key -> indexSettingsBuilderGenerator.get());
    }

    @Deprecated(since = "2.4", forRemoval = true)
    public Map<Class<?>, T> getAllSpecificSettingsBuilders() {
        return new ConcurrentHashMap<>(specificSettingsBuilders);
    }
}
