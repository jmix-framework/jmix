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

package io.jmix.searchelasticsearch.index.impl;

import co.elastic.clients.elasticsearch.indices.IndexSettingsAnalysis;
import io.jmix.search.index.IndexConfiguration;
import io.jmix.search.index.impl.BaseExtendedIndexSettingsConfigurer;
import io.jmix.search.index.mapping.ExtendedSearchSettings;
import io.jmix.search.index.mapping.IndexConfigurationManager;
import io.jmix.searchelasticsearch.index.ElasticsearchIndexSettingsConfigurationContext;
import io.jmix.searchelasticsearch.index.ElasticsearchIndexSettingsConfigurer;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component("search_ElasticsearchExtendedIndexSettingsConfigurer")
public class ElasticsearchExtendedIndexSettingsConfigurer
        extends BaseExtendedIndexSettingsConfigurer<ElasticsearchIndexSettingsConfigurationContext>
        implements ElasticsearchIndexSettingsConfigurer {

    public ElasticsearchExtendedIndexSettingsConfigurer(IndexConfigurationManager indexConfigurationManager) {
        super(indexConfigurationManager);
    }

    @Override
    public void configure(ElasticsearchIndexSettingsConfigurationContext context) {
        Collection<IndexConfiguration> indexConfigurations = indexConfigurationManager.getAllIndexConfigurations();
        indexConfigurations.forEach(config -> configureForIndexConfiguration(context, config));
    }

    @Override
    public boolean isSystem() {
        return true;
    }

    protected void configureForIndexConfiguration(ElasticsearchIndexSettingsConfigurationContext context,
                                                  IndexConfiguration indexConfiguration) {
        ExtendedSearchSettings extendedSearchSettings = indexConfiguration.getExtendedSearchSettings();
        if (extendedSearchSettings.isEnabled()) {
            int edgeNGramMin = extendedSearchSettings.getEdgeNGramMin();
            int edgeNGramMax = extendedSearchSettings.getEdgeNGramMax();

            String prefixFilterName = extendedSearchSettings.getPrefixFilter();
            String prefixAnalyzerName = extendedSearchSettings.getPrefixAnalyzer();
            String prefixSearchAnalyzerName = extendedSearchSettings.getPrefixSearchAnalyzer();
            String prefixTokenizerName = extendedSearchSettings.getTokenizer();

            IndexSettingsAnalysis.Builder analysisBuilder = context.getEntityAnalysisBuilder(indexConfiguration.getEntityClass());
            analysisBuilder
                    .filter(prefixFilterName, filterBuilder ->
                            filterBuilder.definition(filterDefinitionBuilder ->
                                    filterDefinitionBuilder.edgeNgram(edgeNGramFilterBuilder ->
                                            edgeNGramFilterBuilder
                                                    .minGram(edgeNGramMin)
                                                    .maxGram(edgeNGramMax)
                                    )
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
                    );
        }
    }
}
