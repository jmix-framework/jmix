/*
 * Copyright 2024 Haulmont.
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

package io.jmix.search.index.impl;

import io.jmix.search.index.IndexConfiguration;
import io.jmix.search.index.IndexSettingsConfigurer;
import io.jmix.search.index.mapping.AdvancedSearchSettings;
import io.jmix.search.index.mapping.IndexConfigurationManager;

import java.util.Collection;

public abstract class BaseAdvancedIndexSettingsConfigurer<TContext> implements IndexSettingsConfigurer<TContext> {

    public static final String LOWERCASE_FILTER_NAME = "lowercase";

    protected final IndexConfigurationManager indexConfigurationManager;

    public BaseAdvancedIndexSettingsConfigurer(IndexConfigurationManager indexConfigurationManager) {
        this.indexConfigurationManager = indexConfigurationManager;
    }

    @Override
    public void configure(TContext context) {
        Collection<IndexConfiguration> indexConfigurations = indexConfigurationManager.getAllIndexConfigurations();
        indexConfigurations.forEach(config -> configureForIndexConfiguration(context, config));
    }

    protected abstract void configureForIndexConfiguration(TContext context, IndexConfiguration indexConfiguration);


    //todo move resolveXXX methods data to AdvancedSearchSettings?
    protected String resolvePrefixAnalyzerName() {
        return "_jmix_prefix_analyzer";
    }

    protected String resolvePrefixSearchAnalyzerName() {
        return "_jmix_prefix_search_analyzer";
    }

    protected String resolvePrefixTokenizerName() {
        return "standard";
    }

    protected String resolvePrefixFilterName() {
        return "_jmix_prefix_filter";
    }
}
