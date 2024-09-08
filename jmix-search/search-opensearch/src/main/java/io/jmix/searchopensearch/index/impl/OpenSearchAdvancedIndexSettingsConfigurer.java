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

package io.jmix.searchopensearch.index.impl;

import io.jmix.search.index.IndexConfiguration;
import io.jmix.search.index.impl.BaseAdvancedIndexSettingsConfigurer;
import io.jmix.search.index.mapping.AdvancedSearchSettings;
import io.jmix.search.index.mapping.IndexConfigurationManager;
import io.jmix.searchopensearch.index.OpenSearchIndexSettingsConfigurationContext;
import io.jmix.searchopensearch.index.OpenSearchIndexSettingsConfigurer;
import org.opensearch.client.opensearch.indices.IndexSettings;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;

//@Component("search_OpenSearchAdvancedIndexSettingsConfigurer")
public class OpenSearchAdvancedIndexSettingsConfigurer
        extends BaseAdvancedIndexSettingsConfigurer<OpenSearchIndexSettingsConfigurationContext>
        implements OpenSearchIndexSettingsConfigurer {

    /*@Autowired
    protected IndexConfigurationManager indexConfigurationManager;*/

    public OpenSearchAdvancedIndexSettingsConfigurer(IndexConfigurationManager indexConfigurationManager) {
        super(indexConfigurationManager);
    }

    @Override
    public void configure(OpenSearchIndexSettingsConfigurationContext context) {
        Collection<IndexConfiguration> indexConfigurations = indexConfigurationManager.getAllIndexConfigurations();
        indexConfigurations.forEach(config -> configureForIndexConfiguration(context, config));
    }

    @Override
    public boolean isSystem() {
        return true;
    }

    protected void configureForIndexConfiguration(OpenSearchIndexSettingsConfigurationContext context,
                                                  IndexConfiguration indexConfiguration) {
        AdvancedSearchSettings advancedSearchSettings = indexConfiguration.getAdvancedSearchSettings();
        if (advancedSearchSettings != null && advancedSearchSettings.isEnabled()) {
            int edgeNGramMin = advancedSearchSettings.getEdgeNGramMin();
            int edgeNGramMax = advancedSearchSettings.getEdgeNGramMax();

            String prefixFilterName = resolvePrefixFilterName();
            String prefixAnalyzerName = resolvePrefixAnalyzerName();
            String prefixSearchAnalyzerName = resolvePrefixSearchAnalyzerName();
            String prefixTokenizerName = resolvePrefixTokenizerName();

            IndexSettings.Builder builder = context.getEntitySettingsBuilder(indexConfiguration.getEntityClass());
            builder.analysis(analysisBuilder -> analysisBuilder
                    .filter(prefixFilterName, filterBuilder ->
                            filterBuilder.definition(filterDefinitionBuilder ->
                                    filterDefinitionBuilder.edgeNgram(edgeNGramFilterBuilder -> {
                                        return edgeNGramFilterBuilder
                                                .minGram(edgeNGramMin)
                                                .maxGram(edgeNGramMax)
                                                .preserveOriginal(true); //todo: .preserveOriginal(true) - emit original token
                                    })
                            )
                    )
                    .analyzer(prefixAnalyzerName, analyzerBuilder ->
                            analyzerBuilder.custom(customAnalyzerBuilder ->
                                    customAnalyzerBuilder
                                            .tokenizer(prefixTokenizerName)
                                            .filter(LOWERCASE_FILTER_NAME, prefixFilterName)
                            )
                    )
                    .analyzer(prefixSearchAnalyzerName, analyzerBuilder ->
                            analyzerBuilder.custom(customAnalyzerBuilder ->
                                    customAnalyzerBuilder.tokenizer(prefixTokenizerName).filter(LOWERCASE_FILTER_NAME)
                            )
                    )
            );
        }
    }
}
