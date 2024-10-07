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
 * Settings can be configured for all search indexes ({@link BaseIndexSettingsConfigurationContext#getCommonIndexSettingsBuilder()})
 * or for index related to specific entity ({@link BaseIndexSettingsConfigurationContext#getEntityIndexSettingsBuilder(Class)}).
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
     * Provides builder to set settings for all search indexes.
     *
     * @return Index settings builder
     */
    public T getCommonIndexSettingsBuilder() {
        return commonIndexSettingsBuilder;
    }

    public A getCommonAnalysisBuilder() {
        return commonAnalysisBuilder;
    }

    /**
     * Provides builder to set settings for index related to provided entity.
     * All necessary settings should be configured explicitly - they will not be merged with the common ones.
     *
     * @param entityClass entity class
     * @return ES index settings builder
     */
    public T getEntityIndexSettingsBuilder(Class<?> entityClass) {
        return specificIndexSettingsBuilders.computeIfAbsent(entityClass, key -> indexSettingsBuilderGenerator.get());
    }

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
